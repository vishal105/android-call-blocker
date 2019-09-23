package com.vishal.callblocker.layout

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.vishal.callblocker.R
import com.vishal.callblocker.util.AsyncExecutorUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.IntStream

class CountryListAdapter(private val activity: Activity, dialog: AddNumberDialogFragment) : BaseAdapter() {
    private val phoneNumberUtil: PhoneNumberUtil
    private val deviceLocale: Locale
    private var countryCodes: List<Country>? = null

    init {
        this.deviceLocale = activity.resources.configuration.locales.get(0)
        this.phoneNumberUtil = PhoneNumberUtil.getInstance()
        AsyncExecutorUtil.instance.executor.execute(Runnable {
            this@CountryListAdapter.countryCodes = PhoneNumberUtil.getInstance().supportedCallingCodes.stream()
                    .map<Country>(Function<Int, Country> { Country(it) })
                    .sorted()
                    .collect(Collectors.toList<Country>())
            val defaultPosition = IntStream.range(0, countryCodes?.size ?: 0)
                    .filter { index -> countryCodes?.get(index)?.regionCode == deviceLocale.country }
                    .findFirst()
                    .orElse(0)
            activity.runOnUiThread {
                this@CountryListAdapter.notifyDataSetChanged()
                dialog.selectCountry(defaultPosition)
            }
        })
    }

    override fun getCount(): Int {
        return if (countryCodes != null) countryCodes?.size ?: 0 else 0
    }

    override fun getItem(position: Int): Any? {
        return countryCodes?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val inflater = activity.layoutInflater
        val itemView = inflater.inflate(R.layout.item_country, parent, false)

        val regionCode = itemView.findViewById<TextView>(R.id.country_name)
        regionCode.text = countryCodes?.get(position)?.countryName
        val flag = itemView.findViewById<TextView>(R.id.country_flag)
        flag.text = countryCodes?.get(position)?.flagCode

        return itemView
    }

    inner class Country(val countryCode: Int?) : Comparable<Country> {
        val regionCode: String
        val countryName: String
        val flagCode: String

        init {

            this.regionCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode ?: 0)
            val deviceLanguage = deviceLocale.language
            val countryLocale = Locale(deviceLanguage, regionCode)

            val name = countryLocale.displayCountry
            if (name == "World") {
                // Handle odd cases
                this.countryName = "\u200bOther " + this.countryCode
                this.flagCode = ""
            } else {
                this.countryName = name

                // See https://stackoverflow.com/a/35849652
                val firstLetter = Character.codePointAt(regionCode, 0) - 0x41 + 0x1F1E6
                val secondLetter = Character.codePointAt(regionCode, 1) - 0x41 + 0x1F1E6
                this.flagCode = String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
            }
        }

        override fun compareTo(o: Country): Int {
            return countryName.compareTo(o.countryName)
        }
    }
}
