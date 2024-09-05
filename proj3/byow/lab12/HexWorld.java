package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 80;
    private static final int HEIGHT = 60;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);


    public static class Position {
        public int x;
        public int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shiftPosition(int dx, int dy) {
            return new Position(x + dx, y + dy);
        }

        public Position getTopNeighbor(int n) {
            return shiftPosition(0, 2 * n);
        }

        public Position getRightBottomNeighbor(int n) {
            return shiftPosition(2 * n - 1, - n);
        }

        public Position getRightTopNeighbor(int n) {
            return shiftPosition(2 * n - 1, n);
        }
    }


    private static void initiateWorld(TETile[][] world) {
        int width = world.length;
        int height = world[0].length;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                world[i][j] = Tileset.NOTHING;
            }
        }
    }

    private static TETile getRandomTile() {
        int tile = RANDOM.nextInt(5);
        switch (tile) {
            case 0: return Tileset.FLOWER;
            case 1: return Tileset.MOUNTAIN;
            case 2: return Tileset.GRASS;
            case 3: return Tileset.SAND;
            case 4: return Tileset.TREE;
            default: return Tileset.NOTHING;
        }
    }

    private static void addHexagon(TETile[][] world, Position p, int n, TETile tile) {
        addHexagonHelper(world, p, n, tile, 1);
    }

    private static void addHexagonHelper(TETile[][] world, Position p, int n, TETile tile, int row) {
        if (row > n) return;
        for (int i = 0; i < n + 2 * (row - 1); i++) {
            int curr_x = p.x + n - row + i;
            world[curr_x][p.y] = tile;
            int another_y = p.y + (n - row) * 2 + 1;
            world[curr_x][another_y] = tile;
        }

        addHexagonHelper(world, p.shiftPosition(0, 1),n , tile, row + 1);
    }

    private static void addHexColumn(TETile[][] world, Position p, int n, int count) {
        if (count < 1) return;
        addHexagon(world, p, n, getRandomTile());
        Position next_p = p.getTopNeighbor(n);
        addHexColumn(world, next_p, n, count - 1);
    }

    private static void drawHexWorld(TETile[][] world, Position p, int n , int size) {
        Position col_p = p;
        addHexColumn(world, col_p, n, size);
        for (int i = 1; i < size; i++) {
            col_p = col_p.getRightBottomNeighbor(n);
            addHexColumn(world, col_p, n, size + i );
        }

        for (int i = size - 2; i >= 0; i--) {
            col_p = col_p.getRightTopNeighbor(n);
            addHexColumn(world, col_p, n, size + i);
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        initiateWorld(world);

        Position p = new Position(20, 20);
        drawHexWorld(world, p, 3, 4);

        ter.renderFrame(world);


    }
}
