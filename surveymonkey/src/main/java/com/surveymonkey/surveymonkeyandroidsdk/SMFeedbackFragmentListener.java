package com.surveymonkey.surveymonkeyandroidsdk;

import com.surveymonkey.surveymonkeyandroidsdk.utils.SMError;
import org.json.JSONObject;

public interface SMFeedbackFragmentListener {
    void onFetchRespondentSuccess(JSONObject paramJSONObject);

    void onFetchRespondentFailure(SMError paramSMError);
}
