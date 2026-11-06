package com.example.mineguard.model

data class Inspection(
    val id: Long = 0,
    val mineName: String,
    val inspectorName: String,
    val category: String,
    val notes: String,
    val riskScore: Int,
    val riskLevel: String,
    val status: String = "Submitted",
    val createdAt: String
)
