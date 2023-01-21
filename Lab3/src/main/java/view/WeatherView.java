package view;

import lombok.Getter;
import models.WeatherInfo;

import javax.swing.*;

public class WeatherView {

    @Getter
    private final JPanel panel;

    private final JLabel jWeatherInfo;

    public WeatherView() {
        panel = new JPanel();
        jWeatherInfo = new JLabel();
        panel.add(jWeatherInfo);
    }

    private String formatWeatherInfo(WeatherInfo weatherInfo) {
        return String.format(
            "<html>" +
            "<b>Temperature:</b> %s<br>" +
            "<b>Pressure:</b> %s<br>" +
            "<b>Description:</b> %s<br>" +
            "</html>",
            weatherInfo.parameters.temperature,
            weatherInfo.parameters.pressure,
            weatherInfo.general.get(0).description
        );
    }

    public void update(WeatherInfo weatherInfo) {
        jWeatherInfo.setText(formatWeatherInfo(weatherInfo));
        panel.revalidate();
    }
}
