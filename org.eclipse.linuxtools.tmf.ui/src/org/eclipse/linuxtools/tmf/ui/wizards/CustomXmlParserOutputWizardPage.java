package org.eclipse.linuxtools.tmf.ui.wizards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomEventsTable;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTraceDefinition;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class CustomXmlParserOutputWizardPage extends WizardPage {

    private static final Image upImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/up_button.gif");
    private static final Image downImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/down_button.gif");
    private CustomXmlParserWizard wizard;
    private CustomXmlTraceDefinition definition;
    ArrayList<Output> outputs = new ArrayList<Output>();
    Output messageOutput;
    Composite container;
    SashForm sash;
    Text timestampFormatText;
    Text timestampPreviewText;
    ScrolledComposite outputsScrolledComposite;
    Composite outputsContainer;
    ScrolledComposite inputScrolledComposite;
    Composite tableContainer;
    CustomEventsTable previewTable;
    File tmpFile;
    
    protected CustomXmlParserOutputWizardPage(CustomXmlParserWizard wizard) {
        super("CustomParserOutputWizardPage");
        setTitle(wizard.inputPage.getTitle());
        setDescription("Customize the output of the parser");
        this.wizard = wizard;
        setPageComplete(false);
    }

    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());

        sash = new SashForm(container, SWT.VERTICAL);
        sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sash.setBackground(sash.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        
        outputsScrolledComposite = new ScrolledComposite(sash, SWT.V_SCROLL);
        outputsScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        outputsContainer = new Composite(outputsScrolledComposite, SWT.NONE);
        GridLayout outputsLayout = new GridLayout(4, false);
        outputsLayout.marginHeight = 10;
        outputsLayout.marginWidth = 0;
        outputsContainer.setLayout(outputsLayout);
        outputsScrolledComposite.setContent(outputsContainer);
        outputsScrolledComposite.setExpandHorizontal(true);
        outputsScrolledComposite.setExpandVertical(true);

        outputsContainer.layout();
        
        outputsScrolledComposite.setMinSize(outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-5);

        tableContainer = new Composite(sash, SWT.NONE);
        GridLayout tableLayout = new GridLayout();
        tableLayout.marginHeight = 0;
        tableLayout.marginWidth = 0;
        tableContainer.setLayout(tableLayout);
        previewTable = new CustomEventsTable(new CustomXmlTraceDefinition(), tableContainer, 0);

        if (wizard.definition != null) {
            loadDefinition(wizard.definition);
        }
        setControl(container);
        
    }

    @Override
    public void dispose() {
        previewTable.dispose();
        super.dispose();
    }

    private void loadDefinition(CustomTraceDefinition definition) {
        for (OutputColumn outputColumn : definition.outputs) {
            Output output = new Output(outputsContainer, outputColumn.name);
            outputs.add(output);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.definition = wizard.inputPage.getDefinition();
            List<String> outputNames = wizard.inputPage.getInputNames();
            
            // dispose outputs that have been removed in the input page
            Iterator<Output> iter = outputs.iterator();
            while (iter.hasNext()) {
                Output output = iter.next();
                boolean found = false;
                for (String name : outputNames) {
                    if (output.name.equals(name)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    output.dispose();
                    iter.remove();
                }
            }
            
            // create outputs that have been added in the input page
            for (String name : outputNames) {
                boolean found = false;
                for (Output output : outputs) {
                    if (output.name.equals(name)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    outputs.add(new Output(outputsContainer, name));
                }
            }
            
            outputsContainer.layout();
            outputsScrolledComposite.setMinSize(outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-5);
            updatePreviewTable();
            if (sash.getSize().y > outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + previewTable.getTable().getItemHeight()) {
                sash.setWeights(new int[] {outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, sash.getSize().y - outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y});
            } else {
                sash.setWeights(new int[] {outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, previewTable.getTable().getItemHeight()});
            }
            setPageComplete(true);
        } else {
            setPageComplete(false);
        }
        super.setVisible(visible);
    }

    private void moveBefore(Output moved) {
        int i = outputs.indexOf(moved);
        if (i > 0) {
            Output previous = outputs.get(i-1);
            moved.enabledButton.moveAbove(previous.enabledButton);
            moved.nameLabel.moveBelow(moved.enabledButton);
            moved.upButton.moveBelow(moved.nameLabel);
            moved.downButton.moveBelow(moved.upButton);
            outputs.add(i-1, outputs.remove(i));
            outputsContainer.layout();
            outputsScrolledComposite.setMinSize(outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-5);
            container.layout();
            updatePreviewTable();
        }
    }
    
    private void moveAfter(Output moved) {
        int i = outputs.indexOf(moved);
        if (i+1 < outputs.size()) {
            Output next = outputs.get(i+1);
            moved.enabledButton.moveBelow(next.downButton);
            moved.nameLabel.moveBelow(moved.enabledButton);
            moved.upButton.moveBelow(moved.nameLabel);
            moved.downButton.moveBelow(moved.upButton);
            outputs.add(i+1, outputs.remove(i));
            outputsContainer.layout();
            outputsScrolledComposite.setMinSize(outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-5);
            container.layout();
            updatePreviewTable();
        }
    }
    
    private void updatePreviewTable() {
        final int MAX_NUM_ENTRIES = 50;
        definition.outputs = extractOutputs();

        try {
            tmpFile = TmfUiPlugin.getDefault().getStateLocation().addTrailingSeparator().append("customwizard.tmp").toFile();
            FileWriter writer = new FileWriter(tmpFile);
            writer.write(wizard.inputPage.getInputText());
            writer.close();
            
            ITmfTrace trace = new CustomXmlTrace(tmpFile.getName(), definition, tmpFile.getAbsolutePath(), MAX_NUM_ENTRIES);
            previewTable.dispose();
            previewTable = new CustomEventsTable(definition, tableContainer, MAX_NUM_ENTRIES);
            previewTable.setTrace(trace);
            previewTable.getTable().setItemCount(MAX_NUM_ENTRIES); // Trigger parsing
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        tableContainer.layout();
        container.layout();
    }

    public List<OutputColumn> extractOutputs() {
        int numColumns = 0;
        for (int i = 0; i < outputs.size(); i++) {
            if (outputs.get(i).enabledButton.getSelection()) {
                numColumns++;
            }
        }
        List<OutputColumn> outputColumns = new ArrayList<OutputColumn>(numColumns);
        numColumns = 0;
        for (int i = 0; i < outputs.size(); i++) {
            Output output = outputs.get(i);
            if (output.enabledButton.getSelection()) {
                OutputColumn column = new OutputColumn();
                column.name = output.nameLabel.getText();
                outputColumns.add(column); 
            }
        }
        return outputColumns;
    }

    private class Output {
        String name;
        Button enabledButton;
        Text nameLabel;
        Button upButton;
        Button downButton;
        
        public Output(Composite parent, String name) {
            this.name = name;
            
            enabledButton = new Button(parent, SWT.CHECK);
            enabledButton.setToolTipText("Visible");
            enabledButton.setSelection(true);
            enabledButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    updatePreviewTable();
                }
            });
            if (messageOutput != null) {
                enabledButton.moveAbove(messageOutput.enabledButton);
            }
            
            nameLabel = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
            nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            nameLabel.setText(name);
            nameLabel.moveBelow(enabledButton);

            upButton = new Button(parent, SWT.PUSH);
            upButton.setImage(upImage);
            upButton.setToolTipText("Move Before");
            upButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    moveBefore(Output.this);
                }
            });
            upButton.moveBelow(nameLabel);
            
            downButton = new Button(parent, SWT.PUSH);
            downButton.setImage(downImage);
            downButton.setToolTipText("Move After");
            downButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    moveAfter(Output.this);
                }
            });
            downButton.moveBelow(upButton);
        }

        private void dispose() {
            enabledButton.dispose();
            nameLabel.dispose();
            upButton.dispose();
            downButton.dispose();
        }
    }
    
    public CustomXmlTraceDefinition getDefinition() {
        return definition;
    }
    
}
