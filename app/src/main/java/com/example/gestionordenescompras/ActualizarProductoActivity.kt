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
        if(productoId != -1){
            cargarDatosProducto()
        }
        else{
            Toast.makeText(this, "Error al cargar el producto", Toast.LENGTH_SHORT).show()
            finish()
        }
        actualizarProducto()
        configurarBotonRegresar(binding.btnSalir, GestionProductosActivity::class.java)
    }
    private fun cargarDatosProducto(){
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM productos WHERE id = ?",
            arrayOf(productoId.toString())
        )
        if (cursor.moveToFirst()) {
            // Extraer datos usando el índice de la columna
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombreProducto"))
            val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
            val precio = cursor.getString(cursor.getColumnIndexOrThrow("precio"))
            val cantidad = cursor.getString(cursor.getColumnIndexOrThrow("cantidad"))
            binding.etNombre.setText(nombre)
            binding.etDescripcion.setText(descripcion)
            binding.etPrecio.setText(precio)
            binding.etCantidad.setText(cantidad)
        } else {
            Toast.makeText(this, "No se encontró el producto.", Toast.LENGTH_SHORT).show()
            finish()
        }
        cursor.close()
        db.close()
    }
    private fun actualizarProducto(){
        binding.btnActualizar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val precio = binding.etPrecio.text.toString().toDouble()
            val cantidad = binding.etCantidad.text.toString().toInt()

            if (nombre.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "No se permiten campos vacíos.", Toast.LENGTH_SHORT).show()
            }
            else if (precio <= 0.00 || cantidad <= 0){
                Toast.makeText(this, "No se permiten precios o cantidades negativas o igual a cero.", Toast.LENGTH_SHORT).show()
            }
            else{
                val admin = AdministradorBD(this)
                val db = admin.writableDatabase

                val valoresNuevos = ContentValues().apply {
                    put("nombreProducto", nombre)
                    put("descripcion", descripcion)
                    put("precio", precio)
                    put("cantidad",cantidad)
                }
                // Modificar la fila correspondiente en la base de datos
                val filasAfectadas = db.update(
                    "productos",
                    valoresNuevos,
                    "id = ?",
                    arrayOf(productoId.toString())
                )
                db.close()
                if (filasAfectadas > 0) {
                    Toast.makeText(this, "Producto actualizado con éxito.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar los datos.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}