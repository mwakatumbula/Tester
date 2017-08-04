package com.hover.tester.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 4;
	public static final String DATABASE_NAME = "services.db";

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private static final String SERVICE_TABLE_CREATE = "create table "
			+ Contract.OperatorServiceEntry.TABLE_NAME + "("
			+ Contract.OperatorServiceEntry.COLUMN_ENTRY_ID + " integer primary key autoincrement, "
			+ Contract.OperatorServiceEntry.COLUMN_SERVICE_ID + " integer not null, "
			+ Contract.OperatorServiceEntry.COLUMN_NAME + " text not null, "
			+ Contract.OperatorServiceEntry.COLUMN_SLUG + " text not null, "
			+ Contract.OperatorServiceEntry.COLUMN_COUNTRY + " text not null, "
			+ Contract.OperatorServiceEntry.COLUMN_CURRENCY + " text not null, "
			+ Contract.OperatorServiceEntry.COLUMN_ACTIONS + " text, "
			+ "UNIQUE (" + Contract.OperatorServiceEntry.COLUMN_SERVICE_ID + ") ON CONFLICT REPLACE"
			+ ");";

	private static final String ACTION_TABLE_CREATE = "create table "
			+ Contract.OperatorActionEntry.TABLE_NAME + "("
			+ Contract.OperatorActionEntry.COLUMN_ENTRY_ID + " integer primary key autoincrement, "
			+ Contract.OperatorActionEntry.COLUMN_NAME + " text not null, "
			+ Contract.OperatorActionEntry.COLUMN_SLUG + " text not null, "
			+ Contract.OperatorActionEntry.COLUMN_SERVICE_ID + " integer not null, "
			+ Contract.OperatorActionEntry.COLUMN_VARIABLES + " text, "
			+ "UNIQUE (" + Contract.OperatorActionEntry.COLUMN_SERVICE_ID + ", " + Contract.OperatorActionEntry.COLUMN_SLUG + ") ON CONFLICT REPLACE"
			+ ");";

	private static final String VARIABLE_TABLE_CREATE = "create table "
			+ Contract.ActionVariableEntry.TABLE_NAME + "("
			+ Contract.ActionVariableEntry.COLUMN_ENTRY_ID + " integer primary key autoincrement, "
			+ Contract.ActionVariableEntry.COLUMN_ACTION_ID + " integer not null, "
			+ Contract.ActionVariableEntry.COLUMN_NAME + " text not null, "
			+ Contract.ActionVariableEntry.COLUMN_VALUE + " text, "
			+ "UNIQUE (" + Contract.ActionVariableEntry.COLUMN_ACTION_ID + ", " + Contract.ActionVariableEntry.COLUMN_NAME + ") ON CONFLICT REPLACE"
			+ ");";

	private static final String RESULT_TABLE_CREATE = "create table "
			+ Contract.ActionResultEntry.TABLE_NAME + "("
			+ Contract.ActionResultEntry.COLUMN_ENTRY_ID + " integer primary key autoincrement, "
			+ Contract.ActionResultEntry.COLUMN_SDK_ID + " integer not null, "
			+ Contract.ActionResultEntry.COLUMN_ACTION_ID + " integer not null, "
			+ Contract.ActionResultEntry.COLUMN_TEXT + " text not null, "
			+ Contract.ActionResultEntry.COLUMN_STATUS + " integer not null, "
			+ Contract.ActionResultEntry.COLUMN_TIMESTAMP + " text not null, "
			+ Contract.ActionResultEntry.COLUMN_RETURN_VALUES + " text, "
			+ "UNIQUE (" + Contract.ActionResultEntry.COLUMN_SDK_ID + ") ON CONFLICT REPLACE"
			+ ");";

	private static final String SQL_DELETE_SERVICES = "DROP TABLE IF EXISTS " + Contract.OperatorServiceEntry.TABLE_NAME;
	private static final String SQL_DELETE_ACTIONS = "DROP TABLE IF EXISTS " + Contract.OperatorActionEntry.TABLE_NAME;
	private static final String SQL_DELETE_VARIABLES = "DROP TABLE IF EXISTS " + Contract.ActionVariableEntry.TABLE_NAME;
	private static final String SQL_DELETE_RESULTS = "DROP TABLE IF EXISTS " + Contract.ActionResultEntry.TABLE_NAME;

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SERVICE_TABLE_CREATE);
		db.execSQL(ACTION_TABLE_CREATE);
		db.execSQL(VARIABLE_TABLE_CREATE);
		db.execSQL(RESULT_TABLE_CREATE);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_SERVICES);
		db.execSQL(SQL_DELETE_ACTIONS);
		db.execSQL(SQL_DELETE_VARIABLES);
		db.execSQL(SQL_DELETE_RESULTS);
		onCreate(db);
	}
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}
