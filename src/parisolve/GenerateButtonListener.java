package parisolve;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * class representing the dialog querying for parameter when an arena is to be
 * generated.
 * 
 * @author Arne Schröder
 */
final class GenerateButtonListener extends SelectionAdapter {

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

        final TabFolder tabFolder = new TabFolder(generateShell, SWT.BORDER);

        final TabItem randomTabItem = new TabItem(tabFolder, SWT.NONE);
        randomTabItem.setText("Random");
        final Composite randomTab = new Composite(tabFolder, SWT.NONE);
        populateRandomTab(randomTab, generateShell);
        randomTabItem.setControl(randomTab);

        final TabItem hlbTabItem = new TabItem(tabFolder, SWT.NONE);
        hlbTabItem.setText("H_lb");
        final Composite hlbTab = new Composite(tabFolder, SWT.NONE);
        populateHlbTab(hlbTab, generateShell);
        hlbTabItem.setControl(hlbTab);

        tabFolder.pack();

        generateShell.pack();
        generateShell.open();
    }

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

    private void populateRandomTab(final Composite randomTab, final Shell shell) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        randomTab.setLayout(layout);

        final Label nodesLabel = new Label(randomTab, SWT.NONE);
        nodesLabel.setText("# of vertices:");
        final Spinner noVertices = new Spinner(randomTab, SWT.NONE);
        noVertices.setSelection(this.lastSize);

        final Label averageLabel = new Label(randomTab, SWT.NONE);
        averageLabel.setText("average degree:");
        final Spinner degree = new Spinner(randomTab, SWT.NONE);
        degree.setDigits(1);
        degree.setSelection(this.lastAverage);

        final Label priorityLabel = new Label(randomTab, SWT.NONE);
        priorityLabel.setText("max. priority:");
        final Spinner maxPrio = new Spinner(randomTab, SWT.NONE);
        maxPrio.setSelection(this.lastMaxPrio);

        final Button okButton = new Button(randomTab, SWT.PUSH);
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                lastSize = noVertices.getSelection();
                lastAverage = degree.getSelection();
                lastMaxPrio = maxPrio.getSelection();
                ui.generateRandomArena(lastSize, lastAverage / 10.0,
                        lastMaxPrio);
                shell.dispose();
            }
        });
        okButton.setText("Generate");
    }

    int lastLevels = 4;

    int lastBlocks = 3;

    private void populateHlbTab(final Composite hlbTab, final Shell shell) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        hlbTab.setLayout(layout);

        final Label levelsLabel = new Label(hlbTab, SWT.NONE);
        levelsLabel.setText("# of levels:");
        final Spinner noLabels = new Spinner(hlbTab, SWT.NONE);
        noLabels.setSelection(this.lastLevels);

        final Label blocksLabel = new Label(hlbTab, SWT.NONE);
        blocksLabel.setText("# of blocks:");
        final Spinner noBlocks = new Spinner(hlbTab, SWT.NONE);
        noBlocks.setSelection(this.lastBlocks);

        final Button okButton = new Button(hlbTab, SWT.PUSH);
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                lastLevels = noLabels.getSelection();
                lastBlocks = noBlocks.getSelection();
                ui.generateHlbArena(lastLevels, lastBlocks);
                shell.dispose();
            }
        });
        okButton.setText("Generate");
    }
}