package softwarica.sunilprasai.cuid10748110.rental.ui.edit_house

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Image
import softwarica.sunilprasai.cuid10748110.rental.utils.LoadImage

class CurrentImagesRVA : RecyclerView.Adapter<CurrentImagesRVA.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
    }

    private var currentImages: ArrayList<Image> = ArrayList()

    fun setSelectedImages(currentImages: ArrayList<Image>) {
        this.currentImages = currentImages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.current_images_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentImage = currentImages[position]
        LoadImage(object : LoadImage.ImageLoader {
            override fun onImageLoaded(imageBitmap: Bitmap?) {
                holder.image.setImageBitmap(imageBitmap)
            }
        }).execute(currentImage.buffer)
    }

    override fun getItemCount(): Int {
        return currentImages.size
    }
}