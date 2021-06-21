package softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy

import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import softwarica.sunilprasai.cuid10748110.rental.R

class UtilitiesRVItemTouchListener(
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
                if (direction == ItemTouchHelper.LEFT) {
                    callback.onUtilityDeleteSelected(viewHolder.adapterPosition)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    callback.onUtilityEditSelected(viewHolder.adapterPosition)
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
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeLeftBackgroundColor(
                        ContextCompat.getColor(
                            recyclerView.context,
                            R.color.colorRed
                        )
                    ).addSwipeLeftActionIcon(R.drawable.trash_white)
                    .addSwipeRightBackgroundColor(
                        ContextCompat.getColor(
                            recyclerView.context,
                            R.color.colorGreen
                        )
                    ).addSwipeRightActionIcon(R.drawable.pencil)
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
        fun onUtilityDeleteSelected(position: Int)
        fun onUtilityEditSelected(position: Int)
    }
}