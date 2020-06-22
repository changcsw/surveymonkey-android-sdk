package com.surveymonkey.surveymonkeyandroidsdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.surveymonkey.surveymonkeyandroidsdk.loaders.RetrieveSPageTask;
import com.surveymonkey.surveymonkeyandroidsdk.utils.SMNetworkUtils;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;


public class SurveyMonkey {
    private static final String SM_BASE_URL = "https://www.surveymonkey.com/r/";
    private static final String SURVEY_STATUS = "survey_status";
    private static final String HTML = "html";
    private static final String COLLECTOR_CLOSED = "collector_closed";
    private static final long THREE_DAYS = 259200000L;
    private static final long THREE_MONTHS = 7884000000L;
    private static final long THREE_WEEKS = 1814400000L;
    private Activity mContext;
    private String mCollectorHash;
    private JSONObject mCustomVariables;
    private int mRequestCode;
    private String mSPageHTML;
    private SMFeedbackFragment mFragment;

    public void onStart(Activity activity, String appName, int requestCode, String collectorHash, JSONObject... customVariables) {
        Resources res = activity.getResources();
        onStart(activity, requestCode, collectorHash, String.format(res.getString(R.string.sm_prompt_title_text), new Object[]{appName}), res.getString(R.string.sm_prompt_message_text), 259200000L, 1814400000L, 7884000000L, customVariables);
    }


    public void onStart(final Activity activity, final int requestCode, final String collectorHash, final String alertTitleText, final String alertBodyText, long afterInstallInterval, final long afterDeclineInterval, final long afterAcceptInterval, final JSONObject... customVariables) {
        final Context appContext = activity.getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences("com.surveymonkey.surveymonkeyandroidsdk", 0);
        final long currentDate = (new Date()).getTime();
        if (isNetworkConnected(appContext)) {
            long promptDate = prefs.getLong("com.surveymonkey.surveymonkeyandroidsdk.promptdate", 0L);
            if (promptDate == 0L) {
                prefs.edit().putLong("com.surveymonkey.surveymonkeyandroidsdk.promptdate", currentDate + afterInstallInterval).commit();
            } else if (promptDate < currentDate) {
                this.mCollectorHash = collectorHash;
                RetrieveSPageTask sPageTask = new RetrieveSPageTask() {
                    protected void onPostExecute(JSONObject data) {
                        try {
                            if (data != null) {
                                JSONObject sdkData = data.getJSONObject("survey_status");
                                SurveyMonkey.this.mSPageHTML = data.getString("html");
                                if (!sdkData.getBoolean("collector_closed") && !activity.isFinishing()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    LayoutInflater inflater = activity.getLayoutInflater();
                                    View view = inflater.inflate(R.layout.fragment_dialog, null);
                                    TextView headerTextView = (TextView) view.findViewById(R.id.dialog_header_text_line);
                                    headerTextView.setText(alertTitleText);
                                    headerTextView.setVisibility(View.VISIBLE);
                                    TextView messageTextView = (TextView) view.findViewById(R.id.dialog_first_text_line);
                                    messageTextView.setText(alertBodyText);
                                    messageTextView.setVisibility(View.VISIBLE);
                                    builder.setView(view);


                                    builder.setPositiveButton(R.string.sm_action_give_feedback, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            SharedPreferences prefs = appContext.getSharedPreferences("com.surveymonkey.surveymonkeyandroidsdk", 0);
                                            prefs.edit().putLong("com.surveymonkey.surveymonkeyandroidsdk.promptdate", currentDate + afterAcceptInterval).commit();
                                            SurveyMonkey.this.startSMFeedbackActivityForResult(activity, requestCode, collectorHash, customVariables);
                                        }
                                    });
                                    builder.setNegativeButton(R.string.sm_action_not_now, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            SharedPreferences prefs = appContext.getSharedPreferences("com.surveymonkey.surveymonkeyandroidsdk", 0);
                                            prefs.edit().putLong("com.surveymonkey.surveymonkeyandroidsdk.promptdate", currentDate + afterDeclineInterval).commit();
                                        }
                                    });
                                    builder.create().show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                sPageTask.execute(new String[]{SMNetworkUtils.buildURL(this.mCollectorHash, this.mCustomVariables)});
            }
        } else {
            prefs.edit().putLong("com.surveymonkey.surveymonkeyandroidsdk.promptdate", currentDate + afterInstallInterval).commit();
        }
    }


    public void startSMFeedbackActivityForResult(Activity context, int requestCode, String collectorHash, JSONObject... customVariables) {
        this.mContext = context;
        this.mRequestCode = requestCode;
        this.mCustomVariables = (customVariables.length > 0) ? customVariables[0] : null;
        this.mCollectorHash = collectorHash;
        RetrieveSPageTask sPageTask = new RetrieveSPageTask() {
            protected void onPostExecute(JSONObject data) {
                try {
                    if (data != null) {
                        JSONObject sdkData = data.getJSONObject("survey_status");
                        SurveyMonkey.this.mSPageHTML = data.getString("html");
                        if (!sdkData.getBoolean("collector_closed")) {
                            SMFeedbackActivity.startActivityForResult(SurveyMonkey.this.mContext, SurveyMonkey.this.mRequestCode, SMNetworkUtils.buildURL(SurveyMonkey.this.mCollectorHash, SurveyMonkey.this.mCustomVariables), SurveyMonkey.this.mSPageHTML);
                        } else {

                            SMFeedbackActivity.startActivityForResult(SurveyMonkey.this.mContext, SurveyMonkey.this.mRequestCode, SMNetworkUtils.buildURL(SurveyMonkey.this.mCollectorHash, SurveyMonkey.this.mCustomVariables), null);
                        }
                    } else {

                        SMFeedbackActivity.startActivityForResult(SurveyMonkey.this.mContext, SurveyMonkey.this.mRequestCode, SMNetworkUtils.buildURL(SurveyMonkey.this.mCollectorHash, SurveyMonkey.this.mCustomVariables), null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        sPageTask.execute(new String[]{SMNetworkUtils.buildURL(this.mCollectorHash, this.mCustomVariables)});
    }


    public static SMFeedbackFragment newSMFeedbackFragmentInstance(String collectorHash, JSONObject... customVariables) {
        JSONObject customVariablesObj = (customVariables.length > 0) ? customVariables[0] : null;
        return SMFeedbackFragment.newInstance(SMNetworkUtils.buildURL(collectorHash, customVariablesObj), null, false);
    }


    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = manager.getActiveNetworkInfo();
        return (activeNetInfo != null && activeNetInfo.isConnected());
    }
}
