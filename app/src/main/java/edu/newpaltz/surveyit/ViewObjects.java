/**
 * This activity will display all the current survey objects.
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

import java.util.ArrayList;

public class ViewObjects extends Activity {

    Activity myActivity = this;

    /**
     * Method called when activity is first launched.
     * Creates list of objects.
     *
     * @param     savedInstanceState      value stored from onSaveInstanceState(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_objects);
        // initialize list view
        ListView lvSpecies = (ListView) findViewById(R.id.lv_objects);
        lvSpecies.setAdapter(new ListAdapter(
                myActivity, R.id.lv_objects, MyApplication.mListObjects));
    }

    /**
     * List adapter
     */
    private class ListAdapter extends ArrayAdapter<SurveyObject> {

        private ArrayList<SurveyObject> mList;

        // constructor, assigns mListObjects to mList
        public ListAdapter(Context context, int textViewResourceId, ArrayList<SurveyObject> list) {
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
                final SurveyObject object = mList.get(position);
                if (object != null) {
                    // setting list_item views
                    ( (TextView) view.findViewById(R.id.tv_name) ).setTextSize(20);
                    ( (TextView) view.findViewById(R.id.tv_name) ).setText( object.getId() + " : " + object.getOptions().get(0) );
                    // go to individual species page
                    ( view.findViewById(R.id.tv_name) ).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent myIntent = new Intent(ViewObjects.this, DisplayObject.class);
                            myIntent.putExtra("id", object.getId());
                            myIntent.putExtra("sid", object.getSid());
                            myIntent.putExtra("jpg", object.getJpg());
                            myIntent.putExtra("options", object.getOptions());
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            ViewObjects.this.startActivity(myIntent);
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