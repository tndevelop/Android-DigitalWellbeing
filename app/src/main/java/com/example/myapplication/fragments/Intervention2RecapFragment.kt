package com.example.myapplication.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.myapplication.R
import com.example.myapplication.db.data.Path
import com.example.myapplication.db.data.SuggestionOnApp
import com.example.myapplication.utils.Constants
import com.example.myapplication.viewModels.HomeViewModel

class Intervention2RecapFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var navController: NavController
    private lateinit var pathN: Path
    private var result = -1
    private var nextLevel = -1
    private var checkIfLevelChanged = true


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_intervention2_recap, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        if(getArguments() != null && requireArguments().get("change_level") != null){
            checkIfLevelChanged = requireArguments().get("change_level") as Boolean
        }

        var nInt: Float
        var nStartPen = 0F
        var nEndPen = 0F
        var nSecPen = 0F
        var nDelayPen = 0F
        val level = view.findViewById<TextView>(R.id.recap1_level)
        val appList = view.findViewById<TextView>(R.id.intervention_app)
        val nIntTxt = view.findViewById<TextView>(R.id.n_interventions)
        //val nStartPenTxt = view.findViewById<TextView>(R.id.start_penalty_measure)
        val nEndPenTxt = view.findViewById<TextView>(R.id.end_penalty_measure)
        val nSecPenTxt = view.findViewById<TextView>(R.id.end_penalty_seconds_measure)
        val nDelayPenTxt = view.findViewById<TextView>(R.id.delay_penalty_measure)
        val recapTxt = view.findViewById<TextView>(R.id.recap_text)
        val resultImg = view.findViewById<ImageView>(R.id.result_image)
        val buttonOk = view.findViewById<Button>(R.id.ok_button)

        viewModel.getPath().observe(viewLifecycleOwner) { path ->
            pathN = path
            appList.text = path.appList[0].appName
            level.text = getString(R.string.level_placeholder, path.level)
            nInt = path.n_interventions.toFloat()
            nIntTxt.text = getString(R.string.n_interventions, nInt.toInt())

            viewModel.getPathResults().observe(viewLifecycleOwner) { list ->
                nSecPen = 0F
                nEndPen = 0F
                nDelayPen = 0F
                for (result in list) {
                    if (result.start_pen)
                        nStartPen++
                    if (result.end_pen) {
                        nEndPen++
                        if (!result.del_pen)
                            nSecPen += result.sec_delay
                    }
                    if (result.del_pen)
                        nDelayPen++
                }
                //nStartPenTxt.text = nStartPen.toInt().toString()
                nEndPenTxt.text = nEndPen.toInt().toString()
                nSecPenTxt.text = nSecPen.toInt().toString()
                nDelayPenTxt.text = nDelayPen.toInt().toString()

                when (checkResults(nInt, nStartPen, nEndPen, nSecPen, nDelayPen)) {
                    Constants.LEVEL_DOWN -> {
                        nextLevel = path.level - 1
                        result = Constants.LEVEL_DOWN
                        if (nextLevel == 0) {
                            recapTxt.text = getString(R.string.level_same)
                            resultImg.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_level_same
                                )
                            )
                            resultImg.visibility = View.VISIBLE
                        } else {
                            recapTxt.text = getString(R.string.level_down)
                            resultImg.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_level_down
                                )
                            )
                            resultImg.visibility = View.VISIBLE
                        }
                    }
                    Constants.LEVEL_SAME -> {
                        result = Constants.LEVEL_SAME
                        recapTxt.text = getString(R.string.level_same)
                        resultImg.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_level_same
                            )
                        )
                        resultImg.visibility = View.VISIBLE
                    }
                    Constants.LEVEL_UP -> {
                        nextLevel = path.level + 1
                        result = Constants.LEVEL_UP
                        if (nextLevel == 5) {
                            recapTxt.text = getString(R.string.level_max)
                            resultImg.setImageResource(R.drawable.ic_throphy)
                            resultImg.visibility = View.VISIBLE
                        } else {
                            recapTxt.text = getString(R.string.level_up)
                            resultImg.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_level_up
                                )
                            )
                            resultImg.visibility = View.VISIBLE
                        }
                    }
                }
            }

            if(!checkIfLevelChanged){
                recapTxt.visibility = View.INVISIBLE

                buttonOk.text = "EDIT PATH"
                buttonOk.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit, 0)
                buttonOk.setOnClickListener {
                    val appInfo = requireActivity().packageManager.getApplicationInfo(path.appList[0].appPackage, 0)
                    val bundle = bundleOf("apps" to mutableListOf(SuggestionOnApp(
                        path.appList[0].appName,
                        path.appList[0].appPackage,
                        path.int_duration.toLong(),
                        path.hasVibration,
                        path.hasDisplayModification
                    )), "actual" to 0, "comesFrom" to "path_edit", "includedApps" to mutableListOf(true))
                    navController.navigate(R.id.action_nav_intervention2_recap_to_createPathFragment, bundle)
                }
            }
        }

        buttonOk.setOnClickListener {
            if (result != -1) {
                when (result) {
                    Constants.LEVEL_UP -> {
                        if (nextLevel == 5)
                            openDialogMax()
                        else
                            openDialogUp()
                    }
                    Constants.LEVEL_DOWN -> {
                        if (nextLevel == 0) {
                            viewModel.pathLevelUpdate(pathN, { operationSuccess() }, { operationFailure() })
                        } else
                            openDialogDown()
                    }
                    Constants.LEVEL_SAME -> {
                        viewModel.pathLevelUpdate(pathN, { operationSuccess() }, { operationFailure() })
                    }
                }
            }
        }


    }

    private fun checkResults(nInt: Float, nStartPen: Float, nEndPen: Float, nSecPen: Float, nDelayPen: Float): Any {
        return when {
            nDelayPen > nInt * Constants.DOWN_RATIO_DEL_PEN -> Constants.LEVEL_DOWN
            (nEndPen > nInt * Constants.DOWN_RATIO_END_PEN && nSecPen > Constants.DOWN_AVG_SEC_DELAY *nEndPen) -> Constants.LEVEL_DOWN
            (   nDelayPen <= nInt * Constants.UP_RATIO_DEL_PEN
                    && nEndPen <= nInt * Constants.UP_RATIO_END_PEN
                    && nSecPen <= Constants.UP_AVG_SEC_DELAY * nEndPen ) -> Constants.LEVEL_UP
            else -> Constants.LEVEL_SAME
        }
    }

    private fun openDialogDown() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_level_info, null)
        val interventionText = dialogView.findViewById<TextView>(R.id.intervention_info)
        val levelText =  dialogView.findViewById<TextView>(R.id.level_info)
        interventionText.text = getString(R.string.path_levelDown_text)
        when (nextLevel) {
            1 -> levelText.text = getString(R.string.info_intervention2_level1)
            2 -> levelText.text = getString(R.string.info_intervention2_level2)
            3 -> levelText.text = getString(R.string.info_intervention2_level3)
        }

        AlertDialog.Builder(requireContext()).setView(dialogView).apply {
            setTitle(R.string.path_levelDown_title)
            setPositiveButton(R.string.ok) { dialog, _ ->
                pathN.level -= 1
                viewModel.pathLevelUpdate(pathN, { operationSuccess() }, { operationFailure() })
                dialog.dismiss()
            }
        }.show()
    }

    private fun openDialogUp() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_level_info, null)
        val interventionText = dialogView.findViewById<TextView>(R.id.intervention_info)
        val levelText =  dialogView.findViewById<TextView>(R.id.level_info)
        interventionText.text = getString(R.string.path_levelUp_text)
        when (nextLevel) {
            2 -> levelText.text = getString(R.string.info_intervention2_level2)
            3 -> levelText.text = getString(R.string.info_intervention2_level3)
            4 -> levelText.text = getString(R.string.info_intervention2_level4)
        }

        AlertDialog.Builder(requireContext()).setView(dialogView).apply {
            setTitle(R.string.path_levelUp_title)
            setPositiveButton(R.string.ok) { dialog, _ ->
                pathN.level += 1
                viewModel.pathLevelUpdate(pathN, { operationSuccess() }, { operationFailure() })
                dialog.dismiss()
            }
        }.show()
    }

    private fun openDialogMax() {
        AlertDialog.Builder(requireContext())
                .setView(R.layout.dialog_level_max)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    viewModel.pathMaxLevelUpdate(pathN, { operationSuccess() }, { operationFailure() })
                    dialog.dismiss()
                }
                .show()
    }

    private fun operationSuccess(){
        navController.navigate(R.id.action_nav_intervention2_recap_to_nav_home_paths)
    }

    private fun operationFailure() {
        //Toast.makeText(requireContext(), "Database error, retry later", Toast.LENGTH_LONG).show()
    }
}