/*
 Helper class for the sqlite database.
*/

package edu.newpaltz.surveyit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DBAdapter extends SQLiteOpenHelper {

    private static final String DB_NAME = "SurveyIt.sqlite";
    private static final String DB_PATH = MyApplication.getAppContext().getDatabasePath(DB_NAME).getPath();
    private Context myContext;
    public SQLiteDatabase myDatabase;

    // survey
    private static final String TABLE_SURVEY = "survey";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIP = "descrip";
    private static final String KEY_DATE = "date";
    private static final String KEY_OPNUM = "opNum";
    private static final String CREATE_SURVEY = "CREATE TABLE " + TABLE_SURVEY +
            "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME + " TEXT, " +
            KEY_DESCRIP + " TEXT, " + KEY_OPNUM + " INTEGER, " + KEY_DATE + " DATE);";

    // survey object
    private static final String TABLE_SURVEY_OBJECT = "SurveyObject";
    private static final String KEY_SID = "sid";
    private static final String KEY_JPG = "jpg";
    private static final String KEY_OPTION1 = "option1";
    private static final String KEY_OPTION2 = "option2";
    private static final String KEY_OPTION3 = "option3";
    private static final String KEY_OPTION4 = "option4";
    private static final String KEY_OPTION5 = "option5";
    private static final String KEY_OPTION6 = "option6";
    private static final String KEY_OPTION7 = "option7";
    private static final String KEY_OPTION8 = "option8";
    private static final String KEY_OPTION9 = "option9";
    private static final String KEY_OPTION10 = "option10";
    private static final String CREATE_SURVEY_OBJECT = "CREATE TABLE " + TABLE_SURVEY_OBJECT +
            "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_SID + " INTEGER, " +
            KEY_JPG + " TEXT, " + KEY_OPTION1 + " TEXT, " + KEY_OPTION2 + " TEXT, " +
            KEY_OPTION3 + " TEXT, " + KEY_OPTION4 + " TEXT, " + KEY_OPTION5 + " TEXT, " +
            KEY_OPTION6 + " TEXT, " + KEY_OPTION7 + " TEXT, " + KEY_OPTION8 + " TEXT, " +
            KEY_OPTION9 + " TEXT, " + KEY_OPTION10 + " TEXT);";

    // survey instance
    private static final String TABLE_SURVEY_INSTANCE = "surveyInstance";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_OBSERVER = "observer";
    private static final String KEY_COMMENT = "comment";
    private static final String CREATE_SURVEY_INSTANCE = "CREATE TABLE " + TABLE_SURVEY_INSTANCE +
            "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_SID + " INTEGER, " +
            KEY_LOCATION + " TEXT, " + KEY_OBSERVER + " TEXT, " + KEY_COMMENT + " TEXT, " +
            KEY_DATE + " DATE);";

    // survey question
    private static final String TABLE_SURVEY_QUESTION = "surveyQuestion";
    private static final String KEY_QUESTION = "question";
    private static final String KEY_RESTYPE = "resType";
    private static final String KEY_RESVAL = "resVal";
    private static final String CREATE_SURVEY_QUESTION = "CREATE TABLE " + TABLE_SURVEY_QUESTION +
            "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_SID + " INTEGER, " +
            KEY_QUESTION + " TEXT, " + KEY_RESTYPE + " TEXT, " + KEY_RESVAL + " TEXT);";

    // survey sighting
    private static final String TABLE_SURVEY_SIGHTING = "surveySighting";
    private static final String KEY_SIID = "siId";
    private static final String KEY_OID = "oId";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String CREATE_SURVEY_SIGHTING = "CREATE TABLE " + TABLE_SURVEY_SIGHTING +
            "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_SIID + " INTEGER, " +
            KEY_OID + " INTEGER, " + KEY_JPG + " TEXT, " + KEY_LAT + " TEXT, " + KEY_LNG + " TEXT, " +
            KEY_DATE + " DATETIME);";

    // survey response
    private static final String TABLE_SURVEY_RESPONSE = "surveyResponse";
    private static final String KEY_SSID = "ssId";
    private static final String KEY_SQID = "sqId";
    private static final String KEY_RESPONSE = "response";
    private static final String CREATE_SURVEY_RESPONSE = "CREATE TABLE " +
            TABLE_SURVEY_RESPONSE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_SIID + " INTEGER, " + KEY_SSID + " INTEGER, " + KEY_SQID + " INTEGER, " +
            KEY_RESPONSE + " TEXT);";

    // survey object value
    private static final String TABLE_SURVEY_OBJECT_VALUE = "surveyObjectValue";
    private static final String CREATE_SURVEY_OBJECT_VALUE = "CREATE TABLE " +
            TABLE_SURVEY_OBJECT_VALUE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_SID + " INTEGER, " + KEY_OPNUM + " INTEGER, " + KEY_DESCRIP + " TEXT);";

    // survey object image
    private static final String TABLE_SURVEY_OBJECT_IMAGE = "surveyObjectImage";
    private static final String KEY_RANK = "rank";
    private static final String KEY_FLAG = "flag";
    private static final String CREATE_SURVEY_OBJECT_IMAGE = "CREATE TABLE " +
            TABLE_SURVEY_OBJECT_IMAGE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_OID + " INTEGER, " + KEY_JPG + " TEXT, " + KEY_SSID + " INTEGER, " +
            KEY_DATE + " DATETIME, " + KEY_RANK + " INTEGER, " + KEY_FLAG + " TEXT);";

    // constructor
    public DBAdapter (Context context) throws IOException {
        super(context, DB_NAME, null, 1);
        myContext = context;
        boolean dbExist = checkDatabase();
        if (dbExist)
            openDatabase();
        else
            createDatabase();
    }

    public void createDatabase() {
        boolean dbExist = checkDatabase();
        if(!dbExist) {
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private Boolean checkDatabase() {
        boolean checkDb = false;
        try {
            File dbFile = new File(DB_PATH);
            checkDb = dbFile.exists();
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkDb;
    }

    private void copyDatabase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        OutputStream myOutput = new FileOutputStream(DB_PATH);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer,0,length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDatabase() throws SQLiteException {
        myDatabase = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void close() {
        if(myDatabase != null) {
            myDatabase.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SURVEY);
        db.execSQL(CREATE_SURVEY_INSTANCE);
        db.execSQL(CREATE_SURVEY_OBJECT);
        db.execSQL(CREATE_SURVEY_OBJECT_VALUE);
        db.execSQL(CREATE_SURVEY_QUESTION);
        db.execSQL(CREATE_SURVEY_RESPONSE);
        db.execSQL(CREATE_SURVEY_SIGHTING);
        db.execSQL(CREATE_SURVEY_OBJECT_IMAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SURVEY);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SURVEY_INSTANCE);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SURVEY_OBJECT_VALUE);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SURVEY_RESPONSE);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SURVEY_OBJECT);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SURVEY_QUESTION);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SURVEY_SIGHTING);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SURVEY_OBJECT_IMAGE);
        onCreate(db);
    }

    /********************** CRUD OPERATIONS **********************/

    // delete survey responses from database
    public void deleteSurveyResponses(String whereClause) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM surveyResponse WHERE id IN (" + whereClause + ");";
        db.execSQL(query);
        db.close();
    }

    // delete survey responses from database
    public void deleteSurveySightings(String whereClause) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM surveySighting WHERE id IN (" + whereClause + ");";
        db.execSQL(query);
        db.close();
    }

    // checks if the survey ID returns any results from database
    public Boolean checkSurvey(String sid) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM survey WHERE id = '" + sid + "';";
        Cursor c = db.rawQuery(query, null);
        int count = c.getCount();
        db.close();
        return count > 0; // true if count is greater than zero
    }

    // add a new survey the sqlite database
    public void addSurvey(Survey survey) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, survey.getId());
        values.put(KEY_NAME, survey.getName());
        values.put(KEY_DESCRIP, survey.getDesc());
        values.put(KEY_DATE, survey.getDate());
        values.put(KEY_OPNUM, survey.getOpNum());
        db.insert(TABLE_SURVEY, null, values);
        db.close();
    }

    // checks if the surveyObjectValue ID returns any results from database
    public Boolean checkSurveyObjectValue(String sovId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyObjectValue WHERE id = '" + sovId + "';";
        Cursor c = db.rawQuery(query, null);
        int count = c.getCount();
        db.close();
        return count > 0; // true if count is greater than zero
    }

    // add a new survey object value to the sqlite database
    public void addSurveyObjectValue(SurveyObjectValue sov) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, sov.getId());
        values.put(KEY_SID, sov.getSid());
        values.put(KEY_OPNUM, sov.getOpNum());
        values.put(KEY_DESCRIP, sov.getDescrip());
        db.insert(TABLE_SURVEY_OBJECT_VALUE, null, values);
        db.close();
    }

    // checks if the surveyObject ID returns any results from database
    public Boolean checkSurveyObject(String soId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyObject WHERE id = '" + soId + "';";
        Cursor c = db.rawQuery(query, null);
        int count = c.getCount();
        db.close();
        return count > 0; // true if count is greater than zero
    }

    // add a new survey object value to the sqlite database
    public void addSurveyObjectImage(SurveyObjectImage soi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, soi.getId());
        values.put(KEY_OID, soi.getoId());
        values.put(KEY_JPG, soi.getJpg());
        values.put(KEY_SSID, soi.getSsId());
        values.put(KEY_DATE, soi.getDate());
        values.put(KEY_RANK, soi.getRank());
        values.put(KEY_FLAG, soi.getFlag());
        db.insert(TABLE_SURVEY_OBJECT_IMAGE, null, values);
        db.close();
    }

    // checks if the surveyObject ID returns any results from database
    public Boolean checkSurveyObjectImage(String soiId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyObjectImage WHERE id = '" + soiId + "';";
        Cursor c = db.rawQuery(query, null);
        int count = c.getCount();
        db.close();
        return count > 0; // true if count is greater than zero
    }

    // add a new survey object to the sqlite database
    public void addSurveyObject(SurveyObject surveyObject, int options) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, surveyObject.getId());
        values.put(KEY_SID, surveyObject.getSid());
        values.put(KEY_JPG, surveyObject.getJpg());
        switch (options) {
            case 10: values.put(KEY_OPTION10, surveyObject.getOptions().get(9));
            case 9: values.put(KEY_OPTION9, surveyObject.getOptions().get(8));
            case 8: values.put(KEY_OPTION8, surveyObject.getOptions().get(7));
            case 7: values.put(KEY_OPTION7, surveyObject.getOptions().get(6));
            case 6: values.put(KEY_OPTION6, surveyObject.getOptions().get(5));
            case 5: values.put(KEY_OPTION5, surveyObject.getOptions().get(4));
            case 4: values.put(KEY_OPTION4, surveyObject.getOptions().get(3));
            case 3: values.put(KEY_OPTION3, surveyObject.getOptions().get(2));
            case 2: values.put(KEY_OPTION2, surveyObject.getOptions().get(1));
            case 1: values.put(KEY_OPTION1, surveyObject.getOptions().get(0));
        }
        db.insert(TABLE_SURVEY_OBJECT, null, values);
        db.close();
    }

    // checks if the surveyQuestion ID returns any results from database
    public Boolean checkSurveyQuestion(String sqId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyQuestion WHERE id = '" + sqId + "';";
        Cursor c = db.rawQuery(query, null);
        int count = c.getCount();
        db.close();
        return count > 0; // true if count is greater than zero
    }

    // add a new survey question to the sqlite database
    public void addSurveyQuestion(SurveyQuestion surveyQuestion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, surveyQuestion.getId());
        values.put(KEY_SID, surveyQuestion.getSid());
        values.put(KEY_QUESTION, surveyQuestion.getQuestion());
        values.put(KEY_RESTYPE, surveyQuestion.getResType());
        values.put(KEY_RESVAL, surveyQuestion.getResVal());
        db.insert(TABLE_SURVEY_QUESTION, null, values);
        db.close();
    }

    // add a new survey instance to the sqlite database and return the survey instance ID
    public long addSurveyInstance(SurveyInstance surveyInstance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SID, surveyInstance.getSid());
        values.put(KEY_LOCATION, surveyInstance.getLocation());
        values.put(KEY_OBSERVER, surveyInstance.getObserver());
        values.put(KEY_COMMENT, surveyInstance.getComment());
        values.put(KEY_DATE, surveyInstance.getDate());
        long siId = db.insert(TABLE_SURVEY_INSTANCE, null, values);
        db.close();
        return siId;
    }

    // add a new survey sighting to the sqlite database and return the survey sighting ID
    public long addSurveySighting(SurveySighting ss) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SIID, ss.getSiId());
        values.put(KEY_OID, ss.getoId());
        values.put(KEY_JPG, ss.getJpg());
        values.put(KEY_LAT, ss.getLat());
        values.put(KEY_LNG, ss.getLng());
        values.put(KEY_DATE, ss.getDate());
        long ssId = db.insert(TABLE_SURVEY_SIGHTING, null, values);
        db.close();
        return ssId;
    }

    // add a new survey response the sqlite database and return the survey response ID
    public long addSurveyResponse(SurveyResponse sr) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SIID, sr.getSiId());
        values.put(KEY_SSID, sr.getSsId());
        values.put(KEY_SQID, sr.getSqId());
        values.put(KEY_RESPONSE, sr.getResponse());
        long srId = db.insert(TABLE_SURVEY_RESPONSE, null, values);
        db.close();
        return srId;
    }

    // get a list of surveys
    public ArrayList<Survey> getSurveys() {
        ArrayList<Survey> surveys = new ArrayList<Survey>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM survey;";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // get survey values
                String id = c.getString(0);
                String name = c.getString(1);
                String descrip = c.getString(2);
                int opNum = Integer.parseInt(c.getString(3));
                String date = c.getString(4);
                // create survey and add to array
                Survey s = new Survey(id, name, descrip, opNum, date);
                surveys.add(s);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return surveys;
    }

    // get a list of survey objects
    public ArrayList<SurveyObject> getSurveyObjects(String sid) {
        ArrayList<SurveyObject> surveyObjects = new ArrayList<SurveyObject>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyObject where sid = '" + sid + "';";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                ArrayList<String> options = new ArrayList<String>();
                // get survey object values
                String id = c.getString(0);
                String jpg = c.getString(2);
                for (int i = 0; i < MyApplication.mOpNum; i++) {
                    options.add(c.getString(3+i));
                }
                // create survey object and add to array
                SurveyObject so = new SurveyObject(id, sid, jpg, options);
                surveyObjects.add(so);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return surveyObjects;
    }

    // get a list of survey object values
    public ArrayList<SurveyObjectValue> getSurveyObjectValues(String sid) {
        ArrayList<SurveyObjectValue> surveyObjectValues = new ArrayList<SurveyObjectValue>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyObjectValue where sid = '" + sid + "';";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // get survey object value values
                String id = c.getString(0);
                String descrip = c.getString(2);
                String opNum = c.getString(3);
                // create survey object value and add to array
                SurveyObjectValue sov = new SurveyObjectValue(id, sid, descrip, opNum);
                surveyObjectValues.add(sov);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return surveyObjectValues;
    }

    // get a list of survey questions
    public ArrayList<SurveyQuestion> getSurveyQuestions(String sid) {
        ArrayList<SurveyQuestion> surveyQuestions = new ArrayList<SurveyQuestion>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyQuestion where sid = '" + sid + "';";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // get survey question values
                String id = c.getString(0);
                String question = c.getString(2);
                String resType = c.getString(3);
                String resVal = c.getString(4);
                // create survey question and add to array
                SurveyQuestion sq = new SurveyQuestion(id, sid, question, resType, resVal);
                surveyQuestions.add(sq);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return surveyQuestions;
    }

    // get a list of survey objects images
    public ArrayList<SurveyObjectImage> getSurveyObjectImages(String whereClause) {
        ArrayList<SurveyObjectImage> surveyObjects = new ArrayList<SurveyObjectImage>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyObjectImage where oId in (" + whereClause + ");";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // get survey object values
                String id = c.getString(0);
                String oid = c.getString(1);
                String jpg = c.getString(2);
                String ssid = c.getString(3);
                String date = c.getString(4);
                String rank = c.getString(5);
                String flag = c.getString(6);
                // create survey object image and add to array
                SurveyObjectImage soi = new SurveyObjectImage(id, oid, jpg, ssid, date, rank, flag);
                surveyObjects.add(soi);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return surveyObjects;
    }

    // get a list of all survey instances
    public ArrayList<SurveyInstance> getSurveyInstances(String sid) {
        ArrayList<SurveyInstance> outings = new ArrayList<SurveyInstance>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyInstance WHERE sid = '" + sid + "';";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // get survey instance values
                String id = c.getString(0);
                String location = c.getString(2);
                String observer = c.getString(3);
                String comment = c.getString(4);
                String date = c.getString(5);
                // create survey response object and add to array
                SurveyInstance si = new SurveyInstance(id, sid, location, observer, comment, date);
                outings.add(si);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return outings;
    }

    // this method returns a list of survey sightings for the current survey instance
    public ArrayList<SurveySighting> getSurveySightings(String siId) {
        ArrayList<SurveySighting> sightings = new ArrayList<SurveySighting>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveySighting WHERE siId = '" + siId + "';";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // get survey sighting values
                String id = c.getString(0);
                String oId = c.getString(2);
                String jpg = c.getString(3);
                String lat = c.getString(4);
                String lng = c.getString(5);
                String date = c.getString(6);
                // create survey response object and add to array
                SurveySighting ss = new SurveySighting(id, siId, oId, jpg, lat, lng, date);
                sightings.add(ss);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return sightings;
    }

    // this method returns a list of survey responses for the current sighting
    public ArrayList<SurveyResponse> getSightingResponses(String ssId) {
        ArrayList<SurveyResponse> responses = new ArrayList<SurveyResponse>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyResponse where ssId = '"+ssId+"';";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // get survey response values
                String id = c.getString(0);
                String siId = c.getString(1);
                String sqId = c.getString(3);
                String response = c.getString(4);
                // create survey response object and add to array
                SurveyResponse sr = new SurveyResponse(id, siId, ssId, sqId, response);
                responses.add(sr);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return responses;
    }

    // this method returns a list of survey responses for the current sighting
    public ArrayList<SurveyResponse> getSurveyResponses(String siId) {
        ArrayList<SurveyResponse> responses = new ArrayList<SurveyResponse>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM surveyResponse where siId = '"+siId+"';";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // get survey response values
                String id = c.getString(0);
                String ssId = c.getString(2);
                String sqId = c.getString(3);
                String response = c.getString(4);
                // create survey response object and add to array
                SurveyResponse sr = new SurveyResponse(id, siId, ssId, sqId, response);
                responses.add(sr);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return responses;
    }
    }