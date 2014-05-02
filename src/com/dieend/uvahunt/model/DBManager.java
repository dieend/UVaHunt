package com.dieend.uvahunt.model;

import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.dieend.uvahunt.UvaHuntActivity;

public class DBManager {
	private static class DBHelper  extends SQLiteOpenHelper {
		private static final int DATABASE_VERSION = 8;
		public static final String DB_NAME = UvaHuntActivity.TAG + ".DATABASE";
		
		public static final String PROBLEM_TABLE = "problem";
		public static final String PROBLEM_COLUMN_ID = "id"; //0
		public static final String PROBLEM_COLUMN_NUMBER = "number"; //1
		public static final String PROBLEM_COLUMN_TITLE = "title"; //2
		public static final String PROBLEM_COLUMN_DACU = "dacu"; //3
		public static final String PROBLEM_COLUMN_BEST_RUNTIME = "best_runtime"; //4
		public static final String PROBLEM_COLUMN_BEST_MEMORY = "best_memory"; //5
		public static final String PROBLEM_COLUMN_COMPILE_ERROR = "nce"; //5
		public static final String PROBLEM_COLUMN_RUNTIME_ERROR = "nre";//12
		public static final String PROBLEM_COLUMN_OUTPUT_LIMIT = "nole";//13
		public static final String PROBLEM_COLUMN_TIME_LIMIT = "ntle";//14
		public static final String PROBLEM_COLUMN_MEMORY_LIMIT = "nmle";//15
		public static final String PROBLEM_COLUMN_WRONG_ANSWER = "nwa"; //16
		public static final String PROBLEM_COLUMN_PRESENTATION_ERROR = "npe";//17
		public static final String PROBLEM_COLUMN_ACCEPTED = "nac"; //18
		public static final String PROBLEM_COLUMN_RUNTIME_LIMIT = "runtime_limit"; // 19
		public static final String[] PROBLEM_COLUMNS = {PROBLEM_COLUMN_ID,
			PROBLEM_COLUMN_NUMBER,
			PROBLEM_COLUMN_TITLE,
			PROBLEM_COLUMN_DACU,
			PROBLEM_COLUMN_BEST_RUNTIME,
			PROBLEM_COLUMN_RUNTIME_LIMIT,
			PROBLEM_COLUMN_BEST_MEMORY,
			PROBLEM_COLUMN_COMPILE_ERROR,
			PROBLEM_COLUMN_RUNTIME_ERROR,
			PROBLEM_COLUMN_OUTPUT_LIMIT,
			PROBLEM_COLUMN_TIME_LIMIT,
			PROBLEM_COLUMN_MEMORY_LIMIT,
			PROBLEM_COLUMN_WRONG_ANSWER,
			PROBLEM_COLUMN_PRESENTATION_ERROR,
			PROBLEM_COLUMN_ACCEPTED};
		public static final String SUBMISSION_TABLE = "submission";
		public static final String SUBMISSION_ID = "id";
		public static final String SUBMISSION_PROBLEM_ID ="problem_id";
		public static final String SUBMISSION_VERDICT_ID ="verdict";
		public static final String SUBMISSION_RUNTIME ="runtime";
		public static final String SUBMISSION_SUBMIT_TIME ="submit_time";
		public static final String SUBMISSION_LANG ="lang";
		public static final String SUBMISSION_RANK ="rank";
		public static final String[] SUBMISSION_COLUMNS = {
			 SUBMISSION_ID,
			 SUBMISSION_PROBLEM_ID,
			 SUBMISSION_VERDICT_ID,
			 SUBMISSION_RUNTIME,
			 SUBMISSION_SUBMIT_TIME,
			 SUBMISSION_LANG,
			 SUBMISSION_RANK
		};
		private static final String CREATE_TABLE_SUBMISSION = "create table "
			      + SUBMISSION_TABLE + "(" + 
			      SUBMISSION_ID + " INTEGER primary key," + 
			      SUBMISSION_PROBLEM_ID + " INTEGER, " +
			      SUBMISSION_VERDICT_ID + " INTEGER, " +
			      SUBMISSION_RUNTIME + " INTEGER, " +
			      SUBMISSION_SUBMIT_TIME + " INTEGER, " +
			      SUBMISSION_LANG + " INTEGER, " +
			      SUBMISSION_RANK + " INTEGER);";

		private static final String CREATE_TABLE_PROBLEM = "create table "
			      + PROBLEM_TABLE + "(" + 
				PROBLEM_COLUMN_ID + " INTEGER primary key," + 
			    PROBLEM_COLUMN_NUMBER + " INTEGER, " +
			    PROBLEM_COLUMN_TITLE + " TEXT, " +
				PROBLEM_COLUMN_DACU + " INTEGER, " +
				PROBLEM_COLUMN_BEST_RUNTIME + " INTEGER, " +
				PROBLEM_COLUMN_RUNTIME_LIMIT + " INTEGER, " +
				PROBLEM_COLUMN_BEST_MEMORY + " INTEGER, " +
				PROBLEM_COLUMN_COMPILE_ERROR + " INTEGER, " +
				PROBLEM_COLUMN_RUNTIME_ERROR + " INTEGER, " +
				PROBLEM_COLUMN_OUTPUT_LIMIT + " INTEGER, " +
				PROBLEM_COLUMN_TIME_LIMIT + " INTEGER, " +
				PROBLEM_COLUMN_MEMORY_LIMIT + " INTEGER, " +
				PROBLEM_COLUMN_WRONG_ANSWER + " INTEGER, " +
				PROBLEM_COLUMN_PRESENTATION_ERROR + " INTEGER, " +
				PROBLEM_COLUMN_ACCEPTED + " INTEGER);";
		
		public DBHelper(Context context) {
			super(context, DB_NAME, null, DATABASE_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			  db.execSQL(CREATE_TABLE_PROBLEM);
			  db.execSQL(CREATE_TABLE_SUBMISSION);
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + PROBLEM_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SUBMISSION_TABLE);
		    onCreate(db);
		}
	}
	private static DBManager instance = null;
	SharedPreferences sp;
	private DBManager(Context context){
		sp = context.getSharedPreferences(UvaHuntActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
		dbHelper = new DBHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private static boolean ready = false;
	public static DBManager $() {
		if ((instance == null) || !ready) {
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
	public static void close(){
		if (instance != null) {
			instance.db.close();
			instance = null;
			ready = false;
		}
	}
	private Map<Integer, Submission> submissionById = new TreeMap<Integer,Submission>();;
	public void resetSubmission() {
		db.beginTransaction();
		try {
			db.delete(DBHelper.SUBMISSION_TABLE, null, null);
			db.setTransactionSuccessful();
			Problem.reset();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	public void populateSubmission(String json, int uid) {
		db.beginTransaction();
		try {
			JSONObject jsonObj = new JSONObject(json);
			JSONArray jsonSubmission = jsonObj.getJSONArray("subs");
			for (int i=0; i<jsonSubmission.length(); i++) {
				JSONArray data = jsonSubmission.getJSONArray(i);
				createSubmission(data.getInt(0),
						uid,
						data.getInt(1),
						data.getInt(2),
						data.getInt(3),
						data.getLong(4),
						data.getInt(5),
						data.getInt(6)
					);
			}
			db.setTransactionSuccessful();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		queryAllSubmission(uid);
		
	}

	public Map<Integer, Submission> updateSubmissionFromLiveSubmission(JSONArray all, int uid) {
		Map<Integer, Submission> ret = new TreeMap<Integer, Submission>();
		try {
			for (int i=0; i<all.length(); i++) {
				JSONObject event = all.getJSONObject(i);
				JSONObject submission = event.getJSONObject("msg");
				Submission s = new Submission(submission.getInt("sid"), submission.getInt("uid"), submission.getInt("pid"), submission.getInt("ver"), 
						submission.getInt("run"), submission.getInt("sbt"), submission.getInt("lan"), submission.getInt("rank"));
				if (submission.getInt("uid") == uid) {
					createSubmission(s);
				} 
				ret.put(s.id, s);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}
	private Submission createSubmission(int id, int uid, int problemId, int verdict, int runtime,
			long submitTime, int lang, int rank) {
		return createSubmission(new Submission(id, uid, problemId, verdict, runtime, submitTime, lang, rank));
	}
	private Submission createSubmission(Submission submission) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.SUBMISSION_ID,submission.id);
		values.put(DBHelper.SUBMISSION_PROBLEM_ID,submission.problemId);
		values.put(DBHelper.SUBMISSION_VERDICT_ID,submission.verdict);
		values.put(DBHelper.SUBMISSION_RUNTIME,submission.runtime);
		values.put(DBHelper.SUBMISSION_SUBMIT_TIME,submission.submitTime);
		values.put(DBHelper.SUBMISSION_LANG,submission.lang);
		values.put(DBHelper.SUBMISSION_RANK,submission.rank);
		db.insertWithOnConflict(DBHelper.SUBMISSION_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		submissionById.put(submission.id, submission);
		Problem.tried(submission.problemId);
		if (submission.isAccepted()) {
			Problem.solve(submission.problemId);
		}
		return submission;
	}
	public Map<Integer,Submission> getAllSubmission() {
		return new TreeMap<Integer,Submission>(submissionById);
	}
	private Submission cursorToSubmission(Cursor cursor, int uid) {
		Submission ret = new Submission(cursor.getInt(0),
				uid,
				cursor.getInt(1), 
				cursor.getInt(2), 
				cursor.getInt(3), 
				cursor.getLong(4), 
				cursor.getInt(5), 
				cursor.getInt(6));
		return ret;
	}

	public boolean queryAllSubmission(int uid) {
		submissionById = new TreeMap<Integer,Submission>();
		Cursor cursor = db.query(DBHelper.SUBMISSION_TABLE, DBHelper.SUBMISSION_COLUMNS, null, null, null, null,null);
		boolean ret = cursor.moveToFirst();
		if (ret) {
			while (!cursor.isAfterLast()) {
				Submission s = cursorToSubmission(cursor, uid);
				submissionById.put(s.id, s);
				Problem.tried(s.problemId);
				if (s.isAccepted()) {
					Problem.solve(s.problemId);
				}
				cursor.moveToNext();
			}
		}
		return ret;
	}
	public Submission getLastSubmission(int uid) {
		Cursor cursor = db.rawQuery("SELECT * FROM "+ DBHelper.SUBMISSION_TABLE +" ORDER BY "+ DBHelper.SUBMISSION_ID +" DESC LIMIT 1;",null );
		if (cursor.moveToFirst()) {
			return cursorToSubmission(cursor, uid);
		} else {
			return null;
		}
	}
	public void populateProblem(String json) {
		problemsById = new SparseArray<Problem>();
		problemsNumToId = new SparseIntArray();
		db.beginTransaction();
		try {
			JSONArray jsonProblems = new JSONArray(json);
			for (int i=0; i<jsonProblems.length(); i++) {
				JSONArray data = jsonProblems.getJSONArray(i);
				createProblem(data.getInt(0), // id
						data.getInt(1), // number
						data.getString(2), // title
						data.getInt(3), // dacu
						data.getInt(4), // best RT
						data.getInt(5), // best Mem
						data.getInt(10), // nCE
						data.getInt(12), // nRE
						data.getInt(13), // nOL
						data.getInt(14), // nTL
						data.getInt(15), // nML
						data.getInt(16), // nWA
						data.getInt(17), // nPE
						data.getInt(18), // nAC
						data.getInt(19)); // TL
			}
			db.setTransactionSuccessful();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		queryAllProblem();
	}
	private Problem createProblem(int id, int number, String title, int dacu, int bestRuntime, 
								int bestMemory, int nCE,int nRE, int nOLE, int nTLE, int nMLE, 
								int nWA, int nPE, int nAC, int timeLimit){
		ContentValues values = new ContentValues();
	    values.put(DBHelper.PROBLEM_COLUMN_ID, id);
	    values.put(DBHelper.PROBLEM_COLUMN_NUMBER, number);
	    values.put(DBHelper.PROBLEM_COLUMN_TITLE, title);
	    values.put(DBHelper.PROBLEM_COLUMN_DACU, dacu);
	    values.put(DBHelper.PROBLEM_COLUMN_BEST_RUNTIME, bestRuntime);
	    values.put(DBHelper.PROBLEM_COLUMN_BEST_MEMORY, bestMemory);
	    values.put(DBHelper.PROBLEM_COLUMN_COMPILE_ERROR, nCE);
	    values.put(DBHelper.PROBLEM_COLUMN_RUNTIME_ERROR, nRE);
	    values.put(DBHelper.PROBLEM_COLUMN_OUTPUT_LIMIT, nOLE);
	    values.put(DBHelper.PROBLEM_COLUMN_TIME_LIMIT, nTLE);
	    values.put(DBHelper.PROBLEM_COLUMN_MEMORY_LIMIT, nMLE);
	    values.put(DBHelper.PROBLEM_COLUMN_WRONG_ANSWER, nWA);
	    values.put(DBHelper.PROBLEM_COLUMN_PRESENTATION_ERROR, nPE);
	    values.put(DBHelper.PROBLEM_COLUMN_ACCEPTED, nAC);
	    values.put(DBHelper.PROBLEM_COLUMN_RUNTIME_LIMIT, timeLimit);
	    db.insertWithOnConflict(DBHelper.PROBLEM_TABLE, null,values, SQLiteDatabase.CONFLICT_REPLACE);
	    return new Problem(
	    		id,
	    	    number,
	    	    title,
	    	    dacu,
	    	    bestRuntime,
	    	    bestMemory,
	    	    nCE,
	    	    nRE,
	    	    nOLE,
	    	    nTLE,
	    	    nMLE,
	    	    nWA,
	    	    nPE,
	    	    nAC,
	    	    timeLimit);
	}
	
	
	private SparseArray<Problem> problemsById = null;
	private SparseIntArray problemsNumToId = null;
	public boolean queryAllProblem() {
		Cursor cursor = db.query(DBHelper.PROBLEM_TABLE, DBHelper.PROBLEM_COLUMNS, null, null, null, null, null);
		boolean ret = cursor.moveToFirst();
		if (ret) {
			problemsById = new SparseArray<Problem>();
			problemsNumToId = new SparseIntArray();
			while (!cursor.isAfterLast()) {
				Problem p = cursorToProblem(cursor);
				problemsById.put(p.id, p);
				problemsNumToId.put(p.number, p.id);
				cursor.moveToNext();
			}
		}
		return ret;
	}
	public Problem getProblemsById(int id) {
		if (problemsById == null) throw new RuntimeException("problems not initiated");
		return problemsById.get(id);
	}
	public Problem getProblemsByNum(int num) {
		if (problemsById == null) throw new RuntimeException("problems not initiated");
		return problemsById.get(problemsNumToId.get(num));
	}
	private Problem cursorToProblem(Cursor cursor) {
		Problem ret = new Problem(cursor.getInt(0), 
				cursor.getInt(1), 
				cursor.getString(2), 
				cursor.getInt(3), 
				cursor.getInt(4), 
				cursor.getInt(5), 
				cursor.getInt(6), 
				cursor.getInt(7), 
				cursor.getInt(8), 
				cursor.getInt(9), 
				cursor.getInt(10), 
				cursor.getInt(11), 
				cursor.getInt(12), 
				cursor.getInt(13),
				cursor.getInt(14));
		return ret;
	}
}
