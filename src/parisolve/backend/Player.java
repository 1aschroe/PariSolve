package parisolve.backend;

import org.eclipse.zest.core.widgets.ZestStyles;

/**
 * enumeration of the two players and their properties respectively.
 * 
 * @author Arne Schröder
 */
public enum Player {
    /* ...::: ELEMENTS :::... */
    /**
     * Player A aka 0, wins evens and is represented with round nodes.
     */
    A("oval", ZestStyles.NODES_CIRCULAR_SHAPE, 0) {
        @Override
        public Player getOponent() {
            return B;
        }
    },
    /**
     * Player B aka 1 or P, wins odds and is represented with rectangular nodes.
     */
    B("box", ZestStyles.NONE, 1) {
        @Override
        public Player getOponent() {
            return A;
        }
    };

    /* ...::: MEMBERS :::... */

    Player(final String shapeString, final int zestStyle, final int number) {
        this.shapeString = shapeString;
        zestStyleFlag = zestStyle;
        this.number = number;
    }

    public abstract Player getOponent();

    /**
     * textual description of the shape of nodes, owned by this player. This
     * description is to be understood by GraphViz.
     */
    private final String shapeString;

    /**
     * accessor for <code>shapeString</code>
     * 
     * @see Player.shapeString
     * @return shapeString
     */
    public String getShapeString() {
        return shapeString;
    }

    /**
     * return the player which has <code>shape</code> as its shape-string.
     * 
     * @see Player.getPlayerForPriority
     * @see Player.getShapeString
     * @see Player.shapeString
     * 
     * @param shape
     *            the textual description of the player's shape
     * @return a player for which <code>getShapeString().equals(shape)</code>
     */
    public static Player getPlayerForShapeString(final String shape) {
        for (final Player player : Player.values()) {
            if (player.getShapeString().equals(shape)) {
                return player;
            }
        }
        // TODO: throw an exception?
        return null;
    }

    /**
     * constant from <code>ZestStyles</code> indicating the shape of a node
     * owned by this player.
     * 
     * @see org.eclipse.zest.core.widgets.ZestStyles.NODES_CIRCULAR_SHAPE
     */
    private final int zestStyleFlag;

    /**
     * accessor for <code>zestStyleFlag</code>
     * 
     * @see Player.zestStyleFlag
     * @return zestStyleFlag
     */
    public int getZestStyleFlag() {
        return zestStyleFlag;
    }

    /**
     * a numerical identifier for the player. For convenience it should hold
     * that a player <code>sigma</code> wins a priority <code>prio</code> for
     * which <code>p == sigma.getNumber()</code>.
     */
    private final int number;

    /**
     * accessor for <code>number</code>
     * 
     * @see Player.number
     * @return number
     */
    public int getNumber() {
        return number;
    }

    /**
     * stores the players to access them in a numerical way: 0 -> A and 1 -> B.
     */
    static final Player[] PLAYER_FOR_RIORITY = new Player[] { A, B };
    static {
        // TODO: can one do this more explicitly so no wrong assignment was
        // possible?
        for (final Player player : values()) {
            assert player == PLAYER_FOR_RIORITY[player.getNumber()];
        }
    }

    /**
     * returns the player which wins, if the given priority was the maximal
     * priority encountered.
     * 
     * @param priority
     *            the priority for which the player has to be determined.
     * @return the player who wins this priority
     */
    public static Player getPlayerForPriority(final int priority) {
        return PLAYER_FOR_RIORITY[priority % 2];
    }
}
