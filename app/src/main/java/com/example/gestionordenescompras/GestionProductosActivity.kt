package com.example.gestionordenescompras

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.database.Cursor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import com.example.gestionordenescompras.adapter.ProductosAdapter
import com.example.gestionordenescompras.databinding.ActivityGestionProductosBinding

class GestionProductosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGestionProductosBinding
    private lateinit var productosAdapter : ProductosAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Navigation back
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        configurarListaProductos()
        configurarAcciones()
    }
    
    override fun onResume() {
        super.onResume()
        refrescarListaProductos()
    }
    
    private fun refrescarListaProductos() {
        if (::productosAdapter.isInitialized) {
            val nuevoCursor = obtenerCursorProductos(binding.etBuscar.text.toString())
            if (nuevoCursor.count == 0) {
                binding.rvProductos.visibility = android.view.View.GONE
                binding.tvListaVacia.visibility = android.view.View.VISIBLE
            } else {
                binding.rvProductos.visibility = android.view.View.VISIBLE
                binding.tvListaVacia.visibility = android.view.View.GONE
            }
            productosAdapter.cambiarCursor(nuevoCursor)
        }
    }
    
    private fun configurarAcciones() {
        binding.fabAgregar.setOnClickListener {
            val intent = Intent(this, ActualizarProductoActivity::class.java).apply {
                putExtra("PRODUCTO_ID", -1)
            }
            startActivity(intent)
        }
        
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refrescarListaProductos()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun configurarListaProductos(){
        productosAdapter = ProductosAdapter(
            cursor = obtenerCursorProductos(""),
            onProductoClick = { idProducto ->
                val intent = Intent(this, ActualizarProductoActivity::class.java).apply {
                    putExtra("PRODUCTO_ID", idProducto)
                }
                startActivity(intent)
            },
            onEliminarClick = {idProducto, nombreProducto ->
                mostrarDialogoConfirmacion(idProducto, nombreProducto){
                    eliminarProducto(idProducto)
                }
            }
        )
        binding.rvProductos.apply {
            layoutManager = LinearLayoutManager(this@GestionProductosActivity)
            adapter = productosAdapter
        }
    }
    
    private fun obtenerCursorProductos(filtro: String): Cursor {
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        return if (filtro.isEmpty()) {
            db.rawQuery("SELECT * FROM productos", null)
        } else {
            db.rawQuery("SELECT * FROM productos WHERE nombreProducto LIKE ?", arrayOf("%$filtro%"))
        }
    }
    
    private fun eliminarProducto(id: Int){
        val admin = AdministradorBD(this)
        val db = admin.writableDatabase
        val filasEliminadas = db.delete("productos", "id = ?", arrayOf(id.toString()))
        db.close()
        if (filasEliminadas > 0) {
            Toast.makeText(this, "Producto eliminado con éxito.", Toast.LENGTH_SHORT).show()
            refrescarListaProductos()
        } else {
            Toast.makeText(this, "Error al intentar eliminar un producto.", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        val admin = AdministradorBD(this)
        admin.close()
    }
}