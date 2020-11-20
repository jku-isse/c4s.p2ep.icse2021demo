package c4s.processdefinition.blockly2java;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import c4s.processdefinition.blockly2java.processors.AbstractBlockProcessor;
import c4s.processdefinition.blockly2java.processors.AbstractStatementProcessor;
import c4s.processdefinition.blockly2java.processors.RuleGenerator;
import https.developers_google_com.blockly.xml.BlockType;
import https.developers_google_com.blockly.xml.Statement;
import passiveprocessengine.definition.ArtifactType;
import passiveprocessengine.definition.DecisionNodeDefinition;
import passiveprocessengine.definition.DefaultWorkflowDefinition;
import passiveprocessengine.definition.ITaskDefinition;
import passiveprocessengine.definition.MappingDefinition.Pair;
import passiveprocessengine.definition.WorkflowDefinition;

public class ProcessingContext {
	
	private static Logger log = LogManager.getLogger(ProcessingContext.class);
	private HashMap<String, WorkflowDefinition> flows = new HashMap<String, WorkflowDefinition>();	
	private Set<AbstractBlockProcessor> blockProcessors = new HashSet<>();
	private Set<AbstractStatementProcessor> stmtProcessors = new HashSet<>();
	
	private int counter = 0;		
	private DefaultWorkflowDefinition currentWFD = null;
	private LinkedList<DecisionNodeDefinition> closingDND = new LinkedList<>();
	private LinkedList<DecisionNodeDefinition> dndStack = new LinkedList<>();
	private LinkedList<Long> sequenceLengthHierarchy = new LinkedList<>(); //within a parallel/sequence
	
	private int noopCount = 0;
	private LinkedList<ITaskDefinition> taskHierarchy = new LinkedList<>();
	
	private HashMap<String, ArtifactType> varIndex = new HashMap<String, ArtifactType>(); // tracks which var is of which type			
	private HashMap<String, Pair<String,String>> varId2outputRole = new HashMap<>();
	
	private LinkedList<BlockType> checkPoints = new LinkedList<>();
	
	
	private RuleGenerator ruleGen = new RuleGenerator();
	
	
	
	private static BlockType dummyBlock = null;
	public static BlockType getDummyStepBlock() {
		if (dummyBlock == null) {
			dummyBlock = new BlockType();
			dummyBlock.setType("noopstep");
		}
		return dummyBlock;
	}
	
	public LinkedList<BlockType> getCheckPoints() {
		return checkPoints;
	}

	public void setCurrentWFD(DefaultWorkflowDefinition currentWFD) {
		this.currentWFD = currentWFD;
	}
	
	public DefaultWorkflowDefinition getCurrentWFD() {
		return currentWFD;
	}
	public LinkedList<DecisionNodeDefinition> getDndStack() {
		return dndStack;
	}

	public HashMap<String, ArtifactType> getVarIndex() {
		return varIndex;
	}
	
	public String produceId() {
		return ""+counter++;
	}
	
	public void addBlocksProcessor(AbstractBlockProcessor processor) {
		blockProcessors.add(processor);
	}
	
	public void addStatementProcessor(AbstractStatementProcessor processor) {
		stmtProcessors.add(processor);
	}
	
	public void processBlock(BlockType block) {
		long count = blockProcessors.stream()
			.filter(processor -> processor.getSupportedTypes().contains(block.getType()))
			.map(processor -> { processor.processBlock(block); return block; })
			.count();
		if (count <= 0) {
			log.debug(String.format("No processor applied to block: %s %s ", block.getType(), block.getId()));
		}
	}
	
	public void processStatement(Statement stmt) {
		long count = stmtProcessors.stream()
			.filter(processor -> processor.willAccept(stmt))
			.map(processor -> { processor.processStatement(stmt); return stmt; })
			.count();
		if (count <= 0) {
			log.debug(String.format("No processor applied to statement: %s ", stmt.getName()));
		}
	}

	public HashMap<String, WorkflowDefinition> getFlows() {
		return flows;
	}

	public LinkedList<DecisionNodeDefinition> getClosingDND() {
		return closingDND;
	}

//	public void setCurrentClosingDND(DecisionNodeDefinition currentClosingDND) {
//		this.currentClosingDND = currentClosingDND;
//	}

	public LinkedList<ITaskDefinition> getTaskHierarchy() {
		return taskHierarchy;
	}

	public LinkedList<Long> getSequenceLengthHierarchy() {
		return sequenceLengthHierarchy;
	}

	public int getAndIncrNoopCounter() {
		return noopCount++;
	}

	public HashMap<String, Pair<String,String>> getVarId2outputRole() {
		return varId2outputRole;
	}
	
	public Optional<Pair<String,String>> getOutputRoleFor(String varId) {
		return Optional.ofNullable(varId2outputRole.get(varId));
	}

	public RuleGenerator getRuleGen() {
		return ruleGen;
	}
}
