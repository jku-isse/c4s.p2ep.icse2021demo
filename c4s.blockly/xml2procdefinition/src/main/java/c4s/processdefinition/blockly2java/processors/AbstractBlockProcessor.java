package c4s.processdefinition.blockly2java.processors;

import java.util.HashSet;
import java.util.Set;

import c4s.processdefinition.blockly2java.ProcessingContext;
import https.developers_google_com.blockly.xml.BlockType;

public abstract class AbstractBlockProcessor {

	private Set<String> supportedTypes = new HashSet<String>();
	protected ProcessingContext ctx = null; 
	
	public Set<String> getSupportedTypes() {
		return Set.copyOf(supportedTypes);		
	}
	
	public AbstractBlockProcessor(Set<String> supportedTypes, ProcessingContext ctx) {
		this.supportedTypes = supportedTypes;
		this.ctx = ctx;
	}
	
	public AbstractBlockProcessor(String supportedType, ProcessingContext ctx) {
		this.supportedTypes.add(supportedType);
		this.ctx = ctx;
	}
	
	public abstract void processBlock(BlockType block);
}
