package com.developers.sd.Rscanner;

import android.graphics.Color;
import java.util.ArrayList;

public class ColorUtils {

    private ArrayList<ColorName> initColorList() {
        ArrayList<ColorName> colorList = new ArrayList<>();
        colorList.add(new ColorName("Black", 0x00, 0x00, 0x00));
        colorList.add(new ColorName("Brown", 0xA5, 0x2A, 0x2A));
        colorList.add(new ColorName("Red", 0xFF, 0x00, 0x00));
        colorList.add(new ColorName("Orange", 0xFF, 0xA5, 0x00));
        colorList.add(new ColorName("Yellow", 0xFF, 0xFF, 0x00));
        colorList.add(new ColorName("Green", 0x00, 0x80, 0x00));
        colorList.add(new ColorName("Blue", 0x00, 0x00, 0xFF));
        colorList.add(new ColorName("Violet", 0xEE, 0x82, 0xEE));
        colorList.add(new ColorName("Grey", 0x00, 0x00, 0x00));
        colorList.add(new ColorName("White", 0x80, 0x80, 0x80));
        return colorList;
    }

    public String getColorNameFromRgb (int r, int g, int b) {
        ArrayList<ColorName> colorList = initColorList();
        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorName c : colorList) {
            mse = c.computeMSE(r, g, b);
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

    public String getColorNameFromHex (int hexColor) {
        int r = (hexColor & 0xFF0000) >> 16;
        int g = (hexColor & 0xFF00) >> 8;
        int b = (hexColor & 0xFF);
        return getColorNameFromRgb(r, g, b);
    }

//    public int colorToHex (Color c) {
//        return Integer.decode("0x" + Integer.toHexString(c.getRGB()).substring(2));
//    }
//
//    public String getColorNameFromColor (Color c) {
//        return  getColorNameFromRgb(c.getRed(), c.getGreen(), c.getBlue);
//    }

    public class ColorName {
        public int r, g, b;
        public String name;

        public ColorName(String cname, int cr, int cg, int cb) {
            this.r = cr;
            this.g = cg;
            this.b = cb;
            this.name = cname;
        }

        public int computeMSE (int pixR, int pixG, int pixB) {
            return (int) (((pixR - r)*(pixR - r) + (pixG - g)*(pixG - g) + (pixB - b)*(pixB - b))/3);
        }

        public int getR() {
            return  r;
        }

        public int getG() {
            return g;
        }

        public int getB() {
            return b;
        }

        public String getName() {
            return name;
        }
    }
}
