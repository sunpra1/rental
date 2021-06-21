package softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Utility

class FixedUtilitiesRVA : RecyclerView.Adapter<FixedUtilitiesRVA.ViewHolder>() {
    private var utilities: ArrayList<Utility> = ArrayList()
    private var fixedCostProvidedListener: FixedCostProvidedListener? = null
    fun setUtilities(utilities: ArrayList<Utility>) {
        this.utilities = utilities
    }

    fun setOnVariableCostProvidedListener(listener: FixedCostProvidedListener) {
        this.fixedCostProvidedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fixed_utilities_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val utility = utilities[position]
        holder.fixedCostCB.apply {
            text = utility.name
            setOnCheckedChangeListener { _, isChecked ->
                fixedCostProvidedListener?.onFixedCostProvided(
                    utility,
                    isChecked
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return utilities.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fixedCostCB: CheckBox = itemView.findViewById(R.id.fixedCostCB)
    }

    interface FixedCostProvidedListener {
        fun onFixedCostProvided(utility: Utility, toBeIncluded: Boolean)
    }
}
