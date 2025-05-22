package com.mycompany.brickbreaker;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Brick extends Rectangle {

    public Brick(double x, double y, double width, double height, String hexColor) {
        super(x, y, width, height);
        setArcWidth(20);
        setArcHeight(20);
        setFill(Color.web(hexColor));
        setStroke(null);
    }

}
