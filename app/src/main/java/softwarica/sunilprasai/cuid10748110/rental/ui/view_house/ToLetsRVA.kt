package softwarica.sunilprasai.cuid10748110.rental.ui.view_house

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.ToLet
import softwarica.sunilprasai.cuid10748110.rental.utils.yyyy_MM_dd
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ToLetsRVA : RecyclerView.Adapter<ToLetsRVA.ViewHolder>() {
    private var toLets: ArrayList<ToLet> = ArrayList()

    fun setToLets(toLets: ArrayList<ToLet>) {
        this.toLets = toLets
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.house_to_lets_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val toLet = toLets[position]
        holder.roomType.text = toLet.roomType!!.toString()

        val facilitiesInfo = StringBuilder()
        facilitiesInfo.append("Room facilities includes ")
        toLet.facilities!!.mapIndexed { index, facility ->
            when {
                index < toLet.facilities!!.size - 2 -> {
                    facilitiesInfo.append(facility).append(", ")
                }
                index < toLet.facilities!!.size - 1 -> {
                    facilitiesInfo.append(facility).append(" and ")
                }
                else -> {
                    facilitiesInfo.append(facility)
                }
            }
        }
        holder.toLetFacilities.text = facilitiesInfo
        holder.toLetRoomCount.text = toLet.roomCount!!.toString()
        holder.toLetRoomRentAmount.text = toLet.amount!!.toString()
        holder.toLetCreatedDate.text =
            SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(toLet.createdAt!!)
    }

    override fun getItemCount(): Int {
        return toLets.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomType: TextView = itemView.findViewById(R.id.toLetRoomType)
        val toLetFacilities: TextView = itemView.findViewById(R.id.toLetFacilities)
        val toLetRoomCount: TextView = itemView.findViewById(R.id.toLetRoomCount)
        val toLetRoomRentAmount: TextView = itemView.findViewById(R.id.toLetRoomRentAmount)
        val toLetCreatedDate: TextView = itemView.findViewById(R.id.toLetCreatedDate)
    }
}