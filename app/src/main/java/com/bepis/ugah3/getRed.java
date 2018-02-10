package com.bepis.ugah3;
public class getRed {

	public int main(int x){
		x = x & 0xFF0000;
		x >>>= 16;
		return x;
	}
}
