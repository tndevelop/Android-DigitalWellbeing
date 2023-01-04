package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.db.data.CustomUsageStats
import com.example.myapplication.managers.UsageStatManager
import com.example.myapplication.utils.Functions
import com.example.myapplication.utils.adapters.AppUsageAdapter
import com.example.myapplication.utils.charts.MyPieChart
import com.example.myapplication.viewModels.HomeViewModel
import com.github.mikephil.charting.charts.PieChart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PhoneMonitoringFragment: Fragment() {

    private val functions = Functions()
    private lateinit var viewModel: HomeViewModel
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private var adapter: AppUsageAdapter? = null
    private lateinit var mPieChart: PieChart
    private lateinit var pieChart: MyPieChart

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_monitoring_phone, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        (root.findViewById(R.id.app_usage_list) as RecyclerView)
                .apply {
                    addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
                }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        mPieChart = view.findViewById(R.id.chart_screen_time)
        progressBar = view.findViewById(R.id.phoneM_progress_bar)
        val listWrapper = view.findViewById<ConstraintLayout>(R.id.app_usage_list_wrapper)
        val recyclerView = view.findViewById<RecyclerView>(R.id.app_usage_list)

        viewModel.startOfDayStatList.observe(viewLifecycleOwner) { list ->
            var totalTime: Long = 0
            list.forEach { totalTime += it.timeInForeground }
            if(list.isNotEmpty()) {
                progressBar.visibility = View.INVISIBLE
                mPieChart.visibility = View.VISIBLE
                listWrapper.visibility = View.VISIBLE
            }

            pieChart = MyPieChart(mPieChart, requireContext())
            pieChart.initChart()
            pieChart.drawScreenTimeChart(list)
            mPieChart.centerText = getString(R.string.label_screen_time) + "\n" + functions.convertMsecToHHMM(totalTime)

            adapter = setupAdapter(recyclerView, list)
            adapter!!.onItemClick = { stat ->
                viewModel.setAppStats(stat)
                navController.navigate(R.id.action_nav_phone_monitoring_to_nav_app_monitoring)
            }
        }

    }

    /*

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.monitoring_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_refresh) {
            val startIntent = Intent(requireActivity().applicationContext, NotificationService::class.java)
            startIntent.putExtra("MANUALLY_STARTED", true)
            requireActivity().applicationContext.startService(startIntent)
        }
        return super.onOptionsItemSelected(item)
    }

    */

    private fun setupAdapter(recyclerView: RecyclerView?, statList: List<CustomUsageStats>): AppUsageAdapter {
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        val adapter = AppUsageAdapter(statList)
        recyclerView.adapter = adapter
        return adapter
    }
}