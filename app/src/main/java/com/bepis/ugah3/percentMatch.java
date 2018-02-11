package com.bepis.ugah3;
public class percentMatch {

	public static boolean percentMatch(int x, int y) {
		final double range = .30;
		int xR = getColors.getRed(x);
		int xG = getColors.getGreen(x);
		int xB = getColors.getBlue(x);

		int yR = getColors.getRed(y);
		int yG = getColors.getGreen(y);
		int yB = getColors.getBlue(y);

		double zR, zG, zB;
		if(xR>=yR){
			zR = (double)yR/xR;
		}
		else{
			zR = (double)xR/yR;
		}
		if(xG>=yG){
			zG = (double)yG/xG;
		}
		else{
			zG = (double)xG/yG;
		}
		if(xB>=yB){
			zB = (double)yB/xB;
		}
		else{
			zB = (double)xB/yB;
		}
		double zfinal = (zR+zG+zB)/3;

		if (1-zfinal<=range){
			return true;
		}
		else{
			return false;
		}
	}

}