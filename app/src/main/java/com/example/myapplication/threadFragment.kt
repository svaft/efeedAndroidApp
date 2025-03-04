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


class threadFragment : odFragment(0), UpdateableFragment  {
    lateinit var v: View
    private val viewModel: MyViewModel by activityViewModels()
//    private lateinit var viewModel: MyViewModel // by activityViewModels()
    override var mMainText =  "THREAD" //getString(R.string.thread)

    private var _binding: ThreadBinding? = null
    private val b get() = _binding!!
    private var mInit: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ThreadBinding.inflate(inflater, container, false)
        v = b.root
        mInit = 1

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
        b.extInt.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.internalCut.value = 1
                mOd = 1// internal
            } else {
                viewModel.internalCut.value = 0
                mOd = 0 // external
            }
        }
        return v
    }


    override fun update() {
        super.update()
        viewModel.threadMode.value = true
    }



    override fun buildCmd(): String {
        if(viewModel.internalCut.value == 0){
            a.sendCommand("!O")
        } else {
            a.sendCommand("!I")
        }
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

    private fun onClickUnit(v: View) {
        (v as TextView).text = if(viewModel.mMetricUnit.value == true) getString(R.string.inch) else getString(R.string.metric)
        viewModel.mMetricUnit.value = !(viewModel.mMetricUnit.value)!!
    }

    override fun fabText(): String {
        return mMainText
    }
}