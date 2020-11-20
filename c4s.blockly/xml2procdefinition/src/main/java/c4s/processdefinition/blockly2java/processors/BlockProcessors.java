package c4s.processdefinition.blockly2java.processors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jackson.ListOfMapEntrySerializer;
import org.w3c.dom.Element;

import c4s.processdefinition.blockly2java.ProcessingContext;
import https.developers_google_com.blockly.xml.BlockType;
import https.developers_google_com.blockly.xml.Statement;
import passiveprocessengine.definition.ArtifactType;
import passiveprocessengine.definition.DecisionNodeDefinition;
import passiveprocessengine.definition.DecisionNodeDefinition.InFlowType;
import passiveprocessengine.definition.DefaultBranchDefinition;
import passiveprocessengine.definition.DefaultWorkflowDefinition;
import passiveprocessengine.definition.MappingDefinition.Pair;
import passiveprocessengine.definition.NoOpTaskDefinition;
import passiveprocessengine.definition.TaskDefinition;

public class BlockProcessors {

	private static Logger log = LogManager.getLogger(BlockProcessors.class);

	public static ArtifactType UNKNOWNTYPE = new ArtifactType("Unknown");

	public static class VarSetBlockProcessor extends AbstractBlockProcessor {

		public final static String VARIABLES_SET = "variables_set";

		public VarSetBlockProcessor(ProcessingContext ctx) {
			super(VARIABLES_SET, ctx);
		}

		@Override
		public void processBlock(BlockType block) {
			Utils.getField(block.getField(), "VAR").ifPresent(
					field -> { String varId = field.getId(); 
					Utils.getFirstValue(block.getStatementOrValue()).ifPresent(
							value -> DataBlockProcessors.getArtifactTypeFromBlock(value.getBlock(), ctx).ifPresent( 
									type -> { 									
										// we set the variable type (i.e., artifact type)
										ctx.getVarIndex().replace(varId, type);
										// now  we also add this variable definition/setting to parent step only if no previous value set or of unknonw type
										// TODO: commented out for now, as this adds any variables defined anywhere to process input which is not intended
//										Optional.ofNullable(ctx.getTaskHierarchy().peek()).ifPresent(
//												td -> { 																
//													if (td.getExpectedInput().containsKey(field.getValue())) {												
//														td.getExpectedInput().replace(field.getValue(), UNKNOWNTYPE, type);
//													} else {
//														td.getExpectedInput().put(field.getValue(), type);
//													}
//												} );													
									} ) ); 
					} );				
		}						
	}



	public static class FunctionsBlockProcessor extends AbstractBlockProcessor {

		public final static String[] PROCEDURETYPES = new String[]{"procedures_defnoreturn", "procedures_defreturn"};

		public FunctionsBlockProcessor(ProcessingContext ctx) {
			super(Arrays.asList(PROCEDURETYPES).parallelStream().collect(Collectors.toSet()), ctx);
		}

		@Override
		public void processBlock(BlockType block) {						
			String funcName = Utils.getFieldValue(block.getField(), "NAME").orElse("Anonym");
			log.info("Processing: "+funcName);						
			DefaultWorkflowDefinition wfd = new DefaultWorkflowDefinition(funcName);
			ctx.getTaskHierarchy().push(wfd);
			ctx.setCurrentWFD(wfd);
			// TODO:			wfd.getExpectedInput().add();
			ctx.getFlows().put(funcName, wfd);
			DecisionNodeDefinition dnd = new DecisionNodeDefinition(ctx.produceId(), wfd);				
			dnd.setInBranchingType(InFlowType.AND);
			wfd.getDecisionNodeDefinitions().add(dnd);
			ctx.getDndStack().push(dnd);
			// prepare Closing DND			
			DecisionNodeDefinition closingDND = new DecisionNodeDefinition(ctx.produceId(), ctx.getCurrentWFD());
			closingDND.setOutBranchingType(InFlowType.AND);			
			wfd.getDecisionNodeDefinitions().add(closingDND);
			ctx.getClosingDND().push(closingDND);			


			Optional<Statement> opStatement = Utils.getStatement(block.getStatementOrValue());
			opStatement.ifPresent(stmt -> {
				//List<BlockType> blocks = Utils.flattenByNext(stmt.getBlock());
				//blocks.stream()
				//.forEach(block2 -> ctx.processBlock(block2));
				ctx.processStatement(stmt);
			});		
			//clean up for next process/function
			ctx.setCurrentWFD(null);
			ctx.getClosingDND().clear();
			ctx.getDndStack().clear();
			ctx.getTaskHierarchy().clear();
			ctx.getCheckPoints().clear();
			// DOES NOT WORK!!! --> we need a assignment with ArtifactType anyway to define the variable type
			//			Object mutation = block.getMutation();
			//			if (mutation != null && mutation instanceof Element) { // we have input parameters, process input parameters 
			//				Element mutEl = (Element)mutation;
			//				System.out.println(mutEl.getChildNodes());
			//			}			
		}						
	}


	public static class ParallelBlockProcessor extends AbstractBlockProcessor {
		public final static String PARALLEL_TYPE = "parallelexecution";

		public ParallelBlockProcessor(ProcessingContext ctx) {
			super(Arrays.asList(PARALLEL_TYPE).parallelStream().collect(Collectors.toSet()), ctx);
		}

		@Override
		public void processBlock(BlockType paraEx) {
			//Object mutation = paraEx.getMutation(); 													
			getOutflowType(paraEx).ifPresent(bType -> ctx.getDndStack().peek().setOutBranchingType(bType));
			if (ctx.getCheckPoints().size() > 0) {
				ctx.getDndStack().peek().setHasExternalContextRules(true);
				ctx.getCheckPoints().clear(); // assuming we first transformed them to Drools rules
			}
			long size = ctx.getSequenceLengthHierarchy().pop(); //remove sequence length
			size--;
			ctx.getSequenceLengthHierarchy().push(size);// reduce by one and place back on stack

			if (size > 0) {//this is not the last item in the sequence, we thus need to create a new closing DND				
				DecisionNodeDefinition closingDND = new DecisionNodeDefinition(ctx.produceId(), ctx.getCurrentWFD()); 						
				getInflowType(paraEx).ifPresent(bType -> closingDND.setInBranchingType(bType));	
				ctx.getCurrentWFD().getDecisionNodeDefinitions().add(closingDND);
				ctx.getClosingDND().push(closingDND);				
			} else {				
				getInflowType(paraEx).ifPresent(bType -> ctx.getClosingDND().peek().setInBranchingType(bType));
				ctx.getClosingDND().push(ctx.getClosingDND().peek()); //we push the same element again, so that at the end we can remove one without checking
			}				
			//part of the parallel type, now lets look inside eachof them
			if (Utils.getStatements(paraEx.getStatementOrValue()).stream().count() > 0) {
				Utils.getStatements(paraEx.getStatementOrValue()).stream()
					.forEach(branch -> ctx.processStatement(branch));
			} else { // if there is nothing in the parablock yet, then put in a dummy
				produceDummyStep();
			}
			// push Closing DND onto stack 			
			ctx.getDndStack().push(ctx.getClosingDND().pop());
		}
		
		private void produceDummyStep() {
			// we simulate we are a block
			new StepBlockProcessor(this.ctx).processBlock(ProcessingContext.getDummyStepBlock());
		}

		private Optional<InFlowType> getOutflowType (BlockType paraEx) {
			return Utils.getFieldValue(paraEx.getField(), "OutFlowType").map(strType -> InFlowType.AND); // we currently return AND as the new type is not supported in the definitions yet.
		}

		private Optional<InFlowType> getInflowType (BlockType paraEx) {
			return Utils.getFieldValue(paraEx.getField(), "InFlowType").map(strType -> InFlowType.valueOf(strType)); 
		}
	}

	public static class CheckpointBlockProcessor extends AbstractBlockProcessor {
		public final static String CHECKPOINT_TYPE = "checkpoint";

		public CheckpointBlockProcessor(ProcessingContext ctx) {
			super(Arrays.asList(CHECKPOINT_TYPE).parallelStream().collect(Collectors.toSet()), ctx);
		}

		@Override
		public void processBlock(BlockType cp) {
			ctx.getCheckPoints().push(cp);
		}
	}

	public static class ParaBranchProcessor extends AbstractStatementProcessor {

		List<BlockType> preStepBlocks = new LinkedList<BlockType>();
		List<BlockType> postStepBlocks = new LinkedList<BlockType>();
		
		public ParaBranchProcessor(ProcessingContext ctx) {
			super(ctx);			
		}

		@Override
		public boolean willAccept(Statement stmt) {
			return stmt.getName().startsWith("DO") || stmt.getName().equals("STACK");
		}

		@Override
		public void processStatement(Statement stmt) {
			preStepBlocks.clear();
			postStepBlocks.clear();
			
			List<BlockType> blocks = Utils.flattenByNext(stmt.getBlock());
			DecisionNodeDefinition openingDND = ctx.getDndStack().peek();
			//List<TaskDefinition> newTDs = new LinkedList<>();
			long expTasks = blocks.stream()
					.filter(block -> isExecutionBlock(block)) 
					.count();
			if (expTasks <= 0) {
				ctx.getSequenceLengthHierarchy().push(expTasks);
				// we have an empty parallel branch, so the outer might expect a step/task to be produced here
				// additionally, we might be the last one from the outer, 
				// for simplicity, we just generate a dummy/noop step
				log.info("  |-> empty para, adding dummy ");
				ctx.processBlock(ProcessingContext.getDummyStepBlock());
			} else {
				// split into three parts: preExe, exe, and postExe
				reduceToPreExeBlocks(blocks);
				preStepBlocks.stream().forEach(block -> {
					log.info("  |-> "+block.getType());
					ctx.processBlock(block);				
				});
				produceProcInput();
				
				// check if the first block of step, noop step and parallelexe is a parallel execution, as we then need to insert a dummy step
				boolean needToInsertLeadingDummy = isNeedToInsertLeadingDummy(blocks);
				// check if the last block is a parallel, then also we need to insert a dummy
				boolean needToInsertTrailingDummy = isNeedToInsertTrailingDummy(blocks);
				if (needToInsertLeadingDummy) expTasks++;
				if (needToInsertTrailingDummy) expTasks++;
				ctx.getSequenceLengthHierarchy().push(expTasks);
				if (needToInsertLeadingDummy) {
					ctx.processBlock(ProcessingContext.getDummyStepBlock());
				}
				// regardless of noop step added with dnd or not, we now process the rest
				blocks.stream().forEach(block -> {
					log.info("  |-> "+block.getType());
					ctx.processBlock(block);				
				});
				if (needToInsertTrailingDummy) {
					ctx.processBlock(ProcessingContext.getDummyStepBlock());
				// need to add noop step incl dnd, situation is exactly as if we had a noop step between the parallelexecution statements
				}
				// now remove all DND from stack as we have processed all steps within this parallel branch/sequence
				while (ctx.getDndStack().peek() != openingDND) {
					ctx.getDndStack().pop(); //pops all intermediary DNDs between opening and closing DND
				}
				reduceToPostExeBlocks(blocks);
				produceProcOutput();
			}
			ctx.getSequenceLengthHierarchy().pop(); // we are done with the sequence, so we remove the entry
			
		}
	
		private void reduceToPreExeBlocks(List<BlockType> blocks) {
			if (blocks.isEmpty()) return;
			while (!isExecutionBlock(blocks.get(0))) {
				preStepBlocks.add(blocks.remove(0));
			}
		}
		
		private void reduceToPostExeBlocks(List<BlockType> blocks) {
			if (blocks.isEmpty()) return;
			Collections.reverse(blocks);
			while (!isExecutionBlock(blocks.get(0))) {
				postStepBlocks.add(blocks.remove(0));
			}
			Collections.reverse(blocks);//back to original order
			Collections.reverse(postStepBlocks);
		}
		
		private void produceProcInput() {
			preStepBlocks.stream()
			.filter(block -> block.getType().equals("variables_set"))
			.forEach(varBlock -> { 
				Map.Entry<String,String> idVal = DataBlockProcessors.getVarIdAndNameFromBlock(varBlock);
				ArtifactType type= ctx.getVarIndex().get(idVal.getKey());
				Optional.ofNullable(ctx.getTaskHierarchy().peek()).ifPresent(
						td -> { 																
							if (td.getExpectedInput().containsKey(idVal.getValue())) {												
								td.getExpectedInput().replace(idVal.getValue(), UNKNOWNTYPE, type);
							} else {
								td.getExpectedInput().put(idVal.getValue(), type);
							}
							ctx.getVarId2outputRole().put(idVal.getKey(), Pair.of(td.getId(),idVal.getValue()));
						} );	
			});
		}
		
		
		private void produceProcOutput() {
			postStepBlocks.stream()
			.filter(block -> block.getType().equals("artuse"))
			.forEach(artBlock ->  Utils.getFirstValue(artBlock.getStatementOrValue()).ifPresent(
					value -> { 
						Map.Entry<String,String> idVal = DataBlockProcessors.getVarIdAndNameFromBlock(value.getBlock());
						ArtifactType type= ctx.getVarIndex().get(idVal.getKey());
						Optional.ofNullable(ctx.getTaskHierarchy().peek()).ifPresent(
								td -> { 																
									if (td.getExpectedOutput().containsKey(idVal.getValue())) {												
										td.getExpectedOutput().replace(idVal.getValue(), UNKNOWNTYPE, type);
									} else {
										td.getExpectedOutput().put(idVal.getValue(), type);
									}
								} );	
					}));
		}
		
		private boolean isNeedToInsertLeadingDummy(List<BlockType> blocks) {
			return blocks.stream()
					.filter(block -> isExecutionBlock(block))
					.findFirst()
					.map( b -> { if (b.getType().equals("parallelexecution")) {
								// need to add noop step incl dnd, situation is exactly as if we had a noop step between the parallelexecution statements
								return b;
							} else return null;
							}).isPresent();
		}
		
		private boolean isNeedToInsertTrailingDummy(List<BlockType> blocks) {
			return blocks.stream()
					.filter(block -> isExecutionBlock(block))
					.reduce(($, current) -> current) // retains the last, only efficien for short lists like here
					.map( b -> { if (b.getType().equals("parallelexecution")) {
								// need to add noop step incl dnd, situation is exactly as if we had a noop step between the parallelexecution statements
								return b;
							} else return null;
							}).isPresent();
		}
		
	}

	public static class StepBlockProcessor extends AbstractBlockProcessor {
		public final static String[] STEPTYPES = new String[]{"step", "noopstep"};

		public StepBlockProcessor(ProcessingContext ctx) {
			super(Arrays.asList(STEPTYPES).parallelStream().collect(Collectors.toSet()), ctx);
		}

		@Override
		public void processBlock(BlockType step) {
			TaskDefinition td = null;
			DecisionNodeDefinition dnd = ctx.getDndStack().peek(); //previous DND
			if (step.getType().equals("step")) {
				String name = step.getField().get(0).getValue();
				td = new TaskDefinition(name, ctx.getCurrentWFD());			
				List<Statement> stepContent = Utils.getStatements(step.getStatementOrValue());
				processStepContent(stepContent, td, dnd);
			} else if (step.getType().equals("noopstep")) {
				String name = "NoOpStep"+ctx.getAndIncrNoopCounter();
				td = new NoOpTaskDefinition(name, ctx.getCurrentWFD());				
			} //todo if another parallel or function
			ctx.getCurrentWFD().getWorkflowTaskDefinitions().add(td);			
			// now wire up step
			
			//establish whether there is some activation conditions
			boolean hasActivationCondition = false;
			if (ctx.getCheckPoints().size() > 0) {
				hasActivationCondition = true;
				ctx.getCheckPoints().clear(); // assuming we first transformed them to Drools rules
			}		
			//establish whether there is some dataflow, if there is required input, then yes
			boolean hasInDataFlow = td.getExpectedInput().keySet().size() > 0;
			dnd.addOutBranchDefinition(new DefaultBranchDefinition("outTo-"+td.getId(), td, hasActivationCondition, hasInDataFlow, dnd));			
			//now setup next DND after this, if this is not the last one
			//TODO: establish whether there is some progress conditions			
			//establish whether there is some dataflow
			boolean hasOutDataFlow = td.getExpectedOutput().keySet().size() > 0; //TODO:revise this count if QAdocuments are added to output
			long size = ctx.getSequenceLengthHierarchy().pop(); //remove sequence length
			size--;
			ctx.getSequenceLengthHierarchy().push(size);// reduce by one and place back on stack
			if (size > 0) {
				DecisionNodeDefinition dndNext = new DecisionNodeDefinition(ctx.produceId(), ctx.getCurrentWFD());			
				ctx.getCurrentWFD().getDecisionNodeDefinitions().add(dndNext);
				dndNext.addInBranchDefinition(new DefaultBranchDefinition("inFrom-"+td.getId(), td, false, hasOutDataFlow, dndNext));
				ctx.getDndStack().push(dndNext);
			} else { // for last one, use closing DND
				DecisionNodeDefinition dndClosing = ctx.getClosingDND().peek();
				dndClosing.addInBranchDefinition(new DefaultBranchDefinition("inFrom-"+td.getId(), td, false, hasOutDataFlow, dndClosing));			
			}		
		}

		private void processStepContent(List<Statement> content, TaskDefinition td, DecisionNodeDefinition prevDnd) {
			// look for Input, Transitions, and output
			content.stream().filter(stmt -> stmt.getName().equals("Input"))
			.flatMap(inputStmt -> Utils.flattenByNext(inputStmt.getBlock()).stream())
			.filter(block -> block.getType().equals("artuse"))
			.forEach(block -> Utils.getFieldValue(block.getField(), "NAME").ifPresent(
					name ->	Utils.getFirstValue(block.getStatementOrValue()).ifPresent(
							value -> DataBlockProcessors.getArtifactTypeFromBlock(value.getBlock(), ctx).ifPresent(
									artType -> { td.getExpectedInput().put(name, artType); // to to task definition
										// if this is a var created somewhere else, let map there 
									DataBlockProcessors.getVariableIdFromBlock(value.getBlock()).ifPresent( 
											varId -> ctx.getOutputRoleFor(varId).ifPresent( 
													from -> prevDnd.addMapping(from.getFirst(), from.getSecond(), td.getId(), name)));
									}) ) )						 
					);
			
			//TODO LATER: process transitions	
			content.stream().filter(stmt -> stmt.getName().equals("Transitions"))
				.flatMap(stmt -> Utils.flattenByNext(stmt.getBlock()).stream())
				.filter(block -> block.getType().equals("transition"))
				.forEach(trans -> ctx.getRuleGen().addCompletionRule(td, trans));
			
			//addCompletionRule(TaskDefinition td, BlockType condition)

			content.stream().filter(stmt -> stmt.getName().equals("Output"))
			.flatMap(inputStmt -> Utils.flattenByNext(inputStmt.getBlock()).stream())
			.map(block -> { ctx.processBlock(block); return block; })
			// TODO: support not only artifact usage but also QA checks
			.filter(block -> block.getType().equals("artuse"))
			.forEach(block -> Utils.getFieldValue(block.getField(), "NAME").ifPresent(
					name ->	Utils.getFirstValue(block.getStatementOrValue()).ifPresent(
							value -> DataBlockProcessors.getArtifactTypeFromBlock(value.getBlock(), ctx).ifPresent(
									artType -> { td.getExpectedOutput().put(name, artType); 
									// only if that role is a variable, which gets exposed, can we add that		
									DataBlockProcessors.getVariableIdFromBlock(value.getBlock()).ifPresent( 
											varId -> ctx.getVarId2outputRole().put(varId, Pair.of(td.getId(),name)));
									} ) ) )						 
					);
		}	
	}
	
	public static boolean isExecutionBlock(BlockType block) {
		return block.getType().equals("step") || block.getType().equals("noopstep") || block.getType().equals("parallelexecution"); //todo support subprocess steps
	}

}
