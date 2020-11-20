package passiveprocessengine.definition;

import lombok.Getter;
import lombok.Setter;

public class WorkflowWrapperTaskDefinition extends TaskDefinition {

    private @Getter @Setter String subWfdId;

    public WorkflowWrapperTaskDefinition(String definitionId, WorkflowDefinition wfd, String subWfdId) {
        super(definitionId, wfd);
        this.subWfdId = subWfdId;
    }
    @Deprecated // needed only for passiveprocessengine.persistance.neo4j persistence mechanism requires non-arg constructor
    public WorkflowWrapperTaskDefinition() {
        super();
    }

}
