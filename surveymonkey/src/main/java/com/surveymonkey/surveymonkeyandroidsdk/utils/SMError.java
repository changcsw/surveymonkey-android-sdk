package com.surveymonkey.surveymonkeyandroidsdk.utils;

import java.io.Serializable;

public class SMError extends Error implements Serializable {
    public int errorCode;
    public String description;
    public String domain;
    private Exception exception;

    public enum ErrorType {
        ERROR_CODE_TOKEN(1),
        ERROR_CODE_BAD_CONNECTION(2),
        ERROR_CODE_RESPONSE_PARSE_FAILED(3),
        ERROR_CODE_COLLECTOR_CLOSED(4),
        ERROR_CODE_RETRIEVING_RESPONSE(5),
        ERROR_CODE_SURVEY_DELETED(6),
        ERROR_CODE_RESPONSE_LIMIT_HIT(7),
        ERROR_CODE_RESPONDENT_EXITED_SURVEY(8),
        ERROR_CODE_NONEXISTENT_LINK(9),
        ERROR_CODE_INTERNAL_SERVER_ERROR(10),
        ERROR_CODE_USER_CANCELED(1);

        public final int mValue;

        ErrorType(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public static class ErrorDescription {
        public static final String ERROR_CODE_TOKEN_DESCRIPTION = "Could not retrieve your respondent. Be sure you're using an SDK Collector.";
        public static final String ERROR_CODE_BAD_CONNECTION_DESCRIPTION = "There was an error connecting to the server.";
        public static final String ERROR_CODE_RESPONSE_PARSE_FAILED_DESCRIPTION = "There was an error parsing the response from the server.";
        public static final String ERROR_CODE_COLLECTOR_CLOSED_DESCRIPTION = "The collector for this survey has been closed.";
        public static final String ERROR_CODE_RETRIEVING_RESPONSE_DESCRIPTION = "There was a problem retrieving the user's response to this survey.";
        public static final String ERROR_CODE_SURVEY_DELETED_DESCRIPTION = "This survey has been deleted.";
        public static final String ERROR_CODE_RESPONSE_LIMIT_HIT_DESCRIPTION = "Response limit exceeded for your plan. Upgrade to access more responses through the SDK.";
        public static final String ERROR_CODE_RESPONDENT_EXITED_SURVEY_DESCRIPTION = "The user canceled out of the survey.";
        public static final String ERROR_CODE_NONEXISTENT_LINK_DESCRIPTION = "Custom link no longer exists.";
        public static final String ERROR_CODE_INTERNAL_SERVER_ERROR_DESCRIPTION = "Internal server error.";
    }

    public static class Domain {
        public static final String SDK_SERVER_DOMAIN = "SurveyMonkeySDK_ServerError";
        public static final String SDK_CLIENT_DOMAIN = "SurveyMonkeySDK_ClientError";
    }

    public SMError(String domain, ErrorType errorType, Exception exception, String description) {
        this.domain = domain;
        this.errorCode = errorType.getValue();
        this.description = description;
        this.exception = exception;
    }

    public static SMError sdkClientErrorFromCode(ErrorType errorType, Exception exception) {
        String description = "";
        switch (errorType) {
            case ERROR_CODE_USER_CANCELED:
                description = "The user canceled out of the survey.";
                break;
        }
        return new SMError("SurveyMonkeySDK_ClientError", errorType, exception, description);
    }

    public static SMError sdkServerErrorFromCode(ErrorType errorType, Exception exception) {
        String description = "";
        switch (errorType) {
            case ERROR_CODE_TOKEN:
                description = "Could not retrieve your respondent. Be sure you're using an SDK Collector.";
                break;
            case ERROR_CODE_BAD_CONNECTION:
                description = "There was an error connecting to the server.";
                break;
            case ERROR_CODE_RESPONSE_PARSE_FAILED:
                description = "There was an error parsing the response from the server.";
                break;
            case ERROR_CODE_COLLECTOR_CLOSED:
                description = "The collector for this survey has been closed.";
                break;
            case ERROR_CODE_RETRIEVING_RESPONSE:
                description = "There was a problem retrieving the user's response to this survey.";
                break;
            case ERROR_CODE_SURVEY_DELETED:
                description = "This survey has been deleted.";
                break;
            case ERROR_CODE_RESPONSE_LIMIT_HIT:
                description = "Response limit exceeded for your plan. Upgrade to access more responses through the SDK.";
                break;
            case ERROR_CODE_RESPONDENT_EXITED_SURVEY:
                description = "The user canceled out of the survey.";
                break;
            case ERROR_CODE_NONEXISTENT_LINK:
                description = "Custom link no longer exists.";
                break;
            case ERROR_CODE_INTERNAL_SERVER_ERROR:
                description = "Internal server error.";
                break;
        }
        return new SMError("SurveyMonkeySDK_ServerError", errorType, exception, description);
    }

    public String getDomain() {
        return this.domain;
    }

    public String getDescription() {
        if (this.exception == null) {
            return "Domain: " + this.domain + " Code: " + this.errorCode + " SMDescription: " + this.description;
        }
        return "Domain: " + this.domain + " Code: " + this.errorCode + " Description: " + this.exception.getLocalizedMessage() + " SMDescription: " + this.description;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public Exception getException() {
        return this.exception;
    }
}
