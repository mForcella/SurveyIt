/**
 * Allows the user to select a survey to work on.
 * All surveys must be created through the web portal at the following url,
 *    http://cs.newpaltz.edu/~forcel96/surveyit/
 * Surveys will be stored in the 'survey' table.
 * The 'survey' table contains the following columns:
 *    id:      Survey ID number (integer)
 *    name:    Survey name (text)
 *    descrip: Survey description (text)
 *    opNum:   The number of object value entries associated with the survey (integer)
 *    date:    Date the survey was created (date)
 * Each survey has a set of entries in the 'surveyObject' table,
 * which specifies the objects that are being surveyed.
 * The 'surveyObject' table contains the following columns:
 *    id:         Object ID number (integer)
 *    sid:        Survey ID (integer)
 *    jpg:        Image path, optional (text)
 *    option1:    Object row values (text)
 *    ...         ...
 *    option10:   Object row values (text)
 * The optional 'option' columns hold the values for each created object.
 * Each survey has a set of entries in the 'surveyObjectValue' table.
 * These entries specify the values of the object option columns.
 * The 'surveyObjectValue' table contains the following columns:
 *    id:      Survey Object Value ID number (integer)
 *    sid:     Survey ID number (integer)
 *    descrip: Description of the object option field (text)
 *    opNum:   Which object option (1-10) is being described (integer)
 * Each survey has a set of questions in the 'surveyQuestion' table.
 * These entries specify what questions are asked when recording an object sighting.
 * The 'surveyQuestion' table contains the following columns:
 *   id:       Survey Question ID (integer)
 *   sid:      Survey ID (integer)
 *   question: Survey question being asked (text)
 *   resType:  Response type ('text','single','multi','yesNo')
 *   resVal:   Response values, for 'single' and 'multi' only (text; values separated by '|')
 */

package edu.newpaltz.surveyit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MySurvey extends Activity {

    Activity myActivity = this;
    ArrayList<String> surveyList = new ArrayList<String>(); // string list to populate dropdown

    /**
     * Method called when activity is first launched.
     * It retrieves the values from the 'survey' table and uses them to populate
     * the dropdown menu. When the user selects a survey to work on it will
     * launch an activity that creates a new instance of that survey.
     *
     * @param     savedInstanceState      value stored from onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_survey);
        surveyList.add("Choose your survey!"); // dropdown prompt
        if (isOnline()) {
            // online mode
            // get survey list from online database
            new GetSurveyList().execute();
            Toast toast = Toast.makeText(this, "Fetching surveys from online database...", Toast.LENGTH_LONG);
            toast.show();
        } else {
            // offline mode
            Toast toast = Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT);
            toast.show();
            toast = Toast.makeText(this, "Accessing stored survey data...", Toast.LENGTH_LONG);
            toast.show();
            // get surveys from sqlite database
            try {
                DBAdapter db = new DBAdapter(this);
                MyApplication.mListSurveys = db.getSurveys();
                db.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            // build survey list for dropdown
            for (Survey s: MyApplication.mListSurveys) {
                surveyList.add(s.getId() + ":" + s.getName());
            }
        }
        // add survey list to dropdown
        Spinner spSurveys = (Spinner) findViewById(R.id.surveySelect);
        ArrayAdapter<String> surveyAdapter = new ArrayAdapter<String>(
                myActivity, android.R.layout.simple_spinner_dropdown_item, surveyList);
        surveyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSurveys.setAdapter(surveyAdapter);
        spSurveys.setPromptId(R.string.choose_survey);
        spSurveys.setOnItemSelectedListener(new MyOnItemSelectedListener());
    }

    /**
     * The listener class for the survey dropdown menu. When a survey has been selected,
     * queries the database to get the associated entries from the
     * 'surveyObject', 'surveyObjectValue' and 'surveyQuestion' tables.
     */
    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        /**
         * This method will get the values for the selected survey from the dropdown menu.
         * It will query the 'surveyObject', 'surveyObjectValue' and 'surveyQuestion' tables.
         *
         * @param     parent      the adapter view where the selection occurred
         * @param     view        the dropdown object that was clicked
         * @param     pos         the position of the dropdown that was selected
         * @param     id          the row id of the item selected
         */
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (pos > 0) { // pos 0 = "Choose your survey!"
                // get values from selected database
                String surveyID = parent.getItemAtPosition(pos).toString().split(":")[0];
                for (Survey survey: MyApplication.mListSurveys) {
                    // find survey object that matches survey id from dropdown
                    if (survey.getId().equals(surveyID)) {
                        MyApplication.mSurveyId = survey.getId();
                        MyApplication.mSurveyName = survey.getName();
                        MyApplication.mSurveyDesc = survey.getDesc();
                        MyApplication.mSurveyDate = survey.getDate();
                        MyApplication.mOpNum = survey.getOpNum();
                    }
                }
                if (isOnline()) {
                    // online mode
                    // get survey objects and questions for selected survey
                    new GetSurveyObjectValues().execute();
                    new GetSurveyObjects().execute();
                    new GetSurveyQuestions().execute();
                    // get list of object images
                    new GetSurveyObjectImages().execute();
                    // download object images
                    new DownloadImageTask().execute();
                    Toast toast = Toast.makeText(myActivity, "Downloading object images...", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    // offline mode
                    // get survey objects and questions for selected survey
                    try {
                        DBAdapter db = new DBAdapter(myActivity);
                        MyApplication.mListObjects = db.getSurveyObjects(MyApplication.mSurveyId);
                        MyApplication.mListObjectVals = db.getSurveyObjectValues(MyApplication.mSurveyId);
                        MyApplication.mListQuestions = db.getSurveyQuestions(MyApplication.mSurveyId);
                        MyApplication.mQuNum = MyApplication.mListQuestions.size();
                        // get survey object images
                        // get list of object IDs
                        String whereClause = "";
                        for (SurveyObject so: MyApplication.mListObjects) {
                            whereClause += so.getId()+",";
                        }
                        if (whereClause.length() > 1) {
                            whereClause = whereClause.substring(0, whereClause.length() - 1); // remove last comma
                        }
                        MyApplication.mListObjectImages = db.getSurveyObjectImages(whereClause);
                        db.close();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // go to main screen with selected survey values
                Intent mI = new Intent(myActivity, CreateSurveyInstance.class);
                mI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mI);
            }
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }

    /**
     * Background task to get a list of surveys.
     */
    private class GetSurveyList extends AsyncTask<String,String,String> {

        /**
         * This method performs a GET http operation.
         * The values from the 'survey' table are returned and parsed.
         * A separate survey object is created for each survey in the database and
         * used to populate the dropdown menu.
         *
         * @param     args    dummy variable, not used
         * @return            dummy return value
         */
        protected String doInBackground(String ... args) {
            // parse return value from http get request
            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.makeHttpRequest(MyApplication.getSurveyListUrl, null);
            MyApplication.mListSurveys = new ArrayList<Survey>();
            try {
                // get array of surveys
                JSONArray surveys = json.getJSONArray("surveys");
                for (int i = 0; i < surveys.length(); i++){
                    JSONObject s = surveys.getJSONObject(i);
                    // creating a new survey object
                    Survey survey = new Survey(s.getString("id"), s.getString("name"),
                            s.getString("descrip"), s.getInt("opNum"), s.getString("date"));
                    MyApplication.mListSurveys.add(survey); // add survey object to array
                    // add string values to array for dropdown menu
                    surveyList.add(survey.getId() + ":" + survey.getName());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * This method checks the sqlite database and adds the selected survey
         * and associated values to the sqlite database if not present.
         *
         * @param     result      dummy variable, not used
         */
        protected void onPostExecute(String result) {
            // check if surveys need to be added to sqlite database
            try {
                DBAdapter db = new DBAdapter(myActivity);
                for (Survey s: MyApplication.mListSurveys) {
                    // if survey not in database
                    if (!db.checkSurvey(s.getId())) {
                        // add to database
                        db.addSurvey(s);
                    }
                }
                db.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Background task to get surveys object values.
     */
    private class GetSurveyObjectValues extends AsyncTask<String,String,String> {

        /**
         * This method performs a GET http operation. Queries the database to find
         * all 'surveyObjectValue' entries with matching survey ID entries.
         *
         * @param     args    dummy variable, not used
         * @return            dummy return value
         */
        protected String doInBackground(String ... args) {
            // parse return value from http get request
            JSONParser jsonParser = new JSONParser();
            // add survey id as parameter to get request
            List<NameValuePair> sid = new ArrayList<NameValuePair>();
            sid.add(new BasicNameValuePair("sid", MyApplication.mSurveyId));
            JSONObject json = jsonParser.makeHttpRequest(MyApplication.getSurveyObjectValuesUrl, sid);
            MyApplication.mListObjectVals = new ArrayList<SurveyObjectValue>();
            try {
                // get array of values
                JSONArray values = json.getJSONArray("values");
                for (int i = 0; i < values.length(); i++){
                    JSONObject sov = values.getJSONObject(i);
                    // creating a new surveyObjectValues object
                    SurveyObjectValue surveyOV = new SurveyObjectValue(
                            sov.getString("id"), sov.getString("sid"), sov.getString("opNum"),
                            sov.getString("descrip"));
                    MyApplication.mListObjectVals.add(surveyOV); // add survey object value to array
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * This method checks the sqlite database and adds the survey object values
         * to the sqlite database if not present.
         *
         * @param     result      dummy variable, not used
         */
        protected void onPostExecute(String result) {
            // check if surveyObjectValues need to be added to sqlite database
            try {
                DBAdapter db = new DBAdapter(myActivity);
                for (SurveyObjectValue sov: MyApplication.mListObjectVals) {
                    // if surveyObjectValue not in database
                    if (!db.checkSurveyObjectValue(sov.getId())) {
                        // add to database
                        db.addSurveyObjectValue(sov);
                    }
                }
                db.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Background task to get surveys objects.
     */
    private class GetSurveyObjects extends AsyncTask<String,String,String> {

        /**
         * This method performs a GET http operation. Queries the database to find all
         * 'surveyObject' entries with matching survey ID entries.
         *
         * @param     args    dummy variable, not used
         * @return            dummy return value
         */
        protected String doInBackground(String ... args) {
            if (MyApplication.mListObjects == null) {
                MyApplication.mListObjects = new ArrayList<SurveyObject>();
                // parse return value from http get request
                JSONParser jsonParser = new JSONParser();
                // add survey id as parameter to get request
                List<NameValuePair> sid = new ArrayList<NameValuePair>();
                sid.add(new BasicNameValuePair("sid", MyApplication.mSurveyId));
                JSONObject json = jsonParser.makeHttpRequest(MyApplication.getSurveyObjectsUrl, sid);
                MyApplication.mListObjects = new ArrayList<SurveyObject>();
                try {
                    // create array of survey objects
                    JSONArray objects = json.getJSONArray("objects");
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject so = objects.getJSONObject(i);
                        // get option values for survey object
                        ArrayList<String> options = new ArrayList<String>();
                        for (int j = 1; j <= MyApplication.mOpNum; j++) {
                            options.add(so.getString("option" + j));
                        }
                        // creating a new survey object
                        SurveyObject object = new SurveyObject(
                                so.getString("id"), so.getString("sid"), so.getString("jpg"), options);
                        MyApplication.mListObjects.add(object); // add survey object to array
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * This method checks the sqlite database and adds the survey objects
         * to the sqlite database if not present.
         *
         * @param     result      dummy variable, not used
         */
        protected void onPostExecute(String result) {
            // check if surveyObjects need to be added to sqlite database
            try {
                DBAdapter db = new DBAdapter(myActivity);
                for (SurveyObject so: MyApplication.mListObjects) {
                    // if surveyObject not in database
                    if (!db.checkSurveyObject(so.getId())) {
                        // add to database
                        db.addSurveyObject(so, MyApplication.mOpNum);
                    }
                }
                db.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Background task to get surveys questions.
     */
    private class GetSurveyQuestions extends AsyncTask<String,String,String> {

        /**
         * This method performs a GET http operation. Queries the database to find all
         * 'surveyQuestion' entries with matching survey ID entries.
         *
         * @param     args    dummy variable, not used
         * @return            dummy return value
         */
        protected String doInBackground(String... args) {
            if (MyApplication.mListQuestions == null) {
                MyApplication.mListQuestions = new ArrayList<SurveyQuestion>();
                // parse return value from http get request
                JSONParser jsonParser = new JSONParser();
                // add survey id as parameter to get request
                List<NameValuePair> sid = new ArrayList<NameValuePair>();
                sid.add(new BasicNameValuePair("sid", MyApplication.mSurveyId));
                JSONObject json = jsonParser.makeHttpRequest(MyApplication.getSurveyQuestionsUrl, sid);
                MyApplication.mQuNum = 0;
                try {
                    // get array of questions
                    JSONArray questions = json.getJSONArray("questions");
                    for (int i = 0; i < questions.length(); i++) {
                        JSONObject sq = questions.getJSONObject(i);
                        // creating a new surveyQuestion object
                        final SurveyQuestion surveyQ = new SurveyQuestion(
                                sq.getString("id"), sq.getString("sid"), sq.getString("question"),
                                sq.getString("resType"), sq.getString("resVal"));
                        MyApplication.mListQuestions.add(surveyQ); // add survey question to array
                        MyApplication.mQuNum++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * This method checks the sqlite database and adds the survey questions
         * to the sqlite database if not present.
         *
         * @param     result      dummy variable, not used
         */
        protected void onPostExecute(String result) {
            // check if surveyQuestions need to be added to sqlite database
            try {
                DBAdapter db = new DBAdapter(myActivity);
                for (SurveyQuestion sq: MyApplication.mListQuestions) {
                    // if surveyQuestion not in database
                    if (!db.checkSurveyQuestion(sq.getId())) {
                        // add to database
                        db.addSurveyQuestion(sq);
                    }
                }
                db.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Background task to get surveys questions.
     */
    private class GetSurveyObjectImages extends AsyncTask<String,String,String> {

        /**
         * This method performs a GET http operation. Queries the database to find all
         * 'surveyObjectImage' entries for each object ID.
         *
         * @param     args    dummy variable, not used
         * @return            dummy return value
         */
        protected String doInBackground(String... args) {
            if (MyApplication.mListObjectImages == null) {
                MyApplication.mListObjectImages = new ArrayList<SurveyObjectImage>();
                // parse return value from http get request
                JSONParser jsonParser = new JSONParser();
                // build where clause from object IDs
                String whereClause = "";
                for (SurveyObject so: MyApplication.mListObjects) {
                    whereClause += so.getId()+",";
                }
                if (whereClause.length() > 1) {
                    whereClause = whereClause.substring(0, whereClause.length() - 1); // remove last comma
                }
                List<NameValuePair> wc = new ArrayList<NameValuePair>();
                wc.add(new BasicNameValuePair("where", whereClause));
                JSONObject json = jsonParser.makeHttpRequest(MyApplication.getSurveyObjectImagesUrl, wc);
                try {
                    // get array of images
                    JSONArray images = json.getJSONArray("images");
                    for (int i = 0; i < images.length(); i++) {
                        JSONObject soi = images.getJSONObject(i);
                        // creating a new surveyQuestion object
                        final SurveyObjectImage image = new SurveyObjectImage(
                                soi.getString("id"), soi.getString("oId"), soi.getString("jpg"),
                                soi.getString("ssId"), soi.getString("date"), soi.getString("rank"),
                                soi.getString("flag"));
                        MyApplication.mListObjectImages.add(image); // add object image to array
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * This method checks the sqlite database and adds the survey image
         * to the sqlite database if not present.
         *
         * @param     result      dummy variable, not used
         */
        protected void onPostExecute(String result) {
            // check if surveyQuestions need to be added to sqlite database
            try {
                DBAdapter db = new DBAdapter(myActivity);
                for (SurveyObjectImage soi: MyApplication.mListObjectImages) {
                    // if survey image not in database
                    if (!db.checkSurveyObjectImage(soi.getId())) {
                        // add to database
                        db.addSurveyObjectImage(soi);
                    }
                }
                db.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Background task to download the object images and store them locally.
     */
    private class DownloadImageTask extends AsyncTask<String, String, String> {

        protected String doInBackground(String... urls) {
            // get image url for each object image
            for (SurveyObjectImage soi: MyApplication.mListObjectImages) {
                String url = soi.getJpg();
                String urlInput = MyApplication.imageUrl+url;
                DownloaderThread downloaderThread = new DownloaderThread(urlInput);
                downloaderThread.start();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            Toast toast = Toast.makeText(myActivity, "Image download complete!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Check if the device is online.
     * @return true or false
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}