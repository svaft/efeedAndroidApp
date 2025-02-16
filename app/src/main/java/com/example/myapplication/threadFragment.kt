package com.example.myapplication


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity.Direction
import java.util.Locale


class threadFragment : Fragment(), UpdateableFragment  {
    val types = arrayOf("simple User", "Admin")
    //inflate the layout
    lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v=inflater.inflate(R.layout.thread, container, false)
        a = activity as MainActivity
        val spinner = v.findViewById<Spinner>(R.id.spinner3)
        spinner?.adapter = activity?.let {
                        ArrayAdapter.createFromResource(
                            it.applicationContext, R.array.threads_array,
                android.R.layout.simple_spinner_dropdown_item
            )
        } as SpinnerAdapter
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("erreur")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val type = parent?.getItemAtPosition(position).toString()
//                Toast.makeText(activity,type, Toast.LENGTH_LONG).show()
                println(type)
            }

        }
        spinner.setSelection(4)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    private lateinit var a: MainActivity
    override fun update() {
        a.threadMode = true
        a.fab.text = "Резьба"
    }

    override fun buildCmd(): String {
        val minus =
            if (a.feedDirection == Direction.Right || a.feedDirection == Direction.Backward) "" else "-"
        val gCmd = "G33"
        var gTaper = ""
        val axis = if (a.feedDirection == Direction.Right || a.feedDirection == Direction.Left) " Z" else " X"

        val gLength = v.findViewById<TextView>(R.id.editTextThreadLength).text.toString()
        val tvTaperValue =  v.findViewById<TextView>(R.id.tvTaperValue).text.toString().toFloat()

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

        var gSub = "K" + v.findViewById<TextView>(R.id.tvScrewPitch).text
        val tvMultiThread = v.findViewById<TextView>(R.id.editTextMultiThreadCount).text.toString()
        if (tvMultiThread.toInt() > 1) {
            gSub += " M$tvMultiThread"
        }
        val out = "$gCmd$axis$minus$gLength$gTaper $gSub"
        return out
    }

    override fun fabText(): String {
        return "Thread"
    }
}