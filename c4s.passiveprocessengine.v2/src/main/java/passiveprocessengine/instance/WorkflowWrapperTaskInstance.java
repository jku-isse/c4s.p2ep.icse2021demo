package passiveprocessengine.instance;

import com.github.oxo42.stateless4j.StateMachine;
import lombok.Getter;
import passiveprocessengine.definition.AbstractWorkflowDefinition;
import passiveprocessengine.definition.TaskLifecycle;
import passiveprocessengine.definition.TaskStateTransitionEventPublisher;

public class WorkflowWrapperTaskInstance extends WorkflowTask {

    private @Getter String subWfdId;
    private @Getter String subWfiId;

    @Deprecated
    public WorkflowWrapperTaskInstance() {
        super();
    }

    public WorkflowWrapperTaskInstance(String taskId, WorkflowInstance wfi, StateMachine<TaskLifecycle.State, TaskLifecycle.Events> sm, TaskStateTransitionEventPublisher pub, String subWfdId) {
        super(taskId, wfi, sm, pub);
        subWfiId = "Nested#"+taskId;
        this.subWfdId = subWfdId;
    }

}
