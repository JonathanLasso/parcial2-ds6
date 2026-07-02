package com.example.gestionordenescompras.adapter

import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionordenescompras.databinding.ItemProductoBinding

class ProductosAdapter(private var cursor: Cursor?,
                       private val onProductoClick: (Int) -> Unit, // Ahora devolvemos solo el ID (Int)
                       private val onEliminarClick: (Int, String) -> Unit //Recibe: ID y Nombre del cliente para el mensaje
): RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder>() {
    class ProductoViewHolder(
        val binding: ItemProductoBinding
    ): RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ProductoViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        // Movemos el cursor a la posición exacta que el RecyclerView quiere dibujar
        if(cursor != null && cursor!!.moveToPosition(position)){
            // Extraemos los datos directamente de las columnas
            val id = cursor!!.getInt(cursor!!.getColumnIndexOrThrow("id"))
            val nombre = cursor!!.getString(cursor!!.getColumnIndexOrThrow("nombreProducto"))
            val descripcion = cursor!!.getString(cursor!!.getColumnIndexOrThrow("descripcion"))
            val precio = cursor!!.getString(cursor!!.getColumnIndexOrThrow("precio"))
            // Pintamos la vista
            holder.binding.tvNombreProducto.text = nombre
            holder.binding.tvDescripcionProducto.text = descripcion
            holder.binding.tvPrecioProducto.text = "$$precio"
            // Evento click: mandamos el ID extraído del Cursor
            holder.binding.root.setOnClickListener {
                onProductoClick(id)
            }
            // Evento click: para eliminar
            holder.binding.btnEliminarProducto.setOnClickListener {
                onEliminarClick(id,nombre)
            }
        }
    }
    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }
    // Función para refrescar el RecyclerView cuando agregas un cliente nuevo
    fun cambiarCursor(nuevoCursor: Cursor?) {
        if (cursor == nuevoCursor) return
        cursor?.close() // Cerramos el cursor viejo para liberar memoria
        cursor = nuevoCursor
        if (nuevoCursor != null) {
            notifyDataSetChanged()
        }
    }
}