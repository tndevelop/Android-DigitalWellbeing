package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.myapplication.R
import com.example.myapplication.db.data.CustomUsageStats
import com.example.myapplication.utils.Functions
import com.example.myapplication.utils.charts.MyBarChart
import com.example.myapplication.viewModels.HomeViewModel
import com.github.mikephil.charting.charts.BarChart

class AppMonitoringFragment: Fragment() {

    private val functions = Functions()
    private lateinit var viewModel: HomeViewModel
    private lateinit var navController: NavController
    private lateinit var stat: CustomUsageStats
    private lateinit var mBarChart: BarChart
    private lateinit var barChart: MyBarChart
    private lateinit var excludedWrapper: ConstraintLayout

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_monitoring_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val appIcon = view.findViewById<ImageView>(R.id.app_icon)
        val appName = view.findViewById<TextView>(R.id.text_app_name)
        val appTime = view.findViewById<TextView>(R.id.text_app_screen_time)
        val appOpenings = view.findViewById<TextView>(R.id.app_openings)
        mBarChart = view.findViewById(R.id.chart_app)

        val confrontationWrapper = view.findViewById<ConstraintLayout>(R.id.confrontation_wrapper)

        excludedWrapper = view.findViewById(R.id.excluded_wrapper)
        val excludedText = view.findViewById<TextView>(R.id.excluded_text)
        val excludedButton = view.findViewById<Button>(R.id.excluded_button)

        stat = viewModel.getAppStats()
        appIcon.setImageDrawable(stat.appIcon)
        appName.text = stat.appName
        appTime.text = getString(R.string.screen_time, functions.convertMsecToHHMM(stat.timeInForeground))
        appOpenings.text = getString(R.string.n_openings, stat.nOpenings)

        barChart = MyBarChart(mBarChart, requireContext())
        barChart.initChart()
        barChart.drawScreenTimeChart(stat.eventList)

        confrontationWrapper.visibility = View.GONE
        viewModel.getExcludedApp(stat.appPackage).observe(viewLifecycleOwner) {
            if (it != null) {
                excludedText.text = getString(R.string.excluded_app_text, stat.appName)
                excludedButton.setOnClickListener {
                    viewModel.removeExcludedApp(stat.appName, stat.appPackage, { operationSuccess() }, { operationFailure() })
                }
                excludedWrapper.visibility = View.VISIBLE
            }

        }
    }

    fun operationSuccess() {
        Toast.makeText(requireContext(), "Operation done", Toast.LENGTH_LONG).show()
        excludedWrapper.visibility = View.GONE
    }

    fun operationFailure() {
        Toast.makeText(requireContext(), "Database error", Toast.LENGTH_LONG).show()
    }
}