package com.example.myapplication.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.db.data.Path
import com.example.myapplication.utils.Functions
import com.example.myapplication.viewModels.HomeViewModel
import java.util.*


class PathAdapter(private val list: ArrayList<Path>) : RecyclerView.Adapter<PathAdapter.ViewHolder>() {

    private val functions = Functions()
    var onItemClick: ((Path) -> Unit)? = null
    lateinit var context: Context
    var vibrationIcon = 0;
    var brightnessIcon = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_path, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.path_icon)
        private val intervention: TextView = view.findViewById(R.id.intervention)
        private val level: TextView = view.findViewById(R.id.level)
        private val catchPhrase: TextView = view.findViewById(R.id.app_list)
        private val recap: LinearLayoutCompat = view.findViewById(R.id.recap_layout)
        private val minutes: TextView = view.findViewById(R.id.minutes)

        init{
            view.setOnClickListener{
                onItemClick?.invoke(list[bindingAdapterPosition])
            }
            view.setOnLongClickListener {
                Toast.makeText(context, context.getString(R.string.delete_info), Toast.LENGTH_LONG).show()
                true
            }
        }

        fun bind(path : Path) {
            val appInfo = context.packageManager.getApplicationInfo(path.appList[0].appPackage, 0)
            icon.setImageDrawable(appInfo.loadIcon(context.packageManager))
            intervention.text = path.appList[0].appName
            vibrationIcon = 0
            brightnessIcon = 0
            if (functions.checkRecap(path)) {
                recap.visibility = View.VISIBLE
                //catchPhrase.text = context.getString(R.string.recap_catchphrase)
            }else {

                recap.visibility = View.GONE
            }
            catchPhrase.text = " "
            if(path.hasVibration)
                vibrationIcon = R.drawable.ic_vibration
            if (path.hasDisplayModification)
                brightnessIcon = R.drawable.ic_brightness

            catchPhrase.setCompoundDrawablesWithIntrinsicBounds(brightnessIcon, 0, vibrationIcon, 0)

            level.text =  context.getString(R.string.level_placeholder, path.level)
            minutes.text = path.int_duration.toString()

        }
    }

    fun deleteItem(position: Int, viewModel: HomeViewModel, operationSuccess: () -> Unit, operationFailure: () -> Unit) {
        viewModel.removePath(list[position], operationSuccess, operationFailure)
        notifyDataSetChanged()
    }
}