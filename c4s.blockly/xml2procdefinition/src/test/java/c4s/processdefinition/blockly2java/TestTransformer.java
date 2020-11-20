package c4s.processdefinition.blockly2java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;

import https.developers_google_com.blockly.xml.ObjectFactory;
import https.developers_google_com.blockly.xml.Xml;
import passiveprocessengine.definition.WorkflowDefinition;

class TestTransformer {

	Xml2Java x2j = new Xml2Java();
	
	@Test
	void testParaStructures() throws IOException, JAXBException {
		String path = "src/test/resources/TestParaStructures.xml"; 
		Transformer t = new Transformer();
		String content = Files.readString(Paths.get(path));		
		Optional<Xml> optRoot = x2j.parse(content);
		optRoot.ifPresent(root -> {
			t.toProcessDefinition(root).stream().forEach(wfd -> { printWFD(wfd);
				// some non existing DNDs
				assert(!checkExactlyOneDndMatches(Set.of("Noop"), Set.of("Step2b"), wfd));
				// expected DNDs
				assert(checkExactlyOneDndMatches(Collections.emptySet(), Set.of("Step1"), wfd));
				assert(checkExactlyOneDndMatches(Set.of("Step1"), Set.of("NoOpStep0"), wfd));
				assert(checkExactlyOneDndMatches(Set.of("NoOpStep0"), Set.of("Step1b", "Step1c"), wfd));
				assert(checkExactlyOneDndMatches(Set.of("Step1b", "Step1c"), Set.of("Step1d"), wfd));
				assert(checkExactlyOneDndMatches(Set.of("Step1d"), Set.of("Step2"), wfd));
				assert(checkExactlyOneDndMatches(Set.of("Step2"), Set.of("NoOpStep1", "Step2b"), wfd));
				assert(checkExactlyOneDndMatches(Set.of("NoOpStep1"), Set.of("NoOpStep2") , wfd));
				assert(checkExactlyOneDndMatches(Set.of("NoOpStep2"), Set.of("NoOpStep3") , wfd));
				assert(checkExactlyOneDndMatches(Set.of("NoOpStep3"), Set.of("NoOpStep4") , wfd));
				assert(checkExactlyOneDndMatches(Set.of("Step2b", "NoOpStep4"), Set.of("NoOpStep5"), wfd));
				assert(checkExactlyOneDndMatches(Set.of("NoOpStep5"), Collections.emptySet(), wfd));
				assert(wfd.getDecisionNodeDefinitions().size() == 11);
			});
		});
	}
	
	
	
	private boolean checkExactlyOneDndMatches(Set<String> inbranches, Set<String> outbranches, WorkflowDefinition wfd) {
		Set<String> inb = inbranches.stream().map(b -> "inFrom-"+b).collect(Collectors.toSet());
		Set<String> outb = outbranches.stream().map(b -> "outTo-"+b).collect(Collectors.toSet());
		
		return wfd.getDecisionNodeDefinitions().stream()
			.filter(dnd -> dnd.getInBranches().stream()
								.map(br -> br.getName()).collect(Collectors.toSet())
								.equals(inb) )
			.filter(dnd -> dnd.getOutBranches().stream()
					.map(br -> br.getName()).collect(Collectors.toSet())
					.equals(outb) )
			.count() == 1;
	}
	
	private void printWFD(WorkflowDefinition wfd) {		
		System.out.println(wfd.toString());
		wfd.getWorkflowTaskDefinitions().stream().forEach(td -> System.out.println(td.toString()));
		wfd.getDecisionNodeDefinitions().stream().forEach(dnd -> System.out.println(dnd.toString()));
	}
	


}
