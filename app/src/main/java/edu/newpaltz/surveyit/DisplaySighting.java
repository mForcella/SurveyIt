/**
 * This activity will display the values for a selected sighting.
 */

package edu.newpaltz.surveyit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DisplaySighting extends Activity {

    Activity myActivity = this;
    String id, object, lat, lng, date, jpg; // values for passing between intents
    ArrayList<BasicNameValuePair> quResp; // for passing values
    ArrayList<SurveyResponse> responses;

    /**
     * Method called when activity is first launched.
     * Creates dynamic layout.
     *
     * @param    savedInstanceState    value stored from onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get object data
        getSightingData();
        // create linear layout
        ScrollView sv = new ScrollView(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(15,15,15,15);
        // configure width and height
        LinearLayout.LayoutParams llLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llLP);
        // create image view
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // set object image if available
        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams llIV = new LinearLayout.LayoutParams(500,500);
        llIV.gravity = Gravity.CENTER;
        iv.setLayoutParams(llIV);
        if (!jpg.equals("null")) {
            try {
                FileInputStream in = new FileInputStream(jpg);
                BufferedInputStream inputStream = new BufferedInputStream(in, 8192);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                iv.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ll.addView(iv);
        // display object, date, latitude and longitude
        TextView tvObject = new TextView(this);
        tvObject.setLayoutParams(llParams);
        tvObject.setText(object);
        ll.addView(tvObject);
        TextView tvDate = new TextView(this);
        tvDate.setLayoutParams(llParams);
        tvDate.setText("Date: " + date);
        ll.addView(tvDate);
        TextView tvLatLng = new TextView(this);
        tvLatLng.setLayoutParams(llParams);
        tvLatLng.setText("Lat : Long: " + lat + " : " + lng);
        ll.addView(tvLatLng);
        // display questions and responses
        for (BasicNameValuePair qr: quResp) {
            TextView tvQR = new TextView(this);
            tvQR.setLayoutParams(llParams);
            tvQR.setText(qr.getName() + ": " + qr.getValue());
            ll.addView(tvQR);
        }
        // create prev and next buttons
        RelativeLayout navBtn = new RelativeLayout(this);
        RelativeLayout.LayoutParams llNav = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        navBtn.setLayoutParams(llNav);
        Button prevBtn = new Button(this);
        RelativeLayout.LayoutParams llPrev = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llPrev.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        prevBtn.setText("Previous");
        prevBtn.setLayoutParams(llPrev);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                prevOnClick(v);
            }
        });
        Button nextBtn = new Button(this);
        RelativeLayout.LayoutParams llNext = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llNext.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        nextBtn.setText("Next");
        nextBtn.setLayoutParams(llNext);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextOnClick(v);
            }
        });
        navBtn.addView(prevBtn);
        navBtn.addView(nextBtn);
        ll.addView(navBtn);
        sv.addView(ll);
        setContentView(sv);
    }

    /**
     * Method to get intent extras.
     */
    public void getSightingData() {
        Intent intent = getIntent();
        quResp = new ArrayList<BasicNameValuePair>();
        id = intent.getStringExtra("id");
        object = intent.getStringExtra("object");
        date = intent.getStringExtra("date");
        lat = intent.getStringExtra("lat");
        lng = intent.getStringExtra("lng");
        if (intent.getStringExtra("jpg") != null)
            jpg = MyApplication.mImagePath + intent.getStringExtra("jpg");
        else
            jpg = "null";
        for (int i = 0; i < MyApplication.mQuNum; i++) {
            quResp.add(new BasicNameValuePair(
                    intent.getStringExtra("question"+i), intent.getStringExtra("response"+i)));
        }
    }

    /**
     * Method to get values from a particular sighting and pass
     * them to the next intent.
     *
     * @param   intent      the intent receiving the values
     * @param   sighting    the sighting object
     */
    public void getSightingExtras(Intent intent, ArrayList<SurveyResponse> responses, SurveySighting sighting) {
        intent.putExtra("id", sighting.getId());
        intent.putExtra("object", MyApplication.getSurveyObject(sighting.getoId()));
        intent.putExtra("date", sighting.getDate());
        intent.putExtra("lat",sighting.getLat());
        intent.putExtra("lng",sighting.getLng());
        intent.putExtra("jpg",sighting.getJpg());
        for (int i = 0; i < MyApplication.mQuNum; i++) {
            intent.putExtra("question"+i, MyApplication.getSurveyQuestion(responses.get(i).getSqId()));
            intent.putExtra("response"+i, responses.get(i).getResponse());
        }
    }

    /**
     * Changes the view to the previous sighting in the database.
     *
     * @param   v   the objecting being clicked
     */
    public void prevOnClick(View v) {
        Boolean found = false;
        Intent yI = new Intent(this, DisplaySighting.class);
        yI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // get index of previous sighting
        // find index from mListSightings with matching ID
        for (SurveySighting ss: MyApplication.mListSightings) {
            if (ss.getId().equals(id)) {
                int prevIndex = MyApplication.mListSightings.indexOf(ss)-1;
                if (prevIndex >= 0) {
                    SurveySighting prevSighting = MyApplication.mListSightings.get(prevIndex);
                    // get response values for previous sighting
                    try {
                        DBAdapter db = new DBAdapter(myActivity);
                        responses = db.getSightingResponses(prevSighting.getId());
                        db.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getSightingExtras(yI, responses, prevSighting);
                    found = true;
                }
            }
        }
        if (found) {
            startActivity(yI);
        } else {
            Toast toast = Toast.makeText(this, "No more sightings to display", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Changes the view to the next sighting in the database.
     *
     * @param   v   the objecting being clicked
     */
    public void nextOnClick(View v) {
        Boolean found = false;
        Intent yI = new Intent(this, DisplaySighting.class);
        yI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // find index of next sighting
        // find index from mListSightings with matching ID
        for (SurveySighting ss: MyApplication.mListSightings) {
            if (ss.getId().equals(id)) {
                int nextIndex = MyApplication.mListSightings.indexOf(ss)+1;
                if (nextIndex < MyApplication.mListSightings.size()) {
                    SurveySighting nextSighting = MyApplication.mListSightings.get(nextIndex);
                    // get response values for next sighting
                    try {
                        DBAdapter db = new DBAdapter(myActivity);
                        responses = db.getSightingResponses(nextSighting.getId());
                        db.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getSightingExtras(yI, responses, nextSighting);
                    found = true;
                }
            }
        }
        if (found) {
            startActivity(yI);
        } else {
            Toast toast = Toast.makeText(this, "No more sightings to display", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}