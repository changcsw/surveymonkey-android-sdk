package com.surveymonkey.surveymonkeyandroidsdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.surveymonkey.surveymonkeyandroidsdk.utils.SMError;

import org.json.JSONObject;

public class SMFeedbackActivity
        extends FragmentActivity
        implements SMFeedbackFragmentListener {
    public static final String KEY_SM_SPAGE_HTML = "smSPageHTML";
    public static final String KEY_SM_SPAGE_URL = "smSPageURL";
    public static final String KEY_SM_ERROR = "smError";
    public static final String KEY_SM_DESCRIPTION = "smDescription";
    public static final String SM_RESPONDENT = "smRespondent";
    public static final String SM_ERROR_CODE = "smErrorCode";
    private String mSPageHTML;
    private String mURL;
    private SMError mError;

    public static void startActivityForResult(Activity context, int requestCode, String url, String sPageHTML) {
        Intent intent = new Intent(context, SMFeedbackActivity.class);
        if (sPageHTML != null) {
            intent.putExtra("smSPageHTML", sPageHTML);
        }
        intent.putExtra("smSPageURL", url);
        context.startActivityForResult(intent, requestCode);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        this.mSPageHTML = intent.getStringExtra("smSPageHTML");
        this.mURL = intent.getStringExtra("smSPageURL");
        if (this.mSPageHTML == null) {
            this.mError = SMError.sdkClientErrorFromCode(SMError.ErrorType.ERROR_CODE_COLLECTOR_CLOSED, null);
            Log.d("SM_SDK_DEBUG", this.mError.getDescription());
            onFetchRespondentFailure(this.mError);
        } else if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(16908290, SMFeedbackFragment.newInstance(this.mURL, this.mSPageHTML, true), SMFeedbackFragment.TAG).commit();
        }
    }

    public void onBackPressed() {
        this.mError = SMError.sdkClientErrorFromCode(SMError.ErrorType.ERROR_CODE_USER_CANCELED, null);
        Log.d("SM_SDK_DEBUG", this.mError.getDescription());
        onFetchRespondentFailure(this.mError);
    }

    public void onFetchRespondentSuccess(JSONObject respondent) {
        Intent intent = new Intent();
        intent.putExtra("smRespondent", respondent.toString());
        setResult(-1, intent);
        finish();
    }

    public void onFetchRespondentFailure(SMError e) {
        Intent intent = new Intent();
        intent.putExtra("smError", e);
        if (e != null) {
            intent.putExtra("smDescription", e.getDescription());
            intent.putExtra("smErrorCode", e.getErrorCode());
        } else {
            intent.putExtra("smDescription", "");
            intent.putExtra("smErrorCode", -1);
        }
        setResult(0, intent);
        finish();
    }
}
