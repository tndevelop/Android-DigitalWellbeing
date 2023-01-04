package com.example.myapplication.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.db.data.CustomUsageStats
import com.example.myapplication.utils.Functions

class AppUsageAdapter (private val list: List<CustomUsageStats>): RecyclerView.Adapter<AppUsageAdapter.ViewHolder>(){

    private val functions = Functions()
    var onItemClick: ((CustomUsageStats) -> Unit)? = null
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_app_usage, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val appIcon = view.findViewById<ImageView>(R.id.app_icon)
        private val appName = view.findViewById<TextView>(R.id.text_app_name)
        private val appUsage = view.findViewById<TextView>(R.id.text_screen_time)
        private val appOpenings = view.findViewById<TextView>(R.id.text_checked)


        init{
            view.setOnClickListener{
                onItemClick?.invoke(list[bindingAdapterPosition])
            }
        }

        fun bind(stat: CustomUsageStats) {
            appIcon.setImageDrawable(stat.appIcon)
            appName.text = stat.appName
            appUsage.text = context.getString(R.string.screen_time, functions.convertMsecToHHMM(stat.timeInForeground))
            appOpenings.text = context.getString(R.string.n_openings, stat.nOpenings)
        }

    }
}
