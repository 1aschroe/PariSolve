package parisolve;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

/**
 * class representing the dialog querying for parameter when an arena is to be
 * generated.
 * 
 * @author Arne Schröder
 */
final class GenerateButtonListener extends SelectionAdapter {
    /**
     * last value for the number of vertices in a generated arena. At start this
     * is the default value.
     */
    int lastSize = 16;
    /**
     * last value for the average degree in a generated arena. At start this is
     * the default value.
     */
    int lastAverage = 30;
    /**
     * last value for the maximal priority in a generated arena. At start this
     * is the default value.
     */
    int lastMaxPrio = 7;

    private final AbstractUI ui;

    /**
     * @param ui
     *            user interface providing a method for generating an arena
     */
    GenerateButtonListener(final AbstractUI ui) {
        this.ui = ui;
    }

    @Override
    public void widgetSelected(final SelectionEvent arg0) {
        final Shell generateShell = new Shell(GraphicalUI.DISPLAY);
        generateShell.setImage(GraphicalUI
                .getIcon("images/32px-2-Dice-Icon.svg.png"));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        generateShell.setLayout(layout);

        Label nodesLabel = new Label(generateShell, SWT.NONE);
        nodesLabel.setText("# of vertices:");
        final Spinner noVertices = new Spinner(generateShell, SWT.NONE);
        noVertices.setSelection(this.lastSize);

        Label averageLabel = new Label(generateShell, SWT.NONE);
        averageLabel.setText("average degree:");
        final Spinner degree = new Spinner(generateShell, SWT.NONE);
        degree.setDigits(1);
        degree.setSelection(this.lastAverage);

        Label priorityLabel = new Label(generateShell, SWT.NONE);
        priorityLabel.setText("max. priority:");
        final Spinner maxPrio = new Spinner(generateShell, SWT.NONE);
        maxPrio.setSelection(this.lastMaxPrio);

        Button okButton = new Button(generateShell, SWT.PUSH);
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                lastSize = noVertices.getSelection();
                lastAverage = degree.getSelection();
                lastMaxPrio = maxPrio.getSelection();
                ui.generateRandomArena(lastSize, lastAverage / 10.0, lastMaxPrio);
                generateShell.dispose();
            }
        });
        okButton.setText("Generate");

        generateShell.pack();
        generateShell.open();
    }
}