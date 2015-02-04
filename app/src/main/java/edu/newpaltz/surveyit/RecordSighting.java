/**
 * This activity allows the user to record a new sighting.
 * The survey sightings are stored in the 'surveySighting' table.
 * The 'surveySighting' table contains the following columns:
 *   id:
 *   siId:
 *   oId:
 *   jpg:
 *   lat:
 *   lng:
 *   date:
 * Each sighting will have an associated set of responses,
 * one for each survey question.
 * The survey responses are stored in the 'surveyResponse' table.
 * The 'surveyResponse' table contains the following columns:
 *   id:          The response ID (integer)
 *   siId:        The survey instance ID (integer)
 *   ssId:        The survey sighting ID (integer)
 *   sqId:        The survey question being answered (integer)
 *   response:    The response to the question (text)
 */

package edu.newpaltz.surveyit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecordSighting extends Activity implements LocationListener {

    Activity myActivity = this;
    ArrayList<NameValuePair> quResp = new ArrayList<NameValuePair>(); // questions and responses
    Spinner spObjects; // dropdown of objects
    SharedPreferences savedVals; // for storing responses

    /**
     * Method called when activity is first launched.
     * Creates dynamic layout.
     *
     * @param     savedInstanceState      value stored from onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView sv = new ScrollView(myActivity);
        LinearLayout ll = new LinearLayout(myActivity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(15,15,15,15);

        // configure width and height
        LinearLayout.LayoutParams llLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llLP);

        // get list of survey objects
        TextView objects = new TextView(myActivity);
        objects.setText("Survey Objects:");
        ll.addView(objects);

        // add object details to string list
        ArrayList<String> objectList = new ArrayList<String>(); // array for object dropdown values
        objectList.add("Select an object!"); // dropdown prompt
        for (SurveyObject so: MyApplication.mListObjects) {
            objectList.add(so.getId() + " : " + so.getOptions().get(0));
        }

        // create dropdown of survey objects
        spObjects = new Spinner(myActivity);
        ArrayAdapter<String> objectAdapter = new ArrayAdapter<String>(
                myActivity, android.R.layout.simple_spinner_dropdown_item, objectList);
        objectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spObjects.setAdapter(objectAdapter);
        spObjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected (AdapterView < ? > parent, View view,int pos, long id){
                if (pos > 0) { // pos 0 = "Select an object!"
                    MyApplication.mObjectId = parent.getItemAtPosition(pos).toString().split(":")[0];
                }
            }
            public void onNothingSelected (AdapterView parent){ }
        });

        // check for passed object ID
        if (MyApplication.mObjectId != null) {
            spObjects.setSelection(getIndex(spObjects, MyApplication.mObjectId + " : " +
                    MyApplication.getSurveyObject(MyApplication.mObjectId)));
        }
        ll.addView(spObjects);

        // get survey questions and create dynamic layout for each question
        for (int i = 0; i < MyApplication.mListQuestions.size(); i++) {
            SurveyQuestion sq = MyApplication.mListQuestions.get(i);
            if (addLayout(sq) != null)
                ll.addView(addLayout(sq));
        }

        // photo button
        RelativeLayout navBtn = new RelativeLayout(myActivity);
        RelativeLayout.LayoutParams llBtn = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        navBtn.setLayoutParams(llBtn);
        Button photoBtn = new Button(myActivity);
        RelativeLayout.LayoutParams llPhoto = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llPhoto.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        photoBtn.setText("Take Photo");
        photoBtn.setLayoutParams(llPhoto);
        photoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // save responses
                getResponses();
                // start camera activity
                Intent cI = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                if (cI.resolveActivity(getPackageManager()) != null) {
                    // create new file
                    File image = null;
                    try {
                        image = createImageFile();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    // call camera activity
                    if (image != null) {
                        cI.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                        startActivityForResult(cI, MyApplication.REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        // record button
        Button recordBtn = new Button(myActivity);
        RelativeLayout.LayoutParams llUpload = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llUpload.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        recordBtn.setText("Record Sighting");
        recordBtn.setLayoutParams(llUpload);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (getResponses()) { // check if all questions are answered
                    double[] latLng = getLatLng(); // set latitude and longitude
                    MyApplication.mLat = String.valueOf(latLng[0]);
                    MyApplication.mLng = String.valueOf(latLng[1]);
                    addSighting(); // add sighting to sqlite database
                } else {
                    Toast toast = Toast.makeText(myActivity, "Please answer all questions", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        navBtn.addView(photoBtn);
        navBtn.addView(recordBtn);
        ll.addView(navBtn);
        sv.addView(ll);
        setContentView(sv);

        // check for saved responses
        savedVals = getSharedPreferences("SavedVals", MODE_PRIVATE);
        // object spinner
        int spinnerIndex = savedVals.getInt("object",-1);
        if(spinnerIndex != -1) {
            spObjects.setSelection(spinnerIndex);
        }
        // survey questions and responses
        for (SurveyQuestion sq: MyApplication.mListQuestions) { // check response for each question
            String qId = sq.getId(); // get question ID
            String resType = sq.getResType(); // the response type
            if (resType.equals("single") | sq.getResType().equals("yesNo")) { // single item spinner
                spinnerIndex = savedVals.getInt(qId, -1); // get selected index
                if (spinnerIndex != -1) {
                    Spinner response = (Spinner) findViewById(Integer.parseInt(qId));
                    response.setSelection(spinnerIndex);
                }
            }
            if (resType.equals("multi")) { // multi-select spinner
                Set<String> storedVals = savedVals.getStringSet(qId, null);
                if (storedVals != null) {
                    List<String> spinnerValues = new ArrayList<String>(storedVals);
                    MultiSelectSpinner response = (MultiSelectSpinner) findViewById(Integer.parseInt(qId));
                    response.setSelection(spinnerValues);
                }
            }
            if (resType.equals("text")) { // edit text
                String textValue = savedVals.getString(qId, null);
                if (textValue != null) {
                    EditText response = (EditText) findViewById(Integer.parseInt(qId));
                    response.setText(textValue);
                }

            }
        }
    }

    private File createImageFile() throws IOException {
        // generate file name in format: surveyID_timeStamp
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String fileName = "siid-" + MyApplication.mSurveyInstanceId + "_" + timeStamp;
        MyApplication.mJpg = fileName + ".jpg";
        Log.i("jpg path", MyApplication.mImagePath + MyApplication.mJpg);
        return new File(MyApplication.mImagePath + MyApplication.mJpg); // storage directory
    }

    /**
     * Method to create a layout element for a particular survey question.
     *
     * @param     sq      the survey question
     * @return            a relative layout object containing the survey question
     */
    public View addLayout(SurveyQuestion sq) {
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout rl = new RelativeLayout(myActivity);
        rl.setLayoutParams(ll);
        // create text editor
        if (sq.getResType().equals("text")) {
            TextView tvRow = new TextView(myActivity);
            EditText etRow = new EditText(myActivity);
            RelativeLayout.LayoutParams llText = new RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llText.setMargins(0, 15, 0, 0);  // left, top, right, bottom
            tvRow.setText(sq.getQuestion());
            tvRow.setLayoutParams(ll);
            etRow.setLayoutParams(llText);
            etRow.setId(Integer.parseInt(sq.getId())); // set id to question ID
            rl.addView(tvRow);
            rl.addView(etRow);
        }
        // create single item select dropdown
        if (sq.getResType().equals("single")) {
            TextView tvRow = new TextView(myActivity);
            Spinner spValues = new Spinner(myActivity);
            RelativeLayout.LayoutParams llSpin = new RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvRow.setText(sq.getQuestion());
            tvRow.setLayoutParams(ll);
            rl.addView(tvRow);
            // get response values
            String[] values = sq.getResVal().split("\\|");
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add("Select a response!");
            Collections.addAll(valueList, values);
            // add values to dropdown
            ArrayAdapter<String> valueAdapter = new ArrayAdapter<String>(
                    myActivity, android.R.layout.simple_spinner_dropdown_item, valueList);
            valueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spValues.setAdapter(valueAdapter);
            spValues.setLayoutParams(llSpin);
            spValues.setId(Integer.parseInt(sq.getId())); // set id to question ID
            rl.addView(spValues);
        }
        // create multi item select dropdown
        if (sq.getResType().equals("multi")) {
            TextView tvRow = new TextView(myActivity);
            MultiSelectSpinner spValues = new MultiSelectSpinner(myActivity);
            RelativeLayout.LayoutParams llMulti = new RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llMulti.setMargins(0, 15, 0, 0);  // left, top, right, bottom
            tvRow.setText(sq.getQuestion());
            tvRow.setLayoutParams(ll);
            rl.addView(tvRow);
            // get response values
            String[] values = sq.getResVal().split("\\|");
            ArrayList<String> valueList = new ArrayList<String>();
            Collections.addAll(valueList, values);
            // add values to dropdown
            spValues.setItems(valueList);
            llMulti.setMargins(0, 15, 0, 0);  // left, top, right, bottom
            spValues.setLayoutParams(llMulti);
            spValues.setId(Integer.parseInt(sq.getId())); // set id to question ID
            rl.addView(spValues);
        }
        // create yes/no dropdown
        if (sq.getResType().equals("yesNo")) {
            TextView tvRow = new TextView(myActivity);
            RelativeLayout.LayoutParams llYN = new RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvRow.setText(sq.getQuestion());
            tvRow.setLayoutParams(ll);
            rl.addView(tvRow);
            Spinner spYN = new Spinner(myActivity);
            // get spinner values for dropdown
            ArrayList<String> yesNo = new ArrayList<String>();
            Collections.addAll(yesNo, "Select a response!", "Yes", "No");
            ArrayAdapter<String> ynAdapter = new ArrayAdapter<String>(
                    myActivity, android.R.layout.simple_spinner_dropdown_item, yesNo);
            ynAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spYN.setAdapter(ynAdapter);
            spYN.setLayoutParams(llYN);
            spYN.setId(Integer.parseInt(sq.getId())); // set id to question ID
            rl.addView(spYN);
        }
        return rl;
    }

    /**
     * Method to get the response values from the layout.
     * All responses are stored. If any questions have not been answered
     * the method will return false.
     *
     * @return        specifies whether all questions have been answered.
     */
    public Boolean getResponses() {
        Boolean done = true;
        MyApplication.mSavedText = new ArrayList<NameValuePair>();
        savedVals = getSharedPreferences("SavedVals", 0);
        SharedPreferences.Editor prefEditor = savedVals.edit();
        // check if object selected
        if (spObjects.getSelectedItemPosition() != 0) {
            prefEditor.putInt("object", spObjects.getSelectedItemPosition());
        } else {
            done = false;
        }
        // get response for each question
        for (SurveyQuestion sq: MyApplication.mListQuestions) {
            View v = findViewById(Integer.parseInt(sq.getId())); // get view by question ID
            if (v instanceof EditText) { // if text view, get text input value
                if (!((EditText) v).getText().toString().equals("")) {
                    String response = ((EditText) v).getText().toString();
                    quResp.add(new BasicNameValuePair(sq.getId(), response));
                    prefEditor.putString(sq.getId(), ((EditText) v).getText().toString());
                    //MyApplication.mSavedText.add(new BasicNameValuePair(sq.getId(),response));
                } else {
                    done = false;
                }
            }
            else if (v instanceof MultiSelectSpinner) { // if multi spinner, get string set
                if (((MultiSelectSpinner) v).getSelectedItemsAsString().length() != 0) {
                    String response = ((MultiSelectSpinner) v).getSelectedItemsAsString();
                    quResp.add(new BasicNameValuePair(sq.getId(), response));
                    prefEditor.putStringSet(sq.getId(), new HashSet<String>(((MultiSelectSpinner) v).getSelectedStrings()));
                } else {
                    done = false;
                }
            }
            else if (v instanceof Spinner) { // if spinner, get item selected index
                if (((Spinner) v).getSelectedItemPosition() != 0) {
                    String response = ((Spinner) v).getSelectedItem().toString();
                    quResp.add(new BasicNameValuePair(sq.getId(), response));
                    prefEditor.putInt(sq.getId(), ((Spinner) v).getSelectedItemPosition());
                } else {
                    done = false;
                }
            }
        }
        prefEditor.apply();
        return done;
    }

    /**
     * Method to record a new sighting and add it to the sqlite database.
     */
    public void addSighting() {
        Toast toast = Toast.makeText(this, "Saving Sighting", Toast.LENGTH_SHORT);
        toast.show();
        // get date/time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = sdf.format(new Date());
        String ssid = "";
        // create a new SurveySighting
        SurveySighting ss = new SurveySighting(
                MyApplication.mSurveyInstanceId, MyApplication.mObjectId, MyApplication.mJpg,
                MyApplication.mLat, MyApplication.mLng, dateTime);
        // add sighting to sqlite database and get sighting ID
        try {
            DBAdapter db = new DBAdapter(this);
            ssid = String.valueOf(db.addSurveySighting(ss));
            db.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        // create a SurveyResponse for each question
        ArrayList<SurveyResponse> responses = new ArrayList<SurveyResponse>(); // survey responses
        for (int i = 0; i < MyApplication.mQuNum; i++) {
            String sqid = quResp.get(i).getName();
            String response = quResp.get(i).getValue();
            SurveyResponse sr = new SurveyResponse(
                    MyApplication.mSurveyInstanceId, ssid, sqid, response);
            responses.add(sr);
        }
        // add responses to sqlite database
        try {
            DBAdapter db = new DBAdapter(this);
            for (SurveyResponse sr: responses) {
                db.addSurveyResponse(sr);
            }
            db.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        // clear saved values
        MyApplication.mObjectId = null;
        MyApplication.mJpg = null;
        MyApplication.mSavedText = null;
        savedVals = getSharedPreferences("SavedVals", 0);
        savedVals.edit().clear().apply();
        // return to main screen
        Intent mI = new Intent(this, SurveyMain.class);
        mI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mI);
    }

    /**
     * Method to get the index of a particular value from a spinner.
     *
     * @param     spinner     the spinner name
     * @param     value       the value of the spinner
     * @return                the index of the value
     */
    private int getIndex(Spinner spinner, String value){
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++)
            if (spinner.getItemAtPosition(i).equals(value))
                index = i;
        return index;
    }

    /**
     * Method to set the latitude and longitude values.
     */
    private double[] getLatLng() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        /* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
        Location l = null;
        for (int i = providers.size()-1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }
        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
    }

    /**
     * Method to check for location change.
     *
     * @param     location    the location object
     */
    public void onLocationChanged (Location location) {
        MyApplication.mLat = String.valueOf(location.getLatitude());
        MyApplication.mLng = String.valueOf(location.getLongitude());
    }

    /**
     * Method called when returning from camera activity.
     * Returns to calling activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // image saved successfully; return to previous screen
        Intent mI = new Intent(this.getApplication(), RecordSighting.class);
        mI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mI);
    }

    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}