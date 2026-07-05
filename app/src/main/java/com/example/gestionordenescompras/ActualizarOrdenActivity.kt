package com.example.gestionordenescompras

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.ArrayAdapter
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
    private var cursorClientes: Cursor? = null
    private var cursorProductos: Cursor? = null

    private val listaEstados = arrayOf("Pendiente", "Procesado", "Completado", "Cancelado")

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

        cursorClientes = db.rawQuery("SELECT id AS _id, nombre FROM clientes", null)
        val adapterClientes = SimpleCursorAdapter(
            this, android.R.layout.simple_spinner_item, cursorClientes,
            arrayOf("nombre"),
            intArrayOf(android.R.id.text1), 0
        )
        binding.spClientesModificar.adapter = adapterClientes

        cursorProductos = db.rawQuery("SELECT id AS _id, nombreProducto FROM productos", null)
        val adapterProductos = SimpleCursorAdapter(
            this, android.R.layout.simple_spinner_item, cursorProductos,
            arrayOf("nombreProducto"),
            intArrayOf(android.R.id.text1), 0
        )
        binding.spProductosModificar.adapter = adapterProductos

        val adapterEstados = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaEstados)
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spEstadoModificar.adapter = adapterEstados
    }

    private fun cargarDatosCabeceraOrden() {
        val admin = AdministradorBD(this)
        val db = admin.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ordenes WHERE id = ?", arrayOf(ordenId.toString()))
        if (cursor.moveToFirst()) {
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
            val idCliente = cursor.getInt(cursor.getColumnIndexOrThrow("idCliente"))
            val estadoActual = cursor.getString(cursor.getColumnIndexOrThrow("estado")) ?: "Pendiente"

            binding.etFechaModificar.setText(fecha)

            val adapterClientes = binding.spClientesModificar.adapter as SimpleCursorAdapter
            for (i in 0 until adapterClientes.count) {
                val itemCursor = adapterClientes.getItem(i) as Cursor
                if (itemCursor.getInt(itemCursor.getColumnIndexOrThrow("_id")) == idCliente) {
                    binding.spClientesModificar.setSelection(i)
                    break
                }
            }

            val posicionEstado = listaEstados.indexOf(estadoActual)
            if (posicionEstado != -1) {
                binding.spEstadoModificar.setSelection(posicionEstado)
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

            // Corregido: Validamos contra la columna real 'cantidad' de la base de datos
            val c = db.rawQuery("SELECT precio, cantidad FROM productos WHERE id = ?", arrayOf(idProducto.toString()))
            if (c.moveToFirst()) {
                val precio = c.getDouble(c.getColumnIndexOrThrow("precio"))
                val subtotal = precio * cantidad

                listaDetallesEdicion.add(DetalleOrdenTemporal(idProducto, nombreProducto, cantidad, precio, subtotal))
                detalleAdapter.notifyItemInserted(listaDetallesEdicion.size - 1)
                totalGeneralOrden += subtotal
                binding.tvTotalGeneralModificar.text = String.format(Locale.US, "$%.2f", totalGeneralOrden)
                binding.etCantidadModificar.text?.clear()
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
            val estadoSeleccionado = binding.spEstadoModificar.selectedItem.toString()

            val admin = AdministradorBD(this)
            val db = admin.writableDatabase

            db.beginTransaction()
            try {
                // STEP 1: Devolver las cantidades antiguas al stock global antes de limpiar
                val cursorDetallesViejos = db.rawQuery(
                    "SELECT idProducto, cantidad FROM detalleOrden WHERE idOrden = ?",
                    arrayOf(ordenId.toString())
                )
                if (cursorDetallesViejos.moveToFirst()) {
                    do {
                        val idProdViejo = cursorDetallesViejos.getInt(cursorDetallesViejos.getColumnIndexOrThrow("idProducto"))
                        val cantVieja = cursorDetallesViejos.getInt(cursorDetallesViejos.getColumnIndexOrThrow("cantidad"))

                        // Reabastecemos temporalmente el inventario
                        db.execSQL(
                            "UPDATE productos SET cantidad = cantidad + ? WHERE id = ?",
                            arrayOf(cantVieja.toString(), idProdViejo.toString())
                        )
                    } while (cursorDetallesViejos.moveToNext())
                }
                cursorDetallesViejos.close()

                // STEP 2: Verificar si hay suficiente stock disponible para la NUEVA lista editada
                for (det in listaDetallesEdicion) {
                    val c = db.rawQuery("SELECT cantidad, nombreProducto FROM productos WHERE id = ?", arrayOf(det.idProducto.toString()))
                    if (c.moveToFirst()) {
                        val stockDisponibleConDevolucion = c.getInt(c.getColumnIndexOrThrow("cantidad"))
                        if (det.cantidad > stockDisponibleConDevolucion) {
                            Toast.makeText(
                                this,
                                "Error: '${det.nombreProducto}' supera el stock máximo disponible ($stockDisponibleConDevolucion unidades).",
                                Toast.LENGTH_LONG
                            ).show()

                            // Abortamos de inmediato. La transacción se cancelará en el finally conservando todo intacto
                            return@setOnClickListener
                        }
                    }
                    c.close()
                }

                // STEP 3: Si la validación fue exitosa, procedemos a limpiar los detalles viejos en DB
                db.delete("detalleOrden", "idOrden = ?", arrayOf(ordenId.toString()))

                // STEP 4: Guardar los nuevos cambios en la cabecera de la orden
                val valoresOrden = ContentValues().apply {
                    put("idCliente", idCliente)
                    put("fecha", nuevaFecha)
                    put("total", totalGeneralOrden)
                    put("estado", estadoSeleccionado)
                }
                db.update("ordenes", valoresOrden, "id = ?", arrayOf(ordenId.toString()))

                // STEP 5: Insertar el nuevo detalle y restar el nuevo stock definitivo
                for (det in listaDetallesEdicion) {
                    val valoresDetalle = ContentValues().apply {
                        put("idOrden", ordenId)
                        put("idProducto", det.idProducto)
                        put("cantidad", det.cantidad)
                    }
                    db.insertOrThrow("detalleOrden", null, valoresDetalle)

                    // Restamos del inventario ya actualizado
                    db.execSQL(
                        "UPDATE productos SET cantidad = cantidad - ? WHERE id = ?",
                        arrayOf(det.cantidad.toString(), det.idProducto.toString())
                    )
                }

                db.setTransactionSuccessful() // Confirmamos la transacción
                Toast.makeText(this, "Orden y stock actualizados correctamente.", Toast.LENGTH_SHORT).show()
                finish() // Regresamos al historial

            } catch (e: Exception) {
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                db.endTransaction()
                db.close()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cursorClientes?.close()
        cursorProductos?.close()
    }
}