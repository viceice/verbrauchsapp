package de.anipe.verbrauchsapp.objects;

public enum Brand {

    VW("Volkswagen"),
    BMW("BMW"),
    SKODA("Skoda"),
    FORD("Ford"),
    MERCEDES("Daimler-Benz"),
    AUDI("Audi"),
    PORSCHE("Porsche");
    
    private final String value;

    Brand(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Brand fromValue(String v) {
        for (Brand c: Brand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
