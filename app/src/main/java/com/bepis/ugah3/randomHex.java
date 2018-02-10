package com.bepis.ugah3;
import java.util.Random;
public class randomHex {
	
	public int main() {
	Random random = new Random();
	
	int h = random.nextInt(0xFFFFFF);
	return(h);
	}

}
