package softwarica.sunilprasai.cuid10748110.rental.ui.houses

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

class HousesRVItemTouchListener(
    context: Context,
    recyclerView: RecyclerView,
    callback: OnHouseTouch
) : RecyclerView.SimpleOnItemTouchListener() {

    private val gestureDetector = GestureDetectorCompat(context, object :
        GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return e?.let {
                val v: View? = recyclerView.findChildViewUnder(it.x, it.y)
                v?.let {
                    callback.onHouseClick(recyclerView.getChildAdapterPosition(v))
                    true
                }
            } ?: super.onSingleTapUp(e)
        }
    })

    private val itemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    callback.onHouseDeleteSelected(viewHolder.adapterPosition)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    callback.onHouseEditSelected(viewHolder.adapterPosition)
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
                    ).addSwipeLeftActionIcon(R.drawable.trash_white)
                    .addSwipeRightBackgroundColor(
                        ContextCompat.getColor(
                            recyclerView.context,
                            R.color.colorGreen
                        )
                    ).addSwipeRightActionIcon(R.drawable.pencil)
                    .create()
                    .decorate()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

    init {
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(e)
    }

    interface OnHouseTouch {
        fun onHouseClick(position: Int)
        fun onHouseDeleteSelected(position: Int)
        fun onHouseEditSelected(position: Int)
    }
}