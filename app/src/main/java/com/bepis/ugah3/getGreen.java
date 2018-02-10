package com.bepis.ugah3;
public class getGreen {

	public int main(int x){
		x = x & 0x00FF00;
		x >>>= 8;
		return x;
	}
}