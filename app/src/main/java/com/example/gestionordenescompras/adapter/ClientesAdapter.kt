package com.example.gestionordenescompras.adapter

import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionordenescompras.databinding.ItemClienteBinding

class ClientesAdapter(
    private var cursor: Cursor?,
    private val onClienteClick: (Int) -> Unit, // Ahora devolvemos solo el ID (Int)
    private val onEliminarClick: (Int, String) -> Unit //Recibe: ID y Nombre del cliente para el mensaje
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(val binding: ItemClienteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        // Movemos el cursor a la posición exacta que el RecyclerView quiere dibujar
        if (cursor != null && cursor!!.moveToPosition(position)) {

            // Extraemos los datos directamente de las columnas
            val id = cursor!!.getInt(cursor!!.getColumnIndexOrThrow("id"))
            val nombre = cursor!!.getString(cursor!!.getColumnIndexOrThrow("nombre"))
            val correo = cursor!!.getString(cursor!!.getColumnIndexOrThrow("correo"))
            val telefono = cursor!!.getString(cursor!!.getColumnIndexOrThrow("telefono"))
            val direccion = cursor!!.getString(cursor!!.getColumnIndexOrThrow("direccion"))

            // Pintamos la vista
            holder.binding.tvNombre.text = nombre
            holder.binding.tvCorreo.text = correo
            holder.binding.tvTelefono.text = telefono
            holder.binding.tvDireccion.text = direccion

            // Evento click: mandamos el ID extraído del Cursor
            holder.binding.root.setOnClickListener {
                onClienteClick(id)
            }
            // Evento click: para eliminar
            holder.binding.btnEliminar.setOnClickListener {
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