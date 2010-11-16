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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfEventType;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTraceDefinition.InputAttribute;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomXmlTraceDefinition.InputElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CustomXmlTrace extends TmfTrace<CustomXmlEvent> {

    private CustomXmlTraceDefinition fDefinition;
    private InputElement fRecordInputElement;
    
    public CustomXmlTrace(String name, CustomXmlTraceDefinition definition, String path, int cacheSize) throws FileNotFoundException {
        super(name, CustomXmlEvent.class, path, cacheSize);
        fDefinition = definition;
        fRecordInputElement = getRecordInputElement(fDefinition.rootInputElement);
    }

    @Override
    public TmfContext seekLocation(ITmfLocation<?> location) {
        //System.out.println(Thread.currentThread().getName() + "::" + getName() + " seekLocation(" + ((location == null || location.getLocation() == null) ? "null" : location) + ")");
        //new Throwable().printStackTrace();
        CustomXmlTraceContext context = new CustomXmlTraceContext(new TmfLocation<Long>((Long)null), ITmfContext.INITIAL_RANK);
        if (!new File(getPath()).isFile()) {
            return context;
        }
        try {
            context.raFile = new RandomAccessFile(getPath(), "r"); //$NON-NLS-1$
            if (location != null && location.getLocation() instanceof Long) {
                context.raFile.seek((Long)location.getLocation());
            }
            
            String line;
            String recordElementStart = "<" + fRecordInputElement.elementName; //$NON-NLS-1$
            long rawPos = context.raFile.getFilePointer();
            
            while ((line = context.raFile.readLine()) != null) {
                int idx = line.indexOf(recordElementStart); 
                if (idx != -1) {
                    context.setLocation(new TmfLocation<Long>(rawPos + idx));
                    return context;
                }
                rawPos = context.raFile.getFilePointer();
            }
            return context;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return context;
        } catch (IOException e) {
            e.printStackTrace();
            return context;
        }
        
    }

    @Override
	public ITmfTrace createTraceCopy() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public ITmfLocation<?> getCurrentLocation() {
        return new TmfLocation<Object>(null);
    }

    @Override
    public synchronized TmfEvent getNextEvent(TmfContext context) {
        ITmfContext savedContext = context.clone();
        TmfEvent event = parseEvent(context);
        if (event != null) {
            updateIndex(savedContext, savedContext.getRank(), event.getTimestamp());
            context.updateRank(1);
        }
        return event;
    }

    @Override
    public TmfEvent parseEvent(TmfContext tmfContext) {
        //System.out.println(Thread.currentThread().getName() + ":: " + getName() + " parseEvent(" + tmfContext.getRank() + " @ " + (tmfContext.getLocation().getLocation() == null ? "null" : tmfContext.getLocation()));
        if (!(tmfContext instanceof CustomXmlTraceContext)) {
            return null;
        }
        
        CustomXmlTraceContext context = (CustomXmlTraceContext) tmfContext;
        if (!(context.getLocation().getLocation() instanceof Long)) {
            return null;
        }

        synchronized (context.raFile) {
            CustomXmlEvent event = null;
            try {
                if (context.raFile.getFilePointer() != (Long)context.getLocation().getLocation() + 1) {
                    context.raFile.seek((Long)context.getLocation().getLocation() + 1); // +1 is for the <
                }
                StringBuffer elementBuffer = new StringBuffer("<"); //$NON-NLS-1$
                readElement(elementBuffer, context.raFile);
                Element element = parseElementBuffer(elementBuffer);
                
                event = extractEvent(element, fRecordInputElement);
                
                String line;
                String recordElementStart = "<" + fRecordInputElement.elementName; //$NON-NLS-1$
                long rawPos = context.raFile.getFilePointer();
                
                while ((line = context.raFile.readLine()) != null) {
                    int idx = line.indexOf(recordElementStart); 
                    if (idx != -1) {
                        context.setLocation(new TmfLocation<Long>(rawPos + idx));
                        return event;
                    }
                    rawPos = context.raFile.getFilePointer();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            context.setLocation(new TmfLocation<Long>((Long)null));
            return event;
        }
    }

    private Element parseElementBuffer(StringBuffer elementBuffer) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            EntityResolver resolver = new EntityResolver () {
                @Override
				public InputSource resolveEntity (String publicId, String systemId) {
                    String empty = ""; //$NON-NLS-1$
                    ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
                    return new InputSource(bais);
                }
            };
            db.setEntityResolver(resolver);

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler(){
                @Override
				public void error(SAXParseException saxparseexception) throws SAXException {}
                @Override
				public void warning(SAXParseException saxparseexception) throws SAXException {}
                @Override
				public void fatalError(SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }});
            
            Document doc = db.parse(new ByteArrayInputStream(elementBuffer.toString().getBytes()));
            return doc.getDocumentElement();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readElement(StringBuffer buffer, RandomAccessFile raFile) {
        try {
            int numRead = 0;
            boolean startTagClosed = false;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                char c = (char)i;
                buffer.append(c);
                if (c == '"') {
                    readQuote(buffer, raFile, '"');
                } else if (c == '\'') {
                    readQuote(buffer, raFile, '\'');
                } else if (c == '<') {
                    readElement(buffer, raFile);
                } else if (c == '/' && numRead == 1) {
                    break; // found "</"
                } else if (c == '-' && numRead == 3 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("!-")) { //$NON-NLS-1$
                    readComment(buffer, raFile); // found "<!--"
                } else if (i == '>') {
                    if (buffer.charAt(buffer.length() - 2) == '/') {
                        break; // found "/>"
                    } else if (startTagClosed) {
                        break; // found "<...>...</...>"
                    } else {
                        startTagClosed = true; // found "<...>"
                    }
                }
            }
            return;
        } catch (IOException e) {
            return;
        }
    }

    private void readQuote(StringBuffer buffer, RandomAccessFile raFile, char eq) {
        try {
            int i;
            while ((i = raFile.read()) != -1) {
                char c = (char)i;
                buffer.append(c);
                if (c == eq) {
                    break; // found matching end-quote
                }
            }
            return;
        } catch (IOException e) {
            return;
        }
    }

    private void readComment(StringBuffer buffer, RandomAccessFile raFile) {
        try {
            int numRead = 0;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                char c = (char)i;
                buffer.append(c);
                if (c == '>' && numRead >= 2 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("--")) { //$NON-NLS-1$
                    break; // found "-->"
                }
            }
            return;
        } catch (IOException e) {
            return;
        }
    }

    public static StringBuffer parseElement(Element parentElement, StringBuffer buffer) {
        NodeList nodeList = parentElement.getChildNodes();
        String separator = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (separator == null) {
                    separator = " | "; //$NON-NLS-1$
                } else {
                    buffer.append(separator);
                }
                Element element = (Element) node;
                if (element.hasChildNodes() == false) {
                    buffer.append(element.getNodeName());
                } else if (element.getChildNodes().getLength() == 1 && element.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    buffer.append(element.getNodeName() + ":" + element.getFirstChild().getNodeValue().trim()); //$NON-NLS-1$
                } else {
                    buffer.append(element.getNodeName());
                    buffer.append(" [ "); //$NON-NLS-1$
                    parseElement(element, buffer);
                    buffer.append(" ]"); //$NON-NLS-1$
                }
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                if (node.getNodeValue().trim().length() != 0) {
                    buffer.append(node.getNodeValue().trim());
                }
            }
        }
        return buffer;
    }

    public InputElement getRecordInputElement(InputElement inputElement) {
        if (inputElement.logEntry) {
            return inputElement;
        } else if (inputElement.childElements != null) {
            for (InputElement childInputElement : inputElement.childElements) {
                InputElement recordInputElement = getRecordInputElement(childInputElement);
                if (recordInputElement != null) {
                    return recordInputElement;
                }
            }
        }
        return null;
    }
    
    public CustomXmlEvent extractEvent(Element element, InputElement inputElement) {
        CustomXmlEvent event = new CustomXmlEvent(fDefinition, TmfTimestamp.Zero, new TmfEventSource(""), new TmfEventType(fDefinition.definitionName, new String[0]), new TmfEventReference("")); //$NON-NLS-1$ //$NON-NLS-2$
        parseElement(element, event, inputElement);
        return event;
    }
    
    private void parseElement(Element element, CustomXmlEvent event, InputElement inputElement) {
        if (inputElement.inputName != null && !inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_IGNORE)) {
            event.parseInput(parseElement(element, new StringBuffer()).toString(), inputElement.inputName, inputElement.inputAction, inputElement.inputFormat);
        }
        if (inputElement.attributes != null) {
            for (InputAttribute attribute : inputElement.attributes) {
                event.parseInput(element.getAttribute(attribute.attributeName), attribute.inputName, attribute.inputAction, attribute.inputFormat);
            }
        }
        NodeList childNodes = element.getChildNodes();
        if (inputElement.childElements != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    for (InputElement child : inputElement.childElements) {
                        if (node.getNodeName().equals(child.elementName)) {
                            parseElement((Element) node, event, child);
                            break;
                        }
                    }
                }
            }
        }
        return;
    }

    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }
}
