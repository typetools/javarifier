// SAXTest.java - test application for SAX2

package org.htmlparser.tests;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Test class for SAX2.
 */
public class SAXTest implements ContentHandler, ErrorHandler
{

    ////////////////////////////////////////////////////////////////////
    // Main app.
    ////////////////////////////////////////////////////////////////////


    /**
     * Main application entry point.
     */
    public static void main (String args[])
    {
	System.out.println("************************************" +
			   "************************************");
	System.out.println("* Testing SAX2");
	System.out.println("************************************" +
			   "************************************");
	System.out.print("\n");

	//
	// Figure out the XML reader
	//

//	String driverName =
//	    System.getProperty("org.xml.sax.driver",
//			       "org.apache.xerces.parsers.SAXParser");
        String driverName = "org.htmlparser.sax.XMLReader";
	System.out.println("SAX driver class: " +
			   driverName +
			   "\n  (you can specify a different one using the " +
			   "org.xml.sax.driver property)");
	System.out.print("\n");


	//
	// Create the XML reader
	//

	System.out.println("Now, we'll try to create an instance of the " +
			   "driver, using XMLReaderFactory");
	XMLReader reader = null;
	try {
	    reader = XMLReaderFactory.createXMLReader(driverName);
	} catch (SAXException e) {
	    System.out.println("Failed to create XMLReader: " +
			       e.getMessage() +
			       "\nMake sure that the class actually " +
			       "exists and is present on your CLASSPATH" +
			       "\nor specify a different class using the " +
			       "org.xml.sax.driver property");
	    System.exit(1);
	}
	System.out.println("XMLReader created successfully\n");


	//
	// Check features.
	//
	System.out.println("Checking defaults for some well-known features:");
	checkFeature(reader, "http://xml.org/sax/features/namespaces");
	checkFeature(reader, "http://xml.org/sax/features/namespace-prefixes");
	checkFeature(reader, "http://xml.org/sax/features/string-interning");
	checkFeature(reader, "http://xml.org/sax/features/validation");
	checkFeature(reader,
		     "http://xml.org/sax/features/external-general-entities");
	checkFeature(reader,
		     "http://xml.org/sax/features/external-parameter-entities");
	System.out.print("\n");

	
	//
	// Assign handlers.
	//
	System.out.println("Creating and assigning handlers\n");
	SAXTest handler = new SAXTest();
	reader.setContentHandler(handler);
	reader.setErrorHandler(handler);

	//
	// Parse documents.
	//
	if (args.length > 0) {
	    for (int i = 0; i < args.length; i++) {
		String systemId = makeAbsoluteURL(args[i]);
		System.out.println("Trying file " + systemId);
		try {
		    reader.parse(systemId);
		} catch (SAXException e1) {
		    System.out.println(systemId +
				       " failed with XML error: " +
				       e1.getMessage());
		} catch (IOException e2) {
		    System.out.println(systemId +
				       " failed with I/O error: " +
				       e2.getMessage());
		}
		System.out.print("\n");
	    }
	} else {
	    System.out.println("No documents supplied on command line; " +
			       "parsing skipped.");
	}


	//
	// Done.
	//
	System.out.println("SAX2 test finished.");
    }


    /**
     * Check and display the value of a feature.
     */
    private static void checkFeature (XMLReader reader, String name)
    {
	try {
	    System.out.println("  " +
			       name +
			       " = " +
			       reader.getFeature(name));
	} catch (SAXNotRecognizedException e) {
	    System.out.println("XMLReader does not recognize feature " +
			       name);
	} catch (SAXNotSupportedException e) {
	    System.out.println("XMLReader recognizes feature " +
			       name +
			       " but does not support checking its value");
	}
    }


    /**
     * Construct an absolute URL if necessary.
     *
     * This method is useful for relative file paths on a command
     * line; it converts them to absolute file: URLs, using the
     * correct path separator.  This method is based on an
     * original suggestion by James Clark.
     *
     * @param url The (possibly relative) URL.
     * @return An absolute URL of some sort.
     */
    private static String makeAbsoluteURL (String url)
    {
	URL baseURL;
	
	String currentDirectory = System.getProperty("user.dir");
	String fileSep = System.getProperty("file.separator");
	String file = currentDirectory.replace(fileSep.charAt(0), '/') + '/';
	
	if (file.charAt(0) != '/') {
	    file = "/" + file;
	}

	try {
	    baseURL = new URL("file", null, file);
	    return new URL(baseURL, url).toString();
	} catch (MalformedURLException e) {
	    System.err.println(url + ": " + e.getMessage());
	    return url;
	}
    }

    private static String makeNSName (String uri, String localName,
				      String qName)
    {
	if (uri.equals("")) 
	    uri = "[none]";
	if (localName.equals(""))
	    localName = "[none]";
	if (qName.equals(""))
	    qName = "[none]";
	return uri + '/' + localName + '/' + qName;
    }

    private static String escapeData (char ch[], int start, int length)
    {
	StringBuffer buf = new StringBuffer();
	for (int i = start; i < start + length; i++) {
	    switch(ch[i]) {
	    case '\n':
		buf.append("\\n");
		break;
	    case '\t':
		buf.append("\\t");
		break;
	    case '\r':
		buf.append("\\r");
		break;
	    default:
		buf.append(ch[i]);
		break;
	    }
	}
	return buf.toString();
    }


    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.ContentHandler.
    ////////////////////////////////////////////////////////////////////

    public void setDocumentLocator (Locator locator)
    {
	System.out.println("  EVENT: setDocumentLocator");
    }

    public void startDocument ()
	throws SAXException
    {
	System.out.println("  EVENT: startDocument");
    }

    public void endDocument ()
	throws SAXException
    {
	System.out.println("  EVENT: endDocument");
    }

    public void startPrefixMapping (String prefix, String uri)
	throws SAXException
    {
	System.out.println("  EVENT: startPrefixMapping " +
			   prefix + " = " + uri);
    }

    public void endPrefixMapping (String prefix)
	throws SAXException
    {
	System.out.println("  EVENT: endPrefixMapping " + prefix);
    }

    public void startElement (String namespaceURI, String localName,
			      String qName, Attributes atts)
	throws SAXException
    {
	System.out.println("  EVENT: startElement " +
			   makeNSName(namespaceURI, localName, qName));
	int attLen = atts.getLength();
	for (int i = 0; i < attLen; i++) {
	    char ch[] = atts.getValue(i).toCharArray();
	    System.out.println("    Attribute " +
			       makeNSName(atts.getURI(i),
					  atts.getLocalName(i),
					  atts.getQName(i)) +
			       '=' +
			       escapeData(ch, 0, ch.length));
	}
    }

    public void endElement (String namespaceURI, String localName,
			    String qName)
	throws SAXException
    {
	System.out.println("  EVENT: endElement " +
			   makeNSName(namespaceURI, localName, qName));
    }

    public void characters (char ch[], int start, int length)
	throws SAXException
    {
	System.out.println("  EVENT: characters " +
			   escapeData(ch, start, length));
    }

    public void ignorableWhitespace (char ch[], int start, int length)
	throws SAXException
    {
	System.out.println("  EVENT: ignorableWhitespace " +
			   escapeData(ch, start, length));
    }

    public void processingInstruction (String target, String data)
	throws SAXException
    {
	System.out.println("  EVENT: processingInstruction " +
			   target + ' ' + data);
    }

    public void skippedEntity (String name)
	throws SAXException
    {
	System.out.println("  EVENT: skippedEntity " + name);
    }


    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.ErrorHandler.
    ////////////////////////////////////////////////////////////////////

    public void warning (SAXParseException e)
	throws SAXException
    {
	System.out.println("  EVENT: warning " +
			   e.getMessage() + ' ' +
			   e.getSystemId() + ' ' +
			   e.getLineNumber() + ' ' +
			   e.getColumnNumber());
    }

    public void error (SAXParseException e)
	throws SAXException
    {
	System.out.println("  EVENT: error " +
			   e.getMessage() + ' ' +
			   e.getSystemId() + ' ' +
			   e.getLineNumber() + ' ' +
			   e.getColumnNumber());
    }

    public void fatalError (SAXParseException e)
	throws SAXException
    {
	System.out.println("  EVENT: fatal error " +
			   e.getMessage() + ' ' +
			   e.getSystemId() + ' ' +
			   e.getLineNumber() + ' ' +
			   e.getColumnNumber());
    }

}

// end of SAXTest.java
