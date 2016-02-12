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
	JFrame frame;
	GamePanel panel = new GamePanel();
	GenerationTest1(){
		frame = new JFrame();
	}
	
	private class GamePanel extends JPanel{
		
	}
}
