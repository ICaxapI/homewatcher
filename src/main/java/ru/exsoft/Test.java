package ru.exsoft;

import ru.exsoft.utils.MathUtils;

import java.util.Date;

public class Test {
    public static void main(String[] args) throws Exception {
        int[] sizes = MathUtils.getExternalRectangle(800, 600, 45);
        System.out.println(Math.abs(sizes[0]));
        System.out.println(Math.abs(sizes[1]));
    }

}