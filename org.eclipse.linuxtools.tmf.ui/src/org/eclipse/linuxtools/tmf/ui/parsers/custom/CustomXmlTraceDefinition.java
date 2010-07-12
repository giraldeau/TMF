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

public class CustomXmlTraceDefinition extends CustomTraceDefinition {

    protected static final String CUSTOM_XML_TRACE_DEFINITIONS_FILE_NAME = "custom_xml_parsers.xml";
    protected static final String CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME =
        TmfUiPlugin.getDefault().getStateLocation().addTrailingSeparator().append(CUSTOM_XML_TRACE_DEFINITIONS_FILE_NAME).toString();

    public static final String TAG_IGNORE = "Ignore";

    private static final String CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT = "CustomXMLTraceDefinitionList";
    private static final String DEFINITION_ELEMENT = "Definition";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String LOG_ENTRY_ATTRIBUTE = "logentry";
    private static final String TIME_STAMP_OUTPUT_FORMAT_ELEMENT = "TimeStampOutputFormat";
    private static final String INPUT_ELEMENT_ELEMENT = "InputElement";
    private static final String ATTRIBUTE_ELEMENT = "Attribute";
    private static final String INPUT_DATA_ELEMENT = "InputData";
    private static final String ACTION_ATTRIBUTE = "action";
    private static final String FORMAT_ATTRIBUTE = "format";
    private static final String OUTPUT_COLUMN_ELEMENT = "OutputColumn";
    
    public InputElement rootInputElement;

    public CustomXmlTraceDefinition() {
        this("", null, new ArrayList<OutputColumn>(), "");
    };

    public CustomXmlTraceDefinition(String logtype, InputElement rootElement, List<OutputColumn> outputs, String timeStampOutputFormat) {
        this.definitionName = logtype;
        this.rootInputElement = rootElement;
        this.outputs = outputs;
        this.timeStampOutputFormat = timeStampOutputFormat;
    }

    public static class InputElement {
        public String elementName;
        public boolean logEntry;
        public String inputName;
        public int inputAction;
        public String inputFormat;
        public List<InputAttribute> attributes;
        public InputElement parentElement;
        public InputElement nextElement;
        public List<InputElement> childElements;
        
        public InputElement() {};
        
        public InputElement(String elementName, boolean logEntry, String inputName, int inputAction, String inputFormat, List<InputAttribute> attributes) {
            this.elementName = elementName;
            this.logEntry = logEntry;
            this.inputName = inputName;
            this.inputAction = inputAction;
            this.inputFormat = inputFormat;
            this.attributes = attributes;
        }
        
        public void addAttribute(InputAttribute attribute) {
            if (attributes == null) {
                attributes = new ArrayList<InputAttribute>(1);
            }
            attributes.add(attribute);
        }

        public void addChild(InputElement input) {
            if (childElements == null) {
                childElements = new ArrayList<InputElement>(1);
            } else if (childElements.size() > 0) {
                InputElement last = childElements.get(childElements.size() - 1);
                last.nextElement = input;
            }
            childElements.add(input);
            input.parentElement = this;
        }

        public void addNext(InputElement input) {
            if (parentElement != null) {
                int index = parentElement.childElements.indexOf(this);
                parentElement.childElements.add(index + 1, input);
                InputElement next = nextElement;
                nextElement = input;
                input.nextElement = next;
            }
            input.parentElement = this.parentElement;
        }

        public void moveUp() {
            if (parentElement != null) {
                int index = parentElement.childElements.indexOf(this);
                if (index > 0) {
                    parentElement.childElements.add(index - 1 , parentElement.childElements.remove(index));
                    parentElement.childElements.get(index).nextElement = nextElement;
                    nextElement = parentElement.childElements.get(index);
                }
            }
        }

        public void moveDown() {
            if (parentElement != null) {
                int index = parentElement.childElements.indexOf(this);
                if (index < parentElement.childElements.size() - 1) {
                    parentElement.childElements.add(index + 1 , parentElement.childElements.remove(index));
                    nextElement = parentElement.childElements.get(index).nextElement;
                    parentElement.childElements.get(index).nextElement = this;
                }
            }
        }

    }

    public static class InputAttribute {
        public String attributeName;
        public String inputName;
        public int inputAction;
        public String inputFormat;
        
        public InputAttribute() {};
        
        public InputAttribute(String attributeName, String inputName, int inputAction, String inputFormat) {
            this.attributeName = attributeName;
            this.inputName = inputName;
            this.inputAction = inputAction;
            this.inputFormat = inputFormat;
        }
    }

    public void save() {
        save(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
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
                if (! doc.getDocumentElement().getNodeName().equals(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT)) {
                    return;
                }
            } else {
                doc = db.newDocument();
                Node node = doc.createElement(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT);
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

            if (rootInputElement != null) {
                definitionElement.appendChild(createInputElementElement(rootInputElement, doc));
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
    
    private Element createInputElementElement(InputElement inputElement, Document doc) {
        Element inputElementElement = doc.createElement(INPUT_ELEMENT_ELEMENT);
        inputElementElement.setAttribute(NAME_ATTRIBUTE, inputElement.elementName);
        
        if (inputElement.logEntry) {
            inputElementElement.setAttribute(LOG_ENTRY_ATTRIBUTE, Boolean.toString(inputElement.logEntry));
        }

        if (inputElement.parentElement != null) {
            Element inputDataElement = doc.createElement(INPUT_DATA_ELEMENT);
            inputElementElement.appendChild(inputDataElement);
            inputDataElement.setAttribute(NAME_ATTRIBUTE, inputElement.inputName);
            inputDataElement.setAttribute(ACTION_ATTRIBUTE, Integer.toString(inputElement.inputAction));
            if (inputElement.inputFormat != null) {
                inputDataElement.setAttribute(FORMAT_ATTRIBUTE, inputElement.inputFormat);
            }
        }

        if (inputElement.attributes != null) {
            for (InputAttribute attribute : inputElement.attributes) {
                Element inputAttributeElement = doc.createElement(ATTRIBUTE_ELEMENT);
                inputElementElement.appendChild(inputAttributeElement);
                inputAttributeElement.setAttribute(NAME_ATTRIBUTE, attribute.attributeName);
                Element inputDataElement = doc.createElement(INPUT_DATA_ELEMENT);
                inputAttributeElement.appendChild(inputDataElement);
                inputDataElement.setAttribute(NAME_ATTRIBUTE, attribute.inputName);
                inputDataElement.setAttribute(ACTION_ATTRIBUTE, Integer.toString(attribute.inputAction));
                if (attribute.inputFormat != null) {
                    inputDataElement.setAttribute(FORMAT_ATTRIBUTE, attribute.inputFormat);
                }
            }
        }
        
        if (inputElement.childElements != null) {
            for (InputElement childInputElement : inputElement.childElements) {
                inputElementElement.appendChild(createInputElementElement(childInputElement, doc));
            }
        }
        
        return inputElementElement;
    }
    
    public static CustomXmlTraceDefinition[] loadAll() {
        return loadAll(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
    }
    
    public static CustomXmlTraceDefinition[] loadAll(String path) {
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
                return new CustomXmlTraceDefinition[0];
            }
            Document doc = db.parse(file);

            Element root = doc.getDocumentElement();
            if (! root.getNodeName().equals(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT)) {
                return new CustomXmlTraceDefinition[0];
            }

            ArrayList<CustomXmlTraceDefinition> defList = new ArrayList<CustomXmlTraceDefinition>();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element && node.getNodeName().equals(DEFINITION_ELEMENT)) {
                    CustomXmlTraceDefinition def = extractDefinition((Element) node);
                    if (def != null) {
                        defList.add(def);
                    }
                }
            }
            return defList.toArray(new CustomXmlTraceDefinition[0]);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CustomXmlTraceDefinition[0];
    }

    public static CustomXmlTraceDefinition load(String definitionName) {
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

            File file = new File(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
            Document doc = db.parse(file);

            Element root = doc.getDocumentElement();
            if (! root.getNodeName().equals(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT)) {
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
    
    public static CustomXmlTraceDefinition extractDefinition(Element definitionElement) {
        CustomXmlTraceDefinition def = new CustomXmlTraceDefinition();
        
        def.definitionName = definitionElement.getAttribute(NAME_ATTRIBUTE);
        if (def.definitionName == null) return null;
        
        NodeList nodeList = definitionElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals(TIME_STAMP_OUTPUT_FORMAT_ELEMENT)) {
                Element formatElement = (Element) node;
                def.timeStampOutputFormat = formatElement.getTextContent();
            } else if (nodeName.equals(INPUT_ELEMENT_ELEMENT)) {
                InputElement inputElement = extractInputElement((Element) node);
                if (inputElement != null) {
                    if (def.rootInputElement == null) {
                        def.rootInputElement = inputElement;
                    } else {
                        return null;
                    }
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

    private static InputElement extractInputElement(Element inputElementElement) {
        InputElement inputElement = new InputElement();
        inputElement.elementName = inputElementElement.getAttribute(NAME_ATTRIBUTE);
        inputElement.logEntry = (Boolean.toString(true).equals(inputElementElement.getAttribute(LOG_ENTRY_ATTRIBUTE))) ? true : false;
        NodeList nodeList = inputElementElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals(INPUT_DATA_ELEMENT)) {
                Element inputDataElement = (Element) node;
                inputElement.inputName = inputDataElement.getAttribute(NAME_ATTRIBUTE);
                inputElement.inputAction = Integer.parseInt(inputDataElement.getAttribute(ACTION_ATTRIBUTE));
                inputElement.inputFormat = inputDataElement.getAttribute(FORMAT_ATTRIBUTE);
            } else if (nodeName.equals(ATTRIBUTE_ELEMENT)) {
                Element attributeElement = (Element) node;
                InputAttribute attribute = new InputAttribute();
                attribute.attributeName = attributeElement.getAttribute(NAME_ATTRIBUTE);
                NodeList attributeNodeList = attributeElement.getChildNodes();
                for (int j = 0; j < attributeNodeList.getLength(); j++) {
                    Node attributeNode = attributeNodeList.item(j);
                    String attributeNodeName = attributeNode.getNodeName();
                    if (attributeNodeName.equals(INPUT_DATA_ELEMENT)) {
                        Element inputDataElement = (Element) attributeNode;
                        attribute.inputName = inputDataElement.getAttribute(NAME_ATTRIBUTE);
                        attribute.inputAction = Integer.parseInt(inputDataElement.getAttribute(ACTION_ATTRIBUTE));
                        attribute.inputFormat = inputDataElement.getAttribute(FORMAT_ATTRIBUTE);
                    }
                }
                inputElement.addAttribute(attribute);
            } else if (nodeName.equals(INPUT_ELEMENT_ELEMENT)) {
                Element childInputElementElement = (Element) node;
                InputElement childInputElement = extractInputElement(childInputElementElement);
                if (childInputElement != null) {
                    inputElement.addChild(childInputElement);
                }
            }
        }
        return inputElement;
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

            File file = new File(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
            Document doc = db.parse(file);

            Element root = doc.getDocumentElement();
            if (! root.getNodeName().equals(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT)) {
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
