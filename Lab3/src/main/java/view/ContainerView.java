package view;

import api.AddressService;
import lombok.Getter;
import models.AddressList;
import utils.ApiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ContainerView {

    @Getter
    private final JPanel panel;

    private final JTextField searchInput;
    private final JButton searchButton;

    private final AddressListView addressListView;
    private final PlaceListView placeListView;
    private final WeatherView weatherView;

    public ContainerView() {
        panel = new JPanel();
        searchInput = new JTextField(50);
        searchButton = new JButton("Search");
        placeListView = new PlaceListView();
        weatherView = new WeatherView();
        addressListView = new AddressListView(placeListView, weatherView);
        draw();
        addEvents();
    }

    private void draw() {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.gridheight = 1;
        panel.add(searchInput, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        panel.add(searchButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 2;
        panel.add(addressListView.getPanel(), gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        panel.add(weatherView.getPanel(), gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        panel.add(placeListView.getPanel(), gbc);
    }

    private void addEvents() {
        searchInput.addActionListener(this::handleSearchEvent);
        searchButton.addActionListener(this::handleSearchEvent);
    }

    private void handleSearchEvent(ActionEvent event) {
        updateAddressList(searchInput.getText());
    }

    public void updateAddressList(String query) {
        ApiUtils.call(AddressService.getAddressList(query), AddressList.class)
            .thenAcceptAsync(addressListView::update);
    }
}
