package softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Payment
import softwarica.sunilprasai.cuid10748110.rental.utils.MMMMM_yyyy
import softwarica.sunilprasai.cuid10748110.rental.utils.yyyy_MM
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PaymentsRVA : RecyclerView.Adapter<PaymentsRVA.ViewHolder>() {
    private var payments: ArrayList<Payment> = ArrayList()

    fun setPayments(payments: ArrayList<Payment>) {
        this.payments = payments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.payments_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val payment = payments[position]
        val exclusiveOfDueAndAdvance =
            payment.paymentDetails!!.filter { it.name != "DUE_INCLUDED" && it.name != "ADVANCE_ADJUSTED" }
        val ifItContainsDueAndAdvance =
            payment.paymentDetails!!.filter { it.name == "DUE_INCLUDED" || it.name == "ADVANCE_ADJUSTED" }

        val paymentSummary = StringBuilder().append("Payment received against ")
        exclusiveOfDueAndAdvance.mapIndexed { index, utility ->
            when {
                index < exclusiveOfDueAndAdvance.size - 2 -> {
                    paymentSummary.append(utility.name!!.toLowerCase(Locale.ROOT)).append(", ")
                }
                index < exclusiveOfDueAndAdvance.size - 1 -> {
                    paymentSummary.append(utility.name!!.toLowerCase(Locale.ROOT)).append(" and ")
                }
                else -> {
                    paymentSummary.append(utility.name!!.toLowerCase(Locale.ROOT))
                }
            }
        }

        paymentSummary.append(". ")
        if (ifItContainsDueAndAdvance.isNotEmpty()) {
            if (ifItContainsDueAndAdvance[0].name == "DUE_INCLUDED")
                paymentSummary.append("Due was included")
            else if (ifItContainsDueAndAdvance[0].name == "ADVANCE_ADJUSTED")
                paymentSummary.append("Advance was adjusted")

            if (ifItContainsDueAndAdvance.size == 2) {
                if (ifItContainsDueAndAdvance[1].name == "DUE_INCLUDED")
                    paymentSummary.append(" and due was included")
                else if (ifItContainsDueAndAdvance[1].name == "ADVANCE_ADJUSTED")
                    paymentSummary.append(" and advance was adjusted")
            }
        }

        holder.paidFor.text =
            holder.itemView.context.resources.getString(R.string.paid_for).format(
                SimpleDateFormat(MMMMM_yyyy, Locale.ENGLISH).format(
                    SimpleDateFormat(
                        yyyy_MM, Locale.ENGLISH
                    ).parse("${payment.paidForYear}-${payment.paiForMonth}")!!
                )
            )
        holder.paymentSummary.text = paymentSummary
        holder.amountPayable.text = payment.amountPayable.toString()
        holder.amountReceived.text = payment.amountReceived.toString()
        if (payment.approved!!) {
            holder.paymentApprovedStatus.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.check_circle_green
                )
            )
        } else {
            holder.paymentApprovedStatus.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.danger_red
                )
            )
        }
        holder.approved = payment.approved!!
    }

    override fun getItemCount(): Int {
        return payments.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentApprovedStatus: ImageView = itemView.findViewById(R.id.paymentApprovedStatus)
        val paidFor: TextView = itemView.findViewById(R.id.paidFor)
        val paymentSummary: TextView = itemView.findViewById(R.id.paymentSummary)
        val amountPayable: TextView = itemView.findViewById(R.id.amountPayable)
        val amountReceived: TextView = itemView.findViewById(R.id.amountReceived)
        var approved: Boolean = false
    }
}