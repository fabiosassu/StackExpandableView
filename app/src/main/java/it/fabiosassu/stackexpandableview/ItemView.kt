package it.fabiosassu.stackexpandableview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat

/**
 *
 */
class ItemView : LinearLayoutCompat {

    private var textView: TextView? = null
    var text: String = ""
        set(value) {
            field = value
            textView?.text = field
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.layout_item, this, true)
        textView = findViewById(R.id.text)
    }

}
