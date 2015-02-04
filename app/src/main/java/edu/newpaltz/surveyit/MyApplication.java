package edu.newpaltz.surveyit;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

public class MyApplication extends Application{

    // urls
    public static String postSurveyInstanceUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/postNewSurveyInstance.php";
    public static String postSurveyResponseUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/postNewSurveyResponse.php";
    public static String postSurveySightingUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/postNewSurveySighting.php";
    public static String postSurveyObjectImageUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/postNewSurveyObjectImage.php";
    public static String getSurveyObjectValuesUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/getSurveyObjectValues.php";
    public static String getSurveyListUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/getSurveys.php";
    public static String getSurveyObjectsUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/getSurveyObjects.php";
    public static String getSurveyQuestionsUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/getSurveyQuestions.php";
    public static String getSurveyInstanceUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/getSurveyInstance.php";
    public static String getSurveyObjectImagesUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/dbRequest/getSurveyObjectImages.php";
    public static String attachUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/uploadAttach.php";
    public static String imageUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/images/";

    // survey values
    public static String mSurveyId;
    public static String mSurveyName;
    public static String mSurveyDesc;
    public static String mSurveyDate;
    public static int mOpNum; // the number of object options
    public static int mQuNum; // the number of survey questions

    // survey instance values
    public static String mSurveyInstanceId;
    public static String mSurveyLoc;
    public static String mSurveyObs;
    public static String mSurveyComm;

    // sighting values
    public static String mJpg;
    public static String mLat;
    public static String mLng;
    public static String mObjectId;

    // database lists
    public static ArrayList<SurveyObject> mListObjects;
    public static ArrayList<SurveyObjectValue> mListObjectVals;
    public static ArrayList<SurveyQuestion> mListQuestions;
    public static ArrayList<Survey> mListSurveys;
    public static ArrayList<SurveySighting> mListSightings;
    public static ArrayList<SurveyResponse> mListResponses;
    public static ArrayList<SurveyInstance> mListOutings;
    public static ArrayList<SurveyObjectImage> mListObjectImages;
    public static ArrayList<NameValuePair> mSavedText;

    public static final int REQUEST_IMAGE_CAPTURE = 1111; // image capture value
    public static Context context; // to get the application context
    public static String mImagePath;

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
        MyApplication.mImagePath = Environment.getExternalStorageDirectory() + "/";
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    // return the question associated with question ID
    public static String getSurveyQuestion(String qId) {
        for (SurveyQuestion sq: mListQuestions) {
            if (sq.getId().equals(qId)) {
                return sq.getQuestion();
            }
        }
        return null;
    }

    // return the object associated with the object ID
    public static String getSurveyObject(String soId) {
        for (SurveyObject so: mListObjects) {
            if (so.getId().equals(soId)) {
                return so.getOptions().get(0);
            }
        }
        return null;
    }
}