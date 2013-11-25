package net.fe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Grid {
	private Unit[][] grid;
	private Terrain[][] terrain;
	
	public Grid(int width, int height) {
		grid = new Unit[height][width];
		terrain = new Terrain[height][width];
	}
	
	private void makeTestGrid() {
		terrain = new Terrain[10][10];
		for(int i=0; i<10; i++) {
			for(int j=0; j<10; j++) {
				terrain[j][i] = Terrain.PLAIN;
			}
		}
	}

	/**
	 * Adds a unit to the grid at the given coordinates.
	 * @param u
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean addUnit(Unit u, int x, int y) {
		if(grid[y][x] != null)
			return false;
		grid[y][x] = u;
		u.xcoord = x;
		u.ycoord = y;
		return true;
	}
	
	public Unit removeUnit(int x, int y) {
		if(grid[y][x] == null)
			return null;
		Unit ans = grid[y][x];
		grid[y][x] = null;
		return ans;
	}
	
	public Terrain getTerrain(int x, int y){
		return terrain[y][x];
	}
	
	public Unit getUnit(int x, int y){
		return grid[y][x];
	}
	
	public Path getShortestPath(Unit unit, int x, int y) {
		int move = unit.get("Mov");
		Set<Node> closed = new HashSet<Node>();
		Set<Node> open = new HashSet<Node>();
		
		Node start = new Node(unit.xcoord, unit.ycoord);
		Node goal = new Node(x, y);
		start.g = 0;
		start.f = heuristic(start, goal);
		open.add(start);
		
		while(!open.isEmpty()) {
			//get node in open with best f score
			Node cur = null;
			for(Node n : open) {
				if(cur == null || n.f > cur.f) {
					cur = n;
				}
			}
			if(cur.equals(goal)) {
				return getPath(goal);
			}
			open.remove(cur);
			closed.add(cur);
			for(Node n : cur.getNeighbors()) {
				int g = cur.g + terrain[n.y][n.x].getMoveCost(unit.getTheClass());
				int f = g + heuristic(n, goal);
				if(closed.contains(n) && f >= n.f) {
					continue;
				} else if(!closed.contains(n) || f < n.f) {
					if(g > move)
						continue;
					n.parent = cur;
					n.g = g;
					n.f = f;
					open.add(n);
				}
			}
		}
		
		//failure
		return null;
	}
	
	private Path getPath(Node goal) {
		Path path = new Path();
		Node cur = goal;
		do {
			path.add(cur);
			cur = cur.parent;
		} while (cur != null);
		return path;
	}
	
	private int heuristic(Node a, Node b) {
		//Manhattan heuristic is pretty good because no diag mvmt
		return Math.abs(b.x-a.x) + Math.abs(b.y-a.y);
	}
	
}
