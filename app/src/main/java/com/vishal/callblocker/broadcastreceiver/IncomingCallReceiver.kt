package com.vishal.callblocker.broadcastreceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
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
        var wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var params1 = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                , WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                , WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT)

        params1.height = 75
        params1.width = 512
        params1.x = 265
        params1.y = 400
        params1.format = PixelFormat.TRANSLUCENT

        var ly1 = LinearLayout(context)
        ly1.setBackgroundColor(Color.BLACK)
        ly1.setOrientation(LinearLayout.VERTICAL)

        wm.addView(ly1, params1)
        /*val i = Intent(context, IncomingCallActivity::class.java)
        i.putExtras(intent)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context.startActivity(i)*/
    }

    companion object {
        private val LOG_TAG = IncomingCallReceiver::class.java.simpleName
    }
}
