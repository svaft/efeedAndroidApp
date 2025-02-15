package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class idFragment : Fragment(), UpdateableFragment {
    private var feedUnitG95 = true
    private lateinit var tvFeedUnit: TextView
    //inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        inflater.inflate(R.layout.id_turn, container, false)!!

    lateinit var v: View
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view
        tvFeedUnit =v.findViewById<TextView>(R.id.tvFeedUnit)
        tvFeedUnit.setOnLongClickListener {
      // todo make one parent with base functions      changeFeedUnit()
            true
        }
        if (feedUnitG95) {
            tvFeedUnit.setText(R.string.feed_unit_metric_G95)
        } else {
            tvFeedUnit.setText(R.string.feed_unit_metric_G94)
        }

    }



    override fun update(a: MainActivity) {

    }

    override fun fabText(): String {
        return "Feed"

        TODO("Not yet implemented")
    }

    override fun buildCmd(a: MainActivity): String {
        TODO("Not yet implemented")
    }
}
