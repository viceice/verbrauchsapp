package de.anipe.verbrauchsapp.objects;

public enum Brand {

	AUDI("Audi"),
	BMW("BMW"),
	FORD("Ford"),
	HONDA("Honda"),
	MAZDA("Mazda"),
	MERCEDES("Daimler-Benz"),
	NISSAN("Nissan"),
	PORSCHE("Porsche"),
	SKODA("Skoda"),
	TOYOTA("Toyota"),
	VW("Volkswagen");

	private final String value;

	Brand(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static Brand fromValue(String v) {
		for (Brand c : Brand.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
