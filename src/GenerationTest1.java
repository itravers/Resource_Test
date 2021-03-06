import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	private JLabel poolLabel;
	private JLabel protoLabel;
	private JLabel resourceLabel;
	
	//Game Pieces
	private int resourcePool;
	private ArrayList<ProtoResource> resources;
	private ArrayList<ProtoResource> gameResources;
	private boolean inGame;
	private Random random;
	
	
	//Constants
	private double g = .005f; // The Acceleration of Gravity
	private int NUM_PROTO = 10000;
	private double MASS_SIZE_RATIO = 4;
	private double MASS_THRESHOLD = NUM_PROTO / 20;
	private int RESOURCE_PER_FRAME = 4;
	
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
			if(resourcePool > RESOURCE_PER_FRAME - 1){
				for(int i = 0; i < RESOURCE_PER_FRAME; i++){
					resources.add(new ProtoResource(random.nextDouble()*frame.getWidth(), random.nextDouble()*frame.getHeight(), 1));
					resourcePool --;
				}
				
				
			}
			
			//sixth We check each proto-resource for collisions with other proto-resources and combine them into one
			for(ProtoResource r : resources){
				for(ProtoResource r2 : resources){
					if(r != r2){
						if(r.collides(r2)){
							if(r.mass >= r2.mass){
								//combine momentums Momentum = Velocity * Mass //Figure out new velocity from momentum
								Vector2D momentum1 = r.velocity.scalarMult(r.mass);
								Vector2D momentum2 = r2.velocity.scalarMult(r2.mass);
								r.velocity = momentum1.plus(momentum2).scalarMult(1/(r.mass + r2.mass));
								r.mass += r2.mass; //combine masses of two resources
								r2.remove();
							}else{
								//combine momentums Momentum = Velocity * Mass //Figure out new velocity from momentum
								Vector2D momentum1 = r2.velocity.scalarMult(r2.mass);
								Vector2D momentum2 = r.velocity.scalarMult(r.mass);
								r2.velocity = momentum1.plus(momentum2).scalarMult(1/(r2.mass + r.mass));
								r2.mass += r.mass; //combine masses of two resources
								r.remove();
							}
						}
					}
				}
				
				//update radius based on mass as a volume
				r.radius = Math.sqrt(r.mass/Math.PI) * MASS_SIZE_RATIO;
				
				System.out.println(r.radius);
			}
			
			//second we apply gravity to the velocity of each proto resource O(x^2)
			for(ProtoResource r : resources){
				Vector2D force = new Vector2D(0, 0);
				for(ProtoResource r2 : resources){
					if(r != r2 && r.location.distance(r2.location) < panel.getWidth()/5){ //we don't apply gravity for ourself
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
				}//F=MA or F/M = A
				
				//calculate the new acceleration based on force
				Vector2D accel = force.scalarMult(1/r.mass);
				
				while(accel.length() > 2){
					accel = accel.scalarMult(.9f);
				}
				
				//third we update the velocity of each proto resource based on acceleration
				//we limit it
				
				//r.velocity = r.velocity.scalarMult(elapsedTime/1000 / .9f);
				r.velocity = r.velocity.plus(accel);
				while(r.velocity.length() > 10){
					r.velocity = r.velocity.scalarMult(.9f);
				}
				
				
				
				//forth we update the location based on velocity
				r.location = r.location.plus(r.velocity);
			}
			
			//Collision Check
			
			//fifth we check resources to make sure they're not out of bounds, if they are, we move them
			//back into bounds and reverse the correct element of velocity to make them bounce.
			for(ProtoResource r : resources){
				if(r.location.x + r.radius > panel.getWidth()){ //off right bounds
					r.location.x = panel.getWidth() -r.radius - 1; //move back in bounds
					r.velocity.x = -r.velocity.x; //bounce the velocity
				}else if(r.location.x < 0){ //Off left bounds
					r.location.x = 1;
					r.velocity.x = -r.velocity.x;
				}else if(r.location.y + r.radius > panel.getHeight()){//Off Bottom of Screen
					r.location.y = panel.getHeight() - r.radius - 1;
					r.velocity.y = - r.velocity.y;
				}else if(r.location.y < 0){//Off top of screen
					r.location.y = 1;
					r.velocity.y = -r.velocity.y;
				}
				//System.out.println(r.velocity);	
			}
			
			
			
			//remove resources marked for remove, and check for resources over default value
			//and turn those into gameResources
			for(int i = 0; i < resources.size(); i++){
				if(resources.get(i).remove){
					resources.remove(i);
				}else if(resources.get(i).mass >= MASS_THRESHOLD){
					ProtoResource r = resources.get(i);
					resources.remove(i);
					if(!gameResources.contains(r)) gameResources.add(r);
				
				}
			}
			
			//now we update the labels
			 poolLabel.setText(" Resource Pool: " + resourcePool);
			protoLabel.setText(   "Proto Resource: " + resources.size());
			resourceLabel.setText("Total Resource: " + gameResources.size());
			
			//next we draw the frame
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
		resourcePool = NUM_PROTO;
		resources = new ArrayList<ProtoResource>();
		gameResources = new ArrayList<ProtoResource>();
		random  = new Random(System.currentTimeMillis());
	}
	
	private void setupGraphics(){
		frame = new JFrame("GenrationTest1");
		panel = new GamePanel();
		
		poolLabel =  new JLabel("Resource  Pool: " + NUM_PROTO);
		poolLabel.setLocation(10, 10);
		protoLabel =    new JLabel("\n Proto Resource: " + 0);
		protoLabel.setLocation(20, 20);
		resourceLabel = new JLabel("\n Total Resource: " + 0);
		resourceLabel.setLocation(30, 30);
		
		
		
		panel.add(poolLabel);
		panel.add(protoLabel);
		panel.add(resourceLabel);
		
		frame.setSize(1200,  900);
		frame.setVisible(true);
		frame.setLocation(50, 50);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.add(panel);
		System.out.println("added");
		
	}
	
	private class GamePanel extends JPanel implements MouseListener{
		public GamePanel(){
			addMouseListener(this);
			setPreferredSize(new Dimension(1200, 900));
		}
		
		@Override
		public void paintComponent(Graphics g){
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, this.getWidth(), this.getHeight()); //draw a blank slate
			drawGameResources((Graphics2D)g);
			drawProtoResources((Graphics2D)g);
		}
		
		/**
		 * Loop through the list of proto resources and draw something in it's location
		 * corresponding to its mass;
		 * @param g
		 */
		private void drawProtoResources(Graphics2D g){
			for(ProtoResource r : resources){
				r.draw(g, Color.white);
			}
			
		}
		
		private void drawGameResources(Graphics2D g){
			g.setColor(Color.green);
			for(ProtoResource r : gameResources){
				r.draw(g, Color.green);
			}
		}
		
		@Override
	    public void mouseClicked(MouseEvent e) {
			  // TODO Auto-generated method stub
	       

	    }   

	    @Override
	    public void mouseEntered(MouseEvent arg0) {
	        // TODO Auto-generated method stub

	    }

	    @Override
	    public void mouseExited(MouseEvent arg0) {
	        // TODO Auto-generated method stub

	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	        // TODO Auto-generated method stub
	    	 double x = e.getX();
		        double y = e.getY();
		        System.out.println("mouse clicked");
		        
		        //check to see if this x, y value intersects any of the gameResources
		        for(int i = 0; i < gameResources.size(); i++){
		        	ProtoResource r = gameResources.get(i);
		        	if(x >= r.location.x && x < r.location.x + r.radius
		        	   && y >= r.location.y && y < r.location.y + r.radius){
		        		//this is a mouse collision. remove the resource and add it's mass to the pool
		        		gameResources.remove(i);
		        		resourcePool += r.mass;
		        		System.out.println("Match");
		        	}else{
		        		System.out.println("NOMATCH");
		        	}
		        }

	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
	        // TODO Auto-generated method stub

	    }

	   
	}
	
	/**
	 * Proto Resources have a location, velocity and a mass. It's velocity is effected by gravity
	 * @author Isaac Assegai
	 *
	 */
	private class ProtoResource{
		boolean remove;
		Vector2D location;
		Vector2D velocity;
		double mass;
		double radius;
		
		public ProtoResource(double x, double y, double mass){
			remove = false;
			location = new Vector2D(x, y);
			velocity = new Vector2D(0, 0);
			this.mass = mass;
			
		}
		
		public void draw(Graphics2D g, Color c){
			g.setColor(c);
			//g.fillOval((int)location.x, (int)location.y, (int)(mass*MASS_SIZE_RATIO*4), (int)(mass*MASS_SIZE_RATIO*4));
			//g.setColor(Color.blue);
			g.fillOval((int)location.x, (int)location.y, (int)(radius), (int)(radius));
		}
		
		public boolean collides(ProtoResource r){
			boolean returnVal;
			if(location.x >= r.location.x && location.x <= r.location.x + r.radius
			   && location.y >= r.location.y && location.y <= r.location.y + r.radius){
				returnVal = true;
			}else{
				returnVal = false;
			}
			return returnVal;
		}
		
		public void remove(){
			remove = true;
		}
	}
}
























