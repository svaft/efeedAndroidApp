package com.example.myapplication

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity.Direction
import java.util.Locale

class odFragment(i: Int) : Fragment(), UpdateableFragment {
    private var mOd = i

    private var cmdRecorded = false
    private var str = "false"

    private var feedUnitG95 = true

    private lateinit var tvFeedUnit: TextView
    private lateinit var etFeedLength: EditText
    private lateinit var etFeedValue: EditText
    private lateinit var etTaperValue: EditText
    private lateinit var swLoop: Switch
    private lateinit var etOuterCutTargetDiameter: EditText

    lateinit var v: View

//    private var _binding2: IdTurnBinding? = null
//    private val bi get() = _binding2!!
//
//    private var _binding: OdTurnBinding? = null
//    private val b get() = _binding!!

//    private var _a: MainActivity? = null
    private lateinit var a: MainActivity// get() = _a!!
    //inflate the layout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myView: View = inflater.inflate(R.layout.od_turn, container, false)!!
        if(mOd == 0){
            myView.findViewById<ImageView>(R.id.imageInnerProgram).setImageResource(R.drawable.left_outer_full_ex3d)
        } else {
            myView.findViewById<ImageView>(R.id.imageInnerProgram).setImageResource(R.drawable.left_inner_full_ex3d)
        }

        a = activity as MainActivity
        v = myView

        tvFeedUnit = v.findViewById(R.id.tvFeedUnit)
        etFeedLength = v.findViewById(R.id.etFeedLength)
        etFeedValue = v.findViewById(R.id.etFeedValue)
        etTaperValue = v.findViewById(R.id.etTaperValue)
        swLoop = v.findViewById(R.id.swLoop)
        etOuterCutTargetDiameter = v.findViewById(R.id.etOuterCutTargetDiameter)

        a.b.tvFeedValue.text = etFeedValue.text

        swLoop.setOnClickListener {
            onLoopClicked(it)
        }

        tvFeedUnit.setOnLongClickListener {
            changeFeedUnit()
            true
        }
        if (feedUnitG95) {
            tvFeedUnit.setText(R.string.feed_unit_metric_G95)
        } else {
            tvFeedUnit.setText(R.string.feed_unit_metric_G94)
        }

        etFeedValue.setOnEditorActionListener (
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed) {
                        val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                        outerCutTargetDiameterMod = true
//                        onOuterCutSourceDiameterClick(v)

                        val getX =
                            String.format(locale = Locale.ROOT,"%.2f", v.text.toString().toFloat())
                        etFeedValue.setText(getX)
                        if(a.mfUpdIf == this) //check fragment is active(longpress) and update feed value on main activity
                            a.tvFeedValue.text = getX
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                        return@OnEditorActionListener true // consume.
                    }
                }
                false // pass on to other listeners.
            }
        )
        return myView
    }


    private fun changeFeedUnit() {
        if (feedUnitG95) {
            feedUnitG95 = false
            tvFeedUnit.setText(R.string.feed_unit_metric_G94)
            a.putLog("set mm\\min: G94")
            a.sendCommand("G94")
        } else {
            feedUnitG95 = true
            tvFeedUnit.setText(R.string.feed_unit_metric_G95)
            //            tvFeedValue.setText("0.1");
            a.putLog("set mm\\rev: G95")
            a.sendCommand("G95")
        }
    }


    override fun update() {
//        a.feedDirection = Direction.Right
//        a.fab.setIconResource(R.drawable.ic_baseline_arrow_forward_24)
    }

    override fun fabText(): String {
        return "Feed"

    }

    override fun buildCmd(): String {
        val minus =
            if (a.feedDirection == Direction.Right || a.feedDirection == Direction.Backward) "" else "-"
        val gCmd = "G01"
        var gTaper = ""
        val axis = if (a.feedDirection == Direction.Right || a.feedDirection == Direction.Left) " Z" else " X"
        val tvTaperValue = etTaperValue.text.toString().toFloat()

        val gSub: String = "F" + etFeedValue.text
        val gLength: String = etFeedLength.text.toString()
        if(tvTaperValue > 0){
            var targetTaperFloat = tvTaperValue*gLength.toFloat()///2 // controller use diameter mode for X axis so no need to divide by 2 here
            if(axis == " Z"){
                if(a.internalCut){ // for internal cut inverse positive value of taper
                    targetTaperFloat = -targetTaperFloat
                }
                val taperStr =  String.format(locale = Locale.ROOT,"%.3f", targetTaperFloat)
                gTaper = " X${taperStr}"
                a.putLog("taper:$taperStr")
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


    private var loopMode = false
    private var ping = false

    fun onLoopClicked(v: View) {
        val checked = (v as Switch).isChecked
        if (checked) {
            loopMode = true
            ping = false
           a.putLog("loop mode on")
            pingpong()
        } else {
            if (loopMode) {
                loopMode = false
                a.putLog("loop mode off")
            }
        }
    }

    private fun pingpong() {
        if (!loopMode) return
        ping = if (!ping) {
            val cmd = a.buildCmd()
            //            String cmd = "G01 Z" + minus + tvFeedLength.getText() +" F" +  tvFeedValue.getText();
            a.putLog("ping:$cmd")
            a.sendCommand(cmd)
            true
        } else {
            val cmd = "G01 Z0 F" + etFeedValue.text.toString()
            a.putLog("pong:$cmd")
            a.sendCommand(cmd)
            false
        }
    }
}