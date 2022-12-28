package code;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Person {
	//Class for people. 
	
	public static int random(int min, int max) {
		return (int)(Math.random() * (max - min) + min);
	}
		
	int BOX_BOTTOM, BOX_TOP, BOX_LEFT, BOX_RIGHT; 
	int countryNumber;
	int timeLastInfected;
	
	double x, y;
	double dx, dy; 
	double recoverProbability;
	double infectionProbability;
	
	boolean infected = false;
	boolean source = false;
	boolean deceased = false;
	boolean travelling = false;
	boolean travellingToConference = false;

	public Person() {
		do{
			dx = random(-6, 6);
		} while(dx == 0);
		
		do {
			dy = random(-6, 6);
		} while(dy == 0);
	}
	
	public void display(Graphics g) {
		if(infected)
			g.setColor(Color.RED);
		else 
			g.setColor(Color.BLUE);
		
		if(source)
			g.setColor(Color.CYAN);
		if(travelling && infected)
			g.setColor(Color.YELLOW);
		else if(travelling)
			g.setColor(Color.GREEN);
		
		if(deceased) {
			dx = 0;
			dy = 0;
			infected = false;
			g.setColor(Color.LIGHT_GRAY);
		}
		
		
		if(!travelling) {
			if(x > BOX_RIGHT - 10 || x < BOX_LEFT + 5)
				dx *= -1;
			if(y > BOX_BOTTOM - 15 || y < BOX_TOP + 5)
				dy *= -1;
		}
		
		
		
		g.fillOval((int)x, (int)y, 10, 10);
	}
	

}
