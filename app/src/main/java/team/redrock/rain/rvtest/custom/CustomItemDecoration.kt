package team.redrock.rain.rvtest.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import team.redrock.rain.rvtest.R

/**
 * team.redrock.rain.rvtest.custom.CustomItemDecoration.kt
 * RecyclerViewTest
 *
 * @author 寒雨
 * @since 2023/4/21 下午7:31
 */
class CustomItemDecoration(private val context: Context) : RecyclerView.ItemDecoration() {

    private val mPaint by lazy {
        Paint().apply {
            color = context.getColor(R.color.black)
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // 在 Item 的下层绘制
        // 也就是说你得利用 getItemOffsets 给 Decoration 腾出一块空间，再在这块区域进行绘制
        // 或者在 Item 的空白区域作画也行
        val childCount = parent.childCount
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0..childCount) {
            // 拿到 Item View
            val view = parent.getChildAt(i) ?: continue
            val top = view.bottom
            val bottom = view.bottom + context.resources.displayMetrics.density.toInt()
            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // 在 Item 的上层绘制
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // 给底部腾出 1dp 位置
        outRect.bottom += context.resources.displayMetrics.density.toInt()
    }
}