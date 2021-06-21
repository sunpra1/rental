package softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Utility

class UtilitiesRVA : RecyclerView.Adapter<UtilitiesRVA.ViewHolder>() {
    private var utilities: ArrayList<Utility> = ArrayList()

    fun setUtilities(utilities: ArrayList<Utility>) {
        this.utilities = utilities
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.utilities_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val utility = utilities[position]
        holder.utilityName.text = utility.name!!
        holder.utilityPrice.text = utility.price!!.toString()
        holder.isVariableCost.text = if (utility.isVariableCost!!) "YES" else "NO"
        if (utility.approved!!) {
            holder.utilityApprovedStatus.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.check_circle_green
                )
            )
        } else {
            holder.utilityApprovedStatus.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.danger_red
                )
            )
        }
        holder.approved = utility.approved!!
    }

    override fun getItemCount(): Int {
        return utilities.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val utilityApprovedStatus: ImageView = itemView.findViewById(R.id.utilityApprovedStatus)
        val utilityName: TextView = itemView.findViewById(R.id.utilityName)
        val utilityPrice: TextView = itemView.findViewById(R.id.utilityPrice)
        val isVariableCost: TextView = itemView.findViewById(R.id.isVariableCost)
        var approved: Boolean = false
    }
}
