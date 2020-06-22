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

import org.json.JSONException;
import org.json.JSONObject;


public class GetRespondentTokenTaskLoader
        extends AsyncTaskLoader {
    private static final String RESPONDENT_TOKEN = "respondent_token";
    private static final String MASHERY_API_KEY = "mashery_api_key";
    private static final String ERROR = "error";
    private static final String USER_EXITED_SURVEY = "user_exited_survey";
    private String mResponse;
    private String mUrl;
    private SMError mError;
    private String mToken;
    private SMExceptionHandler mExceptionHandler;

    public GetRespondentTokenTaskLoader(Context context, String url, SMExceptionHandler handler) {
        super(context);
        this.mUrl = url;
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


    private String getResponse() {
        return this.mResponse;
    }


    public JSONObject loadInBackground() throws SMError {
        try {
            return retrieveRespondentToken(this.mUrl);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject retrieveRespondentToken(String inputURL) throws IOException, JSONException, SMError {
        InputStream is = null;
        try {
            URL url = new URL(inputURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            handleResponseCode(conn.getResponseCode(), conn);
            is = conn.getInputStream();
            JSONObject contentAsJSON = readIt(is);
        } catch (SocketTimeoutException e) {
            this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_BAD_CONNECTION, e);
            Log.d("SM_SDK_DEBUG", this.mError.getDescription());
            this.mExceptionHandler.handleError(this.mError);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
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
            e.printStackTrace();

            return null;
        }
    }

    private void handleResponseCode(int statusCode, HttpURLConnection connection) throws SMError {
        if (statusCode != 200) {
            switch (statusCode) {
                case 404:
                    this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_SURVEY_DELETED, null);
                    Log.d("SM_SDK_DEBUG", this.mError.getDescription());
                    connection.disconnect();
                    this.mExceptionHandler.handleError(this.mError);
                case 410:
                    this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_NONEXISTENT_LINK, null);
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
