package passiveprocessengine.instance;


import passiveprocessengine.definition.IdentifiableObject;

public interface IWorkflowInstanceObject extends IdentifiableObject {
    WorkflowInstance getWorkflow();

    //	@Modifies( { "workflow" } )
    void setWorkflow(WorkflowInstance wfi);
}
