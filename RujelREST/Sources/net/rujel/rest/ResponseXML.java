package net.rujel.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableSet;

import net.rujel.reusables.xml.AbstractObjectReader;
import net.rujel.reusables.xml.TransormationErrorListener;

public class ResponseXML extends AbstractObjectReader{

	public static byte[] generate(ReportSource input) throws TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setErrorListener(new TransormationErrorListener(null));
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty("indent", "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
		XMLReader reader = new ResponseXML();
        Source src = new SAXSource(reader,input);
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	Result res = new StreamResult(out);
      	transformer.transform(src, res);
    	return out.toByteArray();
	}
	
	@Override
	public void parse(InputSource input) throws IOException, SAXException {
        if (input instanceof ReportSource) {
            parse((ReportSource)input);
        } else {
            throw new SAXException("Unsupported InputSource specified. "
                    + "Must be a ReportSource");
        }
	}

	public void parse(ReportSource in) throws IOException, SAXException {
	      if (handler == null) {
	            throw new IllegalStateException("ContentHandler not set");
	        }
	        handler.startDocument();
	        handler.prepareAttribute("entity", in.entity);
	        handler.startElement("response");
	        if(in.attributes != null && in.attributes.count() > 0) {
	        	Enumeration enu = in.attributes.keyEnumerator();
	        	while (enu.hasMoreElements()) {
					String att = (String) enu.nextElement();
					handler.prepareAttribute("attribute", att);
					handler.element("param", in.attributes.valueForKey(att).toString());
				}
	        }
	        if(in.rows != null && in.rows.count() > 0) {
	        	if(in.groupings != null) {
	        		EOSortOrdering[] so = new EOSortOrdering[in.groupings.length];
	        		for (int i = 0; i < so.length; i++) {
						so[i] = new EOSortOrdering(in.groupings[i], 
								EOSortOrdering.CompareAscending);
					}
	        		NSArray sorter = new NSArray(so);
	        		in.rows = EOSortOrdering.sortedArrayUsingKeyOrderArray(in.rows, sorter);
	        	}
	        	Enumeration enu = in.rows.objectEnumerator();
	        	while (enu.hasMoreElements()) {
					NSDictionary row = (NSDictionary) enu.nextElement();
					if(in.level != null)
						handler.prepareAttribute("stage", in.level.toString());
					handler.startElement("grouping");
					if(in.groupings != null) {
						for (int i = 0; i < in.groupings.length; i++) {
							String key = in.groupings[i];
							Object value = row.valueForKey(key);
							if(value == null)
								continue;
							handler.prepareAttribute("name", key);
							handler.element("attribute", value.toString());
						}
					}
					if(in.agregates != null) {
						for (int i = 0; i < in.agregates.length; i++) {
							String key = in.agregates[i];
							Agregator value = (Agregator)row.valueForKey(key);
							if(value == null)
								continue;
							handler.prepareAttribute("name", key);
							handler.prepareAttribute("type",value.getType());
							if(value.getAttribute() != null)
								handler.prepareAttribute("attribute",value.getAttribute());
							handler.element("agregate", value.toString());
						}
					}
					if(in.lists != null) {
						for (int i = 0; i < in.lists.length; i++) {
							String key = in.lists[i];
							NSMutableSet list = (NSMutableSet)row.valueForKey(key);
							if(list == null || list.count() == 0)
								continue;
							handler.prepareAttribute("attribute", key);
							handler.startElement("list");
							Enumeration lenu = list.objectEnumerator();
							while (lenu.hasMoreElements()) {
								Object val = (Object) lenu.nextElement();
								if(val instanceof AgrEntity.Wrapper) {
									((AgrEntity.Wrapper)val).parce(handler);
								} else {
									handler.element("value", val.toString());
								}
							}
							handler.endElement("list");
						}
					}
					handler.endElement("grouping");
				} // rows enumeration
	        }// rows != null
	        handler.endElement("response");
	        handler.endDocument();
	}
}
