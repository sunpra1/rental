package softwarica.sunilprasai.cuid10748110.rental.ui.edit_house

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.utils.FileHandler
import softwarica.sunilprasai.cuid10748110.rental.utils.VALID_IMAGE_SIZE

class SelectedImagesRVA : RecyclerView.Adapter<SelectedImagesRVA.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val deleteImage: TextView = itemView.findViewById(R.id.deleteImage)
    }

    private var selectedImages: ArrayList<Uri> = ArrayList()
    private var callback: OnSelectedImageDeleteClickListener? = null

    fun setOnSelectedImageDeleteClickListener(callback: OnSelectedImageDeleteClickListener) {
        this.callback = callback
    }

    fun setSelectedImages(selectedImages: ArrayList<Uri>) {
        this.selectedImages = selectedImages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.selected_images_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selectedImage = selectedImages[position]
        holder.image.setImageURI(selectedImage)
        if (FileHandler.getFileSize(selectedImage, holder.itemView.context) > VALID_IMAGE_SIZE) {
            holder.image.setBackgroundResource(R.drawable.red_border)
        }

        holder.deleteImage.setOnClickListener {
            callback?.onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return selectedImages.size
    }

    interface OnSelectedImageDeleteClickListener {
        fun onDeleteClick(position: Int)
    }
}