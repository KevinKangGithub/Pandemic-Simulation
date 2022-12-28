package code;

import java.awt.Color;
import java.awt.Graphics;

public class Country {
	//Class for countries. 
	
	int x1, y1, x2, y2;
	boolean holdingConference = false;
	
	public void display(Graphics g) {
		//Yellow color if country is not holding a conference. 
		if(!holdingConference)
			g.setColor(new Color(255, 229, 204));
		
		//Purple color if country is in the middle of a conference. 
		else 
			g.setColor(new Color(173, 71, 250));
		
		//Each country is portrayed by a rectangle. 
		g.fillRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
	}

}
