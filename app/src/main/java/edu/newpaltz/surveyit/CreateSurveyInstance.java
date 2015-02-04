/**
 * This activity will create a new survey instance for a particular survey.
 * Each survey instance, or outing, represents a particular day surveying in a
 * particular location by a particular group of observers.
 * The user may resume an outing from earlier in the day or create a new outing.
 * The user will enter the outing location, observers and any additional comments.
 * The survey instance will be added to the sqlite database.
 * The 'surveyInstance' table contains the following columns:
 *   id:         Survey instance ID number (integer)
 *   sid:        ID of the survey for which this is an instance of (integer)
 *   location:   Location description (text)
 *   observer:   Name of observer or observers (text)
 *   comment:    Additional comments (text)
 *   date:       Date of survey instance (date)
 */

package edu.newpaltz.surveyit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;

public class CreateSurveyInstance extends Activity {

    Activity myActivity = this;
    EditText etSurvLoc, etSurvObs, etSurvComm; // text boxes to enter new outing details

    /**
     * Method called when activity is first launched.
     * User enters values for the new survey instance into the text boxes.
     * A new survey instance will be added to the sqlite databases.
     * Also allows the user to resume a survey from the same day.
     *
     * @param     savedInstanceState      value stored from onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_survey_instance);
        // get survey instances for current survey
        try {
            DBAdapter db = new DBAdapter(this);
            MyApplication.mListOutings = db.getSurveyInstances(MyApplication.mSurveyId);
            db.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        // add survey instances to dropdown
        ArrayList<String> outingList = new ArrayList<String>(); // string list to populate dropdown
        Spinner spOutings = (Spinner) findViewById(R.id.outingSelect);
        if (MyApplication.mListOutings.size() == 0) {
            outingList.add("No outings to resume");
        } else {
            outingList.add("Select an outing");
            // add survey instance values to string list
            for (SurveyInstance si : MyApplication.mListOutings) {
                outingList.add(si.getId() + ": " + si.getObserver() + ", " + si.getLocation() + ", " + si.getDate());
            }
        }
        ArrayAdapter<String> outingAdapter = new ArrayAdapter<String>(
                myActivity, android.R.layout.simple_spinner_dropdown_item, outingList);
        outingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOutings.setAdapter(outingAdapter);
        spOutings.setPromptId(R.string.choose_survey);
        spOutings.setOnItemSelectedListener(new MyOnItemSelectedListener());

        // survey button: create a new survey, add to sqlite database
        final Button projectButton = (Button) findViewById(R.id.survBeginBtn);
        projectButton.setEnabled(true);
        projectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // get values from text box
                etSurvLoc = (EditText)findViewById(R.id.survLocBox);
                MyApplication.mSurveyLoc = etSurvLoc.getText().toString();
                etSurvObs = (EditText)findViewById(R.id.survObsBox);
                MyApplication.mSurveyObs = etSurvObs.getText().toString();
                etSurvComm = (EditText)findViewById(R.id.survCommBox);
                MyApplication.mSurveyComm = etSurvComm.getText().toString();

                // post to sqlite database; get survey instance ID
                SurveyInstance newSurveyInstance = new SurveyInstance(MyApplication.mSurveyId,
                        MyApplication.mSurveyLoc, MyApplication.mSurveyObs,
                        MyApplication.mSurveyComm, MyApplication.mSurveyDate);
                try {
                    DBAdapter db = new DBAdapter(myActivity);
                    MyApplication.mSurveyInstanceId = String.valueOf(db.addSurveyInstance(newSurveyInstance));
                    db.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // go to main screen with newly created outing
                Intent mI = new Intent(myActivity, SurveyMain.class);
                mI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mI);
            }
        });
        projectButton.setSelected(true);
    }

    /**
     * The listener class for the surveyInstance dropdown menu.
     */
    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        /**
         * This method will get the values for the selected outing from the dropdown menu.
         *
         * @param     parent      the adapter view where the selection occurred
         * @param     view        the dropdown object that was clicked
         * @param     pos         the position of the dropdown that was selected
         * @param     id          the row id of the item selected
         */
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (pos > 0) { // pos 0 = "Choose your survey!"
                // get selected survey instance ID
                String siid = parent.getItemAtPosition(pos).toString().split(":")[0];
                // get values for the selected survey instance
                for (SurveyInstance si: MyApplication.mListOutings) {
                    // find survey object that matches survey instance ID from dropdown
                    if (si.getId().equals(siid)) {
                        MyApplication.mSurveyInstanceId = siid;
                        MyApplication.mSurveyLoc = si.getLocation();
                        MyApplication.mSurveyObs = si.getObserver();
                        MyApplication.mSurveyComm = si.getComment();
                        MyApplication.mSurveyDate = si.getDate();
                    }
                }

                // go to main screen with selected survey instance values
                Intent mI = new Intent(myActivity, SurveyMain.class);
                mI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mI);
            }
        }

        public void onNothingSelected(AdapterView parent) { }
    }
}