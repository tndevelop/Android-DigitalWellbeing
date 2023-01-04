package com.example.myapplication.Rasa

import android.content.Context
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.db.data.SuggestionOnApp
import com.example.myapplication.utils.adapters.AppSuggestionAdapter

class MessageAdapter(
    var context: Context,
    var messageList: MutableList<MessageClass>,
    navController: NavController,
    activity: FragmentActivity?
):RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val USER_LAYOT = 0
    private val BOT_LAYOUT = 1
    private val activity = activity


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if(viewType.equals(USER_LAYOT)) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.user_message_box, parent, false)
            return MessageViewHolder(view)
        }else{
            val view =
                LayoutInflater.from(context).inflate(R.layout.bot_message_box, parent, false)
            return MessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if(currentMessage.sender.equals(USER_LAYOT)){
            holder.message_view.setText(currentMessage.message)

        }else if(currentMessage.sender.equals(BOT_LAYOUT)){
            holder.message_view.setText(Html.fromHtml(currentMessage.message))
            if(currentMessage.dangerousApps.size > 0){
                val adapter = setupAdapter(holder.suggestion_list, currentMessage.dangerousApps)
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val view = messageList[position]
        if(view.sender.equals(USER_LAYOT)){
            return USER_LAYOT
        }else{
            return BOT_LAYOUT
        }
    }

    class MessageViewHolder(view:View): RecyclerView.ViewHolder(view){
        val message_view = view.findViewById<TextView>(R.id.message_tv)
        val time_view = view.findViewById<TextView>(R.id.time_tv)
        val suggestion_list = view.findViewById<RecyclerView>(R.id.itemList)
    }

    private fun setupAdapter(v: RecyclerView, list: List<SuggestionOnApp>): AppSuggestionAdapter {
        v.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        val adapter = AppSuggestionAdapter(list)
        v.adapter = adapter
        return adapter
    }
}

