package c4s.processdefinition.blockly2java;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Driver;
import com.sun.tools.xjc.XJCListener;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Generator {

    public static void main(String[] args) throws BadCommandLineException, IOException {
        final String targetDir = "src/main/java";
        Path path = Paths.get(targetDir);
        if(!Files.exists(path)) {
            Files.createDirectories(path);
        }
        Driver.run(new String[]{"-d", targetDir,
                "blockly.xsd"}, new XJCListener() {

            @Override
            public void error(SAXParseException e) {
                printError(e, "ERROR");
            }

            @Override
            public void fatalError(SAXParseException e) {
                printError(e, "FATAL");
            }

            @Override
            public void warning(SAXParseException e) {
                printError(e, "WARN");
            }

            @Override
            public void info(SAXParseException e) {
                printError(e, "INFO");
            }

            private void printError(SAXParseException e, String level) {
                System.err.printf("%s: SAX Parse exception", level);
                e.printStackTrace();
            }
        });
    }
}