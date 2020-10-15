package com.anarock.calls.utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Report;

import java.util.Locale;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class PhoneNumberFormatter {

    private static String getCountryISO(final Context context) {
        TelephonyManager telephonyManager = ContextCompat.getSystemService(context, TelephonyManager.class);
        if (telephonyManager != null && !TextUtils.isEmpty(telephonyManager.getNetworkCountryIso())) {
            return telephonyManager.getNetworkCountryIso().toUpperCase(Locale.US);
        } else {
            return Locale.getDefault().getCountry();
        }
    }

    public static String getE164PhoneNumber(final Context context, final String phoneNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(context);
        try {
            Phonenumber.PhoneNumber numberProto = phoneNumberUtil.parse(phoneNumber, PhoneNumberFormatter.getCountryISO(context));
            return phoneNumberUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            Bugsnag.notify(e, new com.bugsnag.android.Callback() {
                @Override
                public void beforeNotify(@NonNull Report report) {
                    report.getError().getMetaData().addToTab("Additional Info", "raw phone", phoneNumber);
                }
            });
        }
        return null;
    }
}
