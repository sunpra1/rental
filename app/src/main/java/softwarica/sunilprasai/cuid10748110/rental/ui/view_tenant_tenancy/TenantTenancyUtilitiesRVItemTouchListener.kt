package softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy

import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy.UtilitiesRVA

class TenantTenancyUtilitiesRVItemTouchListener(
    recyclerView: RecyclerView,
    callback: OnUtilityTouch
) : RecyclerView.SimpleOnItemTouchListener() {

    private val itemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT && (viewHolder as UtilitiesRVA.ViewHolder).approved) {
                    callback.onUtilityDisapproveSelected(viewHolder.adapterPosition)
                } else if (direction == ItemTouchHelper.RIGHT && !(viewHolder as UtilitiesRVA.ViewHolder).approved) {
                    callback.onUtilityApproveSelected(viewHolder.adapterPosition)
                } else {
                    recyclerView.adapter!!.notifyItemChanged(viewHolder.adapterPosition)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val recyclerViewSwipeDecorator = RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                if ((viewHolder as UtilitiesRVA.ViewHolder).approved) {
                    recyclerViewSwipeDecorator
                        .addSwipeLeftBackgroundColor(
                            ContextCompat.getColor(
                                recyclerView.context,
                                R.color.colorRed
                            )
                        ).addSwipeLeftActionIcon(R.drawable.thumb_down_white)
                } else {
                    recyclerViewSwipeDecorator
                        .addSwipeRightBackgroundColor(
                            ContextCompat.getColor(
                                recyclerView.context,
                                R.color.colorGreen
                            )
                        ).addSwipeRightActionIcon(R.drawable.thumb_up_white)
                }

                recyclerViewSwipeDecorator
                    .create()
                    .decorate()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

    init {
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    interface OnUtilityTouch {
        fun onUtilityApproveSelected(position: Int)
        fun onUtilityDisapproveSelected(position: Int)
    }
}