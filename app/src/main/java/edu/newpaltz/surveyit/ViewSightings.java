/**
 * View a list of the current sightings.
 */

package edu.newpaltz.surveyit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class ViewSightings extends Activity {

    Activity myActivity = this;

    /**
     * Method called when activity is first launched.
     * Creates list of sightings.
     *
     * @param     savedInstanceState      value stored from onSaveInstanceState(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_sightings);
        // get list of current sightings
        try {
            DBAdapter db = new DBAdapter(myActivity);
            MyApplication.mListSightings = db.getSurveySightings(MyApplication.mSurveyInstanceId);
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // initialize list view
        ListView lvSpecies = (ListView) findViewById(R.id.lv_sightings);
        lvSpecies.setAdapter(new ListAdapter(
                myActivity, R.id.lv_sightings, MyApplication.mListSightings));
    }

    /**
     * List adapter
     */
    private class ListAdapter extends ArrayAdapter<SurveySighting> {

        private ArrayList<SurveySighting> mList;

        // constructor, assigns mListObjects to mList
        public ListAdapter(Context context, int textViewResourceId, ArrayList<SurveySighting> list) {
            super(context, textViewResourceId, list);
            mList = list;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            View view = convertView;
            try{
                if (view == null) {
                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(R.layout.list_item, parent, false);
                }
                final SurveySighting sighting = mList.get(position);
                if (sighting != null) {
                    String object = MyApplication.getSurveyObject(sighting.getoId());
                    String date = sighting.getDate();
                    // setting list_item views
                    ( (TextView) view.findViewById(R.id.tv_name) ).setTextSize(20);
                    ( (TextView) view.findViewById(R.id.tv_name) ).setText(
                            object + " : " + date);
                    // go to individual sighting page
                    ( view.findViewById(R.id.tv_name) ).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent myIntent = new Intent(ViewSightings.this, DisplaySighting.class);
                            // get sighting values
                            myIntent.putExtra("id", sighting.getId());
                            myIntent.putExtra("object", MyApplication.getSurveyObject(sighting.getoId()));
                            myIntent.putExtra("date", sighting.getDate());
                            myIntent.putExtra("lat", sighting.getLat());
                            myIntent.putExtra("lng", sighting.getLng());
                            myIntent.putExtra("jpg", sighting.getJpg());
                            // get response values for selected sighting
                            ArrayList<SurveyResponse> responses;
                            try {
                                DBAdapter db = new DBAdapter(myActivity);
                                responses = db.getSightingResponses(sighting.getId());
                                db.close();
                                for (int i = 0; i < MyApplication.mQuNum; i++) {
                                    myIntent.putExtra("question"+i, MyApplication.getSurveyQuestion(responses.get(i).getSqId()));
                                    myIntent.putExtra("response"+i, responses.get(i).getResponse());
                                }
                                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                ViewSightings.this.startActivity(myIntent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            return view;
        }
    }
}