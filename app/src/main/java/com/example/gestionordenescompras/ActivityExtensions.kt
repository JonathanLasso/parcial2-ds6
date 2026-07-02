package com.example.gestionordenescompras

import android.app.Activity
import android.content.Intent

fun Activity.configurarBotonRegresar(boton: android.view.View, destino: Class<*>){
    boton.setOnClickListener {
        val intent = Intent(this,destino)
        startActivity(intent)
        finish()
    }
}