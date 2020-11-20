package passiveprocessengine.definition;


import com.google.gson.annotations.JsonAdapter;
import org.neo4j.ogm.annotation.*;
import passiveprocessengine.instance.IBranchInstance;
import passiveprocessengine.instance.WorkflowInstance;
import passiveprocessengine.persistance.json.TaskDefinitionTypeAdapter;

@RelationshipEntity
public abstract class AbstractBranchDefinition implements IBranchDefinition {
	@Id
	private String name;
	@EndNode
	@JsonAdapter(TaskDefinitionTypeAdapter.class)
	protected TaskDefinition task;	// transient to allow for serialization via json
	@StartNode
	transient protected DecisionNodeDefinition dnd;
	
	// when used as outbranch: no activation condition means, branch is fullfilled once the DNI context is fullfilled, we can activate task at end
	// when used as inbranch: no condition on output of branch: as soon as task is set to output complete, branch is fulfilled
	@Property
	private boolean hasActivationCondition = false;
		
	// when used as outbranch: task at end receives no input set from the rule part, optionally task input data is set out of bounds
	// when used as inbranch: task at end produces not output that is relevant for the process, optionally output data is propagated out of bounds
	@Property
	private boolean hasDataFlow = false;
	
	@Deprecated
	public AbstractBranchDefinition() {
		
	}
	
	public AbstractBranchDefinition(String name, TaskDefinition task, DecisionNodeDefinition dnd) {
		this.name = name;
		this.task = task;
		this.dnd = dnd;
	}
	
	public AbstractBranchDefinition(String name, TaskDefinition task, DecisionNodeDefinition dnd, boolean hasActivationCondition, boolean hasDataFlow) {
		this(name, task, dnd);
		this.hasActivationCondition = hasActivationCondition;
		this.hasDataFlow = hasDataFlow;
	}


	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchDefinition#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchDefinition#getTask()
	 */
	@Override
	public TaskDefinition getTask() {
		return task;
	}
	
	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchDefinition#hasActivationCondition()
	 */
	@Override
	public boolean hasActivationCondition() {
		return hasActivationCondition;
	}

	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchDefinition#hasDataFlow()
	 */
	@Override
	public boolean hasDataFlow() {
		return hasDataFlow;
	}
	
	
	
	@Override
	public String toString() {
		return "BranchDef [name=" + name + ", task=" + task.getId() + ", hasActivationCondition="
				+ hasActivationCondition + ", hasDataFlow=" + hasDataFlow + "]";
	}

	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchDefinition#createInstance()
	 */
	@Override
	public abstract IBranchInstance createInstance(WorkflowInstance wfi);

}