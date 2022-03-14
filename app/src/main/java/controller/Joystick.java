package controller;

public class Joystick {

    private int outerCircleRadius;
    private int innerCircleRadius;
    private int outerCircleCenterPositionX;
    private int outerCircleCenterPositionY;
    private int innerCircleCenterPositionX;
    private int innerCircleCenterPositionY;

    public Joystick(int centerPositionX, int centerPositionY, int outerCircleRadiys, int innerCircleRadiys){

        //To make up the joystick
        this.outerCircleCenterPositionX = centerPositionX;
        this.outerCircleCenterPositionY = centerPositionY;
        this.innerCircleCenterPositionX = centerPositionX;
        this.innerCircleCenterPositionY = centerPositionY;

        //Radii of circles
        this.outerCircleRadius = outerCircleRadiys;
        this.outerCircleRadius = innerCircleRadiys;

        //paint of circles

    }

    public void draw(){

    }

    public void update(){

    }
}
