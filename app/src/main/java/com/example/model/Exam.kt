package com.example.model

data class Exam(
    val id: String,
    val name: String,
    val category: String,
    val kV: Int,
    val mAs: Double,
    val chassis: String,
    val distance: String,
    val grid: Boolean,
    val breathing: String,
    val positioning: String,
    val centralRay: String,
    val indications: String
)

enum class ExamCategory(val displayName: String) {
    TORAX("Tórax"),
    ABDOMEN("Abdômen"),
    COLUNA("Coluna"),
    CRANIO("Crânio"),
    MEMBROS_SUP("Membros Sup."),
    MEMBROS_INF("Membros Inf.")
}
