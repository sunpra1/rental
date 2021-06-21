package softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_history_payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.PaymentDetail
import java.util.*
import kotlin.collections.ArrayList

class PaymentDetailsRVA : RecyclerView.Adapter<PaymentDetailsRVA.ViewHolder>() {
    private var paymentDetails: ArrayList<PaymentDetail> = ArrayList();

    fun setPaymentDetails(paymentDetails: ArrayList<PaymentDetail>) {
        this.paymentDetails = paymentDetails
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.payment_detail_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paymentDetail = paymentDetails[position]
        holder.paidFor.text = if (paymentDetail.name == "ADVANCE_ADJUSTED")
            "Advance adjusted"
        else if (paymentDetail.name == "DUE_INCLUDED")
            "Due included"
        else
            holder.itemView.context.resources.getString(R.string.paid_for)
                .format(paymentDetail.name!!.toLowerCase(Locale.ROOT))

        holder.paidCalc.text = if (paymentDetail.isVariableCost!!)
            holder.itemView.context.resources.getString(R.string.for_units)
                .format(paymentDetail.price, paymentDetail.units) else ""

        holder.paidAmount.text =
            if (paymentDetail.isVariableCost!!)
                (paymentDetail.price!! * paymentDetail.units!!).toString()
            else
                paymentDetail.price.toString()

    }

    override fun getItemCount(): Int {
        return paymentDetails.size
    }

    class ViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        val paidFor: TextView = viewHolder.findViewById(R.id.paidFor)
        val paidCalc: TextView = viewHolder.findViewById(R.id.paidCalc)
        val paidAmount: TextView = viewHolder.findViewById(R.id.paidAmount)
    }
}