package com.vishal.callblocker.layout

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.vishal.callblocker.R
import com.vishal.callblocker.activity.ConfigurationActivity
import com.vishal.callblocker.blockednumber.BlockedNumber
import com.vishal.callblocker.blockednumber.BlockedNumberType

class AddNumberDialogFragment : DialogFragment(), OnItemSelectedListener {
    private val LOG_TAG = AddNumberDialogFragment::class.java.simpleName

    private var countryList: Spinner? = null
    private var countryCodeView: TextView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val configActivity = activity as ConfigurationActivity?
        val args = arguments

        val builder = AlertDialog.Builder(configActivity)
        val inflater = configActivity?.layoutInflater

        val dialogView = args?.getInt(LAYOUT_ID_KEY)?.let { inflater?.inflate(it, null) }
        countryCodeView = dialogView?.findViewById(R.id.country_code)
        val numberView = dialogView?.findViewById<EditText>(R.id.phone_number)
        countryList = dialogView?.findViewById(R.id.country_list)
        val adapter = configActivity?.let { CountryListAdapter(it, this) }
        countryList?.adapter = adapter
        countryList?.onItemSelectedListener = this

        builder.setView(dialogView)
                .setPositiveButton("Add") { dialog, id ->
                    val countryCode = countryCodeView?.text.toString()
                    val phoneNumber = numberView?.text.toString()
                    val type = args?.getSerializable(DIALOG_TYPE) as BlockedNumberType
                    try {
                        val blockedNumber = BlockedNumber(type = type, countryCode = countryCode, phoneNumber = phoneNumber)
                        configActivity?.addNumber(blockedNumber)
                        Log.i(LOG_TAG, String.format("Added valid number: %s", blockedNumber))
                    } catch (e: IllegalArgumentException) {
                        Log.i(LOG_TAG, String.format("Tried to add invalid number: %s", phoneNumber))
                        Toast.makeText(configActivity, "Invalid number", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") { dialog, id -> }
                .setTitle(args?.getString(TITLE))

        val dialog = builder.create()

        dialog.setOnKeyListener(DialogInterface.OnKeyListener { view, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) {
                return@OnKeyListener false
            }
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                    true
                }
                else -> false
            }
        })

        return dialog
    }

    fun selectCountry(position: Int) {
        countryList?.setSelection(position)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val country = parent.getItemAtPosition(position) as CountryListAdapter.Country
        countryCodeView?.text = "+" + country.countryCode
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        countryCodeView?.text = ""
    }

    companion object {

        val LAYOUT_ID_KEY = "layout"
        val DIALOG_TYPE = "dialogType"
        val TITLE = "title"
    }
}
