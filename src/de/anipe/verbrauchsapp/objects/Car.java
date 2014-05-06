package de.anipe.verbrauchsapp.objects;

import android.graphics.Bitmap;
import de.anipe.verbrauchsapp.objects.Fueltype;

public class Car implements Comparable<Car> {

	private long carId;
	private String type;
	private Brand brand;
	private String numberPlate;
	private Fueltype fuelType;
	private int startKm;
	private Bitmap icon;
	private Bitmap image;

	public Car() {
	};

	public Car(String type, Brand brand, String numberPlate, Fueltype fuelType,
			int startKm, Bitmap icon) {
		this.carId = -1;
		this.type = type;
		this.numberPlate = numberPlate;
		this.fuelType = fuelType;
		this.startKm = startKm;
		this.icon = icon;
	}

	public long getCarId() {
		return carId;
	}

	public void setCarId(long carId) {
		this.carId = carId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Brand getBrand() {
		return brand;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}

	public String getNumberPlate() {
		return numberPlate;
	}

	public void setNumberPlate(String numberPlate) {
		this.numberPlate = numberPlate;
	}

	public Fueltype getFuelType() {
		return fuelType;
	}

	public void setFuelType(Fueltype fuelType) {
		this.fuelType = fuelType;
	}

	public int getStartKm() {
		return startKm;
	}

	public void setStartKm(int startKm) {
		this.startKm = startKm;
	}

	@Override
	public int compareTo(Car another) {
		return this.type.compareTo(another.type);
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}
}
