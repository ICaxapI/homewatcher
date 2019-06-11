package ru.exsoft.utils;

public class MathUtils {

    public static int[] getExternalRectangle(int widthInternal, int heightInternal, double angle){
        double[] firstTriangle = getTriangle(heightInternal, 90 - angle);
        double[] secondTriangle = getTriangle(widthInternal, 90 - angle);
        return new int[]{
                (int) Math.abs(Math.ceil(firstTriangle[1] + secondTriangle[0])),
                (int) Math.abs(Math.ceil(firstTriangle[0] + secondTriangle[1]))
        };
    }

    public static double[] getTriangle(double c, double alphaAngle){
        double a = c * Math.sin(Math.toRadians(alphaAngle));
        double b = c * Math.cos(Math.toRadians(alphaAngle));
        return new double[]{a, b, c};
    }

}
