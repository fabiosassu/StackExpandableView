package it.fabiosassu.demo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.button.MaterialButton

/**
 *
 */
class ItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle) {

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
        LayoutInflater.from(context).inflate(R.layout.layout_item, this, true)
        materialButton = findViewById(R.id.text)
    }

}
