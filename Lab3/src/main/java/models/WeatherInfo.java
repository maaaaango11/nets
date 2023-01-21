package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class WeatherInfo {
    public int visibility;

    @JsonProperty("weather")
    public List<General> general;
    public static final class General {
        public String main;
        public String description;
    }

    @JsonProperty("main")
    public Parameters parameters;
    public static final class Parameters {
        public int pressure;
        public int humidity;
        @JsonProperty("temp")
        public double temperature;
        @JsonProperty("feels_like")
        public double feelsLike;
        @JsonProperty("temp_min")
        public double temperatureMin;
        @JsonProperty("temp_max")
        public double temperatureMax;
        @JsonProperty("sea_level")
        public int seaLevel;
        @JsonProperty("grnd_level")
        public int groundLevel;
    }

    public Wind wind;
    public static final class Wind {
        public double speed;
        public double gust;
        @JsonProperty("deg")
        public int degree;
    }
}
