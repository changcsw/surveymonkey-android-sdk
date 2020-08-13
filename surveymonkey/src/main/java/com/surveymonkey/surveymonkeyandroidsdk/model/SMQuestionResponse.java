package com.surveymonkey.surveymonkeyandroidsdk.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SMQuestionResponse {
    public static final String PAGE_ID = "page_id";
    public static final String QUESTION_ID = "question_id";
    public static final String PAGE_INDEX = "page_index";
    public static final String QUESTION_INDEX = "question_index";
    public static final String QUESTION_VALUE = "question_value";
    public static final String ANSWERS = "answers";
    private String mPageID;
    private int mPageIndex;
    private String mQuestionID;
    private int mQuestionSurveyIndex;
    private String mQuestionValue;
    private ArrayList<SMAnswerResponse> mAnswers;

    public SMQuestionResponse(JSONObject jsonObject) {
        try {
            this.mPageID = jsonObject.getString("page_id");
            this.mQuestionID = jsonObject.getString("question_id");
            this.mPageIndex = jsonObject.getInt("page_index");
            this.mQuestionSurveyIndex = jsonObject.getInt("question_index");
            this.mQuestionValue = jsonObject.getString("question_value");
            ArrayList<SMAnswerResponse> answers = new ArrayList<SMAnswerResponse>();
            JSONArray answersArray = jsonObject.getJSONArray("answers");
            for (int i = 0; i < answersArray.length(); i++) {
                SMAnswerResponse currentAnswer = new SMAnswerResponse(answersArray.getJSONObject(i));
                answers.add(currentAnswer);
            }
            this.mAnswers = answers;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public String getPageID() {
        return this.mPageID;
    }


    public int getPageIndex() {
        return this.mPageIndex;
    }


    public String getQuestionID() {
        return this.mQuestionID;
    }


    public int getQuestionSurveyIndex() {
        return this.mQuestionSurveyIndex;
    }


    public String getQuestionValue() {
        return this.mQuestionValue;
    }


    public ArrayList<SMAnswerResponse> getAnswers() {
        return this.mAnswers;
    }


    public String getDescription() {
        int size = 0;
        if (this.mAnswers != null) {
            size = this.mAnswers.size();
        }
        return "<SMQuestionResponse: " + this + "; questionValue='" + this.mQuestionValue + "'; " + size + " answers>";
    }
}
