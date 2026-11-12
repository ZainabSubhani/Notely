package com.zainab.notely.ui.checklist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zainab.notely.R
import com.zainab.notely.data.local.ChecklistItem
import com.zainab.notely.databinding.ItemChecklistItemBinding

class ChecklistItemAdapter(
    private val onToggle: (ChecklistItem, Boolean) -> Unit,
    private val onRemove: (ChecklistItem) -> Unit
) : ListAdapter<ChecklistItem, ChecklistItemAdapter.ItemViewHolder>(DiffCallback) {

    class ItemViewHolder(private val binding: ItemChecklistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChecklistItem, onToggle: (ChecklistItem, Boolean) -> Unit, onRemove: (ChecklistItem) -> Unit) {
            // Detach the old listener first: a recycled row must not fire a fake toggle
            binding.cbDone.setOnCheckedChangeListener(null)
            binding.cbDone.isChecked = item.isChecked
            binding.cbDone.setOnCheckedChangeListener { _, checked -> onToggle(item, checked) }

            binding.tvText.text = item.text
            val ctx = binding.root.context
            if (item.isChecked) {
                binding.tvText.paintFlags = binding.tvText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvText.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
            } else {
                binding.tvText.paintFlags = binding.tvText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvText.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
            }
            binding.btnRemove.setOnClickListener { onRemove(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        ItemChecklistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position), onToggle, onRemove)

    object DiffCallback : DiffUtil.ItemCallback<ChecklistItem>() {
        override fun areItemsTheSame(a: ChecklistItem, b: ChecklistItem) = a.id == b.id
        override fun areContentsTheSame(a: ChecklistItem, b: ChecklistItem) = a == b
    }
}