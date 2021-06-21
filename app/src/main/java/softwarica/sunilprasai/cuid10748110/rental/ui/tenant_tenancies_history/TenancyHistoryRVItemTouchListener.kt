package softwarica.sunilprasai.cuid10748110.rental.ui.tenant_tenancies_history

import android.content.Context
import android.graphics.Canvas
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import softwarica.sunilprasai.cuid10748110.rental.R

class TenancyHistoryRVItemTouchListener(
    context: Context,
    recyclerView: RecyclerView,
    callback: OnTenancyHistoryTouch
) : RecyclerView.SimpleOnItemTouchListener() {
    val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return e?.let {
                    val v: View? = recyclerView.findChildViewUnder(it.x, it.y)
                    v?.let {
                        callback.onTenancyHistoryClick(recyclerView.getChildAdapterPosition(v))
                        true
                    }
                } ?: super.onSingleTapUp(e)
            }
        })

    val itemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    callback.onTenancyHistoryDeleteSelected(viewHolder.adapterPosition)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeLeftBackgroundColor(
                        ContextCompat.getColor(
                            recyclerView.context,
                            R.color.colorRed
                        )
                    )
                    .addSwipeLeftActionIcon(R.drawable.trash_white)
                    .create()
                    .decorate()
            }
        }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(e)
    }

    init {
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    interface OnTenancyHistoryTouch {
        fun onTenancyHistoryClick(position: Int)
        fun onTenancyHistoryDeleteSelected(position: Int)
    }
}