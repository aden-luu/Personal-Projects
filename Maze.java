import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldEnd;
import javalib.worldimages.WorldImage;
import tester.Tester;

//Represents a mutable collection of items
interface ICollection<T> {

  // Is this collection empty?
  boolean isEmpty();

  // EFFECT: adds the item to the collection
  void add(T item);

  // Returns the first item of the collection
  // EFFECT: removes that first item
  T pop();
}

class Stack<T> implements ICollection<T> {
  ArrayDeque<T> contents;

  Stack() {
    this.contents = new ArrayDeque<T>();
  }

  // check if the contents ArrayDeque<T> is empty
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // EFFECT: removes the first item of the ArrayDeque<T>
  // returns the first item of the collection
  public T pop() {
    return this.contents.pop();
  }

  // EFFECT: adds the item to the Front of the ArrayDeque<T>
  public void add(T item) {
    this.contents.addFirst(item);
  }
}

class Queue<T> implements ICollection<T> {
  ArrayDeque<T> contents;

  Queue() {
    this.contents = new ArrayDeque<T>();
  }

  // check if the contents ArrayDeque<T> is empty
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // EFFECT: removes the first item of the ArrayDeque<T>
  // returns the first item of the collection
  public T pop() {
    return this.contents.pop();
  }

  // EFFECT: adds the item to the Front of the ArrayDeque<T>
  public void add(T item) {
    this.contents.addLast(item); // NOTE: Different from Stack!
  }
}

class Vertex {

  int col;
  int row;

  ArrayList<Edge> edges; // list of the Vertex's edges

  // does this vertex have an edge in this direction?
  boolean north;
  boolean south;
  boolean east;
  boolean west;

  // color
  Color c;

  // for bfs and dfs
  boolean alreadySeen; // has the Vertex been visited by the algorithm
  boolean inFinalPath; // is this Vertex in the solution path

  Vertex(int col, int row) {
    this.col = col;
    this.row = row;
    this.edges = new ArrayList<Edge>();
    this.north = false;
    this.south = false;
    this.east = false;
    this.west = false;
    this.alreadySeen = false;
    this.inFinalPath = false;
    this.c = new Color(210, 210, 210);
  }

  // does this vertex equal the given object
  // for the purposes of the maze, we only care about the coordinates of the
  // vertex
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Vertex)) {
      return false;
    }
    else {
      Vertex v = (Vertex) o;
      return v.row == this.row && v.col == this.col;
    }
  }

  // hashcode override
  @Override
  public int hashCode() {
    return 10000 * this.col + this.row;
  }

  // draws a Vertex on a given world scene
  WorldScene place(WorldScene scene, int maxCols, int maxRows, boolean searching) {
    int s = Maze.SIZE;
    this.c = this.getColor(maxCols - 1, maxRows - 1);

    WorldImage rect = new RectangleImage(s, s, "solid", this.c).movePinhole(-s / 2, -s / 2);
    scene.placeImageXY(rect, this.col * s, this.row * s);

    if (!this.c.equals(new Color(210, 210, 210)) && searching) {
      this.c = Color.pink;
    }

    return scene;
  }

  // returns the color of the Vertex
  Color getColor(int maxCols, int maxRows) {

    if ((this.row == 0 && this.col == 0) || (this.row == maxRows && this.col == maxCols)) {
      return Color.pink;
    }

    if (this.alreadySeen) {
      return Color.pink;
    }

    return new Color(210, 210, 210);
  }
}

class Edge {

  int weight;

  Vertex src;
  Vertex dest;

  Edge(int weight, Vertex from, Vertex to) {
    this.weight = weight;
    this.src = from;
    this.dest = to;
  }
}

class Maze extends World {
  static int SIZE = 25; /* Maze.SIZE is 30 unless large maze (> 40x20) is provided */

  int cols;
  int rows;

  ArrayList<ArrayList<Vertex>> vertices;

  // Helps generate random mazes
  Random rand = new Random();
  long seed = rand.nextLong();

  // Start and Finish Vertices
  Vertex start;
  Vertex finish;

  // for search
  boolean searching; // is the algorithm currently running or "searching"
  ICollection<Vertex> worklist; // list of Vertices the algorithm needs to check
  ArrayDeque<Vertex> alreadySeen; // list of Vertices that have been visited

  // new Hashmap to connect Vertices for the final path displayed
  HashMap<Vertex, Vertex> searchingPath = new HashMap<Vertex, Vertex>();
  // list of all Vertices on the final path determined
  ArrayList<Vertex> finalPath = new ArrayList<Vertex>();

  WorldScene scene = new WorldScene(2000, 1600); // generates a new world scene

  // keep score of each algorithm and how many Vertices they visit
  boolean bfs; // helper boolean to determine which algorithm is being used
  int bfsScore; // Active score of BFS search
  int dfsScore; // Active score of DFS search

  Maze(int cols, int rows) {
    if (cols > 40 || rows > 20) {
      Maze.SIZE = 10;
      this.scene = new WorldScene(this.cols * Maze.SIZE + 400, this.rows * Maze.SIZE + 2);
    }

    this.cols = cols;
    this.rows = rows;
    this.vertices = new ArrayList<ArrayList<Vertex>>();
    this.colored = new ArrayList<Vertex>();
    this.alreadySeen = new ArrayDeque<Vertex>();
    this.rand.setSeed(this.seed);
    this.constructGraph();
    this.start = this.vertices.get(0).get(0);
    this.finish = this.vertices.get(rows - 1).get(cols - 1);
    this.searching = false;
    this.drawInitialMaze();
  }

  Maze(int cols, int rows, int seed) {
    if (cols > 40 || rows > 20) {
      Maze.SIZE = 10;
      this.scene = new WorldScene(this.cols * Maze.SIZE + 400, this.rows * Maze.SIZE + 2);
    }
    this.cols = cols;
    this.rows = rows;
    this.rand.setSeed(seed);
    this.vertices = new ArrayList<ArrayList<Vertex>>();
    this.colored = new ArrayList<Vertex>();
    this.alreadySeen = new ArrayDeque<Vertex>();
    this.constructGraph();
    this.start = this.vertices.get(0).get(0);
    this.finish = this.vertices.get(rows - 1).get(cols - 1);
    this.searching = false;
    this.drawInitialMaze();
  }

  // generates the maze
  void constructGraph() {
    this.createArray();
    this.assignEdges();
    this.kruskals();
    this.setWalls();
  }

  // creates an array with all the vertices
  void createArray() {
    for (int y = 0; y < this.rows; y++) {
      this.vertices.add(new ArrayList<Vertex>());
      for (int x = 0; x < this.cols; x++) {
        Vertex v = new Vertex(x, y);
        this.vertices.get(y).add(v);
      }
    }
  }

  // connect all adjacent vertices with randomly weighted edges
  void assignEdges() {
    for (ArrayList<Vertex> row : this.vertices) {
      for (Vertex v : row) {
        // check that v isn't a corner or an edge vertex
        if (v.col != 0) {
          v.edges.add(new Edge(this.rand.nextInt(100 * 60), v, row.get(v.col - 1)));
        }

        if (v.col != this.cols - 1) {
          v.edges.add(new Edge(this.rand.nextInt(100 * 60), v, row.get(v.col + 1)));
        }

        if (v.row != 0) {
          v.edges.add(
              new Edge(this.rand.nextInt(100 * 60), v, this.vertices.get(v.row - 1).get(v.col)));
        }

        if (v.row != this.rows - 1) {
          v.edges.add(
              new Edge(this.rand.nextInt(100 * 60), v, this.vertices.get(v.row + 1).get(v.col)));
        }
      }
    }
  }

  // create minimum spanning tree using Kruskal's algorithm
  void kruskals() {
    // get all edges and sort them by weight
    ArrayList<Edge> sortedEdges = this.sortEdges();

    // reset all vertices' edges
    for (ArrayList<Vertex> row : this.vertices) {
      for (Vertex v : row) {
        v.edges = new ArrayList<Edge>();
      }
    }

    // hashmap to keep track of representatives and nodes
    HashMap<Vertex, Vertex> representatives = new HashMap<Vertex, Vertex>();
    // initialize each vertex's representative to itself
    for (ArrayList<Vertex> row : this.vertices) {
      for (Vertex v : row) {
        representatives.put(v, v);
      }
    }

    // to keep track of number of edges
    int count = 0;

    // while #edges is less than #vertices - 1
    while (count < this.cols * this.rows - 1) {
      // get the edge with the lowest weight
      Edge e = sortedEdges.get(0);

      // if this edge's source's representative is not equal to its destination's
      // representative,
      // add this edge to the MST
      if (this.find(representatives, e.src) != this.find(representatives, e.dest)) {

        // add edges to form minimum spanning tree
        e.src.edges.add(e);
        e.dest.edges.add(new Edge(e.weight, e.dest, e.src));

        // perform union operation
        Vertex result = this.find(representatives, e.dest);
        representatives.remove(result);
        representatives.put(result, find(representatives, e.src));

        // advance the counter
        count++;
      }
      // discard this edge and go on to the next
      sortedEdges.remove(0);
    }
  }

  // returns the Vertex from the given representative
  Vertex find(HashMap<Vertex, Vertex> representatives, Vertex rep) {
    if (representatives.get(rep).equals(rep)) {
      return rep;
    }
    return find(representatives, representatives.get(rep));
  }

  // sort all edges and return sorted arraylist
  ArrayList<Edge> sortEdges() {
    ArrayList<Edge> result = new ArrayList<Edge>();

    for (ArrayList<Vertex> row : this.vertices) {
      for (Vertex v : row) {
        for (Edge e : v.edges) {
          result.add(e);
        }
      }
    }

    result.sort(new CompareWeights());
    return result;
  }

  // determine what "walls" each vertex should have
  void setWalls() {
    for (ArrayList<Vertex> row : this.vertices) {
      for (Vertex v : row) {
        for (Edge e : v.edges) {
          if (v.row != 0 && e.dest.equals(this.vertices.get(v.row - 1).get(v.col))) {
            v.north = true;
          }
          if (v.row != this.rows - 1 && e.dest.equals(this.vertices.get(v.row + 1).get(v.col))) {
            v.south = true;
          }
          if (v.col != this.cols - 1 && e.dest.equals(row.get(v.col + 1))) {
            v.east = true;
          }
          if (v.col != 0 && e.dest.equals(row.get(v.col - 1))) {
            v.west = true;
          }
        }
      }
    }
  }

  // draws the initial Maze
  void drawInitialMaze() {

    // draw the vertices
    for (ArrayList<Vertex> row : this.vertices) {
      for (Vertex v : row) {
        scene = v.place(scene, this.cols, this.rows, this.searching);
      }
    }

    // draw the walls
    WorldImage sidewaysWall = new RectangleImage(Maze.SIZE, 2, "solid", Color.darkGray)
        .movePinhole(-Maze.SIZE / 2, -1);
    WorldImage uprightWall = new RectangleImage(2, Maze.SIZE, "solid", Color.darkGray)
        .movePinhole(-1, -Maze.SIZE / 2);
    for (ArrayList<Vertex> row : this.vertices) {
      for (Vertex v : row) {
        if (!v.north) {
          scene.placeImageXY(sidewaysWall, v.col * Maze.SIZE, v.row * Maze.SIZE);
        }
        if (!v.south) {
          scene.placeImageXY(sidewaysWall, v.col * Maze.SIZE, (v.row + 1) * Maze.SIZE);
        }
        if (!v.east) {
          scene.placeImageXY(uprightWall, (v.col + 1) * Maze.SIZE, v.row * Maze.SIZE);
        }
        if (!v.west) {
          scene.placeImageXY(uprightWall, v.col * Maze.SIZE, v.row * Maze.SIZE);
        }
      }
    }
  }

  ArrayList<Vertex> colored;
  
  // gets the color of the Vertex based on the distance from the start
  public Color getColor(Vertex v) {
    int endCol = this.finish.col;
    int endRow = this.finish.row;
    double hue = Math.hypot(v.col, v.row)
        / Math.hypot(endCol - this.start.col, endRow - this.start.row) * 67 / 72;
    return Color.getHSBColor((float) hue, 1, 1);
  }

  // generates the maze image and world scene
  public WorldScene makeScene() {

    int sT = Maze.SIZE / 4;
    int s = Maze.SIZE - sT;

    WorldImage rect = new RectangleImage(s, s, "solid", Color.pink).movePinhole(-s / 2, -s / 2);

    for (ArrayList<Vertex> row : this.vertices) {
      for (Vertex v : row) {
        if (v.alreadySeen && !this.colored.contains(v)) {

          this.scene.placeImageXY(rect, v.col * Maze.SIZE + sT, v.row * Maze.SIZE + sT);
          this.colored.add(v);
        }
      }
    }

    for (ArrayList<Vertex> row : this.vertices) {
      for (Vertex v : row) {
        if (v.inFinalPath) {
          WorldImage rect3 = new RectangleImage(s - 1, s - 1, "solid", getColor(v))
              .movePinhole(-s / 2, -s / 2);
          WorldImage rect4 = new RectangleImage(s, s, "solid", new Color(210, 210, 210))
              .movePinhole(-s / 2, -s / 2);
          scene.placeImageXY(rect4, v.col * Maze.SIZE + sT, v.row * Maze.SIZE + sT);
          scene.placeImageXY(rect3, v.col * Maze.SIZE + sT, v.row * Maze.SIZE + sT);
        }
      }
    }

    WorldImage scoreB = new TextImage("BFS: " + Integer.toString(this.bfsScore), 400 / Maze.SIZE,
        Color.black);

    WorldImage scoreD = new TextImage("DFS: " + Integer.toString(this.dfsScore), 400 / Maze.SIZE,
        Color.black);

    scene.placeImageXY(new RectangleImage(1800 / Maze.SIZE, 1000 / Maze.SIZE, "solid", Color.white),
        this.cols * Maze.SIZE + 100, Maze.SIZE + 80);
    scene.placeImageXY(scoreB, this.cols * Maze.SIZE + 100, Maze.SIZE + 80);

    scene.placeImageXY(new RectangleImage(1800 / Maze.SIZE, 1000 / Maze.SIZE, "solid", Color.white),
        this.cols * Maze.SIZE + 100, Maze.SIZE + 180);
    scene.placeImageXY(scoreD, this.cols * Maze.SIZE + 100, Maze.SIZE + 180);

    return this.scene;
  }

  // resets the board with the same maze (unsolved), keeping scores
  void reset(Vertex start, Vertex finish) {

    if (cols > 40 || rows > 20) {
      Maze.SIZE = 10;
    }
    this.vertices = new ArrayList<ArrayList<Vertex>>();
    this.rand.setSeed(this.seed);
    this.colored = new ArrayList<Vertex>();
    this.alreadySeen = new ArrayDeque<Vertex>();
    this.constructGraph();
    this.start = this.vertices.get(0).get(0);
    this.finish = this.vertices.get(rows - 1).get(cols - 1);
    this.searching = false;
    this.drawInitialMaze();
  }

  // reconstruction method that helps generate the final path
  // image of final path is created in WorldScene
  public void finalPath(Vertex curr) {
    curr.inFinalPath = true;
    if (curr.equals(this.start)) {
      return;
    }
    this.finalPath(this.searchingPath.get(curr));
  }

  // help with the animation of the search
  @Override
  public void onTick() {

    if (this.searching) {
      searchHelp();
    }
  }

  // takes inputs from keys pressed to execute search and reset
  @Override
  public void onKeyEvent(String key) {
    if (this.searching) {
      return;
    }

    if (key.equals("b")) {
      this.searching = true;
      this.worklist = new Queue<Vertex>();
      this.alreadySeen = new ArrayDeque<Vertex>();
      this.worklist.add(start);
      this.bfs = true;
      this.bfsScore = 0;
    }

    if (key.equals("d")) {
      this.searching = true;
      this.worklist = new Stack<Vertex>();
      this.alreadySeen = new ArrayDeque<Vertex>();
      this.worklist.add(start);
      this.bfs = false;
      this.dfsScore = 0;
    }

    if (key.equals("r")) {
      this.reset(this.start, this.finish);
    }

    if (key.equals("p")) {
      this.searching = false;
    }
  }
  
  // end the world and display the winner for the maze
  @Override
  public WorldEnd worldEnds() {

    if (this.bfsScore > 0 && this.dfsScore > 0 && !this.searching) {
      return new WorldEnd(true, this.makeLastScene());
    }
    return new WorldEnd(false, this.makeScene());
  }
  
  // Displays last scene
  WorldScene makeLastScene() {
    this.scene = this.getEmptyScene();

    this.drawInitialMaze();
    this.makeScene();

    int dfsFinalScore = this.dfsScore;
    int bfsFinalScore = this.bfsScore;

    WorldImage bfsWin = new TextImage("BFS Wins!", 30, Color.black);
    WorldImage dfsWin = new TextImage("DFS Wins!", 30, Color.black);

    if (bfsFinalScore > 0 && dfsFinalScore > 0 && bfsFinalScore < dfsFinalScore
        && !this.searching) {
      scene.placeImageXY(bfsWin, this.cols * Maze.SIZE + 100, Maze.SIZE + 300);
    }
    else {
      scene.placeImageXY(dfsWin, this.cols * Maze.SIZE + 100, Maze.SIZE + 300);
    }

    return scene;
  }

  // implementation of the abstraction of bfs and dfs
  void searchHelp() {
    // As long as the worklist isn't empty...
    if (!worklist.isEmpty()) {
      Vertex current = worklist.pop();
      if (current.equals(finish)) {
        this.searching = false;
        this.finalPath(this.finish);
        return; // Success!
      }
      else if (alreadySeen.contains(current)) {
        // does nothing: we've already seen this one
      }
      else {
        // add all the neighbors of next to the worklist for further processing
        for (Edge e : current.edges) {
          if (!this.alreadySeen.contains(e.dest)) {
            worklist.add(e.dest);
            this.searchingPath.put(e.dest, current);
          }
        }
        // add next to alreadySeen, since we're done with it

        if (this.bfs) {
          bfsScore += 1;
        }
        else {
          dfsScore += 1;
        }

        alreadySeen.addFirst(current);

        current.alreadySeen = true;
      }
    }
  }

}

class CompareWeights implements Comparator<Edge> {
  public int compare(Edge e1, Edge e2) {
    return e1.weight - e2.weight;
  }
}

class ExamplesMaze {
  Maze maze;
  Maze testMaze;
  Maze testMazeSmall;
  Maze tm;

  ArrayList<ArrayList<Vertex>> testSmallNodes;

  Vertex v1;
  Vertex v2;
  Vertex v3;

  Stack<Vertex> s1;
  Queue<Vertex> q1;

  void init() {
    this.maze = new Maze(40, 20);
    this.testMaze = new Maze(10, 6, 425);
    this.testMazeSmall = new Maze(3, 2, 425);
    this.tm = new Maze(2, 2, 425);

    this.testSmallNodes = new ArrayList<ArrayList<Vertex>>(Arrays.asList(
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 0), new Vertex(1, 0), new Vertex(2, 0))),
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 1), new Vertex(1, 1), new Vertex(2, 1)))));

    this.v1 = new Vertex(34, 18);
    this.v2 = new Vertex(28, 18);
    this.v3 = new Vertex(34, 18);

    this.s1 = new Stack<Vertex>();
    this.q1 = new Queue<Vertex>();
  }

  // All mazes generated automatically apply our kruskals method to generate
  // the minimum spanning tree

  /*
   * USE THIS TO PLAY: testMaze for deterministic behavior, maze for random
   */
  void testGame(Tester t) {
    this.init();
    // this.testMazeSmall.bigBang(testMaze.cols * Maze.SIZE, testMaze.rows *
    // Maze.SIZE, 0.01);
    this.maze.bigBang(maze.cols * Maze.SIZE + 400, maze.rows * Maze.SIZE + 2, .001);
  }

  void testCreateArray(Tester t) {
    this.init();
    this.testMazeSmall.vertices = new ArrayList<ArrayList<Vertex>>();
    this.testMazeSmall.createArray();
    t.checkExpect(this.testMazeSmall.vertices, this.testSmallNodes);
  }

  void testSortEdges(Tester t) {
    this.init();
    ArrayList<Edge> sorted = this.testMazeSmall.sortEdges();

    for (int i = 0; i < sorted.size(); i++) {
      if (i > 0) {
        t.checkExpect(sorted.get(i).weight >= sorted.get(i - 1).weight, true);
      }
    }
  }

  void testSetWalls(Tester t) {
    this.init();

    this.testMazeSmall.setWalls();
    ArrayList<ArrayList<Vertex>> vList = this.testMazeSmall.vertices;

    t.checkExpect(vList.get(0).get(0).north, false);
    t.checkExpect(vList.get(0).get(0).south, true);
    t.checkExpect(vList.get(0).get(0).east, false);
    t.checkExpect(vList.get(0).get(0).west, false);

    t.checkExpect(vList.get(0).get(1).north, false);
    t.checkExpect(vList.get(0).get(1).south, false);
    t.checkExpect(vList.get(0).get(1).east, true);
    t.checkExpect(vList.get(0).get(1).west, false);

    t.checkExpect(vList.get(0).get(2).north, false);
    t.checkExpect(vList.get(0).get(2).south, true);
    t.checkExpect(vList.get(0).get(2).east, false);
    t.checkExpect(vList.get(0).get(2).west, true);

    t.checkExpect(vList.get(1).get(0).north, true);
    t.checkExpect(vList.get(1).get(0).south, false);
    t.checkExpect(vList.get(1).get(0).east, true);
    t.checkExpect(vList.get(1).get(0).west, false);

    t.checkExpect(vList.get(1).get(1).north, false);
    t.checkExpect(vList.get(1).get(1).south, false);
    t.checkExpect(vList.get(1).get(1).east, true);
    t.checkExpect(vList.get(1).get(1).west, true);

    t.checkExpect(vList.get(1).get(2).north, true);
    t.checkExpect(vList.get(1).get(2).south, false);
    t.checkExpect(vList.get(1).get(2).east, false);
    t.checkExpect(vList.get(1).get(2).west, true);

  }

  void testMakeScene(Tester t) {
    this.init();
    WorldScene scene = new WorldScene(2000, 1600);

    Maze testMazeTiny = new Maze(1, 1, 1);

    WorldImage vert = new RectangleImage(25, 25, "solid", Color.pink).movePinhole(-12, -12);
    WorldImage side = new RectangleImage(2, 25, "solid", new Color(64, 64, 64)).movePinhole(-1,
        -12);
    WorldImage up = new RectangleImage(25, 2, "solid", new Color(64, 64, 64)).movePinhole(-12, -1);

    scene.placeImageXY(vert, 0, 0);
    scene.placeImageXY(up, 0, 0);
    scene.placeImageXY(up, 0, 25);
    scene.placeImageXY(side, 25, 0);
    scene.placeImageXY(side, 0, 0);

    WorldImage bfs = new TextImage("BFS: 0", 16, Color.black);
    WorldImage dfs = new TextImage("DFS: 0", 16, Color.black);

    WorldImage whiteBG = new RectangleImage(72, 40, "solid", Color.white);

    scene.placeImageXY(whiteBG, 125, 105);
    scene.placeImageXY(bfs, 125, 105);
    scene.placeImageXY(whiteBG, 125, 205);
    scene.placeImageXY(dfs, 125, 205);

    t.checkExpect(testMazeTiny.makeScene(), scene);
    t.checkExpect(scene, testMazeTiny.makeScene());
  }

  void testFind(Tester t) {
    this.init();

    HashMap<Vertex, Vertex> representatives = new HashMap<Vertex, Vertex>();

    for (ArrayList<Vertex> row : this.testMazeSmall.vertices) {
      for (Vertex v : row) {
        representatives.put(v, v);
      }
    }

    Vertex ver1 = new Vertex(10, 12);
    Vertex ver2 = new Vertex(10, 12);
    Vertex ver3 = new Vertex(10, 12);

    representatives.put(ver1, ver2);
    representatives.put(ver2, ver3);

    t.checkExpect(this.testMazeSmall.find(representatives, testMazeSmall.vertices.get(0).get(0)),
        testMazeSmall.vertices.get(0).get(0));
    t.checkExpect(this.testMazeSmall.find(representatives, testMazeSmall.vertices.get(1).get(2)),
        testMazeSmall.vertices.get(1).get(2));
    t.checkExpect(this.testMazeSmall.find(representatives, ver2), ver1);
    t.checkExpect(this.testMazeSmall.find(representatives, ver3), ver1);
  }

  void testAssignEdges(Tester t) {
    this.init();
    this.testMazeSmall.assignEdges();

    ArrayList<Edge> edges = new ArrayList<Edge>();

    for (ArrayList<Vertex> row : testMazeSmall.vertices) {
      for (Vertex v : row) {
        for (Edge e : v.edges) {
          edges.add(e);
        }
      }
    }

    // every vertex should be connected to every other one -> 24 edges
    t.checkExpect(edges.size(), 24);
    t.checkExpect(edges.get(0).weight, 787);
    t.checkExpect(edges.get(18).weight, 1200);
  }

  void testKruskals(Tester t) {
    this.init();

    // kruskals is already called when you generate a new maze
    Maze m2 = new Maze(2, 2, 425);

    // generates a 2x2 maze with 1 edge
    m2.kruskals();

    // tests show that each Vertex is connected, but the Vertexes don't loop,
    // if you start in the top left of the maze (or anywhere),
    // you cannot get back to where you start through any Vertex

    // shows that the top left Vertex of the maze has an edge with the bottom left
    // there isn't an edge to connect the top left and top right
    t.checkExpect(m2.vertices.get(0).get(0).east, false);
    t.checkExpect(m2.vertices.get(0).get(0).west, false);
    t.checkExpect(m2.vertices.get(0).get(0).north, false);
    t.checkExpect(m2.vertices.get(0).get(0).south, true);

    // shows that the top right Vertex of the maze has an edge with the bottom right
    t.checkExpect(m2.vertices.get(0).get(1).east, false);
    t.checkExpect(m2.vertices.get(0).get(1).west, false);
    t.checkExpect(m2.vertices.get(0).get(1).north, false);
    t.checkExpect(m2.vertices.get(0).get(1).south, true);

    // shows that the bottom left Vertex of the maze has an edge with top left and
    // bottom right
    t.checkExpect(m2.vertices.get(1).get(0).east, true);
    t.checkExpect(m2.vertices.get(1).get(0).west, false);
    t.checkExpect(m2.vertices.get(1).get(0).north, true);
    t.checkExpect(m2.vertices.get(1).get(0).south, false);

    // shows that the bottom right Vertex of the maze has an edge with the bottom
    // left and top right
    t.checkExpect(m2.vertices.get(1).get(1).east, false);
    t.checkExpect(m2.vertices.get(1).get(1).west, true);
    t.checkExpect(m2.vertices.get(1).get(1).north, true);
    t.checkExpect(m2.vertices.get(1).get(1).south, false);

    // other tests

    // amount of edges that top left node has (1)
    t.checkExpect(m2.vertices.get(0).get(0).edges.size(), 1);

    // top left node has an edge with bottom left node and NOT the top right node
    t.checkExpect(m2.vertices.get(0).get(0).edges.get(0).dest, m2.vertices.get(1).get(0));

    // amount of edges that bottom left node has (2)
    t.checkExpect(m2.vertices.get(1).get(0).edges.size(), 2);

    // bottom left node has an edge with top left node
    t.checkExpect(m2.vertices.get(1).get(0).edges.get(0).dest, m2.vertices.get(0).get(0));
    // bottom left node has an edge with bottom right node
    t.checkExpect(m2.vertices.get(1).get(0).edges.get(1).dest, m2.vertices.get(1).get(1));

    // amount of edges that the bottom right node has (2)
    t.checkExpect(m2.vertices.get(1).get(0).edges.size(), 2);

    // bottom right node has an edge with top right node
    t.checkExpect(m2.vertices.get(1).get(1).edges.get(0).dest, m2.vertices.get(0).get(1));
    // bottom right node has an edge with bottom left node
    t.checkExpect(m2.vertices.get(1).get(1).edges.get(1).dest, m2.vertices.get(1).get(0));

    // amount of edges that the top right node has (1)
    t.checkExpect(m2.vertices.get(0).get(1).edges.size(), 1);

    // top right node has an edge with bottom right node and NOT the top left node
    t.checkExpect(m2.vertices.get(0).get(1).edges.get(0).dest, m2.vertices.get(1).get(1));
  }

  void testConstructGraph(Tester t) {
    this.init();

    // constructgraph is called when you create a maze
    Maze testMazeTiny = new Maze(1, 1, 4425);

    Vertex v = new Vertex(0, 0);
    v.c = Color.pink;

    ArrayList<ArrayList<Vertex>> verts = new ArrayList<ArrayList<Vertex>>(
        Arrays.asList(new ArrayList<Vertex>(Arrays.asList(v))));

    Maze m2 = new Maze(2, 1, 425);
    Vertex v1 = new Vertex(0, 0);
    Vertex v2 = new Vertex(1, 0);
    v1.east = true;
    v2.west = true;

    v2.c = new Color(255, 175, 175);
    v1.c = new Color(255, 175, 175);
    Edge e1 = new Edge(787, v1, v2);
    Edge e2 = new Edge(787, v2, v1);
    v1.edges.add(e1);
    v2.edges.add(e2);

    ArrayList<ArrayList<Vertex>> verts2 = new ArrayList<ArrayList<Vertex>>(
        Arrays.asList(new ArrayList<Vertex>(Arrays.asList(v1, v2))));

    t.checkExpect(m2.vertices, verts2);
    t.checkExpect(testMazeTiny.vertices, verts);
  }

  void testEquals(Tester t) {
    this.init();

    t.checkExpect(this.v1.equals(v1), true);
    t.checkExpect(this.v1.equals(v2), false);
    t.checkExpect(this.v1.equals(v3), true);
  }

  void testHashCode(Tester t) {
    this.init();

    t.checkExpect(this.v1.hashCode(), 340018);
    t.checkExpect(this.v2.hashCode(), 280018);
    t.checkExpect(this.v3.hashCode(), 340018);
  }

  void testPlace(Tester t) {
    this.init();

    Vertex x1 = new Vertex(0, 0);
    Vertex x2 = new Vertex(1, 2);

    WorldScene test = this.testMazeSmall.getEmptyScene();

    WorldScene mt = this.testMazeSmall.getEmptyScene();

    WorldImage rect = new RectangleImage(26, 26, "solid", Color.green).movePinhole(-13, -13);
    test.placeImageXY(rect, 0, 0);

    t.checkExpect(x1.place(mt, 3, 2, false), test);

    WorldImage rect1 = new RectangleImage(26, 26, "solid", new Color(210, 210, 210))
        .movePinhole(-13, -13);
    test.placeImageXY(rect1, 26, 0);

    t.checkExpect(x2.place(mt, 3, 2, false), test);
  }

  void testGetColorVertex(Tester t) {
    this.init();

    t.checkExpect(this.v1.getColor(100, 60), new Color(210, 210, 210));
    t.checkExpect(this.v1.getColor(34, 18), Color.pink);
    t.checkExpect(new Vertex(0, 0).getColor(100, 60), Color.pink);
  }

  void testDrawInitialMaze(Tester t) {
    this.init();
    WorldScene scene = new WorldScene(2000, 1600);

    Maze testMazeTiny = new Maze(1, 1, 1);

    WorldImage vert = new RectangleImage(25, 25, "solid", Color.pink).movePinhole(-12, -12);
    WorldImage side = new RectangleImage(2, 25, "solid", new Color(64, 64, 64)).movePinhole(-1,
        -12);
    WorldImage up = new RectangleImage(25, 2, "solid", new Color(64, 64, 64)).movePinhole(-12, -1);

    scene.placeImageXY(vert, 0, 0);
    scene.placeImageXY(side, 0, 0);
    scene.placeImageXY(up, 0, 25);
    scene.placeImageXY(side, 25, 0);
    scene.placeImageXY(up, 0, 0);

    testMazeTiny.drawInitialMaze();

    t.checkExpect(testMazeTiny.scene, scene);
    t.checkExpect(scene, testMazeTiny.scene);
  }

  // getColor in Maze
  void testGetColorMaze(Tester t) {
    this.init();

    Vertex v1 = this.testMaze.vertices.get(0).get(0);
    Vertex v2 = this.testMaze.vertices.get(3).get(2);
    Vertex v3 = this.testMaze.vertices.get(2).get(5);

    t.checkExpect(this.testMaze.getColor(v1), Color.red);
    t.checkExpect(this.testMaze.getColor(v2), new Color(11, 255, 0));
    t.checkExpect(this.testMaze.getColor(v3), new Color(0, 255, 235));

  }

  void testStackIsEmpty(Tester t) {
    this.init();

    // is a newly initialized Stack empty?
    // expect it to be
    t.checkExpect(s1.isEmpty(), true);

    // add a vertex to the Stack
    s1.add(v1);

    // expect Stack to no longer be empty
    t.checkExpect(s1.isEmpty(), false);
  }

  void testQueueIsEmpty(Tester t) {
    this.init();

    // is newly initialized Queue empty?
    // expect to be true
    t.checkExpect(q1.isEmpty(), true);

    // add vertex to Queue
    q1.add(v2);

    // expect Queue to no longer be empty
    t.checkExpect(q1.isEmpty(), false);
  }

  void testStackAdd(Tester t) {
    this.init();

    t.checkExpect(s1.isEmpty(), true);

    s1.add(v3);

    t.checkExpect(this.s1.contents.getFirst(), v3);

    this.s1.add(v2);

    t.checkExpect(this.s1.contents.getFirst(), v2);
  }

  void testQueueAdd(Tester t) {
    this.init();

    t.checkExpect(q1.isEmpty(), true);

    q1.add(v1);

    t.checkExpect(this.q1.contents.getFirst(), v1);

    this.q1.add(v2);

    t.checkExpect(this.q1.contents.getFirst(), v1);
  }

  void testStackPop(Tester t) {
    this.init();

    t.checkExpect(s1.isEmpty(), true);

    s1.add(v1);
    s1.add(v2);

    t.checkExpect(s1.contents.pop(), v2);
    t.checkExpect(s1.contents.pop(), v1);

    t.checkExpect(s1.isEmpty(), true);
  }

  void testQueuePop(Tester t) {
    this.init();

    t.checkExpect(q1.isEmpty(), true);

    q1.add(v1);
    q1.add(v2);

    t.checkExpect(q1.contents.pop(), v1);
    t.checkExpect(q1.contents.pop(), v2);

    t.checkExpect(q1.isEmpty(), true);
  }

  void testOnTick(Tester t) {
    this.init();

    this.tm.onKeyEvent("b");

    this.tm.onTick();

    t.checkExpect(tm.searching, true);

    t.checkExpect(tm.alreadySeen.size(), 1);
  }

  void testOnKeyEvent(Tester t) {
    this.init();

    this.tm.onKeyEvent("a");

    t.checkExpect(tm, tm);

    this.tm.onKeyEvent("d");

    t.checkExpect(tm.searching, true);

  }

  void searchHelp(Tester t) {
    this.init();

    this.tm.onKeyEvent("d");

    this.tm.onTick();

    t.checkExpect(tm.searching, true);

    t.checkExpect(tm.alreadySeen.size(), 1);

  }

  void testFinalPath(Tester t) {
    this.init();

    Maze tiny = new Maze(2, 2, 1);

    tiny.onKeyEvent("b");

    while (!tiny.worklist.isEmpty()) {
      tiny.searchHelp();
    }
    tiny.finalPath(tiny.finish);

    Vertex v1 = tiny.vertices.get(0).get(0);
    Vertex v2 = tiny.vertices.get(0).get(1);
    Vertex v3 = tiny.vertices.get(1).get(0);
    Vertex v4 = tiny.vertices.get(1).get(1);

    t.checkExpect(v1.inFinalPath, true);
    t.checkExpect(v2.inFinalPath, false);
    t.checkExpect(v3.inFinalPath, true);
    t.checkExpect(v4.inFinalPath, true);
  }

  void testReset(Tester t) {
    this.init();

    Maze tiny = new Maze(2, 2, 1);

    tiny.onKeyEvent("b");

    while (!tiny.worklist.isEmpty()) {
      tiny.searchHelp();
    }
    tiny.finalPath(tiny.finish);

    Vertex v1 = tiny.vertices.get(0).get(0);
    Vertex v2 = tiny.vertices.get(0).get(1);
    Vertex v3 = tiny.vertices.get(1).get(0);
    Vertex v4 = tiny.vertices.get(1).get(1);

    tiny.reset(tiny.start, tiny.finish);

    t.checkExpect(v1.c, new Color(255, 175, 175));
    t.checkExpect(v2.c, new Color(210, 210, 210));
    t.checkExpect(v3.c, new Color(210, 210, 210));
    t.checkExpect(v4.c, new Color(255, 175, 175));
  }
}