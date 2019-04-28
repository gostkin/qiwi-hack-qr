package org.chainify.qiwi_blockchain_qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

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
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RecoveryMenu extends Fragment {
    private static final String LOG_TAG = "Barcode Scanner API";
    private static final int PHOTO_REQUEST = 10;
    private TextView scanResults;
    private BarcodeDetector detector;
    private Uri imageUri;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";

    Button restoreBtn;

    TextView restoredText;

    EditText sk;
    EditText passwordEdit;

    LinearLayout restoredMain;
    LinearLayout restorePasswordLayout;

    String passwdCopy = "";

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
        sk = thisView.findViewById(R.id.sk_edit_restored);

        restoredText = thisView.findViewById(R.id.restored_text);

        passwordEdit = thisView.findViewById(R.id.password_restored);
        restoredMain = thisView.findViewById(R.id.restored_layout);
        restorePasswordLayout = thisView.findViewById(R.id.passport_layout_restored);

        scanResults = thisView.findViewById(R.id.scan_results);
        if (savedInstanceState != null) {
            imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
            scanResults.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
        }

        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    new RequestTask().execute();
                    Context ctx = getContext();
                    String psc = passwordEdit.getText().toString();
                    if (psc.isEmpty()) {
                        Toast.makeText(getContext(),"You should provide all required data", Toast.LENGTH_SHORT).show();
                    } else {
                        passwdCopy = psc;
                        requestPermissions(new
                                String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,  Manifest.permission.CAMERA}, REQUEST_WRITE_PERMISSION);
                        /*AsyncTask task = new RecoveryMenu.DownloadWebpageTask().execute(psc);
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
                        }*/
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(),"Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        detector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        if (!detector.isOperational()) {
            scanResults.setText("Could not set up the detector!");
        }

        updateVisibility(ctx);

        return thisView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Recovery menu");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(getContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            launchMediaScanIntent();
            try {
                Bitmap bitmap = decodeBitmapUri(getContext(), imageUri);
                if (detector.isOperational() && bitmap != null) {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Barcode> barcodes = detector.detect(frame);
                    for (int index = 0; index < barcodes.size(); index++) {
                        Barcode code = barcodes.valueAt(index);
                        String link = code.displayValue;

                        scanResults.setText(scanResults.getText() + code.displayValue + "\n");
                        System.out.println("OUTP " + link);

                        AsyncTask task = new DownloadWebpageTask().execute(link, passwdCopy);
                        String res = ((DownloadWebpageTask) task).get();
                        if (res.startsWith("error")) {
                            Toast.makeText(getContext(),"Restore failed: " + res, Toast.LENGTH_SHORT).show();
                        } else {
                            sk.setText(res);
                            restored  = true;
                            updateVisibility(getContext());
                            Toast.makeText(getContext(),"Restore success", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (barcodes.size() == 0) {
                        scanResults.setText("Scan Failed: Found nothing to scan");
                    }
                } else {
                    scanResults.setText("Could not set up the detector!");
                }
            } catch (Exception e) {
                Toast.makeText(getContext(),"Restore failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, e.toString());
            }
        }
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

        private String downloadUrl(String url, String password) throws IOException {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {

                //String secret = SymmetricEncryption.encrypt(privateKey, password);

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

                JSONObject obj = new JSONObject(responseBody);
                String secretKey = "";
                try {
                    secretKey = obj.getString("secret");
                    secretKey = SymmetricEncryption.decrypt(secretKey, passwdCopy);
                    return secretKey;
                } catch (Exception e) {
                    Toast.makeText(getContext(),"Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return "error" + e.getMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "error" + e.getMessage();
            }
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        imageUri = FileProvider.getUriForFile(getContext(),
                BuildConfig.APPLICATION_ID + ".provider", photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, scanResults.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }
}