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
import parisolve.io.ArenaManager;

public class GraphicalUI {

    private final class SolveSelectionAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(final SelectionEvent arg0) {
            for (final SolveListener listener : solveListeners) {
                listener.solve();
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

    private final static Display DISPLAY = new Display();
    final static Shell SHELL = new Shell(DISPLAY, SWT.SHELL_TRIM | SWT.RESIZE
            | SWT.SCROLL_PAGE);

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

    private void createToolbar() {
        final ToolBar bar = new ToolBar(SHELL, SWT.NONE);
        final GridData data = new GridData();
        data.heightHint = 55;
        data.grabExcessVerticalSpace = false;
        bar.setLayoutData(data);
        bar.setLayout(new GridLayout());

        createOpenButton(bar);

        createSolveButton(bar);
    }

    private void createOpenButton(final ToolBar bar) {
        // image's source:
        // https://openclipart.org/detail/119905/load-cedric-bosdonnat-01-by-anonymous
        createButton(bar, "images/load_cedric_bosdonnat_01.png", "Open",
                new SelectionOpenAdapter());
    }

    private void createSolveButton(final ToolBar bar) {
        // image's source:
        // https://commons.wikimedia.org/wiki/File:Pocket_cube_twisted.jpg
        createButton(bar, "images/Pocket_cube_twisted.jpg", "Solve",
                new SolveSelectionAdapter());
    }

    private static final int DEFAULT_ICON_SIZE = 32;

    private void createButton(final ToolBar bar, final String imagePath,
            final String text, final SelectionListener listener) {
        final Image icon = new Image(DISPLAY, Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(imagePath));

        final ToolItem toolItem = new ToolItem(bar, SWT.PUSH);
        toolItem.setImage(resize(icon, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE));
        toolItem.setText(text);

        toolItem.addSelectionListener(listener);
    }

    private Image resize(final Image image, final int width, final int height) {
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

    public void run() {
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

    public void highlightWinningRegion(
            final Collection<? extends ParityVertex> winningRegion) {
        for (final ParityVertex winningVertex : winningRegion) {
            correspondence.get(winningVertex).setBackgroundColor(GREEN);
        }
    }

    protected void populateGraphWithArena(final Arena arena) {
        for (final GraphNode node : correspondence.values()) {
            node.dispose();
        }

        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        correspondence = new HashMap<>();
        for (final ParityVertex vertex : vertices) {
            correspondence.put(vertex, new GraphNode(graph,
                    (vertex.getPlayer() == 0 ? ZestStyles.NODES_CIRCULAR_SHAPE
                            : 0), vertex.getPriority() + ""));
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

    public void displayError(final String message) {
        displayMessage(message, "Error!", SWT.ICON_ERROR);
    }

    public void displayInfo(final String message) {
        displayMessage(message, "Info", SWT.ICON_INFORMATION);
    }
    
    public void displayMessage(final String message, final String title, final int style) {
        final MessageBox errorBox = new MessageBox(SHELL, style);
        errorBox.setText(title);
        errorBox.setMessage(message);
        errorBox.open();
    }
}
