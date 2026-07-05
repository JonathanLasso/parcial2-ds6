package com.example.gestionordenescompras

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AlertDialog

fun Activity.configurarBotonRegresar(boton: android.view.View, destino: Class<*>){
    boton.setOnClickListener {
        val intent = Intent(this,destino)
        startActivity(intent)
        finish()
    }
}
fun Activity.mostrarDialogoConfirmacion(
    id: Int,
    nombre: String? = null,
    funcionEliminar: (Int) -> Unit
) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Eliminar")

    // Personalizamos el mensaje dinámicamente según la Activity que invoque la función
    val mensaje = when (this) {
        // Si se llama desde la pantalla de Clientes
        is GestionClientesActivity -> "¿Estás seguro de que deseas eliminar al cliente $nombre de la base de datos?"

        // Si se llama desde la pantalla de Productos
        is GestionProductosActivity -> "¿Estás seguro de que deseas eliminar el producto $nombre (ID: $id)?"

        // Si se llama desde la pantalla de Pedidos (u otra donde nombre sea null)
        else -> "¿Estás seguro de que deseas eliminar el pedido N° $id?"
    }

    builder.setMessage(mensaje)

    builder.setPositiveButton("Eliminar") { dialog, _ ->
        funcionEliminar(id)
        dialog.dismiss()
    }

    builder.setNegativeButton("Cancelar") { dialog, _ ->
        dialog.dismiss()
    }

    builder.create().show()
}