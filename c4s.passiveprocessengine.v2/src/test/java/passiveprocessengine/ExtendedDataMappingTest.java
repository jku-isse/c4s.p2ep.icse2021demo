package passiveprocessengine;

import org.junit.Before;
import org.junit.Test;
import passiveprocessengine.definition.ArtifactType;
import passiveprocessengine.definition.ArtifactTypes;
import passiveprocessengine.definition.IWorkflowTask;
import passiveprocessengine.definition.TaskDefinition;
import passiveprocessengine.definition.TaskLifecycle;
import passiveprocessengine.definition.TaskLifecycle.Events;
import passiveprocessengine.exampleworkflows.LinearNoCondWorkflowWithDatamapping;
import passiveprocessengine.exampleworkflows.SimpleWorkflow;
import passiveprocessengine.instance.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExtendedDataMappingTest {

    private ResourceLink rl;
    private final String ID = "test";

    @Before
    public void setup() {
        rl = new ResourceLink("context", "href", "rel", "as", "linkType", "title");
    }

    @Test
    public void testMapOutputsToExpectedInputs() {
        // create a new workflow definition
    	LinearNoCondWorkflowWithDatamapping workflow = new LinearNoCondWorkflowWithDatamapping();
        workflow.setTaskStateTransitionEventPublisher(event -> { System.out.println(String.format("%s : %s -> %s", event.getTask().getType().getId(), event.getFromState(), event.getToState())); /*No Op*/}); // publisher must be set to prevent NullPointer

        // create an instance out of the workflow definition
        WorkflowInstance wfi = workflow.createInstance(ID);
        wfi.addInput(new ArtifactInput(rl, SimpleWorkflow.ROLE_WPTICKET, new ArtifactType(ArtifactTypes.ARTIFACT_TYPE_QA_CHECK_DOCUMENT)));
        wfi.enableWorkflowTasksAndDecisionNodes();

        // Get First Step and signal completion
        IWorkflowTask openT = wfi.getWorkflowTask("Open#test");
        openT.addOutput(new ArtifactOutput(rl, SimpleWorkflow.ROLE_WPTICKET, new ArtifactType(ArtifactTypes.ARTIFACT_TYPE_QA_CHECK_DOCUMENT)));
        
        // this should needs to be triggered if there the inbranch is marked to have an externally evaluated constraint
        DecisionNodeInstance dniOpen2Closed = wfi.getDecisionNodeInstance("open2closed#"+ID);
        dniOpen2Closed.activateInBranch(
        		dniOpen2Closed.getInBranches().stream()
        	.filter(b -> b.getBranchDefinition().hasActivationCondition())
        	.map(b -> b.getBranchDefinition().getName())
        	.findAny().get());
//        	        
        // Get Second Step and signal completion
        IWorkflowTask closedT = wfi.getWorkflowTask("Closed#test");
        closedT.addOutput(new ArtifactOutput(rl, SimpleWorkflow.ROLE_WPTICKET, new ArtifactType(ArtifactTypes.ARTIFACT_TYPE_QA_CHECK_DOCUMENT)));
        
        assertEquals(1, closedT.getInput().size());
        assertEquals(1, wfi.getOutput().size());
    }
}
