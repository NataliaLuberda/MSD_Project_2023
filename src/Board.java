import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

public class Board extends JComponent implements MouseInputListener, ComponentListener {
	private static final long serialVersionUID = 1L;
	private Point[][] pointer;
	private int size = 10;
	public int editType=0;
	private String version = "moore";
	private int repulsionRadius;	
	private static final int SFMAX = 100000;
	private int iteration;
	private static int fireConst = 2; //~ 10 / (10 - 4)

	public Board(int length, int height) {
		addMouseListener(this);
		addComponentListener(this);
		addMouseMotionListener(this);
		setBackground(Color.WHITE);
		setOpaque(true);
		initialize(length, height);
		repulsionRadius = (int) (0.06*Math.min(length,height));
		iteration = 0;
	}

	public void iteration() {
		for (int x = 1; x < pointer.length - 1; ++x){
			for (int y = 1; y < pointer[x].length - 1; ++y){
				if(pointer[x][y].type != 1 && pointer[x][y].type != 2){
					pointer[x][y].blocked = false;
				}
			}
		}
		for (int x = 1; x < pointer.length - 1; ++x){
			for (int y = 1; y < pointer[x].length - 1; ++y){
				int tmp = pointer[x][y].move();
				if(tmp == 1){
					clearFire(pointer[x][y]);
				}else if(pointer[x][y].type == 1){
					makeBarier(pointer[x][y]);
				}else if(pointer[x][y].type ==4 && iteration % fireConst == 0){
					calculateFire(pointer[x][y]);
				}
			}
		}
		this.repaint();
		iteration++;
	}

	public void clear() {
		for (int x = 0; x < pointer.length; ++x)
			for (int y = 0; y < pointer[x].length; ++y) {
				pointer[x][y].clear();
			}
		calculateField();
		this.repaint();
	}

	private void initialize(int length, int height) {
		pointer = new Point[length][height];
		ArrayList<Point> walls = new ArrayList<Point>();
		for (int x = 0; x < pointer.length; ++x){
			for (int y = 0; y < pointer[x].length; ++y){
				pointer[x][y] = new Point(x,y);
				if(x==0 || y==0 || x == pointer.length-1 || y == pointer[x].length-1){
					pointer[x][y].type = 1;
					pointer[x][y].staticField = 1000000;
					walls.add(pointer[x][y]);
				}
			}
		}

		for (int x = 1; x < pointer.length-1; ++x) {
			for (int y = 1; y < pointer[x].length-1; ++y) {
					if(version == "moore"){
						pointer[x][y].addNeighbor(pointer[x+1][y]);
						pointer[x][y].addNeighbor(pointer[x+1][y+1]);
						pointer[x][y].addNeighbor(pointer[x][y+1]);
						pointer[x][y].addNeighbor(pointer[x+1][y-1]);
						pointer[x][y].addNeighbor(pointer[x][y-1]);
						pointer[x][y].addNeighbor(pointer[x-1][y-1]);
						pointer[x][y].addNeighbor(pointer[x-1][y+1]);
						pointer[x][y].addNeighbor(pointer[x-1][y]);
					}else if (version == "neuman"){
						pointer[x][y].addNeighbor(pointer[x+1][y]);
						pointer[x][y].addNeighbor(pointer[x][y+1]);
						pointer[x][y].addNeighbor(pointer[x][y-1]);
						pointer[x][y].addNeighbor(pointer[x-1][y]);
					}
				}
		}

		while(!walls.isEmpty()){
			Point pointe = walls.get(0);
			makeBarier(pointe);
			walls.remove(0);
		}
		
	}
	
	public void makeBarier(Point pointe){//create barier fo walls
		if(!pointe.blocked){
			int x_c = pointe.x;
			int y_c = pointe.y;
			for (int i = x_c - this.repulsionRadius; i <= x_c + repulsionRadius; i++) {
				for (int j = y_c - repulsionRadius; j <= y_c + repulsionRadius; j++) {
					if (i >= 0 && i < pointer.length && j >= 0 && j < pointer[i].length) {
						if (pointer[i][j].type != 1 && pointer[i][j].type != 2) { // if pedestrian
							double distance = Math.sqrt((i - x_c) * (i - x_c) + (j - y_c) * (j - y_c));
							if (distance < repulsionRadius && distance != 0) {
								double repulsion = 0.7 / distance;
								pointer[i][j].staticField = pointer[i][j].staticField + repulsion;
							}
						}
					}
				}
			}
			pointe.blocked = true;
		}
	}

	public void calculateField(){
		ArrayList<Point> toCheck = new ArrayList<Point>();
		for (int x = 1; x < pointer.length-1; ++x) {
			for (int y = 1; y < pointer[x].length-1; ++y) {
				if(pointer[x][y].type == 2){
					pointer[x][y].staticField = 0;
					toCheck.addAll(pointer[x][y].neighbors);
					int x_c = pointer[x][y].x;
					int y_c = pointer[x][y].y;
					if(!pointer[x][y].blocked){
						pointer[x][y].blocked = true;
						for (int i = x_c - this.repulsionRadius; i <= x_c + repulsionRadius; i++) {//make new calculate statistic field
							for (int j = y_c - repulsionRadius; j <= y_c + repulsionRadius; j++) {
								if (i >= 0 && i < pointer.length && j >= 0 && j < pointer[i].length) {
									if (pointer[i][j].type != 1 && pointer[i][j].type != 2) { // if pedestrian
										double distance = Math.sqrt((i - x_c) * (i - x_c) + (j - y_c) * (j - y_c));
										if (distance < repulsionRadius && distance != 0) {
											double repulsion = 0.5 / distance;
											pointer[i][j].staticField = pointer[i][j].staticField-repulsion;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		Point currPoint;
		while(!toCheck.isEmpty()){
			currPoint = toCheck.get(0);
			if(currPoint.type!= 2 && currPoint.type!= 1 && currPoint.calcStaticField()){
				toCheck.addAll(currPoint.neighbors);
        	}
			toCheck.remove(0);
		}
	}
	


	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		g.setColor(Color.GRAY);
		drawNetting(g, size);
	}

	private void drawNetting(Graphics g, int gridSpace) {
		Insets insets = getInsets();
		int firstX = insets.left;
		int firstY = insets.top;
		int lastX = this.getWidth() - insets.right;
		int lastY = this.getHeight() - insets.bottom;

		int x = firstX;
		while (x < lastX) {
			g.drawLine(x, firstY, x, lastY);
			x += gridSpace;
		}

		int y = firstY;
		while (y < lastY) {
			g.drawLine(firstX, y, lastX, y);
			y += gridSpace;
		}

		for (x = 1; x < pointer.length-1; ++x) {
			for (y = 1; y < pointer[x].length-1; ++y) {
				if(pointer[x][y].type==0){
					Double staticField = pointer[x][y].staticField;
					float intensity = (float) (staticField/100);
					if (intensity > 1.0) {
						intensity = (Float) 1.0f;
					}
					g.setColor(new Color(intensity, intensity,intensity ));

					float smoke = pointer[x][y].smokeDensity;
					if (smoke < 10 && smoke >0.3) {
						float density = smoke / 10;
						if (density > 1.0) {
							density = 1.0f;
						}
						g.setColor(new Color(1, density, density/2));
					}
				}
				else if (pointer[x][y].type==1){
					g.setColor(new Color(0.79f, 0.92f, 0.30f, 1f));
				}
				else if (pointer[x][y].type==2){
					g.setColor(new Color(0.0f, 1.0f, 0.0f, 0.7f));
				}
				if (pointer[x][y].isPedestrian){
					g.setColor(new Color(0.0f, 0.0f, 1.0f, 0.7f));
				}
				if (pointer[x][y].type == 4){
					g.setColor(new Color(1f, 0.0f, 0.0f, 0.7f));
				}
				g.fillRect((x * size) + 1, (y * size) + 1, (size - 1), (size - 1));
			}
		}

	}

	public void mouseClicked(MouseEvent e) {
		int x = e.getX() / size;
		int y = e.getY() / size;
		if ((x < pointer.length) && (x > 0) && (y < pointer[x].length) && (y > 0) && pointer[x][y].type != 1 && pointer[x][y].type != 2&&pointer[x][y].type != 4) {
			if(editType==3){
				pointer[x][y].isPedestrian = true;
				pointer[x][y].type = editType;
			}
			else{
				pointer[x][y].type = editType;
				if(editType == 1){
					makeBarier(pointer[x][y]);
				}else if(editType == 4){
					calculateFire(pointer[x][y]);
				}
			}
			this.repaint();
		}
	}

	public void componentResized(ComponentEvent e) {
		int dlugosc = (this.getWidth() / size) + 1;
		int wysokosc = (this.getHeight() / size) + 1;
		initialize(dlugosc, wysokosc);
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX() / size;
		int y = e.getY() / size;
		if ((x < pointer.length) && (x > 0) && (y < pointer[x].length) && (y > 0) && pointer[x][y].type != 1 && pointer[x][y].type != 2&&pointer[x][y].type != 4) {
			if(editType==3){
				pointer[x][y].isPedestrian=true;
			}
			else{
				pointer[x][y].type= editType;
				if(editType == 1){
					makeBarier(pointer[x][y]);
				}else if(editType == 4){
					calculateFire(pointer[x][y]);
				}
			}
			this.repaint();
		}
	}


	public void calculateFire(Point fire) {
		fire.smokeDensity = 10000;
		int x_c = fire.x;
		int y_c = fire.y;
		for (int i = x_c - repulsionRadius; i <= x_c + repulsionRadius; i++) {
			for (int j = y_c - repulsionRadius; j <= y_c + repulsionRadius; j++) {
				if (i >= 0 && i < pointer.length && j >= 0 && j < pointer[i].length) {
					if (pointer[i][j].type != 1 && pointer[i][j].type != 2) { // if pedestrian
						double distance = Math.sqrt((i - x_c) * (i - x_c) + (j - y_c) * (j - y_c));
						if (distance < repulsionRadius && distance!= 0) {
							double repulsion = 0.02 / distance;
							pointer[i][j].smokeDensity += repulsion;
						}
					}
				}
			}
		}
	}

	public void clearFire(Point fire) {
		fire.smokeDensity = 0;
		fire.type = 0;
		int x_c = fire.x;
		int y_c = fire.y;
		for (int i = x_c - repulsionRadius; i <= x_c + repulsionRadius; i++) {
			for (int j = y_c - repulsionRadius; j <= y_c + repulsionRadius; j++) {
				if (i >= 0 && i < pointer.length && j >= 0 && j < pointer[i].length) {
					if (pointer[i][j].type != 1 && pointer[i][j].type != 2) { // if pedestrian
						double distance = Math.sqrt((i - x_c) * (i - x_c) + (j - y_c) * (j - y_c));
						if (distance < repulsionRadius) {
							pointer[i][j].smokeDensity = 0;
						}
					}
				}
			}
		}
	}

       


	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}
	
}
