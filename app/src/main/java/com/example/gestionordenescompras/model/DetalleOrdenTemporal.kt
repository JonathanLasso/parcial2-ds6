package com.example.gestionordenescompras.model

data class DetalleOrdenTemporal(
    val idProducto: Int,
    val nombreProducto: String,
    val cantidad: Int,
    val precio: Double,
    val subtotal: Double
)
