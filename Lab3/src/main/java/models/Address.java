package models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {
    public String name;
    public String country;
    public String state;
    public String town;
    public String road;
    public String street;
    @JsonProperty("countrycode")
    public String countryCode;
    @JsonProperty("postcode")
    public String postCode;
    @JsonProperty("point")
    public Coordinates coordinates;
    public static class Coordinates {
        public double lat;
        public double lng;
    }
}
