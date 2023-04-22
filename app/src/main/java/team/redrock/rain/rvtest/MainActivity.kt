package team.redrock.rain.rvtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import team.redrock.rain.rvtest.adapter.MainAdapter
import team.redrock.rain.rvtest.adapter.TagAdapter
import team.redrock.rain.rvtest.custom.CustomItemDecoration
import team.redrock.rain.rvtest.databinding.ActivityMainBinding
import java.util.Collections
import kotlin.concurrent.thread

typealias AdapterLessonData = MainAdapter.Data.Lesson
typealias AdapterCollegeData = MainAdapter.Data.College

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val data = mutableListOf(
        AdapterCollegeData("Carnegie Mellon University", "https://bkimg.cdn.bcebos.com/pic/18d8bc3eb13533fa2d5b5093a2d3fd1f40345b9a?x-bce-process=image/watermark,image_d2F0ZXIvYmFpa2UxNTA=,g_7,xp_5,yp_5"),
        AdapterLessonData("CMU 15-213", "Computer Systems: A Programmer's Perspective"),
        AdapterLessonData("CMU 15-445", "Database Systems"),
        AdapterCollegeData("Stanford University", "https://upload.wikimedia.org/wikipedia/zh/thumb/5/55/Stanford_University_logo.svg/400px-Stanford_University_logo.svg.png"),
        AdapterLessonData("Stanford CS143", "Compilers"),
        AdapterLessonData("Stanford CS144", "Computer Network"),
        AdapterCollegeData("Massachusetts Institute of Technology", "https://upload.wikimedia.org/wikipedia/zh/thumb/4/44/MIT_Seal.svg/400px-MIT_Seal.svg.png"),
        AdapterLessonData("MIT6.824", "Distributed System")
    )

    private val tags = listOf(
        "数据库系统",
        "操作系统",
        "Carnegie Mellon University",
        "计算机网络",
        "编译原理",
        "Stanford",
        "分布式系统",
        "Massachusetts Institute of Technology",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rv.apply {
            layoutAnimation = LayoutAnimationController(AnimationUtils.loadAnimation(this@MainActivity, R.anim.anim_fade_in))
            val manager = FlexboxLayoutManager(this@MainActivity)
            manager.apply {
                justifyContent = JustifyContent.FLEX_START
                alignItems = AlignItems.FLEX_START
            }
            layoutManager = manager
            adapter = MainAdapter(data)
//            addItemDecoration(CustomItemDecoration(this@MainActivity))
            val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    // 允许往上往下拖拽移动
                    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    // 允许左滑右滑删除
                    val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    return makeMovementFlags(dragFlags, swipeFlags)
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // 开启长按拖拽
                    val from = viewHolder.bindingAdapterPosition
                    val to = target.bindingAdapterPosition
                    // 交换下位置
                    Collections.swap(data, from, to)
                    // notify 下
                    adapter!!.notifyItemMoved(from, to)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // 被滑走时
                    val pos = viewHolder.bindingAdapterPosition
                    data.removeAt(pos)
                    adapter!!.notifyItemRemoved(pos)
                }

            })
            helper.attachToRecyclerView(binding.rv)
        }
//        binding.rv.adapter?.notifyItemChanged(0, "卡耐基梅隆大学")
    }
}