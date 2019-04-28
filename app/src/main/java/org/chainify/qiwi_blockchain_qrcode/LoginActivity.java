package org.chainify.qiwi_blockchain_qrcode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;


import org.bouncycastle.jcajce.provider.digest.Blake2b;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

public class LoginActivity extends AppCompatActivity {

    EditText password;
    Button submitBtn;
    RelativeLayout loginForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // SaveSharedPreference.resetKeys(getApplicationContext());
        setContentView(R.layout.activity_login);

        password = findViewById(R.id.passwordText);
        submitBtn = findViewById(R.id.submit);
        loginForm = findViewById(R.id.loginForm);

        // Check if UserResponse is Already Logged In
        if(SaveSharedPreference.getPasswordHash(getApplicationContext()).isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {
            loginForm.setVisibility(View.VISIBLE);
        }


        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userLogin(password.getText().toString());
            }
        });
    }

    /**
     * Login API call
     * TODO: Please modify according to your need it is just an example
     * @param password
     */
    private void userLogin(String password) {
        Security.addProvider(new BouncyCastleProvider());

        String hashedString = password;//SaveSharedPreference.toHex(SaveSharedPreference.blake2b256Digest(password.getBytes()));
        if (hashedString.equals(SaveSharedPreference.getPasswordHash(getApplicationContext()))) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK |FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Credentials are not Valid.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}