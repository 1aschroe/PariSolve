package parisolve;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import parisolve.backend.ParityEdge;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.AlgorithmManager;
import parisolve.backend.algorithms.Solver;

public class GraphicalUI extends AbstractUI {

    private final class SolveSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(final SelectionEvent arg0) {
            if (algorithmCombo.getSelectedAlgorithm() == null) {
                displayError("No algorithm selected.");
                return;
            }
            fireSolve(algorithmCombo.getSelectedAlgorithm());
        }
    }

    private final class SelectionOpenListener extends SelectionAdapter {
        @Override
        public void widgetSelected(final SelectionEvent arg0) {
            final FileDialog fileDialog = new FileDialog(SHELL, SWT.OPEN);
            fileDialog.setText("Load Arena");
            final String filename = fileDialog.open();
            if (filename != null) {
                loadArenaFromFile(filename);
            }
        }
    }

    final static List<Solver> ALGORITHMS = AlgorithmManager.getAlgorithms();
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

    final static Display DISPLAY = new Display();
    final static Shell SHELL = new Shell(DISPLAY, SWT.SHELL_TRIM | SWT.RESIZE
            | SWT.SCROLL_PAGE);

    static AlgorithmCombo algorithmCombo = null;

    /**
     * the graph-widget to display the arena.
     */
    private static Graph graph = null;
    /**
     * represents the correspondence between the vertices in the model and the
     * graph-nodes being displayed
     */
    private static Map<ParityVertex, GraphNode> vertexCorrespondence = new ConcurrentHashMap<ParityVertex, GraphNode>();
    private static Map<ParityEdge, GraphConnection> edgeCorrespondence = new ConcurrentHashMap<>();

    public GraphicalUI() {
        SHELL.setText("PariSolve");
        SHELL.setLayout(new GridLayout());

        createToolbar();

        createGraph();
    }

    /**
     * initial height and width of the graph area.
     */
    private static final int DEFAULT_GRAPH_SIZE = 500;

    /**
     * adds the graph to the shell.
     */
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

        createGenerateButton(bar);

        createSaveButton(bar);

        createSolveButton(bar);

        createAboutButton(bar);

        algorithmCombo = new AlgorithmCombo(parent);
    }

    /**
     * adds a button to the toolbar, to open an arena from file.
     * 
     * @param bar
     *            the toolbar to add the button to
     */
    private void createOpenButton(final ToolBar bar) {
        // image's source:
        // https://openclipart.org/detail/119905/load-cedric-bosdonnat-01-by-anonymous
        createButton(bar, "images/load_cedric_bosdonnat_01.png", "Open",
                new SelectionOpenListener());
    }

    /**
     * adds a button to the toolbar, to generate and load an arena.
     * 
     * @param bar
     *            the toolbar to add the button to
     */
    private void createGenerateButton(final ToolBar bar) {
        // image's source:
        // http://en.wikipedia.org/wiki/File:2-Dice-Icon.svg
        createButton(bar, "images/32px-2-Dice-Icon.svg.png", "Generate",
                new GenerateButtonListener(this));
    }

    private void createSaveButton(final ToolBar bar) {
        // image's source:
        // https://commons.wikimedia.org/wiki/File:1328102005_Save.png
        // from: https://www.iconfinder.com/icons/49256/disk_save_icon
        // Author: VistaICO.com
        createButton(bar, "images/1328102005_Save.png", "Save",
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent arg0) {
                        final FileDialog fileDialog = new FileDialog(SHELL,
                                SWT.SAVE);
                        fileDialog.setText("Save Arena");
                        final String filename = fileDialog.open();
                        if (filename != null) {
                            fireSave(filename);
                        }
                    }
                });
    }

    /**
     * adds a button to the toolbar, to solve the current arena.
     * 
     * @param bar
     *            the toolbar to add the button to
     */
    private void createSolveButton(final ToolBar bar) {
        // image's source:
        // https://commons.wikimedia.org/wiki/File:Pocket_cube_twisted.jpg
        createButton(bar, "images/Pocket_cube_twisted.jpg", "Solve",
                new SolveSelectionListener());
    }

    /**
     * adds a button to the toolbar, to display information about the parity
     * game being displayed.
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
                                + "Once solved, the winning region of player 0 is displayed in green.\n"
                                + "The winning region of player 1 is displayed in blue.");
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
        final Image icon = getIcon(imagePath);

        final ToolItem toolItem = new ToolItem(bar, SWT.PUSH);
        toolItem.setImage(resize(icon, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE));
        toolItem.setText(text);

        toolItem.addSelectionListener(listener);
    }

    /**
     * creates an image from the path given. This path has to be relative to the
     * resources folder.
     * 
     * @param imagePath
     *            image's path, relative to resources folder
     * @return image
     */
    protected static Image getIcon(final String imagePath) {
        return new Image(DISPLAY, Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(imagePath));
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
    @Override
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

    private static final Color BLUE = new Color(DISPLAY, 172, 172, 255);
    private static final Color GREEN = new Color(DISPLAY, 0, 255, 0);
    private static final Color BLACK = new Color(DISPLAY, 0, 0, 0);

    /**
     * displays the winning regions of player A and B and their respective
     * strategies.
     * 
     * @param region
     *            winning region of player A
     * @param strategy
     *            strategies of both players which lead to this partition
     */
    @Override
    public final void highlightSolution(
            final Collection<? extends ParityVertex> region,
            final Map<ParityVertex, ParityVertex> strategy) {
        for (final ParityVertex vertex : vertexCorrespondence.keySet()) {
            if (region.contains(vertex)) {
                vertexCorrespondence.get(vertex).setBackgroundColor(GREEN);
            } else {
                vertexCorrespondence.get(vertex).setBackgroundColor(BLUE);
            }
        }

        for (final ParityEdge edge : edgeCorrespondence.keySet()) {
            GraphConnection connection = edgeCorrespondence.get(edge);
            final ParityVertex from = edge.getFrom();
            // TODO ugly
            if (strategy.containsKey(from)
                    && strategy.get(from).equals(edge.getTo())) {
                if (from.getPlayer() == Player.A && region.contains(from)) {
                    connection.setLineColor(GREEN);
                    connection.setLineWidth(2);
                } else if (from.getPlayer() == Player.B
                        && !region.contains(from)) {
                    connection.setLineColor(BLUE);
                    connection.setLineWidth(2);
                } else {
                    connection.setLineColor(BLACK);
                    connection.setLineWidth(1);
                }
            } else {
                connection.setLineColor(BLACK);
                connection.setLineWidth(1);
            }
        }
    }

    /**
     * clears the current graph and inserts nodes and connections, which depict
     * the vertices and edges from the arena given.
     * 
     * @param arena
     *            the arena to display
     */
    public final void populateGraphWithArena(final Arena arena) {
        for (final GraphNode node : vertexCorrespondence.values()) {
            node.dispose();
        }
        for (final GraphConnection connection : edgeCorrespondence.values()) {
            connection.dispose();
        }

        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        vertexCorrespondence.clear();
        edgeCorrespondence.clear();
        for (final ParityVertex vertex : vertices) {
            vertexCorrespondence.put(vertex,
                    new GraphNode(graph, vertex.getPlayer().getZestStyleFlag(),
                            Integer.toString(vertex.getPriority())));
        }
        for (final ParityVertex fromVertex : vertices) {
            for (final ParityVertex toVertex : fromVertex.getSuccessors()) {
                final GraphConnection connection = new GraphConnection(graph,
                        ZestStyles.CONNECTIONS_DIRECTED,
                        vertexCorrespondence.get(fromVertex),
                        vertexCorrespondence.get(toVertex));
                if (fromVertex.equals(toVertex)) {
                    connection.setCurveDepth(20);
                } else if (toVertex.getSuccessors().contains(fromVertex)) {
                    connection.setCurveDepth(10);
                }
                connection.setLineColor(BLACK);
                edgeCorrespondence.put(new ParityEdge(fromVertex, toVertex),
                        connection);
            }
        }

        graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(
                LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
    }

    /**
     * display an error-box with the message specified.
     * 
     * @param message
     *            the message to be inside the error-box
     */
    @Override
    public final void displayError(final String message) {
        displayMessage(message, "Error!", SWT.ICON_ERROR);
    }

    /**
     * display an info-box with the message specified.
     * 
     * @param message
     *            the message to be inside the info-box
     */
    @Override
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

    @Override
    public final void handleArguments(final String[] arenas) {
        if (arenas.length > 0) {
            loadArenaFromFile(arenas[0]);
        }
    }
}
