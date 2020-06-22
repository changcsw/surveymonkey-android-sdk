package com.surveymonkey.surveymonkeyandroidsdk;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.surveymonkey.surveymonkeyandroidsdk.loaders.GetRespondentTaskLoader;
import com.surveymonkey.surveymonkeyandroidsdk.loaders.GetRespondentTokenTaskLoader;
import com.surveymonkey.surveymonkeyandroidsdk.loaders.RetrieveSPageTask;
import com.surveymonkey.surveymonkeyandroidsdk.utils.SMError;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;



public class SMFeedbackFragment
        extends Fragment
        implements SMExceptionHandler
{
    public static final String TAG = SMFeedbackFragment.class.getSimpleName();

    private static final int RESPONDENT_TOKEN_LOADER_KEY = 1;

    private static final int RESPONDENT_LOADER_KEY = 2;

    public static final String KEY_SM_SPAGE_URL = "smSPageURL";
    public static final String KEY_SM_SPAGE_HTML = "smSPageHTML";
    public static final String KEY_SM_HAS_LOADED_SPAGE_HTML = "smHasLoadedSPageHTML";
    private static final String SURVEY_STATUS = "survey_status";
    private static final String HTML = "html";
    private static final String COLLECTOR_CLOSED = "collector_closed";
    private WebView mWebView;
    private String mSPageHTML;
    private boolean mHasPreLoadedHTML;
    private boolean mHasLoadedSPageWebView;
    private String mURL;
    private SMError mError;
    private String mTokenURL;
    private String mToken;
    private String mMasheryApiKey;
    private ProgressDialog mProgressDialog;
    private ConnectivityMonitor connectivityMonitor;
    private GetRespondentTokenTaskLoader getRespondentTokenTaskLoader;
    private GetRespondentTaskLoader getRespondentTaskLoader;

    public static SMFeedbackFragment newInstance(String url, String spageHTML, boolean hasLoadedHTML) {
        SMFeedbackFragment fragment = new SMFeedbackFragment();
        Bundle bundle = new Bundle();
        bundle.putString("smSPageURL", url);
        bundle.putString("smSPageHTML", spageHTML);
        bundle.putBoolean("smHasLoadedSPageHTML", hasLoadedHTML);
        fragment.setArguments(bundle);
        return fragment;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHasPreLoadedHTML = false;
        this.mHasLoadedSPageWebView = false;
        this.mSPageHTML = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mURL = bundle.getString("smSPageURL");
            this.mHasPreLoadedHTML = bundle.getBoolean("smHasLoadedSPageHTML");
            if (this.mHasPreLoadedHTML) {
                this.mSPageHTML = bundle.getString("smSPageHTML");
                loadSurveyPage();
            } else {

                RetrieveSPageTask sPageTask = new RetrieveSPageTask()
                {
                    protected void onPostExecute(JSONObject data) {
                        try {
                            if (data != null) {
                                JSONObject sdkData = data.getJSONObject("survey_status");
                                SMFeedbackFragment.this.mSPageHTML = data.getString("html");
                                if (!sdkData.getBoolean("collector_closed")) {
                                    SMFeedbackFragment.this.loadSurveyPage();
                                }
                                else {

                                    SMFeedbackFragment.this.handleCollectorClosed();
                                }
                            } else {

                                SMFeedbackFragment.this.handleCollectorClosed();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                sPageTask.execute(new String[] { this.mURL });
            }
        }
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { return inflater.inflate(R.layout.fragment_smfeedback, container, false); }



    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.connectivityMonitor = new ConnectivityMonitor();
        if (!this.mHasLoadedSPageWebView && this.mSPageHTML != null) {
            loadSurveyPage();
        }
        if (getActivity() != null) {
            getActivity().registerReceiver(this.connectivityMonitor, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            this.mProgressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.sm_loading_status));
        }
    }


    public void onDestroy() {
        if (this.connectivityMonitor != null && getActivity() != null) {
            getActivity().unregisterReceiver(this.connectivityMonitor);
        }
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }

        if (this.getRespondentTaskLoader != null) {
            this.getRespondentTaskLoader.cancelLoad();
        }

        if (this.getRespondentTokenTaskLoader != null) {
            this.getRespondentTokenTaskLoader.cancelLoad();
        }

        super.onDestroy();
    }

    private void loadSurveyPage() {
        if (getView() != null) {
            this.mHasLoadedSPageWebView = true;
            this.mWebView = (WebView)getView().findViewById(R.id.sm_feedback_webview);
            this.mWebView.getSettings().setJavaScriptEnabled(true);
            this.mWebView.setWebViewClient(new SMWebviewClient());
            this.mWebView.loadDataWithBaseURL(this.mURL, this.mSPageHTML, null, "UTF-8", null);
        }
    }

    private void handleCollectorClosed() {
        this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_COLLECTOR_CLOSED, null);
        Log.d("SM_SDK_DEBUG", this.mError.getDescription());
        handleError(this.mError);
    }


    public GetRespondentTokenTaskLoader onCreateRespondentTokenTaskLoader(int id, Bundle args) {
        this.getRespondentTokenTaskLoader = new GetRespondentTokenTaskLoader(getActivity(), this.mTokenURL, this);
        return this.getRespondentTokenTaskLoader;
    }

    public GetRespondentTaskLoader onCreateRespondentTaskLoader(int id, Bundle args) {
        this.getRespondentTaskLoader = new GetRespondentTaskLoader(getActivity(), this.mToken, this.mMasheryApiKey, this);
        return this.getRespondentTaskLoader;
    }

    public void onGetRespondentTokenTaskLoadFinished(Loader<JSONObject> loader, JSONObject data) {
        if (data != null) {
            try {
                this.mToken = data.getString("respondent_token");
                this.mMasheryApiKey = data.getString("mashery_api_key");
                getActivity().getSupportLoaderManager().restartLoader(2, null, new LoaderManager.LoaderCallbacks<JSONObject>()
                {
                    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
                        return SMFeedbackFragment.this.onCreateRespondentTaskLoader(id, args);
                    }



                    public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) { SMFeedbackFragment.this.onGetRespondentTaskLoadFinished(loader, data); }




                    public void onLoaderReset(Loader<JSONObject> loader) {}
                });
            } catch (JSONException e) {
                this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_TOKEN, e);
                Log.d("SM_SDK_DEBUG", this.mError.getDescription());
                handleError(this.mError);
            }
        }
        this.getRespondentTokenTaskLoader = null;
    }

    public void onGetRespondentTaskLoadFinished(Loader<JSONObject> loader, JSONObject data) {
        if (data != null) {
            try {
                JSONObject result = data.getJSONObject("data");
                handleRespondent(result);
            } catch (JSONException e) {
                this.mError = SMError.sdkServerErrorFromCode(SMError.ErrorType.ERROR_CODE_RETRIEVING_RESPONSE, e);
                Log.d("SM_SDK_DEBUG", this.mError.getDescription());
                handleError(this.mError);
            }
        }
        this.getRespondentTaskLoader = null;
    }


    private void handleRespondent(JSONObject r) {
        try {
            if (getActivity() != null) {
                ((SMFeedbackFragmentListener)getActivity()).onFetchRespondentSuccess(r);
            }
        }
        catch (ClassCastException cce) {
            Log.d("SM_SDK_DEBUG", "SMFeedbackFragmentListener has not been implemented");
            showEndOfSurveyOverlay();
        }
    }

    public void handleError(SMError e) {
        try {
            if (getActivity() != null) {
                ((SMFeedbackFragmentListener)getActivity()).onFetchRespondentFailure(e);
            }
        }
        catch (ClassCastException cce) {
            Log.d("SM_SDK_DEBUG", "SMFeedbackFragmentListener has not been implemented");
            if (e.getErrorCode() == SMError.ErrorType.ERROR_CODE_COLLECTOR_CLOSED.getValue()) {
                showSurveyClosedOverlay();
            } else {

                showEndOfSurveyOverlay();
            }
        }
    }

    private void showEndOfSurveyOverlay() {
        View v = getView();
        if (v != null) {
            v.findViewById(R.id.sm_feedback_survey_ended).setVisibility(View.VISIBLE);
            v.findViewById(R.id.sm_feedback_webview).setVisibility(View.GONE);
        }
    }

    private void showSurveyClosedOverlay() {
        View v = getView();
        if (v != null) {
            v.findViewById(R.id.sm_feedback_survey_closed).setVisibility(View.VISIBLE);
            v.findViewById(R.id.sm_feedback_webview).setVisibility(View.GONE);
        }
    }

    private void getToken() {
        if (getActivity() != null) {
            getActivity().getSupportLoaderManager().restartLoader(1, null, new LoaderManager.LoaderCallbacks<JSONObject>()
            {
                public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
                    return SMFeedbackFragment.this.onCreateRespondentTokenTaskLoader(id, args);
                }



                public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) { SMFeedbackFragment.this.onGetRespondentTokenTaskLoadFinished(loader, data); }




                public void onLoaderReset(Loader<JSONObject> loader) {}
            });
        } else {
            Log.d("SM_SDK_DEBUG", "getActivity is null in SMFeedbackFragment.getToken()");
        }
    }

    private class SMWebviewClient
            extends WebViewClient {
        private SMWebviewClient() {}

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            URL resourceURL = null;
            SMFeedbackFragment.this.mProgressDialog.show();
            try {
                resourceURL = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (resourceURL != null && resourceURL.getPath() != null && resourceURL
                    .getPath().startsWith("/r/embed/sdk_token")) {
                view.stopLoading();

                view.loadUrl("about:blank");
                SMFeedbackFragment.this.mTokenURL = url;
                SMFeedbackFragment.this.getToken();
            } else {
                super.onPageStarted(view, url, favicon);
            }
        }


        public void onPageFinished(WebView view, String url) {
            if (SMFeedbackFragment.this.getActivity() != null && !SMFeedbackFragment.this.getActivity().isDestroyed()) {
                SMFeedbackFragment.this.mProgressDialog.dismiss();
            }
        }


        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!url.contains("surveymonkey.com/r/")) {
                Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                SMFeedbackFragment.this.startActivity(browserIntent);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    public static class ConnectivityMonitor
            extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent) {
            if (!isNetworkConnected(context)) {
                ((FragmentActivity)context).getSupportFragmentManager().findFragmentByTag(SMFeedbackFragment.TAG).getView().findViewById(R.id.sm_feedback_no_network).setVisibility(View.VISIBLE);
                ((FragmentActivity)context).getSupportFragmentManager().findFragmentByTag(SMFeedbackFragment.TAG).getView().findViewById(R.id.sm_feedback_webview).setVisibility(View.GONE);
            } else {
                ((FragmentActivity)context).getSupportFragmentManager().findFragmentByTag(SMFeedbackFragment.TAG).getView().findViewById(R.id.sm_feedback_no_network).setVisibility(View.GONE);
                ((FragmentActivity)context).getSupportFragmentManager().findFragmentByTag(SMFeedbackFragment.TAG).getView().findViewById(R.id.sm_feedback_webview).setVisibility(View.VISIBLE);
            }
        }

        public boolean isNetworkConnected(Context context) {
            FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
            if (fragmentManager != null) {
                Fragment f = fragmentManager.findFragmentByTag(SMFeedbackFragment.TAG);
                if (f != null) {
                    FragmentActivity fragmentActivity = f.getActivity();
                    if (fragmentActivity != null) {
                        ConnectivityManager manager = (ConnectivityManager)fragmentActivity.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (manager != null) {
                            NetworkInfo activeNetInfo = manager.getActiveNetworkInfo();
                            return (activeNetInfo != null && activeNetInfo.isConnected());
                        }
                    }
                }
            }
            return false;
        }
    }
}
