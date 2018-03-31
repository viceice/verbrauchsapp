package de.anipe.verbrauchsapp.objects;

import java.util.Date;

public class Consumption implements Comparable<Consumption> {

	private long id;
	private long carId;
	private Date date;
	private int refuelmileage;
	private double refuelliters;
	private double refuelprice;
	private int drivenmileage;
	private double consumption;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCarId() {
		return carId;
	}

	public void setCarId(long carId) {
		this.carId = carId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getRefuelmileage() {
		return refuelmileage;
	}

	public void setRefuelmileage(int refuelmileage) {
		this.refuelmileage = refuelmileage;
	}

	public double getRefuelliters() {
		return refuelliters;
	}

	public void setRefuelliters(double refuelliters) {
		this.refuelliters = refuelliters;
	}

	public double getRefuelprice() {
		return refuelprice;
	}

	public void setRefuelprice(double refuelprice) {
		this.refuelprice = refuelprice;
	}

	public int getDrivenmileage() {
		return drivenmileage;
	}

	public void setDrivenmileage(int drivenmileage) {
		this.drivenmileage = drivenmileage;
	}

	public double getConsumption() {
		return consumption;
	}

	public void setConsumption(double consumption) {
		this.consumption = consumption;
	}

	@Override
	public int compareTo(Consumption another) {
		if (this.date.getTime() < another.date.getTime()) {
			return -1;
		}
		if (this.date.getTime() > another.date.getTime()) {
			return 1;
		}
		return 0;
	}
}
