package com.example.kiit.talkingwiki;

import android.content.Intent;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import static java.lang.Thread.sleep;

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
        Connect connect=new Connect();
        connect.execute(Query);
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
    public void respond(final String response) throws IOException {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject reader;
                    try {
                        reader = new JSONObject(response);
                        Content = reader.getString("Result");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }


    private class Connect extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String params[]) {
            String response="0";
            try {
                URL url= new URL("http://www.mediawiki.org/w/api.php?action=query&titles="+params[0]+"&prop=revisions&rvprop=content&format=json");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    response=slurp(in);
                } finally {
                    urlConnection.disconnect();
                }

            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
            }
            try{
                respond(response);
            }
            catch (Exception e){
                Log.e("Developer",e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String message) {
            content.setText(Content);
            }
        private String slurp(InputStream is) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            return stringBuilder.toString();
        }
    }
}
