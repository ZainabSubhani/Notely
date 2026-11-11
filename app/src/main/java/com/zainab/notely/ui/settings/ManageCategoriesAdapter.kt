package com.zainab.notely.ui.settings

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zainab.notely.data.local.Category
import com.zainab.notely.databinding.ItemCategoryBinding
import com.zainab.notely.databinding.ItemCategoryHeaderBinding

class ManageCategoriesAdapter(
    private val onRename: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : ListAdapter<CategoryListItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CategoryListItem.Header -> TYPE_HEADER
        is CategoryListItem.Row -> TYPE_ROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderHolder(ItemCategoryHeaderBinding.inflate(inflater, parent, false))
        } else {
            RowHolder(ItemCategoryBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryListItem.Header -> (holder as HeaderHolder).binding.tvHeader.text = item.title
            is CategoryListItem.Row -> (holder as RowHolder).bind(item.category, onRename, onDelete)
        }
    }

    class HeaderHolder(val binding: ItemCategoryHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    class RowHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(c: Category, onRename: (Category) -> Unit, onDelete: (Category) -> Unit) {
            binding.tvName.text = c.name
            binding.vDot.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(c.colorHex))
            }
            // Reset both states every time: rows are reused
            binding.tvLock.visibility = if (c.isDefault) View.VISIBLE else View.GONE
            binding.btnRename.visibility = if (c.isDefault) View.GONE else View.VISIBLE
            binding.btnDelete.visibility = if (c.isDefault) View.GONE else View.VISIBLE
            binding.btnRename.setOnClickListener { onRename(c) }
            binding.btnDelete.setOnClickListener { onDelete(c) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<CategoryListItem>() {
        override fun areItemsTheSame(a: CategoryListItem, b: CategoryListItem) = when {
            a is CategoryListItem.Header && b is CategoryListItem.Header -> a.title == b.title
            a is CategoryListItem.Row && b is CategoryListItem.Row -> a.category.id == b.category.id
            else -> false
        }
        override fun areContentsTheSame(a: CategoryListItem, b: CategoryListItem) = a == b
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ROW = 1
    }
}