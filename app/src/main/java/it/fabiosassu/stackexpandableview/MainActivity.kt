package it.fabiosassu.stackexpandableview

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import it.fabiosassu.stackexpandableview.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var counter = STARTING_ITEM_NUMBER
    private val padding: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8.toFloat(),
            resources.displayMetrics
        ).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // set data
        binding.horizontalStack.setWidgets(getLayouts(false))
        binding.verticalStack.setWidgets(getLayouts())
        // add items on button click
        binding.addGroupBtn.setOnClickListener {
            counter++
            binding.verticalStack.addWidget(getLayout(counter))
            binding.horizontalStack.addWidget(getLayout(counter, false))
        }
    }

    private fun getLayouts(isVertical: Boolean = true) = mutableListOf<ItemView>().apply {
        for (index in 1..STARTING_ITEM_NUMBER) {
            this.add(getLayout(index, isVertical))
        }
    }.toList()

    private fun getLayout(index: Int, isVertical: Boolean = true): ItemView {
        return ItemView(this@MainActivity).apply {
            id = ViewCompat.generateViewId()
            layoutParams = if (isVertical) ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                setPadding(0, padding, 0, padding)
            } else ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT,
            ).apply {
                setPadding(padding, 0, padding, 0)
            }
            text = "Layout $index"
        }
    }

    companion object {
        const val STARTING_ITEM_NUMBER = 30
    }

}