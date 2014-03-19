package com.dieend.uvahunt.model;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dieend.uvahunt.UvaHuntActivity;

public class DBManager {
	private static class DBHelper  extends SQLiteOpenHelper {
		public static final String DB_NAME = UvaHuntActivity.TAG + ".DATABASE";
		
		public static final String PROBLEM_TABLE = "problem";
		public static final String PROBLEM_COLUMN_ID = "id"; //0
		public static final String PROBLEM_COLUMN_NUMBER = "number"; //1
		public static final String PROBLEM_COLUMN_TITLE = "title"; //2
		public static final String PROBLEM_COLUMN_DACU = "dacu"; //3
		public static final String PROBLEM_COLUMN_BEST_RUNTIME = "best_runtime"; //4
		public static final String PROBLEM_COLUMN_BEST_MEMORY = "best_memory"; //5
		public static final String PROBLEM_COLUMN_RUNTIME_ERROR = "nre";//12
		public static final String PROBLEM_COLUMN_OUTPUT_LIMIT = "nole";//13
		public static final String PROBLEM_COLUMN_TIME_LIMIT = "ntle";//14
		public static final String PROBLEM_COLUMN_MEMORY_LIMIT = "nmle";//15
		public static final String PROBLEM_COLUMN_WRONG_ANSWER = "nwa"; //16
		public static final String PROBLEM_COLUMN_PRESENTATION_ERROR = "npe";//17
		public static final String PROBLEM_COLUMN_ACCEPTED = "nac"; //18
		public static final String PROBLEM_COLUMN_RUNTIME_LIMIT = "runtime_limit"; // 19
		public static final String PROBLEM_COLUMN_IS_SOLVED = "solved"; //
		public static final String[] PROBLEM_COLUMNS = {PROBLEM_COLUMN_ID,
			PROBLEM_COLUMN_NUMBER,
			PROBLEM_COLUMN_TITLE,
			PROBLEM_COLUMN_DACU,
			PROBLEM_COLUMN_BEST_RUNTIME,
			PROBLEM_COLUMN_RUNTIME_LIMIT,
			PROBLEM_COLUMN_BEST_MEMORY,
			PROBLEM_COLUMN_RUNTIME_ERROR,
			PROBLEM_COLUMN_OUTPUT_LIMIT,
			PROBLEM_COLUMN_TIME_LIMIT,
			PROBLEM_COLUMN_MEMORY_LIMIT,
			PROBLEM_COLUMN_WRONG_ANSWER,
			PROBLEM_COLUMN_PRESENTATION_ERROR,
			PROBLEM_COLUMN_ACCEPTED,
			PROBLEM_COLUMN_IS_SOLVED};
		private static final int DATABASE_VERSION = 1;
	
		private static final String CREATE_TABLE_PROBLEM = "create table "
			      + PROBLEM_TABLE + "(" + 
				PROBLEM_COLUMN_ID + " INTEGER primary key," + 
			    PROBLEM_COLUMN_NUMBER + " INTEGER, " +
			    PROBLEM_COLUMN_TITLE + " TEXT, " +
				PROBLEM_COLUMN_DACU + " INTEGER, " +
				PROBLEM_COLUMN_BEST_RUNTIME + " INTEGER, " +
				PROBLEM_COLUMN_RUNTIME_LIMIT + " INTEGER, " +
				PROBLEM_COLUMN_BEST_MEMORY + " INTEGER, " +
				PROBLEM_COLUMN_RUNTIME_ERROR + " INTEGER, " +
				PROBLEM_COLUMN_OUTPUT_LIMIT + " INTEGER, " +
				PROBLEM_COLUMN_TIME_LIMIT + " INTEGER, " +
				PROBLEM_COLUMN_MEMORY_LIMIT + " INTEGER, " +
				PROBLEM_COLUMN_WRONG_ANSWER + " INTEGER, " +
				PROBLEM_COLUMN_PRESENTATION_ERROR + " INTEGER, " +
				PROBLEM_COLUMN_ACCEPTED + " INTEGER" +
				PROBLEM_COLUMN_IS_SOLVED + " INTEGER);";
		
		public DBHelper(Context context) {
			super(context, DB_NAME, null, DATABASE_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			  db.execSQL(CREATE_TABLE_PROBLEM);
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + PROBLEM_TABLE);
		    onCreate(db);
		}
	}
	public static DBManager instance = null;
	private DBManager(Context context){
		dbHelper = new DBHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private static boolean ready = false;
	public static DBManager $() {
		if (instance == null) {
			throw new RuntimeException("must prepared first");
		}
		return instance;
	}
	public static boolean isReady() {
		return ready;
	}
	
	public static void prepare(Context context) {
		ready = false;
		if (instance == null) {
			instance = new DBManager(context);
		}
		ready = true;
	}
	private void createProblem(int id, int number, String title, int dacu, int bestRuntime, 
								int bestMemory, int nRE, int nOLE, int nTLE, int nMLE, 
								int nWA, int nPE, int nAC, int timeLimit){
		ContentValues values = new ContentValues();
	    values.put(DBHelper.PROBLEM_COLUMN_ID, id);
	    values.put(DBHelper.PROBLEM_COLUMN_NUMBER, number);
	    values.put(DBHelper.PROBLEM_COLUMN_TITLE, title);
	    values.put(DBHelper.PROBLEM_COLUMN_DACU, dacu);
	    values.put(DBHelper.PROBLEM_COLUMN_BEST_RUNTIME, bestRuntime);
	    values.put(DBHelper.PROBLEM_COLUMN_BEST_MEMORY, bestMemory);
	    values.put(DBHelper.PROBLEM_COLUMN_RUNTIME_ERROR, nRE);
	    values.put(DBHelper.PROBLEM_COLUMN_OUTPUT_LIMIT, nOLE);
	    values.put(DBHelper.PROBLEM_COLUMN_TIME_LIMIT, nTLE);
	    values.put(DBHelper.PROBLEM_COLUMN_MEMORY_LIMIT, nMLE);
	    values.put(DBHelper.PROBLEM_COLUMN_WRONG_ANSWER, nWA);
	    values.put(DBHelper.PROBLEM_COLUMN_PRESENTATION_ERROR, nPE);
	    values.put(DBHelper.PROBLEM_COLUMN_ACCEPTED, nAC);
	    values.put(DBHelper.PROBLEM_COLUMN_RUNTIME_LIMIT, timeLimit);
	    values.put(DBHelper.PROBLEM_COLUMN_IS_SOLVED, false);
	    db.insertWithOnConflict(DBHelper.PROBLEM_TABLE, null,values, SQLiteDatabase.CONFLICT_REPLACE);
	}
	public void populateProblem(String json) {
		try {
			JSONArray problems = new JSONArray(json);
			for (int i=0; i<problems.length(); i++) {
				JSONArray data = problems.getJSONArray(i);
				createProblem(data.getInt(0), data.getInt(1), 
						data.getString(2), 
						data.getInt(3), 
						data.getInt(4), 
						data.getInt(5), 
						data.getInt(12), 
						data.getInt(13), 
						data.getInt(14), 
						data.getInt(15), 
						data.getInt(16), 
						data.getInt(17), 
						data.getInt(18), 
						data.getInt(19));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
