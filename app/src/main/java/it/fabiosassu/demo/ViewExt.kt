package it.fabiosassu.demo

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.view.ViewCompat

/**
 * @author fabiosassu
 * @version 1.0
 */

const val STARTING_ITEM_NUMBER = 30

fun getLayouts(
    context: Context,
    isVertical: Boolean = true
) = mutableListOf<ItemView>().apply {
    for (index in 1..STARTING_ITEM_NUMBER) {
        add(getLayout(context, index, isVertical))
    }
}.toList()

fun getLayout(
    context: Context,
    index: Int,
    isVertical: Boolean = true
) = ItemView(context).apply {
    id = ViewCompat.generateViewId()
    if (isVertical) {
        MATCH_PARENT to WRAP_CONTENT
    } else {
        WRAP_CONTENT to WRAP_CONTENT
    }.let { (width, height) ->
        layoutParams = LayoutParams(width, height)
    }
    text = "Layout $index"
}

