package softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy_payment

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.PaymentDetail
import softwarica.sunilprasai.cuid10748110.rental.model.Utility

private const val TAG = "VariableUtilitiesRVA"

class VariableUtilitiesPaymentRVA : RecyclerView.Adapter<VariableUtilitiesPaymentRVA.ViewHolder>() {
    private var paymentDetails: ArrayList<PaymentDetail> = ArrayList()
    private var newUtilities: ArrayList<Utility> = ArrayList()

    private var variableCostUpdatedListener: VariableCostUpdatedListener? = null

    fun setPaymentDetails(
        paymentDetails: ArrayList<PaymentDetail>,
        newUtilities: ArrayList<Utility>
    ) {
        this.paymentDetails = paymentDetails
        this.newUtilities = newUtilities
    }

    fun setOnVariableCostUpdatedListener(listener: VariableCostUpdatedListener) {
        this.variableCostUpdatedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.variable_utilities_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < paymentDetails.size) {
            val paymentDetail = paymentDetails[position]
            holder.apply {
                variableCostTil.isHintEnabled = true
                variableCostTil.hint = paymentDetail.name
                variableCostEt.hint = paymentDetail.name
                variableCostEt.setText(paymentDetail.units.toString())
                variableCostEt.setOnKeyListener(object : View.OnKeyListener {
                    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                        if (event != null && event.action == KeyEvent.ACTION_UP) {
                            variableCostEt.text?.let {
                                if (it.trim().isNotEmpty() && it.trim().isDigitsOnly()) {
                                    val value: Int = it.trim().toString().toInt()
                                    variableCostUpdatedListener?.onVariableCostUpdated(
                                        Utility().apply {
                                            name = paymentDetail.name
                                            isVariableCost = paymentDetail.isVariableCost
                                            price = paymentDetail.price
                                            approved = true
                                        },
                                        value
                                    )
                                } else {
                                    variableCostUpdatedListener?.onVariableCostUpdated(
                                        Utility().apply {
                                            name = paymentDetail.name
                                            isVariableCost = paymentDetail.isVariableCost
                                            price = paymentDetail.price
                                            approved = true
                                        },
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
        } else {
            val utility = newUtilities[position - paymentDetails.size]
            holder.apply {
                variableCostTil.isHintEnabled = true
                variableCostTil.hint = utility.name
                variableCostEt.hint = utility.name
                variableCostEt.setText("")
                variableCostEt.setOnKeyListener(object : View.OnKeyListener {
                    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                        if (event != null && event.action == KeyEvent.ACTION_UP) {
                            variableCostEt.text?.let {
                                if (it.trim().isNotEmpty() && it.trim().isDigitsOnly()) {
                                    val value: Int = it.trim().toString().toInt()
                                    variableCostUpdatedListener?.onVariableCostUpdated(
                                        utility,
                                        value
                                    )
                                } else {
                                    variableCostUpdatedListener?.onVariableCostUpdated(
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
    }

    override fun getItemCount(): Int {
        return paymentDetails.size + newUtilities.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val variableCostTil: TextInputLayout = itemView.findViewById(R.id.variableCostTil)
        val variableCostEt: TextInputEditText = itemView.findViewById(R.id.variableCostEt)
    }

    interface VariableCostUpdatedListener {
        fun onVariableCostUpdated(utility: Utility, value: Int)
    }
}
