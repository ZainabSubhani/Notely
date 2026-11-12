package com.zainab.notely.ui.checklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zainab.notely.data.local.ChecklistWithProgress
import com.zainab.notely.databinding.ItemChecklistBinding
import com.zainab.notely.ui.common.bindCategoryTag

class ChecklistAdapter(private val onClick: (ChecklistWithProgress) -> Unit) :
    ListAdapter<ChecklistWithProgress, ChecklistAdapter.ChecklistViewHolder>(DiffCallback) {

    class ChecklistViewHolder(private val binding: ItemChecklistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChecklistWithProgress, onClick: (ChecklistWithProgress) -> Unit) {
            binding.tvTitle.text = item.title
            binding.progressBar.progress =
                if (item.totalItems == 0) 0 else (item.doneItems * 100) / item.totalItems
            binding.tvProgress.text = "${item.doneItems} of ${item.totalItems} done"
            bindCategoryTag(binding.vAccent, binding.tvTag, item.categoryName, item.categoryColor)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChecklistViewHolder(
        ItemChecklistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) =
        holder.bind(getItem(position), onClick)

    object DiffCallback : DiffUtil.ItemCallback<ChecklistWithProgress>() {
        override fun areItemsTheSame(a: ChecklistWithProgress, b: ChecklistWithProgress) = a.id == b.id
        override fun areContentsTheSame(a: ChecklistWithProgress, b: ChecklistWithProgress) = a == b
    }
}