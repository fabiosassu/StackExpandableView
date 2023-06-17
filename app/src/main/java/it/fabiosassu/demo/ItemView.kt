package it.fabiosassu.demo

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.material.button.MaterialButton

/**
 *
 */
class ItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var materialButton: MaterialButton? = null
    var text: String = ""
        set(value) {
            field = value
            materialButton?.text = field
        }

    init {
        init()
    }

    private fun init() {
        inflate(context, R.layout.layout_item, this)
        materialButton = findViewById(R.id.text)
    }

}
