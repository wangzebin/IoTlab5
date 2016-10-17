package com.example.zebinwang.recorder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.util.Locale;
import java.util.ArrayList;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import android.os.Handler;
import android.os.Message;


public class MainActivity extends Activity implements OnClickListener{

    private Button sender, record;
    private TextView txtSpeechInput;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    private String request;

    public MainActivity() {

    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 200:
                    Toast.makeText(getApplicationContext(),
                            "Send Success!",
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }

        }
    };

    private void initView() {
        record = (Button) findViewById(R.id.btn2);
        sender = (Button) findViewById(R.id.btn1);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);

        record.setOnClickListener(this);
        sender.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn2:
                promptSpeechInput();
                break;
            case R.id.btn1:
                try {
                    // td.sendGet(); //send HTTP GET Request
                    sender_message(); // send HTTP POST Request
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sender_message() throws IOException {

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                //code to do the HTTP request
                try {
                    String url = "http://192.168.0.9";
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    //add reuqest header
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                    String urlParameters = URLEncoder.encode(request, "UTF-8");

                    // Send post request
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    int responseCode = con.getResponseCode();
                    System.out.println("Response Code : " + responseCode);
                    mHandler.sendEmptyMessage(responseCode);
                    wr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    request = result.get(0);
                }
                break;
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

}