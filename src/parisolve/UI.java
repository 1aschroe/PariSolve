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

public class UI {

	private final class SolveSelectionAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			for (SolveListener listener : solveListeners) {
				listener.solve();
			}
		}
	}

	private final class SelectionOpenAdapter extends SelectionAdapter {
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
				box.setMessage("While loading the arena, the following exception occurred:\n"
						+ e.getMessage() + "\n" + e.getStackTrace());
			}
		}
	}

    private static Display display = new Display();
    final static Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.RESIZE | SWT.SCROLL_PAGE);

	static Graph graph = null;
    static Map<ParityVertex, GraphNode> correspondence = new HashMap<ParityVertex, GraphNode>();
    protected List<SolveListener> solveListeners = new ArrayList<SolveListener>();
    protected List<OpenListener> openListeners = new ArrayList<OpenListener>();

    public UI() {
        shell.setText("PariSolve");
		GridLayout layout = new GridLayout();
		shell.setLayout(layout);

		createToolbar();

		createGraph();
	}

	private void createGraph() {
		graph = new Graph(shell, SWT.NONE);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = 500;
		gridData.widthHint = 500;
		graph.setLayoutData(gridData);
    }

    private void createToolbar() {
		final ToolBar bar = new ToolBar(shell, SWT.NONE);
		GridData data = new GridData();
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
        final Image openIcon = new Image(display, StartUp.class.getClassLoader().getResourceAsStream("images/load_cedric_bosdonnat_01.png"));

        final ToolItem openToolItem = new ToolItem(bar, SWT.PUSH);
		openToolItem.setImage(resize(openIcon, 32, 32));
        openToolItem.setText("Open");

		openToolItem.addSelectionListener(new SelectionOpenAdapter());
    }

    private void createSolveButton(final ToolBar bar) {
        // image's source:
        // https://commons.wikimedia.org/wiki/File:Pocket_cube_twisted.jpg
        final Image solveIcon = new Image(display, StartUp.class.getClassLoader().getResourceAsStream("images/Pocket_cube_twisted.jpg"));

        final ToolItem solveToolItem = new ToolItem(bar, SWT.PUSH);
		solveToolItem.setImage(resize(solveIcon, 32, 32));
        solveToolItem.setText("Solve");

		solveToolItem.addSelectionListener(new SolveSelectionAdapter());
                }

	private Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width,
				image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		image.dispose(); // don't forget about me!
		return scaled;
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
    Color BLACK = new Color(display, 0, 0, 0);

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
                GraphConnection connection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, correspondence.get(fromVertex), correspondence.get(toVertex));
                if (fromVertex == toVertex) {
                    connection.setCurveDepth(20);
                } else if (arena.getSuccessors(toVertex).contains(fromVertex)) {
                    connection.setCurveDepth(10);
                }
                connection.setLineColor(BLACK);
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

    public void displayError(String string) {
        MessageBox errorBox = new MessageBox(shell, SWT.ICON_ERROR);
        errorBox.setText("Error!");
        errorBox.setMessage(string);
        errorBox.open();
    }

    public void displayMessage(String string) {
        MessageBox errorBox = new MessageBox(shell, SWT.ICON_INFORMATION);
        errorBox.setText("Info!");
        errorBox.setMessage(string);
        errorBox.open();
    }
}
