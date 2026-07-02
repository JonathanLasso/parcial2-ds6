package com.example.gestionordenescompras.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionordenescompras.databinding.ItemDetalleOrdenBinding
import com.example.gestionordenescompras.model.DetalleOrdenTemporal
import java.util.Locale

class DetalleOrdenAdapter(
    private val listaDetalles: MutableList<DetalleOrdenTemporal>
) : RecyclerView.Adapter<DetalleOrdenAdapter.DetalleViewHolder>() {

    class DetalleViewHolder(val binding: ItemDetalleOrdenBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleViewHolder {
        val binding = ItemDetalleOrdenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetalleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetalleViewHolder, position: Int) {
        val detalle = listaDetalles[position]
        holder.binding.tvItemProducto.text = detalle.nombreProducto
        holder.binding.tvItemCantidad.text = "Cant: ${detalle.cantidad}"
        holder.binding.tvItemSubtotal.text = String.format(Locale.US, "$%.2f", detalle.subtotal)
    }

    override fun getItemCount(): Int = listaDetalles.size
}