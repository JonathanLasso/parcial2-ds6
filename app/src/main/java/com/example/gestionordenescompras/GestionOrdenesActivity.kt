package com.example.gestionordenescompras

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionordenescompras.adapter.DetalleOrdenAdapter
import com.example.gestionordenescompras.adapter.OrdenesAdapter
import com.example.gestionordenescompras.databinding.ActivityGestionOrdenesBinding
import com.example.gestionordenescompras.model.DetalleOrdenTemporal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GestionOrdenesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGestionOrdenesBinding
    private lateinit var dbHelper: AdministradorBD
    private lateinit var detalleAdapter: DetalleOrdenAdapter
    private lateinit var ordenesAdapter: OrdenesAdapter
    private var cursorClientes: Cursor? = null
    private var cursorProductos: Cursor? = null
    // Almacenamiento temporal en memoria
    private val listaDetallesTemporales = mutableListOf<DetalleOrdenTemporal>()
    private var totalGeneralOrden: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionOrdenesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Inicializamos la conexión UNA SOLA VEZ
        dbHelper = AdministradorBD(this)
        establecerFechaAutomatica()
        configurarSelectorFecha()
        cargarSpinners()
        configurarListaDetallesTemporales()
        configurarListaHistorialOrdenes()
        accionesBotones()
        configurarBotonRegresar(binding.btnRegresarAlMenu, MainActivity::class.java)
    }
    override fun onResume() {
        super.onResume()
        refrescarHistorialOrdenes()
    }
    private fun establecerFechaAutomatica() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.etFecha.setText(sdf.format(Date()))
    }
    private fun configurarSelectorFecha() {
        // Evitamos que el teclado nativo emerja tapando el calendario
        binding.etFecha.isFocusable = false
        binding.etFecha.isClickable = true
        binding.etFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val año = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)
            val dpd =
                DatePickerDialog(this, { _, añoSeleccionado, mesSeleccionado, diaSeleccionado ->
                    // Formateamos la fecha seleccionada de manera segura con padding de ceros a la izquierda (ej: "2026-07-02")
                    val fechaFormateada = String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        añoSeleccionado,
                        mesSeleccionado + 1,
                        diaSeleccionado
                    )
                    binding.etFecha.setText(fechaFormateada)
                }, año, mes, dia)

            dpd.show()
        }
    }
    private fun cargarSpinners() {
        val db = dbHelper.readableDatabase
        cursorClientes = db.rawQuery("SELECT id AS _id, nombre FROM clientes", null)
        val fromClientes = arrayOf("nombre")
        val toClientes = intArrayOf(android.R.id.text1)
        val adapterClientes = SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorClientes, fromClientes, toClientes, 0)
        adapterClientes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spClientes.adapter = adapterClientes

        cursorProductos = db.rawQuery("SELECT id AS _id, nombreProducto FROM productos", null)
        val fromProductos = arrayOf("nombreProducto")
        val toProductos = intArrayOf(android.R.id.text1)
        val adapterProductos = SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorProductos, fromProductos, toProductos, 0)
        adapterProductos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spProductos.adapter = adapterProductos
    }
    private fun configurarListaDetallesTemporales() {
        detalleAdapter = DetalleOrdenAdapter(listaDetallesTemporales)
        binding.rvDetallesActuales.apply {
            layoutManager = LinearLayoutManager(this@GestionOrdenesActivity)
            adapter = detalleAdapter
        }
    }
    private fun configurarListaHistorialOrdenes() {
        ordenesAdapter = OrdenesAdapter(
            cursor = obtenerCursorHistorialOrdenes(),
            onOrdenClick = { idOrden ->
                val intent = Intent(this, ActualizarOrdenActivity::class.java).apply {
                    putExtra("ORDEN_ID", idOrden)
                }
                startActivity(intent)
            },
            onOrdenBorrarClick = { idOrden, nombreCliente ->
                mostrarDialogoConfirmacion(idOrden, nombreCliente){
                    eliminarOrdenYDetalle(idOrden)
                }
            }
        )
        binding.rvOrdenesHistorial.apply {
            layoutManager = LinearLayoutManager(this@GestionOrdenesActivity)
            adapter = ordenesAdapter
        }
    }
    private fun eliminarOrdenYDetalle(idOrden: Int) {
        val db = dbHelper.writableDatabase
        val filasAfectadas = db.delete("ordenes", "id = ?", arrayOf(idOrden.toString()))
        if (filasAfectadas > 0) {
            Toast.makeText(this, "Orden #${idOrden} eliminada.", Toast.LENGTH_SHORT).show()
            refrescarHistorialOrdenes()
        } else {
            Toast.makeText(this, "No se pudo eliminar la orden.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerCursorHistorialOrdenes(): Cursor {
        val db = dbHelper.readableDatabase
        return db.rawQuery(
            "SELECT o.id, o.fecha, o.estado, o.total, c.nombre AS nombreCliente FROM ordenes o INNER JOIN clientes c ON o.idCliente = c.id",
            null
        )
    }
    private fun refrescarHistorialOrdenes() {
        if (::ordenesAdapter.isInitialized) {
            val nuevoCursor = obtenerCursorHistorialOrdenes()
            if (nuevoCursor.count == 0) {
                binding.rvOrdenesHistorial.visibility = View.GONE
                binding.tvOrdenesVacias.visibility = View.VISIBLE
            } else {
                binding.rvOrdenesHistorial.visibility = View.VISIBLE
                binding.tvOrdenesVacias.visibility = View.GONE
            }
            val cursorViejo = ordenesAdapter.cursor
            ordenesAdapter.cambiarCursor(nuevoCursor)
            cursorViejo?.close()
        }
    }
    private fun accionesBotones() {
        binding.btnAgregarProducto.setOnClickListener {
            val itemSeleccionado = binding.spProductos.selectedItem
            if (itemSeleccionado == null) {
                Toast.makeText(this, "Debes registrar o seleccionar un producto válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cantidadStr = binding.etCantidad.text.toString()
            if (cantidadStr.isEmpty() || cantidadStr.toInt() <= 0) {
                Toast.makeText(this, "Ingresa una cantidad válida mayor a 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cantidad = cantidadStr.toInt()
            val cursorProducto = itemSeleccionado as Cursor
            val idProducto = cursorProducto.getInt(cursorProducto.getColumnIndexOrThrow("_id"))
            val nombreProducto = cursorProducto.getString(cursorProducto.getColumnIndexOrThrow("nombreProducto"))

            val db = dbHelper.readableDatabase
            // 1. Modificamos la consulta para traer también el stock actual
            val consulta = db.rawQuery("SELECT precio, cantidad FROM productos WHERE id = ?", arrayOf(idProducto.toString()))

            if (consulta.moveToFirst()) {
                val precio = consulta.getDouble(consulta.getColumnIndexOrThrow("precio"))
                val cantidadActual = consulta.getInt(consulta.getColumnIndexOrThrow("cantidad")) // Asegúrate de que tu columna se llame 'stock' o 'cantidad'

                // 2. Primera validación: ¿Hay suficiente stock físico para agregar al carrito?
                if (cantidad > cantidadActual) {
                    Toast.makeText(this, "Stock insuficiente. Disponible: $cantidadActual", Toast.LENGTH_LONG).show()
                    consulta.close()
                    return@setOnClickListener
                }

                val subtotal = precio * cantidad
                val nuevoDetalle = DetalleOrdenTemporal(idProducto, nombreProducto, cantidad, precio, subtotal)
                listaDetallesTemporales.add(nuevoDetalle)
                detalleAdapter.notifyItemInserted(listaDetallesTemporales.size - 1)
                totalGeneralOrden += subtotal
                binding.tvTotalGeneral.text = String.format(Locale.US, "$%.2f", totalGeneralOrden)
                binding.etCantidad.text?.clear()
                Toast.makeText(this, "Añadido al resumen.", Toast.LENGTH_SHORT).show()
            }
            consulta.close()
        }

        binding.btnGuardarOrden.setOnClickListener {
            val clienteSeleccionado = binding.spClientes.selectedItem
            if (clienteSeleccionado == null) {
                Toast.makeText(this, "Por favor, selecciona un cliente.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (listaDetallesTemporales.isEmpty()) {
                Toast.makeText(this, "La orden debe contener al menos un producto.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fecha = binding.etFecha.text.toString()
            val cursorCliente = clienteSeleccionado as Cursor
            val idCliente = cursorCliente.getInt(cursorCliente.getColumnIndexOrThrow("_id"))

            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                // 3. Segunda validación (Filtro de seguridad dentro de la transacción)
                // Verificamos producto por producto antes de alterar nada
                for (det in listaDetallesTemporales) {
                    val c = db.rawQuery("SELECT cantidad FROM productos WHERE id = ?", arrayOf(det.idProducto.toString()))
                    if (c.moveToFirst()) {
                        val cantidadReal = c.getInt(c.getColumnIndexOrThrow("cantidad"))
                        if (det.cantidad > cantidadReal) {
                            Toast.makeText(this, "Error: El producto '${det.nombreProducto}' ya no tiene stock suficiente (Disponible: $cantidadReal).", Toast.LENGTH_LONG).show()
                            c.close()
                            return@setOnClickListener // Cancela el click y no hace el commit de la transacción
                        }
                    }
                    c.close()
                }

                // Si pasó la verificación de todos los productos, procedemos a insertar la orden
                val datosOrden = ContentValues().apply {
                    put("idCliente", idCliente)
                    put("fecha", fecha)
                    put("estado", "Pendiente")
                    put("total", totalGeneralOrden)
                }

                val idOrdenGenerado = db.insert("ordenes", null, datosOrden)
                if (idOrdenGenerado != -1L) {
                    for (det in listaDetallesTemporales) {
                        // Insertar en detalleOrden
                        val datosDetalle = ContentValues().apply {
                            put("idOrden", idOrdenGenerado.toInt())
                            put("idProducto", det.idProducto)
                            put("cantidad", det.cantidad)
                        }
                        db.insert("detalleOrden", null, datosDetalle)

                        // 4. Actualizar el stock restando la cantidad vendida
                        db.execSQL(
                            "UPDATE productos SET cantidad = cantidad - ? WHERE id = ?",
                            arrayOf(det.cantidad.toString(), det.idProducto.toString())
                        )
                    }

                    db.setTransactionSuccessful() // Confirma la orden y el nuevo stock
                    Toast.makeText(this, "Orden #$idOrdenGenerado registrada con éxito.", Toast.LENGTH_SHORT).show()

                    // Limpieza de interfaz
                    listaDetallesTemporales.clear()
                    detalleAdapter.notifyDataSetChanged()
                    totalGeneralOrden = 0.0
                    binding.tvTotalGeneral.text = "$0.00"
                    refrescarHistorialOrdenes()
                } else {
                    Toast.makeText(this, "Error al crear la cabecera de la orden.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error en transacciones: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                db.endTransaction()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cursorClientes?.close()
        cursorProductos?.close()
        if (::ordenesAdapter.isInitialized) {
            ordenesAdapter.cursor?.close()
        }
        dbHelper.close()
    }
}