package softwarica.sunilprasai.cuid10748110.rental.ui.to_lets

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

class ToLetsRVItemTouchListener(
    context: Context,
    recyclerView: RecyclerView,
    callback: ToLetsRVItemTouchListener.OnToLetTouch
) : RecyclerView.SimpleOnItemTouchListener() {

    val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return e?.let {
                    val v = recyclerView.findChildViewUnder(it.x, it.y)
                    v?.let {
                        callback.onToLetClicked(recyclerView.getChildAdapterPosition(v))
                        true
                    }
                } ?: super.onSingleTapUp(e)
            }
        })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(e)
    }

    interface OnToLetTouch {
        fun onToLetClicked(position: Int)
    }
}