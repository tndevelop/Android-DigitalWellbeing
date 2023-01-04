package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.db.data.User
import com.example.myapplication.managers.AlarmsManager
import com.example.myapplication.services.AppMonitorService
import com.example.myapplication.utils.Constants
import com.example.myapplication.viewModels.HomeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import androidx.lifecycle.Observer

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: HomeViewModel
    private val alarmManager = AlarmsManager()

    val userObserver = Observer<User> { user ->
        if(user!= null)
            alarmManager.setNotificationAlarm(applicationContext, Constants.TODAY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home_paths,R.id.nav_phone_monitoring, R.id.nav_notification_list, R.id.nav_chat
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val name = intent.getStringExtra("USER_NAME")
        val email = intent.getStringExtra("USER_EMAIL")
        val id = intent.getStringExtra("USER_ID")
        if(name != null && email != null)
            setupDrawer(name, email)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        if (id != null)
            viewModel.uploadUserID(id)


        viewModel.currentUser.observe(this, userObserver)
        alarmManager.setUploadAlarm(applicationContext, Constants.TODAY)
        val startIntent = Intent(applicationContext, AppMonitorService::class.java)
        applicationContext!!.startService(startIntent)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupDrawer(userName: String, userEmail: String) {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val name: TextView = headerView.findViewById(R.id.username)
        val email: TextView = headerView.findViewById(R.id.usermail)
        val logoutButton: ImageButton = headerView.findViewById(R.id.logout_button)

        name.text = userName
        email.text = userEmail

        logoutButton.setOnClickListener {
            logout()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun logout(revokeAccess: Boolean = false) {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut()
        // Google revoke access or sign out
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.tokenId_googleSingIn))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso).apply { if(revokeAccess) revokeAccess() else signOut() }
        viewModel.deleteUser()
    }


}