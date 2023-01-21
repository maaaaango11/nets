package api;

import constants.*;
import models.Address.*;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class PlaceService {

    public static String OPEN_TRIP_URL = "https://api.opentripmap.com/0.1/%s/places/%s";
    public static String OPEN_WEATHER_URL = "https://api.openweathermap.org/data/2.5/%s";

    public static Request getPlaceList(Coordinates coords, int radius) {
        String baseUrl = String.format(OPEN_TRIP_URL, Constants.LANGUAGE, "radius");
        HttpUrl url = HttpUrl
            .parse(baseUrl)
            .newBuilder()
            .addQueryParameter("lat", Double.toString(coords.lat))
            .addQueryParameter("lon", Double.toString(coords.lng))
            .addQueryParameter("radius", Double.toString(radius))
            .addQueryParameter("limit", Integer.toString(Constants.ITEMS_NUMBER))
            .addQueryParameter("format", "json")
            .addQueryParameter("apikey", EnvVariables.get("OPEN_TRIP_API_KEY"))
            .build();
        return new Request.Builder().url(url).build();
    }

    public static Request getPlaceDescription(String xid) {
        String baseUrl = String.format(
            OPEN_TRIP_URL,
            Constants.LANGUAGE,
            String.format("xid/%s", xid)
        );
        System.out.println(baseUrl);
        HttpUrl url = HttpUrl
            .parse(baseUrl)
            .newBuilder()
            .addQueryParameter("apikey", EnvVariables.get("OPEN_TRIP_API_KEY"))
            .build();
        return new Request.Builder().url(url).build();
    }

    public static Request getPlaceWeatherInfo(Coordinates coords) {
        String baseUrl = String.format(OPEN_WEATHER_URL, "weather");
        HttpUrl url = HttpUrl
            .parse(baseUrl)
            .newBuilder()
            .addQueryParameter("lat", Double.toString(coords.lat))
            .addQueryParameter("lon", Double.toString(coords.lng))
            .addQueryParameter("units", "metric")
            .addQueryParameter("appid", EnvVariables.get("OPEN_WEATHER_API_KEY"))
            .build();
        return new Request.Builder().url(url).get().build();
    }
}
