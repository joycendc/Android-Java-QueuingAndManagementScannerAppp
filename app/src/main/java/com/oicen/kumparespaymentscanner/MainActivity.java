package com.oicen.kumparespaymentscanner;




import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.oicen.kumparespaymentscanner.BuildConfig.HOST;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button scanBtn;
    TextView messageText, messageFormat;
    ConstraintLayout container;
    static String MARKPAID_URL = HOST + "markPaid.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanBtn = findViewById(R.id.scanBtn);
        messageText = findViewById(R.id.textContent);
        messageFormat = findViewById(R.id.textFormat);
        container = findViewById(R.id.container);

        // adding listener to the button
        scanBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        container.setBackgroundColor(Color.parseColor("#ffffff"));
        messageText.setText("---");
        messageFormat.setText("---");
        changeTextColor(Color.BLACK);
        // we need to create the object
        // of IntentIntegrator class
        // which is the class of QR library
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a QR Code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                messageText.setText(intentResult.getContents());
                messageFormat.setText(intentResult.getFormatName());

                String content[] = intentResult.getContents().split(" ");

                markPaid(content[0]);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    void changeTextColor(int color){
        messageText.setTextColor(color);
        messageFormat.setTextColor(color);
    }

    private void markPaid(String queueId){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MARKPAID_URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject obj = new JSONObject(response);

                        if (!obj.getBoolean("error")) {
                            changeTextColor(Color.WHITE);
                            container.setBackgroundColor(Color.parseColor("#4AC948"));
                            Toast.makeText(getApplicationContext(), obj.getString("message") , Toast.LENGTH_LONG).show();
                        }else{
                            changeTextColor(Color.WHITE);
                            container.setBackgroundColor(Color.parseColor("#A3161B"));
                            Toast.makeText(getApplicationContext(), obj.getString("error"), Toast.LENGTH_LONG).show();
                        }

                        messageText.setText(obj.getString("message"));
                    }catch (Exception e){
                        Log.e("Scan", e.getMessage());
                        Toast.makeText(getApplicationContext(), e.getMessage() + " catch", Toast.LENGTH_LONG).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("Scan", volleyError.getMessage());

                    Toast.makeText(getApplicationContext(), "Please Connect to our Wifi first before using this app", Toast.LENGTH_LONG).show();
                }
            }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("queue_id", queueId);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String KEY_COOKIE = "cookie";
                String VALUE_CONTENT = "__test=5083fb2d45d3ffeb754bd17e54732692; expires=Tue, 19 Jan 2038 03:14:07 UTC; PHPSESSID=bfa95c09254ed73071abd5eb05059595";

                headers.put(KEY_COOKIE, VALUE_CONTENT);
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}