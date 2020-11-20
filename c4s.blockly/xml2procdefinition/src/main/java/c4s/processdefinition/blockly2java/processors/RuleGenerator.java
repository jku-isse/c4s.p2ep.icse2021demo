package c4s.processdefinition.blockly2java.processors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import https.developers_google_com.blockly.xml.BlockType;
import passiveprocessengine.definition.TaskDefinition;
import passiveprocessengine.definition.TaskLifecycle;
import passiveprocessengine.definition.TaskLifecycle.Events;
import passiveprocessengine.definition.TaskLifecycle.State;

public class RuleGenerator {

	StringBuffer ruleFile = new StringBuffer();
	
	public void addCompletionRule(TaskDefinition td, BlockType condition) {
		addTransitionRule(td, "dddd", State.COMPLETED, Arrays.asList(new Events[]{Events.ACTIVATE, Events.OUTPUTCONDITIONS_FULFILLED}));
		//		ruleFile.append("\r\n rule \"TriggerCompletion"+td.getId()+"\"  \r\n");
//		ruleFile.append("   no-loop \r\n when \r\n");
//		// when part
//		ruleFile.append("      $task : WorkflowTask (  getWorkflow().getId().equals($wfi.getId()) &&\r\n" + 
//				"								!(getLifecycleState().equals(TaskLifecycle.State.COMPLETED)) &&\r\n" + 
//				"								getTaskType().getId() == \""+td.getId() +"\",	\r\n" + 
//				"								$art"+condition.getType()+" : getAnyOneInputByRole(\""+condition.getType()+"\")						 " +
//				"								) @watch( lifecycleState )\r\n" + 
//				"		$dn : DecisionNodeInstance( getWorkflow().getId().equals($task.getWorkflow().getId()) &&        							\r\n" + 
//				"        							getInBranchForWorkflowTask($task) != null &&\r\n" + 
//				"        							!isTaskCompletionConditionsFullfilled()) @watch( state )");
//	// then part
//		ruleFile.append("\r\n      then\r\n" + 
//				"		modify($task) {\r\n" + 
//				"			signalEvent(Events.ACTIVATE),\r\n" + 
//				"			signalEvent(Events.OUTPUTCONDITIONS_FULFILLED)\r\n" + 
//				"		}\r\n" + 
//				"		Set<AbstractWorkflowInstanceObject> newWFOs = $dn.activateInBranch($dn.getInBranchForWorkflowTask($task));\r\n" + 
//				"		System.out.println(String.format(\"Task complete with progress: %s  - %s \",newWFOs.size(), $task.toString()));       	\r\n" + 
//				"       	newWFOs.stream()\r\n" + 
//				"       		.map(wfo -> { System.out.println(\"Inserting: \"+wfo.getId()); return wfo; } )\r\n" + 
//				"       		.forEach( dni -> insert(dni) );\r\n" + 
//				"end");
	}
	
//	public void addActivationRule() {
//		
//	}
	
	
	private void addTransitionRule(TaskDefinition td, String conditionAsText, TaskLifecycle.State checkNotPresent, List<TaskLifecycle.Events> signals ) {
		ruleFile.append("\r\n rule \"TriggerCompletion"+td.getId()+"\"  \r\n");
		ruleFile.append("   no-loop \r\n when \r\n");
		// when part
		
		ruleFile.append("      $task : WorkflowTask (  getWorkflow().getId().equals($wfi.getId()) &&\r\n" + 
				"								!(getLifecycleState().equals(TaskLifecycle.State."+checkNotPresent.name()+")) &&\r\n" + 
				"								getTaskType().getId() == \""+td.getId() +"\",	\r\n" + 
				// TODO: obtain artifact from input or output
				"								 // "+conditionAsText + " \r\n "+
				"								) @watch( lifecycleState )\r\n" + 
				// check if subsequent DNI is not completed yet
				"		$dn : DecisionNodeInstance( getWorkflow().getId().equals($task.getWorkflow().getId()) &&        							\r\n" + 
				"        							getInBranchForWorkflowTask($task) != null &&\r\n" + 
				"        							!isTaskCompletionConditionsFullfilled()) @watch( state )");
	// then part
		ruleFile.append("\r\n      then\r\n" + 
				"		modify($task) {\r\n") ; 
		ruleFile.append(signals.stream().map(signal -> "             signalEvent(Events."+signal.name()+")\r\n")
				.collect(Collectors.joining(",")));
// TODO: replace with Commandgateway call in Axion
		ruleFile.append(		"		}\r\n" + 
// TODO: replace with Commandgateway call in Axion
				"		Set<AbstractWorkflowInstanceObject> newWFOs = $dn.activateInBranch($dn.getInBranchForWorkflowTask($task));\r\n" + 
				"		System.out.println(String.format(\"Task complete with progress: %s  - %s \",newWFOs.size(), $task.toString()));       	\r\n" + 
				"       	newWFOs.stream()\r\n" + 
				"       		.map(wfo -> { System.out.println(\"Inserting: \"+wfo.getId()); return wfo; } )\r\n" + 
				"       		.forEach( dni -> insert(dni) );\r\n" + 
				"end");
	}
	
	public StringBuffer getRuleFileAsBuffer() {
		return ruleFile;
	}
}
