package passiveprocessengine.instance;

import java.util.Set;

import org.neo4j.ogm.annotation.*;
import passiveprocessengine.definition.IBranchDefinition;
import passiveprocessengine.definition.TaskLifecycle.State;

@RelationshipEntity
public abstract class AbstractBranchInstance implements IBranchInstance {


	@Id
	private String id;
//	@GeneratedValue
//	private Long id; //just used for passiveprocessengine.persistance.neo4j OGM
	
	//@Relationship(type="SPECIFIED_BY") 
	protected transient IBranchDefinition bd;
	@EndNode
	private WorkflowTask task = null;
	@Property
	private BranchState state = BranchState.Waiting;
	@StartNode
	private DecisionNodeInstance dni;
	
	@Deprecated
	public AbstractBranchInstance() {}		
	
	public  AbstractBranchInstance(WorkflowTask task, IBranchDefinition bd, WorkflowInstance wfi) {
		this.id = bd.getName()+"#"+wfi.getId();
		this.task = task;
		this.bd = bd;
	}
	
	public AbstractBranchInstance( IBranchDefinition bd, WorkflowInstance wfi) {
		this.id = bd.getName()+"#"+wfi.getId();
		this.bd = bd;
	}
	
	@Override
	public void setDecisionNodeInstance(DecisionNodeInstance dni) {
		this.dni = dni;
	}
	
	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchInstance#getTask()
	 */
	@Override
	public WorkflowTask getTask() {
		if (hasTask())
			return task;
		else
			return null;		
	}
	
	public boolean isConnectedTaskCompleted() {
		if (hasTask()) {
			if (this.getTask().getLifecycleState().equals(State.COMPLETED))
				return true;
			else return false;
		} else return false;
	}

	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchInstance#setTask(c4s.impactassessment.workflowmodel.workflowmodel.WorkflowTask)
	 */
	@Override
	public void setTask(WorkflowTask task) {
		if (!hasTask())			
			this.task = task;		
		else
			System.err.println(String.format("Branch %s already has task, cannot set new task: %s ", this, task));
	}

	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchInstance#getBranchDefinition()
	 */
	@Override
	public IBranchDefinition getBranchDefinition() {
		return bd;
	}
	
	@Override
	public DecisionNodeInstance getDecisionNodeInstance() {
		return this.dni;
	}

//	public boolean isFullfilled() {
//		return fullfilled;
//	}
	
	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchInstance#getState()
	 */
	@Override
	public BranchState getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchInstance#setConditionsFulfilled()
	 */
	@Override
	public void setConditionsFulfilled() {
		state = BranchState.TransitionEnabled;
	}
	
	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchInstance#setBranchUsedForProgress()
	 */
	@Override
	public void setBranchUsedForProgress() {
		state = BranchState.TransitionPassed;
	}
	
	/* (non-Javadoc)
	 * @see c4s.impactassessment.workflowmodel.workflowmodel.IBranchInstance#setConditionsNoLongerHold()
	 */
	@Override
	public void setConditionsNoLongerHold() {
		//if (state == BranchState.TransitionEnabled)
			state = BranchState.Waiting;
	}
	
	@Override
	public void setBranchNotAllowed() {
		state = BranchState.Disabled;
	}
	
	@Override
	public boolean hasTask() {
		return task!=null && !(task instanceof PlaceHolderTask);
	}
	
	@Override
	public String toString() {
		if (task == null || task instanceof PlaceHolderTask)
			return "BranchInstance [task=NONE-YET , state=" + state + " , ofType: " + /*bd.getName() +*/ "]";	//TODO
		else
			return "BranchInstance [task=" + task.getId() + ", state=" + state + " , ofType: " + bd.getName() + "]";
	}

}
