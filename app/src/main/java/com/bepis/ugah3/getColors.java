package com.bepis.ugah3;

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
		return 0;
	}
}
