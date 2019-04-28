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

        editor.putString(ENCRYPTED_PK, new String(pair.getPub()));
        editor.putString(ENCRYPTED_SK, pair.getPriv().toString());
        editor.putString(PASSWORD_HASH, password);
        editor.apply();
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
}