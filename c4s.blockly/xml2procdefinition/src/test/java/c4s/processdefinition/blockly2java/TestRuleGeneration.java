package c4s.processdefinition.blockly2java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;

import https.developers_google_com.blockly.xml.Xml;
import passiveprocessengine.definition.WorkflowDefinition;

class TestRuleGeneration {

Xml2Java x2j = new Xml2Java();
	
	@Test
	void testRuleGen() throws IOException, JAXBException {
		// TODO: fix steps within function
		// TODO: test pre/post conditions between blocks
		String path = "src/test/resources/TestRuleGen.xml"; 
		Transformer t = new Transformer();
		String content = Files.readString(Paths.get(path));		
		Optional<Xml> optRoot = x2j.parse(content);
		optRoot.ifPresent(root -> {
			t.toProcessDefinition(root).stream().forEach(wfd -> { 
				printWFD(wfd);
				printDataMappings(wfd);
				printRuleFile(t.getContext());
				});
		});
		
	}
	
	private void printWFD(WorkflowDefinition wfd) {		
		System.out.println(wfd.toString());
		wfd.getWorkflowTaskDefinitions().stream().forEach(td -> System.out.println(td.toString()));
		wfd.getDecisionNodeDefinitions().stream().forEach(dnd -> System.out.println(dnd.toString()));
	}

	private void printDataMappings(WorkflowDefinition wfd) {
		wfd.getDecisionNodeDefinitions().stream()
			.flatMap(dnd -> dnd.getMappings().stream())
			.forEach(mapping -> System.out.println(mapping.getFrom() + " --> "+mapping.getTo()) );
	}
	
	private void printRuleFile(ProcessingContext ctx) {
		System.out.println(ctx.getRuleGen().getRuleFileAsBuffer().toString());
	}
}
