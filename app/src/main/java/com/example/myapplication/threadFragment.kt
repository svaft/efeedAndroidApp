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


class threadFragment : Fragment(), UpdateableFragment  {
    val types = arrayOf("simple User", "Admin")
    //inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val t=inflater.inflate(R.layout.thread, container, false)
        val spinner = t.findViewById<Spinner>(R.id.spinner3)
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
        return t
    }

    lateinit var v: View
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view
//        val spinner: Spinner = view.findViewById(R.id.spinner3)
//
//        if (spinner != null) {
//            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
//            spinner.adapter = adapter
//
//
//        // Настраиваем адаптер
//        val adapter: ArrayAdapter<*> =
//            ArrayAdapter.createFromResource(
//                this, R.array.threads_array,
//                android.R.layout.simple_spinner_item
//            )
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//
//// Вызываем адаптер
//        spinner.adapter = adapter
// Create an ArrayAdapter using the string array and a default spinner layout.
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.planets_array,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            // Specify the layout to use when the list of choices appears.
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            // Apply the adapter to the spinner.
//            spinner.adapter = adapter
//        }
    }

    override fun update(a: MainActivity) {
        a.threadMode = true
        a.fab.text = "Резьба"
    }

    override fun buildCmd(a: MainActivity): String {
        val minus =
            if (a.feedDirection == Direction.Right || a.feedDirection == Direction.Backward) "" else "-"
        var gCmd = "G01"
        val gLength: String
        val gTaper = ""
        val axis = if (a.feedDirection == Direction.Right || a.feedDirection == Direction.Left) " Z" else " X"

        val tvTaperValue =  v.findViewById<TextView>(R.id.tvTaperValue).text.toString().toFloat()

        v.findViewById<TextView>(R.id.tvScrewPitch).text.toString().toFloat()

        gCmd = "G33"
        var gSub: String = "K" + v.findViewById<TextView>(R.id.tvScrewPitch).text
        val tvLen: TextView = v.findViewById(R.id.editTextThreadLength)
        gLength = tvLen.text.toString()
        val tvMultiThread = v.findViewById<TextView>(R.id.editTextMultiThreadCount)
        val cnt = tvMultiThread.text.toString().toInt()
        if (cnt > 1) {
            gSub += " " + "M" + tvMultiThread.text.toString()
        }
        val out = "$gCmd$axis$minus$gLength$gTaper $gSub"
        return out
    }

    override fun fabText(): String {
        return "Резьба"
    }
}