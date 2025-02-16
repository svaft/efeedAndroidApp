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
    private lateinit var v: View

    private var log = CircularFifoQueue<String>(20)

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
        if (!this::tvLog.isInitialized)
        return
        tvLog.text = log.toString()
    }

    override fun update() {
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