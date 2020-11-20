package c4s.processdefinition.blockly2java.processors;

import c4s.processdefinition.blockly2java.ProcessingContext;
import https.developers_google_com.blockly.xml.Statement;

public abstract class AbstractStatementProcessor {

	protected ProcessingContext ctx = null; 
	
	public abstract boolean willAccept(Statement stmt);
	
	
	public AbstractStatementProcessor(ProcessingContext ctx) {
		this.ctx = ctx;
	}
	
	public abstract void processStatement(Statement stmt);
}
