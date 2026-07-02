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
fun Activity.mostrarDialogoConfirmacion(id: Int, nombre: String, funcionEliminar: (Int) -> Unit) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Eliminar")
    builder.setMessage("¿Estás seguro de que deseas eliminar a $nombre de la base de datos?")

    // Si el usuario confirma la acción
    builder.setPositiveButton("Eliminar") { dialog, _ ->
        funcionEliminar(id) // Ejecuta la lógica de eliminación de esa Activity
        dialog.dismiss()
    }

    // Si el usuario cancela
    builder.setNegativeButton("Cancelar") { dialog, _ ->
        dialog.dismiss()
    }

    val dialog = builder.create()
    dialog.show()
}