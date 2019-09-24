package com.vishal.callblocker.broadcastreceiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
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
                if (match?.isPresent != true) {
                    Log.i(LOG_TAG, "No blocked number matched")
                    return@Runnable
                }
                Log.i(LOG_TAG, String.format("Blocked number matched: %s", match.get().toFormattedString()))

                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.endCall()

                // TODO Some UI
            })
        } else {
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (phoneNumber == null) {
                Log.d(LOG_TAG, "Ignoring call; for some reason every state change is doubled")
                return
            }
            Log.i(LOG_TAG, String.format("outGoing  call from %s", phoneNumber))

            val blockedNumberDao = BlockedNumberDatabase.getInstance(context)?.blockedNumberDao()
            if (phoneNumber.equals("1234567890")) {
                Log.i(LOG_TAG, String.format("Blocked number matched: %s", phoneNumber))
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.endCall()
                dialog(context, phoneNumber)
            }
            AsyncExecutorUtil.instance.executor.execute(Runnable {
                val match = blockedNumberDao?.all?.stream()?.filter { blockedNumber -> blockedNumber.regex.matcher(phoneNumber).find() }?.findAny()
                if (match?.isPresent != true) {
                    Log.i(LOG_TAG, "No blocked number matched")
                    return@Runnable
                }
                Log.i(LOG_TAG, String.format("Blocked number matched: %s", match.get().toFormattedString()))

                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.endCall()
                dialog(context, match.get().toFormattedString())

                // TODO Some UI
            })
        }
    }

    companion object {
        private val LOG_TAG = IncomingCallReceiver::class.java.simpleName
    }

    @SuppressLint("MissingPermission")
    fun dialog(context: Context, phn_no: String?) {
        var alertDialog: AlertDialog? = null
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Title")
        //set message for alert dialog
        builder.setMessage("Write your message here.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Redial") { dialogInterface, which ->
            try {
                val my_callIntent = Intent(Intent.ACTION_CALL)
                my_callIntent.data = Uri.parse("tel:$phn_no")
//                if (Activity.checkSelfPermission(context, Manifest.permission.CALL_PHONE) !=
//                        PackageManager.PERMISSION_GRANTED) {
//
//                }
                context.startActivity(my_callIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Error in your phone call" + e.message, Toast.LENGTH_LONG).show()
            }

        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            alertDialog?.dismiss()
        }
        // Create the AlertDialog
        alertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
