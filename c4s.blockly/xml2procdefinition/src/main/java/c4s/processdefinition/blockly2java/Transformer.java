package c4s.processdefinition.blockly2java;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import c4s.processdefinition.blockly2java.processors.BlockProcessors;
import c4s.processdefinition.blockly2java.processors.DataBlockProcessors;
import https.developers_google_com.blockly.xml.Xml;
import passiveprocessengine.definition.ITaskDefinition;
import passiveprocessengine.definition.TaskDefinition;
import passiveprocessengine.definition.WorkflowDefinition;

public class Transformer {
	
	private static Logger log = LogManager.getLogger(Transformer.class);
	private ProcessingContext ctx = new ProcessingContext();
	
	public Transformer() {
		ctx.addBlocksProcessor(new BlockProcessors.VarSetBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.FunctionsBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.ParallelBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.CheckpointBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.StepBlockProcessor(ctx));
		ctx.addBlocksProcessor(new DataBlockProcessors.StreamBlockProcessor(ctx));
		
		ctx.addStatementProcessor(new BlockProcessors.ParaBranchProcessor(ctx));
	}
		
	
	public List<WorkflowDefinition> toProcessDefinition(Xml root) {						
				
		root.getVariables().getVariable().forEach(var -> ctx.getVarIndex().put(var.getId(), BlockProcessors.UNKNOWNTYPE));				
		root.getBlock().stream()
			.forEach(block -> ctx.processBlock(block));			
			// all highlevel subroutines/procedures/functions etc
			//TODO: distinguish between processes and functions used in quality assurance constraints, etc			
		// only return workflows with at least one task.
		
		return ctx.getFlows().values().stream()
				.filter(wfd -> wfd.getWorkflowTaskDefinitions().size() > 0)
				.collect(Collectors.toList());	
	}
	
	public ProcessingContext getContext() {
		return ctx;
	}
	
	
}
