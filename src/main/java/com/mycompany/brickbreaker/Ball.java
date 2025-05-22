package com.mycompany.brickbreaker;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Ball extends Circle {

    private double dx = 3;
    private double dy = -3;

    public Ball(double centerX, double centerY, double radius) {
        super(centerX, centerY, radius);
        setFill(Color.WHITE);
        setStroke(null);
    }

    public void move() {
        setCenterX(getCenterX() + dx);
        setCenterY(getCenterY() + dy);
    }

    public void reverseX() {
        dx = -dx;
    }

    public void reverseY() {
        dy = -dy;
    }

    public void speedUp(double factor) {
        dx *= factor;
        dy *= factor;

        // 限速：最大速度為 6
        double maxSpeed = 6;

        // 根據方向限制速度大小
        if (Math.abs(dx) > maxSpeed) {
            dx = maxSpeed * Math.signum(dx);
        }
        if (Math.abs(dy) > maxSpeed) {
            dy = maxSpeed * Math.signum(dy);
        }
    }
}
