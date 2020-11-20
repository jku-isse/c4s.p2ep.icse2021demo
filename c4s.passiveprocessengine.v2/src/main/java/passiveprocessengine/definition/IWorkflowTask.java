package passiveprocessengine.definition;

import passiveprocessengine.instance.AbstractWorkflowInstanceObject;
import passiveprocessengine.instance.ArtifactInput;
import passiveprocessengine.instance.ArtifactOutput;
import passiveprocessengine.instance.IWorkflowInstanceObject;

import java.util.List;
import java.util.Set;

public interface IWorkflowTask extends IWorkflowInstanceObject {

    ITaskDefinition getType();
    TaskLifecycle.State getLifecycleState();
    Set<AbstractWorkflowInstanceObject> signalEvent(TaskLifecycle.Events event);

    List<ArtifactOutput> getOutput();
    boolean removeOutput(ArtifactOutput ao);
    Set<AbstractWorkflowInstanceObject> addOutput(ArtifactOutput ao);

    List<ArtifactInput> getInput();
    boolean removeInput(ArtifactInput ai);
    void addInput(ArtifactInput ai);
	Artifact getAnyOneOutputByRole(String outputRole);
	Artifact getAnyOneOutputByType(String artifactType);
	Artifact getAnyOneInputByRole(String inputRole);
	Artifact getAnyOneInputByType(String artifactType);
}
