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

    //inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        inflater.inflate(R.layout.conf, container, false)!!
    lateinit var v: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view
        tvLog = v.findViewById(R.id.tvReadLog)
        tvLog.movementMethod = ScrollingMovementMethod()
        tvLog.text = log.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private var log = CircularFifoQueue<String>(20)

    public fun putLog(value: String) {
        log.add("\r\n $value")
        if (!this::tvLog.isInitialized)
        return
        tvLog.text = log.toString()
    //        if (mutex.tryAcquire()) {
//            try {
//                runOnUiThread {
//                    tvRead.text = log.toString()
////                    tvZLabel.text = "Z: %.${2}f".format(mZ)
////                    tvXLabel.text = "X: %.${2}f".format(mX)
//
//                    mutex.release()
//                }
//            } catch (ex: Exception) {
//                Log.i("---", "Exception in thread")
//            }
//        }
    }

    override fun update(a: MainActivity) {
    }

    override fun fabText(): String {
        return "config"
    }

    override fun buildCmd(a: MainActivity): String {
        return ""
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putBoolean("IS_EDITING_KEY", cmdRecorded)
//        outState.putString("RANDOM_GOOD_DEED_KEY", str)
    }
}