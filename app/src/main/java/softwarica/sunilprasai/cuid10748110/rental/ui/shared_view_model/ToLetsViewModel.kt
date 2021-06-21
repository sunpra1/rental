package softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import softwarica.sunilprasai.cuid10748110.rental.model.ToLet

class ToLetsViewModel : ViewModel() {
    private val toLets = MutableLiveData<ArrayList<ToLet>>().apply {
        value = ArrayList()
    }

    fun getToLets(): LiveData<ArrayList<ToLet>> {
        return toLets
    }

    private fun getToLetsValue(): ArrayList<ToLet> {
        return toLets.value!!
    }

    fun setToLets(toLets: ArrayList<ToLet>) {
        this.toLets.value = toLets
    }

    fun getToLet(toLetPosition: Int): ToLet {
        return getToLetsValue()[toLetPosition]
    }

    fun replaceToLet(toLetPosition: Int, toLet: ToLet) {
        getToLetsValue()[toLetPosition] = toLet
        this.toLets.value = getToLetsValue()
    }
}