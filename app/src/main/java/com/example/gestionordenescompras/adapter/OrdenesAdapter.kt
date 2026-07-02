package com.example.gestionordenescompras.adapter

import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionordenescompras.AdministradorBD
import com.example.gestionordenescompras.databinding.ItemOrdenBinding
import java.util.Locale

class OrdenesAdapter(
    var cursor: Cursor?,
    private val onOrdenClick: (Int) -> Unit,
    private val onOrdenBorrarClick: (Int, String) -> Unit
) : RecyclerView.Adapter<OrdenesAdapter.OrdenViewHolder>() {

    class OrdenViewHolder(val binding: ItemOrdenBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdenViewHolder {
        val binding = ItemOrdenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrdenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdenViewHolder, position: Int) {
        if (cursor != null && cursor!!.moveToPosition(position)) {
            val context = holder.itemView.context

            val id = cursor!!.getInt(cursor!!.getColumnIndexOrThrow("id"))
            val cliente = cursor!!.getString(cursor!!.getColumnIndexOrThrow("nombreCliente"))
            val fecha = cursor!!.getString(cursor!!.getColumnIndexOrThrow("fecha"))
            val total = cursor!!.getDouble(cursor!!.getColumnIndexOrThrow("total"))

            // Asignación de datos principales
            holder.binding.tvHistorialIdOrden.text = "Orden #${id} - ${cliente}"
            holder.binding.tvHistorialFecha.text = "Fecha: $fecha"
            holder.binding.tvHistorialTotal.text = String.format(Locale.US, "$%.2f", total)

            // --- CARGA DINÁMICA DE PRODUCTOS ---
            // 1. Limpiamos cualquier vista previa que haya quedado por el reciclaje de celdas
            holder.binding.containerProductos.removeAllViews()

            // 2. Abrimos la base de datos para consultar los productos de esta orden específica
            val dbHelper = AdministradorBD(context)
            val db = dbHelper.readableDatabase

            // Consulta que une los detalles con la información y precio del producto
            val queryProductos = """
                SELECT d.cantidad, p.nombreProducto, p.precio 
                FROM detalleOrden d 
                INNER JOIN productos p ON d.idProducto = p.id 
                WHERE d.idOrden = ?
            """.trimIndent()

            val cursorProductos = db.rawQuery(queryProductos, arrayOf(id.toString()))

            // 3. Recorremos el resultado e inflamos los textos de cada producto
            if (cursorProductos.moveToFirst()) {
                do {
                    val cantidad = cursorProductos.getInt(cursorProductos.getColumnIndexOrThrow("cantidad"))
                    val nombre = cursorProductos.getString(cursorProductos.getColumnIndexOrThrow("nombreProducto"))
                    val precio = cursorProductos.getDouble(cursorProductos.getColumnIndexOrThrow("precio"))
                    val subtotal = cantidad * precio

                    // Creamos la estructura visual para el producto usando código de manera limpia
                    val layoutItem = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 4, 0, 4)
                    }

                    // Texto Izquierdo: Cantidad y Nombre
                    val tvNombreCant = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        text = "${cantidad}x $nombre"
                        setTextColor(android.graphics.Color.parseColor("#333333"))
                        textSize = 14f
                    }

                    // Texto Derecho: Subtotal calculado
                    val tvSubtotal = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        text = String.format(Locale.US, "$%.2f", subtotal)
                        setTextColor(android.graphics.Color.parseColor("#333333"))
                        textSize = 14f
                    }

                    layoutItem.addView(tvNombreCant)
                    layoutItem.addView(tvSubtotal)

                    // Lo añadimos al contenedor de la tarjeta actual
                    holder.binding.containerProductos.addView(layoutItem)

                } while (cursorProductos.moveToNext())
            }
            cursorProductos.close()
            db.close()
            // ------------------------------------

            // Listeners de los botones
            holder.binding.root.setOnClickListener {
                onOrdenClick(id)
            }

            holder.binding.btnEliminar.setOnClickListener {
                onOrdenBorrarClick(id,cliente)
            }
        }
    }

    override fun getItemCount(): Int = cursor?.count ?: 0

    fun cambiarCursor(nuevoCursor: Cursor?) {
        if (cursor == nuevoCursor) return
        cursor?.close()
        cursor = nuevoCursor
        if (nuevoCursor != null) {
            notifyDataSetChanged()
        }
    }
}