package com.example.gestionordenescompras

import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenescompras.databinding.ActivityActualizarProductoBinding

class ActualizarProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActualizarProductoBinding
    private var productoId: Int = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActualizarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        productoId = intent.getIntExtra("PRODUCTO_ID", -1)
        
        if (productoId != -1) {
            cargarDatosProducto()
            binding.tvTituloPantalla.text = "Actualizar Producto"
            binding.btnActualizar.text = "Actualizar"
        } else {
            binding.tvTituloPantalla.text = "Registrar Producto"
            binding.btnActualizar.text = "Guardar"
        }
        
        configurarBotonAccion()
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun cargarDatosProducto() {
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM productos WHERE id = ?", arrayOf(productoId.toString()))
        if (cursor.moveToFirst()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombreProducto"))
            val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
            val precio = cursor.getString(cursor.getColumnIndexOrThrow("precio"))
            binding.etNombre.setText(nombre)
            binding.etDescripcion.setText(descripcion)
            binding.etPrecio.setText(precio)
        }
        cursor.close()
        db.close()
    }
    
    private fun configurarBotonAccion() {
        binding.btnActualizar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val precioStr = binding.etPrecio.text.toString().trim()

            if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty()) {
                Toast.makeText(this, "No se permiten campos vacíos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val precio = precioStr.toDoubleOrNull() ?: 0.0
            if (precio <= 0) {
                Toast.makeText(this, "El precio debe ser mayor a cero.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val admin = AdministradorBD(this)
            val db = admin.writableDatabase

            val valores = ContentValues().apply {
                put("nombreProducto", nombre)
                put("descripcion", descripcion)
                put("precio", precio)
            }

            if (productoId != -1) {
                // Modo Actualizar
                val filasAfectadas = db.update("productos", valores, "id = ?", arrayOf(productoId.toString()))
                if (filasAfectadas > 0) {
                    Toast.makeText(this, "Producto actualizado con éxito.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar los datos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Modo Registrar
                val resultado = db.insert("productos", null, valores)
                if (resultado != -1L) {
                    Toast.makeText(this, "Producto registrado con éxito.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar el producto.", Toast.LENGTH_SHORT).show()
                }
            }
            db.close()
        }
    }
}