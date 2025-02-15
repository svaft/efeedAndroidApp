package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity.Direction

class odFragment : Fragment(), UpdateableFragment {
    private var cmdRecorded = false
    private var str = "false"

    private var feedUnitG95 = true
    private lateinit var tvFeedUnit: TextView

    //inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        inflater.inflate(R.layout.od_turn, container, false)!!
    lateinit var v: View
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view
        tvFeedUnit =v.findViewById<TextView>(R.id.tvFeedUnit)
        tvFeedUnit.setOnLongClickListener {
            changeFeedUnit()
            true
        }
        if (feedUnitG95) {
            tvFeedUnit.setText(R.string.feed_unit_metric_G95)
        } else {
            tvFeedUnit.setText(R.string.feed_unit_metric_G94)
        }

    }

    private fun changeFeedUnit() {
        if (feedUnitG95) {
            feedUnitG95 = false
            tvFeedUnit.setText(R.string.feed_unit_metric_G94)
            (activity as MainActivity).putLog("set mm\\min: G94")
            (activity as MainActivity).sendCommand("G94")
        } else {
            feedUnitG95 = true
            tvFeedUnit.setText(R.string.feed_unit_metric_G95)
            //            tvFeedValue.setText("0.1");
            (activity as MainActivity).putLog("set mm\\rev: G95")
            (activity as MainActivity).sendCommand("G95")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        cmdRecorded = savedInstanceState!!.getBoolean("IS_EDITING_KEY", false)
//        str = savedInstanceState!!.getString("RANDOM_GOOD_DEED_KEY")!!

    }

    override fun update(a: MainActivity) {
//        a.feedDirection = Direction.Right
//        a.fab.setIconResource(R.drawable.ic_baseline_arrow_forward_24)
    }

    override fun fabText(): String {
        return "Подача"

    }

    override fun buildCmd(a: MainActivity): String {
        val minus =
            if (a.feedDirection == Direction.Right || a.feedDirection == Direction.Backward) "" else "-"
        var gCmd = "G01"
        var gSub: String
        val gLength: String
        var gTaper = ""
        val axis = if (a.feedDirection == Direction.Right || a.feedDirection == Direction.Left) " Z" else " X"
        val tvLen: TextView

        val tvTaperValue = v.findViewById<TextView>(R.id.tvTaperValue3).text.toString().toFloat()

        gSub = "F" + v.findViewById<TextView>(R.id.tvFeedValue3).text
        gLength = v.findViewById<TextView>(R.id.editTextFeedLength3).text.toString()
        if(tvTaperValue > 0){
            var targetTaperFloat = tvTaperValue*gLength.toFloat()///2 // controller use diameter mode for X axis so no need to divide by 2 here
            if(axis == " Z"){
                if(a.internalCut){ // for internal cut inverse positive value of taper
                    targetTaperFloat = -targetTaperFloat
                }
                var taperStr =  String.format("%.3f", targetTaperFloat).replace(",", ".") // "%.$3f".format(targetTaperFloat.toString())
                gTaper = " X${taperStr}"
                (activity as MainActivity).putLog("taper:$taperStr")
            } else {
                TODO("taper on X axis?")
            }
        }
        val out = "$gCmd$axis$minus$gLength$gTaper $gSub"
        return out
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("IS_EDITING_KEY", cmdRecorded)
        outState.putString("RANDOM_GOOD_DEED_KEY", str)
    }
}