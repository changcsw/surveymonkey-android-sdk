package com.surveymonkey.surveymonkeyandroidsdk.utils;

import android.net.Uri;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;



public class SMNetworkUtils
{
    private static final String SM_BASE_URL = "https://www.surveymonkey.com/r/";

    public static String buildURL(String collectorHash, JSONObject customVariables) {
        String url = "https://www.surveymonkey.com/r/" + collectorHash;
        if (customVariables != null) {
            url = url + JSONToQueryString(customVariables);
        }
        return url;
    }

    public static String JSONToQueryString(JSONObject jsonObject) {
        String queryString = "?";
        ArrayList<String> params = new ArrayList<String>();
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            try {
                String key = (String)keys.next();
                if (jsonObject.get(key) instanceof String) {
                    params.add(String.format("%1$s=%2$s", new Object[] { key, Uri.encode(jsonObject.getString(key)) }));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return queryString + TextUtils.join("&", params);
    }
}
