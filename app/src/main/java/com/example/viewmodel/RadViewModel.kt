package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GeminiClient
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.data.ExamData
import com.example.model.Exam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class RadViewModel : ViewModel() {

    // --- Tab / Navigation State ---
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    // --- Search & Catalog State ---
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _filteredExams = MutableStateFlow<List<Exam>>(ExamData.list)
    val filteredExams: StateFlow<List<Exam>> = _filteredExams.asStateFlow()

    private val _selectedExam = MutableStateFlow<Exam?>(null)
    val selectedExam: StateFlow<Exam?> = _selectedExam.asStateFlow()

    init {
        updateFilteredExams()
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
        updateFilteredExams()
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
        updateFilteredExams()
    }

    fun selectExam(exam: Exam?) {
        _selectedExam.value = exam
    }

    private fun updateFilteredExams() {
        val query = _searchText.value.lowercase().trim()
        val cat = _selectedCategory.value

        _filteredExams.value = ExamData.list.filter { exam ->
            val matchesSearch = query.isEmpty() ||
                    exam.name.lowercase().contains(query) ||
                    exam.positioning.lowercase().contains(query) ||
                    exam.indications.lowercase().contains(query)
            val matchesCategory = cat == null || exam.category == cat
            matchesSearch && matchesCategory
        }
    }

    // --- Exposure Calculator State ---
    private val _thicknessCm = MutableStateFlow("15") // Default body part thickness (e.g. knee or skull)
    val thicknessCm: StateFlow<String> = _thicknessCm.asStateFlow()

    private val _equipmentConstant = MutableStateFlow("30") // Default equipment constant (commonly 30 or 40)
    val equipmentConstant: StateFlow<String> = _equipmentConstant.asStateFlow()

    private val _hasGrid = MutableStateFlow(true) // Whether grid is used
    val hasGrid: StateFlow<Boolean> = _hasGrid.asStateFlow()

    private val _regionType = MutableStateFlow("membros") // 'membros', 'torax', 'abdomen_coluna'
    val regionType: StateFlow<String> = _regionType.asStateFlow()

    fun updateThickness(value: String) {
        // Allow empty string so user can clear, but parse carefully
        _thicknessCm.value = value.filter { it.isDigit() }
    }

    fun updateConstant(value: String) {
        _equipmentConstant.value = value.filter { it.isDigit() }
    }

    fun toggleGrid(value: Boolean) {
        _hasGrid.value = value
    }

    fun selectRegionType(type: String) {
        _regionType.value = type
        // Auto adjust grid default based on region
        when (type) {
            "membros" -> {
                _hasGrid.value = false
                _thicknessCm.value = "10"
            }
            "torax" -> {
                _hasGrid.value = true
                _thicknessCm.value = "22"
            }
            "abdomen_coluna" -> {
                _hasGrid.value = true
                _thicknessCm.value = "20"
            }
        }
    }

    // Calculated Exposure values
    // kV = 2 * thickness + constant
    // mAs recommended is based on body part and grid factor
    fun getCalculatedKV(): Int {
        val thickness = _thicknessCm.value.toIntOrNull() ?: 0
        val constant = _equipmentConstant.value.toIntOrNull() ?: 0
        val baseKV = (2 * thickness) + constant
        // kV must be within clinical limits (typically 40 to 125 kV)
        return baseKV.coerceIn(40, 125)
    }

    fun getCalculatedMAs(): Double {
        val thickness = _thicknessCm.value.toIntOrNull() ?: 0
        if (thickness <= 0) return 0.0

        val baseMAs = when (_regionType.value) {
            "membros" -> {
                if (thickness < 8) 1.5 else 3.0
            }
            "torax" -> {
                if (thickness < 18) 2.0 else 4.0
            }
            "abdomen_coluna" -> {
                if (thickness < 18) 16.0 else 28.0
            }
            else -> 4.0
        }

        // Apply grid factor (generally 4x if grid is used, sem grade is 1x)
        val factor = if (_hasGrid.value) 4.0 else 1.0
        val finalMAs = baseMAs * factor

        // Format to 1 decimal place
        return (finalMAs * 10.0).roundToInt() / 10.0
    }


    // --- AI Chat Consulting State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            content = "Olá! Sou o Rad Assistente. Posso ajudar você com dúvidas sobre posicionamento de pacientes, ajustes de técnicas para pediatria/geriatria, efeito talão, ou outras questões de radiologia clínica. Como posso ajudar hoje?",
            isUser = false
        )
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    fun sendChatMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return

        val userMsg = ChatMessage(content = messageText, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true
        _chatError.value = null

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        content = "Erro: Chave de API do Gemini não configurada. Por favor, adicione sua chave nas configurações do AI Studio (Secrets panel) usando o nome GEMINI_API_KEY.",
                        isUser = false
                    )
                    _isChatLoading.value = false
                    return@launch
                }

                // Prepare system instruction
                val systemInstruction = Content(
                    parts = listOf(Part(text = """
                        Você é o 'Rad Assistente', um assistente virtual inteligente especialista em Técnicas, Parâmetros de Exposição e Posicionamentos em Radiologia Médica Convencional. Seu público-alvo são técnicos e tecnólogos em radiologia em ambiente de trabalho.
                        
                        Diretrizes de resposta:
                        1. Responda sempre em PORTUGUÊS.
                        2. Seja conciso, extremamente clínico, preciso e objetivo. Evite enrolações desnecessárias, pois o profissional pode estar no meio de um plantão de exames.
                        3. Use tópicos (marcadores / bullets) e termos em negrito para destacar KV, MAS, INCIDÊNCIA e POSICIONAMENTO.
                        4. Dê conselhos práticos e realistas (ex: o que fazer se o paciente não puder ficar em pé, como ajustar dose para gesso ou tração, etc.).
                        5. Se não souber algo relacionado a diagnósticos médicos profundos, recomende cautela e encaminhamento ao médico radiologista, pois seu foco é estritamente a aquisição da imagem técnica.
                    """.trimIndent()))
                )

                // Gather conversation history (limit to last 10 messages for context)
                val chatHistory = _chatMessages.value.takeLast(10).map { msg ->
                    Content(parts = listOf(Part(text = msg.content)))
                }

                val request = GenerateContentRequest(
                    contents = chatHistory,
                    systemInstruction = systemInstruction
                )

                val response = GeminiClient.service.generateContent(apiKey, request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Desculpe, não consegui obter uma resposta do assistente de IA."

                _chatMessages.value = _chatMessages.value + ChatMessage(
                    content = replyText,
                    isUser = false
                )
            } catch (e: Exception) {
                _chatError.value = e.message
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    content = "Desculpe, ocorreu um erro de conexão. Verifique sua conexão com a internet ou as credenciais de API. Detalhes: ${e.localizedMessage}",
                    isUser = false
                )
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                content = "Histórico redefinido. Como posso ajudar com novas dúvidas radiológicas?",
                isUser = false
            )
        )
    }
}
