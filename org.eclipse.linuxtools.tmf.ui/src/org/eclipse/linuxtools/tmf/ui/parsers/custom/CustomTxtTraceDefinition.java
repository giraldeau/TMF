/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CustomTxtTraceDefinition extends CustomTraceDefinition {

    protected static final String CUSTOM_TXT_TRACE_DEFINITIONS_FILE_NAME = "custom_txt_parsers.xml";
    protected static final String CUSTOM_TXT_TRACE_DEFINITIONS_PATH_NAME =
        TmfUiPlugin.getDefault().getStateLocation().addTrailingSeparator().append(CUSTOM_TXT_TRACE_DEFINITIONS_FILE_NAME).toString();

    private static final String CUSTOM_TXT_TRACE_DEFINITION_ROOT_ELEMENT = "CustomTxtTraceDefinitionList";
    private static final String DEFINITION_ELEMENT = "Definition";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String TIME_STAMP_OUTPUT_FORMAT_ELEMENT = "TimeStampOutputFormat";
    private static final String INPUT_LINE_ELEMENT = "InputLine";
    private static final String CARDINALITY_ELEMENT = "Cardinality";
    private static final String MIN_ATTRIBUTE = "min";
    private static final String MAX_ATTRIBUTE = "max";
    private static final String REGEX_ELEMENT = "RegEx";
    private static final String INPUT_DATA_ELEMENT = "InputData";
    private static final String ACTION_ATTRIBUTE = "action";
    private static final String FORMAT_ATTRIBUTE = "format";
    private static final String OUTPUT_COLUMN_ELEMENT = "OutputColumn";
    
    public List<InputLine> inputs;

    public CustomTxtTraceDefinition() {
        this("", new ArrayList<InputLine>(0), new ArrayList<OutputColumn>(0), "");
    };
    
    public CustomTxtTraceDefinition(String logtype, List<InputLine> inputs, List<OutputColumn> outputs, String timeStampOutputFormat) {
        this.definitionName = logtype;
        this.inputs = inputs;
        this.outputs = outputs;
        this.timeStampOutputFormat = timeStampOutputFormat;
    }
    
    public static class InputLine {
        public List<InputData> columns;
        public Cardinality cardinality;
        private String regex;
        private Pattern pattern;
        public InputLine parentInput;
        public int level;
        public InputLine nextInput;
        public List<InputLine> childrenInputs;
        
        public InputLine() {};
        
        public InputLine(Cardinality cardinality, String regex, List<InputData> columns) {
            this.cardinality = cardinality;
            this.regex = regex;
            this.columns = columns;
        }
        
        public void setRegex(String regex) {
            this.regex = regex;
            this.pattern = null;
        }
        
        public String getRegex() {
            return regex;
        }
        
        public Pattern getPattern() throws PatternSyntaxException {
            if (pattern == null) {
                pattern = Pattern.compile(regex);
            }
            return pattern;
        }
        
        public void addChild(InputLine input) {
            if (childrenInputs == null) {
                childrenInputs = new ArrayList<InputLine>(1);
            } else if (childrenInputs.size() > 0) {
                InputLine last = childrenInputs.get(childrenInputs.size() - 1);
                last.nextInput = input;
            }
            childrenInputs.add(input);
            input.parentInput = this;
            input.level = this.level + 1;
        }

        public void addNext(InputLine input) {
            if (parentInput != null) {
                int index = parentInput.childrenInputs.indexOf(this);
                parentInput.childrenInputs.add(index + 1, input);
                InputLine next = nextInput;
                nextInput = input;
                input.nextInput = next;
            }
            input.parentInput = this.parentInput;
            input.level = this.level;
        }

        public void moveUp() {
            if (parentInput != null) {
                int index = parentInput.childrenInputs.indexOf(this);
                if (index > 0) {
                    parentInput.childrenInputs.add(index - 1 , parentInput.childrenInputs.remove(index));
                    parentInput.childrenInputs.get(index).nextInput = nextInput;
                    nextInput = parentInput.childrenInputs.get(index);
                }
            }
        }

        public void moveDown() {
            if (parentInput != null) {
                int index = parentInput.childrenInputs.indexOf(this);
                if (index < parentInput.childrenInputs.size() - 1) {
                    parentInput.childrenInputs.add(index + 1 , parentInput.childrenInputs.remove(index));
                    nextInput = parentInput.childrenInputs.get(index).nextInput;
                    parentInput.childrenInputs.get(index).nextInput = this;
                }
            }
        }

        public void addColumn(InputData column) {
            if (columns == null) {
                columns = new ArrayList<InputData>(1);
            }
            columns.add(column);
        }
        
        public List<InputLine> getNextInputs(Map<InputLine, Integer> countMap) {
            List<InputLine> nextInputs = new ArrayList<InputLine>();
            InputLine next = nextInput;
            while (next != null) {
                nextInputs.add(next);
                if (next.cardinality.min > 0) {
                    return nextInputs;
                }
                next = next.nextInput;
            }
            if (parentInput != null && parentInput.level > 0) {
                int parentCount = countMap.get(parentInput);
                if (parentCount < parentInput.getMaxCount()) {
                    nextInputs.add(parentInput);
                }
                if (parentCount < parentInput.getMinCount()) {
                    return nextInputs;
                }
                nextInputs.addAll(parentInput.getNextInputs(countMap));
            }
            return nextInputs;
        }
        
        public int getMinCount() {
            return cardinality.min;
        }
        
        public int getMaxCount() {
            return cardinality.max;
        }

        @Override
        public String toString() {
            return regex + " " + cardinality;
        }
        
    }

    public static class InputData {
        public String name;
        public int action;
        public String format;
        
        public InputData() {};
        
        public InputData(String name, int action, String format) {
            this.name = name;
            this.action = action;
            this.format = format;
        }
        
        public InputData(String name, int action) {
            this.name = name;
            this.action = action;
        }
    }

    public static class Cardinality {
        public final static int INF = Integer.MAX_VALUE;
        public final static Cardinality ONE = new Cardinality(1, 1);
        public final static Cardinality ONE_OR_MORE = new Cardinality(1, INF);
        public final static Cardinality ZERO_OR_ONE = new Cardinality(0, 1);
        public final static Cardinality ZERO_OR_MORE = new Cardinality(0, INF); 
        
        private int min;
        private int max;
        
        public Cardinality(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return "(" + (min >= 0 ? min : "?") + "," + (max == INF ? "\u221E" : (max >= 0 ? max : "?")) + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Cardinality)) return false;
            Cardinality other = (Cardinality) obj;
            return (this.min == other.min && this.max == other.max);
        }
    }
    
    public void save() {
        save(CUSTOM_TXT_TRACE_DEFINITIONS_PATH_NAME);
    }
    
    public void save(String path) {
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
                    throw saxparseexception;
                }});
            
            Document doc = null;
            File file = new File(path);
            if (file.canRead()) {
                doc = db.parse(file);
                if (! doc.getDocumentElement().getNodeName().equals(CUSTOM_TXT_TRACE_DEFINITION_ROOT_ELEMENT)) {
                    return;
                }
            } else {
                doc = db.newDocument();
                Node node = doc.createElement(CUSTOM_TXT_TRACE_DEFINITION_ROOT_ELEMENT);
                doc.appendChild(node);
            }

            Element root = doc.getDocumentElement();
            
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element &&
                        node.getNodeName().equals(DEFINITION_ELEMENT) &&
                        definitionName.equals(((Element) node).getAttribute(NAME_ATTRIBUTE))) {
                    root.removeChild(node);
                }
            }
            Element definitionElement = doc.createElement(DEFINITION_ELEMENT);
            root.appendChild(definitionElement);
            definitionElement.setAttribute(NAME_ATTRIBUTE, definitionName);
            
            Element formatElement = doc.createElement(TIME_STAMP_OUTPUT_FORMAT_ELEMENT);
            definitionElement.appendChild(formatElement);
            formatElement.appendChild(doc.createTextNode(timeStampOutputFormat));

            if (inputs != null) {
                for (InputLine inputLine : inputs) {
                    definitionElement.appendChild(createInputLineElement(inputLine, doc));
                }
            }

            if (outputs != null) {
                for (OutputColumn output : outputs) {
                    Element outputColumnElement = doc.createElement(OUTPUT_COLUMN_ELEMENT);
                    definitionElement.appendChild(outputColumnElement);
                    outputColumnElement.setAttribute(NAME_ATTRIBUTE, output.name);
                }
            }
            
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            //initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();
            
            FileWriter writer = new FileWriter(file);
            writer.write(xmlString);
            writer.close();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private Element createInputLineElement(InputLine inputLine, Document doc) {
        Element inputLineElement = doc.createElement(INPUT_LINE_ELEMENT);
        
        Element cardinalityElement = doc.createElement(CARDINALITY_ELEMENT);
        inputLineElement.appendChild(cardinalityElement);
        cardinalityElement.setAttribute(MIN_ATTRIBUTE, Integer.toString(inputLine.cardinality.min));
        cardinalityElement.setAttribute(MAX_ATTRIBUTE, Integer.toString(inputLine.cardinality.max));
        
        Element regexElement = doc.createElement(REGEX_ELEMENT);
        inputLineElement.appendChild(regexElement);
        regexElement.appendChild(doc.createTextNode(inputLine.regex));

        if (inputLine.columns != null) {
            for (InputData inputData : inputLine.columns) {
                Element inputDataElement = doc.createElement(INPUT_DATA_ELEMENT);
                inputLineElement.appendChild(inputDataElement);
                inputDataElement.setAttribute(NAME_ATTRIBUTE, inputData.name);
                inputDataElement.setAttribute(ACTION_ATTRIBUTE, Integer.toString(inputData.action));
                if (inputData.format != null) {
                    inputDataElement.setAttribute(FORMAT_ATTRIBUTE, inputData.format);
                }
            }
        }
        
        if (inputLine.childrenInputs != null) {
            for (InputLine childInputLine : inputLine.childrenInputs) {
                inputLineElement.appendChild(createInputLineElement(childInputLine, doc));
            }
        }
        
        return inputLineElement;
    }
    
    public static CustomTxtTraceDefinition[] loadAll() {
        return loadAll(CUSTOM_TXT_TRACE_DEFINITIONS_PATH_NAME);
    }
    
    public static CustomTxtTraceDefinition[] loadAll(String path) {
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
                    throw saxparseexception;
                }});

            File file = new File(path);
            if (!file.canRead()) {
                return new CustomTxtTraceDefinition[0];
            }
            Document doc = db.parse(file);

            Element root = doc.getDocumentElement();
            if (! root.getNodeName().equals(CUSTOM_TXT_TRACE_DEFINITION_ROOT_ELEMENT)) {
                return new CustomTxtTraceDefinition[0];
            }

            ArrayList<CustomTxtTraceDefinition> defList = new ArrayList<CustomTxtTraceDefinition>();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element && node.getNodeName().equals(DEFINITION_ELEMENT)) {
                    CustomTxtTraceDefinition def = extractDefinition((Element) node);
                    if (def != null) {
                        defList.add(def);
                    }
                }
            }
            return defList.toArray(new CustomTxtTraceDefinition[0]);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CustomTxtTraceDefinition[0];
    }

    public static CustomTxtTraceDefinition load(String definitionName) {
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
                    throw saxparseexception;
                }});

            File file = new File(CUSTOM_TXT_TRACE_DEFINITIONS_PATH_NAME);
            Document doc = db.parse(file);

            Element root = doc.getDocumentElement();
            if (! root.getNodeName().equals(CUSTOM_TXT_TRACE_DEFINITION_ROOT_ELEMENT)) {
                return null;
            }

            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element &&
                        node.getNodeName().equals(DEFINITION_ELEMENT) &&
                        definitionName.equals(((Element) node).getAttribute(NAME_ATTRIBUTE))) {
                    return extractDefinition((Element) node);
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static CustomTxtTraceDefinition extractDefinition(Element definitionElement) {
        CustomTxtTraceDefinition def = new CustomTxtTraceDefinition();
        
        def.definitionName = definitionElement.getAttribute(NAME_ATTRIBUTE);
        if (def.definitionName == null) return null;
        
        NodeList nodeList = definitionElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals(TIME_STAMP_OUTPUT_FORMAT_ELEMENT)) {
                Element formatElement = (Element) node;
                def.timeStampOutputFormat = formatElement.getTextContent();
            } else if (nodeName.equals(INPUT_LINE_ELEMENT)) {
                InputLine inputLine = extractInputLine((Element) node);
                if (inputLine != null) {
                    def.inputs.add(inputLine);
                }
            } else if (nodeName.equals(OUTPUT_COLUMN_ELEMENT)) {
                Element outputColumnElement = (Element) node;
                OutputColumn outputColumn = new OutputColumn();
                outputColumn.name = outputColumnElement.getAttribute(NAME_ATTRIBUTE);
                def.outputs.add(outputColumn);
            }
        }
        return def;
    }

    private static InputLine extractInputLine(Element inputLineElement) {
        InputLine inputLine = new InputLine();
        NodeList nodeList = inputLineElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals(CARDINALITY_ELEMENT)) {
                Element cardinalityElement = (Element) node;
                try {
                    int min = Integer.parseInt(cardinalityElement.getAttribute(MIN_ATTRIBUTE));
                    int max = Integer.parseInt(cardinalityElement.getAttribute(MAX_ATTRIBUTE));
                    inputLine.cardinality = new Cardinality(min, max);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (nodeName.equals(REGEX_ELEMENT)) {
                Element regexElement = (Element) node;
                inputLine.regex = regexElement.getTextContent();
            } else if (nodeName.equals(INPUT_DATA_ELEMENT)) {
                Element inputDataElement = (Element) node;
                InputData inputData = new InputData();
                inputData.name = inputDataElement.getAttribute(NAME_ATTRIBUTE);
                inputData.action = Integer.parseInt(inputDataElement.getAttribute(ACTION_ATTRIBUTE));
                inputData.format = inputDataElement.getAttribute(FORMAT_ATTRIBUTE);
                inputLine.addColumn(inputData);
            } else if (nodeName.equals(INPUT_LINE_ELEMENT)) {
                Element childInputLineElement = (Element) node;
                InputLine childInputLine = extractInputLine(childInputLineElement);
                if (childInputLine != null) {
                    inputLine.addChild(childInputLine);
                }
            }
        }
        return inputLine;
    }
    
    public static void delete(String definitionName) {
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
                    throw saxparseexception;
                }});

            File file = new File(CUSTOM_TXT_TRACE_DEFINITIONS_PATH_NAME);
            Document doc = db.parse(file);

            Element root = doc.getDocumentElement();
            if (! root.getNodeName().equals(CUSTOM_TXT_TRACE_DEFINITION_ROOT_ELEMENT)) {
                return;
            }

            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element &&
                        node.getNodeName().equals(DEFINITION_ELEMENT) &&
                        definitionName.equals(((Element) node).getAttribute(NAME_ATTRIBUTE))) {
                    root.removeChild(node);
                }
            }
            
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            //initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();
            
            FileWriter writer = new FileWriter(file);
            writer.write(xmlString);
            writer.close();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
