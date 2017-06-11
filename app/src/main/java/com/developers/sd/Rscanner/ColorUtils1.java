package com.developers.sd.Rscanner;

/**
 * Created by sohamdeshmukh on 09/06/17.
 */

import android.graphics.Color;

import org.opencv.core.Scalar;

import java.util.ArrayList;

public class ColorUtils1 {

    private ArrayList<ColorUtils1.ColorName> initColorList() {
        ArrayList<ColorUtils1.ColorName> colorList = new ArrayList<>();
        colorList.add(new ColorName("Black", 0, 0, 0));
        colorList.add(new ColorName("Black", 0, 0, 10));
        colorList.add(new ColorName("Black", 4, 30, 20));
        colorList.add(new ColorName("Black", 180, 250, 100));
        colorList.add(new ColorName("Brown", 0, 90, 10));
        colorList.add(new ColorName("Brown", 15, 250, 100));

        colorList.add(new ColorName("Red", 0, 65, 100));
        colorList.add(new ColorName("Red", 2, 250, 150));
        colorList.add(new ColorName("Red", 171, 65, 50));
        colorList.add(new ColorName("Red", 180, 250, 150));

        colorList.add(new ColorName("Orange", 4, 100, 100));
        colorList.add(new ColorName("Orange", 9, 250, 150));
        colorList.add(new ColorName("Yellow", 20, 130, 100));
        colorList.add(new ColorName("Yellow", 30, 250, 150));
        colorList.add(new ColorName("Green", 45, 50, 60));
        colorList.add(new ColorName("Green", 72, 250, 150));

        colorList.add(new ColorName("Blue", 80, 50, 50));
        colorList.add(new ColorName("Blue", 106, 250, 150));
        colorList.add(new ColorName("Violet", 130, 40, 50));
        colorList.add(new ColorName("Violet", 155, 250, 150));

        colorList.add(new ColorName("Grey", 0, 0, 50));
        colorList.add(new ColorName("Grey", 180, 50, 80));
        colorList.add(new ColorName("White", 0, 0, 90));
        colorList.add(new ColorName("White", 180, 15, 140));

        return colorList;
    };


    public String getColorNameFromHSV (int h, int s, int v) {
        ArrayList<ColorUtils1.ColorName> colorList = initColorList();
        ColorUtils1.ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorUtils1.ColorName c : colorList) {
            mse = c.computeMSE(h, s, v);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }
        if (closestMatch != null) {
            return closestMatch.getName();
        } else {
            return "No matched color name";
        }
    }

    public class ColorName {
        public int h, s, v;
        public String name;

        public ColorName(String cname, int cr, int cg, int cb) {
            this.h = cr;
            this.s = cg;
            this.v = cb;
            this.name = cname;
        }

        public int computeMSE (int pixR, int pixG, int pixB) {
            return (int) (((pixR - h)*(pixR - h) + (pixG - s)*(pixG - s) + (pixB - v)*(pixB - v))/3);
        }

        public int getR() {
            return  h;
        }

        public int getG() {
            return s;
        }

        public int getB() {
            return v;
        }

        public String getName() {
            return name;
        }
    }


}
