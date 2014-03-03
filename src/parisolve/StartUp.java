package parisolve;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

public class StartUp {

    private static Display display = new Display();
    
	public static void main(String[] args) {
		Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.RESIZE | SWT.SCROLL_PAGE);
        shell.setText("Testtitel");
        
        Composite parent = shell;
        
     // Graph will hold all other objects
        Graph graph = new Graph(parent, SWT.NONE);
        // now a few nodes
        GraphNode node1 = new GraphNode(graph, SWT.NONE, "Jim");
        GraphNode node2 = new GraphNode(graph, SWT.NONE, "Jack");
        GraphNode node3 = new GraphNode(graph, SWT.NONE, "Joe");
        GraphNode node4 = new GraphNode(graph, SWT.NONE, "Bill");
        // Lets have a directed connection
        new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, node1,
            node2);
        // Lets have a dotted graph connection
        new GraphConnection(graph, ZestStyles.CONNECTIONS_DOT, node2, node3);
        // Standard connection
        new GraphConnection(graph, SWT.NONE, node3, node1);
        // Change line color and line width
        GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE,
            node1, node4);
        graphConnection.changeLineColor(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
        // Also set a text
        graphConnection.setText("This is a text");
        graphConnection.setHighlightColor(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
        graphConnection.setLineWidth(3);

        graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        // Selection listener on graphConnect or GraphNode is not supported
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=236528
        graph.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            System.out.println(e);
          }

        });
        
        graph.pack();
        graph.setSize(500, 500);
        
        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
	}

}
