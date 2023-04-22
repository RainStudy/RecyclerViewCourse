package team.redrock.rain.rvtest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import team.redrock.rain.rvtest.R

/**
 * team.redrock.rain.rvtest.adapter.MainAdapter.kt
 * RecyclerViewTest
 *
 * @author 寒雨
 * @since 2023/4/17 下午5:13
 */
class MainAdapter(private val data: List<Data>) : RecyclerView.Adapter<MainAdapter.Holder>() {

    companion object {
        const val TYPE_LESSON = 0
        const val TYPE_COLLEGE = 1
    }

    sealed class Data(val type: Int) {
        data class Lesson(val title: String, val desc: String): Data(TYPE_LESSON)
        data class College(val name: String, val avatar: String): Data(TYPE_COLLEGE)
    }

    sealed class Holder(root: View) : ViewHolder(root) {
        class Lesson(root: View) : Holder(root) {
            val tvTitle: TextView = root.findViewById(R.id.tv_title)
            val tvDesc: TextView = root.findViewById(R.id.tv_desc)
        }
        class College(root: View) : Holder(root) {
            val tvName: TextView = root.findViewById(R.id.tv_name)
            val ivAvatar: ImageView = root.findViewById(R.id.iv_avatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // 拿到 LayoutInflater
        val inflater = LayoutInflater.from(parent.context)
        // inflate item 布局
        val view = inflater.inflate(if (viewType == TYPE_LESSON) R.layout.item_lesson else R.layout.item_college, parent, false)
        return if (viewType == TYPE_LESSON) Holder.Lesson(view) else Holder.College(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int, payloads: MutableList<Any>) {
        val payload = payloads.getOrNull(0) as? String
        holder.apply {
            when (val itemData = data[position]) {
                is Data.Lesson -> {
                    this as Holder.Lesson
                    tvTitle.text = payload ?: itemData.title
                    tvDesc.text = itemData.desc
                }
                is Data.College -> {
                    this as Holder.College
                    tvName.text = payload ?: itemData.name
                    Glide.with(ivAvatar)
                        .load(itemData.avatar)
                        .centerCrop()
                        .into(ivAvatar)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }
}