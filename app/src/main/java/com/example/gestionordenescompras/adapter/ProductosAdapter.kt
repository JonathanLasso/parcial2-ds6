package com.example.gestionordenescompras.adapter

import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionordenescompras.databinding.ItemProductoBinding

class ProductosAdapter(
    private var cursor: Cursor?,
    private val onProductoClick: (Int) -> Unit,
    private val onEliminarClick: (Int, String) -> Unit
) : RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(val binding: ItemProductoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        if (cursor != null && cursor!!.moveToPosition(position)) {
            val id = cursor!!.getInt(cursor!!.getColumnIndexOrThrow("id"))
            val nombre = cursor!!.getString(cursor!!.getColumnIndexOrThrow("nombreProducto"))
            val precio = cursor!!.getString(cursor!!.getColumnIndexOrThrow("precio"))

            holder.binding.tvNombreProducto.text = nombre
            holder.binding.tvPrecioProducto.text = "$$precio"

            holder.binding.root.setOnClickListener {
                onProductoClick(id)
            }
            
            holder.binding.btnEditarProducto.setOnClickListener {
                onProductoClick(id)
            }
            
            holder.binding.btnEliminarProducto.setOnClickListener {
                onEliminarClick(id, nombre)
            }
        }
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    fun cambiarCursor(nuevoCursor: Cursor?) {
        if (cursor == nuevoCursor) return
        cursor?.close()
        cursor = nuevoCursor
        notifyDataSetChanged()
    }
}