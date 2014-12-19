/*
 A survey response.
*/

package edu.newpaltz.surveyit;

public class SurveyResponse {

    String id, ssId, siId, sqId, response;

    public SurveyResponse(String siId, String ssId, String sqId, String response) {
        this.siId = siId;
        this.ssId = ssId;
        this.sqId = sqId;
        this.response = response;
    }

    public SurveyResponse(String id, String siId, String ssId, String sqId, String response) {
        this.id = id;
        this.siId = siId;
        this.ssId = ssId;
        this.sqId = sqId;
        this.response = response;
    }

    public String getId() {        return id;    }

    public String getSsId() {        return ssId;    }

    public String getSiId() {       return siId;    }

    public String getSqId() {        return sqId;    }

    public String getResponse() {        return response;    }
}
