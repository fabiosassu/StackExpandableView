package it.fabiosassu.demo

import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.view.ViewCompat
import it.fabiosassu.demo.databinding.ActivityMainBinding
import it.fabiosassu.demo.databinding.ActivityMainBinding.inflate
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var counter = STARTING_ITEM_NUMBER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        with(binding) {
            // set data
            horizontalStack.setWidgets(getLayouts(false))
            verticalStack.setWidgets(getLayouts())
            // add items on button click
            addGroupBtn.setOnClickListener {
                counter++
                verticalStack.addWidget(getLayout(counter))
                horizontalStack.addWidget(getLayout(counter, false))
            }
        }
    }

    private fun getLayouts(isVertical: Boolean = true) = mutableListOf<ItemView>().apply {
        for (index in 1..STARTING_ITEM_NUMBER) {
            add(getLayout(index, isVertical))
        }
    }.toList()

    private fun getLayout(index: Int, isVertical: Boolean = true) =
        ItemView(this@MainActivity).apply {
            id = ViewCompat.generateViewId()
            if (isVertical) {
                MATCH_PARENT to WRAP_CONTENT
            } else {
                WRAP_CONTENT to MATCH_PARENT
            }.let { (width, height) ->
                layoutParams = LayoutParams(width, height)
            }
            text = "Layout $index"
        }

    companion object {
        const val STARTING_ITEM_NUMBER = 30
    }

}