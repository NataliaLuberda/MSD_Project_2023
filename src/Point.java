import java.util.ArrayList;
import java.util.Random;



public class Point{

	private static final int SFMAX = 100000;
	public boolean blocked = false;
	public ArrayList<Point> neighbors;
	public static Integer []types ={0,1,2,3,4};
	public int type;
	public double staticField;
	public boolean isPedestrian;
	public int x;
	public int y;
	public float smokeDensity = 0;
	public boolean checker = false;
	private int iteration;
	private static int peopleConst = 5; // 10 / (10 - 8)

	public Point(int x,int y) {
		type=0;
		staticField = SFMAX;
		neighbors= new ArrayList<Point>();
		this.x = x;
		this.y = y;
		iteration = 0;
	}
	
	public void clear() {
		staticField = SFMAX;
		blocked = false;
		smokeDensity = 0;
	}

	public boolean calcStaticField() {		
		double smalestField = 100000;
		if(!(neighbors.size()== 0 || this.type ==1)){
			for(Point neighbour : neighbors){
				smalestField = (smalestField>neighbour.staticField+1? neighbour.staticField+1 : smalestField);
			}
			if(this.staticField>smalestField){
				this.staticField = smalestField;
				return true;
			}
		}
		return false;
	}
	
	public int move(){
		if (isPedestrian && !blocked && iteration % peopleConst != 0){
			Random random = new Random();
			Point nextP = this;
			ArrayList<Point> nextPos = new ArrayList<Point>();
			double theSmallest = this.staticField;
			for (Point neigh : neighbors){
				if (neigh.staticField < theSmallest && !neigh.isPedestrian && neigh.type != 1) theSmallest = neigh.staticField;
			}
			for (Point neigh : neighbors){
				if (neigh.staticField == theSmallest && !neigh.isPedestrian && neigh.type != 1) nextPos.add(neigh);
			}
			if (!nextPos.isEmpty()) {
				int id = random.nextInt(nextPos.size());
				nextP = nextPos.get(id);
				if (!nextP.isPedestrian) {
					if (nextP.type != 2) {
						nextP.isPedestrian = true;
						nextP.blocked = true;
					}
					this.isPedestrian = false;
				}
			}
			else if (nextP == this){ //trying random choose
				for (Point neigh : neighbors){
					int tmp = random.nextInt(100);
					if (!neigh.isPedestrian && neigh.type != 1 && tmp < 10) nextP = neigh;
				}
				if (!nextP.isPedestrian){
					if (nextP.type != 2){
						nextP.isPedestrian = true;
						nextP.blocked = true;
					}
					this.isPedestrian = false;
				}
			}
		}
		iteration++;
		return 0;
		
	}

	public void addNeighbor(Point nei) {
		neighbors.add(nei);
	}

	public void makeClean(){
		this.smokeDensity = 0;
		for(Point p: neighbors){
			p.smokeDensity = 0;
		
		}
	}
	
}