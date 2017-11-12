package com.akotnana.pollr.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.ContactsContract;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by anees on 11/11/2017.
 */

public class DataStorage {

    Context context;

    public DataStorage(Context con) {
        context = con;
    }

    public void storeData(String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString(key,value);
        editor.apply();
    }

    public String getData(String key) {
        return context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).getString(key, "");
    }

    public String md5(final String s) throws NoSuchAlgorithmException {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
