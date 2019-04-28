package org.chainify.qiwi_blockchain_qrcode;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import org.bouncycastle.jcajce.provider.digest.Blake2b;

import static org.chainify.qiwi_blockchain_qrcode.PreferencesUtility.*;


public class SaveSharedPreference {

    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    // TODO: make adequate encryption
    public static void setKeys(Context context, ECKeyPair pair, String password) {
        SharedPreferences.Editor editor = getPreferences(context).edit();

        editor.putString(ENCRYPTED_PK, Base64.encodeToString(pair.getPub(), 0));
        editor.putString(ENCRYPTED_SK, pair.getPriv().toString());

        editor.putString(PASSWORD_HASH, password);
        editor.apply();
        editor.commit();
    }

    public static void resetKeys(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();

        editor.remove(ENCRYPTED_PK);
        editor.remove(ENCRYPTED_SK);
        editor.remove(PASSWORD_HASH);
        editor.remove(IDENTITY_VERIFIED);
        editor.remove(REG_FINISHED);
        editor.remove(UID);

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


    public static String getID(Context context) {
        return getPreferences(context).getString(UID, "");
    }

    public static void setVerified(Context context, boolean status, String id) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(IDENTITY_VERIFIED, status);
        if (status) {
            editor.putString(UID, id);
        } else {
            editor.remove(UID);
        }
        editor.apply();
        editor.commit();
    }


    public static boolean getVerified(Context context) {
        return getPreferences(context).getBoolean(IDENTITY_VERIFIED, false);
    }

    public static void setFinished(Context context, boolean status) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(REG_FINISHED, status);
        editor.apply();
        editor.commit();
    }


    public static boolean getFinished(Context context) {
        return getPreferences(context).getBoolean(REG_FINISHED, false);
    }

    public static String toHex(byte[] bytes) {
        return Base64.encodeToString(bytes, 0);
    }

    public static byte[] blake2b256Digest(byte[]... data) {
        Blake2b.Blake2b256 digest = new Blake2b.Blake2b256();
        digest.reset();
        for (byte[] bytes : data) {
            digest.update(bytes);
        }
        return digest.digest();
    }
}