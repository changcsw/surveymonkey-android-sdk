package com.surveymonkey.surveymonkeyandroidsdk.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SMRespondent {
    private static final String RESPONDENT_ID = "respondent_id";
    private static final String COMPLETION_STATUS = "completion_status";
    private static final String DATE_START = "date_start";
    private static final String DATE_MODIFIED = "date_modified";
    private static final String RESPONSES = "responses";
    private static final String[] SM_COMPLETION_STATUSES = {"partially", "completed"};

    private static final String PAPI_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private SMCompletionStatus mCompletionStatus;
    private Calendar mDateModified;
    private Calendar mDateStarted;
    private String mRespondentID;
    private ArrayList<SMQuestionResponse> mQuestionResponses;

    public SMRespondent(JSONObject jsonObject) {
        try {
            this.mRespondentID = jsonObject.getString("respondent_id");
            this.mCompletionStatus = SMCompletionStatus.values()[Arrays.asList(SM_COMPLETION_STATUSES).indexOf(jsonObject.getString("completion_status"))];
            this.mDateStarted = gregorianFormattedDateCalendarFromString(jsonObject.getString("date_start"));
            this.mDateModified = gregorianFormattedDateCalendarFromString(jsonObject.getString("date_modified"));
            ArrayList<SMQuestionResponse> responses = new ArrayList<SMQuestionResponse>();
            JSONArray responsesArray = jsonObject.getJSONArray("responses");
            for (int i = 0; i < responsesArray.length(); i++) {
                SMQuestionResponse currentResponse = new SMQuestionResponse(responsesArray.getJSONObject(i));
                responses.add(currentResponse);
            }
            this.mQuestionResponses = responses;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Calendar gregorianFormattedDateCalendarFromString(String dateString) {
        Calendar gregorianCalendarDateString = null;
        if (dateString != null && !dateString.equals("")) {
            try {
                Date simpleDateFormat = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).parse(dateString.substring(0, dateString.length() - 2));
                gregorianCalendarDateString = new GregorianCalendar();
                gregorianCalendarDateString.setTime(simpleDateFormat);
            } catch (ParseException parseException) {
            }
        }
        return gregorianCalendarDateString;
    }

    public enum SMCompletionStatus {
        SMCompletionStatusPartiallyComplete(0),
        SMCompletionStatusComplete(1);

        public final int mValue;

        SMCompletionStatus(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public ArrayList getQuestionResponses() {
        return this.mQuestionResponses;
    }

    public SMCompletionStatus getCompletionStatus() {
        return this.mCompletionStatus;
    }

    public Calendar getDateModified() {
        return this.mDateModified;
    }

    public Calendar getDateStarted() {
        return this.mDateStarted;
    }

    public String getRespondentID() {
        return this.mRespondentID;
    }

    public String getDescription() {
        int size = 0;
        if (this.mQuestionResponses != null) {
            size = this.mQuestionResponses.size();
        }
        return "<SMRespondent: " + this + "; respondentID='" + this.mRespondentID + "'; " + size + " questionResponses>";
    }
}
