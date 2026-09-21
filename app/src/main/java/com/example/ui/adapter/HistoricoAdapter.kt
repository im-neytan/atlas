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
import com.example.data.model.HistoricoItem

class HistoricoAdapter(
    private val onItemClick: ((HistoricoItem) -> Unit)? = null
) : ListAdapter<HistoricoItem, HistoricoAdapter.HistoricoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historico, parent, false)
        return HistoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class HistoricoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStatusBadge: TextView? = itemView.findViewById(R.id.tvStatusBadge)
        private val tvTimestamp: TextView? = itemView.findViewById(R.id.tvTimestamp)
        private val tvDestino: TextView? = itemView.findViewById(R.id.tvDestino)
        private val tvMegas: TextView? = itemView.findViewById(R.id.tvMegas)
        private val tvUssdResponse: TextView? = itemView.findViewById(R.id.tvUssdResponse)

        fun bind(item: HistoricoItem) {
            tvDestino?.text = item.numeroDestino
            tvMegas?.text = item.megas
            tvTimestamp?.text = item.formattedTime
            tvUssdResponse?.text = item.ussdResposta

            when (item.status) {
                "CONCLUIDO" -> {
                    tvStatusBadge?.text = "✅ CONCLUÍDO"
                    tvStatusBadge?.setTextColor(Color.parseColor("#00E676"))
                    tvStatusBadge?.setBackgroundColor(Color.parseColor("#052B1B"))
                }
                "FALHA" -> {
                    tvStatusBadge?.text = "❌ FALHA"
                    tvStatusBadge?.setTextColor(Color.parseColor("#F87171"))
                    tvStatusBadge?.setBackgroundColor(Color.parseColor("#381515"))
                }
                else -> {
                    tvStatusBadge?.text = "⚠️ CANCELADO"
                    tvStatusBadge?.setTextColor(Color.parseColor("#FBBF24"))
                    tvStatusBadge?.setBackgroundColor(Color.parseColor("#3B2D05"))
                }
            }

            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<HistoricoItem>() {
        override fun areItemsTheSame(oldItem: HistoricoItem, newItem: HistoricoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HistoricoItem, newItem: HistoricoItem): Boolean {
            return oldItem == newItem
        }
    }
}
