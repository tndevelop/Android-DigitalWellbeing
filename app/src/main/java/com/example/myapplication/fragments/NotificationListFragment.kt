package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.db.data.Notification
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.adapters.NotificationAdapter
import com.example.myapplication.viewModels.HomeViewModel

class NotificationListFragment: Fragment()  {

    private lateinit var viewModel: HomeViewModel
    private lateinit var navController: NavController
    private var adapter: NotificationAdapter? = null
    private var notificationList: ArrayList<Notification> = ArrayList()

    private lateinit var emptyMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_notification_list, container, false)
        (root.findViewById(R.id.itemList) as RecyclerView)
                .apply {
                    addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
                }
                .also {
                    adapter = setupAdapter(it, notificationList)
                }
        emptyMsg = root.findViewById(R.id.notifications_empty_list)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        viewModel.getUNotificationList().observe( viewLifecycleOwner) { UList ->

                notificationList.removeAll(notificationList)
                UList.forEach {
                    val uNotification =
                        Notification(Constants.NOTIFICATION_USAGE, it, null, it.date)
                    notificationList.add(uNotification)
                }
                notificationList.sortBy { it.date }
                adapter!!.notifyDataSetChanged()
                if (notificationList.isNotEmpty())
                    hideMessage()
                else
                    displayMessage()


        }

        adapter!!.onItemClick = { notification ->
            when (notification.type) {
                Constants.NOTIFICATION_USAGE -> {
                    viewModel.initUNotification(notification.usage!!)
                    viewModel.updateNotificationOpened(notification.usage!!, {}, {})
                    val bundle = bundleOf("originating" to "proactivity", "appName" to notification.usage.appName, "appPackage" to notification.usage.appPackage, "appName" to notification.usage.appName
                    , "nAccess" to notification.usage.nAccess, "timeSpent" to notification.usage.timeSpent)
                    navController.navigate(R.id.action_nav_notification_list_to_nav_chat, bundle)
                }
            }
        }
    }

    private fun setupAdapter(v: RecyclerView, list: ArrayList<Notification>): NotificationAdapter {
        v.layoutManager = LinearLayoutManager(activity)
        val adapter = NotificationAdapter(list)
        v.adapter = adapter
        return adapter
    }

    private fun hideMessage() {
        emptyMsg.visibility = View.GONE
    }

    private fun displayMessage() {
        emptyMsg.visibility = View.VISIBLE
    }

}