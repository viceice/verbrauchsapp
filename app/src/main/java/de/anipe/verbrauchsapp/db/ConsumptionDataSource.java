package de.anipe.verbrauchsapp.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.objects.Brand;
import de.anipe.verbrauchsapp.objects.Car;
import de.anipe.verbrauchsapp.objects.Consumption;
import de.anipe.verbrauchsapp.objects.Fueltype;

public class ConsumptionDataSource implements Serializable {

	private static ConsumptionDataSource dataSouce;

	private static final long serialVersionUID = 368016508421825334L;
	private SQLiteDatabase database;
	private DBHelper dbHelper;
	private FileSystemAccessor accessor;
	private Context context;

	private ConsumptionDataSource(Context context) {
		dbHelper = new DBHelper(context);
		accessor = FileSystemAccessor.getInstance();
		this.context = context;
	}

	public static ConsumptionDataSource getInstance(Context context) {
		if (dataSouce == null) {
			dataSouce = new ConsumptionDataSource(context);
		}

		return dataSouce;
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public List<Consumption> getConsumptionCycles(long carId) {

		List<Consumption> consumptionCyclestList = new LinkedList<Consumption>();
		Cursor cursor = database.query(DBHelper.TABLE_CONSUMPTIONS, null,
				DBHelper.CONSUMPTION_CAR_ID + "=?",
				new String[] { String.valueOf(carId) }, null, null,
				DBHelper.CONSUMPTION_COLUMN_DATE);

		while (cursor.moveToNext()) {
			Consumption cons = cursorToConsumption(cursor);
			consumptionCyclestList.add(cons);
		}
		cursor.close();

		return consumptionCyclestList;
	}

	public Car getCarForId(long carId) {

		Cursor cursor = database.query(DBHelper.TABLE_CARS, null,
				DBHelper.COLUMN_ID + "=?",
				new String[] { String.valueOf(carId) }, null, null, null, "1");

		if (cursor.moveToFirst()) {
			return cursorToCar(cursor);
		}
		cursor.close();
		return null;
	}

	public int getMileageForCar(long carId) {

		Cursor cursor = database.query(DBHelper.TABLE_CONSUMPTIONS,
				new String[] { "MAX("
						+ DBHelper.CONSUMPTION_COLUMN_REFUELMILEAGE + ")" },
				DBHelper.CONSUMPTION_CAR_ID + "=?",
				new String[] { String.valueOf(carId) }, null, null, null, "1");

		int mileage = 0;

		if (cursor.moveToFirst() && !cursor.isNull(0)) {
			mileage = cursor.getInt(0);
			Log.d("ConsumptionDataSource", "Found mileage: " + mileage);
		} else {
			cursor.close();
			cursor = database.query(DBHelper.TABLE_CARS,
					new String[] { DBHelper.CAR_COLUMN_STARTKM },
					DBHelper.COLUMN_ID + "=?",
					new String[] { String.valueOf(carId) }, null, null, null,
					"1");
			if (cursor.moveToFirst()) {
				mileage = cursor.getInt(0);
				Log.d("ConsumptionDataSource", "Found start mileage: "
						+ mileage);
			}
		}
		cursor.close();
		return mileage;
	}

	public double getOverallConsumptionForCar(long carId) {

		double consumption = 0;
		int cycleCount = 0;

		Cursor cursor = database.query(DBHelper.TABLE_CONSUMPTIONS,
				new String[] {
						"SUM(" + DBHelper.CONSUMPTION_COLUMN_CONSUMPTION + ")",
						"COUNT(" + DBHelper.COLUMN_ID + ")" },
				DBHelper.CONSUMPTION_CAR_ID + "=?",
				new String[] { String.valueOf(carId) }, null, null, null);

		if (cursor.moveToFirst() && !cursor.isNull(0)) {
			consumption = cursor.getDouble(0);
			cycleCount = cursor.getInt(1);
		}
		cursor.close();
		return cycleCount == 0 ? 0 : (consumption / cycleCount);
	}

	public double getOverallCostsForCar(long carId) {

		double costs = 0;

		Cursor cursor = database.query(DBHelper.TABLE_CONSUMPTIONS,
				new String[] { "SUM("
						+ DBHelper.CONSUMPTION_COLUMN_REFUELLITERS + "*"
						+ DBHelper.CONSUMPTION_COLUMN_REFUELPRICE + ")" },
				DBHelper.CONSUMPTION_CAR_ID + "=?",
				new String[] { String.valueOf(carId) }, null, null, null);

		if (cursor.moveToFirst() && !cursor.isNull(0)) {
			costs = cursor.getDouble(0);
		}
		cursor.close();
		return costs;
	}

	public List<Car> getCarList() {

		List<Car> carList = new LinkedList<Car>();

		Cursor cursor = database.query(DBHelper.TABLE_CARS, null, null, null,
				null, null, DBHelper.CAR_COLUMN_TYPE);

		while (cursor.moveToNext()) {
			Car car = cursorToCar(cursor);
			carList.add(car);
		}
		cursor.close();
		return carList;
	}

	public List<String> getCarTypesList() {

		List<String> carTypesList = new ArrayList<String>();

		Cursor cursor = database.query(DBHelper.TABLE_CARS,
				new String[] { DBHelper.CAR_COLUMN_TYPE }, null, null, null,
				null, DBHelper.CAR_COLUMN_TYPE);

		while (cursor.moveToNext()) {
			carTypesList.add(cursor.getString(0));
		}
		cursor.close();
		return carTypesList;
	}

	public long addCar(Car car) {

		ContentValues values = new ContentValues();
		values.put(DBHelper.CAR_COLUMN_TYPE, car.getType());
		values.put(DBHelper.CAR_COLUMN_BRAND, car.getBrand().value());
		values.put(DBHelper.CAR_COLUMN_NUMBER, car.getNumberPlate());
		values.put(DBHelper.CAR_COLUMN_STARTKM, car.getStartKm());
		values.put(DBHelper.CAR_COLUMN_FUELTPE, car.getFuelType().value());
		values.put(DBHelper.CAR_COLUMN_IMAGEDATA,
				getByteArrayForBitMap(car.getImage()));

		try {
			return database.insertOrThrow(DBHelper.TABLE_CARS, null, values);
		} catch (android.database.SQLException sqe) {
			sqe.printStackTrace();
			return -1;
		}
	}

	public long updateCar(Car car) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.CAR_COLUMN_TYPE, car.getType());
		values.put(DBHelper.CAR_COLUMN_BRAND, car.getBrand().value());
		values.put(DBHelper.CAR_COLUMN_NUMBER, car.getNumberPlate());
		values.put(DBHelper.CAR_COLUMN_STARTKM, car.getStartKm());
		values.put(DBHelper.CAR_COLUMN_FUELTPE, car.getFuelType().value());
		values.put(DBHelper.CAR_COLUMN_IMAGEDATA,
				getByteArrayForBitMap(car.getImage()));

		return database.update(DBHelper.TABLE_CARS, values, DBHelper.COLUMN_ID
				+ "=?", new String[] { String.valueOf(car.getCarId()) });
	}

	public int deleteCar(Car car) {
		try {
			return database.delete(DBHelper.TABLE_CARS, DBHelper.COLUMN_ID
					+ "=?", new String[] { String.valueOf(car.getCarId()) });
		} catch (android.database.SQLException sqe) {
			sqe.printStackTrace();
			return -1;
		}
	}

	public long storeImageForCar(long carId, Bitmap bitmap) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.CAR_COLUMN_IMAGEDATA, getByteArrayForBitMap(bitmap));
		return database.update(DBHelper.TABLE_CARS, values, DBHelper.COLUMN_ID
				+ "=?", new String[] { String.valueOf(carId) });
	}

	public Bitmap getImageForCarId(long carId) {

		Cursor cursor = database.query(DBHelper.TABLE_CARS,
				new String[] { DBHelper.CAR_COLUMN_IMAGEDATA },
				DBHelper.COLUMN_ID + "=?",
				new String[] { String.valueOf(carId) }, null, null, null, "1");

		Bitmap bm = null;

		if (cursor.moveToFirst()) {
			bm = getBitMapForByteArray(cursor.getBlob(0));
		}
		cursor.close();
		return bm;
	}

	public int deleteConsumption(long consumptionId) {
		try {
			return database.delete(DBHelper.TABLE_CONSUMPTIONS,
					DBHelper.COLUMN_ID + "=" + consumptionId, null);
		} catch (android.database.SQLException sqe) {
			sqe.printStackTrace();
			return -1;
		}
	}

	public int deleteConsumptionsForCar(long carId) {
		try {
			return database.delete(DBHelper.TABLE_CONSUMPTIONS,
					DBHelper.CONSUMPTION_CAR_ID + "=" + carId, null);
		} catch (android.database.SQLException sqe) {
			sqe.printStackTrace();
			return -1;
		}
	}

	public void addConsumptions(List<Consumption> consumptionCyclesList) {
		for (Consumption cycle : consumptionCyclesList) {
			addConsumption(cycle);
		}
	}

	public long addConsumption(Consumption cycle) {

		ContentValues values = new ContentValues();
		values.put(DBHelper.CONSUMPTION_CAR_ID, cycle.getCarId());
		values.put(DBHelper.CONSUMPTION_COLUMN_DATE, cycle.getDate().getTime());
		values.put(DBHelper.CONSUMPTION_COLUMN_REFUELMILEAGE,
				cycle.getRefuelmileage());
		values.put(DBHelper.CONSUMPTION_COLUMN_REFUELLITERS,
				cycle.getRefuelliters());
		values.put(DBHelper.CONSUMPTION_COLUMN_REFUELPRICE,
				cycle.getRefuelprice());
		values.put(DBHelper.CONSUMPTION_COLUMN_DRIVENMILEAGE,
				cycle.getDrivenmileage());
		values.put(DBHelper.CONSUMPTION_COLUMN_CONSUMPTION,
				cycle.getConsumption());

		try {
			return database.insertOrThrow(DBHelper.TABLE_CONSUMPTIONS, null,
					values);
		} catch (android.database.SQLException sqe) {
			return -1;
		}
	}

	private Car cursorToCar(Cursor cursor) {
		Car car = new Car();

		car.setCarId(cursor.getLong(0));
		car.setType(cursor.getString(1));
		car.setBrand(Brand.fromValue(cursor.getString(2)));
		car.setNumberPlate(cursor.getString(3));
		car.setStartKm(cursor.getInt(4));
		car.setFuelType(Fueltype.fromValue(cursor.getString(5)));
		car.setImage(getBitMapForByteArray(cursor.getBlob(6)));
		car.setIcon(accessor.getBitmapForBrand(context, car.getBrand()));

		return car;
	}

	private Consumption cursorToConsumption(Cursor cursor) {
		Consumption consumption = new Consumption();

		consumption.setId(cursor.getLong(0));
		consumption.setCarId(cursor.getLong(1));
		consumption.setDate(new Date(cursor.getLong(2)));
		consumption.setRefuelmileage(cursor.getInt(3));
		consumption.setRefuelliters(cursor.getDouble(4));
		consumption.setRefuelprice(cursor.getDouble(5));
		consumption.setDrivenmileage(cursor.getInt(6));
		consumption.setConsumption(cursor.getDouble(7));

		return consumption;
	}

	private byte[] getByteArrayForBitMap(Bitmap image) {
		if (image == null) {
			return new byte[0];
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, bos);
		return bos.toByteArray();
	}

	private Bitmap getBitMapForByteArray(byte[] blob) {
		ByteArrayInputStream imageStream = new ByteArrayInputStream(blob);
		return BitmapFactory.decodeStream(imageStream);
	}
}
