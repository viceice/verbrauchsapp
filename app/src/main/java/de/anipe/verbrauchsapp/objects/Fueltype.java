package de.anipe.verbrauchsapp.objects;

public enum Fueltype {

	BENZIN("Benzin"), DIESEL("Diesel"), LPG("LPG"), ETHANOL("Ethanol"), WASSERSTOFF(
			"Wasserstoff"), STROM("Strom"), SONSTIGE("Sonstige");

	private final String value;

	Fueltype(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static Fueltype fromValue(String v) {
		for (Fueltype c : Fueltype.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
