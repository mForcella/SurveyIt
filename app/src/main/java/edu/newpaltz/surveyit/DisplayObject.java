/**
 * This activity will display the values for a selected object.
 * Allows the user to record a new sighting for the current object.
 */

package edu.newpaltz.surveyit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class DisplayObject extends Activity {

    Activity myActivity = this;
    String id, jpg; // used to pass values between intents
    ArrayList<String> options; // used to pass values between intents

    /**
     * Method called when activity is first launched.
     * Creates the dynamic layout for the current object.
     *
     * @param     savedInstanceState      value stored from onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get object data
        getObjectData();
        // create linear layout
        ScrollView sv = new ScrollView(myActivity);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        // configure width and height
        LayoutParams llLP = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llLP);
        // create image view
        ImageView iv = new ImageView(this);
        LayoutParams llIV = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        iv.setLayoutParams(llIV);
        // set object image if available
        if (!jpg.equals("NULL")) {
            String path = Environment.getExternalStorageDirectory().getPath()+"/"+jpg;
            File imgFile = new File(path);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                iv.setImageBitmap(myBitmap);
            }
        }
        ll.addView(iv);
        // create a text view for each option
        LayoutParams llTV = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < options.size(); i++) {
            final TextView tvRow = new TextView(this);
            tvRow.setLayoutParams(llTV);
            // set object info
            tvRow.setText(MyApplication.mListObjectVals.get(i).getDescrip() + ": " + options.get(i));
            ll.addView(tvRow);
        }
        // create button to record a sighting
        Button recordBtn = new Button(this);
        recordBtn.setText("Record a sighting");
        recordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent yI = new Intent(myActivity, RecordSighting.class);
                yI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                MyApplication.mObjectId = getIntent().getStringExtra("id"); // set object ID
                startActivity(yI);
            }
        });
        ll.addView(recordBtn);
        // create prev and next buttons
        RelativeLayout navBtn = new RelativeLayout(this);
        RelativeLayout.LayoutParams llNav = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        navBtn.setLayoutParams(llNav);
        Button prevBtn = new Button(this);
        RelativeLayout.LayoutParams llPrev = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
     * Method to get the stored intent extras.
     */
    public void getObjectData() {
        Intent i = getIntent();
        id = i.getStringExtra("id");
        jpg = i.getStringExtra("jpg");
        options = i.getStringArrayListExtra("options");
    }

    /**
     * Method called when switching the view to the previous object in the database.
     */
    public void prevOnClick(View v) {
        Intent yI = new Intent(this, DisplayObject.class);
        yI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Boolean found = false;
        // previous object ID
        String oid = String.valueOf(Integer.parseInt(id)-1);
        // look for previous object in object list
        for (SurveyObject so: MyApplication.mListObjects) {
            if (so.getId().equals(oid)) {
                yI.putExtra("id", so.getId());
                yI.putExtra("jpg", so.getJpg());
                yI.putExtra("options", so.getOptions());
                found = true;
            }
        }
        if (found) {
            startActivity(yI);
        } else {
            Toast toast = Toast.makeText(this, "No more objects to display", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Method called when switching the view to the next object in the database.
     */
    public void nextOnClick(View v) {
        Boolean found = false;
        Intent yI = new Intent(this, DisplayObject.class);
        yI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // next object ID
        String oid = String.valueOf(Integer.parseInt(id)+1);
        // look for next object in object list
        for (SurveyObject so: MyApplication.mListObjects) {
            if (so.getId().equals(oid)) {
                yI.putExtra("id", so.getId());
                yI.putExtra("jpg", so.getJpg());
                yI.putExtra("options", so.getOptions());
                found = true;
            }
        }
        if (found) {
            startActivity(yI);
        } else {
            Toast toast = Toast.makeText(this, "No more objects to display", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}