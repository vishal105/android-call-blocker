package com.vishal.callblocker.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button

import com.vishal.callblocker.R
import com.vishal.callblocker.util.PermissionsUtil

class RequestPermissionsActivity : AppCompatActivity() {

    private var permissionsUtil: PermissionsUtil? = null
    private var permissionsButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_permissions)

        permissionsButton = findViewById(R.id.permissions_button)
        permissionsButton?.setOnClickListener(PermissionsButtonListener())

        this.permissionsUtil = PermissionsUtil(this)
    }

    override fun onStart() {
        super.onStart()

        val permissionsGranted = permissionsUtil?.checkPermissions()
        if (!(permissionsGranted == true)) {
            permissionsButton?.isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != PERMISSIONS_REQUEST_CODE) {
            return
        }

        if (grantResults.size != permissions.size) {
            val errorMsg = "Permissions not granted"
            resetOnMissingPermissions(errorMsg)
            return
        }

        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                val errorMsg = String.format("Permission %s not granted", permissions[i])
                resetOnMissingPermissions(errorMsg)
                return
            }
        }

        Log.d(LOG_TAG, "Permissions granted")
        finish()       // Return to ConfigurationActivity
    }

    private fun resetOnMissingPermissions(errorMsg: String) {
        Log.e(LOG_TAG, errorMsg)
        permissionsButton?.isEnabled = true
    }

    private inner class PermissionsButtonListener : View.OnClickListener {
        override fun onClick(v: View) {
            permissionsButton?.isEnabled = false
            requestPermissions(PermissionsUtil.REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }

    companion object {
        private val LOG_TAG = RequestPermissionsActivity::class.java.simpleName

        private val PERMISSIONS_REQUEST_CODE = 1
    }
}
