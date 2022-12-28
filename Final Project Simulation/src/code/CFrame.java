package code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;



/*
 * IMPORTANT INFORMATION - PLEASE READ. 
 * Every line of code is designed, created, and written by: Kevin Kang, 2022. 
 * Computer Science 20 Final Project
 * Project Name: Virus Simulation
 * ----------------------------
 * TUTORIAL
 * Step 1. Add countries by dragging your cursor. 
 * Step 2. Add airport connections between countries. 
 * Step 3. Add first person. 
 * Step 4. Run simulation.
 * Note: you may pause the simulation by clicking on the "Run" button in the top-left corner. You may add additional countries and connections when the simulation is paused. 
 * Note: you may reset people but keep the countries and connections. 
 */

public class CFrame extends JPanel implements ActionListener{
	
	final int population = 100; //population must be greater than 1, else won't run because 100% people are infected at the start. 
	final static int INFECTION_RADIUS = 5;
	final int FPS = 55;
	int timer = 0;
	int countryCount = 0;
	int connectionCount = 0;
	int option;
	int lastConferenceTime = -1;
	
	final double initialInfectionProbability = 0.8;
	final double deceasedProbability = 0.03;
	final double recoverProbability = 0.003;
	
	boolean setAdjacencyList = false;
	boolean simulationRunning = false;
	boolean beginSimulation = false;
	boolean makePeople = false;
	boolean drawingConnection = false;
	boolean drawingCountry = false;
	boolean madeFirstPerson = false;
	
	boolean[][] connectionLookup = new boolean[100][100];
	
	JButton startSimulation;
	JButton addFirstPerson;
	JButton addCountry;
	JButton addConnection;
	JButton resetPeople;
	
	ArrayList<ArrayList<Integer>> adjacencyList = new ArrayList<>(100);
	ArrayList<Person> people = new ArrayList<Person>();
	ArrayList<Country> countries = new ArrayList<Country>();
	ArrayList<Connection> connections = new ArrayList<Connection>();
	
	MouseListener listener = new MouseListener();
		
	public static boolean infectionProximity(double x1, double x2, double y1, double y2) {
		return ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) <= INFECTION_RADIUS * INFECTION_RADIUS);
	}
	
	public static int random(int min, int max) {
		return (int)(Math.random() * (max - min + 1) + min);
	}
	
	public static void main(String[] arg) {
		CFrame c = new CFrame();
	}
	
	
	
	
	
	//Setting up CFrame and the main GUI. 
	public CFrame() {
		//Title of the application will be "Pandemic Simulation". 
		JFrame frame = new JFrame("Pandemic Simulation");
		
		//Initializing button names. 
		startSimulation = new JButton("Run"); 
		addFirstPerson = new JButton("Add First Person");
		addCountry = new JButton("Add Country"); 
		addConnection = new JButton("Add Connection");
		resetPeople = new JButton("Reset People"); 
		
		/*
		 * Initializing button locations. setBounds takes in four parameters: 
		 * First parameter: x-coordinate of the top-left corner. (Left of screen is 0, right of screen is 1000). 
		 * Second parameter: y-coordinate of the top-left corner. (Top of screen is 0, button of screen is 800). 
		 * Third parameter: width of the button. 
		 * Fourth parameter: height of the button. 
		 */
		startSimulation.setBounds(0,0,100,50);
		addFirstPerson.setBounds(100, 0, 200, 50);
		addCountry.setBounds(300, 0, 100, 50);
		addConnection.setBounds(400, 0, 200, 50);
		resetPeople.setBounds(600, 0, 100, 50);
		
		//Add action listeners as methods which are at the bottom of this function. 
		startSimulation.addActionListener(this);
		addFirstPerson.addActionListener(this);
		addCountry.addActionListener(this);
		addConnection.addActionListener(this);
		resetPeople.addActionListener(this);
		
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Initializing adjacency list with empty ArrayLists. 
		for(int i = 0; i < 100; i++) {
		    adjacencyList.add(new ArrayList());
		}
		
		Timer t = new Timer(15 , this);
		t.restart();
		
		//Adding buttons to frame. 
		frame.add(startSimulation);
		frame.add(addFirstPerson);
		frame.add(addConnection);
		frame.add(addCountry);
		frame.add(resetPeople);
		frame.add(this);
		
		frame.setVisible(true);
		
	}

	
	void updatePeople() {
		
		//At the start of each simulation, the program will initialize uninfected people by placing them randomly among countries. 
		if(!makePeople) {
			for(int i = 1; i < population; i++) {
				
				//Each person will be assigned a random country and a random location within the country. 
				Person newPerson = new Person();
				int countryNumber = random(0, countries.size() - 1);
				
				people.add(new Person());
				people.get(i).countryNumber = countryNumber;
				people.get(i).infectionProbability = initialInfectionProbability;
				people.get(i).timeLastInfected = 0;

				
				int BOX_TOP = countries.get(countryNumber).y1;
				int BOX_BOTTOM = countries.get(countryNumber).y2;
				int BOX_LEFT = countries.get(countryNumber).x1;
				int BOX_RIGHT = countries.get(countryNumber).x2;				

				people.get(i).x = random(BOX_LEFT + 50, BOX_RIGHT - 50);
				people.get(i).y = random(BOX_TOP + 50, BOX_BOTTOM - 50);
				
				people.get(i).BOX_BOTTOM = BOX_BOTTOM;
				people.get(i).BOX_TOP = BOX_TOP;
				people.get(i).BOX_LEFT = BOX_LEFT;
				people.get(i).BOX_RIGHT = BOX_RIGHT;
			}
			makePeople = true;
		}
		
		//Bug fix to remove glitched people created when the user does not initialize the position of patient-zero, causing it to have a coordinate of (0, 0). 
		for(int i = 0; i < people.size(); i++) {
			if(people.get(i).x == 0)
				people.remove(i);
		}
		
		//People can randomly travel from one country to another country if their airports are connected. 
		for(int i = 0; i < population; i++) {
			int infectedInCountry = 0, populationInCountry = 0;
			double travelProbability;
			
			//A person is more likely to travel out of a country if there is a large percentage of people infected in their own country. 
			travelProbability = (1.0 - percentageHealthy(people.get(i).countryNumber) + 0.20) / 100.0;
			
			if(!people.get(i).travellingToConference && !people.get(i).travelling && adjacencyList.get(people.get(i).countryNumber).size() > 0 && Math.random() <= travelProbability) {

				//Randomly assigning the destination country through choosing a random country in its adjacency list. 
				int newCountryNumber = adjacencyList.get(people.get(i).countryNumber).get(random(0, adjacencyList.get(people.get(i).countryNumber).size() - 1));
				
				//A person will more likely travel to a country with a higher percentage of healthy people. 
				if(percentageHealthy(newCountryNumber) < percentageHealthy(people.get(i).countryNumber) - 0.20)
					break;
				
				people.get(i).travelling = true;
				people.get(i).countryNumber = newCountryNumber;
				
				
				//Shift box and and borders to be the destination country so the person will not bounce back when they reach the borders. 
				int BOX_TOP = countries.get(newCountryNumber).y1;
				int BOX_BOTTOM = countries.get(newCountryNumber).y2;
				int BOX_LEFT = countries.get(newCountryNumber).x1;
				int BOX_RIGHT = countries.get(newCountryNumber).x2;
				
				people.get(i).BOX_BOTTOM = BOX_BOTTOM;
				people.get(i).BOX_TOP = BOX_TOP;
				people.get(i).BOX_LEFT = BOX_LEFT;
				people.get(i).BOX_RIGHT = BOX_RIGHT;
				
				//The person will move to the center of the destination country in approximately 50 frames. 
				people.get(i).dx = ((BOX_LEFT + BOX_RIGHT) / 2 - people.get(i).x) / 50.0;
				people.get(i).dy = ((BOX_TOP + BOX_BOTTOM) / 2 - people.get(i).y) / 50.0;
			}
		}
		
		//Check if a traveling person has arrived at their destination country. 
		for(int i = 0; i < population; i++) {
			
			//If a person has fully entered their destination country (at least 15 pixels in from each side), disable their traveling status and reset their speed. 
			if(people.get(i).travelling && people.get(i).x >= people.get(i).BOX_LEFT + 15 && people.get(i).x <= people.get(i).BOX_RIGHT - 15 && people.get(i).y >= people.get(i).BOX_TOP + 15 && people.get(i).y <= people.get(i).BOX_BOTTOM - 15) {
				people.get(i).travelling = false;
				do{
					people.get(i).dx = random(-6, 6);
				} while(people.get(i).dx == 0);
				
				do {
					people.get(i).dy = random(-6, 6);
				} while(people.get(i).dy == 0);
			}
		}
		
		
		//Approximately 10 seconds, a random country will hold a conference where everyone meet up. 
		if(timer != 0 && timer % (5 * FPS) == 0) {
			int countryHoldingConference = random(0, countryCount);
			countries.get(countryHoldingConference).holdingConference = true;
			for(int i = 0; i < people.size(); i++) {
				if(people.get(i).countryNumber == countryHoldingConference && !people.get(i).travelling) {
					people.get(i).dx = ((people.get(i).BOX_LEFT + people.get(i).BOX_RIGHT) / 2 - people.get(i).x) / 50.0;
					people.get(i).dy = ((people.get(i).BOX_TOP + people.get(i).BOX_BOTTOM) / 2 - people.get(i).y) / 50.0;
					people.get(i).travellingToConference = true;
				}
			}
			lastConferenceTime = timer;
		}
		
		//Check if a person has arrived at their conference. 
		for(int i = 0; i < population; i++) {
			if(people.get(i).travellingToConference) {
				if(Math.abs(people.get(i).x - (people.get(i).BOX_LEFT + people.get(i).BOX_RIGHT) / 2) <= 15 && Math.abs(people.get(i).y - (people.get(i).BOX_TOP + people.get(i).BOX_BOTTOM) / 2) <= 15) {
					people.get(i).travellingToConference = false;
					do{
						people.get(i).dx = random(-6, 6);
					} while(people.get(i).dx == 0);
					
					do {
						people.get(i).dy = random(-6, 6);
					} while(people.get(i).dy == 0);
				}
					
			}
			
		}
		
		
		//If everyone has arrived at conference, then turn off conference status for every country. 
		if(timer >= lastConferenceTime + 0.7 * FPS) {
			for(int i = 0; i < countries.size(); i++) {
				countries.get(i).holdingConference = false;
			}
		}	
		
		//One person spread infection to another. 
		for(int i = 0; i < population; i++) {
			for(int j = i + 1; j < population; j++) {
				if(infectionProximity(people.get(i).x, people.get(j).x, people.get(i).y, people.get(j).y) && !people.get(i).travelling && !people.get(j).travelling) {
					if(people.get(i).infected && !people.get(j).infected && Math.random() < people.get(j).infectionProbability) {
						people.get(j).infected = true;
						people.get(j).timeLastInfected = timer;
					}
						
					if(people.get(j).infected && !people.get(i).infected && Math.random() < people.get(i).infectionProbability) {
						people.get(i).infected = true;
						people.get(i).timeLastInfected = timer;
					}
						
				}
			}
		}
		for(int i = 0; i < population; i++) {
			
			//If a person has been infected for at least 5 seconds, they start to have a likelihood of dying or recovering. 
			if(timer > people.get(i).timeLastInfected + 5 * FPS) {
				
				//Person dies. Infected status is turned off because dead people can not spread the disease. 
				if(people.get(i).infected == true && Math.random() <= deceasedProbability) {
					people.get(i).deceased = true;
					people.get(i).infected = false;
				}
				
				//Person recovers. Their likelihood of receiving the disease is exponentially lower. 
				//Likehood of person to recieve the disease = initialInfectionProbability * 0.9^(number of times the person has been infected). 
				if(people.get(i).infected == true) {
					if(people.get(i).infected && timer > people.get(i).timeLastInfected + 5 * FPS) {
						people.get(i).infected = false;
						people.get(i).infectionProbability *= 0.9;
					}
				}
			}
		}
		
		//Update position of people based on their velocities. 
		for(int i = 0; i < population; i++) {
			people.get(i).x += people.get(i).dx;
			people.get(i).y += people.get(i).dy;
		}
		
		
		//Count number of infected people so that when higher than 80% people are either infected or dead, simulation stops. 
		int infected = 0, deceased = 0;
		for(int i = 0; i < population; i++) {
			infected += (people.get(i).infected ? 1 : 0);
			deceased += (people.get(i).deceased ? 1 : 0);
		}
		
		//Comment the section below if you do not want the code to stop after 80% of people are infected or dead. 
		if((infected + deceased) * 10 >= population * 8 || infected == 0) { 
			simulationRunning = false;
		}

	}
	
	double percentageHealthy(int countryID) {
		//Returns the percentage of healthy population in a country (not counting deceased). 
		int healthy = 0, total = 0;
		for(int i = 0; i < population; i++) {
			if(people.get(i).countryNumber == countryID && !people.get(i).deceased) {
				total++;
				if(!people.get(i).infected)
					healthy++;
			}
		}
		
		//Edge case: if a country has no population, the healthy percentage is 100%. 
		if(total == 0)
			return 1.0;
		
		return (double)healthy/(double)total;
	}

	
	void drawCountry() {
		//Adding a new country. 
		//MouseListener option 1 is for drawing a country. 
		option = 1;
		addMouseListener(listener);
        addMouseMotionListener(listener);
        Country newCountry = new Country();
        countries.add(newCountry);
	}
	
	
	void drawConnection() {
		//Adding a connection. 
		//MouseListener option 2 is for drawing a connection. 
		option = 2;
        addMouseListener(listener);
        addMouseMotionListener(listener);
        Connection newConnection = new Connection();
        connections.add(newConnection);

	}
	
	
	void drawFirstPerson() {
		//Adding patient-zero. 
		//MouseListener option 3 is for drawing the position of the first person. 
		option = 3;
        addMouseListener(listener);
        addMouseMotionListener(listener);
		Person newPerson = new Person();
		people.add(new Person());
	}
	

	
	class MouseListener extends MouseAdapter {
		
        public void mousePressed(MouseEvent e) {
        	//Mouse is pressed
        	if(option == 1) {
        		countries.get(countryCount).x1 = e.getX();
            	countries.get(countryCount).y1 = e.getY();
            	drawingCountry = true;
        	} else if(option == 2) {
        		connections.get(connectionCount).x1 = e.getX();
                connections.get(connectionCount).y1 = e.getY();
                drawingConnection = true;
        	} 
        }
        
        public void mouseDragged(MouseEvent e) {
        	//Mouse is dragged
        	if(option == 1) {
        		countries.get(countryCount).x2 = e.getX();
        		if(countries.get(countryCount).x2 < 0)
        			countries.get(countryCount).x2 = 0;
        		else if(countries.get(countryCount).x2 > 1000)
        			countries.get(countryCount).x2 = 1000;
        		
            	countries.get(countryCount).y2 = e.getY();
            	if(countries.get(countryCount).y2 < 50)
        			countries.get(countryCount).y2 = 50;
        		else if(countries.get(countryCount).y2 > 800)
        			countries.get(countryCount).y2 = 800;
            	
                repaint();
        	} else if(option == 2) {
        		connections.get(connectionCount).x2 = e.getX();
                connections.get(connectionCount).y2 = e.getY();
            	repaint();
        	}
        	
        }
        
        public void mouseReleased(MouseEvent e) {
        	//Mouse is released
        	if(option == 1) {
        		countries.get(countryCount).x2 = e.getX();
            	countries.get(countryCount).y2 = e.getY();
            	
            	//Error trapping: program make sure that the country stays within the window if the user drags the cursor outside of the window. 
            	int x1 = Math.min(countries.get(countryCount).x1, countries.get(countryCount).x2);
            	int x2 = Math.max(countries.get(countryCount).x1, countries.get(countryCount).x2);
            	int y1 = Math.min(countries.get(countryCount).y1, countries.get(countryCount).y2);
            	int y2 = Math.max(countries.get(countryCount).y1, countries.get(countryCount).y2);

            	countries.get(countryCount).x1 = Math.max(0, x1);
            	countries.get(countryCount).y1 = Math.max(50, y1);
            	countries.get(countryCount).x2 = Math.min(1000, x2);
            	countries.get(countryCount).y2 = Math.min(800, y2);
            	
            	/*
            	 * Error trapping: 
            	 * 1. The country must not be too small. 
            	 * 2. The country must not overlap with another country. 
            	 */
            	
            	boolean valid = true;
            	
            	if(Math.abs(countries.get(countryCount).x1 - countries.get(countryCount).x2) <= 50 || Math.abs(countries.get(countryCount).y1 - countries.get(countryCount).y2) < 50) {
            		valid = false;
            		
            	} 
            	
            	for(int i = 0; i < countries.size() - 1; i++) {
            		if(countries.get(i).x1 < countries.get(countryCount).x2 && countries.get(countryCount).x1 < countries.get(i).x2 && countries.get(i).y1 < countries.get(countryCount).y2 && countries.get(countryCount).y1 < countries.get(i).y2) {
            			valid = false;
            		}
            	}
            	
            	if(valid) {
            		countryCount++;
            	} else {
            		countries.remove(countryCount);
            		repaint();
            		return;
            	}
            	
        	} else if(option == 2) {
        		/*
        		 * Error trapping: 
        		 * 1. Each end of the connection must correspond to a country. 
        		 * 2. The two countries must not already be connected. 
        		 * 3. The country must not be connected to itself. 
        		 */
        		
        		connections.get(connectionCount).x2 = e.getX();
                connections.get(connectionCount).y2 = e.getY();
                int country1 = getCountryID(connections.get(connectionCount).x1, connections.get(connectionCount).y1);
                int country2 = getCountryID(connections.get(connectionCount).x2, connections.get(connectionCount).y2);
                if(country1 != -1 && country2 != -1 && country1 != country2 && !connectionLookup[country1][country2] && !connectionLookup[country2][country1]) {
                	connectionCount++;
                	connectionLookup[country1][country2] = true;
                	connectionLookup[country2][country1] = true;
                	adjacencyList.get(country1).add(country2);
                	adjacencyList.get(country2).add(country1);
                } else {
                	connections.remove(connectionCount);
                }
                drawingConnection = false;
                repaint();
        	} else if(option == 3) {
        		people.get(0).x = e.getX();
        		people.get(0).y = e.getY();

        		int countryNumber = getCountryID(e.getX(), e.getY());
        		
        		//Error trapping: patient-zero must be within a country. 
        		if(countryNumber != -1) {
        			people.get(0).countryNumber = countryNumber;

            		int BOX_TOP = countries.get(countryNumber).y1;
            		int BOX_BOTTOM = countries.get(countryNumber).y2;
            		int BOX_LEFT = countries.get(countryNumber).x1;
            		int BOX_RIGHT = countries.get(countryNumber).x2;				

            		people.get(0).BOX_BOTTOM = BOX_BOTTOM;
            		people.get(0).BOX_TOP = BOX_TOP;
            		people.get(0).BOX_LEFT = BOX_LEFT;
            		people.get(0).BOX_RIGHT = BOX_RIGHT;
            		people.get(0).infected = true;
            		people.get(0).source = true;
            		madeFirstPerson = true;
            		repaint();
        		} else {
        			people.remove(0);
        		}
        		drawingCountry = false;
        	}
        }
    }
	
	//Get the country indice that is covering 
	public int getCountryID(int x, int y) {
		for(int i = 0; i < countries.size(); i++) {
			if(x >= countries.get(i).x1 && x <= countries.get(i).x2 && y >= countries.get(i).y1 && y <= countries.get(i).y2)
				return i;
		}
		return -1;
	}
	
	//Using BFS to see check if the countries form a strongly connected component. 
	boolean stronglyConnected(ArrayList<Country> countries, ArrayList<ArrayList<Integer>> adjacencyList) {
		
		//Starting from vertex 0 and keeping track of visited country indices through using a queue. 
		boolean[] visited = new boolean[countries.size()];
		int[] queue = new int[countries.size()];
		queue[0] = 0;
		int next = 1;
		for(int i = 0; i < queue.length; i++) {
			visited[queue[i]] = true;
			for(int x = 0; x < adjacencyList.get(queue[i]).size(); x++) {
				if(!visited[adjacencyList.get(queue[i]).get(x)]) {
					//Adding unseen neighbours to the queue. 
					queue[next++] = adjacencyList.get(queue[i]).get(x);
					visited[adjacencyList.get(queue[i]).get(x)] = true; 
				}
			}
		}
		
		//Checking if every vertex can be reached from vertex 0. 
		boolean ok = true;
		for(int i = 0; i < visited.length; i++) {
			ok &= visited[i];
		}
		return ok;
	}
	

	
	//Function to display all graphical objects. 
	public void paint(Graphics g) {
		
		//Always display countries. 
		for(Country c : countries) {
			c.display(g);
		}
		
		//Always display connections between airports. 
		for(Connection c : connections) {
			c.display(g);
		}
		
		//Display patient zero after the user has set the location of patient zero. 
		if(madeFirstPerson && !beginSimulation) {
			people.get(0).display(g);
		}
		
		//If simulation is running, update people and increase timer tick. 
		if(beginSimulation) {
			//If user adds connections or countries, the program halts. 
			if(!drawingConnection && !drawingCountry) {
				updatePeople();
				timer++;
			}
			for(Person p : people) {
				p.display(g);
			}
		}
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(simulationRunning) 
			repaint();
		
		if(e.getSource() == startSimulation) {
			//Begin simulation is the countries are strongly connected, and patient-zero has been created. 
			if(stronglyConnected(countries, adjacencyList) && madeFirstPerson) {
				
				//Simulation button uses XOR to continue/pause simulation. 
				simulationRunning ^= true;
				
				beginSimulation = true;
			}
		}
		
		//User wants to add country
		if(e.getSource() == addCountry) {
			if(!beginSimulation && countries.size() < 100) {
				drawCountry();
				repaint(); 
			}
		}
		
		//User wants to add patient zero
		if(e.getSource() == addFirstPerson) {
			if(!simulationRunning && people.size() == 0)
				drawFirstPerson();
			repaint();
		}
		
		//User wants to add connection
		if(e.getSource() == addConnection) {
			if(!beginSimulation)
				drawConnection();
			repaint();
		}
		
		//User wants to reset people. 
		if(e.getSource() == resetPeople) {
			//Remove every person from people array. 
			while(people.size() > 0)
				people.remove(0);
			
			makePeople = false;
			madeFirstPerson = false;
			beginSimulation = false;
			simulationRunning = false;
			repaint();
		}
	}
}

