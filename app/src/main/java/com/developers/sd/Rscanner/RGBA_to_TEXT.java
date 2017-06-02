package com.developers.sd.Rscanner;

/**
 * Created by sohamdeshmukh on 31/05/17.
 */

public class RGBA_to_TEXT {

    int rgba0;
    int rgba1;
    int rgba2;

    public RGBA_to_TEXT (int A, int B, int C)
    {
        rgba0 = A;
        rgba1 = B;
        rgba2 = C;
    }

    public String convertToColor(){
        if(rgba0==0 && rgba1==0 && rgba2 ==0) return "Black";
        if(rgba0==139 && rgba1==69 && rgba2 ==90) return "Brown";
        if(rgba0==0 && rgba1==0 && rgba2 ==0) return "Black";
        if(rgba0==0 && rgba1==0 && rgba2 ==0) return "Black";
        if(rgba0==0 && rgba1==0 && rgba2 ==0) return "Black";
        if(rgba0==0 && rgba1==0 && rgba2 ==0) return "Black";
        if(rgba0==0 && rgba1==0 && rgba2 ==0) return "Black";
        if(rgba0==0 && rgba1==0 && rgba2 ==0) return "Black";
        if(rgba0==0 && rgba1==0 && rgba2 ==0) return "Black";
        if(rgba0==0 && rgba1==0 && rgba2 ==0) return "Black";
        else return "";
    }

}
