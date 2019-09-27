package com.vishal.callblocker.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.telephony.TelephonyManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.osquare.support.utils.sharedPreference.SharedPrefsUtil
import kotlinx.android.synthetic.main.item_phone_number.*


class IncomingCallActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            Log.d("IncomingCallActivity: onCreate: ", "flag2");
            dialog(context = this, phn_no = getIntent()?.getStringExtra(
                    TelephonyManager.EXTRA_INCOMING_NUMBER)
            )
        } catch (e: Exception) {
            Log.d("Exception", e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState)
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
                val intent = Intent(Intent.ACTION_DIAL)
                SharedPrefsUtil.setNumber(phn_no,this)
                intent.data = Uri.parse("tel:$phn_no")
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Error in your phone call" + e.message, Toast.LENGTH_LONG).show()
            }

        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            SharedPrefsUtil.setNumber("",this)
            alertDialog?.dismiss()
        }

        builder.setOnDismissListener {
            finish()
            java.lang.System.exit(0)
        }
        // Create the AlertDialog
        alertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
        Handler().postDelayed(Runnable {
            alertDialog.dismiss()
            SharedPrefsUtil.setNumber("",this)
        }, 5000)
    }
}
