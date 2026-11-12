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
import com.zainab.notely.ui.common.bindCategoryTag

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

            bindCategoryTag(binding.vAccent, binding.tvTag, item.categoryName, item.categoryColor)
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
