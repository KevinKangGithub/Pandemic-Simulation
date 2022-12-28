package code;

import java.awt.Color;
import java.awt.Graphics;

public class Connection {
	//Class for airport connections. 
	
	int x1, y1, x2, y2;

	public void display(Graphics g) {
		
		//Each connection is represented by a line. 
		g.setColor(Color.BLACK);
		g.drawLine(x1, y1, x2, y2);
	}
}
