package passiveprocessengine.definition;


import passiveprocessengine.instance.TaskStateTransitionEvent;

public interface TaskStateTransitionEventPublisher {

	public void publishEvent(TaskStateTransitionEvent event);
	
}
