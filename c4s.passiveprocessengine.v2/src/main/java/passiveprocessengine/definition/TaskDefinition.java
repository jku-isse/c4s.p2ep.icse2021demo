package passiveprocessengine.definition;

import passiveprocessengine.persistance.neo4j.ArtifactTypeConverter;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.annotation.typeconversion.Convert;

import passiveprocessengine.instance.QACheckDocument;
import passiveprocessengine.instance.WorkflowTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class TaskDefinition extends AbstractWorkflowDefinitionObject implements java.io.Serializable, ITaskDefinition {
	
	private static final long serialVersionUID = 2899700748101656836L;
	@Convert(ArtifactTypeConverter.Input.class)
	private Map<String, ArtifactType> expectedInput = new HashMap<String,ArtifactType>(); // later we can enhance on complexity by wrapping artifact type with conditions etc
	@Convert(ArtifactTypeConverter.Output.class)
	private Map<String,ArtifactType> expectedOutput = new HashMap<String,ArtifactType>();
	@Transient // for now
	private Role responsibleRole;
	
	private transient ICustomRoleSelector roleSelector;
	
	public TaskDefinition(String definitionId, WorkflowDefinition wfd, ICustomRoleSelector roleSelector) {
		super(definitionId, wfd);
		this.roleSelector = roleSelector;		
	}
	
	public void setRoleSelector(ICustomRoleSelector roleSelector) {
		this.roleSelector = roleSelector;
	}

	@Deprecated // needed only for passiveprocessengine.persistance.neo4j persistence mechanism requires non-arg constructor
	public TaskDefinition() {
		super();
	}
	
	public TaskDefinition(String definitionId, WorkflowDefinition wfd) {		
		super(definitionId, wfd);
	}

	@Override
	public Map<String,ArtifactType> getExpectedInput() {
		return expectedInput;
	}

	@Override
	public ArtifactType putExpectedInput(String key, ArtifactType value) {
		return expectedInput.put(key, value);
	}

	@Override
	public Map<String,ArtifactType> getExpectedOutput() {
		return expectedOutput;
	}

	@Override
	public ArtifactType putExpectedOutput(String key, ArtifactType value) {
		return expectedOutput.put(key, value);
	}

	public void setResponsibleRole(Role responsibleRole) {
		this.responsibleRole = responsibleRole;
	}

	public Role getResponsibleRole(IWorkflowTask wt) {
		if (responsibleRole != null)
			return responsibleRole;
		if (roleSelector != null) 
			return roleSelector.getRoleForTaskState(wt, this);			
		else 
			return null;
	}
	
	public TaskLifecycle.InputState calcInputState(IWorkflowTask wt) {
		// default implementation: for every expected input, is there such an artifact
		if (getExpectedInput().isEmpty())
			return TaskLifecycle.InputState.INPUT_SUFFICIENT;
		int count = getExpectedInput().keySet().stream()			
			.map(it -> { return (wt.getAnyOneInputByRole(it) != null) ? 1 : 0; })
			.filter( c -> c > 0)
			.mapToInt(c -> c)
			.sum();
		if (count == 0)
			return TaskLifecycle.InputState.INPUT_MISSING;
		if (count == getExpectedInput().size())
			return TaskLifecycle.InputState.INPUT_SUFFICIENT;
		else
			return TaskLifecycle.InputState.INPUT_PARTIAL;
	}

	public TaskLifecycle.OutputState calcOutputState(IWorkflowTask wt) {
		// default implementation: for every expected output, is there such an artifact
		if (getExpectedOutput().isEmpty() )
			if (allQAFullfilled(wt))
				return TaskLifecycle.OutputState.OUTPUT_SUFFICIENT;
			else 
				return TaskLifecycle.OutputState.OUTPUT_PARTIAL;
		int count = getExpectedOutput().keySet().stream()			
			.map(it -> { return (wt.getAnyOneOutputByRole(it) != null) ?  1 : 0; })
			.filter( c -> c > 0)
			.mapToInt(c -> (int)c)
			.sum();
		if (count == 0)
			return TaskLifecycle.OutputState.OUTPUT_MISSING;
		if (count == getExpectedOutput().size())
			return TaskLifecycle.OutputState.OUTPUT_SUFFICIENT;
		else
			return TaskLifecycle.OutputState.OUTPUT_PARTIAL;
	}
	
	public boolean allQAFullfilled(IWorkflowTask wt) {
		return wt.getOutput().stream()
				.filter(ao -> ao.getArtifactType().getArtifactType().equals(ArtifactTypes.ARTIFACT_TYPE_QA_CHECK_DOCUMENT))
				.map(ao -> ao.getArtifact())
				.filter(QACheckDocument.class::isInstance)
				.map(QACheckDocument.class::cast)
				.allMatch(qaDoc -> qaDoc.areAllConstraintsFulfilled());
	}
	
	public Set<Map.Entry<String, ArtifactType>> getMissingOutput(WorkflowTask wt) {
		return getExpectedOutput().entrySet().stream()
			.filter(tuple -> !wt.hasOutputArtifactOfRole(tuple.getKey()))
			.collect(Collectors.toSet());
	}
	
	public Set<Map.Entry<String, ArtifactType>> getMissingInput(WorkflowTask wt) {
		return getExpectedInput().entrySet().stream()
			.filter(tuple -> !wt.hasInputArtifactOfRole(tuple.getKey()))
			.collect(Collectors.toSet());
	}

	@Override
	public int hashCode() {
		return Objects.hash(expectedInput, expectedOutput, responsibleRole);
	}
	
	@Override
	public String toString() {
		return "[TD: " + id + "] ArtIn: " +expectedInput+ " ArtOut: "+expectedOutput ;
	}
}
