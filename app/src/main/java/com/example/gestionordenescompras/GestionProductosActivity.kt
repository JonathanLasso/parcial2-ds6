package com.example.gestionordenescompras

import android.os.Bundle
import android.widget.Toast
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenescompras.adapter.ProductosAdapter
import com.example.gestionordenescompras.databinding.ActivityGestionProductosBinding

class GestionProductosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGestionProductosBinding
    private lateinit var productosAdapter : ProductosAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarListaProductos()
        registrarProducto()
        configurarBotonRegresar(binding.btnRegresar, MainActivity::class.java)
    }
    // Para refrescar la lista al volver de otra pantalla
    override fun onResume() {
        super.onResume()
        refrescarListaProductos()
    }
    private fun obtenerCursorProductos(): Cursor{
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        return db.rawQuery("SELECT * FROM productos", null)
    }
    private fun refrescarListaProductos(){
        if(::productosAdapter.isInitialized){
            val nuevoCursor = obtenerCursorProductos()
            // Validamos si el cursor no contiene registros
            if(nuevoCursor.count == 0){
                binding.rvProductos.visibility = android.view.View.GONE
                binding.tvListaVacia.visibility = android.view.View.VISIBLE
            }
            else{
                binding.rvProductos.visibility = android.view.View.VISIBLE
                binding.tvListaVacia.visibility = android.view.View.GONE
            }
            productosAdapter.cambiarCursor(nuevoCursor)
        }
    }
    private fun registrarProducto(){
        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val descripcion = binding.etDescripcion.text.toString()
            val precio = binding.etPrecio.text.toString().toDouble()
            val cantidad = binding.etCantidad.text.toString().toInt()
            if(nombre.isEmpty() || descripcion.isEmpty()){
                Toast.makeText(applicationContext, "No se permiten campos vacios.", Toast.LENGTH_SHORT).show()
            }
            else if(precio <= 0.00 || cantidad <= 0){
                Toast.makeText(applicationContext, "No se permiten precios ni cantidades menores o iguales a 0.", Toast.LENGTH_SHORT).show()
            }
            else{
                val admin = AdministradorBD(this)
                val db = admin.writableDatabase
                val datos = ContentValues().apply {
                    put("nombreProducto",nombre)
                    put("descripcion",descripcion)
                    put("precio",precio)
                    put("cantidad",cantidad)
                }
                val resultado = db.insert("productos",null,datos)
                db.close()
                if(resultado != -1L){
                    Toast.makeText(applicationContext, "Datos guardados con éxito.", Toast.LENGTH_SHORT).show()
                    binding.etNombre.text?.clear()
                    binding.etDescripcion.text?.clear()
                    binding.etPrecio.text?.clear()
                    binding.etCantidad.text?.clear()
                    // Actualizamos el adaptador con un Cursor nuevo
                    refrescarListaProductos()
                }
                else{
                    Toast.makeText(applicationContext, "Los datos no se guardaron correctamente.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun configurarListaProductos(){
        // Inicializamos el Adapter pasándole el Cursor crudo
        productosAdapter = ProductosAdapter(
            cursor = obtenerCursorProductos(),
            onProductoClick = { idProducto ->
                val intent = Intent(this, ActualizarProductoActivity::class.java).apply {
                    putExtra("PRODUCTO_ID",idProducto)
                }
                startActivity(intent)
            },
            onEliminarClick = {idProducto, nombreProducto ->
                mostrarDialogoConfirmacion(idProducto,nombreProducto){
                    eliminarProducto(idProducto)
                }
            }
        )
        binding.rvProductos.apply {
            layoutManager = LinearLayoutManager(this@GestionProductosActivity)
            adapter = productosAdapter
        }
    }
    private fun eliminarProducto(id: Int){
        val admin = AdministradorBD(this)
        val db = admin.writableDatabase
        // Ejecutamos el delete usando la cláusula WHERE basada en el ID
        val filasEliminadas = db.delete("productos", "id = ?", arrayOf(id.toString()))
        db.close()

        if (filasEliminadas > 0) {
            Toast.makeText(this, "Producto eliminado con éxito.", Toast.LENGTH_SHORT).show()
            // Refrescamos la lista inmediatamente para que desaparezca visualmente
            refrescarListaProductos()
        } else {
            Toast.makeText(this, "Error al intentar eliminar un producto.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        val admin = AdministradorBD(this)
        admin.close() // Siempre cerrar la base de datos al destruir la actividad
    }
}