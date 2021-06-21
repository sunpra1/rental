package softwarica.sunilprasai.cuid10748110.rental.ui.houses

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.House
import softwarica.sunilprasai.cuid10748110.rental.utils.LoadImage

class HousesRVA : RecyclerView.Adapter<HousesRVA.ViewHolder>() {
    private var houses: ArrayList<House> = ArrayList()
    fun swipeHouses(houses: ArrayList<House>) {
        this.houses = houses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.houses_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val house = houses[position]
        house.images?.let {
            if (it.size > 0) {
                val randomNum = (0 until it.size).random()
                LoadImage(object : LoadImage.ImageLoader {
                    override fun onImageLoaded(imageBitmap: Bitmap?) {
                        holder.houseImage.setImageBitmap(imageBitmap)
                    }
                }).execute(it[randomNum].buffer)
            }
        }
        holder.houseFloorsCount.text = house.floors.toString()
        holder.houseAddress.text = house.address
        holder.houseTenanciesCount.text = house.tenancies!!.size.toString()
        holder.houseToLetsCount.text = house.toLets!!.size.toString()
    }

    override fun getItemCount(): Int {
        return houses.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var houseImage: ImageView = itemView.findViewById(R.id.image)
        var houseFloorsCount: TextView = itemView.findViewById(R.id.floorsCount)
        var houseAddress: TextView = itemView.findViewById(R.id.address)
        var houseTenanciesCount: TextView = itemView.findViewById(R.id.tenanciesCount)
        var houseToLetsCount: TextView = itemView.findViewById(R.id.toLetsCount)
    }
}