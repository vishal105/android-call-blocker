package com.vishal.callblocker.broadcastreceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import com.vishal.callblocker.R
import com.vishal.callblocker.blockednumber.BlockedNumberDatabase
import com.vishal.callblocker.util.AsyncExecutorUtil
import com.vishal.callblocker.activity.IncomingCallActivity




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
                dialog(context, intent)
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

                dialog(context, intent)

                // TODO Some UI
            })
        }
    }

    private fun dialog(context: Context, intent: Intent?) {
        val i = Intent(context, IncomingCallActivity::class.java)
        i.putExtras(intent)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_ANIMATION)
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context.startActivity(i)

//        showCustomPopupMenu(context)
/*
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
                my_callIntent.data = Uri.parse("tel:" + intent?.getStringExtra(
                        TelephonyManager.EXTRA_INCOMING_NUMBER)
                )
//                if (Activity.checkSelfPermission(context, Manifest.permission.CALL_PHONE) !=
//                        PackageManager.PERMISSION_GRANTED) {
//
//                }
                context?.startActivity(my_callIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Error in your phone call" + e.message, Toast.LENGTH_LONG).show()
            }

        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            alertDialog?.dismiss()
        }

        builder.setOnDismissListener {

        }
        // Create the AlertDialog
        alertDialog = builder.create()
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT or WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
        Handler().postDelayed(Runnable {
            alertDialog.dismiss()
        }, 5000)*/
    }

    private fun showCustomPopupMenu(context: Context?) {
        var windowManager2 = context?.getSystemService(WINDOW_SERVICE) as WindowManager?
        val layoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view = layoutInflater!!.inflate(R.layout.xxact_copy_popupmenu, null)
        var params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE or WindowManager.LayoutParams.TYPE_SYSTEM_ALERT or WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        params.gravity = Gravity.CENTER or Gravity.CENTER
        params.x = 0
        params.y = 0
        windowManager2?.addView(view, params)
    }

    companion object {
        private val LOG_TAG = IncomingCallReceiver::class.java.simpleName
    }
}
