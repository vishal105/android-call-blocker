package com.alexazhu.callblocker.broadcastreceiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.alexazhu.callblocker.blockednumber.BlockedNumber;
import com.alexazhu.callblocker.blockednumber.BlockedNumberDao;
import com.alexazhu.callblocker.blockednumber.BlockedNumberDatabase;
import com.alexazhu.callblocker.util.AsyncExecutorUtil;

import java.util.Optional;

public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = IncomingCallReceiver.class.getSimpleName();

    @Override
    @SuppressLint({"MissingPermission"})    // Permissions checked when app opened; just fail here if missing
    public void onReceive(Context context, Intent intent) {
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            Log.e(LOG_TAG, String.format("IncomingCallReceiver called with incorrect intent action: %s", intent.getAction()));
            return;
        }

        final String newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.d(LOG_TAG, String.format("Call state changed to %s", newState));

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(newState)) {
            final String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (phoneNumber == null) {
                Log.d(LOG_TAG, "Ignoring call; for some reason every state change is doubled");
                return;
            }
            Log.i(LOG_TAG, String.format("Incoming call from %s", phoneNumber));

            BlockedNumberDao blockedNumberDao = BlockedNumberDatabase.getInstance(context).blockedNumberDao();
            AsyncExecutorUtil.getInstance().getExecutor().execute(() -> {
                Optional<BlockedNumber> match = blockedNumberDao.getAll().stream().filter(blockedNumber -> blockedNumber.getRegex().matcher(phoneNumber).find()).findAny();
                if (!match.isPresent()) {
                    Log.i(LOG_TAG, "No blocked number matched");
                    return;
                }
                Log.i(LOG_TAG, String.format("Blocked number matched: %s", match.get().toFormattedString()));

                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                telecomManager.endCall();

                // TODO Some UI
            });
        }
    }
}
