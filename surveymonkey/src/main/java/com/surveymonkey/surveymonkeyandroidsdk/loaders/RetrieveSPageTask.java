package com.surveymonkey.surveymonkeyandroidsdk.loaders;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;


public class RetrieveSPageTask extends AsyncTask<String, Void, JSONObject> {
    private static final String EMBED_DATA = "embed_data";

    protected JSONObject doInBackground(String... urlString) {
        String uri = urlString[0];
        HttpURLConnection urlConnection = null;
        JSONObject sPageJSON = new JSONObject();
        try {
            URL url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            sPageJSON = parseResponse(readIt(in));
        } catch (Exception e) {
            Log.w("Server error", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return sPageJSON;
    }

    private JSONObject parseResponse(String response) throws JSONException {
        JSONObject data = new JSONObject();

        int dataLocation = response.indexOf("id=\"embed_data");
        if (dataLocation != -1) {
            int dataLength = "id=\"embed_data".length();
            int position = dataLength + dataLocation;
            String restOfString = response.substring(position);

            int startBracePosition = restOfString.indexOf("'{");
            int endBracePosition = restOfString.indexOf("}'");
            int quoteLength = 1;
            String dataString = restOfString.substring(startBracePosition + quoteLength, endBracePosition + quoteLength);
            JSONObject surveyStatus = new JSONObject(dataString);
            data.put("survey_status", surveyStatus);
            data.put("html", response);
        } else {
            data = null;
        }
        return data;
    }

    private String readIt(InputStream stream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return new String(total);
    }
}
