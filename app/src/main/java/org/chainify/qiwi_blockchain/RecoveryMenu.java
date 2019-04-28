package org.chainify.qiwi_blockchain;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RecoveryMenu extends Fragment {
    Button restoreBtn;

    TextView restoredText;

    EditText pk, sk;
    EditText passportEdit;

    LinearLayout restoredMain;
    LinearLayout restorePasswordLayout;


    boolean restored = false;

    private void updateVisibility(Context ctx) {
        if (!restored) {
            restorePasswordLayout.setVisibility(View.VISIBLE);
            restoredText.setVisibility(View.INVISIBLE);
        } else {
            restorePasswordLayout.setVisibility(View.INVISIBLE);
            restoredText.setVisibility(View.VISIBLE);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        RelativeLayout thisView = (RelativeLayout) inflater.inflate(R.layout.fragment_recovery_menu, container, false);

        final Context ctx = thisView.getContext();
        restoreBtn = thisView.findViewById(R.id.restore_btn);
        pk = thisView.findViewById(R.id.pk_edit_restored);
        sk = thisView.findViewById(R.id.sk_edit_restored);

        restoredText = thisView.findViewById(R.id.restored_text);

        passportEdit = thisView.findViewById(R.id.passport_edit_restored);

        restoredMain = thisView.findViewById(R.id.restored_layout);
        restorePasswordLayout = thisView.findViewById(R.id.passport_layout_restored);

        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    new RequestTask().execute();
                    Context ctx = getContext();
                    String psc = passportEdit.getText().toString();
                    if (psc.isEmpty()) {
                        Toast.makeText(getContext(),"You should provide all required data", Toast.LENGTH_SHORT).show();
                    } else {
                        AsyncTask task = new RecoveryMenu.DownloadWebpageTask().execute(psc);
                        String res = ((RecoveryMenu.DownloadWebpageTask) task).get();
                        if (res.startsWith("error")) {
                            Toast.makeText(getContext(),"Restore failed: " + res, Toast.LENGTH_SHORT).show();
                        } else {
                            JSONObject obj = new JSONObject(res);
                            String pkey = "";
                            String cypher = "";
                            try {
                                pkey = obj.getString("publicKey");
                                cypher = obj.getString("cypherText");



                                byte[] IV = "WRNlywK6BCRpCaJI".getBytes();
                                String key = "fZhcWmVq0eFG9mZaoCvPKebJfuoCsBgo";

                                String secret = AESEncryption.decrypt(Base64.decode(cypher, 0), key.getBytes(), IV);

                                pk.setText(pkey);
                                sk.setText(secret);

                                restored = true;
                                updateVisibility(ctx);
                            } catch (Exception e) {
                                Toast.makeText(getContext(),"Restore failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(),"Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        updateVisibility(ctx);

        return thisView;
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String)downloadUrl((String)urls[0]);
            } catch (IOException e) {
                return "error" + e.getMessage();
            }
        }

        private String downloadUrl(String passportData) throws IOException {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://10.20.3.54:3500/api/v1/sber_cypher/" + passportData);

            try {/*
                HttpParams params = new BasicHttpParams();
                params.setParameter("passport", passportData);
                httpGet.setParams(params);*/

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
                String responseBody = httpclient.execute(httpGet, responseHandler);
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


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Recovery menu");
    }
}