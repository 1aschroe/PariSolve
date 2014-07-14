package parisolve;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import parisolve.io.LinearArenaGenerator.GeneratorType;

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

        final Composite randomTab = createTab(tabFolder, "Random");
        populateRandomTab(randomTab, generateShell);

        final Composite hlbTab = createTab(tabFolder, "H_lb");
        populateHlbTab(hlbTab, generateShell);

        final Composite linearTab = createTab(tabFolder, "Linear");
        populateLinearTab(linearTab, generateShell);

        tabFolder.pack();

        generateShell.pack();
        generateShell.open();
    }

    private Composite createTab(final TabFolder tabFolder, final String text) {
        final TabItem randomTabItem = new TabItem(tabFolder, SWT.NONE);
        randomTabItem.setText(text);
        final Composite randomTab = new Composite(tabFolder, SWT.NONE);
        randomTabItem.setControl(randomTab);
        return randomTab;
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

        final Spinner noVertices = createSpinner(randomTab, "# of vertices:",
                lastSize);
        final Spinner degree = createSpinner(randomTab, "average degree:",
                lastAverage, 1);
        final Spinner maxPrio = createSpinner(randomTab, "max. priority:",
                lastMaxPrio);

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

    private Spinner createSpinner(final Composite tab, final String text,
            final int value) {
        return createSpinner(tab, text, value, 0);
    }

    private Spinner createSpinner(final Composite tab, final String text,
            final int value, final int digits) {
        final Label label = new Label(tab, SWT.NONE);
        label.setText(text);
        final Spinner spinner = new Spinner(tab, SWT.NONE);
        spinner.setSelection(value);
        spinner.setDigits(digits);
        return spinner;
    }

    int lastLevels = 4;

    int lastBlocks = 3;

    private void populateHlbTab(final Composite hlbTab, final Shell shell) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        hlbTab.setLayout(layout);

        final Spinner noLevels = createSpinner(hlbTab, "# of levels:",
                this.lastLevels);
        final Spinner noBlocks = createSpinner(hlbTab, "# of blocks:",
                this.lastBlocks);

        final Button okButton = new Button(hlbTab, SWT.PUSH);
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                lastLevels = noLevels.getSelection();
                lastBlocks = noBlocks.getSelection();
                ui.generateHlbArena(lastLevels, lastBlocks);
                shell.dispose();
            }
        });
        okButton.setText("Generate");
    }

    int lastLinearSize = 4;

    private void populateLinearTab(final Composite linearTab, final Shell shell) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        linearTab.setLayout(layout);

        final Button weakButton = createRadioButton(linearTab, "weak");
        final Button solitaireButton = createRadioButton(linearTab, "solitaire");
        final Button resilientButton = createRadioButton(linearTab, "resilient");
        final Button hardButton = createRadioButton(linearTab, "hard");
        final Button twoRingButton = createRadioButton(linearTab, "two ring");

        final Spinner noLevels = createSpinner(linearTab, "# of levels:",
                lastLinearSize);

        final Button okButton = new Button(linearTab, SWT.PUSH);
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                lastLinearSize = noLevels.getSelection();
                final GeneratorType type;
                if (weakButton.getSelection()) {
                    type = GeneratorType.WEAK;
                } else if (solitaireButton.getSelection()) {
                    type = GeneratorType.SOLITAIRE;
                } else if (resilientButton.getSelection()) {
                    type = GeneratorType.RESILIENT;
                } else if (hardButton.getSelection()) {
                    type = GeneratorType.HARD;
                } else if (twoRingButton.getSelection()) {
                    type = GeneratorType.TWO_RING;
                } else {
                    ui.displayError("No selection of generator type made.");
                    return;
                }
                ui.generateLinearArena(type, lastLinearSize);
                shell.dispose();
            }
        });
        okButton.setText("Generate");
    }

    private Button createRadioButton(final Composite tab, final String text) {
        final Button button = new Button(tab, SWT.RADIO);
        button.setText(text);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        button.setLayoutData(data);
        return button;
    }
}