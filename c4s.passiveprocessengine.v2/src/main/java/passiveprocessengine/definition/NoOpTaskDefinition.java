package passiveprocessengine.definition;


import lombok.Getter;

public class NoOpTaskDefinition extends TaskDefinition {

    private @Getter boolean isNoOp; // needed for WFD deserialization

    public NoOpTaskDefinition(String definitionId, WorkflowDefinition wfd) {
        super(definitionId, wfd);
        this.isNoOp = true;
    }
    @Deprecated // needed only for passiveprocessengine.persistance.neo4j persistence mechanism requires non-arg constructor
    public NoOpTaskDefinition() {
        super();
    }

    @Override
    public TaskLifecycle.InputState calcInputState(IWorkflowTask wt) {
        return TaskLifecycle.InputState.INPUT_SUFFICIENT;
    }
    @Override
    public TaskLifecycle.OutputState calcOutputState(IWorkflowTask wt) {
        return TaskLifecycle.OutputState.OUTPUT_SUFFICIENT;
    }
}
