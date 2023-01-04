package com.example.myapplication.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.db.data.Path
import com.example.myapplication.db.data.SuggestionOnApp
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import com.example.myapplication.viewModels.HomeViewModel
import java.util.ArrayList

class AppSuggestionAdapter(private val list: List<SuggestionOnApp>) : RecyclerView.Adapter<AppSuggestionAdapter.ViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suggested_app, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.app_icon)
        private val name: TextView = view.findViewById(R.id.app_name)
        private val time: TextView = view.findViewById(R.id.time)
        private val brightness: TextView = view.findViewById(R.id.brightness)
        private val vibration: TextView = view.findViewById(R.id.vibration)


        fun bind(app : SuggestionOnApp) {
            val appInfo = context.packageManager.getApplicationInfo(app.appPackage, 0)
            icon.setImageDrawable(appInfo.loadIcon(context.packageManager))
            name.text = app.appName
            time.text = app.suggestedTime.toString()
            if(!app.greyOutSuggested)
                brightness.visibility = View.GONE
            if(!app.vibrationSuggested)
                vibration.visibility = View.GONE

        }
    }

}