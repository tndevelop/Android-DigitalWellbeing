package com.example.myapplication.fragments

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.myapplication.R
import com.example.myapplication.db.data.FWAppUsage
import com.example.myapplication.db.data.Path
import com.example.myapplication.db.data.SuggestionOnApp
import com.example.myapplication.utils.Functions
import com.example.myapplication.viewModels.HomeViewModel


class CreatePathFragment : Fragment(R.layout.fragment_create_path) {

    lateinit var appName: TextView
    lateinit var appIcon: ImageView
    lateinit var hourPicker : NumberPicker
    lateinit var vibrationFlag : SwitchCompat
    lateinit var brightnessFlag : SwitchCompat
    lateinit var nextButton : Button
    lateinit var progressBar : ProgressBar
    lateinit var selectedAppTv : TextView
    private lateinit var navController: NavController
    private lateinit var viewModel: HomeViewModel
    private lateinit var includeCheckBox: CheckBox
    private lateinit var path: Path
    private var existingInt2 = false
    private lateinit var usage: FWAppUsage
    private lateinit var pathList: List<Path>
    private var functions = Functions()

    val INTERVENTION_FIRST_WEEK = 0
    val INTERVENTION_1 = 1
    val INTERVENTION_2 = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){

        appName = view.findViewById(R.id.app_name)
        appIcon = view.findViewById(R.id.app_icon)
        hourPicker = view.findViewById(R.id.hour_picker)
        vibrationFlag = view.findViewById(R.id.vibration_toggle)
        brightnessFlag = view.findViewById(R.id.brightness_toggle)
        nextButton = view.findViewById(R.id.next_or_finish_button)
        progressBar = view.findViewById(R.id.progressBar2)
        selectedAppTv = view.findViewById(R.id.selected_app_tv)
        includeCheckBox = view.findViewById(R.id.check)
        val res: Resources = resources
        navController = Navigation.findNavController(view)
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)


        val selectedApps = arguments?.get("apps") as MutableList<SuggestionOnApp>
        val actualAppIdx = arguments?.getInt("actual")
        val includedApps = arguments?.get("includedApps") as MutableList<Boolean>

        val app =  selectedApps.get(actualAppIdx!!)

        progressBar.min = 0
        progressBar.max = selectedApps.size
        progressBar.progress = actualAppIdx + 1
        val newText = getString(R.string.selected_apps) + " ( ${actualAppIdx + 1} out of ${selectedApps.size} )"
        selectedAppTv.text = newText


        appName.text = app.appName
        val appInfo = requireContext().packageManager?.getApplicationInfo(app.appPackage, 0)
        appIcon.setImageDrawable(appInfo!!.loadIcon(requireContext().packageManager))

        if(selectedApps.size > 1 || requireArguments().get("comesFrom") == "path_edit") {
            includeCheckBox.visibility = View.VISIBLE
            updateChecked(includeCheckBox, includedApps, actualAppIdx)
            includeCheckBox.setOnClickListener {
                includedApps[actualAppIdx] = !includedApps[actualAppIdx]
                updateChecked(includeCheckBox, includedApps, actualAppIdx)

            }
        }else{
            includeCheckBox.visibility = View.INVISIBLE
        }
        hourPicker.minValue = res.getInteger(R.integer.min_value_picker)
        hourPicker.maxValue = res.getInteger(R.integer.max_value_picker)
        hourPicker.value = app.suggestedTime.toInt()
        hourPicker.wrapSelectorWheel = true
        hourPicker.setOnValueChangedListener { _: NumberPicker, _: Int, newVal: Int ->
            app.suggestedTime = newVal.toLong()
        }

        if(vibrationFlag.isChecked != app.vibrationSuggested) {
            vibrationFlag.toggle()
        }
        vibrationFlag.setOnClickListener{
            app.vibrationSuggested = vibrationFlag.isChecked
        }

        if(brightnessFlag.isChecked() != app.greyOutSuggested) {
            brightnessFlag.toggle()
        }
        brightnessFlag.setOnClickListener{
            app.greyOutSuggested = brightnessFlag.isChecked
        }

        if(actualAppIdx == selectedApps.size - 1){
            //last app
            nextButton.text = "FINISH"

            //path is already created and we need to change it
            if(requireArguments().get("comesFrom") == "path_edit"){
                nextButton.setOnClickListener {

                        viewModel.getPath().observe(viewLifecycleOwner) { path ->
                            if (!includedApps[0]) {
                                viewModel.removePath(path, {}, {})
                            }
                            else{
                                path.int_duration = selectedApps[0].suggestedTime.toInt()
                                path.hasVibration = selectedApps[0].vibrationSuggested
                                path.hasDisplayModification = selectedApps[0].greyOutSuggested
                                viewModel.updatePath(path, {}, {})
                            }
                        }

                    navController.navigate(R.id.action_createPathFragment_to_nav_home_paths)
                }
            }else {
                nextButton.setOnClickListener {
                    var originating: String
                    if (requireArguments().get("comesFrom") in listOf("eow", "modify_complete")) {
                        originating = "modify_complete"
                    } else {
                        originating = "save"
                    }
                    val bundle =
                        bundleOf("apps" to selectedApps, "originating" to originating, "includedApps" to includedApps)
                    navController.navigate(R.id.action_createPathFragment_to_nav_chat, bundle)
                }
            }
        }else{
            nextButton.setOnClickListener{
                val bundle = bundleOf("apps" to selectedApps, "actual" to actualAppIdx + 1, "comesFrom" to requireArguments().get("comesFrom"), "includedApps" to includedApps)
                navController.navigate(R.id.action_createPathFragment_self, bundle)
            }
        }

    }

    private fun updateChecked(includeCheckBox: CheckBox, includedApps: MutableList<Boolean>, actualAppIdx: Int) {
        includeCheckBox.isChecked = includedApps[actualAppIdx]
        if(includedApps[actualAppIdx])
            includeCheckBox.text = getString(R.string.included_app)
        else
            includeCheckBox.text = getString(R.string.excluded_app)
    }


    /*private fun openP2Dialog(both: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.path_exist_title)
            .setMessage(if(both) R.string.path2_exist_message_both else R.string.path2_exist_message)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                if(both)
                    viewModel.upgradePathFWto1(path, usage, { operationSuccess() }, { operationFailure() })
            }
            .show()
    }*/


}