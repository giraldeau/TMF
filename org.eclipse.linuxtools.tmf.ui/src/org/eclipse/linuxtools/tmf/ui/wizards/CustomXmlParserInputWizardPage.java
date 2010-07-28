package org.eclipse.linuxtools.tmf.ui.wizards;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTraceDefinition.InputAttribute;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTraceDefinition.InputElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CustomXmlParserInputWizardPage extends WizardPage {

    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String SIMPLE_DATE_FORMAT_URL = "http://java.sun.com/javase/6/docs/api/java/text/SimpleDateFormat.html#skip-navbar_top";
    private static final Image elementImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/element_icon.gif");
    private static final Image addImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/add_button.gif");
    private static final Image addNextImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/addnext_button.gif");
    private static final Image addChildImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/addchild_button.gif");
    private static final Image addManyImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/addmany_button.gif");
    private static final Image deleteImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/delete_button.gif");
    private static final Image moveUpImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/moveup_button.gif");
    private static final Image moveDownImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/movedown_button.gif");
    private static final Image helpImage = TmfUiPlugin.getDefault().getImageFromPath("/icons/help_button.gif");
    private static final Color COLOR_LIGHT_RED = new Color(Display.getDefault(), 255, 192, 192);
    private static final Color COLOR_TEXT_BACKGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    private static final Color COLOR_WIDGET_BACKGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

    private ISelection selection;
    private CustomXmlTraceDefinition definition;
    private String editDefinitionName;
    private String defaultDescription;
    private ElementNode selectedElement;
    private Composite container;
    private Text logtypeText;
    private Text timeStampOutputFormatText;
    private Text timeStampPreviewText;
    private Button removeButton;
    private Button addChildButton;
    private Button addNextButton;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button feelingLuckyButton;
    private ScrolledComposite treeScrolledComposite;
    private ScrolledComposite elementScrolledComposite;
    private TreeViewer treeViewer;
    private Composite treeContainer;
    private Composite elementContainer;
    private Text errorText;
    private StyledText inputText;
    private Font fixedFont;
    private UpdateListener updateListener;
    private Browser helpBrowser;
    private Element documentElement;

    // variables used recursively through element traversal
    private String timeStampValue;
    private String timeStampFormat;
    private boolean timeStampFound;
    private int logEntriesCount;
    private boolean logEntryFound;
    private int logEntryNestedCount;

    protected CustomXmlParserInputWizardPage(ISelection selection, CustomXmlTraceDefinition definition) {
        super("CustomXmlParserWizardPage");
        if (definition == null) {
            setTitle("New Custom XML Parser");
            defaultDescription = "Create a new custom parser for XML log files";
        } else {
            setTitle("Edit Custom XML Parser");
            defaultDescription = "Edit an existing custom parser for XML log files";
        }
        setDescription(defaultDescription);
        this.selection = selection;
        this.definition = definition;
        if (definition != null) {
            this.editDefinitionName = definition.definitionName;
        }
    }

    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());

        updateListener = new UpdateListener();
        
        Composite headerComposite = new Composite(container, SWT.FILL);
        GridLayout headerLayout = new GridLayout(5, false);
        headerLayout.marginHeight = 0;
        headerLayout.marginWidth = 0;
        headerComposite.setLayout(headerLayout);
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label logtypeLabel = new Label(headerComposite, SWT.NULL);
        logtypeLabel.setText("Log type:");
        
        logtypeText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        logtypeText.setLayoutData(new GridData(120, SWT.DEFAULT));
        logtypeText.addModifyListener(updateListener);
        
        Label timeStampFormatLabel = new Label(headerComposite, SWT.NULL);
        timeStampFormatLabel.setText("Time Stamp format:");
        
        timeStampOutputFormatText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        timeStampOutputFormatText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        timeStampOutputFormatText.setText(DEFAULT_TIMESTAMP_FORMAT);
        timeStampOutputFormatText.addModifyListener(updateListener);

        Button dateFormatHelpButton = new Button(headerComposite, SWT.PUSH);
        dateFormatHelpButton.setImage(helpImage);
        dateFormatHelpButton.setToolTipText("Date Format Help");
        dateFormatHelpButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent e) {
                openHelpShell(SIMPLE_DATE_FORMAT_URL);
            }
        });
        
        Label timeStampPreviewLabel = new Label(headerComposite, SWT.NULL);
        timeStampPreviewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1));
        timeStampPreviewLabel.setText("Preview:");
        
        timeStampPreviewText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        timeStampPreviewText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        timeStampPreviewText.setText("*no time stamp element or attribute*");

        createButtonBar();
        
        SashForm vSash = new SashForm(container, SWT.VERTICAL);
        vSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        vSash.setBackground(vSash.getDisplay().getSystemColor(SWT.COLOR_GRAY));

        SashForm hSash = new SashForm(vSash, SWT.HORIZONTAL);
        hSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        treeScrolledComposite = new ScrolledComposite(hSash, SWT.V_SCROLL | SWT.H_SCROLL);
        treeScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeContainer = new Composite(treeScrolledComposite, SWT.NONE);
        treeContainer.setLayout(new FillLayout());
        treeScrolledComposite.setContent(treeContainer);
        treeScrolledComposite.setExpandHorizontal(true);
        treeScrolledComposite.setExpandVertical(true);
        
        treeViewer = new TreeViewer(treeContainer, SWT.SINGLE | SWT.BORDER);
        treeViewer.setContentProvider(new InputElementTreeNodeContentProvider());
        treeViewer.setLabelProvider(new InputElementTreeLabelProvider());
        treeViewer.addSelectionChangedListener(new InputElementTreeSelectionChangedListener());
        treeContainer.layout();
        
        treeScrolledComposite.setMinSize(treeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, treeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        
        elementScrolledComposite = new ScrolledComposite(hSash, SWT.V_SCROLL);
        elementScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        elementContainer = new Composite(elementScrolledComposite, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 1;
        gl.marginWidth = 0;
        elementContainer.setLayout(gl);
        elementScrolledComposite.setContent(elementContainer);
        elementScrolledComposite.setExpandHorizontal(true);
        elementScrolledComposite.setExpandVertical(true);

        if (definition == null) {
            definition = new CustomXmlTraceDefinition();
        }
        loadDefinition(definition);
        treeViewer.expandAll();
        elementContainer.layout();
        
        elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);
        
        hSash.setWeights(new int[] {1, 2});
        
        if (definition.rootInputElement == null) {
            removeButton.setEnabled(false);
            addChildButton.setToolTipText("Add document element");
            addNextButton.setEnabled(false);
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
        } else { // root is selected
            addNextButton.setEnabled(false);
        }
        
        Composite sashBottom = new Composite(vSash, SWT.NONE);
        GridLayout sashBottomLayout = new GridLayout(2, false);
        sashBottomLayout.marginHeight = 0;
        sashBottomLayout.marginWidth = 0;
        sashBottom.setLayout(sashBottomLayout);

        Label previewLabel = new Label(sashBottom, SWT.NULL);
        previewLabel.setText("Preview input");

        errorText = new Text(sashBottom, SWT.SINGLE | SWT.READ_ONLY);
        errorText.setBackground(COLOR_WIDGET_BACKGROUND);
        errorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        errorText.setVisible(false);
        
        inputText = new StyledText(sashBottom, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        if (fixedFont == null) {
            if (System.getProperty("os.name").contains("Windows")) {
                fixedFont = new Font(Display.getCurrent(), new FontData("Courier New", 10, SWT.NORMAL));
            } else {
                fixedFont = new Font(Display.getCurrent(), new FontData("Monospace", 10, SWT.NORMAL));
            }
        }
        inputText.setFont(fixedFont);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd.heightHint = inputText.computeSize(SWT.DEFAULT, inputText.getLineHeight() * 4).y;
        gd.widthHint = 800;
        inputText.setLayoutData(gd);
        inputText.setText(getSelectionText());
        inputText.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e) {
                parseXmlInput(inputText.getText());
            }});
        inputText.addModifyListener(updateListener);

        vSash.setWeights(new int[] {hSash.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, sashBottom.computeSize(SWT.DEFAULT, SWT.DEFAULT).y});
        
        setControl(container);
    }

    private void createButtonBar() {
        Composite buttonBar = new Composite(container, SWT.NONE);
        GridLayout buttonBarLayout = new GridLayout(6, false);
        buttonBarLayout.marginHeight = 0;
        buttonBarLayout.marginWidth = 0;
        buttonBar.setLayout(buttonBarLayout);
        
        removeButton = new Button(buttonBar, SWT.PUSH);
        removeButton.setImage(deleteImage);
        removeButton.setToolTipText("Remove element");
        removeButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty() || selectedElement == null) return;
                removeElement();
                InputElement inputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputElement == definition.rootInputElement) {
                    definition.rootInputElement = null;
                } else {
                    inputElement.parentElement.childElements.remove(inputElement);
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
                removeButton.setEnabled(false);
                if (definition.rootInputElement == null) {
                    addChildButton.setEnabled(true);
                    addChildButton.setToolTipText("Add document element");
                } else {
                    addChildButton.setEnabled(false);
                }
                addNextButton.setEnabled(false);
                moveUpButton.setEnabled(false);
                moveDownButton.setEnabled(false);
            }
        });
        
        addChildButton = new Button(buttonBar, SWT.PUSH);
        addChildButton.setImage(addChildImage);
        addChildButton.setToolTipText("Add child element");
        addChildButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent e) {
                InputElement inputElement = new InputElement("", false, CustomXmlTraceDefinition.TAG_IGNORE, 0, "", null);
                if (definition.rootInputElement == null) {
                    definition.rootInputElement = inputElement;
                    inputElement.elementName = getChildNameSuggestion(null);
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    InputElement parentInputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    parentInputElement.addChild(inputElement);
                    inputElement.elementName = getChildNameSuggestion(parentInputElement);
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputElement), true);
            }
        });
        
        addNextButton = new Button(buttonBar, SWT.PUSH);
        addNextButton.setImage(addNextImage);
        addNextButton.setToolTipText("Add next element");
        addNextButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent e) {
                InputElement inputElement = new InputElement("", false, CustomXmlTraceDefinition.TAG_IGNORE, 0, "", null);
                if (definition.rootInputElement == null) {
                    definition.rootInputElement = inputElement;
                    inputElement.elementName = getChildNameSuggestion(null);
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    InputElement previousInputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    if (previousInputElement == definition.rootInputElement) {
                        return;
                    } else {
                        previousInputElement.addNext(inputElement);
                        inputElement.elementName = getChildNameSuggestion(inputElement.parentElement);
                    }
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputElement), true);
            }
        });
        
        feelingLuckyButton = new Button(buttonBar, SWT.PUSH);
        feelingLuckyButton.setImage(addManyImage);
        feelingLuckyButton.setToolTipText("Feeling lucky");
        feelingLuckyButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent e) {
                InputElement inputElement = null;
                if (definition.rootInputElement == null) {
                    if (getChildNameSuggestion(null).length() != 0) {
                        inputElement = new InputElement(getChildNameSuggestion(null), false, CustomXmlTraceDefinition.TAG_IGNORE, 0, "", null);
                        definition.rootInputElement = inputElement;
                        feelingLucky(inputElement);
                    }
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    inputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    feelingLucky(inputElement);
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputElement), true);
                treeViewer.expandToLevel(inputElement, TreeViewer.ALL_LEVELS);
            }
        });
        
        moveUpButton = new Button(buttonBar, SWT.PUSH);
        moveUpButton.setImage(moveUpImage);
        moveUpButton.setToolTipText("Move up");
        moveUpButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty()) return;
                InputElement inputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputElement == definition.rootInputElement) {
                    return;
                } else {
                    inputElement.moveUp();
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });
        
        moveDownButton = new Button(buttonBar, SWT.PUSH);
        moveDownButton.setImage(moveDownImage);
        moveDownButton.setToolTipText("Move down");
        moveDownButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty()) return;
                InputElement inputElement = (InputElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputElement == definition.rootInputElement) {
                    return;
                } else {
                    inputElement.moveDown();
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });
    }

    private void feelingLucky(InputElement inputElement) {
        while (true) {
            String attributeName = getAttributeNameSuggestion(inputElement);
            if (attributeName.length() == 0) {
                break;
            } else {
                InputAttribute attribute = new InputAttribute(attributeName, attributeName, 0, "");
                inputElement.addAttribute(attribute);
            }
        }
        while (true) {
            String childName = getChildNameSuggestion(inputElement);
            if (childName.length() == 0) {
                break;
            } else {
                InputElement childElement = new InputElement(childName, false, CustomXmlTraceDefinition.TAG_IGNORE, 0, "", null);
                inputElement.addChild(childElement);
                feelingLucky(childElement);
            }
        }
    }
    
    private class InputElementTreeNodeContentProvider implements ITreeContentProvider {

        public Object[] getElements(Object inputElement) {
            CustomXmlTraceDefinition def = (CustomXmlTraceDefinition) inputElement;
            if (def.rootInputElement != null) {
                return new Object[] {def.rootInputElement};
            } else {
                return new Object[0];
            }
        }

        public Object[] getChildren(Object parentElement) {
            InputElement inputElement = (InputElement) parentElement;
            if (inputElement.childElements == null) return new InputElement[0];
            return inputElement.childElements.toArray();
        }

        public boolean hasChildren(Object element) {
            InputElement inputElement = (InputElement) element;
            return (inputElement.childElements != null && inputElement.childElements.size() > 0);
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object getParent(Object element) {
            InputElement inputElement = (InputElement) element;
            return inputElement.parentElement;
        }
    }
    
    private class InputElementTreeLabelProvider extends ColumnLabelProvider {

        @Override
        public Image getImage(Object element) {
            return elementImage;
        }

        @Override
        public String getText(Object element) {
            InputElement inputElement = (InputElement) element;
            return (inputElement.elementName.trim().length() == 0) ? "?" : inputElement.elementName;
        }
    }

    private class InputElementTreeSelectionChangedListener implements ISelectionChangedListener {
        public void selectionChanged(SelectionChangedEvent event) {
            if (selectedElement != null) {
                selectedElement.dispose();
            }
            if (!(event.getSelection().isEmpty()) && event.getSelection() instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                InputElement inputElement = (InputElement) selection.getFirstElement();
                selectedElement = new ElementNode(elementContainer, inputElement);
                elementContainer.layout();
                elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);
                container.layout();
                validate();
                updatePreviews();
                removeButton.setEnabled(true);
                addChildButton.setEnabled(true);
                addChildButton.setToolTipText("Add child element");
                if (definition.rootInputElement == inputElement) {
                    addNextButton.setEnabled(false);
                } else {
                    addNextButton.setEnabled(true);
                }
                moveUpButton.setEnabled(true);
                moveDownButton.setEnabled(true);
            } else {
                removeButton.setEnabled(false);
                if (definition.rootInputElement == null) {
                    addChildButton.setEnabled(true);
                    addChildButton.setToolTipText("Add document element");
                } else {
                    addChildButton.setEnabled(false);
                }
                addNextButton.setEnabled(false);
                moveUpButton.setEnabled(false);
                moveDownButton.setEnabled(false);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        if (fixedFont != null) {
            fixedFont.dispose();
            fixedFont = null;
        }
        super.dispose();
    }

    private void loadDefinition(CustomXmlTraceDefinition def) {
        logtypeText.setText(def.definitionName);
        timeStampOutputFormatText.setText(def.timeStampOutputFormat);
        treeViewer.setInput(def);
        
        if (def.rootInputElement != null) {
            treeViewer.setSelection(new StructuredSelection(def.rootInputElement));
        }
    }
    
    private String getName(InputElement inputElement) {
        String name = (inputElement.elementName.trim().length() == 0) ? "?" : inputElement.elementName.trim();
        if (inputElement.parentElement == null) {
            return name;
        }
        return getName(inputElement.parentElement) + " : " + name;
    }

    private String getName(InputAttribute inputAttribute, InputElement inputElement) {
        String name = (inputAttribute.attributeName.trim().length() == 0) ? "?" : inputAttribute.attributeName.trim();
        return getName(inputElement) + " : " + name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            validate();
            updatePreviews();
        }
        super.setVisible(visible);
    }

    public List<String> getInputNames() {
        return getInputNames(definition.rootInputElement);
    }
    
    public List<String> getInputNames(InputElement inputElement) {
        List<String> inputs = new ArrayList<String>();
        if (inputElement.inputName != null && !inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_IGNORE)) {
            String inputName = inputElement.inputName;
            if (!inputs.contains(inputName)) {
                inputs.add(inputName);
            }
        }
        if (inputElement.attributes != null) {
            for (InputAttribute attribute : inputElement.attributes) {
                String inputName = attribute.inputName;
                if (!inputs.contains(inputName)) {
                    inputs.add(inputName);
                }
            }
        }
        if (inputElement.childElements != null) {
            for (InputElement childInputElement : inputElement.childElements) {
                for (String inputName : getInputNames(childInputElement)) {
                    if (!inputs.contains(inputName)) {
                        inputs.add(inputName);
                    }
                }
            }
        }
        return inputs;
    }
    
    private void removeElement() {
        selectedElement.dispose();
        selectedElement = null;
        elementContainer.layout();
        elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);
        container.layout();
    }

    private String getSelectionText() {
        InputStream inputStream = null;
        if (this.selection instanceof IStructuredSelection) {
            Object selection = ((IStructuredSelection)this.selection).getFirstElement();
            if (selection instanceof IFile) {
                IFile file = (IFile)selection;
                try {
                    inputStream = file.getContents();
                } catch (CoreException e) {
                    return "";
                }
            }
        }
        if (inputStream != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                parseXmlInput(sb.toString());
                return sb.toString();
            } catch (IOException e) {
                return "";
            }
        }
        return "";
    }
    
    private void parseXmlInput(final String string) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            EntityResolver resolver = new EntityResolver () {
                public InputSource resolveEntity (String publicId, String systemId) {
                    String empty = "";
                    ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
                    return new InputSource(bais);
                }
            };
            db.setEntityResolver(resolver);

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler(){
                public void error(SAXParseException saxparseexception) throws SAXException {}
                public void warning(SAXParseException saxparseexception) throws SAXException {}
                public void fatalError(SAXParseException saxparseexception) throws SAXException {
                    if (string.trim().length() != 0) {
                        errorText.setText(saxparseexception.getMessage());
                        errorText.setBackground(COLOR_LIGHT_RED);
                        errorText.setVisible(true);
                    }
                    throw saxparseexception;
                }});
            
            errorText.setVisible(false);
            Document doc = null;
            doc = db.parse(new ByteArrayInputStream(string.getBytes()));
            documentElement = doc.getDocumentElement();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            documentElement = null;
        } catch (SAXException e) {
            documentElement = null;
        } catch (IOException e) {
            e.printStackTrace();
            documentElement = null;
        }
    }

    private void updatePreviews() {
        updatePreviews(false);
    }

    private void updatePreviews(boolean updateAll) {
        if (inputText == null) {
            // early update during construction
            return;
        }
        inputText.setStyleRanges(new StyleRange[] {});
        if (selectedElement == null) {
            return;
        }
        
        timeStampValue = null;
        timeStampFormat = null;
        logEntriesCount = 0;
        logEntryFound = false;
        
        selectedElement.updatePreview();

        if (timeStampValue != null && timeStampFormat != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(timeStampFormat);
                Date date = dateFormat.parse(timeStampValue);
                dateFormat = new SimpleDateFormat(timeStampOutputFormatText.getText().trim());
                timeStampPreviewText.setText(dateFormat.format(date));
            } catch (ParseException e) {
                timeStampPreviewText.setText("*parse exception* [" + timeStampValue + "] <> [" + timeStampFormat + "]");
            } catch (IllegalArgumentException e) {
                timeStampPreviewText.setText("*parse exception* [Illegal Argument]");
            }
        } else {
            timeStampPreviewText.setText("*no matching time stamp*");
        }
    }

    private void openHelpShell(String url) {
        if (helpBrowser != null && !helpBrowser.isDisposed()) {
            helpBrowser.getShell().setActive();
            if (!helpBrowser.getUrl().equals(url)) {
                helpBrowser.setUrl(url);
            }
            return;
        }
        final Shell helpShell = new Shell(getShell(), SWT.SHELL_TRIM);
        helpShell.setLayout(new FillLayout());
        helpBrowser = new Browser(helpShell, SWT.NONE);
        helpBrowser.addTitleListener(new TitleListener() {
           public void changed(TitleEvent event) {
               helpShell.setText(event.title);
           }
        });
        helpBrowser.setBounds(0,0,600,400);
        helpShell.pack();
        helpShell.open();
        helpBrowser.setUrl(url);
    }

    private class UpdateListener implements ModifyListener, SelectionListener {

        public void modifyText(ModifyEvent e) {
            validate();
            updatePreviews();
        }

        public void widgetDefaultSelected(SelectionEvent e) {
            validate();
            updatePreviews();
        }

        public void widgetSelected(SelectionEvent e) {
            validate();
            updatePreviews();
        }

    }
    
    private class ElementNode {
        final InputElement inputElement;
        final Group group;
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        ArrayList<ElementNode> childElements = new ArrayList<ElementNode>();
        Text elementNameText;
        Composite tagComposite;
        Combo tagCombo;
        Label tagLabel;
        Text tagText;
        Combo actionCombo;
        Label previewLabel;
        Text previewText;
        Button logEntryButton;
        Label fillerLabel;
        Composite addAttributeComposite;
        Button addAttributeButton;
        Label addAttributeLabel;
        
        public ElementNode(Composite parent, InputElement inputElement) {
            this.inputElement = inputElement;
            
            group = new Group(parent, SWT.NONE);
            GridLayout gl = new GridLayout(2, false);
            gl.marginHeight = 0;
            group.setLayout(gl);
            group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            group.setText(getName(inputElement));
            
            Label label = new Label(group, SWT.NULL);
            label.setText("Element name:");
            label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            
            elementNameText = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            elementNameText.setLayoutData(gd);
            elementNameText.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e) {
                    ElementNode.this.inputElement.elementName = elementNameText.getText().trim();
                    group.setText(getName(ElementNode.this.inputElement));
                }});
            elementNameText.setText(inputElement.elementName);
            elementNameText.addModifyListener(updateListener);

            if (inputElement.parentElement != null) {
                previewLabel = new Label(group, SWT.NULL);
                previewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
                previewLabel.setText("Preview:");
                
                previewText = new Text(group, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
                gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                gd.widthHint = 0;
                previewText.setLayoutData(gd);
                previewText.setText("*no matching element*");
                previewText.setBackground(COLOR_WIDGET_BACKGROUND);
                
                logEntryButton = new Button(group, SWT.CHECK);
                logEntryButton.setText("Log Entry");
                logEntryButton.setSelection(inputElement.logEntry);
                logEntryButton.addSelectionListener(new SelectionListener(){
                    public void widgetDefaultSelected(SelectionEvent e) {}
                    public void widgetSelected(SelectionEvent e) {
                        InputElement parent = ElementNode.this.inputElement.parentElement;
                        while (parent != null) {
                            parent.logEntry = false;
                            parent = parent.parentElement;
                        }
                    }});
                logEntryButton.addSelectionListener(updateListener);
    
                tagComposite = new Composite(group, SWT.FILL);
                GridLayout tagLayout = new GridLayout(4, false);
                tagLayout.marginWidth = 0;
                tagLayout.marginHeight = 0;
                tagComposite.setLayout(tagLayout);
                tagComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                
                tagCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
                tagCombo.setItems(new String[] {CustomXmlTraceDefinition.TAG_IGNORE,
                                                CustomXmlTraceDefinition.TAG_TIMESTAMP,
                                                CustomXmlTraceDefinition.TAG_MESSAGE,
                                                CustomXmlTraceDefinition.TAG_OTHER});
                tagCombo.setVisibleItemCount(tagCombo.getItemCount());
                tagCombo.addSelectionListener(new SelectionListener(){
                    public void widgetDefaultSelected(SelectionEvent e) {}
                    public void widgetSelected(SelectionEvent e) {
                        tagText.removeModifyListener(updateListener);
                        switch (tagCombo.getSelectionIndex()) {
                        case 0: //Ignore
                            tagLabel.setVisible(false);
                            tagText.setVisible(false);
                            actionCombo.setVisible(false);
                            break;
                        case 1: //Time Stamp
                            tagLabel.setText("format:");
                            tagLabel.setVisible(true);
                            tagText.setVisible(true);
                            tagText.addModifyListener(updateListener);
                            actionCombo.setVisible(true);
                            break;
                        case 2: //Message
                            tagLabel.setVisible(false);
                            tagText.setVisible(false);
                            actionCombo.setVisible(true);
                            break;
                        case 3: //Other
                            tagLabel.setText("tag name:");
                            tagLabel.setVisible(true);
                            if (tagText.getText().trim().length() == 0) {
                                tagText.setText(elementNameText.getText().trim());
                            }
                            tagText.setVisible(true);
                            tagText.addModifyListener(updateListener);
                            actionCombo.setVisible(true);
                            break;
                        }
                        tagComposite.layout();
                        validate();
                        updatePreviews();
                    }});
                
                tagLabel = new Label(tagComposite, SWT.NULL);
                tagLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
                
                tagText = new Text(tagComposite, SWT.BORDER | SWT.SINGLE);
                gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
                gd.widthHint = 0;
                tagText.setLayoutData(gd);
                
                actionCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
                actionCombo.setItems(new String[] {"Set", "Append", "Append with |"});
                actionCombo.select(inputElement.inputAction);
                actionCombo.addSelectionListener(updateListener);
                
                if (inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_IGNORE)) {
                    tagCombo.select(0);
                    tagLabel.setVisible(false);
                    tagText.setVisible(false);
                    actionCombo.setVisible(false);
                } else if (inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_TIMESTAMP)) {
                    tagCombo.select(1);
                    tagLabel.setText("format:");
                    tagText.setText(inputElement.inputFormat);
                    tagText.addModifyListener(updateListener);
                } else if (inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_MESSAGE)) {
                    tagCombo.select(2);
                    tagLabel.setVisible(false);
                    tagText.setVisible(false);
                } else {
                    tagCombo.select(3);
                    tagLabel.setText("tag name:");
                    tagText.setText(inputElement.inputName);
                    tagText.addModifyListener(updateListener);
                }
            }

            if (inputElement.attributes != null) {
                for (InputAttribute inputAttribute : inputElement.attributes) {
                    Attribute attribute = new Attribute(group, this, inputAttribute, attributes.size()+1);
                    attributes.add(attribute);
                }
            }
            
            createAddButton();
        }
        
        private void updatePreview() {
            Element element = getPreviewElement(inputElement);
            if (inputElement.parentElement != null) { // no preview text for document element
                previewText.setText("*no matching element*");
                if (element != null) {
                    previewText.setText(CustomXmlTrace.parseElement(element, new StringBuffer()).toString());
                    if (logEntryButton.getSelection()) {
                        if (logEntryFound == false) {
                            logEntryFound = true;
                            logEntriesCount++;
                        } else {
                            logEntryButton.setSelection(false); // remove nested log entry
                        }
                    }
                    if (tagCombo.getText().equals(CustomXmlTraceDefinition.TAG_TIMESTAMP) && logEntriesCount <= 1) {
                        String value = previewText.getText().trim();
                        if (value.length() != 0) {
                            if (actionCombo.getSelectionIndex() == CustomXmlTraceDefinition.ACTION_SET) {
                                timeStampValue = value;
                                timeStampFormat = tagText.getText().trim();
                            } else if (actionCombo.getSelectionIndex() == CustomXmlTraceDefinition.ACTION_APPEND) {
                                if (timeStampValue != null) {
                                    timeStampValue += value;
                                    timeStampFormat += tagText.getText().trim();
                                } else {
                                    timeStampValue = value;
                                    timeStampFormat = tagText.getText().trim();
                                }
                            } else if (actionCombo.getSelectionIndex() == CustomXmlTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
                                if (timeStampValue != null) {
                                    timeStampValue += " | " + value;
                                    timeStampFormat += " | " + tagText.getText().trim();
                                } else {
                                    timeStampValue = value;
                                    timeStampFormat = tagText.getText().trim();
                                }
                            }
                        }
                    }
                }
            }
            for (Attribute attribute : attributes) {
                if (element != null) {
                    String value = element.getAttribute(attribute.attributeNameText.getText().trim());
                    if (value.length() != 0) {
                        attribute.previewText.setText(value);
                        if (attribute.tagCombo.getText().equals(CustomXmlTraceDefinition.TAG_TIMESTAMP) && logEntriesCount <= 1) {
                            if (attribute.actionCombo.getSelectionIndex() == CustomXmlTraceDefinition.ACTION_SET) {
                                timeStampValue = value;
                                timeStampFormat = attribute.tagText.getText().trim();
                            } else if (attribute.actionCombo.getSelectionIndex() == CustomXmlTraceDefinition.ACTION_APPEND) {
                                if (timeStampValue != null) {
                                    timeStampValue += value;
                                    timeStampFormat += attribute.tagText.getText().trim();
                                } else {
                                    timeStampValue = value;
                                    timeStampFormat = attribute.tagText.getText().trim();
                                }
                            } else if (attribute.actionCombo.getSelectionIndex() == CustomXmlTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
                                if (timeStampValue != null) {
                                    timeStampValue += " | " + value;
                                    timeStampFormat += " | " + attribute.tagText.getText().trim();
                                } else {
                                    timeStampValue = value;
                                    timeStampFormat = attribute.tagText.getText().trim();
                                }
                            }
                        }
                    } else {
                        attribute.previewText.setText("*no matching attribute*");
                    }
                } else {
                    attribute.previewText.setText("*no matching element*");
                }
            }
            for (ElementNode child : childElements) {
                child.updatePreview();
            }
            if (logEntryButton != null && logEntryButton.getSelection()) {
                logEntryFound = false;
            }
        }

        private void createAddButton() {
            fillerLabel = new Label(group, SWT.NONE);
            
            addAttributeComposite = new Composite(group, SWT.NONE);
            addAttributeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            GridLayout addAttributeLayout = new GridLayout(2, false);
            addAttributeLayout.marginHeight = 0;
            addAttributeLayout.marginWidth = 0;
            addAttributeComposite.setLayout(addAttributeLayout);
            
            addAttributeButton = new Button(addAttributeComposite, SWT.PUSH);
            addAttributeButton.setImage(addImage);
            addAttributeButton.setToolTipText("Add attribute");
            addAttributeButton.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent e) {
                    removeAddButton();
                    String attributeName = getAttributeNameSuggestion(inputElement);
                    InputAttribute inputAttribute = new InputAttribute(attributeName, attributeName, 0, "");
                    attributes.add(new Attribute(group, ElementNode.this, inputAttribute, attributes.size()+1));
                    createAddButton();
                    elementContainer.layout();
                    elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);
                    group.getParent().layout();
                    validate();
                    updatePreviews();
                }
            });
            
            addAttributeLabel = new Label(addAttributeComposite, SWT.NULL);
            addAttributeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            addAttributeLabel.setText("New attribute");
        }

        private void removeAddButton() {
            fillerLabel.dispose();
            addAttributeComposite.dispose();
        }
        
        private void removeAttribute(int attributeNumber) {
            if (--attributeNumber < attributes.size()) {
                attributes.remove(attributeNumber).dispose();
                for (int i = attributeNumber; i < attributes.size(); i++) {
                    attributes.get(i).setAttributeNumber(i+1);
                }
                elementContainer.layout();
                elementScrolledComposite.setMinSize(elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, elementContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);
                group.getParent().layout();
            }
        }
        
        private void dispose() {
            group.dispose();
        }

        private void extractInputs() {
            inputElement.elementName = elementNameText.getText().trim();
            if (inputElement.parentElement != null) {
                inputElement.logEntry = logEntryButton.getSelection();
                if (tagCombo.getText().equals(CustomXmlTraceDefinition.TAG_OTHER)) {
                    inputElement.inputName = tagText.getText().trim();
                } else {
                    inputElement.inputName = tagCombo.getText();
                    if (tagCombo.getText().equals(CustomXmlTraceDefinition.TAG_TIMESTAMP)) {
                        inputElement.inputFormat = tagText.getText().trim();
                    }
                }
                inputElement.inputAction = actionCombo.getSelectionIndex();
            }
            inputElement.attributes = new ArrayList<InputAttribute>(attributes.size());
            for (int i = 0; i < attributes.size(); i++) {
                Attribute attribute = attributes.get(i);
                InputAttribute inputAttribute = new InputAttribute();
                inputAttribute.attributeName = attribute.attributeNameText.getText().trim();
                if (attribute.tagCombo.getText().equals(CustomXmlTraceDefinition.TAG_OTHER)) {
                    inputAttribute.inputName = attribute.tagText.getText().trim();
                } else {
                    inputAttribute.inputName = attribute.tagCombo.getText();
                    if (attribute.tagCombo.getText().equals(CustomXmlTraceDefinition.TAG_TIMESTAMP)) {
                        inputAttribute.inputFormat = attribute.tagText.getText().trim();
                    }
                }
                inputAttribute.inputAction = attribute.actionCombo.getSelectionIndex();
                inputElement.addAttribute(inputAttribute);
            }
        }
    }

    private class Attribute {
        ElementNode element;
        int attributeNumber;
        
        // children of parent (must be disposed)
        Composite labelComposite;
        Composite attributeComposite;
        Label filler;
        Composite tagComposite;

        // children of labelComposite
        Label attributeLabel;
        
        // children of attributeComposite
        Text attributeNameText;
        Text previewText;
        
        // children of tagComposite
        Combo tagCombo;
        Label tagLabel;
        Text tagText;
        Combo actionCombo;
        
        public Attribute(Composite parent, ElementNode element, InputAttribute inputAttribute, int attributeNumber) {
            this.element = element;
            this.attributeNumber = attributeNumber;
            
            labelComposite = new Composite(parent, SWT.FILL);
            GridLayout labelLayout = new GridLayout(2, false);
            labelLayout.marginWidth = 0;
            labelLayout.marginHeight = 0;
            labelComposite.setLayout(labelLayout);
            labelComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            
            Button deleteButton = new Button(labelComposite, SWT.PUSH);
            deleteButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            deleteButton.setImage(deleteImage);
            deleteButton.setToolTipText("Remove attribute");
            deleteButton.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent e) {
                    Attribute.this.element.removeAttribute(Attribute.this.attributeNumber);
                    validate();
                    updatePreviews();
                }
            });
            
            attributeLabel = new Label(labelComposite, SWT.NULL);
            attributeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            attributeLabel.setText("Attribute");
            
            attributeComposite = new Composite(parent, SWT.FILL);
            GridLayout attributeLayout = new GridLayout(4, false);
            attributeLayout.marginWidth = 0;
            attributeLayout.marginHeight = 0;
            attributeComposite.setLayout(attributeLayout);
            attributeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            Label nameLabel = new Label(attributeComposite, SWT.NONE);
            nameLabel.setText("name:");
            
            attributeNameText = new Text(attributeComposite, SWT.BORDER | SWT.SINGLE);
            attributeNameText.setLayoutData(new GridData(120, SWT.DEFAULT));
            attributeNameText.setText(inputAttribute.attributeName);
            attributeNameText.addModifyListener(updateListener);
            
            Label previewLabel = new Label(attributeComposite, SWT.NONE);
            previewLabel.setText("Preview:");
            
            previewText = new Text(attributeComposite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            previewText.setLayoutData(gd);
            previewText.setText("*no match*");
            previewText.setBackground(COLOR_WIDGET_BACKGROUND);

            filler = new Label(parent, SWT.NULL);
            
            tagComposite = new Composite(parent, SWT.FILL);
            GridLayout tagLayout = new GridLayout(4, false);
            tagLayout.marginWidth = 0;
            tagLayout.marginHeight = 0;
            tagComposite.setLayout(tagLayout);
            tagComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            
            tagCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
            tagCombo.setItems(new String[] {CustomXmlTraceDefinition.TAG_TIMESTAMP,
                                            CustomXmlTraceDefinition.TAG_MESSAGE,
                                            CustomXmlTraceDefinition.TAG_OTHER});
            tagCombo.select(2); //Other
            tagCombo.addSelectionListener(new SelectionListener(){
                public void widgetDefaultSelected(SelectionEvent e) {}
                public void widgetSelected(SelectionEvent e) {
                    tagText.removeModifyListener(updateListener);
                    switch (tagCombo.getSelectionIndex()) {
                    case 0: //Time Stamp
                        tagLabel.setText("format:");
                        tagLabel.setVisible(true);
                        tagText.setVisible(true);
                        tagText.addModifyListener(updateListener);
                        break;
                    case 1: //Message
                        tagLabel.setVisible(false);
                        tagText.setVisible(false);
                        break;
                    case 2: //Other
                        tagLabel.setText("tag name:");
                        tagLabel.setVisible(true);
                        if (tagText.getText().trim().length() == 0) {
                            tagText.setText(attributeNameText.getText().trim());
                        }
                        tagText.setVisible(true);
                        tagText.addModifyListener(updateListener);
                        break;
                    }
                    tagComposite.layout();
                    validate();
                    updatePreviews();
                }});
            
            tagLabel = new Label(tagComposite, SWT.NULL);
            tagLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            
            tagText = new Text(tagComposite, SWT.BORDER | SWT.SINGLE);
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            tagText.setLayoutData(gd);
            tagText.setText(attributeNameText.getText());
            
            actionCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
            actionCombo.setItems(new String[] {"Set", "Append", "Append with |"});
            actionCombo.select(inputAttribute.inputAction);
            actionCombo.addSelectionListener(updateListener);
            
            if (inputAttribute.inputName.equals(CustomXmlTraceDefinition.TAG_TIMESTAMP)) {
                tagCombo.select(0);
                tagLabel.setText("format:");
                tagText.setText(inputAttribute.inputFormat);
                tagText.addModifyListener(updateListener);
            } else if (inputAttribute.inputName.equals(CustomXmlTraceDefinition.TAG_MESSAGE)) {
                tagCombo.select(1);
                tagLabel.setVisible(false);
                tagText.setVisible(false);
            } else {
                tagCombo.select(2);
                tagLabel.setText("tag name:");
                tagText.setText(inputAttribute.inputName);
                tagText.addModifyListener(updateListener);
            }
        }
        
        private void dispose() {
            labelComposite.dispose();
            attributeComposite.dispose();
            filler.dispose();
            tagComposite.dispose();
        }
        
        private void setAttributeNumber(int attributeNumber) {
            this.attributeNumber = attributeNumber;
            labelComposite.layout();
        }
    }

    private Element getPreviewElement(InputElement inputElement) {
        Element element = documentElement;
        if (element != null) {
            if (!documentElement.getNodeName().equals(definition.rootInputElement.elementName)) {
                return null;
            }
            ArrayList<String> elementNames = new ArrayList<String>();
            while (inputElement != null) {
                elementNames.add(inputElement.elementName);
                inputElement = inputElement.parentElement;
            }
            for (int i = elementNames.size() - 1; --i >= 0;) {
                NodeList childList = element.getChildNodes();
                element = null;
                for (int j = 0; j < childList.getLength(); j++) {
                    Node child = childList.item(j);
                    if (child instanceof Element && child.getNodeName().equals(elementNames.get(i))) {
                        element = (Element)child;
                        break;
                    }
                }
                if (element == null) {
                    break;
                }
            }
            if (element != null) {
                return element;
            }
        }
        return null;
    }
    
    private String getChildNameSuggestion(InputElement inputElement) {
        if (inputElement == null) {
            if (documentElement != null) {
                return documentElement.getNodeName();
            }
        } else {
            Element element = getPreviewElement(inputElement);
            if (element != null) {
                NodeList childNodes = element.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node instanceof Element) {
                        boolean unused = true;
                        if (inputElement.childElements != null) {
                            for (InputElement child : inputElement.childElements) {
                                if (child.elementName.equals(node.getNodeName())) {
                                    unused = false;
                                    break;
                                }
                            }
                        }
                        if (unused) {
                            return node.getNodeName();
                        }
                    }
                }
            }
        }
        return ("");
    }
    
    private String getAttributeNameSuggestion(InputElement inputElement) {
        Element element = getPreviewElement(inputElement);
        if (element != null) {
            NamedNodeMap attributeMap = element.getAttributes();
            for (int i = 0; i < attributeMap.getLength(); i++) {
                Node node = attributeMap.item(i);
                boolean unused = true;
                if (inputElement.attributes != null) {
                    for (InputAttribute attribute : inputElement.attributes) {
                        if (attribute.attributeName.equals(node.getNodeName())) {
                            unused = false;
                            break;
                        }
                    }
                }
                if (unused) {
                    return node.getNodeName();
                }
            }
        }
        return ("");
    }

    private void validate() {
        definition.definitionName = logtypeText.getText().trim();
        definition.timeStampOutputFormat = timeStampOutputFormatText.getText().trim();
        
        if (selectedElement != null) {
            selectedElement.extractInputs();
            treeViewer.refresh();
        }
        
        StringBuffer errors = new StringBuffer();

        if (definition.definitionName.length() == 0) {
            errors.append("Enter a name for the new log type. ");
            logtypeText.setBackground(COLOR_LIGHT_RED);
        } else {
            logtypeText.setBackground(COLOR_TEXT_BACKGROUND);
            for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                if (definition.definitionName.equals(def.definitionName)) {
                    if (editDefinitionName == null || ! editDefinitionName.equals(definition.definitionName)) {
                        errors.append("The log type name already exists. ");
                        logtypeText.setBackground(COLOR_LIGHT_RED);
                        break;
                    }
                }
            }
        }
        
        if (definition.rootInputElement == null) {
            errors.append("Add a document element. ");
        }
        
        if (definition.rootInputElement != null) {
            logEntryFound = false;
            logEntryNestedCount = 0;
            timeStampFound = false;
            
            errors.append(validateElement(definition.rootInputElement));

            if ((definition.rootInputElement.attributes != null && definition.rootInputElement.attributes.size() != 0) ||
                    (definition.rootInputElement.childElements != null && definition.rootInputElement.childElements.size() != 0) || errors.length() == 0) {
                if (!logEntryFound) {
                    errors.append("Identify a Log Entry element. ");
                }

                if (timeStampFound) {
                    if (timeStampOutputFormatText.getText().trim().length() == 0) {
                        errors.append("Enter the output format for the Time Stamp field. ");
                        timeStampOutputFormatText.setBackground(COLOR_LIGHT_RED);
                    } else {
                        try {
                            new SimpleDateFormat(timeStampOutputFormatText.getText().trim());
                            timeStampOutputFormatText.setBackground(COLOR_TEXT_BACKGROUND);
                        } catch (IllegalArgumentException e) {
                            errors.append("Enter a valid output format for the Time Stamp field. ");
                            timeStampOutputFormatText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                } else {
                    timeStampPreviewText.setText("*no time stamp element or attribute*");
                }
            }
        } else {
            timeStampPreviewText.setText("*no time stamp element or attribute*");
        }
    
        if (errors.length() == 0) {
            setDescription(defaultDescription);
            setPageComplete(true);
        } else {
            setDescription(errors.toString());
            setPageComplete(false);
        }
    }
    
    public StringBuffer validateElement(InputElement inputElement) {
        StringBuffer errors = new StringBuffer();
        ElementNode elementNode = null;
        if (selectedElement != null && selectedElement.inputElement.equals(inputElement)) elementNode = selectedElement;
        if (inputElement == definition.rootInputElement) {
            if (inputElement.elementName.length() == 0) {
                errors.append("Enter a name for the document element. ");
                if (elementNode != null) elementNode.elementNameText.setBackground(COLOR_LIGHT_RED);
            } else {
                if (elementNode != null) elementNode.elementNameText.setBackground(COLOR_TEXT_BACKGROUND);
            }
        }
        if (inputElement != definition.rootInputElement) {
            if (inputElement.logEntry) {
                logEntryFound = true;
                logEntryNestedCount++;
            }
            if (inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_TIMESTAMP)) {
                timeStampFound = true;
                if (inputElement.inputFormat.length() == 0) {
                    errors.append("Enter the input format for the Time Stamp (Element " + getName(inputElement) + "). ");
                    if (elementNode != null) elementNode.tagText.setBackground(COLOR_LIGHT_RED);
                } else {
                    try {
                        new SimpleDateFormat(inputElement.inputFormat);
                        if (elementNode != null) elementNode.tagText.setBackground(COLOR_TEXT_BACKGROUND);
                    } catch (IllegalArgumentException e) {
                        errors.append("Enter a valid input format for the Time Stamp (Element " + getName(inputElement) + "). ");
                        if (elementNode != null) elementNode.tagText.setBackground(COLOR_LIGHT_RED);
                    }
                }
            } else if (inputElement.inputName.length() == 0) {
                errors.append("Enter a name for the input . ");
                if (elementNode != null) elementNode.tagText.setBackground(COLOR_LIGHT_RED);
            } else {
                if (elementNode != null) elementNode.tagText.setBackground(COLOR_TEXT_BACKGROUND);
            }
        }
        if (inputElement.attributes != null) {
            if (elementNode != null) {
                for (Attribute attribute : elementNode.attributes) {
                    attribute.attributeNameText.setBackground(COLOR_TEXT_BACKGROUND);
                }
            }
            for (int i = 0; i < inputElement.attributes.size(); i++) {
                InputAttribute attribute = inputElement.attributes.get(i);
                boolean duplicate = false;
                for (int j = i + 1; j < inputElement.attributes.size(); j++) {
                    InputAttribute otherAttribute = inputElement.attributes.get(j);
                    if (otherAttribute.attributeName.equals(attribute.attributeName)) {
                        duplicate = true;
                        if (elementNode != null) {
                            elementNode.attributes.get(j).attributeNameText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                }
                if (attribute.attributeName.length() == 0) {
                    errors.append("Enter a name for the attribute (Attribute " + getName(inputElement) + ": ?). ");
                    if (elementNode != null) elementNode.attributes.get(i).attributeNameText.setBackground(COLOR_LIGHT_RED);
                } else if (duplicate) {
                    errors.append("Duplicate attribute names (Attribute " + getName(attribute, inputElement) +"). ");
                    if (elementNode != null) elementNode.attributes.get(i).attributeNameText.setBackground(COLOR_LIGHT_RED);
                }
                if (attribute.inputName.equals(CustomXmlTraceDefinition.TAG_TIMESTAMP)) {
                    timeStampFound = true;
                    if (attribute.inputFormat.length() == 0) {
                        errors.append("Enter the input format for the Time Stamp (Attribute " + getName(attribute, inputElement) +"). ");
                        if (elementNode != null) elementNode.attributes.get(i).tagText.setBackground(COLOR_LIGHT_RED);
                    } else {
                        try {
                            new SimpleDateFormat(attribute.inputFormat);
                            if (elementNode != null) elementNode.attributes.get(i).tagText.setBackground(COLOR_TEXT_BACKGROUND);
                        } catch (IllegalArgumentException e) {
                            errors.append("Enter a valid input format for the Time Stamp (Attribute " + getName(attribute, inputElement) +"). ");
                            if (elementNode != null) elementNode.attributes.get(i).tagText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                } else if (attribute.inputName.length() == 0) {
                    errors.append("Enter a name for the data group (Attribute " + getName(attribute, inputElement) +"). ");
                    if (elementNode != null) elementNode.attributes.get(i).tagText.setBackground(COLOR_LIGHT_RED);
                } else {
                    if (elementNode != null) elementNode.attributes.get(i).tagText.setBackground(COLOR_TEXT_BACKGROUND);
                }
            }
        }
        if (inputElement.childElements != null) {
            for (InputElement child : inputElement.childElements) {
                ElementNode childElementNode = null;
                if (selectedElement != null && selectedElement.inputElement.equals(child)) childElementNode = selectedElement;
                if (childElementNode != null) childElementNode.elementNameText.setBackground(COLOR_TEXT_BACKGROUND);
            }
            for (int i = 0; i < inputElement.childElements.size(); i++) {
                InputElement child = inputElement.childElements.get(i);
                ElementNode childElementNode = null;
                if (selectedElement != null && selectedElement.inputElement.equals(child)) childElementNode = selectedElement;
                if (child.elementName.length() == 0) {
                    errors.append("Enter a name for the element (Element " + getName(child) + "). ");
                    if (childElementNode != null) childElementNode.elementNameText.setBackground(COLOR_LIGHT_RED);
                } else {
                    boolean duplicate = false;
                    for (int j = i + 1; j < inputElement.childElements.size(); j++) {
                        InputElement otherChild = inputElement.childElements.get(j);
                        if (otherChild.elementName.equals(child.elementName)) {
                            duplicate = true;
                            ElementNode otherChildElementNode = null;
                            if (selectedElement != null && selectedElement.inputElement.equals(otherChild)) otherChildElementNode = selectedElement;
                            if (otherChildElementNode != null) otherChildElementNode.elementNameText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                    if (duplicate) {
                        errors.append("Duplicate element names (Element " + getName(child) + "). ");
                        if (childElementNode != null) childElementNode.elementNameText.setBackground(COLOR_LIGHT_RED);
                    }
                }
                
                errors.append(validateElement(child));
            }
        }
        if (inputElement.logEntry) {
            logEntryNestedCount--;
        }
        return errors;
    }
    
    public CustomXmlTraceDefinition getDefinition() {
        return definition;
    }

    public char[] getInputText() {
        return inputText.getText().toCharArray();
    }
}
