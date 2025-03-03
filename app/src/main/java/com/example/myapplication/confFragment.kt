package com.example.myapplication

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.apache.commons.collections4.queue.CircularFifoQueue


class confFragment : Fragment(), UpdateableFragment {
    lateinit var tvLog: TextView
    lateinit var tv1: TextView
    lateinit var tv2: TextView

    private lateinit var v: View

    private var log = CircularFifoQueue<String>(20)
    private lateinit var a: MainActivity// get() = _a!!

    //inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.conf, container, false)!!

        tvLog = v.findViewById(R.id.tvReadLog)
        tvLog.movementMethod = ScrollingMovementMethod()
        tvLog.text = log.toString()
        a = activity as MainActivity


        tv1 = v.findViewById(R.id.textView)
        tv2 = v.findViewById(R.id.textView5)

//        val joystick = v.findViewById<JoystickView>(R.id.vJoystick)
//        joystick.setDirectionType(JoystickView.DirectionType.COMPLETE)
//
//        joystick.apply {
//            setMoveListener {
//                tv1.text = getString(R.string.joystick_direction).format(it.name)
//                val position = this.position
//                tv2.text = getString(R.string.joystick_position).format(position.x, position.y)
//            }
//        }

        val joystick2 = v.findViewById(R.id.joystick) as Joystick
        joystick2.setJoystickListener(object : JoystickListener {
            override fun onDown() {
                // ..
            }

            override fun onDrag(degrees: Float, offset: Float) {
                tv1.text = "$offset"
                tv2.text = "$degrees"
            }

            override fun onUp() {
                // ..
            }
        })




        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    public fun putLog(value: String) {
        log.add("\r\n $value")
        if (!this::tvLog.isInitialized) {
            return
        }
        tvLog.text = log.toString()
    }

    override fun update() {
        if(::a.isInitialized)
            a.b.btMain.isEnabled = false
    }

    override fun fabText(): String {
        return "config"
    }

    override fun buildCmd(): String {
        return ""
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putBoolean("IS_EDITING_KEY", cmdRecorded)
//        outState.putString("RANDOM_GOOD_DEED_KEY", str)
    }
}