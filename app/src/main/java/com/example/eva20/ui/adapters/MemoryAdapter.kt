package com.example.eva20.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eva20.R
import com.example.eva20.network.models.Memory
import java.text.SimpleDateFormat
import java.util.Locale

class MemoryAdapter(private val onDeleteClick: (Memory) -> Unit) :
    ListAdapter<Memory, MemoryAdapter.MemoryViewHolder>(MemoryDiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MemoryViewHolder(
            inflater.inflate(R.layout.item_memory, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memory = getItem(position)
        holder.bind(memory)
    }

    inner class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewMemoryTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.textViewMemoryContent)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textViewMemoryTimestamp)
        private val syncStatusView: View = itemView.findViewById(R.id.viewSyncStatus)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(memory: Memory) {
            titleTextView.text = memory.title
            contentTextView.text = memory.content
            timestampTextView.text = dateFormat.format(memory.timestamp)

            // Show sync status
            syncStatusView.setBackgroundResource(
                if (memory.isSynced) R.drawable.ic_synced
                else R.drawable.ic_not_synced
            )

            // Set tags if any
            if (memory.tags.isNotEmpty()) {
                // Implement tag display if needed
            }

            // Set delete button click listener
            deleteButton.setOnClickListener {
                onDeleteClick(memory)
            }
        }
    }

    class MemoryDiffCallback : DiffUtil.ItemCallback<Memory>() {
        override fun areItemsTheSame(oldItem: Memory, newItem: Memory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Memory, newItem: Memory): Boolean {
            return oldItem == newItem
        }
    }
}