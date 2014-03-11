package parisolve;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import parisolve.backend.PrimitiveAlgorithm;
import parisolve.backend.Solver;
import parisolve.io.ArenaManager;

public class StartUp {

    private static Display display = new Display();
    final static Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.RESIZE | SWT.SCROLL_PAGE);

    final static Graph graph = new Graph(shell, SWT.NONE);

    static Arena currentArena;
    static Map<ParityVertex, GraphNode> correspondence = new HashMap<>();

    public static void main(String[] args) {
        shell.setText("PariSolve");

        graph.setLocation(0, 55);
        graph.setSize(500, 500);

        // set the size and location of the user interface widgets
        final ToolBar bar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT | SWT.WRAP);
        bar.setSize(500, 55);
        bar.setLocation(0, 0);

        final Image openIcon = new Image(display, "resources/load_cedric_bosdonnat_01.png");
        final Image solveIcon = new Image(display, "resources/Pocket_cube_twisted.jpg");

        // Configure the ToolBar
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
                        currentArena = ArenaManager.loadArena(filename);
                        populateGraphWithArena();
                    }
                } catch (IOException e) {
                    MessageBox box = new MessageBox(shell, SWT.ERROR);
                    box.setText("Exception occurred");
                    box.setMessage("While loading the arena, the following exception occurred:\n" + e.getMessage() + "\n" + e.getStackTrace());
                }
            }
        });

        final ToolItem solveToolItem = new ToolItem(bar, SWT.PUSH);
        solveToolItem.setImage(solveIcon);
        solveToolItem.setText("Solve");

        solveToolItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                Solver solver = new PrimitiveAlgorithm();
                Collection<? extends ParityVertex> winningRegionForPlayer = solver.getWinningRegionForPlayer(currentArena, 0);
                for (ParityVertex winningVertex : winningRegionForPlayer) {
                    correspondence.get(winningVertex).highlight();
                }
            }
        });

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    protected static void populateGraphWithArena() {
        for (GraphNode node : correspondence.values()) {
            node.dispose();
        }
        
        Collection<? extends ParityVertex> vertices = currentArena.getVertices();
        correspondence = new HashMap<>();
        for (ParityVertex vertex : vertices) {
            correspondence.put(vertex, new GraphNode(graph, ZestStyles.NODES_EMPTY | (vertex.getPlayer() == 0 ? ZestStyles.NODES_CIRCULAR_SHAPE : 0),
                    vertex.getPriority() + ""));
        }
        for (ParityVertex fromVertex : vertices) {
            for (ParityVertex toVertex : currentArena.getSuccessors(fromVertex)) {
                new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, correspondence.get(fromVertex), correspondence.get(toVertex));
            }
        }

        graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
    }

}
