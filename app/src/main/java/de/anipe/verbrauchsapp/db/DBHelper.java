package de.anipe.verbrauchsapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	public static final String TABLE_CARS = "cars";
	public static final String TABLE_CONSUMPTIONS = "consumptions";

	public static final String COLUMN_ID = "_id";

	public static final String CAR_COLUMN_TYPE = "type";
	public static final String CAR_COLUMN_BRAND = "brand";
	public static final String CAR_COLUMN_NUMBER = "numberplate";
	public static final String CAR_COLUMN_STARTKM = "startkm";
	public static final String CAR_COLUMN_FUELTPE = "fueltype";
	public static final String CAR_COLUMN_IMAGEDATA = "imagedata";

	public static final String CONSUMPTION_CAR_ID = "carid";
	public static final String CONSUMPTION_COLUMN_DATE = "date";
	public static final String CONSUMPTION_COLUMN_REFUELMILEAGE = "refuelkm";
	public static final String CONSUMPTION_COLUMN_REFUELLITERS = "liter";
	public static final String CONSUMPTION_COLUMN_REFUELPRICE = "price";
	public static final String CONSUMPTION_COLUMN_DRIVENMILEAGE = "drivenkm";
	public static final String CONSUMPTION_COLUMN_CONSUMPTION = "consumptionvalue";

	private static final String DATABASE_NAME = "consumption.db";
	private static final int DATABASE_VERSION = 1;

	private static final String CAR_DATABASE_CREATE = "create table "
			+ TABLE_CARS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + CAR_COLUMN_TYPE
			+ " text not null, " + CAR_COLUMN_BRAND + " text not null, "
			+ CAR_COLUMN_NUMBER + " text not null, " + CAR_COLUMN_STARTKM
			+ " integer not null, " + CAR_COLUMN_FUELTPE + " text not null,"
			+ CAR_COLUMN_IMAGEDATA + " blob);";

	private static final String CONSUMPTION_DATABASE_CREATE = "create table "
			+ TABLE_CONSUMPTIONS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + CONSUMPTION_CAR_ID
			+ " integer not null, " + CONSUMPTION_COLUMN_DATE
			+ " integer not null, " + CONSUMPTION_COLUMN_REFUELMILEAGE
			+ " integer not null, " + CONSUMPTION_COLUMN_REFUELLITERS
			+ " integer not null, " + CONSUMPTION_COLUMN_REFUELPRICE
			+ " real, " + CONSUMPTION_COLUMN_DRIVENMILEAGE
			+ " integer not null, " + CONSUMPTION_COLUMN_CONSUMPTION
			+ " real not null);";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CAR_DATABASE_CREATE);
		database.execSQL(CONSUMPTION_DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONSUMPTIONS);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(), "Downgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONSUMPTIONS);
		onCreate(db);
	}
}
