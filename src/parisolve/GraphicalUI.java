package parisolve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

public class GraphicalUI {

    private final class SolveSelectionAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(final SelectionEvent arg0) {
            if (algorithmCombo.getSelectedAlgorithm() == null) {
                displayError("No algorithm selected.");
                return;
            }
            for (final SolveListener listener : solveListeners) {
                listener.solve(algorithmCombo.getSelectedAlgorithm());
            }
        }
    }

    private final class SelectionOpenAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(final SelectionEvent arg0) {
            final FileDialog fileDialog = new FileDialog(SHELL, SWT.OPEN);
            fileDialog.setText("Load Arena");
            try {
                final String filename = fileDialog.open();
                if (filename != null) {
                    final Arena arena = ArenaManager.loadArena(filename);
                    for (final OpenListener listener : openListeners) {
                        listener.openedArena(arena);
                    }
                }
            } catch (IOException e) {
                displayError("While loading the arena, the following exception occurred:\n"
                        + e.getMessage()
                        + "\n"
                        + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    final static List<Solver> ALGORITHMS = KnownArenasTest.getAlgorithms();
    final static String[] ALGORITHM_NAMES = new String[ALGORITHMS.size()];
    static {
        for (int i = 0; i < ALGORITHMS.size(); i++) {
            // TODO: find nicer names to display for algorithms
            ALGORITHM_NAMES[i] = ALGORITHMS.get(i).getClass().getSimpleName();
        }
    }

    private final class AlgorithmCombo {
        final Combo algorithmCombo;

        public AlgorithmCombo(Composite parent) {
            algorithmCombo = new Combo(parent, SWT.READ_ONLY);
            algorithmCombo.setItems(ALGORITHM_NAMES);
        }

        public Solver getSelectedAlgorithm() {
            final int algorithmIndex = algorithmCombo.getSelectionIndex();
            if (algorithmIndex == -1) {
                return null;
            }
            return ALGORITHMS.get(algorithmIndex);
        }
    }

    private final static Display DISPLAY = new Display();
    final static Shell SHELL = new Shell(DISPLAY, SWT.SHELL_TRIM | SWT.RESIZE
            | SWT.SCROLL_PAGE);

    static AlgorithmCombo algorithmCombo = null;

    static Graph graph = null;
    static Map<ParityVertex, GraphNode> correspondence = new HashMap<ParityVertex, GraphNode>();
    protected List<SolveListener> solveListeners = new ArrayList<SolveListener>();
    protected List<OpenListener> openListeners = new ArrayList<OpenListener>();

    public GraphicalUI() {
        SHELL.setText("PariSolve");
        SHELL.setLayout(new GridLayout());

        createToolbar();

        createGraph();
    }

    private static final int DEFAULT_GRAPH_SIZE = 500;

    private void createGraph() {
        graph = new Graph(SHELL, SWT.NONE);

        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.heightHint = DEFAULT_GRAPH_SIZE;
        gridData.widthHint = DEFAULT_GRAPH_SIZE;
        graph.setLayoutData(gridData);
    }

    /**
     * adds a toolbar to the shell.
     */
    private void createToolbar() {
        // the visible toolbar is actually a toolbar next to a combobox.
        // That is why we need this extra composite, layout and numColums = 2.
        final Composite parent = new Composite(SHELL, SWT.FILL);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        parent.setLayout(layout);

        final ToolBar bar = new ToolBar(parent, SWT.NONE);
        final GridData data = new GridData();
        data.heightHint = 55;
        data.grabExcessVerticalSpace = false;
        bar.setLayoutData(data);
        bar.setLayout(new GridLayout());

        createOpenButton(bar);

        createSolveButton(bar);

        createAboutButton(bar);

        algorithmCombo = new AlgorithmCombo(parent);
    }

    /**
     * adds an open-button to the toolbar given
     * 
     * @param bar
     *            the toolbar to add the button to
     */
    private void createOpenButton(final ToolBar bar) {
        // image's source:
        // https://openclipart.org/detail/119905/load-cedric-bosdonnat-01-by-anonymous
        createButton(bar, "images/load_cedric_bosdonnat_01.png", "Open",
                new SelectionOpenAdapter());
    }

    /**
     * adds a solve-button to the toolbar given
     * 
     * @param bar
     *            the toolbar to add the button to
     */
    private void createSolveButton(final ToolBar bar) {
        // image's source:
        // https://commons.wikimedia.org/wiki/File:Pocket_cube_twisted.jpg
        createButton(bar, "images/Pocket_cube_twisted.jpg", "Solve",
                new SolveSelectionAdapter());
    }

    /**
     * adds an about-button to the toolbar given
     * 
     * @param bar
     *            the toolbar to add the button to
     */
    private void createAboutButton(final ToolBar bar) {
        // image's source:
        // http://commons.wikimedia.org/wiki/File:Information.png
        createButton(bar, "images/Information.png", "About",
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(final SelectionEvent arg0) {
                        displayInfo("The parity game played is a max priority parity game.\n"
                                + "Player 0 has round vertices, player 1 has square vertices.\n"
                                + "Player 0 wins even priorities and player 1 wins odd priorities.\n"
                                + "Once solved, the winning region of player 0 is displayed in green.");
                    }
                });
    }

    /**
     * the default icon size on buttons to have them all the same size.
     */
    private static final int DEFAULT_ICON_SIZE = 32;

    /**
     * adds a button to the toolbar given, with the image, text and listener as
     * specified.
     * 
     * @param bar
     *            the toolbar to add the button to
     * @param imagePath
     *            the image's path, relative to the bin-directory
     * @param text
     *            the text to display on the button
     * @param listener
     *            the listener to call once the button is clicked
     */
    private void createButton(final ToolBar bar, final String imagePath,
            final String text, final SelectionListener listener) {
        final Image icon = new Image(DISPLAY, Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(imagePath));

        final ToolItem toolItem = new ToolItem(bar, SWT.PUSH);
        toolItem.setImage(resize(icon, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE));
        toolItem.setText(text);

        toolItem.addSelectionListener(listener);
    }

    /**
     * resizes an image to the height and width specified. Note that the
     * original image is disposed and the caller is responsible to dispose of
     * the returned image.
     * 
     * @param image
     *            the image to resize
     * @param width
     *            the width the image is to be resized to
     * @param height
     *            the height the image is to be resized to
     * @return a new image with height and width as specified
     */
    private static Image resize(final Image image, final int width,
            final int height) {
        final Image scaled = new Image(Display.getDefault(), width, height);
        final GC gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        gc.drawImage(image, 0, 0, image.getBounds().width,
                image.getBounds().height, 0, 0, width, height);
        gc.dispose();
        image.dispose();
        return scaled;
    }

    /**
     * display GUI. Blocks the current thread until the GUI is closed.
     */
    public final void run() {
        SHELL.pack();
        SHELL.open();
        while (!SHELL.isDisposed()) {
            if (!DISPLAY.readAndDispatch()) {
                DISPLAY.sleep();
            }
        }
        DISPLAY.dispose();
    }

    private static final Color GREEN = new Color(DISPLAY, 0, 255, 0);
    private static final Color BLACK = new Color(DISPLAY, 0, 0, 0);

    /**
     * highlights a region of an arena by painting the vertices green. Used to
     * display the winning region of an arena.
     * 
     * @param region
     *            region to highlight
     */
    public final void highlightRegion(
            final Collection<? extends ParityVertex> region) {
        for (final ParityVertex vertex : region) {
            correspondence.get(vertex).setBackgroundColor(GREEN);
        }
    }

    /**
     * clears the current graph and inserts nodes and connections, which depict
     * the vertices and edges from the arena given.
     * 
     * @param arena
     *            the arena to display
     */
    protected final void populateGraphWithArena(final Arena arena) {
        for (final GraphNode node : correspondence.values()) {
            node.dispose();
        }

        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        correspondence = new HashMap<>();
        for (final ParityVertex vertex : vertices) {
            correspondence
                    .put(vertex,
                            new GraphNode(
                                    graph,
                                    (vertex.getPlayer() == 0 ? ZestStyles.NODES_CIRCULAR_SHAPE
                                            : ZestStyles.NONE), Integer
                                            .toString(vertex.getPriority())));
        }
        for (final ParityVertex fromVertex : vertices) {
            for (final ParityVertex toVertex : arena.getSuccessors(fromVertex)) {
                final GraphConnection connection = new GraphConnection(graph,
                        ZestStyles.CONNECTIONS_DIRECTED,
                        correspondence.get(fromVertex),
                        correspondence.get(toVertex));
                if (fromVertex.equals(toVertex)) {
                    connection.setCurveDepth(20);
                } else if (arena.getSuccessors(toVertex).contains(fromVertex)) {
                    connection.setCurveDepth(10);
                }
                connection.setLineColor(BLACK);
            }
        }

        graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(
                LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
    }

    public void addSolveListener(final SolveListener solveListener) {
        if (!solveListeners.contains(solveListener)) {
            solveListeners.add(solveListener);
        }
    }

    public void addOpenListener(final OpenListener openListener) {
        if (!openListeners.contains(openListener)) {
            openListeners.add(openListener);
        }
    }

    /**
     * display an error-box with the message specified.
     * 
     * @param message
     *            the message to be inside the error-box
     */
    public final void displayError(final String message) {
        displayMessage(message, "Error!", SWT.ICON_ERROR);
    }

    /**
     * display an info-box with the message specified.
     * 
     * @param message
     *            the message to be inside the info-box
     */
    public final void displayInfo(final String message) {
        displayMessage(message, "Info", SWT.ICON_INFORMATION);
    }

    /**
     * display a message box with the message, style and title as specified.
     * 
     * @param message
     *            the message to be displayed inside the message box
     * @param title
     *            the title for the message box
     * @param iconConstant
     *            the constant for the message box' icon (SWT.ICON_**)
     */
    public final void displayMessage(final String message, final String title,
            final int iconConstant) {
        final MessageBox errorBox = new MessageBox(SHELL, iconConstant);
        errorBox.setText(title);
        errorBox.setMessage(message);
        errorBox.open();
    }
}
