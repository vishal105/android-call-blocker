package com.vishal.callblocker.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast

class IncomingCallActivity : AppCompatActivity() {

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

        builder.setOnDismissListener {
            finish()
        }
        // Create the AlertDialog
        alertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
        Handler().postDelayed(Runnable {
            alertDialog.dismiss()
        },5000)
    }
}
