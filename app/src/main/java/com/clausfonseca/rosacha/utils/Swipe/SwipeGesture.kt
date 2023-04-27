package com.clausfonseca.rosacha.utils.Swipe

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

abstract class SwipeGesture(context: Context, isDeleteGesture: Boolean = false) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    private val disableRightGesture = isDeleteGesture
    private val requeryContext = context
    private var deleteColor = ContextCompat.getColor(context, R.color.red)
    private var deleteIcon = R.drawable.ic_baseline_delete_forever_24
    private val archiveColor = ContextCompat.getColor(context, R.color.blue)
    private var archiveIcon = R.drawable.ic_baseline_edit_24


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
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
        if (disableRightGesture) {
            deleteColor = ContextCompat.getColor(requeryContext, R.color.blue)
            deleteIcon = R.drawable.ic_visibility
            archiveIcon = R.drawable.ic_visibility
        }
        RecyclerViewSwipeDecorator.Builder(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
            .addSwipeLeftBackgroundColor(deleteColor)
            .addSwipeLeftActionIcon(deleteIcon)
            .addSwipeRightBackgroundColor(archiveColor)
            .addSwipeRightActionIcon(archiveIcon)
            .create()
            .decorate()
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}