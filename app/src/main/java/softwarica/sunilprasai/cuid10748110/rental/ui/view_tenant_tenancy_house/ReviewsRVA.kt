package softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_house

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Review

class ReviewsRVA(private val reviews: ArrayList<Review>) :
    RecyclerView.Adapter<ReviewsRVA.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.reviews_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]
        val context = holder.itemView.context
        holder.verifiedTenantOrOwner.text =
            context.getString(R.string.verified_what).format(review.context)
        holder.reviewComment.text = review.comment
        when (review.rating) {
            1 -> {
                holder.starOne.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starTwo.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
                holder.starThree.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
                holder.starFour.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
                holder.starFive.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
            }

            2 -> {
                holder.starOne.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starTwo.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starThree.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
                holder.starFour.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
                holder.starFive.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
            }

            3 -> {
                holder.starOne.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starTwo.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starThree.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starFour.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
                holder.starFive.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
            }

            4 -> {
                holder.starOne.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starTwo.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starThree.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starFour.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starFive.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_dark
                    )
                )
            }

            5 -> {
                holder.starOne.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starTwo.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starThree.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starFour.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
                holder.starFive.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.star_yellow
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val verifiedTenantOrOwner: TextView = itemView.findViewById(R.id.verifiedTenantOrOwner)
        val reviewComment: TextView = itemView.findViewById(R.id.reviewComment)
        val starOne: ImageButton = itemView.findViewById(R.id.startOne)
        val starTwo: ImageButton = itemView.findViewById(R.id.startTwo)
        val starThree: ImageButton = itemView.findViewById(R.id.startThree)
        val starFour: ImageButton = itemView.findViewById(R.id.startFour)
        val starFive: ImageButton = itemView.findViewById(R.id.startFive)
    }
}