package com.example.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.data.model.PedidoFila

class FilaAdapter(
    private val onItemClick: ((PedidoFila) -> Unit)? = null
) : ListAdapter<PedidoFila, FilaAdapter.FilaViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fila, parent, false)
        return FilaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilaViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class FilaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView? = itemView.findViewById(R.id.tvOrderId)
        private val tvStatus: TextView? = itemView.findViewById(R.id.tvStatus)
        private val tvDestino: TextView? = itemView.findViewById(R.id.tvDestino)
        private val tvMegas: TextView? = itemView.findViewById(R.id.tvMegas)
        private val tvSimSlot: TextView? = itemView.findViewById(R.id.tvSimSlot)

        fun bind(item: PedidoFila) {
            tvOrderId?.text = item.displayId
            tvDestino?.text = item.numeroDestino
            tvMegas?.text = item.megas
            tvStatus?.text = item.status
            tvSimSlot?.text = "• SIM ${item.simSlot}"

            when (item.status) {
                "AGUARDANDO" -> {
                    tvStatus?.setTextColor(Color.parseColor("#FBBF24"))
                    tvStatus?.setBackgroundColor(Color.parseColor("#3B2D05"))
                }
                "EM PROCESSAMENTO" -> {
                    tvStatus?.setTextColor(Color.parseColor("#38BDF8"))
                    tvStatus?.setBackgroundColor(Color.parseColor("#0C2C40"))
                }
                "CONCLUIDO" -> {
                    tvStatus?.setTextColor(Color.parseColor("#00E676"))
                    tvStatus?.setBackgroundColor(Color.parseColor("#052B1B"))
                }
                else -> {
                    tvStatus?.setTextColor(Color.parseColor("#F87171"))
                    tvStatus?.setBackgroundColor(Color.parseColor("#381515"))
                }
            }

            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PedidoFila>() {
        override fun areItemsTheSame(oldItem: PedidoFila, newItem: PedidoFila): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PedidoFila, newItem: PedidoFila): Boolean {
            return oldItem == newItem
        }
    }
}
