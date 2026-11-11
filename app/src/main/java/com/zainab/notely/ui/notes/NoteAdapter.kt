package com.zainab.notely.ui.notes

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zainab.notely.data.local.Note
import com.zainab.notely.data.local.NoteWithCategory
import com.zainab.notely.databinding.ItemNoteBinding

class NoteAdapter(private val onNoteClick: (Note) -> Unit) :
    ListAdapter<NoteWithCategory, NoteAdapter.NoteViewHolder>(DiffCallback) {

    class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NoteWithCategory, onClick: (Note) -> Unit) {
            val note = item.note
            binding.tvTitle.text = note.title
            binding.tvContent.text = note.content
            binding.tvMeta.text = note.sourceType.name
            binding.root.setOnClickListener { onClick(note) }

            val name = item.categoryName
            val colorHex = item.categoryColor
            if (name != null && colorHex != null) {
                val color = Color.parseColor(colorHex)
                binding.vAccent.setBackgroundColor(color)
                binding.tvTag.text = name.uppercase()
                binding.tvTag.background = GradientDrawable().apply {
                    cornerRadius = 5f * binding.root.resources.displayMetrics.density
                    setColor(ColorUtils.setAlphaComponent(color, 64))   // soft tint of the same color
                }
                binding.tvTag.visibility = View.VISIBLE
            } else {
                binding.vAccent.setBackgroundColor(Color.TRANSPARENT)
                binding.tvTag.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position), onNoteClick)
    }

    object DiffCallback : DiffUtil.ItemCallback<NoteWithCategory>() {
        override fun areItemsTheSame(oldItem: NoteWithCategory, newItem: NoteWithCategory) =
            oldItem.note.id == newItem.note.id

        override fun areContentsTheSame(oldItem: NoteWithCategory, newItem: NoteWithCategory) =
            oldItem == newItem
    }
}
