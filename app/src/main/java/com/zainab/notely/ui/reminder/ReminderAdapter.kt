package com.zainab.notely.ui.reminder

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zainab.notely.R
import com.zainab.notely.data.local.Reminder
import com.zainab.notely.databinding.ItemReminderBinding
import com.zainab.notely.databinding.ItemReminderHeaderBinding

class ReminderAdapter(
    private val onToggle: (Reminder, Boolean) -> Unit,
    private val onClick: (Reminder) -> Unit
) : ListAdapter<ReminderListItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ReminderListItem.Header -> TYPE_HEADER
        is ReminderListItem.Row -> TYPE_ROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderHolder(ItemReminderHeaderBinding.inflate(inflater, parent, false))
        } else {
            RowHolder(ItemReminderBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ReminderListItem.Header -> (holder as HeaderHolder).bind(item)
            is ReminderListItem.Row -> (holder as RowHolder).bind(item, onToggle, onClick)
        }
    }

    class HeaderHolder(private val binding: ItemReminderHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReminderListItem.Header) {
            binding.tvHeader.text = item.title
            val color = if (item.isOverdue) R.color.danger else R.color.text_secondary
            binding.tvHeader.setTextColor(ContextCompat.getColor(binding.root.context, color))
        }
    }

    class RowHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReminderListItem.Row, onToggle: (Reminder, Boolean) -> Unit, onClick: (Reminder) -> Unit) {
            val ctx = binding.root.context
            val reminder = item.reminder
            val completed = item.status == ReminderListItem.Status.COMPLETED

            binding.tvTitle.text = reminder.title
            binding.tvMeta.text = item.meta

            // Detach the old listener first: a recycled row must not fire a fake toggle
            binding.cbDone.setOnCheckedChangeListener(null)
            binding.cbDone.isChecked = reminder.isCompleted
            binding.cbDone.setOnCheckedChangeListener { _, checked -> onToggle(reminder, checked) }

            if (completed) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
            } else {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
            }

            // Left edge shows the status, as in the design (rows are reused, so always set it)
            val accent = when (item.status) {
                ReminderListItem.Status.OVERDUE -> ContextCompat.getColor(ctx, R.color.danger)
                ReminderListItem.Status.TODAY -> ContextCompat.getColor(ctx, R.color.accent)
                ReminderListItem.Status.UPCOMING -> ContextCompat.getColor(ctx, R.color.border)
                ReminderListItem.Status.COMPLETED -> Color.TRANSPARENT
            }
            binding.vAccent.setBackgroundColor(accent)

            binding.root.setOnClickListener { onClick(reminder) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ReminderListItem>() {
        override fun areItemsTheSame(a: ReminderListItem, b: ReminderListItem) = when {
            a is ReminderListItem.Header && b is ReminderListItem.Header -> a.title == b.title
            a is ReminderListItem.Row && b is ReminderListItem.Row -> a.reminder.id == b.reminder.id
            else -> false
        }

        override fun areContentsTheSame(a: ReminderListItem, b: ReminderListItem) = a == b
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ROW = 1
    }
}