package softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_history

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

class PaymentsRVItemTouchListener(
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

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(e)
    }

    interface OnPaymentTouch {
        fun onPaymentClick(position: Int)
    }
}