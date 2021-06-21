package softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_payment

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Utility

private const val TAG = "VariableUtilitiesRVA"

class VariableUtilitiesRVA : RecyclerView.Adapter<VariableUtilitiesRVA.ViewHolder>() {
    private var utilities: ArrayList<Utility> = ArrayList()
    private var variableCostProvidedListener: VariableCostProvidedListener? = null

    fun setUtilities(utilities: ArrayList<Utility>) {
        this.utilities = utilities
    }

    fun setOnVariableCostProvidedListener(listener: VariableCostProvidedListener) {
        this.variableCostProvidedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.variable_utilities_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val utility = utilities[position]
        holder.apply {
            variableCostTil.isHintEnabled = true
            variableCostTil.hint = utility.name
            variableCostEt.hint = utility.name
            variableCostEt.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if (event != null && event.action == KeyEvent.ACTION_UP) {
                        variableCostEt.text?.let {
                            if (it.trim().isNotEmpty() && it.trim().isDigitsOnly()) {
                                val value: Int = it.trim().toString().toInt()
                                variableCostProvidedListener?.onVariableCostProvided(
                                    utility,
                                    value
                                )
                            } else {
                                variableCostProvidedListener?.onVariableCostProvided(
                                    utility,
                                    0
                                )
                            }
                            return true
                        }
                    }
                    return false
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return utilities.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val variableCostTil: TextInputLayout = itemView.findViewById(R.id.variableCostTil)
        val variableCostEt: TextInputEditText = itemView.findViewById(R.id.variableCostEt)
    }

    interface VariableCostProvidedListener {
        fun onVariableCostProvided(utility: Utility, value: Int)
    }
}
