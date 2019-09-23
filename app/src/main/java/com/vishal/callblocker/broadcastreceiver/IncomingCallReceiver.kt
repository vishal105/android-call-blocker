package com.vishal.callblocker.broadcastreceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import com.vishal.callblocker.blockednumber.BlockedNumberDatabase
import com.vishal.callblocker.util.AsyncExecutorUtil

class IncomingCallReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")    // Permissions checked when app opened; just fail here if missing
    override fun onReceive(context: Context, intent: Intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {
            Log.e(LOG_TAG, String.format("IncomingCallReceiver called with incorrect intent action: %s", intent.action))
            return
        }

        val newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d(LOG_TAG, String.format("Call state changed to %s", newState))

        if (TelephonyManager.EXTRA_STATE_RINGING == newState) {
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (phoneNumber == null) {
                Log.d(LOG_TAG, "Ignoring call; for some reason every state change is doubled")
                return
            }
            Log.i(LOG_TAG, String.format("Incoming call from %s", phoneNumber))

            val blockedNumberDao = BlockedNumberDatabase.getInstance(context)?.blockedNumberDao()
            AsyncExecutorUtil.instance.executor.execute(Runnable {
                val match = blockedNumberDao?.all?.stream()?.filter { blockedNumber -> blockedNumber.regex.matcher(phoneNumber).find() }?.findAny()
                if (!(match?.isPresent == true)) {
                    Log.i(LOG_TAG, "No blocked number matched")
                    return@Runnable
                }
                Log.i(LOG_TAG, String.format("Blocked number matched: %s", match.get().toFormattedString()))

                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.endCall()

                // TODO Some UI
            })
        }
    }

    companion object {
        private val LOG_TAG = IncomingCallReceiver::class.java.simpleName
    }
}
