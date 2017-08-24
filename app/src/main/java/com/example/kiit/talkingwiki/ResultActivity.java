package com.example.kiit.talkingwiki;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Locale;


public class ResultActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private String Query;
    private TextToSpeech tts;
    private WebView content;
    private TextView searchText;
    private String Value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        tts = new TextToSpeech(this, this);
        Intent intent=getIntent();
        if(intent.hasExtra("query"))
        Query=intent.getExtras().getString("query");
        searchText=(TextView)findViewById(R.id.search_text);
        searchText.setText(Query);
        content=(WebView)findViewById(R.id.content);
        final ProgressDialog progDailog;
        Activity activity = this;
        progDailog = ProgressDialog.show(activity, "Loading","Please wait...", true);
        progDailog.setCancelable(false);
        content.getSettings().setJavaScriptEnabled(true);
        content.getSettings().setLoadWithOverviewMode(true);
        content.getSettings().setUseWideViewPort(true);
        final ImageButton start=(ImageButton)findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.speak(Value, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        final ImageButton stop=(ImageButton)findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.stop();
            }
        });
        start.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);
        content.setWebViewClient(new WebViewClient(){

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    progDailog.show();
                    view.loadUrl(url);
                    return true;
                }
                @Override
                public void onPageFinished(WebView view, final String url) {
                    start.setVisibility(View.VISIBLE);
                    stop.setVisibility(View.VISIBLE);
                    progDailog.dismiss();
                    content.evaluateJavascript("document.getElementsByTagName('p')[1].innerText;",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    Value =value;
                                    tts.speak(Value, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            });
                }
            });
        Query=Query.replace(" ","_");
        content.loadUrl("https://en.m.wikipedia.org/wiki/"+Query);
        }
    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
    private void speakOut() {

        String text = searchText.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
