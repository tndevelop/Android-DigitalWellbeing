package com.example.myapplication.fragments

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.TutorialActivity
import com.example.myapplication.db.data.ActiveIntervention
import com.example.myapplication.db.data.Path
import com.example.myapplication.db.data.User
import com.example.myapplication.services.Intervention2Service
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import com.example.myapplication.utils.SwipeToDeleteCallback
import com.example.myapplication.utils.adapters.PathAdapter
import com.example.myapplication.viewModels.HomeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private val functions = Functions()
    private lateinit var viewModel: HomeViewModel
    private lateinit var fab: FloatingActionButton
    private lateinit var chatFab: FloatingActionButton
    private lateinit var navController: NavController
    private var adapter: PathAdapter? = null
    private var pathList: ArrayList<Path> = ArrayList()
    private var activeIntList: ArrayList<ActiveIntervention> = ArrayList()
    private lateinit var swipeToDelete: ItemTouchHelper


    private lateinit var emptyMsg: TextView
    private lateinit var monitoringPhaseMsg: TextView
    private lateinit var monitoringPhaseTitle: TextView

    val userObserver = Observer<User> { user ->
        if(user!=null) {
            setMessagesAndFab(pathList, functions.isMonitoringPhase(user.startDate), functions.inNDays(user.startDate, Constants.N_DAYS_MONITORING_PHASE))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    override fun onResume(){
        super.onResume()
        if(viewModel.currentUser.value != null) {
            //enable actions when user presses something and than backpresses to HomeFragment
            setMessagesAndFab(pathList, functions.isMonitoringPhase(viewModel.currentUser.value!!.startDate), functions.inNDays(viewModel.currentUser.value!!.startDate, Constants.N_DAYS_MONITORING_PHASE))
        }
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        (root.findViewById(R.id.itemList) as RecyclerView)
                .apply {
                    addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
                }
                .also {
                    adapter = setupAdapter(it, pathList)
                }
        fab = root.findViewById(R.id.fab)
        chatFab = root.findViewById(R.id.chat_button)
        emptyMsg = root.findViewById(R.id.path_empty_list)
        monitoringPhaseMsg = root.findViewById(R.id.monitoring_phase)
        monitoringPhaseTitle = root.findViewById(R.id.monitoring_phase_title)
        viewModel.initStats(requireActivity())

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        viewModel.getPathList().observe( viewLifecycleOwner) { pList ->
            pathList.removeAll(pathList)
            pathList.addAll(pList)
            pathList.sortBy { it.intervention }

            viewModel.getActiveInterventions().observe(viewLifecycleOwner) { aiList ->
                activeIntList.removeAll(activeIntList)
                activeIntList.addAll(aiList)
                adapter!!.notifyDataSetChanged()
                viewModel.currentUser.observe(requireActivity(), userObserver)
            }
        }

        adapter!!.onItemClick = { path ->
            viewModel.initPath(path)
            if(functions.checkRecap(path)) {
                viewModel.initPathResults(path.id)
                navController.navigate(R.id.action_nav_home_paths_to_nav_intervention2_recap)
            } else {
                viewModel.initPathResults(path.id)
                navController.navigate(R.id.action_nav_home_paths_to_nav_intervention2_recap, bundleOf("change_level" to false))
                //navController.navigate(R.id.action_nav_home_paths_to_nav_intervention2)
            }

        }


        swipeToDelete = ItemTouchHelper(SwipeToDeleteCallback(requireContext(), adapter!!, viewModel))
        swipeToDelete.attachToRecyclerView(view.findViewById(R.id.itemList) )

        setHasOptionsMenu(true)
    }

    private fun setupAdapter(v: RecyclerView, list: ArrayList<Path>): PathAdapter {
        v.layoutManager = LinearLayoutManager(activity)
        val adapter = PathAdapter(list)
        v.adapter = adapter
        return adapter
    }

    private fun setMessagesAndFab(pList: List<Path>, isMonitoringPhase: Boolean, endMonitoringDate: Calendar){
        monitoringPhaseMsg.text = getString(R.string.monitoring_phase_message, String.format("%02d", endMonitoringDate.get(Calendar.DAY_OF_MONTH)), String.format("%02d", endMonitoringDate.get(Calendar.MONTH) + 1), String.format("%02d",  endMonitoringDate.get(Calendar.HOUR_OF_DAY)), String.format("%02d", endMonitoringDate.get(Calendar.MINUTE)))
        if(isMonitoringPhase) {
            emptyMsg.visibility = View.GONE
            monitoringPhaseMsg.visibility = View.VISIBLE
            monitoringPhaseTitle.visibility = View.VISIBLE
            fab.setOnClickListener {
                forbidden()
            }
            chatFab.setOnClickListener {
                forbidden()
            }
        }
        else if(pList.isEmpty()){
            emptyMsg.visibility = View.VISIBLE
            monitoringPhaseMsg.visibility = View.GONE
            monitoringPhaseTitle.visibility = View.GONE
        }else{
            emptyMsg.visibility = View.GONE
            monitoringPhaseMsg.visibility = View.INVISIBLE
            monitoringPhaseTitle.visibility = View.INVISIBLE
        }

        if(!isMonitoringPhase){
            fab.setOnClickListener {
                navigateAddPath()
            }
            chatFab.setOnClickListener{
                navigateChat()
            }
        }
    }

    private fun navigateChat() {
        navController.navigate(R.id.action_nav_home_paths_to_nav_chat)
    }

    private fun navigateAddPath() {
        navController.navigate(R.id.action_nav_home_paths_to_nav_add_path)
    }

    private fun forbidden() {
        functions.createPopUp(requireActivity(), getString(R.string.monitoring_phase), getString(R.string.monitoring_phase_message_chat))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_tutorial -> {
                val intent = Intent(requireContext(), TutorialActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_reset_int -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.reset_int)
                    .setMessage(R.string.reset_int_dialog)
                    .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        resetInterventions()
                        dialog.dismiss()
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    private fun resetInterventions() {

        var service2: Intervention2Service?
        val conn2 = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as Intervention2Service.LocalBinder
                service2 = binder.getService()
                service2?.destroyNotification()
            }
            override fun onServiceDisconnected(name: ComponentName) {}
        }

        viewModel.removeAllActiveInterventions()

        val intent2 = Intent(requireActivity().applicationContext, Intervention2Service::class.java)
        requireContext().startService(intent2)
        requireContext().bindService(intent2, conn2, Context.BIND_AUTO_CREATE)
    }
}
