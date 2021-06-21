package softwarica.sunilprasai.cuid10748110.rental.ui.tenant_tenancies

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.utils.LoadImage
import softwarica.sunilprasai.cuid10748110.rental.utils.MMMMM_yyyy
import softwarica.sunilprasai.cuid10748110.rental.utils.yyyy_MM
import softwarica.sunilprasai.cuid10748110.rental.utils.yyyy_MM_dd
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TenanciesRVA : RecyclerView.Adapter<TenanciesRVA.ViewHolder>() {
    private var tenancies: ArrayList<Tenancy> = ArrayList()

    fun setTenantTenancies(tenancies: ArrayList<Tenancy>) {
        this.tenancies = tenancies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.tenant_tenancies_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tenancy = tenancies[position]
        tenancy.house!!.owner!!.image?.let {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    holder.ownerImage.setImageBitmap(imageBitmap)
                }
            }).execute(it.buffer)
        }
        holder.ownerName.text = tenancy.house!!.owner!!.fullName
        holder.houseAddress.text = tenancy.house!!.address

        //Start of tenancyDueInfo
        val today = Calendar.getInstance(Locale.ENGLISH).apply {
            clear(Calendar.AM_PM)
            clear(Calendar.HOUR)
            clear(Calendar.HOUR_OF_DAY)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
        }

        val dateBeingIncreasedByAMonthFromTenancyStartDateTillToday =
            Calendar.getInstance(Locale.ENGLISH).apply {
                time = tenancy.startDate!!
            }
        val due: ArrayList<String> = ArrayList()
        while (dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.before(today.apply {
                set(
                    Calendar.DAY_OF_MONTH,
                    today.get(Calendar.DAY_OF_MONTH) + 1
                )
            })) {
            due.add(
                "${dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.get(Calendar.YEAR)}-${
                    dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.get(
                        Calendar.MONTH
                    ) + 1
                }"
            )
            dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.set(
                Calendar.MONTH,
                dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.get(Calendar.MONTH) + 1
            )
        }
        if (due.size > 0 && tenancy.accrue!! == Tenancy.RentAccrueAt.END) {
            due.removeAt(0)
        }

        val paid = tenancy.payments!!.map {
            "${it.paidForYear}-${it.paiForMonth}"
        }

        val dueButNotPaid = due.filter { paid.indexOf(it) == -1 }
        if (dueButNotPaid.isEmpty()) {
            holder.tenancyDueInfo.text =
                holder.itemView.resources.getString(R.string.you_cleared_all_dues_info)
            holder.tenancyDueInfo.setTextColor(
                holder.itemView.resources.getColor(
                    R.color.colorGreen,
                    null
                )
            )
        } else {
            val dueInfo = StringBuilder()
            dueInfo.append("You have not paid rent for ")
            dueButNotPaid.mapIndexed { index, date ->
                dueInfo.append(
                    SimpleDateFormat(MMMMM_yyyy, Locale.ENGLISH).format(
                        SimpleDateFormat(
                            yyyy_MM, Locale.ENGLISH
                        ).parse(date)!!
                    )
                )

                when {
                    index < dueButNotPaid.size - 2 -> {
                        dueInfo.append(", ")
                    }
                    index < dueButNotPaid.size - 1 -> {
                        dueInfo.append(" and ")
                    }
                    else -> {
                        dueInfo.append("")
                    }
                }
            }
            holder.tenancyDueInfo.text = dueInfo
        }
        //End of tenancyDueInfo
        holder.tenancyRoomCount.text = tenancy.roomCount.toString()
        holder.tenancyRoomRentAmount.text = tenancy.amount.toString()
        holder.tenancyRoomType.text = tenancy.roomType.toString()
        holder.tenancyStartDate.text =
            SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(tenancy.startDate!!)

        if (tenancy.approved!!) {
            holder.tenancyApprovedStatus.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.check_circle_green
                )
            )
        } else {
            holder.tenancyApprovedStatus.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.danger_red
                )
            )
        }
        holder.approved = tenancy.approved!!
    }

    override fun getItemCount(): Int {
        return tenancies.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tenancyApprovedStatus: ImageView = itemView.findViewById(R.id.tenancyApprovedStatus)
        val ownerImage: ImageView = itemView.findViewById(R.id.ownerImage)
        val ownerName: TextView = itemView.findViewById(R.id.ownerName)
        val houseAddress: TextView = itemView.findViewById(R.id.houseAddress)
        val tenancyDueInfo: TextView = itemView.findViewById(R.id.tenancyDueInfo)
        val tenancyRoomCount: TextView = itemView.findViewById(R.id.tenancyRoomCount)
        val tenancyRoomRentAmount: TextView = itemView.findViewById(R.id.tenancyRoomRentAmount)
        val tenancyRoomType: TextView = itemView.findViewById(R.id.tenancyRoomType)
        val tenancyStartDate: TextView = itemView.findViewById(R.id.tenancyStartDate)
        var approved: Boolean = false
    }
}