package softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy_payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.PaymentDetail
import softwarica.sunilprasai.cuid10748110.rental.model.Utility

class FixedUtilitiesPaymentRVA : RecyclerView.Adapter<FixedUtilitiesPaymentRVA.ViewHolder>() {
    private var paymentDetails: ArrayList<PaymentDetail> = ArrayList()
    private var newUtilities: ArrayList<Utility> = ArrayList()

    private var fixedCostUpdatedListener: FixedCostUpdatedListener? = null
    fun setPaymentDetails(
        paymentDetails: ArrayList<PaymentDetail>,
        newUtilities: ArrayList<Utility>
    ) {
        this.paymentDetails = paymentDetails
        this.newUtilities = newUtilities
    }

    fun setOnVariableCostUpdatedListener(listener: FixedCostUpdatedListener) {
        this.fixedCostUpdatedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fixed_utilities_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < paymentDetails.size) {
            val paymentDetail = paymentDetails[position]
            holder.fixedCostCB.apply {
                text = paymentDetail.name
                isChecked = true
                setOnCheckedChangeListener { _, isChecked ->
                    fixedCostUpdatedListener?.onFixedCostUpdated(
                        Utility().apply {
                            name = paymentDetail.name
                            isVariableCost = paymentDetail.isVariableCost
                            price = paymentDetail.price
                            approved = true
                        },
                        isChecked
                    )
                }
            }
        } else {
            val utility = newUtilities[position - paymentDetails.size]
            holder.fixedCostCB.apply {
                text = utility.name
                isChecked = false
                setOnCheckedChangeListener { _, isChecked ->
                    fixedCostUpdatedListener?.onFixedCostUpdated(
                        utility,
                        isChecked
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return paymentDetails.size + newUtilities.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fixedCostCB: CheckBox = itemView.findViewById(R.id.fixedCostCB)
    }

    interface FixedCostUpdatedListener {
        fun onFixedCostUpdated(utility: Utility, toBeIncluded: Boolean)
    }
}
