package com.vishal.callblocker.blockednumber

import android.arch.persistence.room.*
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.regex.Pattern

@Entity(tableName = "blockednumbers")
data class BlockedNumber(@ColumnInfo(name = "type")
                         var type: BlockedNumberType? = BlockedNumberType.EXACT_MATCH,
                         @PrimaryKey
                         var regex: Pattern = Pattern.compile(""),
                         @ColumnInfo(name = "country_code")
                         var countryCode: String? = "",
                         @ColumnInfo(name = "phone_number")
                         var phoneNumber: String? = "123"
) {

    @Ignore
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    private val internationalNumber: String
        get() = String.format("\\+%s%s", countryCode, phoneNumber)

    init {
        this.countryCode = countryCode?.replace("+", "")
        when (type) {
            BlockedNumberType.EXACT_MATCH -> {
                require(Pattern.matches("\\d+", phoneNumber)) { String.format("Blocked number must be a string of digits only: %s", phoneNumber) }
                this.regex = Pattern.compile("^$internationalNumber$")
            }
            BlockedNumberType.REGEX_MATCH -> {
                require(Pattern.matches("\\d+", phoneNumber)) { String.format("Blocked number prefix must be a string of digits only: %s", phoneNumber) }
                this.regex = Pattern.compile("^$internationalNumber\\d+$")
            }
            else -> throw IllegalArgumentException(String.format("Unknown blocked number type: %s", type))
        }
    }

    fun toFormattedString(): String {
        val formatter = phoneNumberUtil.getAsYouTypeFormatter(phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode?:"0")))
        var formattedNumber = ""
        for (digit in phoneNumber?.toCharArray() ?: CharArray(0)) {
            formattedNumber = formatter.inputDigit(digit)
        }
        return formattedNumber
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BlockedNumber) {
            return false
        }

        val otherObj = other as BlockedNumber?

        return this.type === otherObj?.type && this.regex?.pattern() == otherObj?.regex?.pattern()
    }

    override fun toString(): String {
        return String.format("%s|%s", type, regex)
    }

    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + regex.hashCode()
        result = 31 * result + (countryCode?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (phoneNumberUtil?.hashCode() ?: 0)
        return result
    }
}


class BlockNumberTypeConvertor {
    @TypeConverter
    fun typeToString(type: BlockedNumberType): String {
        return type.name
    }

    @TypeConverter
    fun typeFromString(type: String): BlockedNumberType {
        return BlockedNumberType.valueOf(type)
    }

    @TypeConverter
    fun patternToString(regex: Pattern): String {
        return regex.pattern()
    }

    @TypeConverter
    fun patternFromString(regex: String): Pattern {
        return Pattern.compile(regex)
    }
}