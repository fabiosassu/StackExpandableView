package it.fabiosassu.stackexpandableview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import androidx.annotation.IntDef
import androidx.core.animation.addListener
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * A typealias that represents the callback that notifies the caller about expansion/collapse of the
 * [StackExpandableView].
 */
typealias OnExpansionChangedListener = (StackExpandableView, Boolean) -> Unit

/**
 * A view that allows to perform an iOS notification group like animation.
 *
 * @author fabiosassu
 * @version 1.0.3
 */
class StackExpandableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), OnClickListener {

    @Orientation
    private var orientation = VERTICAL
    private var widgetList = mutableListOf<View>()
    private var shownElements = 0
    private var parallaxValue = 0.toFloat()
    private var animationDuration = 0
    private var expanded = false
    private var animator: Animator? = null
    private var stackList: FrameLayout? = null
    private var originalSizes = SparseArray<Pair<Int, Int>>()

    /**
     * This listener is meant to be used to be notified about expansion/collapse of the [StackExpandableView].
     */
    var onExpansionChangedListener: OnExpansionChangedListener? = null

    init {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.StackExpandableView, defStyle, 0)
        shownElements =
            a.getInt(R.styleable.StackExpandableView_shownElements, DEFAULT_ITEMS_NUMBER)
        parallaxValue =
            a.getDimension(
                R.styleable.StackExpandableView_parallaxOffset,
                DEFAULT_PARALLAX_VALUE
            )
        animationDuration = a.getInteger(
            R.styleable.StackExpandableView_animationDuration,
            DEFAULT_ANIMATION_DURATION
        )
        orientation = a.getInteger(
            R.styleable.StackExpandableView_orientation,
            VERTICAL
        )

        when (orientation) {
            VERTICAL -> addView(
                NestedScrollView(context, attrs, defStyle).apply {
                    addView(generateStackList(MATCH_PARENT, WRAP_CONTENT))
                }
            )

            HORIZONTAL -> addView(
                HorizontalScrollView(context, attrs, defStyle).apply {
                    addView(generateStackList(WRAP_CONTENT, WRAP_CONTENT))
                }
            )
        }

        a.recycle()
    }

    private fun generateStackList(
        width: Int,
        height: Int
    ) = FrameLayout(context).apply {
        layoutParams = LayoutParams(width, height)
    }.also { stackList = it }

    /**
     * Allows to set a new [List] to this widget.
     * Notice that the given views must have an ID set.
     *
     * @param views the [List] of [View]s we want to be set.
     */
    fun setWidgets(views: List<View>?) = views?.let {
        widgetList = it.toMutableList()
        redraw {
            setOriginalSizes()
        }
    }

    /**
     * This method allows to add a new [View]. Notice that the given view must have an ID set.
     *
     * @param view the [View] we want to be added
     */
    fun addWidget(view: View?) = view?.let {
        // add new view as transparent in order to avoid flashes
        it.alpha = 0F
        widgetList.add(it)
        redraw {
            originalSizes.put(it.id, it.measuredWidth to it.measuredHeight)
        }
    }

    /**
     * This method allows to remove an existing [View]. Notice that the given view must have an ID set.
     *
     * @param view the [View] we want to be removed
     */
    fun removeWidget(view: View?) = view?.let {
        val index = widgetList.indexOf<Any> { it.id == view.id }
        widgetList.removeAt(index)
        redraw {
            originalSizes.remove(view.id)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        redraw()
    }

    private fun redraw(
        func: (FrameLayout.() -> Unit)? = null
    ) = stackList?.apply {
        removeAllViews()
        widgetList.reversed().forEach {
            val parent = it.parent
            if (parent is ViewGroup) {
                parent.removeView(it)
            }
            it.setOnClickListener(this@StackExpandableView)
            addView(it)
        }
        post {
            func?.invoke(this)
            setupViews()
        }
    }

    private fun FrameLayout.setOriginalSizes() {
        originalSizes.clear()
        getAddedViews().forEach { view ->
            originalSizes.put(view.id, view.measuredWidth to view.measuredHeight)
        }
    }

    private fun FrameLayout.setupViews() {
        if (childCount > 0) {
            var totalSize = 0
            var firstView: View? = null
            getAddedViews().forEachIndexed { index, view ->
                if (index == 0) {
                    firstView = view
                }
                view.apply {
                    if (expanded) {
                        val (originalWidth, originalHeight) = originalSizes[view.id] ?: return
                        alpha = 1F
                        isVisible = true
                        when (orientation) {
                            VERTICAL -> {
                                scaleX = 1F
                                updateLayoutParams<LayoutParams> {
                                    topMargin = totalSize
                                }
                                totalSize += originalHeight
                            }

                            HORIZONTAL -> {
                                scaleY = 1F
                                updateLayoutParams<LayoutParams> {
                                    marginStart = totalSize
                                }
                                totalSize += originalWidth
                            }
                        }
                        updateLayoutParams<LayoutParams> {
                            if (width != originalWidth) {
                                width = originalWidth
                            }
                            if (height != originalHeight) {
                                height = originalHeight
                            }
                        }
                    } else {
                        val translation = getTranslation(index)
                        val scale = 1.toFloat() - (index.toFloat() / widgetList.size)
                        val hidden = index >= shownElements
                        alpha = if (hidden) 0.toFloat() else 1.toFloat()
                        isVisible = !hidden
                        when (orientation) {
                            VERTICAL -> {
                                scaleX = scale
                                updateLayoutParams<LayoutParams> {
                                    topMargin = translation.toInt()
                                }
                            }

                            HORIZONTAL -> {
                                scaleY = scale
                                updateLayoutParams<LayoutParams> {
                                    marginStart = translation.toInt()
                                }
                            }
                        }
                        if (isVisible) {
                            updateLayoutParams<LayoutParams> {
                                firstView?.measuredWidth?.let {
                                    width = it
                                }
                                firstView?.measuredHeight?.let {
                                    height = it
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun FrameLayout.getAddedViews() = mutableListOf<View>().apply {
        for (index in 0 until childCount) {
            add(getChildAt(index))
        }
    }.asReversed()

    private fun getTranslation(index: Int) = applyDimension(
        COMPLEX_UNIT_DIP,
        parallaxValue,
        resources.displayMetrics
    ) * index

    override fun onClick(view: View?) {
        if (expanded) {
            collapse(true)
        } else {
            expand(true)
        }
    }

    /**
     * This method allows to collapse the [StackExpandableView].
     *
     * @param animated a [Boolean] parameter that is used to determine whether the collapse should
     * be animated or not.
     * @param ping a [Boolean] parameter that is used to determine whether we want the
     * [OnExpansionChangedListener] to be triggered or not after the collapse ends. By default this
     * is true.
     */
    fun collapse(
        animated: Boolean,
        ping: Boolean = true
    ) {
        if (!isEnabled || !expanded) {
            return
        }
        if (animated) {
            val animators = mutableListOf<Animator>()
            stackList?.apply {
                if (childCount > 0) {
                    var firstViewId = 0
                    getAddedViews().forEachIndexed { index, view ->
                        if (index == 0) {
                            firstViewId = view.id
                        }
                        val translation = getTranslation(index)
                        val hidden = index >= shownElements
                        val scaleRatio = 1.toFloat() - (index.toFloat() / widgetList.size)
                        val alpha = ObjectAnimator.ofFloat(
                            view,
                            "alpha",
                            if (hidden) 0.toFloat() else 1.toFloat()
                        ).apply {
                            addListener(
                                onEnd = {
                                    if (!expanded) {
                                        view.isVisible = !hidden
                                    }
                                },
                                onStart = {
                                    view.isVisible = true
                                }
                            )
                        }
                        val propertyName = if (orientation == VERTICAL) "scaleX" else "scaleY"
                        val scale = ObjectAnimator.ofFloat(view, propertyName, scaleRatio)
                        val viewMargin =
                            if (orientation == VERTICAL) view.marginTop else view.marginStart
                        val margin =
                            ValueAnimator.ofInt(viewMargin, translation.toInt()).apply {
                                addUpdateListener {
                                    val animatedMargin = animatedValue as Int
                                    view.updateLayoutParams<LayoutParams> {
                                        if (orientation == VERTICAL) {
                                            topMargin = animatedMargin
                                        } else {
                                            marginStart = animatedMargin
                                        }
                                    }
                                }
                            }

                        val (firstWidth, firstHeight) = originalSizes[firstViewId] ?: return
                        val width =
                            ValueAnimator.ofInt(view.measuredWidth, firstWidth).apply {
                                addUpdateListener {
                                    val animatedSide = animatedValue as Int
                                    view.updateLayoutParams<LayoutParams> {
                                        width = animatedSide
                                    }
                                }
                            }

                        val height =
                            ValueAnimator.ofInt(view.measuredHeight, firstHeight).apply {
                                addUpdateListener {
                                    val animatedSide = animatedValue as Int
                                    view.updateLayoutParams<LayoutParams> {
                                        height = animatedSide
                                    }
                                }
                            }.apply {
                                addListener(
                                    onEnd = {
                                        view.post {
                                            view.setLayerType(LAYER_TYPE_NONE, null)
                                        }
                                    },
                                    onStart = { view.setLayerType(LAYER_TYPE_HARDWARE, null) },
                                    onCancel = { view.setLayerType(LAYER_TYPE_NONE, null) }
                                )
                            }

                        animators.apply {
                            add(alpha)
                            add(scale)
                            add(margin)
                            if (!hidden) {
                                add(width)
                                add(height)
                            }
                        }
                    }
                }
            }
            animator = AnimatorSet().apply {
                addListener(
                    onEnd = {
                        animator = null
                        if (ping) {
                            pingListener()
                        }
                    }
                )
                playTogether(animators)
                duration = animationDuration.toLong()
                start()
            }
        } else {
            redraw()
            if (ping) {
                pingListener()
            }
        }
        expanded = false
    }

    /**
     * This method allows to expand the [StackExpandableView].
     *
     * @param animated a [Boolean] parameter that is used to determine whether the expansion should
     * be animated or not.
     * @param ping a [Boolean] parameter that is used to determine whether we want the
     * [OnExpansionChangedListener] to be triggered or not after the expansion ends. By default this
     * is true.
     */
    fun expand(
        animated: Boolean,
        ping: Boolean = true
    ) {
        if (!isEnabled || expanded) {
            return
        }
        if (animated) {
            val animators = mutableListOf<Animator>()
            stackList?.apply {
                if (childCount > 0) {
                    var totalSize = 0
                    getAddedViews().forEachIndexed { index, view ->
                        val hidden = index >= shownElements
                        val (originalWidth, originalHeight) = originalSizes[view.id] ?: return
                        val alpha = ObjectAnimator.ofFloat(
                            view,
                            "alpha",
                            1.toFloat()
                        ).apply {
                            addListener(
                                onStart = {
                                    view.isVisible = true
                                }
                            )
                        }
                        val propertyName = if (orientation == VERTICAL) "scaleX" else "scaleY"
                        val scale = ObjectAnimator.ofFloat(view, propertyName, 1F)
                        val viewMargin =
                            if (orientation == VERTICAL) view.marginTop else view.marginStart
                        val margin =
                            ValueAnimator.ofInt(viewMargin, totalSize)
                                .apply {
                                    addUpdateListener {
                                        val animatedMargin = animatedValue as Int
                                        view.updateLayoutParams<LayoutParams> {
                                            if (orientation == VERTICAL) {
                                                topMargin = animatedMargin
                                            } else {
                                                marginStart = animatedMargin
                                            }
                                        }
                                    }
                                }

                        val width =
                            ValueAnimator.ofInt(view.measuredWidth, originalWidth).apply {
                                addUpdateListener {
                                    val animatedSide = animatedValue as Int
                                    view.updateLayoutParams<LayoutParams> {
                                        width = animatedSide
                                    }
                                }
                            }

                        val height =
                            ValueAnimator.ofInt(view.measuredHeight, originalHeight).apply {
                                addUpdateListener {
                                    val animatedSide = animatedValue as Int
                                    view.updateLayoutParams<LayoutParams> {
                                        height = animatedSide
                                    }
                                }
                            }.apply {
                                addListener(
                                    onEnd = {
                                        view.post {
                                            view.setLayerType(LAYER_TYPE_NONE, null)
                                        }
                                    },
                                    onStart = { view.setLayerType(LAYER_TYPE_HARDWARE, null) },
                                    onCancel = { view.setLayerType(LAYER_TYPE_NONE, null) }
                                )
                            }

                        animators.apply {
                            add(alpha)
                            add(scale)
                            add(margin)
                            if (!hidden) {
                                add(width)
                                add(height)
                            }
                        }

                        totalSize += if (orientation == VERTICAL) originalHeight else originalWidth
                    }
                }
            }
            animator = AnimatorSet().apply {
                addListener(
                    onEnd = {
                        animator = null
                        if (ping) {
                            pingListener()
                        }
                    },
                )
                playTogether(animators)
                duration = animationDuration.toLong()
                start()
            }
        } else {
            redraw()
            if (ping) {
                pingListener()
            }
        }
        expanded = true
    }

    private fun pingListener() = onExpansionChangedListener?.invoke(this, expanded)

    override fun onSaveInstanceState() = SavedState(
        super.onSaveInstanceState(),
        orientation,
        widgetList,
        shownElements,
        parallaxValue,
        animationDuration,
        expanded,
        originalSizes
    )

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        orientation = state.orientation
        widgetList = state.widgetList
        shownElements = state.shownElements
        parallaxValue = state.parallaxValue
        animationDuration = state.animationDuration
        expanded = state.expanded
        originalSizes = state.originalSizes
        requestLayout()
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

    @Parcelize
    data class SavedState(
        val state: Parcelable?,
        val orientation: Int,
        val widgetList: @RawValue MutableList<View>,
        val shownElements: Int,
        val parallaxValue: Float,
        val animationDuration: Int,
        val expanded: Boolean,
        val originalSizes: SparseArray<Pair<Int, Int>>
    ) : BaseSavedState(state)
}