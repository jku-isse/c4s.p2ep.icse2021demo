package c4s.processdefinition.blockly2java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import https.developers_google_com.blockly.xml.Xml;
import passiveprocessengine.persistance.json.DefinitionSerializer;

public class ConverterMain {

	public static void main(String[] args) {
		if (args.length == 2) {
			String inpath = args[0];
			String outpath = args[1];
		try {
			transform(inpath, outpath);
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}
		} else {
			System.out.println("2 Parameters needed: inputfileInclPath and outputFolder without trailing /");
			System.exit(1); }
	}

	public static void transform(String inputFileInclPath, String outputFolderInclPath) throws JAXBException, IOException {
		Xml2Java x2j = new Xml2Java();
		Transformer t = new Transformer();
		String content = Files.readString(Paths.get(inputFileInclPath));	
		Optional<Xml> optRoot = x2j.parse(content);
		optRoot.ifPresent(root -> {
			t.toProcessDefinition(root).stream().forEach(wfd -> { 
				DefinitionSerializer dser = new DefinitionSerializer();
				String defString = dser.toJson(wfd);
				try {
					Files.write(Paths.get(outputFolderInclPath+"/"+wfd.getId()+".json"), defString.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				});
		});
	}
	
}
