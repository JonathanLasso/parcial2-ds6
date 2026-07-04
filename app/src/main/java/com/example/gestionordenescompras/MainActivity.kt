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
        binding.cardClientes.setOnClickListener {
            val intent = Intent(this, GestionClientesActivity::class.java)
            startActivity(intent)
        }
        binding.cardProductos.setOnClickListener {
            val intent = Intent(this, GestionProductosActivity::class.java)
            startActivity(intent)
        }
        binding.cardOrdenes.setOnClickListener {
            val intent = Intent(this, GestionOrdenesActivity::class.java)
            startActivity(intent)
        }
        binding.cardSalir.setOnClickListener {
            finishAffinity()
        }
    }
}