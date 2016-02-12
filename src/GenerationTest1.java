import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * GenerationTest1
 * @author Isaac Assegai
 * A system where:
 * 0. We keep track of a pool of potential/proto resources
 * 1. Proto Resources are generated into the game world over time, from this pool.
 * 2. Proto Resources are generated in random locations throughout the game world.
 * 3. Proto Resources have a mass and are effected by each others gravity 
 *    (they are attracted to eachother, kinky).
 * 4. When Proto Resources collide they become 1 ProtoResource with the mass of both combined.
 * 5. When a Proto Resource become large enough, it graduates to an actual game resource.
 *    For example, it could become a planet, or asteroid for players to mine, etc.
 * 6. In this example resources will be clickable circles.
 * 7. Clicking on a circle is harvesting it's resources, the resources will be added back to the original pool
 * 8. The process continues
 */
public class GenerationTest1 {
	/* Field Variables */
	
	//Graphics varibales
	private JFrame frame;
	private GamePanel panel;
	
	//Game Pieces
	private int resourcePool;
	private ArrayList<ProtoResource> resources;
	private boolean inGame;
	private Random random;
	
	//Constants
	private double g = 20; // The Acceleration of Gravity
	
	GenerationTest1(){
		setupGraphics();
		setupResources();
		//test first resource
		//resources.add(new ProtoResource( 10, 10, 2));
		inGame = true;
		gameLoop();
	}
	
	private void gameLoop(){
		//worry about location, mass, and velocity calucations here
		long startTime = System.currentTimeMillis();
		long elapsedTime;
		while(inGame){
			//figure out the elaspedTime we will be using for this frame.
			long currentTime = System.currentTimeMillis();
			elapsedTime = currentTime - startTime;
			startTime = currentTime;
			//first we will see if we should add another resource from the pool
			if(resourcePool > 0){
				resources.add(new ProtoResource(random.nextDouble()*frame.getWidth(), random.nextDouble()*frame.getHeight(), 1));
				resourcePool--;
			}
			
			//second we apply gravity to the velocity of each proto resource O(x^2)
			for(ProtoResource r : resources){
				Vector2D force = new Vector2D(0, 0);
				for(ProtoResource r2 : resources){
					if(r != r2){ //we don't apply gravity for ourself
						double mass1 = r.mass;
						double mass2 = r2.mass;
						double distanceSQ = r.location.distanceSq(r2.location);
						double distance = r.location.distance(r2.location);
						Vector2D preforce = new Vector2D(0, 0);
						preforce = r2.location.minus(r.location);
						preforce = preforce.scalarMult(g * mass1 * mass2);
						preforce = preforce.scalarMult((1/distanceSQ));
						force = force.plus(preforce);
					}
				}
				
				//calculate the new acceleration based on force
				Vector2D accel = force.scalarMult(1/r.mass);
				
				//third we update the velocity of each proto resource based on acceleration
				//we limit it
				
				//r.velocity = r.velocity.scalarMult(elapsedTime/1000 / .9f);
				r.velocity = r.velocity.plus(accel);
				if(r.velocity.length() > 5){
					r.velocity = r.velocity.scalarMult(.5f);
				}
				
				
				
				//forth we update the location based on velocity
				r.location = r.location.plus(r.velocity);
			}
			
			//Collision Check
			
			//fourth we check resources to make sure they're not out of bounds, if they are, we move them
			//back into bounds and reverse the correct element of velocity to make them bounce.
			for(ProtoResource r : resources){
				if(r.location.x > panel.getWidth()){ //off right bounds
					r.location.x = panel.getWidth() - 1; //move back in bounds
					r.velocity.x = -r.velocity.x; //bounce the velocity
				}else if(r.location.x < 0){ //Off left bounds
					r.location.x = 1;
					r.velocity.x = -r.velocity.x;
				}else if(r.location.y > panel.getHeight()){//Off Bottom of Screen
					r.location.y = panel.getHeight() - 1;
					r.velocity.y = - r.velocity.y;
				}else if(r.location.y < 0){//Off top of screen
					r.location.y = 1;
					r.velocity.y = -r.velocity.y;
				}
				System.out.println(r.velocity);	
			}
			
			//fifth we draw the frame
			frame.repaint();
			
			//let the gameloop pause for a moment before repeated
			try {
				Thread.sleep(33);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void setupResources(){
		resourcePool = 100;
		resources = new ArrayList<ProtoResource>();
		random  = new Random(System.currentTimeMillis());
	}
	
	private void setupGraphics(){
		frame = new JFrame("GenrationTest1");
		panel = new GamePanel();
		
		frame.setSize(800,  600);
		frame.setVisible(true);
		frame.setLocation(50, 50);
		frame.add(panel);
		System.out.println("added");
	}
	
	private class GamePanel extends JPanel{
		public GamePanel(){
			setPreferredSize(new Dimension(800, 600));
		}
		
		@Override
		public void paintComponent(Graphics g){
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, this.getWidth(), this.getHeight()); //draw a blank slate
			drawProtoResources((Graphics2D)g);
		}
		
		/**
		 * Loop through the list of proto resources and draw something in it's location
		 * corresponding to its mass;
		 * @param g
		 */
		private void drawProtoResources(Graphics2D g){
			g.setColor(Color.white);
			for(ProtoResource r : resources){
				r.draw(g);
			}
			
		}
	}
	
	/**
	 * Proto Resources have a location, velocity and a mass. It's velocity is effected by gravity
	 * @author Isaac Assegai
	 *
	 */
	private class ProtoResource{
		Vector2D location;
		Vector2D velocity;
		int mass;
		
		public ProtoResource(double x, double y, int mass){
			location = new Vector2D(x, y);
			velocity = new Vector2D(0, 0);
			this.mass = mass;
			
		}
		
		public void draw(Graphics2D g){
			g.fillOval((int)location.x, (int)location.y, mass*2, mass*2);
		}
	}
}
























