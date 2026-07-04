package com.example.gestionordenescompras

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.database.Cursor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import com.example.gestionordenescompras.adapter.ClientesAdapter
import com.example.gestionordenescompras.databinding.ActivityGestionClientesBinding

class GestionClientesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGestionClientesBinding
    private lateinit var clientesAdapter: ClientesAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Navigation back
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        confugurarListaClientes()
        configurarAcciones()
    }
    
    override fun onResume() {
        super.onResume()
        refrescarListaClientes()
    }
    
    private fun refrescarListaClientes() {
        if (::clientesAdapter.isInitialized) {
            val nuevoCursor = obtenerCursorClientes(binding.etBuscar.text.toString())
            if (nuevoCursor.count == 0) {
                binding.rvClientes.visibility = android.view.View.GONE
                binding.tvListaVacia.visibility = android.view.View.VISIBLE
            } else {
                binding.rvClientes.visibility = android.view.View.VISIBLE
                binding.tvListaVacia.visibility = android.view.View.GONE
            }
            clientesAdapter.cambiarCursor(nuevoCursor)
        }
    }
    
    private fun configurarAcciones() {
        binding.fabAgregar.setOnClickListener {
            val intent = Intent(this, ActualizarClienteActivity::class.java).apply {
                putExtra("CLIENTE_ID", -1)
            }
            startActivity(intent)
        }
        
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refrescarListaClientes()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun confugurarListaClientes(){
        clientesAdapter = ClientesAdapter(
            cursor = obtenerCursorClientes(""),
            onClienteClick = { idCliente ->
                val intent = Intent(this, ActualizarClienteActivity::class.java).apply {
                    putExtra("CLIENTE_ID", idCliente)
                }
                startActivity(intent)
            },
            onEliminarClick = {idCliente, nombreCliente ->
                mostrarDialogoConfirmacion(idCliente, nombreCliente) {
                    eliminarCliente(idCliente)
                }
            }
        )
        binding.rvClientes.apply {
            layoutManager = LinearLayoutManager(this@GestionClientesActivity)
            adapter = clientesAdapter
        }
    }
    
    private fun obtenerCursorClientes(filtro: String): Cursor {
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        return if (filtro.isEmpty()) {
            db.rawQuery("SELECT * FROM clientes", null)
        } else {
            db.rawQuery("SELECT * FROM clientes WHERE nombre LIKE ?", arrayOf("%$filtro%"))
        }
    }
    
    private fun eliminarCliente(id: Int) {
        val admin = AdministradorBD(this)
        val db = admin.writableDatabase
        val filasEliminadas = db.delete("clientes", "id = ?", arrayOf(id.toString()))
        db.close()
        if (filasEliminadas > 0) {
            Toast.makeText(this, "Cliente eliminado con éxito.", Toast.LENGTH_SHORT).show()
            refrescarListaClientes()
        } else {
            Toast.makeText(this, "Error al intentar eliminar el cliente.", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        val admin = AdministradorBD(this)
        admin.close()
    }
}