package softwarica.sunilprasai.cuid10748110.rental.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast

object AppToast {
    fun show(context: Context, message: String, length: Int) {
        val toast = Toast.makeText(context, message, length)
        toast.setGravity(Gravity.TOP or Gravity.CENTER, 0, 0)
        toast.show()
    }
}