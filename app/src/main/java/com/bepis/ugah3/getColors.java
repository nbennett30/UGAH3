package com.bepis.ugah3;

import android.graphics.Color;

public class getColors {
	public static int getGreen(int x){
		x = x & 0x0000FF00;
		x >>>= 8;
		return x;
	}
	
	public static int getRed(int x){
		x = x & 0x00FF0000;
		x >>>= 16;
		return x;
	}
	
	public static int getBlue(int x){
		x = x & 0x000000FF;
		return x;
	}

	public static int getAlpha(int x){
		x = x & 0xFF000000;
		x >>>= 24;
		return x;
	}

	public static int averageHex(int[] pixels) {
		int avgBlue=0;
		int avgGreen=0;
		int avgRed=0;
		for (int i = 0; i < pixels.length; i++){
			avgBlue = avgBlue+getColors.getBlue(pixels[i]);
			avgGreen = avgGreen+getColors.getGreen(pixels[i]);
			avgRed = avgRed+getColors.getRed(pixels[i]);
		}
		avgBlue = avgBlue/pixels.length;
		avgGreen = avgGreen/pixels.length;
		avgRed = avgRed/pixels.length;

		return Color.argb(0xff,avgRed,avgGreen,avgBlue);
	}
}
