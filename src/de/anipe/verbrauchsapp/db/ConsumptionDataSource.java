package de.anipe.verbrauchsapp.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy",
			Locale.getDefault());

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
		String queryString = "SELECT * FROM consumptions where carid=?";
		Cursor cursor = database.rawQuery(queryString,
				new String[] { String.valueOf(carId) });
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			Consumption cons = cursorToConsumption(cursor);
			consumptionCyclestList.add(cons);
			cursor.moveToNext();
		}
		cursor.close();
		Collections.sort(consumptionCyclestList);
		return consumptionCyclestList;
	}

	public Car getCarForId(long carId) {

		String queryString = "SELECT * FROM cars where _id=?";
		Cursor cursor = database.rawQuery(queryString,
				new String[] { String.valueOf(carId) });
		cursor.moveToFirst();

		if (!cursor.isAfterLast()) {
			return cursorToCar(cursor);
		}
		cursor.close();
		return null;
	}

	public int getMileageForCar(long carId) {

		String queryString = "SELECT MAX(refuelkm) FROM consumptions where carid=?";
		Cursor cursor = database.rawQuery(queryString,
				new String[] { String.valueOf(carId) });
		cursor.moveToFirst();

		int mileage = 0;

		if (!cursor.isAfterLast()) {
			mileage = cursor.getInt(0);
		} else {
			cursor.close();
			queryString = "SELECT startkm FROM cars where _id=?";
			cursor = database.rawQuery(queryString,
				new String[] { String.valueOf(carId) });
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				mileage = cursor.getInt(0);
			}
		}
		cursor.close();
		return mileage;
	}

	public double getOverallConsumptionForCar(long carId) {

		double consumption = 0;
		int cycleCount = 0;

		String queryString = "SELECT consumptionvalue FROM consumptions where carid=?";
		Cursor cursor = database.rawQuery(queryString,
				new String[] { String.valueOf(carId) });
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			consumption += cursor.getDouble(0);
			cycleCount++;
			cursor.moveToNext();
		}
		cursor.close();
		return cycleCount == 0 ? 0 : (consumption / cycleCount);
	}

	public double getOverallCostsForCar(long carId) {

		double costs = 0;

		String queryString = "SELECT liter, price FROM consumptions where carid=?";
		Cursor cursor = database.rawQuery(queryString,
				new String[] { String.valueOf(carId) });
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			double liter = cursor.getDouble(0);
			double price = cursor.getDouble(1);
			costs += (liter * price);
			cursor.moveToNext();
		}
		cursor.close();
		return costs;
	}

	public List<Car> getCarList() {

		List<Car> carList = new LinkedList<Car>();

		String queryString = "SELECT * FROM cars";
		Cursor cursor = database.rawQuery(queryString, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			Car car = cursorToCar(cursor);
			carList.add(car);
			cursor.moveToNext();
		}
		cursor.close();
		Collections.sort(carList);
		return carList;
	}

	public List<String> getCarTypesList() {

		List<String> carTypesList = new ArrayList<String>();

		String queryString = "SELECT type FROM cars";
		Cursor cursor = database.rawQuery(queryString, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			carTypesList.add(cursor.getString(0));
			cursor.moveToNext();
		}
		cursor.close();
		Collections.sort(carTypesList);
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
		String queryString = "SELECT imagedata FROM cars where _id=?";
		Cursor cursor = database.rawQuery(queryString,
				new String[] { String.valueOf(carId) });
		cursor.moveToFirst();

		Bitmap bm = null;

		if (!cursor.isAfterLast()) {
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
		values.put(DBHelper.CONSUMPTION_COLUMN_DATE,
				getDateTime(cycle.getDate()));
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
		consumption.setDate(getDatefromString(cursor.getString(2)));
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

	private String getDateTime(Date date) {
		return dateFormat.format(date);
	}

	private Date getDatefromString(String dateString) {
		try {
			return dateFormat.parse(dateString);
		} catch (ParseException e) {
		}
		return null;
	}
}
