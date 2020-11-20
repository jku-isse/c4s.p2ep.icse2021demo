package passiveprocessengine.instance;

import passiveprocessengine.persistance.neo4j.WorkflowInstanceConverter;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import passiveprocessengine.definition.AbstractIdentifiableObject;

@NodeEntity
public abstract class AbstractWorkflowInstanceObject extends AbstractIdentifiableObject implements IWorkflowInstanceObject {
	
	@Convert(WorkflowInstanceConverter.class)
	protected WorkflowInstance workflow;
	
	@Override
	public WorkflowInstance getWorkflow() {
		return workflow;
	}

//	@Modifies( { "workflow" } )
	@Override
	public void setWorkflow(WorkflowInstance wfi) {
		this.workflow = wfi;
	}

	public AbstractWorkflowInstanceObject(String id, WorkflowInstance wfi) {
		super(id);
		this.workflow = wfi;
	}
	
	
	@Deprecated  
	public AbstractWorkflowInstanceObject() {
		super();
	}
	
}
