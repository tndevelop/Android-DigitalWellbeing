package com.example.myapplication.activities

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.fragments.PermissionFragment
import com.example.myapplication.managers.PermissionManager
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import com.example.myapplication.utils.MyDeviceAdmin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PermissionActivity: AppCompatActivity() {
    private var batteryOptimization = false
    private val externalScope: CoroutineScope = GlobalScope
    private val functions = Functions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

    }

    override fun onResume() {
        super.onResume()
        val permissionManager = PermissionManager(applicationContext)

        if(!functions.hasWriteSettingsPermission(applicationContext)){
            val f:PermissionFragment = PermissionFragment.newInstance(getString(R.string.permission_brightness), getString(R.string.permission_brightness_description)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            supportFragmentManager.beginTransaction().replace(R.id.main_container, f).commit()
        }
        else if(!permissionManager.isGranted(PermissionManager.Permission.ADMIN)) {
            val f: PermissionFragment = PermissionFragment.newInstance(getString(R.string.permission_admin), getString(R.string.permission_admin_description)) {
                val compName = ComponentName(applicationContext, MyDeviceAdmin::class.java)
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getText(R.string.permission_admin_description))
                startActivityForResult(intent, Constants.RESULT_ENABLE)
            }
            supportFragmentManager.beginTransaction().replace(R.id.main_container, f).commit()
        }

        else if (!permissionManager.isGranted(PermissionManager.Permission.PHONE_DATA)) {
            val f: PermissionFragment = PermissionFragment.newInstance(getString(R.string.permission_usage_stats), getString(R.string.permission_usage_stats_description)){
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivity(intent)
            }
            supportFragmentManager.beginTransaction().replace(R.id.main_container, f).commit()
        }

        else if (!permissionManager.isGranted(PermissionManager.Permission.POPUP)) {
            val f:PermissionFragment = PermissionFragment.newInstance(getString(R.string.permission_popup), getString(R.string.permission_popup_description)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(intent)
            }
            supportFragmentManager.beginTransaction().replace(R.id.main_container, f).commit()
        }

        else if (!permissionManager.isGranted(PermissionManager.Permission.BATTERY)) {
            val f:PermissionFragment = PermissionFragment.newInstance(getString(R.string.permission_battery), getString(R.string.permission_battery_description)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            supportFragmentManager.beginTransaction().replace(R.id.main_container, f).commit()
        }

        else if(!batteryOptimization && permissionManager.checkBatteryOptimization(applicationContext)) {
            val f:PermissionFragment = PermissionFragment.newInstance(getString(R.string.permission_startup), getString(R.string.permission_startup_description)) {
                val intent = permissionManager.batteryOptimization(applicationContext)
                if(intent != null)
                    startActivity(intent)
                else
                    Log.v("PERMISSION_ACTIVITY", "Device producer not registered")
            }
            supportFragmentManager.beginTransaction().replace(R.id.main_container, f).commit()
            batteryOptimization = true
        }

        else {
            externalScope.launch {
                val user = AppDatabase.getDatabase(applicationContext).userDao().checkUser()
                if(user == null) {

                    val intent = Intent(applicationContext, TutorialActivity::class.java)
                    intent.putExtra("FIRST_OPENING", true)
                    startActivity(intent)
                    finish()
                }
                else {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.RESULT_ENABLE -> if (resultCode == RESULT_OK) {
                Log.i("PERMISSION_ACTIVITY", "Admin enabled!")
            } else {
                Log.i("PERMISSION_ACTIVITY", "Admin enable FAILED!")
                Toast.makeText(applicationContext, getString(R.string.error_admin), Toast.LENGTH_LONG).show()
            }
        }
        return
    }

}
