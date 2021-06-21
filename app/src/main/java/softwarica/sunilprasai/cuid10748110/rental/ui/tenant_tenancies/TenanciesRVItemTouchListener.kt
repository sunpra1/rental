package softwarica.sunilprasai.cuid10748110.rental.ui.tenant_tenancies

import android.content.Context
import android.graphics.Canvas
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import softwarica.sunilprasai.cuid10748110.rental.R

class TenanciesRVItemTouchListener(
    context: Context,
    recyclerView: RecyclerView,
    callback: OnTenantTenancyTouch
) : RecyclerView.SimpleOnItemTouchListener() {

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return e?.let {
                    val v = recyclerView.findChildViewUnder(it.x, it.y)
                    v?.let {
                        callback.onTenantTenancyClick(recyclerView.getChildAdapterPosition(v))
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
                    callback.onTenantTenancyDeleteSelected(viewHolder.layoutPosition)
                } else if (direction == ItemTouchHelper.RIGHT && !(viewHolder as TenanciesRVA.ViewHolder).approved) {
                    callback.onTenantTenancyApproveSelected(viewHolder.adapterPosition)
                } else {
                    recyclerView.adapter!!.notifyItemChanged(viewHolder.adapterPosition)
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
                val recyclerViewSwipeDecorator = RecyclerViewSwipeDecorator.Builder(
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

                if (!(viewHolder as TenanciesRVA.ViewHolder).approved) {
                    recyclerViewSwipeDecorator.addSwipeRightBackgroundColor(
                        ContextCompat.getColor(
                            recyclerView.context,
                            R.color.colorGreen
                        )
                    ).addSwipeRightActionIcon(R.drawable.check)
                }

                recyclerViewSwipeDecorator
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

    interface OnTenantTenancyTouch {
        fun onTenantTenancyClick(position: Int)
        fun onTenantTenancyDeleteSelected(position: Int)
        fun onTenantTenancyApproveSelected(position: Int)
    }
}