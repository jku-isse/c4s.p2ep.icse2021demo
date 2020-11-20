package passiveprocessengine.definition;


import passiveprocessengine.instance.IBranchInstance;
import passiveprocessengine.instance.WorkflowInstance;

public interface IBranchDefinition {

	String getName();

	TaskDefinition getTask();

	boolean hasActivationCondition();

	// true if it has no external dataflow
	boolean hasDataFlow(); // TODO refactor hasNoOrOnlyAutomaticDataFlow

	IBranchInstance createInstance(WorkflowInstance wfi);

}