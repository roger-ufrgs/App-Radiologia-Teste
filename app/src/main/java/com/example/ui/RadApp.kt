package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Exam
import com.example.model.ExamCategory
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.RadViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadApp(viewModel: RadViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        XRayLogoCanvas(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = "Rad Assistente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Técnicas & Parâmetros",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (currentTab == 2) {
                        IconButton(
                            onClick = { viewModel.clearChat() },
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Limpar conversa",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Protocolos") },
                    label = { Text("Protocolos", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_protocols")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Calculadora") },
                    label = { Text("Calculadora", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_calculator")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Consultoria") },
                    label = { Text("Consultoria IA", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_chat")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                0 -> ProtocolosScreen(viewModel)
                1 -> CalculadoraScreen(viewModel)
                2 -> ChatIaScreen(viewModel)
            }
        }
    }
}

// --- CANVAS DECORATION: CHEST XRAY ---
@Composable
fun XRayLogoCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f
        val r = minOf(width, height) / 2f

        // Glowing circle background
        drawCircle(
            color = Color(0xFF0F172A),
            radius = r,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFF0096C7).copy(alpha = 0.25f),
            radius = r - 2f,
            center = Offset(cx, cy)
        )

        // Spine (Coluninha)
        val spineW = r * 0.14f
        val spineH = r * 1.4f
        drawRect(
            color = Color(0xFF94A3B8).copy(alpha = 0.8f),
            topLeft = Offset(cx - spineW / 2f, cy - spineH / 2f),
            size = Size(spineW, spineH)
        )

        // Ribs (Costelas)
        val ribStroke = r * 0.06f
        for (i in 0..4) {
            val ribY = cy - r * 0.5f + (i * r * 0.22f)
            
            // Left Arc
            val leftRib = Path().apply {
                moveTo(cx - spineW / 2f, ribY)
                quadraticTo(
                    cx - r * 0.7f, ribY + r * 0.08f,
                    cx - r * 0.45f, ribY + r * 0.22f
                )
            }
            drawPath(
                path = leftRib,
                color = Color(0xFFCBD5E1).copy(alpha = 0.7f),
                style = Stroke(width = ribStroke, cap = StrokeCap.Round)
            )

            // Right Arc
            val rightRib = Path().apply {
                moveTo(cx + spineW / 2f, ribY)
                quadraticTo(
                    cx + r * 0.7f, ribY + r * 0.08f,
                    cx + r * 0.45f, ribY + r * 0.22f
                )
            }
            drawPath(
                path = rightRib,
                color = Color(0xFFCBD5E1).copy(alpha = 0.7f),
                style = Stroke(width = ribStroke, cap = StrokeCap.Round)
            )
        }
    }
}

// ==========================================
// SCREEN 1: EXAM CATALOG (PROTOCOLOS)
// ==========================================
@Composable
fun ProtocolosScreen(viewModel: RadViewModel) {
    val searchText by viewModel.searchText.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredExams by viewModel.filteredExams.collectAsState()
    val selectedExam by viewModel.selectedExam.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { viewModel.onSearchTextChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field"),
            placeholder = { Text("Buscar exames (ex: tórax, mão, fêmur...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchTextChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Chips
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "Todos" chip
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text("Todos") },
                        modifier = Modifier.testTag("category_all")
                    )

                    // Categories (Row scrolling or wrapped. We have space so let's do a scrollable Row)
                    val categories = ExamCategory.values()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.take(3).forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category.displayName,
                                    onClick = { viewModel.onCategorySelected(category.displayName) },
                                    label = { Text(category.displayName) },
                                    modifier = Modifier.testTag("category_${category.name.lowercase()}")
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.drop(3).forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category.displayName,
                                    onClick = { viewModel.onCategorySelected(category.displayName) },
                                    label = { Text(category.displayName) },
                                    modifier = Modifier.testTag("category_${category.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results Title
        Text(
            text = "Exames Disponíveis (${filteredExams.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Exams List
        if (filteredExams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Não encontrado",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum exame encontrado para sua busca.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredExams) { exam ->
                    ExamCard(exam = exam, onClick = { viewModel.selectExam(exam) })
                }
            }
        }
    }

    // Exam details dialog
    selectedExam?.let { exam ->
        ExamDetailDialog(exam = exam, onDismiss = { viewModel.selectExam(null) })
    }
}

@Composable
fun ExamCard(exam: Exam, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("exam_card_${exam.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exam.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Category Tag
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = exam.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Highlight essential parameters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ParamBadge(label = "kV de Ref.", value = "${exam.kV} kV")
                ParamBadge(label = "mAs de Ref.", value = "${exam.mAs} mAs")
                ParamBadge(label = "Chassis", value = exam.chassis.split(" ")[0])
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exam.indications,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ParamBadge(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailDialog(exam: Exam, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exam.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = exam.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_exam_details")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable details content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section 1: Technical parameters
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "PARÂMETROS DE EXPOSIÇÃO RECOMENDADOS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    DetailParamItem(title = "Tensão (kV)", value = "${exam.kV} kV", icon = Icons.Default.Info)
                                    DetailParamItem(title = "Carga (mAs)", value = "${exam.mAs} mAs", icon = Icons.Default.PlayArrow)
                                    DetailParamItem(
                                        title = "Grade", 
                                        value = if (exam.grid) "Sim (Bucky)" else "Não", 
                                        icon = if (exam.grid) Icons.Default.Check else Icons.Default.Close
                                    )
                                }
                            }
                        }
                    }

                    // Section 2: Chassis and Setup
                    item {
                        Column {
                            SectionHeader(title = "Geometria e Receptor de Imagem")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Tamanho do Filme/Chassis: ",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(text = exam.chassis, style = MaterialTheme.typography.bodyMedium)
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Distância Foco-Filme (DFOFI): ",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(text = exam.distance, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Section 3: Patient positioning
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                SectionHeader(title = "Posicionamento do Paciente")
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = exam.positioning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    // Section 4: Central Ray
                    item {
                        Column {
                            SectionHeader(title = "Raio Central (RC)")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = exam.centralRay,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Section 5: Breathing
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Instruções de respiração",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Instrução de Respiração",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = exam.breathing,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Section 6: Indications
                    item {
                        Column {
                            SectionHeader(title = "Indicações Clínicas Comuns")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = exam.indications,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun DetailParamItem(title: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}


// ==========================================
// SCREEN 2: EXPOSURE CALCULATOR
// ==========================================
@Composable
fun CalculadoraScreen(viewModel: RadViewModel) {
    val thickness by viewModel.thicknessCm.collectAsState()
    val constant by viewModel.equipmentConstant.collectAsState()
    val hasGrid by viewModel.hasGrid.collectAsState()
    val regionType by viewModel.regionType.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable { focusManager.clearFocus() } // Hide keyboard on tap outside
    ) {
        Text(
            text = "Calculadora kV & mAs",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Baseado na fórmula de quilovoltagem: kV = (2 x Espessura) + Constante",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Preset Region Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetChip(
                selected = regionType == "membros",
                label = "Membros (Sem Grade)",
                onClick = { viewModel.selectRegionType("membros") },
                modifier = Modifier.weight(1f).testTag("preset_membros")
            )
            PresetChip(
                selected = regionType == "torax",
                label = "Tórax AP/PA",
                onClick = { viewModel.selectRegionType("torax") },
                modifier = Modifier.weight(1f).testTag("preset_torax")
            )
            PresetChip(
                selected = regionType == "abdomen_coluna",
                label = "Abdômen/Coluna",
                onClick = { viewModel.selectRegionType("abdomen_coluna") },
                modifier = Modifier.weight(1f).testTag("preset_abdomen")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DADOS DA ESTRUTURA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Thickness Input
                OutlinedTextField(
                    value = thickness,
                    onValueChange = { viewModel.updateThickness(it) },
                    label = { Text("Espessura Anatômica (cm)") },
                    placeholder = { Text("Ex: 15") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("thickness_input"),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { Text("cm", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Equipment Constant Input
                OutlinedTextField(
                    value = constant,
                    onValueChange = { viewModel.updateConstant(it) },
                    label = { Text("Constante do Aparelho (C)") },
                    placeholder = { Text("Comum: 30 ou 40") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("constant_input"),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { Text("C", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Grid Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Grade Antidifusora (Bucky)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Aumenta o mAs (geralmente 4x) para compensar a absorção de radiação secundária.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hasGrid,
                        onCheckedChange = { viewModel.toggleGrid(it) },
                        modifier = Modifier.testTag("grid_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Output Console: High tech look
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A) // Dark space background
            ),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(Color(0xFF0096C7)) // Glowing cyan border
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CONSOLE DE EXPOSIÇÃO DIGITAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0096C7),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // KV Output
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TENSÃO", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "${viewModel.getCalculatedKV()}",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(text = "kV", fontSize = 12.sp, color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                    }

                    // Divide line
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .background(Color(0xFF1E293B))
                    )

                    // MAs Output
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "EXPOSIÇÃO", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "${viewModel.getCalculatedMAs()}",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(text = "mAs", fontSize = 12.sp, color = Color(0xFF00B4D8), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Color(0xFF1E293B))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Radiação ativa",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Parâmetros sugeridos para aparelhos calibrados de alta frequência.",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
fun PresetChip(selected: Boolean, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) null else CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}


// ==========================================
// SCREEN 3: AI CHAT CONSULTING
// ==========================================
@Composable
fun ChatIaScreen(viewModel: RadViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    val error by viewModel.chatError.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Auto-scroll chat to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Chat list area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            items(messages) { message ->
                ChatBubble(message = message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Rad Assistente está formulando técnica...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // Quick Suggestion Prompts
        if (messages.size <= 1) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "DÚVIDAS FREQUENTES (Toque para enviar):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                val suggestions = listOf(
                    "Como aplicar o efeito talão na coluna torácica?",
                    "Ajuste de dose para tórax AP em recém-nascidos",
                    "Como fazer perfil de fêmur em paciente com trauma?",
                    "Fórmula de KV na radiologia para que serve?"
                )

                suggestions.forEach { suggestion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.sendChatMessage(suggestion)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Input send bar
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = { Text("Faça uma pergunta sobre radiologia...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (inputText.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("chat_send_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val containerColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }
    val contentColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = contentColor,
                    lineHeight = 20.sp
                )
            }
        }
        
        Text(
            text = if (message.isUser) "Profissional" else "Rad Assistente IA",
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}
