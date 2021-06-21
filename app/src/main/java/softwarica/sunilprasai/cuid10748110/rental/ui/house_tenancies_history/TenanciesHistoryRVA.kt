package softwarica.sunilprasai.cuid10748110.rental.ui.house_tenancies_history

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.utils.LoadImage
import softwarica.sunilprasai.cuid10748110.rental.utils.yyyy_MM_dd
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TenanciesHistoryRVA : RecyclerView.Adapter<TenanciesHistoryRVA.ViewHolder>() {
    private var tenancies: ArrayList<Tenancy> = ArrayList()

    fun setTenancyHistory(tenancies: ArrayList<Tenancy>) {
        this.tenancies = tenancies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.tenancies_histories_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tenancy = tenancies[position]
        tenancy.tenant!!.image?.let {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    holder.tenantImage.setImageBitmap(imageBitmap)
                }
            }).execute(tenancy.tenant!!.image!!.buffer)
        }

        holder.tenantName.text = tenancy.tenant!!.fullName
        holder.tenancyRoomCount.text = tenancy.roomCount.toString()
        holder.tenancyRoomRentAmount.text = tenancy.amount.toString()
        holder.tenancyRoomType.text = tenancy.roomType.toString()
        holder.tenancyStartDate.text =
            SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(tenancy.startDate!!)
        holder.tenancyEndDate.text =
            SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(tenancy.endDate!!)

        var totalAmountPaid = 0;
        tenancy.payments!!.forEach {
            totalAmountPaid += it.amountReceived!!
        }
        holder.tenancyInfo.text =
            holder.itemView.context.getString(R.string.tenancy_history_details_format)
                .format(totalAmountPaid)
    }

    override fun getItemCount(): Int {
        return tenancies.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tenantImage: CircleImageView = itemView.findViewById(R.id.tenantImage)
        val tenantName: TextView = itemView.findViewById(R.id.tenantName)
        val tenancyInfo: TextView = itemView.findViewById(R.id.tenancyInfo)
        val tenancyRoomCount: TextView = itemView.findViewById(R.id.tenancyRoomCount)
        val tenancyRoomRentAmount: TextView = itemView.findViewById(R.id.tenancyRoomRentAmount)
        val tenancyRoomType: TextView = itemView.findViewById(R.id.tenancyRoomType)
        val tenancyStartDate: TextView = itemView.findViewById(R.id.tenancyStartDate)
        val tenancyEndDate: TextView = itemView.findViewById(R.id.tenancyEndDate)
    }
}