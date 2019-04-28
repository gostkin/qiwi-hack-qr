package org.chainify.qiwi_blockchain;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.bouncycastle.jcajce.provider.digest.Blake2b;

import static org.chainify.qiwi_blockchain.PreferencesUtility.*;


public class SaveSharedPreference {

    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    // TODO: make adequate encryption
    public static void setKeys(Context context, ECKeyPair pair, String password) {
        SharedPreferences.Editor editor = getPreferences(context).edit();

        editor.remove(ENCRYPTED_PK);
        editor.putString(ENCRYPTED_PK, new String(pair.getPub()));
        editor.remove(ENCRYPTED_SK);
        editor.putString(ENCRYPTED_SK, pair.getPriv().toString());
        editor.remove(PASSWORD_HASH);

        editor.putString(PASSWORD_HASH, password);
        editor.apply();
        editor.commit();
    }

    public static String getPasswordHash(Context context) {
        return getPreferences(context).getString(PASSWORD_HASH, "");
    }

    public static String getEncryptedPK(Context context) {
        return getPreferences(context).getString(ENCRYPTED_PK, "");
    }

    public static String getEncryptedSK(Context context) {
        return getPreferences(context).getString(ENCRYPTED_SK, "");
    }

    public static void setVerified(Context context, boolean status) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(IDENTITY_VERIFIED, status);
        editor.apply();
        editor.commit();
    }

    public static boolean getVerified(Context context) {
        return getPreferences(context).getBoolean(IDENTITY_VERIFIED, false);
    }
}