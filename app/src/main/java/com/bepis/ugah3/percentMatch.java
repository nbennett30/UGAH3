package com.bepis.ugah3;
public class percentMatch {

	public static boolean percentMatch(int x, int y) {
		final double range = .30;
		int xR = getColors.getRed(x);
		int xG = getColors.getGreen(x);
		int xB = getColors.getBlue(x);

		if(xR==0)
			xR+=20;
		if(xG==0)
			xG+=20;
		if(xB==0)
			xB+=20;
		int yR = getColors.getRed(y);
		int yG = getColors.getGreen(y);
		int yB = getColors.getBlue(y);
		if(yR==0)
			yR+=20;
		if(xG==0)
			yG+=20;
		if(xB==0)
			yB+=20;

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
		if (xR==0&&xG==0&&xB==0){
			if(yR+yG+yB<=60){
				return true;
			}
			else{
				return false;
			}
		}

		if (1-zfinal<=range){
			return true;
		}
		else{
			return false;
		}
	}

}