package view;

import lombok.Getter;

import javax.swing.*;

public class WindowView {

    private static WindowView instance;

    @Getter
    private final JFrame frame;

    private final String WINDOW_TITLE = "Async requests app";
    private final int[] WINDOW_SIZE = new int[] {700, 500};

    private WindowView() {
        frame = new JFrame();
        frame.setTitle(WINDOW_TITLE);
        frame.setSize(WINDOW_SIZE[0], WINDOW_SIZE[1]);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(new ContainerView().getPanel());
        frame.setVisible(true);
    }

    public static WindowView getInstance() {
        if (instance == null) {
            instance = new WindowView();
        }
        return instance;
    }
}
