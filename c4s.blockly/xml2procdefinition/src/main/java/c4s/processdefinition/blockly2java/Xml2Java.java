package c4s.processdefinition.blockly2java;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import https.developers_google_com.blockly.xml.Xml;


public class Xml2Java {

	public Optional<Xml> parse(String xmlString) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Xml.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();				
		StringReader reader = new StringReader(xmlString);
		Xml root = (Xml) unmarshaller.unmarshal(reader);
		return Optional.ofNullable(root);
	}


	public String toXml(Xml root) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Xml.class);
		Marshaller m = jaxbContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		m.marshal(root, sw);

		String result = sw.toString();
		return result;
	}
	//	 private static class XsiTypeReader extends StreamReaderDelegate {
	//
	//	        public XsiTypeReader(XMLStreamReader reader) {
	//	            super(reader);
	//	        }
	//
	//	        @Override
	//	        public String getAttributeNamespace(int arg0) {
	//	            if("type".equals(getAttributeLocalName(arg0))) {
	//	                return XMLConstants.SCHEMA_INSTANCE_URL;
	//	            }
	//	            return super.getAttributeNamespace(arg0);
	//	        }
	//
	//	    }
}
