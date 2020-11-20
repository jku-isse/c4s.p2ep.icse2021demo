package passiveprocessengine.persistance.neo4j;


import passiveprocessengine.definition.*;
import passiveprocessengine.instance.DecisionNodeInstance;
import passiveprocessengine.instance.WorkflowInstance;
import passiveprocessengine.instance.WorkflowTask;

import java.util.List;

public class BasicServices {

	public interface TaskDefinitionService extends IPersistable<TaskDefinition>{}
	public static interface ArtifactTypeService extends IPersistable<ArtifactType> {}
	public static interface WorkflowDefinitionService extends IPersistable<DefaultWorkflowDefinition> {}
	
	public static interface WorkflowInstanceService extends IPersistable<WorkflowInstance> {
		public void deleteAllAbstractWorkflowInstanceObjectsByWorkflowInstanceId(String workflowInstanceId);
		public void deleteWorkflowInstanceViaQuery(String workflowInstanceId);
	}
	public static interface WorkflowTaskService extends IPersistable<WorkflowTask> {
		public List<WorkflowTask> deleteDetachedPlaceHolders();
	}
	public static interface DecisionNodeInstanceService extends IPersistable<DecisionNodeInstance> {}
	public static interface DecisionNodeDefinitionService extends IPersistable<DecisionNodeDefinition> {}
	public static interface ArtifactService extends IPersistable<AbstractArtifact>{
		public List<AbstractArtifact> deleteArtifactsByWorkflowInstanceId(String workflowInstanceId);
	}
	
}
