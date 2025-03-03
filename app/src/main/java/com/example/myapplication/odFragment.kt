package com.example.myapplication

import android.graphics.Color
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
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.OdTurnBinding
import com.google.android.material.button.MaterialButton
import java.util.Locale





enum class fsm { Reset, Initial, StartedNotRecorded, RecordedPaused, RecordedPlayed }

class odFragment(i: Int) : Fragment(), UpdateableFragment {
    private var mOd = i
    private var str = "false"

    private lateinit var tvFeedUnit: TextView
    private lateinit var etFeedLength: EditText
    private lateinit var etFeedValue: EditText
    private lateinit var etTaperValue: EditText
    private lateinit var swLoop: Switch
    private lateinit var etOuterCutTargetDiameter: EditText

    var mFeed94: String = "600.00"
    var mFeed95: String = "0.1"

    private var outerCutTargetDiameterMod: Boolean = false

    private val viewModel: MyViewModel by activityViewModels()
    private var _binding: OdTurnBinding? = null
    private val b get() = _binding!!

    private lateinit var a: MainActivity
    private lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OdTurnBinding.inflate(inflater, container, false)
        v = b.root
//        val myView: View = inflater.inflate(R.layout.od_turn, container, false)!!

        if(mOd == 0){
            v.findViewById<ImageView>(R.id.imageInnerProgram).setImageResource(R.drawable.left_outer_full_ex3d)
//            viewModel.internalCut.value = 0
        } else {
            v.findViewById<ImageView>(R.id.imageInnerProgram).setImageResource(R.drawable.left_inner_full_ex3d)
//            viewModel.internalCut.value = 1
        }

        a = activity as MainActivity

        viewModel.mDOC.value = b.etDOC.text.toString()
        tvFeedUnit = v.findViewById(R.id.tvFeedUnit)
        etFeedLength = v.findViewById(R.id.etFeedLength)
        etFeedValue = v.findViewById(R.id.etFeedValue)
        etTaperValue = v.findViewById(R.id.etTaperValue)
        swLoop = v.findViewById(R.id.swLoop)
        etOuterCutTargetDiameter = v.findViewById(R.id.etOuterCutTargetDiameter)
        etOuterCutTargetDiameter.setOnClickListener{

        }

        if (viewModel.mG94G95.value == 1) {
            tvFeedUnit.setText(R.string.feed_unit_metric_G95)
            etFeedValue.setText(mFeed95)
        } else {
            tvFeedUnit.setText(R.string.feed_unit_metric_G94)
            etFeedValue.setText(mFeed94)
        }

//        a.b.tvFeedValue.text = etFeedValue.text

        val numbers = listOf(R.id.tvF01, R.id.tvF02, R.id.tvF03, R.id.tvF04, R.id.tvF05, R.id.tvF06, R.id.tvF07, R.id.tvF08, R.id.tvF09, R.id.tvF10, R.id.tvF11, R.id.tvF12)
        for (item in numbers)  v.findViewById<TextView>(item).setOnClickListener{ feedHelper(it) }

        swLoop.setOnClickListener {
            onLoopClicked(it)
        }

        tvFeedUnit.setOnLongClickListener {
            if (viewModel.mG94G95.value == 1) {
//                tvFeedUnit.setText(R.string.feed_unit_metric_G94)
                a.putLog("try to set mm\\min: G94")
                a.sendCommand("G94")
 //               viewModel.mG94G95.value = 1
            } else {
                tvFeedUnit.setText(R.string.feed_unit_metric_G95)
                a.putLog("try to set mm\\rev: G95")
                a.sendCommand("G95")
//                viewModel.mG94G95.value = 0
            }
//            changeFeedUnit()
            true
        }

        etFeedValue.setOnEditorActionListener (
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed) {
                        val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        val getX = String.format(locale = Locale.ROOT,"%.2f", v.text.toString().toFloat())
                        v.text = getX
                        if(a.mfUpdIf == this) { //check fragment is active(longpress) and update feed value on main activity
                            viewModel.currentFeed.value = getX
                        }
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                        return@OnEditorActionListener true // consume.
                    }
                }
                false // pass on to other listeners.
            }
        )

        viewModel.mG94G95.observe(viewLifecycleOwner) { value ->
            if(value == 0){
//                mFeed95 =etFeedValue.text.toString()
                etFeedValue.setText(mFeed94)
                tvFeedUnit.setText(R.string.feed_unit_metric_G94)
//                b.tvFeedUnit.text = "OD:"
            } else{
//                mFeed94 =etFeedValue.text.toString()
                etFeedValue.setText(mFeed95)
                tvFeedUnit.setText(R.string.feed_unit_metric_G95)
//                b.tvFeedUnit.text = "ID:"
            }
        }


        return v
    }

    private fun feedHelper(it: View?) {
        etFeedValue.setText((it as TextView).text)
    }



    override fun update() {
        a.b.btMain.isEnabled = true
        a.b.btMain.setText(R.string.feed)
        viewModel.threadMode.value = false

        if(mOd == 0 ) {
            if(viewModel.internalCut.value !=0)
                a.sendCommand("!O")
        } else {
            if(viewModel.internalCut.value !=1)
            a.sendCommand("!I")
        }
    }

    private var fsmState: fsm = fsm.Initial

    override fun programMainLP() {

        a.putLog("reset record")
        fsmState = fsm.Initial
        updateUIbyFsmState(a)
        a.sendCommand("!S")
    }

    fun updateUIbyFsmState(ma: MainActivity){
        when(fsmState) {
            fsm.Reset -> {
                ma.b.viewPager.setPagingEnabled(true)
                ma.enableTabs()
                ma.b.btMain.setBackgroundColor(Color.GREEN)
                ma.b.btPause.setBackgroundColor(Color.GREEN)
                ma.b.btMain.setText(R.string.feed)
                a.updateMainIcon()
            }
            fsm.Initial ->{
                ma.b.viewPager.setPagingEnabled(true)
                ma.enableTabs()
                ma.b.btMain.setBackgroundColor(Color.GREEN)
                ma.b.btPause.setBackgroundColor(Color.GREEN)
                ma.b.btMain.setText(R.string.feed)
                a.updateMainIcon()
            }
            fsm.StartedNotRecorded ->{
                ma.b.viewPager.setPagingEnabled(false)
                ma.disableUnselectedTabs()
                ma.b.btMain.setBackgroundColor(Color.YELLOW)
                ma.b.btMain.setText(R.string.sns)
                a.updateMainIcon()
            }
            fsm.RecordedPaused ->{
                ma.b.btMain.setBackgroundColor(Color.RED)
                ma.b.btPause.setBackgroundColor(Color.RED)
                ma.b.btMain.setText(R.string.feed)
            }
            fsm.RecordedPlayed -> {
                ma.b.btMain.setText(R.string.feed)
            }
        }
    }

    override fun processEvent(e: String) {
        when(e){
            "eom" ->{
                a.putLog("get EOM")
                if(fsmState == fsm.StartedNotRecorded){
                    a.putLog("stop&save last move")
                    a.sendCommand("!2")
                    fsmState = fsm.RecordedPaused
                    updateUIbyFsmState(a)
                } else
                    if(fsmState == fsm.RecordedPlayed){
                        fsmState = fsm.RecordedPaused
                        updateUIbyFsmState(a)
                    }
            }
            "reset" ->{ // reset
                a.putLog("reset record")
                fsmState = fsm.Initial
                updateUIbyFsmState(a)
            }

        }
    }
    override fun programMainClick() {
//        super.programMainClick(ma)
        // change state and update UI
        when(fsmState){
            fsm.Reset ->{
                a.putLog("reset record")
                a.sendCommand("!S")
                fsmState = fsm.Initial
            }
            fsm.Initial ->{
//                ma.mfUpdIf.update()
                val cmd = buildCmd()
                a.sendCommand(cmd)
                a.putLog(cmd)
                fsmState = fsm.StartedNotRecorded
            }
            fsm.StartedNotRecorded -> {
                a.putLog("stop&save last move")
                a.sendCommand("!2")
                fsmState = fsm.RecordedPaused
            }
            fsm.RecordedPaused -> {
                a.putLog("repeat or zero")
                a.sendCommand("!3") //repeat last command or quick move to initial position
                a.b.btMain.setText(R.string.stop)
                fsmState =fsm.RecordedPlayed
            }
            fsm.RecordedPlayed -> {
                a.putLog("stop current move")
                a.sendCommand("!S") // stop current move
            }
        }
        updateUIbyFsmState(a)
    }


    override fun fabText(): String {
        return "Feed"
    }

    override fun buildCmd(): String {
        if(viewModel.internalCut.value == 0){
            a.sendCommand("!O")
        } else {
            a.sendCommand("!I")
        }
        val minus =
            if (viewModel.feedDirection.value == Direction.Right || viewModel.feedDirection.value == Direction.Backward) "" else "-"
        val gCmd = "G01"
        var gTaper = ""
        val axis = if (viewModel.feedDirection.value == Direction.Right || viewModel.feedDirection.value == Direction.Left) " Z" else " X"
        val tvTaperValue = etTaperValue.text.toString().toFloat()

        val gSub: String = "F" + etFeedValue.text
        val gLength: String = etFeedLength.text.toString()
        if(tvTaperValue > 0){
            var targetTaperFloat = tvTaperValue*gLength.toFloat()///2 // controller use diameter mode for X axis so no need to divide by 2 here
            if(axis == " Z"){
                if(viewModel.internalCut.value == 1){ // for internal cut inverse positive value of taper
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

    override fun processProgram() {

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putBoolean("IS_EDITING_KEY", cmdRecorded)
        outState.putString("RANDOM_GOOD_DEED_KEY", str)
    }


    private var loopMode = false
    private var ping = false

    private fun onLoopClicked(v: View) {
        if ((v as Switch).isChecked) {
            loopMode = true
            ping = false
           a.putLog("loop mode on")
            pingpong()
        } else {
            loopMode = false
            a.putLog("loop mode off")
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
    fun onOuterCutTargetDiameter(v: View) {
        outerCutTargetDiameterMod = true
        (v as EditText).selectAll()
    }


//    private fun changeFeedUnit() {
//        if (viewModel.mG94G95.value == 0) {
//            tvFeedUnit.setText(R.string.feed_unit_metric_G94)
//            a.putLog("set mm\\min: G94")
//            a.sendCommand("G94")
//            viewModel.mG94G95.value = 1
//        } else {
//            tvFeedUnit.setText(R.string.feed_unit_metric_G95)
//            a.putLog("set mm\\rev: G95")
//            a.sendCommand("G95")
//            viewModel.mG94G95.value = 0
//        }
//    }
}
