import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;



public class Point{

	private static final int SFMAX = 100000;
	public boolean blocked = false;
	public ArrayList<Point> neighbors;
	public static Integer []types ={0,1,2,3,4};
	public int type;
	public double staticField;
	public boolean isPedestrian;
	public boolean strażak;
	public int x;
	public int y;
	public float smokeDensity = 0;
	public boolean checker = false;

	public Point(int x,int y) {
		type=0;
		staticField = SFMAX;
		neighbors= new ArrayList<Point>();
		this.x = x;
		this.y = y;
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
		if (isPedestrian && !blocked && !strażak){
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
						int tmp = random.nextInt(100);
						nextP.isPedestrian = true;
						nextP.blocked = true;
						if(tmp <40) nextP.strażak = true;
					}
					this.isPedestrian = false;
				}
			}
		}else if(isPedestrian && strażak && !blocked){
			Random random = new Random();
			Point nextP = this;
			ArrayList<Point> nextPos = new ArrayList<Point>();
			double theBiggest = this.smokeDensity;
			for (Point neigh : neighbors){
				if (neigh.smokeDensity> theBiggest && !neigh.isPedestrian && neigh.type != 1) theBiggest = neigh.smokeDensity;
			}
			for (Point neigh : neighbors){
				if (neigh.smokeDensity == theBiggest && theBiggest != 0 && !neigh.isPedestrian && neigh.type != 1 && neigh.type != 2) nextPos.add(neigh);
			}
			if (!nextPos.isEmpty()) {
				int id = random.nextInt(nextPos.size());
				nextP = nextPos.get(id);
				if (!nextP.isPedestrian) {
					if (nextP.type != 4) {
						nextP.isPedestrian = true;
						nextP.blocked = true;
						nextP.strażak = true;
						nextP.makeClean();
					}else if(nextP.type == 4){
						nextP.isPedestrian = true;
						nextP.blocked = true;
						nextP.strażak = true;
						nextP.makeClean();
						nextP.type = 0;
						nextP.smokeDensity = 0;
						this.isPedestrian = false;
						this.strażak = false;
						return 1;
					}
					this.isPedestrian = false;
					this.strażak = false;
					nextP.blocked = true;
				}
			}
			else if (nextP == this){
				if (nextP.smokeDensity == 0){
					if (nextP.type != 2){
						nextP.isPedestrian = true;
						nextP.strażak = false;
					}
				}
			}
		}
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