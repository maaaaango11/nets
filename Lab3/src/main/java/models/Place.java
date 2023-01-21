package models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Place {
    public String xid;
    public String name;
    public String kinds;

    @JsonProperty("point")
    public Address.Coordinates coordinates;
}
