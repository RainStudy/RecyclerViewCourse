package team.redrock.rain.rvtest.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import team.redrock.rain.rvtest.databinding.ItemTagBinding

/**
 * team.redrock.rain.rvtest.adapter.TagAdapter.kt
 * RecyclerViewTest
 *
 * @author 寒雨
 * @since 2023/4/20 下午2:46
 */
class TagAdapter : ListAdapter<String, TagAdapter.Holder>(
    object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
) {

    class Holder(val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.tvTag.text = getItem(position)
    }
}