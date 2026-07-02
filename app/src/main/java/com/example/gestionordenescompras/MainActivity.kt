package com.example.gestionordenescompras

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenescompras.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configuracionBotones()
    }

    private fun configuracionBotones(){
        binding.btnCliente.setOnClickListener {
            val intent = Intent(this, GestionClientesActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnProducto.setOnClickListener {
            val intent = Intent(this, GestionProductosActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnOrden.setOnClickListener {
            val intent = Intent(this, GestionOrdenesActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnSalirApp.setOnClickListener {
            finishAffinity()
        }
    }
}