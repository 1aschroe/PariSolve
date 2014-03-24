package parisolve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
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

public class UI {

    private static Display display = new Display();
    final static Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.RESIZE | SWT.SCROLL_PAGE);

    final static Graph graph = new Graph(shell, SWT.NONE);
    static Map<ParityVertex, GraphNode> correspondence = new HashMap<>();
    protected List<SolveListener> solveListeners = new ArrayList<>();
    protected List<OpenListener> openListeners = new ArrayList<>();

    public UI() {
        shell.setText("PariSolve");

        graph.setLocation(0, 55);
        graph.setSize(500, 500);

        createToolbar();
    }

    private void createToolbar() {
        // set the size and location of the user interface widgets
        final ToolBar bar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT | SWT.WRAP);
        bar.setSize(500, 55);
        bar.setLocation(0, 0);

        createOpenButton(bar);

        createSolveButton(bar);
    }

    private void createOpenButton(final ToolBar bar) {
        // image's source:
        // https://openclipart.org/detail/119905/load-cedric-bosdonnat-01-by-anonymous
        final Image openIcon = new Image(display, StartUp.class.getClassLoader().getResourceAsStream("images/load_cedric_bosdonnat_01.png"));

        final ToolItem openToolItem = new ToolItem(bar, SWT.PUSH);
        openToolItem.setImage(openIcon);
        openToolItem.setText("Open");

        openToolItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
                fileDialog.setText("Load Arena");
                try {
                    String filename = fileDialog.open();
                    if (filename != null) {
                        Arena arena = ArenaManager.loadArena(filename);
                        for (OpenListener listener : openListeners) {
                            listener.openedArena(arena);
                        }
                    }
                } catch (IOException e) {
                    MessageBox box = new MessageBox(shell, SWT.ERROR);
                    box.setText("Exception occurred");
                    box.setMessage("While loading the arena, the following exception occurred:\n" + e.getMessage() + "\n" + e.getStackTrace());
                }
            }
        });
    }

    private void createSolveButton(final ToolBar bar) {
        // image's source:
        // https://commons.wikimedia.org/wiki/File:Pocket_cube_twisted.jpg
        final Image solveIcon = new Image(display, StartUp.class.getClassLoader().getResourceAsStream("images/Pocket_cube_twisted.jpg"));

        final ToolItem solveToolItem = new ToolItem(bar, SWT.PUSH);
        solveToolItem.setImage(solveIcon);
        solveToolItem.setText("Solve");

        solveToolItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                for (SolveListener listener : solveListeners) {
                    listener.solve();
                }
            }
        });
    }

    public void run() {
        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    Color GREEN = new Color(display, 0, 255, 0);

    public void highlightWinningRegion(Collection<? extends ParityVertex> winningRegion) {
        for (ParityVertex winningVertex : winningRegion) {
            correspondence.get(winningVertex).setBackgroundColor(GREEN);
        }
    }

    protected void populateGraphWithArena(Arena arena) {
        for (GraphNode node : correspondence.values()) {
            node.dispose();
        }

        Collection<? extends ParityVertex> vertices = arena.getVertices();
        correspondence = new HashMap<>();
        for (ParityVertex vertex : vertices) {
            correspondence.put(vertex, new GraphNode(graph, (vertex.getPlayer() == 0 ? ZestStyles.NODES_CIRCULAR_SHAPE : 0),
                    vertex.getPriority() + ""));
        }
        for (ParityVertex fromVertex : vertices) {
            for (ParityVertex toVertex : arena.getSuccessors(fromVertex)) {
                new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, correspondence.get(fromVertex), correspondence.get(toVertex));
            }
        }

        graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
    }

    public void addSolveListener(SolveListener solveListener) {
        if (!solveListeners.contains(solveListener)) {
            solveListeners.add(solveListener);
        }
    }

    public void addOpenListener(OpenListener openListener) {
        if (!openListeners.contains(openListener)) {
            openListeners.add(openListener);
        }
    }
}
