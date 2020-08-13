package com.surveymonkey.surveymonkeyandroidsdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class SMAnswerResponse {
    public static final String TEXT_RESPONSE = "text_response";
    public static final String ROW_ID = "row_id";
    public static final String ROW_VALUE = "row_value";
    public static final String ROW_INDEX = "row_index";
    public static final String COLUMN_ID = "column_id";
    public static final String COLUMN_VALUE = "column_value";
    public static final String COLUMN_INDEX = "column_index";
    public static final String COLUMN_DROPDOWN_CHOICE_ID = "column_dropdown_choice_id";
    public static final String COLUMN_DROPDOWN_CHOICE_VALUE = "column_dropdown_choice_value";
    public static final String COLUMN_DROPDOWN_CHOICE_INDEX = "column_dropdown_choice_index";
    private String mTextResponse;
    private String mRowID;
    private int mRowIndex;
    private String mRowValue;
    private String mColumnID;
    private int mColumnIndex;
    private String mColumnValue;
    private String mColumnDropdownID;
    private int mColumnDropdownIndex;
    private String mColumnDropdownValue;

    public SMAnswerResponse(JSONObject jsonObject) {
        try {
            if (jsonObject.has("text_response")) {
                this.mTextResponse = jsonObject.getString("text_response");
            }
            if (jsonObject.has("row_id")) {
                this.mRowID = jsonObject.getString("row_id");
                this.mRowValue = jsonObject.getString("row_value");
                this.mRowIndex = jsonObject.getInt("row_index");
            }
            if (jsonObject.has("column_id")) {
                this.mColumnID = jsonObject.getString("column_id");
                this.mColumnValue = jsonObject.getString("column_value");
                this.mColumnIndex = jsonObject.getInt("column_index");
            }
            if (jsonObject.has("column_dropdown_choice_id")) {
                this.mColumnDropdownID = jsonObject.getString("column_dropdown_choice_id");
                this.mColumnDropdownValue = jsonObject.getString("column_dropdown_choice_value");
                this.mColumnDropdownIndex = jsonObject.getInt("column_dropdown_choice_index");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public String getTextResponse() {
        return this.mTextResponse;
    }


    public String getRowID() {
        return this.mRowID;
    }


    public int getRowIndex() {
        return this.mRowIndex;
    }


    public String getRowValue() {
        return this.mRowValue;
    }


    public String getColumnID() {
        return this.mColumnID;
    }


    public int getColumnIndex() {
        return this.mColumnIndex;
    }


    public String getColumnValue() {
        return this.mColumnValue;
    }


    public String getColumnDropdownID() {
        return this.mColumnDropdownID;
    }


    public int getColumnDropdownIndex() {
        return this.mColumnDropdownIndex;
    }


    public String getColumnDropdownValue() {
        return this.mColumnDropdownValue;
    }


    public String getDescription() {
        return "<SMAnswerResponse: " + this + "; rowValue='" + this.mRowValue + "'; columnValue='" + this.mColumnValue + "'; columnDropdownValue='" + this.mColumnDropdownValue + "'; textResponse='" + this.mTextResponse + " >";
    }
}
