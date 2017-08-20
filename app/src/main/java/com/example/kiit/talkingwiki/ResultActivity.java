package com.example.kiit.talkingwiki;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private String Query;
    private String Content;
    private TextToSpeech tts;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        tts = new TextToSpeech(this, this);
        Intent intent=getIntent();
        if(intent.hasExtra("query"))
        Query=intent.getExtras().getString("query");
        TextView searchText=(TextView)findViewById(R.id.search_text);
        searchText.setText(Query);
        content=(TextView)findViewById(R.id.content);
        try {
            URL url= new URL("http://www.mediawiki.org/w/api.php?action=query&titles="+Query+"&prop=revisions&rvprop=content&format=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try
            {
                Content=slurp(conn.getInputStream());
            }
            finally
            {
                conn.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
        content.setText(Content);
    }
    public String slurp(InputStream is) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        bufferedReader.close();
        return stringBuilder.toString();
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

        String text = content.getText().toString();
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
