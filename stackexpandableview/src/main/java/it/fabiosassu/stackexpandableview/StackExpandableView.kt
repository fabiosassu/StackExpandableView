package it.fabiosassu.stackexpandableview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.IntDef
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.view.ViewCompat
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * A view that allows to perform an iOS notification group like animation.
 *
 * @author fabiosassu
 * @version 1.0.1
 */
class StackExpandableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MotionLayout(context, attrs, defStyle), OnClickListener {

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
            COMPLEX_UNIT_DIP,
            parallaxValue,
            resources.displayMetrics
        ) * shownElements
    private var stackTransition: MotionScene.Transition? = null

    init {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.StackExpandableView, defStyle, 0)
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
        val scene = MotionScene(this)
        stackTransition = createTransition(scene)
        scene.addTransition(stackTransition)
        scene.setTransition(stackTransition)
        setScene(scene)
        redraw()
    }

    /**
     * Create a basic transition programmatically.
     */
    private fun createTransition(scene: MotionScene): MotionScene.Transition {
        val startSetId = ViewCompat.generateViewId()
        val startSet = ConstraintSet()
        startSet.clone(this)
        val endSetId = ViewCompat.generateViewId()
        val endSet = ConstraintSet()
        endSet.clone(this)
        val transitionId = ViewCompat.generateViewId()
        return TransitionBuilder.buildTransition(
            scene,
            transitionId,
            startSetId, startSet,
            endSetId, endSet
        )
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
            val index = widgetList.indexOf<Any> { it.id == view.id }
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
        stackTransition?.let { transition ->
            transition.duration = animationDuration
            if (widgetList.isNotEmpty()) {
                val startConstraint = getConstraintSet(transition.startConstraintSetId)
                startConstraint.clone(this)
                val endConstraintSet = getConstraintSet(transition.endConstraintSetId)
                endConstraintSet.clone(this)
                widgetList.forEachIndexed { index, view ->
                    // set start constraint set
                    val translation = TypedValue.applyDimension(
                        COMPLEX_UNIT_DIP,
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
                            PARENT_ID,
                            TOP,
                            PARENT_ID,
                            BOTTOM,
                            ids,
                            null,
                            CHAIN_PACKED
                        )
                        HORIZONTAL -> {
                            endConstraintSet.createHorizontalChainRtl(
                                PARENT_ID,
                                START,
                                PARENT_ID,
                                END,
                                ids,
                                null,
                                CHAIN_PACKED
                            )
                        }
                    }
                }
                setTransition(transition)
                // set the min height
                viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        when (orientation) {
                            VERTICAL -> {
                                if (measuredHeight > 0) {
                                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                                }
                                val element = if (widgetList.isEmpty()) null else widgetList[0]
                                minHeight =
                                    element?.measuredHeight?.plus(maxTranslation.toInt()) ?: 0

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
    }

    override fun onClick(view: View?) {
        if (isCollapsed) {
            transitionToEnd()
        } else {
            transitionToStart()
        }
        isCollapsed = !isCollapsed
    }

    companion object {
        @IntDef(VERTICAL, HORIZONTAL)
        @Retention(SOURCE)
        annotation class Orientation

        const val VERTICAL = 0
        const val HORIZONTAL = 1
        const val DEFAULT_ITEMS_NUMBER = 3
        const val DEFAULT_PARALLAX_VALUE = 8.toFloat()
        const val DEFAULT_ANIMATION_DURATION = 300
    }

}
