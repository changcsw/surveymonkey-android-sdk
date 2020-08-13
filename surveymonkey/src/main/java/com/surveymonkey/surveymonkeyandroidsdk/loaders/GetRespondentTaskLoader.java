package com.surveymonkey.surveymonkeyandroidsdk.loaders;

import android.content.Context;
import android.util.Log;

import androidx.loader.content.AsyncTaskLoader;

import com.surveymonkey.surveymonkeyandroidsdk.SMExceptionHandler;
import com.surveymonkey.surveymonkeyandroidsdk.utils.SMError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class GetRespondentTaskLoader extends AsyncTaskLoader {
    private static final String RESPONDENT_URL = "https://api.surveymonkey.net/sdk/v1/respondents?api_key=";
    private String mToken;
    private String mMasheryApiKey;
    private JSONObject mResponse;
    private SMError mError;
    private SMExceptionHandler mExceptionHandler;

    public GetRespondentTaskLoader(Context context, String token, String masheryApiKey, SMExceptionHandler handler) {
        super(context);
        this.mToken = token;
        this.mMasheryApiKey = masheryApiKey;
        this.mExceptionHandler = handler;
    }

    protected void onStartLoading() {
        super.onStartLoading();
        if (takeContentChanged() || this.mResponse == null) {
            forceLoad();
        }
        if (getResponse() != null) {
            deliverResult(getResponse());
        }
    }

    private JSONObject getResponse() {
        return this.mResponse;
    }

    public JSONObject loadInBackground() {
        try {
            this.mResponse = makeRespondentRequest(this.mToken);
            return this.mResponse;
        } catch (IOException e) {
            this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_RETRIEVING_RESPONSE, null);
            Log.d("SM_SDK_DEBUG", this.mError.getDescription());
            this.mExceptionHandler.handleError(this.mError);
            return null;
        }
    }

    private JSONObject makeRespondentRequest(String token) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL("https://api.surveymonkey.net/sdk/v1/respondents?api_key=" + this.mMasheryApiKey);
            String tokenString = "bearer " + token;
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Authorization", tokenString);
            conn.connect();
            handleResponseCode(conn.getResponseCode(), conn);
            is = conn.getInputStream();
            JSONObject content = readIt(is);
            return content;
        } catch (SocketTimeoutException e) {
            this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_BAD_CONNECTION, e);
            Log.d("SM_SDK_DEBUG", this.mError.getDescription());
            this.mExceptionHandler.handleError(this.mError);
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private JSONObject readIt(InputStream stream) throws IOException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null) {
            responseStrBuilder.append(inputStr);
        }
        try {
            return new JSONObject(responseStrBuilder.toString());
        } catch (JSONException e) {
            this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_RESPONSE_PARSE_FAILED, e);
            Log.d("SM_SDK_DEBUG", this.mError.getDescription());
            this.mExceptionHandler.handleError(this.mError);
            return null;
        }
    }

    private void handleResponseCode(int statusCode, HttpURLConnection connection) {
        if (statusCode != 200) {
            switch (statusCode) {
                case 403:
                    this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_RESPONSE_LIMIT_HIT, null);
                    Log.d("SM_SDK_DEBUG", this.mError.getDescription());
                    connection.disconnect();
                    this.mExceptionHandler.handleError(this.mError);
                case 500:
                    this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_INTERNAL_SERVER_ERROR, null);
                    Log.d("SM_SDK_DEBUG", this.mError.getDescription());
                    connection.disconnect();
                    this.mExceptionHandler.handleError(this.mError);
                    break;
            }
            this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_INTERNAL_SERVER_ERROR, null);
            Log.d("SM_SDK_DEBUG", this.mError.getDescription());
            connection.disconnect();
            this.mExceptionHandler.handleError(this.mError);
        }
    }
}
