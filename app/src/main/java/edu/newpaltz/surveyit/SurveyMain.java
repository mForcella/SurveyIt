/**
 * This activity will display the information for the current survey.
 * It allows the user to view a list of all the current survey objects.
 * It allows the user to view a list of all the current survey sightings.
 * It allows the user to upload the current sightings to the online database.
 * It allows the user to change their current survey.
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SurveyMain extends Activity {

    Activity myActivity = this;
    ArrayList<BasicNameValuePair> surveyInstance; // for posting a new survey instance
    ArrayList<BasicNameValuePair> surveySighting; // for posting a new survey sighting
    ArrayList<BasicNameValuePair> surveyResponse; // for posting a new survey response
    String whereClauseSighting = ""; // where clause for deleting rows from survey sighting database
    String whereClauseResponse = ""; // where clause for deleting rows from survey response database

    /**
     * Method called when activity is first launched.
     * Displays current survey values. Initializes buttons.
     *
     * @param     savedInstanceState      value stored from onSaveInstanceState(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_main);

        // get survey values to display
        TextView survName = (TextView) findViewById(R.id.survNameText);
        survName.setText("Survey Name: " + MyApplication.mSurveyName);
        TextView survLoc = (TextView) findViewById(R.id.survLocText);
        survLoc.setText("Observer: " + MyApplication.mSurveyObs);
        TextView survObs = (TextView) findViewById(R.id.survObsText);
        survObs.setText("Location: " + MyApplication.mSurveyLoc);
        TextView survComm = (TextView) findViewById(R.id.survCommText);
        survComm.setText("Comments: " + MyApplication.mSurveyComm);
        TextView survDate = (TextView) findViewById(R.id.survDateText);
        survDate.setText("Date: " + MyApplication.mSurveyDate);

        // objects button: view list of survey objects
        final Button surveyListButton = (Button) findViewById(R.id.surveyObjBtn);
        surveyListButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent nI = new Intent(myActivity, ViewObjects.class);
                nI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nI);
            }
        });

        // sightings button: view sightings not yet uploaded
        final Button sightingsButton = (Button) findViewById(R.id.sightingBtn);
        sightingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent yI = new Intent(myActivity, ViewSightings.class);
                yI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(yI);
            }
        });

        // record button: record a new sighting
        final Button recordButton = (Button) findViewById(R.id.recSightingBtn);
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent yI = new Intent(myActivity, RecordSighting.class);
                yI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(yI);
            }
        });

        // upload button: upload sightings to online database; remove from sqlite database
        final Button uploadButton = (Button)findViewById(R.id.uploadBtn);
        uploadButton.setEnabled(true);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isOnline()) {
                    Toast toast = Toast.makeText(myActivity, "Uploading Sighting", Toast.LENGTH_SHORT);
                    toast.show();

                    // check if the current survey instance has been uploaded and add to mysql database if not
                    new CheckSurveyInstance().execute();

                    // get values for each sighting entry and post to database
                    new PostSighting().execute();
                } else {
                    Toast toast = Toast.makeText(myActivity, "No internet connection available...", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        });
        uploadButton.setSelected(true);

        // survey button: select a different survey to work on
        final Button surveyButton = (Button) findViewById(R.id.surveyBtn);
        surveyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // clear current survey values
                MyApplication.mListObjects = null;
                MyApplication.mListQuestions = null;
                MyApplication.mListObjectVals = null;
                // return to survey select screen
                Intent yI = new Intent(myActivity, MySurvey.class);
                yI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(yI);
            }
        });
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

    /**
     * Method to upload an image attachment to the server.
     *
     * @param     sourceFileUri   the source file path
     * @return                    the http response code
     */
    public int uploadFile(String sourceFileUri) {
        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        int serverResponseCode = 0;
        if (!sourceFile.isFile()) {
            return 0;
        } else {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(MyApplication.attachUrl);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + "*****");
                conn.setRequestProperty("uploaded_file", sourceFileUri);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes("--" + "*****" + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + sourceFileUri + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                // send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes("--" + "*****" + "--" + lineEnd);
                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // delete image locally
            File image = new File(MyApplication.mImagePath + MyApplication.mJpg);
            if (image.exists())
                if (image.delete())
                    Log.i("deleteFile", "Image removed locally : " + MyApplication.mJpg);
            return serverResponseCode;
        }
    }

    /**
     * Background task to post a new sighting to the online database.
     * Posts the sighting to the 'surveySighting' table and posts all
     * associated responses to the 'surveyResponse' table.
     */
    private class PostSighting extends AsyncTask<String, String, String> {

        /**
         * This method performs a POST http operation.
         * It will post a new survey instance to the online database.
         *
         * @param     args    dummy variable, not used
         * @return            dummy return value
         */
        protected String doInBackground(String... args) {
            // get current list of sightings
            try {
                DBAdapter db = new DBAdapter(myActivity);
                MyApplication.mListSightings = db.getSurveySightings(MyApplication.mSurveyInstanceId);
                MyApplication.mListResponses = db.getSurveyResponses(MyApplication.mSurveyInstanceId);
                db.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // iterate through sighting list and upload each sighting
            for (SurveySighting sighting: MyApplication.mListSightings) {
                MyApplication.mJpg = sighting.getJpg(); // get image name
                // get sighting values
                surveySighting = new ArrayList<BasicNameValuePair>();
                surveySighting.add(new BasicNameValuePair("id", sighting.getId()));
                surveySighting.add(new BasicNameValuePair("siid", sighting.getSiId()));
                surveySighting.add(new BasicNameValuePair("oid", sighting.getoId()));
                surveySighting.add(new BasicNameValuePair("jpg", sighting.getJpg()));
                surveySighting.add(new BasicNameValuePair("lat", sighting.getLat()));
                surveySighting.add(new BasicNameValuePair("lng", sighting.getLng()));
                surveySighting.add(new BasicNameValuePair("date", sighting.getDate()));
                DefaultHttpClient httpClient = new DefaultHttpClient();
                // post to http
                HttpPost httpPost = new HttpPost(MyApplication.postSurveySightingUrl);
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(surveySighting));
                    httpClient.execute(httpPost);
                    // add ID to delete list
                    whereClauseSighting += sighting.getId() + ",";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // upload image attachment, delete locally
                if (MyApplication.mJpg != null) {
                    new Thread(new Runnable() {
                        public void run() {
                            uploadFile(MyApplication.mImagePath + MyApplication.mJpg);
                        }
                    }).start();
                }
            }
            // iterate through response list and upload each response
            for (SurveyResponse response: MyApplication.mListResponses) {
                surveyResponse = new ArrayList<BasicNameValuePair>();
                // get sighting values
                surveyResponse.add(new BasicNameValuePair("id", response.getId()));
                surveyResponse.add(new BasicNameValuePair("siid", response.getSiId()));
                surveyResponse.add(new BasicNameValuePair("ssid", response.getSsId()));
                surveyResponse.add(new BasicNameValuePair("sqid", response.getSqId()));
                surveyResponse.add(new BasicNameValuePair("response", response.getResponse()));
                DefaultHttpClient httpClient = new DefaultHttpClient();
                // post to http
                HttpPost httpPost = new HttpPost(MyApplication.postSurveyResponseUrl);
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(surveyResponse));
                    httpClient.execute(httpPost);
                    // add ID to delete list
                    whereClauseResponse += response.getId() + ",";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * This method deletes entries from the survey sighting and survey response databases.
         *
         * @param     response      dummy variable, not used
         */
        protected void onPostExecute(String response) {
            // delete sightings from sqlite database
            if (whereClauseSighting.length() > 0) { // make sure there are values to delete
                whereClauseSighting = whereClauseSighting.substring(0, whereClauseSighting.length()-1); // remove last comma
                try {
                    DBAdapter db = new DBAdapter(myActivity);
                    db.deleteSurveySightings(whereClauseSighting);
                    db.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // delete responses from sqlite database
            if (whereClauseResponse.length() > 0) { // make sure there are values to delete
                whereClauseResponse = whereClauseResponse.substring(0, whereClauseResponse.length()-1); // remove last comma
                try {
                    DBAdapter db = new DBAdapter(myActivity);
                    db.deleteSurveyResponses(whereClauseResponse);
                    db.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Background task to post a new survey instance to online database.
     */
    private class PostSurveyInstance extends AsyncTask<String,String,String> {

        /**
         * This method performs a POST http operation.
         * It will post a new survey instance to the online database.
         *
         * @param     args    dummy variable, not used
         * @return            dummy return value
         */
        protected String doInBackground(String... args) {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(MyApplication.postSurveyInstanceUrl);
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(surveyInstance));
                httpClient.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Background task to check if survey instance has been uploaded to database.
     */
    private class CheckSurveyInstance extends AsyncTask<String, String, String> {

        Boolean surveyInstanceUploaded = true;

        /**
         * This method performs a GET http operation.
         * It will check if the query returns any results.
         *
         * @param     args    dummy variable, not used
         * @return            dummy return value
         */
        protected String doInBackground(String... args) {
            // parse return value from http get request
            JSONParser jsonParser = new JSONParser();
            // add survey id as parameter to get request
            List<NameValuePair> siid = new ArrayList<NameValuePair>();
            siid.add(new BasicNameValuePair("siid", MyApplication.mSurveyInstanceId));
            JSONObject json = jsonParser.makeHttpRequest(MyApplication.getSurveyInstanceUrl, siid);
            if (json == null) {
                surveyInstanceUploaded = false;
            }
            return null;
        }

        /**
         * This method checks the online database and adds the survey instance
         * to the database if not present.
         *
         * @param     result      dummy variable, not used
         */
        protected void onPostExecute(String result) {
            if (!surveyInstanceUploaded) {
                // post survey instance to mysql database
                surveyInstance = new ArrayList<BasicNameValuePair>();
                surveyInstance.add(new BasicNameValuePair("id", MyApplication.mSurveyInstanceId));
                surveyInstance.add(new BasicNameValuePair("sid", MyApplication.mSurveyId));
                surveyInstance.add(new BasicNameValuePair("location", MyApplication.mSurveyLoc));
                surveyInstance.add(new BasicNameValuePair("observer", MyApplication.mSurveyObs));
                surveyInstance.add(new BasicNameValuePair("comment", MyApplication.mSurveyComm));
                surveyInstance.add(new BasicNameValuePair("date", MyApplication.mSurveyDate));
                new PostSurveyInstance().execute();
            }
        }
    }
}