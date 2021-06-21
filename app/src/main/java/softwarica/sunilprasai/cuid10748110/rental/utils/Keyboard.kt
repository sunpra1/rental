package softwarica.sunilprasai.cuid10748110.rental.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity

object Keyboard {
    fun hide(fragmentActivity: FragmentActivity): Boolean {
        val view: View? = fragmentActivity.currentFocus
        return view?.let {
            val imm =
                fragmentActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } ?: false
    }
}