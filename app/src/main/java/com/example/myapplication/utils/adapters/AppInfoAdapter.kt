package com.example.myapplication.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.db.data.AppInfo

class AppInfoAdapter(private val list: ArrayList<AppInfo>) : RecyclerView.Adapter<AppInfoAdapter.ViewHolder>()  {

    var onItemClick: ((AppInfo) -> Unit)? = null
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_app, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val appName: TextView = view.findViewById(R.id.app_name)
        private val appIcon: ImageView = view.findViewById(R.id.app_icon)

        init {
            view.setOnClickListener {
                onItemClick?.invoke(list[bindingAdapterPosition])
            }
        }

        fun bind(app: AppInfo) {
            appName.text = app.appName
            appIcon.setImageDrawable(app.icon)
        }
    }
}