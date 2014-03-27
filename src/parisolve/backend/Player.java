package parisolve.backend;

import org.eclipse.zest.core.widgets.ZestStyles;

public enum Player {
    /**
     * Player A aka 0, wins evens.
     */
    A("oval", 0) {
        @Override
        public Player getOponent() {
            return B;
        }

        @Override
        public int getZestStyleFlag() {
            return ZestStyles.NODES_CIRCULAR_SHAPE;
        }
    },
    /**
     * Player B aka 1 or P, wins odds.
     */
    B("box", 1) {
        @Override
        public Player getOponent() {
            return A;
        }

        @Override
        public int getZestStyleFlag() {
            return ZestStyles.NONE;
        }
    };
    
    private String shapeString;
    private int number;

    Player(final String shapeString, final int no) {
        this.shapeString = shapeString;
        this.number = no;
    }
    
    public String getShapeString() {
        return shapeString;
    }
    
    public int getNumber() {
        return number;
    }

    public abstract Player getOponent();

    public abstract int getZestStyleFlag();

    static Player[] playerForNo = new Player[] { A, B };

    public static Player getPlayerForInt(final int number) {
        return playerForNo[number % 2];
    }

    public static Player getPlayerForShapeString(final String shape) {
        for (Player player : Player.values()) {
            if (player.getShapeString().equals(shape)) {
                return player;
            }
        }
        // TODO: throw an exception?
        return null;
    }
}
