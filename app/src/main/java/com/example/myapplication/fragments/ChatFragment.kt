package com.example.myapplication.fragments


import android.icu.util.TimeUnit
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Rasa.BotResponse
import com.example.myapplication.Rasa.MessageAdapter
import com.example.myapplication.Rasa.MessageClass
import com.example.myapplication.Rasa.MessageSender
import com.example.myapplication.db.data.*
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import com.example.myapplication.viewModels.HomeViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val functions = Functions()

    private lateinit var messageListView: RecyclerView
    private val USER = 0
    private val BOT = 1
    private lateinit var adapter: MessageAdapter
    private lateinit var sendBtn: AppCompatImageButton
    private lateinit var messageBox: EditText

    private var suggestionPool = mutableListOf<SuggestionOnApp>()
    private var selectedApp :SuggestionOnApp? = null
    private lateinit var navController: NavController
    private var allApps : List<CustomUsageStats> = listOf()
    private lateinit var comesFrom : String
    private lateinit var viewModel: HomeViewModel
    private var skipNext = false
    private var monitoringPhase = false

    val userObserver = Observer<User> { user ->
        if(user!=null) {
            denyAccessIfMonitoringPhase(functions.isMonitoringPhase(user.startDate))
        }
    }

    override fun onResume(){
        super.onResume()
        denyAccessIfMonitoringPhase(functions.isMonitoringPhase(viewModel.currentUser.value!!.startDate))
    }

    override fun onStart(){
        super.onStart()
        denyAccessIfMonitoringPhase(functions.isMonitoringPhase(viewModel.currentUser.value!!.startDate))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        comesFrom = getString(R.string.base_chat_start)
        navController = Navigation.findNavController(view)
        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        viewModel.initStats(requireActivity())
        if(arguments != null) {
            if(requireArguments().getString("originating") != null)
                comesFrom = requireArguments().getString("originating").toString()
        }

        if(listOf(getString(R.string.base_chat_start), getString(R.string.manual_chat_start), getString(R.string.proactivity_chat_start)).contains(comesFrom)){
            viewModel.currentUser.observe(requireActivity(), userObserver)
            viewModel.initConversation()
        }


        val linearLayoutManager = LinearLayoutManager(requireActivity())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        //linearLayoutManager.stackFromEnd = true
        messageListView = view.findViewById(R.id.message_list)
        messageListView.layoutManager = linearLayoutManager

        adapter = MessageAdapter(this.requireActivity(),viewModel.messageList, navController, activity)
        adapter.setHasStableIds(true)
        messageListView.adapter = adapter
        sendBtn = view.findViewById(R.id.send_btn)
        messageBox = view.findViewById(R.id.message_box)
        sendBtn.setOnClickListener{
            val msg = messageBox.text.toString()
            sendMessage(msg)
            messageBox.setText("")
        }

        viewModel.statList.observe(viewLifecycleOwner) { list ->
            allApps = list
            viewModel.lastWeekUsage.observe(viewLifecycleOwner) { lwList ->
                suggestionPool = functions.setUpChat(comesFrom, getArguments(), viewModel.dangerousApps, viewModel, allApps,
                                listOf(getString(R.string.base_chat_start), getString(R.string.manual_chat_start), getString(R.string.proactivity_chat_start), getString(R.string.save_chat_start), getString(R.string.modify_complete_chat_start)))
            }
        }

        //upgrade all created paths to path 2
        viewModel.getPathList().observe( viewLifecycleOwner) { pList ->
            pList.filter{ it.intervention == Constants.INTERVENTION_FIRST_WEEK}.forEach{
                functions.createPath2(it, viewModel)
            }
        }

        //send starting message to chatbot
        sendMessageNoDisplay(MessageClass(comesFrom, USER))
    }






    @OptIn(ExperimentalStdlibApi::class)
    private fun sendMessageNoDisplay(userMessage:MessageClass){
        var responseReceived = false
        var response :  Call<ArrayList<BotResponse>>


        val messageToBeSent = userMessage.copy()
        messageToBeSent.message = messageToBeSent.message.lowercase().capitalize()

        response = retrofitSend(messageToBeSent)
        response.enqueue(object : Callback<ArrayList<BotResponse>> {
            override fun onResponse(call: Call<ArrayList<BotResponse>>, response: Response<ArrayList<BotResponse>>) {
                responseReceived = true
                if (response.body() != null && response.body()?.size != 0) {
                    val message = replaceWithAppData(response.body()!![0].text, messageToBeSent.message, getArguments())
                    val messageToBeSent = processMessage(message)
                    if (messageToBeSent != null && !messageToBeSent.message.contains("\$")) {
                        addMessage(messageToBeSent)
                    }

                }
            }

            override fun onFailure(call: Call<ArrayList<BotResponse>>, t: Throwable) {
                val message = getString(R.string.check_connetion_message)
                viewModel.addMessage(MessageClass(message, BOT))
            }
        })

        //send message again if no response has been received
        Handler(Looper.getMainLooper()).postDelayed({
            if (!responseReceived)
                response = retrofitSend(messageToBeSent)
        }, Constants.SEND_AGAIN_TO_CHATBOT_DELAY)


    }
    private fun getHttpClient(): OkHttpClient? {
        //val logging = HttpLoggingInterceptor()
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY)


        //TODO : remove logging interceptors as it is to be used for development purpose
        return OkHttpClient.Builder().connectTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
            //.addInterceptor(logging)
            .build()
    }
    private fun retrofitSend(messageToBeSent: MessageClass): Call<ArrayList<BotResponse>> {
        //val okHttpClient = OkHttpClient()
        val retrofit = Retrofit.Builder()
            .baseUrl(viewModel.chatbotBaseUrl + getString(R.string.chatbot_api))
            .client(getHttpClient())
            .addConverterFactory(GsonConverterFactory.create()).build()
        val messagerSender = retrofit.create(MessageSender::class.java)
        Log.d("CHAT_DEBUG", "USER: " + messageToBeSent.message)
        val response = messagerSender.messageSender(messageToBeSent)
        return response
    }

    private fun processMessage(message: String): MessageClass? {
        if(skipNext) {
            skipNext = false
            return null
        }
        val finalMessage = MessageClass(message, BOT)
        if(message.startsWith(getString(R.string.modify_command))){
            Handler(Looper.getMainLooper()).postDelayed({
                functions.hideKeyBoard(requireContext(), view)
                val bundle = bundleOf("apps" to suggestionPool, "actual" to 0, "comesFrom" to comesFrom, "includedApps" to suggestionPool.map{ true})
                navController.navigate(R.id.action_nav_chat_to_createPathFragment, bundle)
            }, Constants.FORWARDING_DELAY)

            finalMessage.message = finalMessage.message.replace(getString(R.string.modify_command) + " ", "")
        }
        else if(message.startsWith(getString(R.string.create_command))){
            functions.createPaths(suggestionPool, viewModel)
            finalMessage.message = finalMessage.message.replace(getString(R.string.create_command) + " ", "")
        }
        else if(message.startsWith(getString(R.string.mod_saved_command))){
            sendMessageNoDisplay(MessageClass("Give_suggestions", USER))
            finalMessage.message = finalMessage.message.replace(getString(R.string.mod_saved_command) + " ", "")
        }
        else if(message.startsWith(getString(R.string.provide_suggestions_command))){
            if(suggestionPool.size > 0) {
                finalMessage.dangerousApps = suggestionPool.map { it.copy() }.toMutableList()
                finalMessage.message = finalMessage.message.replace(getString(R.string.provide_suggestions_command) + " ", "")
            }
            else {
                finalMessage.message = getString(R.string.no_bad_behaviour_msg)
                skipNext = true
            }
        }else if(message.startsWith(getString(R.string.main_page_command))){
            Handler(Looper.getMainLooper()).postDelayed({
                functions.hideKeyBoard(requireContext(), view)
                navController.navigate(R.id.action_nav_chat_to_nav_home_paths)
            }, Constants.FORWARDING_DELAY)

            finalMessage.message = finalMessage.message.replace(getString(R.string.main_page_command) + " ", "")
        }else if(message.startsWith(getString(R.string.manual_suggestion_command))){
            if(suggestionPool.size == 0) {
                finalMessage.message = getString(R.string.unused_app_msg)
            }else{
                finalMessage.message = finalMessage.message.replace(getString(R.string.manual_suggestion_command) + " ", "")
            }

        }
        return finalMessage
    }

    fun sendMessage(message:String){
        var userMessage = MessageClass()
        if(message.isEmpty()){
            Toast.makeText(requireActivity(),"Please type your message", Toast.LENGTH_SHORT).show()
        }

        else{
            userMessage = MessageClass(message,USER)
            addMessage(userMessage)
        }
        sendMessageNoDisplay(userMessage)
    }

    private fun addMessage(userMessage: MessageClass) {
        viewModel.addMessage(userMessage)
        adapter.notifyDataSetChanged()
        adapter.notifyItemInserted(adapter.getItemCount()-1)


        Handler(Looper.getMainLooper()).postDelayed({
            messageListView.smoothScrollToPosition(adapter.getItemCount() - 1)
        }, 300)

    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun replaceWithAppData(text: String, userMessage: String, arguments: Bundle?): String {
        selectedApp = functions.selectApp(suggestionPool, userMessage, selectedApp)
        val appName = arguments?.get("appName") as String?
        return functions.messageReplacement(text, suggestionPool, allApps, selectedApp, appName)
    }

    private fun denyAccessIfMonitoringPhase(monitoringPhase: Boolean) {
        if( monitoringPhase ) {
            functions.createPopUp(requireActivity(), getString(R.string.monitoring_phase), getString(R.string.monitoring_phase_message_chat))
            //denyAccessAction()
            this.monitoringPhase = true
        }else{
            this.monitoringPhase = false
        }
    }


}