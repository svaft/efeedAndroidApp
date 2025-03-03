package com.example.myapplication

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import kotlin.math.atan2
import kotlin.math.roundToInt


class RotaryKnob @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {
    private val gestureDetector: GestureDetectorCompat
    private var maxValue = 9
    private var minValue = 0
    var listener: RotaryKnobListener? = null
    private var value = 0

    private var e1_prev_x: Float = 0f
    private var e1_prev_y : Float = 0f
    private var rotationDegrees2_prev : Float = 0f
    private var knob_degree =0f
    private var total_value =0f

    private val knobImageView: ImageView by lazy { findViewById<ImageView>(R.id.knobImageView) }
    private val knobTopImageView: ImageView by lazy { findViewById<ImageView>(R.id.knobTOP) }
    private var knobDrawable: Drawable? = null
    private var knobTopDrawable: Drawable? = null
    private var divider = 360f / (maxValue - minValue)
    private val SWIPE_THRESHOLD = 270
    private val SWIPE_VELOCITY_THRESHOLD = 100

    interface RotaryKnobListener {
        fun onJogRotate(value: Int, delta: Int)
        fun onJogLongPress(e: MotionEvent)
    }

    init {
        this.maxValue = maxValue + 1
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.rotary_view,  this)
        gestureDetector = GestureDetectorCompat(context, this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RotaryKnob,
            0,
            0
        ).apply {
            try {
                minValue = getInt(R.styleable.RotaryKnob_minValue, 0)
                maxValue = getInt(R.styleable.RotaryKnob_maxValue, 9)
                divider = 300f / (maxValue - minValue)
                value = getInt(R.styleable.RotaryKnob_initialValue, 0)
                knobDrawable = getDrawable(R.styleable.RotaryKnob_knobDrawable)
                knobTopDrawable = getDrawable(R.styleable.RotaryKnob_knobTopDrawable)
                knobImageView.setImageDrawable(knobDrawable)
                knobTopImageView.setImageDrawable(knobTopDrawable)
            } finally {
                recycle()
            }
        }
    }

    private var dirDetector:Boolean = false
    private var ddX:Float = 0f
    private var ddY:Float = 0f
    private var direction:Int = 0

//    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float) : Boolean {
        var rotationDegrees = calculateAngle(e2.x, e2.y)
        if (e1 == null)
            return false
//        Log.d("KNOB"," to degree:$rotationDegrees e1(x,y) ${e1.x};${e1.y}  e2(x,y) ${e2.x};${e2.y}")
        if(dirDetector){

            if (e1.x!= e1_prev_x && e1.y !=e1_prev_y) {
                e1_prev_x = e1.x
                e1_prev_y = e1.y
                ddX = 0f
                ddY = 0f
            }

            ddX+=distanceX
            ddY+=distanceY
            if(Math.abs(ddX) > SWIPE_THRESHOLD || Math.abs(ddY) > SWIPE_THRESHOLD){
                var angle = Math.toDegrees(Math.atan((ddX/ddY).toDouble()))
                angle = Math.abs(angle)
                direction = 0
                if(angle > 68f )
                    direction = 1
                if(angle < 22f )
                    direction = 2
                if(angle > 22f && angle < 68f )
                    direction = 3

                Log.d("KNOB","dirDetector distX:$ddX   distY:$ddY   angle:$angle   direction:$direction")
                ddX = 0f
                ddY = 0f
                dirDetector = false
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

//                val rotationDegrees1 = calculateAngle(e1.x, e1.y)
//            Log.d("KNOB", "\nnew_rotate  $rotationDegrees1 $rotationDegrees")
                e1_prev_x = e1.x
                e1_prev_y = e1.y

                rotationDegrees2_prev = rotationDegrees
//                rotationDegrees -= rotationDegrees1
//                rotationDegrees /= 6
                rotationDegrees = 0f
                return true
            } else{
                return true
            }
        }

//        if (e1.x!= e1_prev_x && e1.y !=e1_prev_y) {
//            dirDetector = true
//            return true
//            e1_prev_x = e1.x
//            e1_prev_y = e1.y
//        }

//        Log.d("KNOB"," to degree:$rotationDegrees e1(x,y) ${e1.x};${e1.y}  e2(x,y) ${e2.x};${e2.y}")
        if (e1.x!= e1_prev_x && e1.y !=e1_prev_y){
            val rotationDegrees1 = calculateAngle(e1.x, e1.y)
//            Log.d("KNOB", "\nnew_rotate  $rotationDegrees1 $rotationDegrees")
            e1_prev_x = e1.x
            e1_prev_y = e1.y

            rotationDegrees2_prev = rotationDegrees
            rotationDegrees -= rotationDegrees1
            rotationDegrees = rotationDegrees/6
        } else{
            var rotationDegrees_delta = rotationDegrees - rotationDegrees2_prev
            rotationDegrees2_prev = rotationDegrees
            rotationDegrees = rotationDegrees_delta
        }
        knob_degree += rotationDegrees


        if(rotationDegrees<-180){
            rotationDegrees += 360
        } else if(rotationDegrees>180){
            rotationDegrees -= 360
        }
        if(knob_degree<-180){
            knob_degree += 360
        } else if(knob_degree>180){
            knob_degree -= 360
        }
        xz(rotationDegrees, knob_degree, direction)
        return true
    }

    private fun xz(rotationDelta:Float, knobDegree:Float, dir:Int ){
        total_value += rotationDelta
        if (knob_degree >= -180 && knob_degree <= 180) {
            val newValue = ((total_value / divider).roundToInt() + minValue)
            if(newValue!=value) {
                val kg = (total_value / divider).roundToInt() *divider
                setKnobPosition(kg)
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                listener!!.onJogRotate(newValue, newValue - value)
                value = newValue
            }
        }
    }

    private fun calculateAngle(x: Float, y: Float): Float {
        val px = (x / width.toFloat()) - 0.5
        val py = ( 1 - y / height.toFloat()) - 0.5
        var angle = -(Math.toDegrees(atan2(py, px)))
            .toFloat() + 90
        if (angle > 180) angle -= 360
        return angle
    }

    private fun setKnobPosition(deg: Float) {
        val matrix = Matrix()
        knobImageView.scaleType = ImageView.ScaleType.MATRIX
        matrix.postRotate(deg, width.toFloat()/2, height.toFloat() / 2)
        knobImageView.imageMatrix = matrix
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("KNOB","onTouch action:${event.action}")
        return if (gestureDetector.onTouchEvent(event))
            true
        else
            super.onTouchEvent(event)
    }
    override fun onDown(event: MotionEvent): Boolean {
        Log.d("KNOB","onDown")
        return true
    }
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d("KNOB","onSingleTapUp")
        return false
    }


    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float)
            : Boolean {
        Log.d("KNOB","fling")
        return false
        if(e1 == null)
            return false
        val diffY = e2.y - e1.y
        val diffX = e2.x - e1.x
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    Log.d("KNOB","swipeRight")
                } else {
                    Log.d("KNOB","swipeLeft")
                }
                true
            }
        } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffY > 0) {
                Log.d("KNOB","swipeBottom")
            } else {
                Log.d("KNOB","swipeTop")
            }
            true
        }
        return false
    }


    override fun onLongPress(e: MotionEvent) {
        Log.d("KNOB","longpress:${e.x}  ${e.y}")
        listener!!.onJogLongPress(e)
    }

    override fun onShowPress(e: MotionEvent) {}
}
