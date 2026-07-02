package com.example.gestionordenescompras

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionordenescompras.adapter.DetalleOrdenAdapter
import com.example.gestionordenescompras.databinding.ActivityActualizarOrdenBinding
import com.example.gestionordenescompras.model.DetalleOrdenTemporal
import java.util.Locale

class ActualizarOrdenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActualizarOrdenBinding
    private lateinit var detalleAdapter: DetalleOrdenAdapter
    private var ordenId: Int = -1
    private val listaDetallesEdicion = mutableListOf<DetalleOrdenTemporal>()
    private var totalGeneralOrden: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActualizarOrdenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ordenId = intent.getIntExtra("ORDEN_ID", -1)
        if (ordenId != -1) {
            cargarSpinnersModificacion()
            cargarDatosCabeceraOrden()
            configurarListaEdicion()
            cargarDetallesExistentesDB()
            configurarAccionesEdicion()
            binding.btnRegresar.setOnClickListener {
                val intent = Intent(this, GestionOrdenesActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, "Error al cargar la orden.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cargarSpinnersModificacion() {
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        // Conservamos id AS _id para que coincida perfectamente con el comportamiento del Adapter
        val cursorClientes = db.rawQuery("SELECT id AS _id, nombre FROM clientes", null)
        val adapterClientes = SimpleCursorAdapter(
            this, android.R.layout.simple_spinner_item, cursorClientes,
            arrayOf("nombre"),
            intArrayOf(android.R.id.text1), 0)
        binding.spClientesModificar.adapter = adapterClientes
        val cursorProductos = db.rawQuery("SELECT id AS _id, nombreProducto FROM productos", null)
        val adapterProductos = SimpleCursorAdapter(
            this, android.R.layout.simple_spinner_item, cursorProductos,
            arrayOf("nombreProducto"),
            intArrayOf(android.R.id.text1), 0)
        binding.spProductosModificar.adapter = adapterProductos
    }

    private fun cargarDatosCabeceraOrden() {
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ordenes WHERE id = ?", arrayOf(ordenId.toString()))
        if (cursor.moveToFirst()) {
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
            val idCliente = cursor.getInt(cursor.getColumnIndexOrThrow("idCliente"))
            binding.etFechaModificar.setText(fecha)
            val adapter = binding.spClientesModificar.adapter as SimpleCursorAdapter
            for (i in 0 until adapter.count) {
                val itemCursor = adapter.getItem(i) as Cursor
                if (itemCursor.getInt(itemCursor.getColumnIndexOrThrow("_id")) == idCliente) {
                    binding.spClientesModificar.setSelection(i)
                    break
                }
            }
        }
        cursor.close()
        db.close()
    }
    private fun configurarListaEdicion() {
        detalleAdapter = DetalleOrdenAdapter(listaDetallesEdicion)
        binding.rvDetallesModificar.apply {
            layoutManager = LinearLayoutManager(this@ActualizarOrdenActivity)
            adapter = detalleAdapter
        }
    }
    private fun cargarDetallesExistentesDB() {
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        val consulta = """
            SELECT d.idProducto, p.nombreProducto, d.cantidad, p.precio 
            FROM detalleOrden d 
            INNER JOIN productos p ON d.idProducto = p.id 
            WHERE d.idOrden = ?
        """.trimIndent()
        val cursor = db.rawQuery(consulta, arrayOf(ordenId.toString()))
        listaDetallesEdicion.clear()
        totalGeneralOrden = 0.0
        if (cursor.moveToFirst()) {
            do {
                val idProducto = cursor.getInt(cursor.getColumnIndexOrThrow("idProducto"))
                val nombreProducto = cursor.getString(cursor.getColumnIndexOrThrow("nombreProducto"))
                val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
                val subtotal = precio * cantidad

                listaDetallesEdicion.add(DetalleOrdenTemporal(idProducto, nombreProducto, cantidad, precio, subtotal))
                totalGeneralOrden += subtotal
            } while (cursor.moveToNext())
        }
        // Al iniciar la pantalla sí es correcto notificar cambios generales
        detalleAdapter.notifyDataSetChanged()
        binding.tvTotalGeneralModificar.text = String.format(Locale.US, "$%.2f", totalGeneralOrden)
        cursor.close()
        db.close()
    }
    private fun configurarAccionesEdicion() {
        binding.btnAgregarProductoModificar.setOnClickListener {
            val cantidadStr = binding.etCantidadModificar.text.toString()
            if (cantidadStr.isEmpty() || cantidadStr.toInt() <= 0) return@setOnClickListener
            val cantidad = cantidadStr.toInt()
            val cursorProducto = binding.spProductosModificar.selectedItem as Cursor
            val idProducto = cursorProducto.getInt(cursorProducto.getColumnIndexOrThrow("_id"))
            val nombreProducto = cursorProducto.getString(cursorProducto.getColumnIndexOrThrow("nombreProducto"))
            val admin = AdministradorBD(this)
            val db = admin.readableDatabase
            val c = db.rawQuery("SELECT precio FROM productos WHERE id = ?", arrayOf(idProducto.toString()))
            if (c.moveToFirst()) {
                val precio = c.getDouble(c.getColumnIndexOrThrow("precio"))
                val subtotal = precio * cantidad
                listaDetallesEdicion.add(DetalleOrdenTemporal(idProducto, nombreProducto, cantidad, precio, subtotal))
                detalleAdapter.notifyItemInserted(listaDetallesEdicion.size - 1)
                totalGeneralOrden += subtotal
                binding.tvTotalGeneralModificar.text = String.format(Locale.US, "$%.2f", totalGeneralOrden)
                binding.etCantidadModificar.text.clear()
            }
            c.close()
            db.close()
        }
        binding.btnActualizarOrden.setOnClickListener {
            val nuevaFecha = binding.etFechaModificar.text.toString().trim()
            if (listaDetallesEdicion.isEmpty() || nuevaFecha.isEmpty()) {
                Toast.makeText(this, "Campos inválidos o lista vacía.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cursorCliente = binding.spClientesModificar.selectedItem as Cursor
            val idCliente = cursorCliente.getInt(cursorCliente.getColumnIndexOrThrow("_id"))
            val admin = AdministradorBD(this)
            val db = admin.writableDatabase
            db.beginTransaction()
            try {
                val valoresOrden = ContentValues().apply {
                    put("idCliente", idCliente)
                    put("fecha", nuevaFecha)
                    put("total", totalGeneralOrden)
                }
                db.update("ordenes", valoresOrden, "id = ?", arrayOf(ordenId.toString()))
                // Limpiamos los detalles antiguos de esta orden en la DB
                db.delete("detalleOrden", "idOrden = ?", arrayOf(ordenId.toString()))
                // Reemplazamos db.insert por db.insertOrThrow para capturar errores de llaves foráneas explícitos
                for (det in listaDetallesEdicion) {
                    val valoresDetalle = ContentValues().apply {
                        put("idOrden", ordenId)
                        put("idProducto", det.idProducto)
                        put("cantidad", det.cantidad)
                    }
                    db.insertOrThrow("detalleOrden", null, valoresDetalle)
                }
                db.setTransactionSuccessful()
                Toast.makeText(this, "Orden actualizada correctamente.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                db.endTransaction()
                db.close()
            }
        }
    }
}