package com.example.gestionordenescompras.adapter

import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionordenescompras.databinding.ItemClienteBinding

class ClientesAdapter(
    private var cursor: Cursor?,
    private val onClienteClick: (Int) -> Unit,
    private val onEliminarClick: (Int, String) -> Unit
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(val binding: ItemClienteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        if (cursor != null && cursor!!.moveToPosition(position)) {
            val id = cursor!!.getInt(cursor!!.getColumnIndexOrThrow("id"))
            val nombre = cursor!!.getString(cursor!!.getColumnIndexOrThrow("nombre"))
            val correo = cursor!!.getString(cursor!!.getColumnIndexOrThrow("correo"))
            val telefono = cursor!!.getString(cursor!!.getColumnIndexOrThrow("telefono"))

            holder.binding.tvNombre.text = nombre
            holder.binding.tvCorreo.text = correo
            holder.binding.tvTelefono.text = telefono

            holder.binding.root.setOnClickListener {
                onClienteClick(id)
            }
            
            holder.binding.btnEditar.setOnClickListener {
                onClienteClick(id)
            }
            
            holder.binding.btnEliminar.setOnClickListener {
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