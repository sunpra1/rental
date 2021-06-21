package softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy_payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.PaymentDetail
import softwarica.sunilprasai.cuid10748110.rental.model.Utility
import kotlin.math.abs

class OtherAdjustmentsRVA : RecyclerView.Adapter<OtherAdjustmentsRVA.ViewHolder>() {
    private var otherAdjustments: ArrayList<PaymentDetail> = ArrayList()
    private var otherAdjustmentProvidedListener: OtherAdjustmentProvidedListener? = null
    fun setOtherAdjustments(otherAdjustments: ArrayList<PaymentDetail>) {
        this.otherAdjustments = otherAdjustments
    }

    fun setOnOtherAdjustmentsProvidedListener(listener: OtherAdjustmentProvidedListener) {
        this.otherAdjustmentProvidedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fixed_utilities_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val adjustment = otherAdjustments[position]
        holder.fixedCostCB.apply {
            isChecked = true
            text =
                if (adjustment.name!! == "ADVANCE_ADJUSTED") "DEDUCTED OLD ADVANCE OF ${
                    abs(
                        adjustment.price!!
                    )
                }"
                else "ADDED OLD DUE OF ${abs(adjustment.price!!)}"
            setOnCheckedChangeListener { _, isChecked ->
                otherAdjustmentProvidedListener?.onOtherAdjustmentProvided(
                    Utility().apply {
                        name = adjustment.name
                        isVariableCost = adjustment.isVariableCost
                        price = adjustment.price
                        approved = true
                    },
                    isChecked
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return otherAdjustments.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fixedCostCB: CheckBox = itemView.findViewById(R.id.fixedCostCB)
    }

    interface OtherAdjustmentProvidedListener {
        fun onOtherAdjustmentProvided(utility: Utility, toBeIncluded: Boolean)
    }
}
