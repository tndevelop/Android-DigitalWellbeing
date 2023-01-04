package com.example.myapplication.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.db.data.App
import com.example.myapplication.db.data.AppInfo
import com.example.myapplication.utils.Functions
import com.example.myapplication.utils.adapters.AppInfoAdapter
import com.example.myapplication.viewModels.HomeViewModel
import com.google.android.material.textfield.TextInputLayout

class AppSelectionFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var navController : NavController
    private var infoAdapter: AppInfoAdapter? = null
    private var appList: ArrayList<AppInfo> = ArrayList()
    private var adapterList: ArrayList<AppInfo> = ArrayList()
    private val functions = Functions()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_app_selection, container, false)
        (root.findViewById(R.id.app_list) as RecyclerView)
                .apply {
                    addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
                }.also {
                    infoAdapter = setupAdapter(it, adapterList)
                }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val progressBar = view.findViewById<ProgressBar>(R.id.progress_circular)

        viewModel.initInstalledApps(requireContext(), getString(R.string.app_name))
        viewModel.getInstalledApp().observe( viewLifecycleOwner) { list ->
            appList.clear()
            appList.addAll(list)
            adapterList.clear()
            adapterList.addAll(list)
            infoAdapter!!.notifyDataSetChanged()
            if (list.isNotEmpty())
                progressBar.visibility = View.INVISIBLE
        }

        infoAdapter!!.onItemClick = { app ->
            openDialog(app)
        }

        val searchText = view.findViewById<TextInputLayout>(R.id.search)
        searchText.editText?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.v("AAAAAA", s.toString())
                if(s == "" || s == null) {
                    adapterList = appList
                    infoAdapter!!.notifyDataSetChanged()
                }
                else {
                    adapterList.clear()
                    appList.forEach { element ->
                        if(element.appName.startsWith(s, true))
                            adapterList.add(element)
                    }
                    appList.forEach { element ->
                        if(element.appName.contains(s, true) && !adapterList.contains(element))
                            adapterList.add(element)
                    }
                    infoAdapter!!.notifyDataSetChanged()
                }
            }
        })
        searchText.editText?.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus)
                functions.hideKeyBoard(requireContext(), view)
        }

    }

    private fun setupAdapter(v: RecyclerView, list: ArrayList<AppInfo>): AppInfoAdapter {
        v.layoutManager = LinearLayoutManager(activity)
        val adapter = AppInfoAdapter(list)
        v.adapter = adapter
        return adapter
    }

    private fun openDialog(selectedApp: AppInfo) {
        var oldPath = false
        viewModel.getPathList().observe(viewLifecycleOwner) { pathList ->
            val list = pathList.map { it.appList }
            for ( appList in list ){
                for( app in appList ){
                    if (app.appPackage == selectedApp.appPackage)
                        oldPath = true
                }
            }
            if(oldPath){
                AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.path_creation_title))
                        .setMessage(getString(R.string.path_already_created_message, selectedApp.appName))
                        .setPositiveButton(getString(R.string.button_continue)) { dialog, _ ->
                            val bundle = bundleOf("originating" to "manual", "appName" to selectedApp.appName, "appPackage" to selectedApp.appPackage, "appName" to selectedApp.appName)
                            navController.navigate(R.id.action_nav_app_selection_to_nav_chat, bundle)
                        }
                        .setNegativeButton(getString(R.string.button_undo)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
            }
            else {
                val bundle = bundleOf("originating" to "manual", "appName" to selectedApp.appName, "appPackage" to selectedApp.appPackage,  "appName" to selectedApp.appName)
                navController.navigate(R.id.action_nav_app_selection_to_nav_chat, bundle)
            }
        }
    }



}