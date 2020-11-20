package c4s.processdefinition.blockly2java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;

import https.developers_google_com.blockly.xml.ObjectFactory;
import https.developers_google_com.blockly.xml.Xml;
import passiveprocessengine.definition.WorkflowDefinition;

class TestGenerator {

	Xml2Java x2j = new Xml2Java();
	
	@Test
	void test() throws IOException, JAXBException {
		String path = "src/test/resources/Testinput.xml"; 
		
		String content = Files.readString(Paths.get(path));		
		Optional<Xml> optRoot = x2j.parse(content);
		optRoot.ifPresent(root -> {
			root.getVariables().getVariable().stream().forEach(var -> System.out.println(var.getValue()));
		});
		
	}
	
	@Test
	void testTransform() throws IOException, JAXBException {
		// TODO: fix steps within function
		// TODO: fix input to process within function with multiple processes
		// TODO: check nested parallel blocks
		// TODO: test pre/post conditions between blocks
		String path = "src/test/resources/Testinput3.xml"; 
		Transformer t = new Transformer();
		String content = Files.readString(Paths.get(path));		
		Optional<Xml> optRoot = x2j.parse(content);
		optRoot.ifPresent(root -> {
			t.toProcessDefinition(root).stream().forEach(wfd -> printWFD(wfd));
		});
		
	}
	
	private void printWFD(WorkflowDefinition wfd) {		
		System.out.println(wfd.toString());
		wfd.getWorkflowTaskDefinitions().stream().forEach(td -> System.out.println(td.toString()));
		wfd.getDecisionNodeDefinitions().stream().forEach(dnd -> System.out.println(dnd.toString()));
	}
	
	@Test
	void testMarshall() throws JAXBException {
		Xml root = new ObjectFactory().createXml();
		System.out.println(x2j.toXml(root));
		//root.getVariables().getVariable().add(e)
	}

}
