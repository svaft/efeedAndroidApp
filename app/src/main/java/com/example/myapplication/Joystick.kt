package com.example.myapplication

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

interface JoystickListener {
    fun onDown()

    /**
     * @param degrees -180 -> 180.
     * @param offset  normalized, 0 -> 1.
     */
    fun onDrag(degrees: Float, offset: Float)

    fun onUp()

}
/**
 * A simple and flexible joystick.
 * Extends FrameLayout and should host one direct child to act as the draggable stick.
 * Use [.setJoystickListener] to observe user inputs.
 */
class Joystick : FrameLayout {
    /**
     * @return Distance in pixels a touch can wander before the joystick thinks the user is
     * manipulating the stick.
     */
    /**
     * @param touchSlop Distance in pixels a touch can wander before the joystick thinks the user is
     * manipulating the stick.
     */
    @get:Suppress("unused")
    @set:Suppress("unused")
    var touchSlop: Int = 0

    private var centerX = 0f
    private var centerY = 0f

    /**
     * @param radius The maximum offset in pixels from the center that the stick is allowed to move.
     */
    @get:Suppress("unused")
    @set:Suppress("unused")
    var radius: Float = 0f

    private var draggedChild: View? = null
    private var detectingDrag = false
    private var dragInProgress = false

    private var downX = 0f
    private var downY = 0f
    private var activePointerId = INVALID_POINTER_ID

    private var locked = false

    /**
     * @param startOnFirstTouch If true, the stick activates immediately on the initial touch.
     * Else, the user must begin to drag their finger across the joystick
     * for the stick to activate.
     */
    @get:Suppress("unused")
    @set:Suppress("unused")
    var isStartOnFirstTouch: Boolean = true
    private var forceSquare = true
    private var hasFixedRadius = false

    enum class MotionConstraint {
        NONE,
        HORIZONTAL,
        VERTICAL
    }

    private var motionConstraint = MotionConstraint.NONE

    private var listener: JoystickListener? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    @Suppress("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop

        if (null != attrs) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.Joystick)
            isStartOnFirstTouch = a.getBoolean(
                R.styleable.Joystick_start_on_first_touch,
                isStartOnFirstTouch
            )
            forceSquare = a.getBoolean(R.styleable.Joystick_force_square, forceSquare)
            hasFixedRadius = a.hasValue(R.styleable.Joystick_radius)
            if (hasFixedRadius) {
                radius =
                    a.getDimensionPixelOffset(R.styleable.Joystick_radius, radius.toInt()).toFloat()
            }
            motionConstraint = MotionConstraint.entries[a.getInt(
                R.styleable.Joystick_motion_constraint,
                motionConstraint.ordinal
            )]
            a.recycle()
        }
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return true
    }

    public override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        centerX = w.toFloat() / 2
        centerY = h.toFloat() / 2
    }

    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed && !hasFixedRadius) {
            recalculateRadius(right - left, bottom - top)
        }
    }

    private fun recalculateRadius(width: Int, height: Int) {
        var stickHalfWidth = 0f
        var stickHalfHeight = 0f
        if (hasStick()) {
            val stick = getChildAt(0)
            stickHalfWidth = stick.width.toFloat() / 2
            stickHalfHeight = stick.height.toFloat() / 2
        }

        radius = when (motionConstraint) {
            MotionConstraint.NONE -> (min(width.toDouble(), height.toDouble()) / 2 - max(
                stickHalfWidth.toDouble(),
                stickHalfHeight.toDouble()
            )).toFloat()

            MotionConstraint.HORIZONTAL -> width.toFloat() / 2 - stickHalfWidth
            MotionConstraint.VERTICAL -> height.toFloat() / 2 - stickHalfHeight
        }
    }

    fun setJoystickListener(listener: JoystickListener?) {
        this.listener = listener

        if (!hasStick()) {
            Log.w(
                LOG_TAG, LOG_TAG + " has no draggable stick, and is therefore not functional. " +
                        "Consider adding a child view to act as the stick."
            )
        }
    }

    /**
     * Locks the stick position when next the user releases it.
     * Note that [JoystickListener.onUp] will not be called upon release.
     * Resets to unlocked state after subsequent touch.
     */
    @Suppress("unused")
    fun lock() {
        locked = true
    }

    @Suppress("unused")
    fun getMotionConstraint(): MotionConstraint {
        return motionConstraint
    }

    @Suppress("unused")
    fun setMotionConstraint(motionConstraint: MotionConstraint) {
        this.motionConstraint = motionConstraint

        if (!hasFixedRadius) recalculateRadius(width, height)
    }

    /*
    TOUCH EVENT HANDLING
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (detectingDrag || !hasStick()) return false

                downX = event.getX(0)
                downY = event.getY(0)
                activePointerId = event.getPointerId(0)

                onStartDetectingDrag()
            }

            MotionEvent.ACTION_MOVE -> {
                if (INVALID_POINTER_ID == activePointerId) return false
                if (detectingDrag && dragExceedsSlop(event)) {
                    onDragStart()
                    return true
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                run {
                    val pointerIndex = event.actionIndex
                    val pointerId = event.getPointerId(pointerIndex)

                    if (pointerId != activePointerId) return false // if active pointer, fall through and cancel!
                }
                run {
                    onTouchEnded()
                    onStopDetectingDrag()
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                onTouchEnded()

                onStopDetectingDrag()
            }
        }

        return false
    }

    override fun onTouchEvent( event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!detectingDrag) return false
                if (isStartOnFirstTouch) onDragStart()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (INVALID_POINTER_ID == activePointerId) return false

                if (dragInProgress) {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val latestX = event.getX(pointerIndex)
                    val latestY = event.getY(pointerIndex)

                    val deltaX = latestX - downX
                    val deltaY = latestY - downY

                    onDrag(deltaX, deltaY)
                    return true
                } else if (detectingDrag && dragExceedsSlop(event)) {
                    onDragStart()
                    return true
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                run {
                    val pointerIndex = event.actionIndex
                    val pointerId = event.getPointerId(pointerIndex)

                    if (pointerId != activePointerId) return false // if active pointer, fall through and cancel!
                }
                run {
                    onTouchEnded()
                    if (dragInProgress) {
                        onDragStop()
                    } else {
                        onStopDetectingDrag()
                    }
                    return true
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                onTouchEnded()

                if (dragInProgress) {
                    onDragStop()
                } else {
                    onStopDetectingDrag()
                }
                return true
            }
        }

        return false
    }

    private fun dragExceedsSlop(event: MotionEvent): Boolean {
        val pointerIndex = event.findPointerIndex(activePointerId)
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)
        val dx = abs((x - downX).toDouble()).toFloat()
        val dy = abs((y - downY).toDouble()).toFloat()

        return when (motionConstraint) {
            MotionConstraint.NONE -> dx * dx + dy * dy > touchSlop * touchSlop
            MotionConstraint.HORIZONTAL -> dx > touchSlop
            MotionConstraint.VERTICAL -> dy > touchSlop
        }
        return false
    }

    private fun onTouchEnded() {
        activePointerId = INVALID_POINTER_ID
    }

    private fun hasStick(): Boolean {
        return childCount > 0
    }

    private fun onStartDetectingDrag() {
        detectingDrag = true
        listener?.onDown()
    }

    private fun onStopDetectingDrag() {
        detectingDrag = false
        if (!locked && null != listener) listener!!.onUp()

        locked = false
    }

    private fun onDragStart() {
        dragInProgress = true
        draggedChild = getChildAt(0)
        draggedChild?.animate()?.cancel()
        onDrag(0f, 0f)
    }

    private fun onDragStop() {
        dragInProgress = false

        if (!locked) {
            draggedChild!!.animate()
                .translationX(0f).translationY(0f)
                .setDuration(STICK_SETTLE_DURATION_MS.toLong())
                .setInterpolator(STICK_SETTLE_INTERPOLATOR)
                .start()
        }

        onStopDetectingDrag()
        draggedChild = null
    }

    /**
     * Where most of the magic happens. What, basic trigonometry isn't magic?!
     */
    private fun onDrag(dx: Float, dy: Float) {
        var x = downX + dx - centerX
        var y = downY + dy - centerY

        when (motionConstraint) {
            MotionConstraint.HORIZONTAL -> y = 0f
            MotionConstraint.VERTICAL -> x = 0f
            MotionConstraint.NONE -> ""
        }

        var offset = sqrt((x * x + y * y).toDouble()).toFloat()
        if (x * x + y * y > radius * radius) {
            x = radius * x / offset
            y = radius * y / offset
            offset = radius
        }

        val radians = atan2(-y.toDouble(), x.toDouble())
        val degrees = (180 * radians / Math.PI).toFloat()

        listener?.onDrag(degrees, if (0f == radius) 0f else offset / radius)

        draggedChild!!.translationX = x
        draggedChild!!.translationY = y
    }

    /*
    FORCE SQUARE
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!forceSquare) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            widthSize
        } else if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            heightSize
        } else {
            if (widthSize < heightSize) widthSize else heightSize
        }

        val finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(finalMeasureSpec, finalMeasureSpec)
    }

    /*
    CENTER CHILD BY DEFAULT
     */
    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        val params = LayoutParams(context, attrs)
        params.gravity = Gravity.CENTER
        return params
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        val params = LayoutParams(p)
        params.gravity = Gravity.CENTER
        return params
    }

    public override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    /*
    ENSURE MAX ONE DIRECT CHILD
     */
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        check(childCount <= 0) { LOG_TAG + " can host only one direct child" }

        super.addView(child, index, params)
    }

    companion object {
        private val LOG_TAG: String = Joystick::class.java.simpleName

        private const val STICK_SETTLE_DURATION_MS = 100
        private val STICK_SETTLE_INTERPOLATOR: Interpolator = DecelerateInterpolator()

        private const val INVALID_POINTER_ID = -1
    }
}
