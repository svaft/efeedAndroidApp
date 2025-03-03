package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.ThreadBinding
import java.util.Locale


class threadFragment : Fragment(), UpdateableFragment  {
    lateinit var v: View
    private val viewModel: MyViewModel by activityViewModels()
//    private lateinit var viewModel: MyViewModel // by activityViewModels()

    private var _binding: ThreadBinding? = null
    private val b get() = _binding!!
    private lateinit var a: MainActivity
    private var mInit: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ThreadBinding.inflate(inflater, container, false)
        v = b.root
        mInit = 1
//        viewModel = activityViewModels()
//        v=inflater.inflate(R.layout.thread, container, false)

        a = activity as MainActivity
        b.spinner3.apply {
            adapter = activity?.let {
                ArrayAdapter.createFromResource(
                    it.applicationContext, R.array.threads_array,
                    android.R.layout.simple_spinner_dropdown_item
                )
            } as SpinnerAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("error")
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val type = parent?.getItemAtPosition(position).toString()
                    println(type)
                }
            }
            setSelection(4) // 1.5mm default pitch
        }
        b.tvScrewPitchUnit.setOnClickListener {
            onClickUnit(it)
        }
        return v
    }


    override fun update() {
        if(::a.isInitialized){
            a.b.btMain.isEnabled = true
            a.b.btMain.setText(R.string.thread)
        }
        viewModel.threadMode.value = true
    }

    override fun programMainLP() {
        a.putLog("reset record")
        a.b.viewPager.setPagingEnabled(true)
        a.enableTabs()
//        mfUpdIf.update()
        a.cmdRecorded = false
        a.cmdStarted = false
        a.sendCommand("!S")
        a.b.btMain.setBackgroundColor(Color.GREEN)
        a.b.btPause.setBackgroundColor(Color.GREEN)
        a.pauseProgramFlag = false
    }

    override fun programMainClick() {

//
//        if (a.cmdRecorded) {
//            if (a.cmdStarted) {
//                a.putLog("stop current move")
//                a.sendCommand("!S") // stop current move
//            } else {
//                a.cmdStarted = true
//                a.putLog("repeat or zero")
//                a.sendCommand("!3") //repeat last command or quick move to initial position
//                a.b.btMain.setText(R.string.stop)
//            }
//        } else { // command not recorded yet
//            if (a.cmdStarted) { // command started but not recorded for future replay
//                a.cmdStarted = false
//                a.cmdRecorded = true
//                a.b.btMain.setBackgroundColor(Color.RED)
//                a.putLog("stop&save last move")
//                a.sendCommand("!2")
//                a.pauseProgramFlag = true
//                a.b.btPause.setBackgroundColor(Color.RED)
//
////                        if (viewModel.threadMode.value == true)
////                            b.btMain.setText(R.string.thread)
////                        else
////                            b.btMain.setText(R.string.feed)
//            }
//            else { // build command with active TAB config and send it to lathe
//                a.mfUpdIf.update()
//                a.b.viewPager.setPagingEnabled(false)
//                a.disableUnselectedTabs()
//
//                val cmd = buildCmd()
//                a.sendCommand(cmd)
//                a.cmdStarted = true
//                a.putLog(cmd)
//                a.b.btMain.setText(R.string.sns)
//            }
//        }
    }






    override fun buildCmd(): String {
        val minus =
            if (viewModel.feedDirection.value == Direction.Right || viewModel.feedDirection.value == Direction.Backward) "" else "-"
        val gCmd = "G33"
        var gTaper = ""
        val axis = if (viewModel.feedDirection.value == Direction.Right || viewModel.feedDirection.value == Direction.Left) " Z" else " X"

        val gLength = b.editTextThreadLength.text.toString()
        val tvTaperValue =  b.tvTaperValue.text.toString().toFloat()

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

        var gSub = "K" + b.tvScrewPitch.text
        val tvMultiThread = b.editTextMultiThreadCount.text.toString()
        if (tvMultiThread.toInt() > 1) {
            gSub += " M$tvMultiThread"
        }
        val out = "$gCmd$axis$minus$gLength$gTaper $gSub"
        return out
    }

    override fun processProgram() {

    }

    private fun onClickUnit(v: View) {
        (v as TextView).text = if(viewModel.mMetricUnit.value == true) getString(R.string.inch) else getString(R.string.metric)
        viewModel.mMetricUnit.value = !(viewModel.mMetricUnit.value)!!
    }

    override fun fabText(): String {
        return "Thread"
    }
}