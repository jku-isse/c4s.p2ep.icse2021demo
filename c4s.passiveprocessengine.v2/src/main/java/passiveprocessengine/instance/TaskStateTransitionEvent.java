package passiveprocessengine.instance;


import passiveprocessengine.definition.IWorkflowTask;
import passiveprocessengine.definition.TaskLifecycle;
import passiveprocessengine.definition.TaskLifecycle.State;

public class TaskStateTransitionEvent {

	public TaskLifecycle.State fromState;
	public TaskLifecycle.State toState;
	public transient IWorkflowTask task;
	
	public TaskStateTransitionEvent(State fromState, State toState, IWorkflowTask task) {
		super();
		this.fromState = fromState;
		this.toState = toState;
		this.task = task;
	}	
		
	public void setFromState(TaskLifecycle.State fromState) {
		this.fromState = fromState;
	}

	public void setToState(TaskLifecycle.State toState) {
		this.toState = toState;
	}

	public TaskLifecycle.State getFromState() {
		return fromState;
	}
	public TaskLifecycle.State getToState() {
		return toState;
	}
	public IWorkflowTask getTask() {
		return task;
	}
	
	
}
