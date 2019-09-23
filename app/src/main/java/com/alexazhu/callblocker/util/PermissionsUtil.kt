package com.alexazhu.callblocker.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

import java.util.HashSet

class PermissionsUtil(private val context: Context) {

    fun checkPermissions(): Boolean {
        Log.d(LOG_TAG, "Checking permissions")

        val missingPermissions = HashSet<String>()
        for (permission in PermissionsUtil.REQUIRED_PERMISSIONS) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }

        return missingPermissions.isEmpty()
    }

    companion object {
        private val LOG_TAG = PermissionsUtil::class.java!!.getSimpleName()

        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE)
    }
}
