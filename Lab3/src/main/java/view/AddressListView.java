package view;

import api.PlaceService;
import lombok.Getter;
import models.Address;
import models.AddressList;
import models.Place;
import models.WeatherInfo;
import utils.ApiUtils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.stream.Collectors;

public class AddressListView {

    @Getter
    private final JPanel panel;

    private final PlaceListView placeListView;
    private final WeatherView weatherView;
    private JList<String> jAddressList;
    private AddressList addressList;

    public AddressListView(PlaceListView _placeListView, WeatherView _weatherView) {
        panel = new JPanel();
        placeListView = _placeListView;
        weatherView = _weatherView;
    }

    public void update(AddressList _addressList) {
        addressList = _addressList;
        Vector<String> addressesNameList = addressList.hits
            .stream()
            .map(address -> address.name)
            .collect(Collectors.toCollection(Vector::new));
        jAddressList = new JList<>(addressesNameList);
        jAddressList.setLayoutOrientation(JList.VERTICAL);
        panel.removeAll();
        panel.add(jAddressList);
        addEvents();
    }

    private void addEvents() {
        jAddressList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    int index = jAddressList.locationToIndex(event.getPoint());
                    if (index >= 0) {
                        Object activeObject = jAddressList.getModel().getElementAt(index);
                        Address activeAddress = addressList.hits
                            .stream()
                            .filter(address -> address.name.equals(activeObject.toString()))
                            .findAny()
                            .orElse(null);
                        updatePlaceListAndWeather(activeAddress.coordinates);
                    }
                }
            }
        });
    }

    public void updatePlaceListAndWeather(Address.Coordinates coordinates) {
        ApiUtils.call(PlaceService.getPlaceList(coordinates, 1000), Place[].class)
            .thenAcceptAsync(placeListView::update);
        ApiUtils.call(PlaceService.getPlaceWeatherInfo(coordinates), WeatherInfo.class)
            .thenAcceptAsync(weatherView::update);
    }
}
