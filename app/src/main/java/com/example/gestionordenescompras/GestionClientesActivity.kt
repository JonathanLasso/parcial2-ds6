package com.example.gestionordenescompras

import android.os.Bundle
import android.widget.Toast
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenescompras.adapter.ClientesAdapter
import com.example.gestionordenescompras.databinding.ActivityGestionClientesBinding

class GestionClientesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGestionClientesBinding
    private lateinit var clientesAdapter: ClientesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        confugurarListaClientes()
        registrarCliente()
        configurarBotonRegresar(binding.btnRegresar, MainActivity::class.java)
    }
    // Para refrescar la lista al volver de otra pantalla
    override fun onResume() {
        super.onResume()
        refrescarListaClientes()
    }
    // Función encargada de actualizar el adapter con datos frescos
    private fun refrescarListaClientes() {
        if (::clientesAdapter.isInitialized) {
            val nuevoCursor = obtenerCursorClientes()
            // Validamos si el cursor no contiene registros
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
    private fun registrarCliente(){
        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val correo = binding.etCorreo.text.toString()
            val telefono = binding.etTelefono.text.toString()
            val direccion = binding.etDireccion.text.toString()
            if(nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || direccion.isEmpty()){
                Toast.makeText(applicationContext, "No se permiten campos vacios.", Toast.LENGTH_SHORT).show()
            }
            else{
                val admin = AdministradorBD(this)
                val db = admin.writableDatabase
                val datos = ContentValues().apply {
                    put("nombre", nombre)
                    put("correo",correo)
                    put("telefono",telefono)
                    put("direccion",direccion)
                }
                val resultado = db.insert("clientes",null,datos)
                db.close()
                if(resultado != -1L){
                    Toast.makeText(applicationContext, "Datos guardados con éxito.", Toast.LENGTH_SHORT).show()
                    binding.etNombre.text.clear()
                    binding.etCorreo.text.clear()
                    binding.etTelefono.text.clear()
                    binding.etDireccion.text.clear()
                    // Actualizamos el Adapter con un Cursor nuevo
                    refrescarListaClientes()
                }
                else{
                    Toast.makeText(applicationContext, "Los datos no se guardaron correctamente.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun confugurarListaClientes(){
        // Inicializamos el Adapter pasándole el Cursor crudo
        clientesAdapter = ClientesAdapter(
            cursor = obtenerCursorClientes(),
            onClienteClick = { idCliente ->
                // Recibimos el ID y abrimos la nueva pantalla
                val intent = Intent(this, ActualizarClienteActivity::class.java).apply {
                    putExtra("CLIENTE_ID", idCliente)
                }
                startActivity(intent)
            },
            onEliminarClick = {idCliente, nombreCliente ->
                mostrarDialogoConfirmacion(idCliente,nombreCliente) {
                    eliminarCliente(idCliente)
                }
            }
        )
        binding.rvClientes.apply {
            layoutManager = LinearLayoutManager(this@GestionClientesActivity)
            adapter = clientesAdapter
        }
    }
    private fun obtenerCursorClientes(): Cursor {
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        return db.rawQuery("SELECT * FROM clientes", null)
    }
    private fun eliminarCliente(id: Int) {
        val admin = AdministradorBD(this)
        val db = admin.writableDatabase

        // Ejecutamos el delete usando la cláusula WHERE basada en el ID
        val filasEliminadas = db.delete("clientes", "id = ?", arrayOf(id.toString()))
        db.close()

        if (filasEliminadas > 0) {
            Toast.makeText(this, "Cliente eliminado con éxito.", Toast.LENGTH_SHORT).show()
            // Refrescamos la lista inmediatamente para que desaparezca visualmente
            refrescarListaClientes()
        } else {
            Toast.makeText(this, "Error al intentar eliminar el cliente.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        val admin = AdministradorBD(this)
        admin.close() // Siempre cerrar la base de datos al destruir la actividad
    }

}