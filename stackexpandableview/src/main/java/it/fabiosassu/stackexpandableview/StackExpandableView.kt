package it.fabiosassu.stackexpandableview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.IntDef
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintSet

/**
 * A view that allows to perform an iOS notification group like animation.
 *
 * @author fabiosassu
 * @version 1.0.0
 */
class StackExpandableView : MotionLayout, View.OnClickListener {

    @Orientation
    var orientation = VERTICAL
        set(value) {
            field = value
            redraw()
        }
    private var widgetList = mutableListOf<View>()
    private var shownElements: Int = 0
    private var parallaxValue: Float = 0.toFloat()
    private var animationDuration: Int = 0
    private var isCollapsed = true
    private val maxTranslation: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            parallaxValue,
            resources.displayMetrics
        ) * shownElements

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        loadLayoutDescription(R.xml.layout_stack_expandable_widget_scene)
        // Load attributes
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.StackExpandableView, defStyle, 0)
        shownElements =
            a.getInt(R.styleable.StackExpandableView_shownElements, DEFAULT_ITEMS_NUMBER)
        parallaxValue =
            a.getDimension(R.styleable.StackExpandableView_parallaxOffset, DEFAULT_PARALLAX_VALUE)
        animationDuration = a.getInteger(
            R.styleable.StackExpandableView_animationDuration,
            DEFAULT_ANIMATION_DURATION
        )
        orientation = a.getInteger(
            R.styleable.StackExpandableView_orientation,
            VERTICAL
        )

        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        redraw()
    }

    /**
     * Allows to set a new [List] to this widget.
     * Notice that the given views must have an ID set.
     * @param views the [List] of [View]s we want to be set.
     */
    fun setWidgets(views: List<View>?) {
        views?.let {
            widgetList = it.toMutableList()
            redraw()
        }
    }

    /**
     * This method allows to add a new [View]. Notice that the given view must have an ID set.
     *
     * @param view the [View] we want to be added
     */
    fun addWidget(view: View?) {
        view?.let {
            widgetList.add(it)
            redraw()
        }
    }

    /**
     * This method allows to remove an existing [View]. Notice that the given view must have an ID set.
     *
     * @param view the [View] we want to be removed
     */
    fun removeWidget(view: View?) {
        view?.let {
            val index = widgetList.indexOf { it.id == view.id }
            widgetList.removeAt(index)
            redraw()
        }
    }

    private fun redraw() {
        removeAllViews()
        widgetList.reversed().forEach {
            it.setOnClickListener(this)
            addView(it)
        }
        rebuildTransition()
    }

    private fun rebuildTransition() {
        val stackTransition = getTransition(R.id.stackTransition)
        stackTransition.duration = animationDuration
        if (widgetList.isNotEmpty()) {
            val startConstraint = getConstraintSet(R.id.startStackConstraintSet)
            startConstraint.clone(this)
            val endConstraintSet = getConstraintSet(R.id.endStackConstraintSet)
            endConstraintSet.clone(this)
            widgetList.forEachIndexed { index, view ->
                // set start constraint set
                val translation = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    parallaxValue,
                    resources.displayMetrics
                ) * index
                val scale = 1.toFloat() - (index.toFloat() / widgetList.size)
                when (orientation) {
                    VERTICAL -> {
                        startConstraint.setScaleX(view.id, scale)
                        startConstraint.setTranslationY(view.id, translation)
                    }
                    HORIZONTAL -> {
                        startConstraint.setScaleY(view.id, scale)
                        startConstraint.setTranslationX(view.id, translation)
                    }
                }
                startConstraint.setAlpha(
                    view.id,
                    if (index >= shownElements) 0.toFloat() else 1.toFloat()
                )
                // set end constraint set
                when (orientation) {
                    VERTICAL -> {
                        endConstraintSet.setTranslationY(view.id, 0.toFloat())
                        endConstraintSet.setScaleX(view.id, 1.toFloat())
                    }
                    HORIZONTAL -> {
                        endConstraintSet.setTranslationX(view.id, 0.toFloat())
                        endConstraintSet.setScaleY(view.id, 1.toFloat())
                    }
                }
                endConstraintSet.setAlpha(view.id, 1.toFloat())
            }
            val ids = widgetList.map { it.id }.toIntArray()
            if (ids.size > 1) {
                when (orientation) {
                    VERTICAL -> endConstraintSet.createVerticalChain(
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.BOTTOM,
                        ids,
                        null,
                        ConstraintSet.CHAIN_PACKED
                    )
                    HORIZONTAL -> {
                        endConstraintSet.createHorizontalChainRtl(
                            ConstraintSet.PARENT_ID,
                            ConstraintSet.START,
                            ConstraintSet.PARENT_ID,
                            ConstraintSet.END,
                            ids,
                            null,
                            ConstraintSet.CHAIN_PACKED
                        )
                    }
                }
            }
            setTransition(stackTransition)
            // set the min height
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    when (orientation) {
                        VERTICAL -> {
                            if (measuredHeight > 0) {
                                viewTreeObserver.removeOnGlobalLayoutListener(this)
                            }
                            val element = if (widgetList.isEmpty()) null else widgetList[0]
                            minHeight = element?.measuredHeight?.plus(maxTranslation.toInt()) ?: 0

                        }
                        HORIZONTAL -> {
                            if (measuredWidth > 0) {
                                viewTreeObserver.removeOnGlobalLayoutListener(this)
                            }
                            val element = if (widgetList.isEmpty()) null else widgetList[0]
                            minWidth = element?.measuredWidth?.plus(maxTranslation.toInt()) ?: 0
                        }
                    }

                }
            })
        }
    }

    override fun onClick(p0: View?) {
        if (isCollapsed) {
            transitionToEnd()
        } else {
            transitionToStart()
        }
        isCollapsed = !isCollapsed
    }

    companion object {
        @IntDef(VERTICAL, HORIZONTAL)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Orientation

        const val VERTICAL = 0
        const val HORIZONTAL = 1
        const val DEFAULT_ITEMS_NUMBER = 3
        const val DEFAULT_PARALLAX_VALUE = 8.toFloat()
        const val DEFAULT_ANIMATION_DURATION = 300
    }

}
