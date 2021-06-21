package softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy

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
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy.PaymentsRVA

class TenantTenancyPaymentsRVItemTouchListener(
    context: Context,
    recyclerView: RecyclerView,
    callback: OnPaymentTouch
) : RecyclerView.SimpleOnItemTouchListener() {
    private val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return e?.let {
                    val v: View? = recyclerView.findChildViewUnder(it.x, it.y)
                    v?.let {
                        callback.onPaymentClick(recyclerView.getChildAdapterPosition(v))
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
                if (direction == ItemTouchHelper.LEFT && (viewHolder as PaymentsRVA.ViewHolder).approved) {
                    callback.onPaymentDisapproveSelected(viewHolder.adapterPosition)
                } else if (direction == ItemTouchHelper.RIGHT && !(viewHolder as PaymentsRVA.ViewHolder).approved) {
                    callback.onPaymentApproveSelected(viewHolder.adapterPosition)
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

                if ((viewHolder as PaymentsRVA.ViewHolder).approved) {
                    recyclerViewSwipeDecorator
                        .addSwipeLeftBackgroundColor(
                            ContextCompat.getColor(
                                recyclerView.context,
                                R.color.colorRed
                            )
                        ).addSwipeLeftActionIcon(R.drawable.thumb_down_white)
                } else {
                    recyclerViewSwipeDecorator.addSwipeRightBackgroundColor(
                        ContextCompat.getColor(
                            recyclerView.context,
                            R.color.colorGreen
                        )
                    ).addSwipeRightActionIcon(R.drawable.thumb_up_white)
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

    interface OnPaymentTouch {
        fun onPaymentClick(position: Int)
        fun onPaymentApproveSelected(position: Int)
        fun onPaymentDisapproveSelected(position: Int)
    }
}