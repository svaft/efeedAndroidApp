package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton

class JogFragment : Fragment(), UpdateableFragment,
    RotaryKnob.RotaryKnobListener {
    val JOG_X = 0
    val JOG_Z = 1
    var jogLP: Boolean = false
    //   lateinit var  jog: RotaryKnob// by lazy { findViewById<RotaryKnobView3>(R.id.jog) }
    private lateinit var myView: View
    private lateinit var a: MainActivity// get() = _a!!
    companion object {
        fun newInstance() = JogFragment()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);

        // TODO: Use the ViewModel
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myView =  inflater.inflate(R.layout.jog_view, container, false)
        a = activity as MainActivity
        // todo jog
        val jogX = myView.findViewById<RotaryKnob>(R.id.knob)
//        jogX.isVisible = false
        jogX.listener = this
        jogX.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP && jogLP) {
                jogLP = false
                (activity as MainActivity).sendCommand("!S")
            }
            false
        }
        val jogZ = myView.findViewById<RotaryKnob>(R.id.knobZ)
//        jogZ.isVisible = false
        jogZ.listener = this
        jogZ.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP && jogLP) {
                jogLP = false
                (activity as  MainActivity).sendCommand("!S")
            }
            false
        }
        myView.findViewById<MaterialButton>(R.id.btJogZ).setOnClickListener { onClickZ(it) }
        myView.findViewById<MaterialButton>(R.id.btJogX).setOnClickListener { onClickX(it) }
        return myView
    }

    private var currentJog : Int = 0

    override fun onJogRotate(value: Int, delta: Int) {
        if(currentJog == JOG_X) {
            if(delta>0)
                (activity as MainActivity).sendCommand("!w")
            else
                (activity as MainActivity).sendCommand("!s")
        }
        else {
            if(delta>0)
                (activity as MainActivity).sendCommand("!d")
            else
                (activity as MainActivity).sendCommand("!a")
        }
    }
    override fun onJogLongPress(e: MotionEvent) {
//        jogLP = true
//        if(currentJog == JOG_X) {
//            if(e.y < 300){
//                sendCommand("G00 X-200")
//            } else if(e.y > 400){
//                sendCommand("G00 X200")
//            }
//        } else if (currentJog ==JOG_Z){
//            if(e.x < 300){
//                sendCommand("G00 Z-200")
//            } else if(e.x > 400){
//                sendCommand("G00 Z200")
//            }
//        }
    }
    override fun update() {
        if(::a.isInitialized)
            a.b.btMain.isEnabled = false
    }
    override fun fabText(): String {
        return ""
    }
    override fun buildCmd(): String {
       return ""
    }
    private fun onClickZ(v: View){
        currentJog = JOG_Z
        myView.findViewById<MaterialButton>(R.id.btJogX).setBackgroundColor(Color.GRAY)
        myView.findViewById<MaterialButton>(R.id.btJogZ).setBackgroundColor(Color.RED)
        myView.findViewById<View>(R.id.knob).isVisible = false
        myView.findViewById<View>(R.id.knobZ).isVisible = true
    }
    private fun onClickX(v: View){
        currentJog = JOG_X
        myView.findViewById<MaterialButton>(R.id.btJogX).setBackgroundColor(Color.RED)
        myView.findViewById<MaterialButton>(R.id.btJogZ).setBackgroundColor(Color.GRAY)

        myView.findViewById<View>(R.id.knob).isVisible = true
        myView.findViewById<View>(R.id.knobZ).isVisible = false
//        findViewById<ConstraintLayout>(R.id.constraintLayoutJog).isVisible = true
    }
//    fun onJogHide(v: View){
//        findViewById<ConstraintLayout>(R.id.constraintLayoutJog).isVisible = false
//    }
//
//    override fun onJogLongPress(e: MotionEvent) {
//        jogLP = true
//        if(currentJog == JOG_X) {
//            if(e.y < 300){
//                sendCommand("G00 X-200")
//            } else if(e.y > 400){
//                sendCommand("G00 X200")
//            }
//        } else if (currentJog ==JOG_Z){
//            if(e.x < 300){
//                sendCommand("G00 Z-200")
//            } else if(e.x > 400){
//                sendCommand("G00 Z200")
//            }
//        }
////        findViewById<ConstraintLayout>(R.id.constraintLayoutJog).isVisible = false
//    }
//
//    override fun onJogRotate(value: Int, delta: Int) {
//        if(currentJog == JOG_X) {
//            if(delta>0)
//                sendCommand("!w")
//            else
//                sendCommand("!s")
//        }
//        else {
//            if(delta>0)
//                sendCommand("!d")
//            else
//                sendCommand("!a")
//        }
//    }

}