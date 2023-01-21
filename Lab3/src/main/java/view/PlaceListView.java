package view;

import api.PlaceService;
import lombok.Getter;
import models.Place;
import models.PlaceDescription;
import utils.ApiUtils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class PlaceListView {

    @Getter
    private final JPanel panel;

    private JList<String> jPlaceList;
    private List<Place> placeList;

    public PlaceListView() {
        panel = new JPanel();
    }

    public void update(Place[] _placeList) {
        placeList = Arrays.asList(_placeList);
        Vector<String> placesNameList = Arrays.stream(_placeList)
            .map(place -> place.name)
            .collect(Collectors.toCollection(Vector::new));
        jPlaceList = new JList<>(placesNameList);
        jPlaceList.setLayoutOrientation(JList.VERTICAL);
        panel.removeAll();
        panel.add(jPlaceList);
        addEvents();
    }

    private String formatDescription(PlaceDescription description) {
        return String.format(
            "<html>" +
            "<b>Country:</b> %s<br>" +
            "<b>State:</b> %s<br>" +
            "<b>Road:</b> %s<br>" +
            "</html>",
            description.address.country,
            description.address.state,
            description.address.road
        );
    }

    private void addEvents() {
        jPlaceList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    int index = jPlaceList.locationToIndex(event.getPoint());
                    if (index >= 0) {
                        Object activeObject = jPlaceList.getModel().getElementAt(index);
                        Place activePlace = placeList
                            .stream()
                            .filter(place -> place.name.equals(activeObject.toString()))
                            .findAny()
                            .orElse(null);
                        ApiUtils.call(PlaceService.getPlaceDescription(activePlace.xid), PlaceDescription.class)
                            .thenAcceptAsync(data -> {
                                WindowView windowView = WindowView.getInstance();
                                JOptionPane.showMessageDialog(windowView.getFrame(), formatDescription(data));
                                windowView.getFrame().revalidate();
                            });
                    }
                }
            }
        });
    }
}
