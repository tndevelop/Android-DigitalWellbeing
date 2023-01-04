package com.example.myapplication.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.db.data.Notification
import com.example.myapplication.utils.Constants
import java.text.SimpleDateFormat
import java.util.*


class NotificationAdapter(private val list: ArrayList<Notification>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>()  {

    var onItemClick: ((Notification) -> Unit)? = null
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_notification, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.notification_title)
        private val date: TextView = view.findViewById(R.id.notification_date)

        init {
            view.setOnClickListener {
                onItemClick?.invoke(list[bindingAdapterPosition])
            }
        }

        fun bind(notification: Notification) {
            if(notification.type == Constants.NOTIFICATION_USAGE)
                title.text = context.getString(R.string.notification_title_bad_usage)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN)
            val string = format.format(notification.date.time)
            date.text = string
        }
    }
}