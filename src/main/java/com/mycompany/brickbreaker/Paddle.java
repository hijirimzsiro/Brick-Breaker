package com.mycompany.brickbreaker;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Paddle extends Rectangle {

    public Paddle(double x, double y, double width, double height) {
        super(x, y, width, height);
        setArcWidth(20); // 圓角
        setArcHeight(20);
        setFill(Color.web("#34d1fd")); // 藍色
        setStroke(null); // 無邊框
    }

    public void moveLeft() {
        if (getX() > 0) {
            setX(getX() - 20);
        }
    }

    public void moveRight() {
        if (getX() + getWidth() < 650) { // 這裡原本是 < 600，現在改對了
            setX(getX() + 20);
        }
    }
}
