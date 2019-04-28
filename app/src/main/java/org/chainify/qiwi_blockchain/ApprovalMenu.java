package org.chainify.qiwi_blockchain;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.bouncycastle.jcajce.provider.symmetric.AES;
import org.bouncycastle.util.StringList;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class ApprovalMenu extends Fragment {
    Button generateKeyBtn;
    Button verifyBtn;
    Button putCypherBtn;

    TextView verified;

    EditText passwordEdit;
    EditText pk, sk;
    EditText passportEdit;

    RelativeLayout generationLayout;

    LinearLayout verificationLayout, passportLayout;

    private void updateVisibility(Context ctx) {
        verified.setVisibility(View.INVISIBLE);
        if (!SaveSharedPreference.getPasswordHash(ctx).isEmpty()) {
            generationLayout.setVisibility(View.INVISIBLE);
            verifyBtn.setClickable(true);

            putCypherBtn.setClickable(false);
        } else {
            verificationLayout.setVisibility(View.INVISIBLE);
            passportLayout.setVisibility(View.INVISIBLE);
        }

        if (SaveSharedPreference.getFinished(ctx)) {
            passportLayout.setVisibility(View.INVISIBLE);
            verified.setVisibility(View.VISIBLE);

            verifyBtn.setClickable(false);
            putCypherBtn.setClickable(false);
        } else {
            passportLayout.setVisibility(View.VISIBLE);
            if (SaveSharedPreference.getVerified(ctx)) {
                verifyBtn.setClickable(false);
                putCypherBtn.setClickable(true);
            }
        }
    }

    private void updateKeys(Context ctx) {
        if (!SaveSharedPreference.getPasswordHash(ctx).isEmpty()) {
            pk.setText(SaveSharedPreference.getEncryptedPK(ctx));
            sk.setText(SaveSharedPreference.getEncryptedSK(ctx));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        RelativeLayout thisView = (RelativeLayout) inflater.inflate(R.layout.fragment_approval_menu, container, false);
        final Context ctx = thisView.getContext();


        generationLayout = thisView.findViewById(R.id.generation_layout);

        verificationLayout = thisView.findViewById(R.id.verification_layout);
        passportLayout = thisView.findViewById(R.id.passport_layout);

        verified = thisView.findViewById(R.id.verified_text);
        generateKeyBtn = thisView.findViewById(R.id.generate_btn);

        passwordEdit = thisView.findViewById(R.id.password_ver_edit);
        passportEdit = thisView.findViewById(R.id.passport_edit);
        verifyBtn = thisView.findViewById(R.id.verify_btn);
        putCypherBtn = thisView.findViewById(R.id.put_cypher_btn);

        pk = thisView.findViewById(R.id.pk_edit);
        sk = thisView.findViewById(R.id.sk_edit);

        generateKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                generateAndSaveKeys(ctx, passwordEdit.getText().toString());

                generationLayout.setVisibility(View.INVISIBLE);
                verificationLayout.setVisibility(View.VISIBLE);

                updateKeys(ctx);
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    new RequestTask().execute();
                    Context ctx = getContext();
                    String pkc = pk.getText().toString();
                    String psc = passportEdit.getText().toString();
                    if (pkc.isEmpty() || psc.isEmpty()) {
                        Toast.makeText(getContext(),"You should provide all required data", Toast.LENGTH_SHORT).show();
                    } else {
                        AsyncTask task = new DownloadWebpageTask().execute(pk.getText().toString(), passportEdit.getText().toString());
                        String res = ((DownloadWebpageTask) task).get();
                        if (res.startsWith("error")) {
                            Toast.makeText(getContext(),"Verification failed: " + res, Toast.LENGTH_SHORT).show();
                        } else {
                            JSONObject obj = new JSONObject(res);
                            String id = "";
                            try {
                                id = obj.getString("id");
                                SaveSharedPreference.setVerified(ctx, true, id);
                                updateVisibility(ctx);
                            } catch (Exception e) {
                                Toast.makeText(getContext(),"Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(),"Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        putCypherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // encrypt sk with temp key, send encrypted sk and temp key
                    Context ctx = getContext();

                    String sk = SaveSharedPreference.getEncryptedSK(ctx);
                    // Generating IV.
                    byte[] IV = "WRNlywK6BCRpCaJI".getBytes();
                    String key = "fZhcWmVq0eFG9mZaoCvPKebJfuoCsBgo";

                    System.out.println("Original Text  : "+sk);

                    byte[] cipherText = AESEncryption.encrypt(sk.getBytes(),key.getBytes(), IV);
                    String encrypted = Base64.encodeToString(cipherText, 0);
                    System.out.println("Original Text  : "+sk);

                    System.out.println("Encrypted Text : "+ encrypted);

                    String decryptedText = AESEncryption.decrypt(cipherText,key.getBytes(), IV);
                    System.out.println("Decrypted Text : "+decryptedText);

                    AsyncTask finalizing = new PutInfo().execute(SaveSharedPreference.getID(ctx), encrypted);
                    String final_res = ((PutInfo) finalizing).get();
                    if (final_res.startsWith("error")) {
                        Toast.makeText(getContext(),"Verification failed" + final_res, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),"Verification success", Toast.LENGTH_SHORT).show();
                        SaveSharedPreference.setFinished(v.getContext(), true);
                        updateVisibility(ctx);
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(),"Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        updateVisibility(ctx);
        updateKeys(ctx);

        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return thisView;
    }

    public void generateAndSaveKeys(Context context, String password) {
        ECKeyPair pair = ECKeyPair.createNew(false);
        SaveSharedPreference.setKeys(context, pair, password);
    }



    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String)downloadUrl((String)urls[0], (String)urls[1]);
            } catch (IOException e) {
                return "error" + e.getMessage();
            }
        }

        private String downloadUrl(String publicKey, String passportData) throws IOException {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://10.20.3.54:3500/api/v1/sber");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("passport", passportData));
                nameValuePairs.add(new BasicNameValuePair("publicKey", publicKey));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(
                            final HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status <= 201) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }

                };
                String responseBody = httpclient.execute(httppost, responseHandler);
                System.out.println("----------------------------------------");
                System.out.println(responseBody);

                return responseBody;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return "error" + e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return "error" + e.getMessage();
            }
        }
    }

    private class PutInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String)downloadUrl((String)urls[0], (String)urls[1]);
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        private String downloadUrl(String id, String encrypted) throws IOException {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPut httpPut = new HttpPut("http://10.20.3.54:3500/api/v1/sber_cypher");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("id", id));
                nameValuePairs.add(new BasicNameValuePair("cypherText", encrypted));
                httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(
                            final HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status <= 201) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else if (status == 400) {
                            HttpEntity entity = response.getEntity();
                            throw new ClientProtocolException("400 status: " + EntityUtils.toString(entity));
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }

                };
                return httpclient.execute(httpPut, responseHandler);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return "error" + e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return "error" + e.getMessage();
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Approval menu");
    }
}