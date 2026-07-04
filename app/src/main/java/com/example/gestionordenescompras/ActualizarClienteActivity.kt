package com.example.gestionordenescompras

import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionordenescompras.databinding.ActivityActualizarClienteBinding

class ActualizarClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActualizarClienteBinding
    private var clienteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActualizarClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clienteId = intent.getIntExtra("CLIENTE_ID", -1)

        if (clienteId != -1) {
            cargarDatosCliente()
            binding.tvTituloPantalla.text = "Actualizar Cliente"
            binding.btnActualizar.text = "Actualizar"
        } else {
            binding.tvTituloPantalla.text = "Registrar Cliente"
            binding.btnActualizar.text = "Guardar"
        }
        
        configurarBotonAccion()
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun cargarDatosCliente() {
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM clientes WHERE id = ?", arrayOf(clienteId.toString()))

        if (cursor.moveToFirst()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"))
            val telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono"))
            val direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion"))
            binding.etNombre.setText(nombre)
            binding.etCorreo.setText(correo)
            binding.etTelefono.setText(telefono)
            binding.etDireccion.setText(direccion)
        }
        cursor.close()
        db.close()
    }

    private fun configurarBotonAccion() {
        binding.btnActualizar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val correo = binding.etCorreo.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val direccion = binding.etDireccion.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(this, "No se permiten campos vacíos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val admin = AdministradorBD(this)
            val db = admin.writableDatabase

            val valores = ContentValues().apply {
                put("nombre", nombre)
                put("correo", correo)
                put("telefono", telefono)
                put("direccion", direccion)
            }

            if (clienteId != -1) {
                // Modo Actualizar
                val filasAfectadas = db.update("clientes", valores, "id = ?", arrayOf(clienteId.toString()))
                if (filasAfectadas > 0) {
                    Toast.makeText(this, "Cliente actualizado con éxito.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar los datos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Modo Registrar
                val resultado = db.insert("clientes", null, valores)
                if (resultado != -1L) {
                    Toast.makeText(this, "Cliente registrado con éxito.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar el cliente.", Toast.LENGTH_SHORT).show()
                }
            }
            db.close()
        }
    }
}