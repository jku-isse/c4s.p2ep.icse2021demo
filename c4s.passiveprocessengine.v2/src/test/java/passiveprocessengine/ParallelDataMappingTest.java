package passiveprocessengine;

import org.junit.Before;
import org.junit.Test;
import passiveprocessengine.definition.ArtifactType;
import passiveprocessengine.definition.ArtifactTypes;
import passiveprocessengine.definition.DecisionNodeDefinition.States;
import passiveprocessengine.definition.IWorkflowTask;
import passiveprocessengine.definition.TaskLifecycle.Events;
import passiveprocessengine.definition.TaskLifecycle.State;
import passiveprocessengine.exampleworkflows.ParallelNoCondWorkflowWithDatamapping;
import passiveprocessengine.instance.*;
import passiveprocessengine.instance.QACheckDocument.QAConstraint;

import static org.junit.Assert.assertEquals;

public class ParallelDataMappingTest {

    private ResourceLink rl;
    private final String ID = "test";
    private QACheckDocument qaDoc;
    private QAConstraint qac;
    
    @Before
    public void setup() {
        rl = new ResourceLink("context", "href", "rel", "as", "linkType", "title");
       qac  = new QAConstraint() {
			boolean ok = false;
    	   @Override
			public boolean isFulfilled() {
				return ok;
			}

			@Override
			public void checkConstraint() {
				ok = true;
			}};
    }


    @Test
    public void testMapOutputsToExpectedInputs() {
        // create a new workflow definition
    	ParallelNoCondWorkflowWithDatamapping workflow = new ParallelNoCondWorkflowWithDatamapping();
        workflow.setTaskStateTransitionEventPublisher(event -> { System.out.println(String.format("%s : %s -> %s", event.getTask().getType().getId(), event.getFromState(), event.getToState())); }); // publisher must be set to prevent NullPointer
        // create an instance out of the workflow definition
        WorkflowInstance wfi = workflow.createInstance(ID);
        qaDoc = new QACheckDocument("someid", wfi);
        qaDoc.addConstraint(qac);
        wfi.addInput(new ArtifactInput(rl, ParallelNoCondWorkflowWithDatamapping.ROLE_WPTICKET, new ArtifactType(ArtifactTypes.ARTIFACT_TYPE_RESOURCE_LINK)));
        wfi.enableWorkflowTasksAndDecisionNodes();

        IWorkflowTask openT = wfi.getWorkflowTask("Open#test");
        openT.addOutput(new ArtifactOutput(qaDoc, "QADOC", new ArtifactType(ArtifactTypes.ARTIFACT_TYPE_QA_CHECK_DOCUMENT)));
        // Get First Para Step and signal completion
        IWorkflowTask docT = wfi.getWorkflowTask("Doc#test");
        docT.addOutput(new ArtifactOutput(rl, ParallelNoCondWorkflowWithDatamapping.ROLE_DOC, new ArtifactType(ArtifactTypes.ARTIFACT_TYPE_RESOURCE_LINK)));
        assertEquals(State.COMPLETED, docT.getLifecycleState());
        
        // now we should have Report input fullfilled and closed input fullfilled, started due to OR SYNC
        IWorkflowTask closedT = wfi.getWorkflowTask("Closed#test");
        IWorkflowTask reportT = wfi.getWorkflowTask("Reported#test");
        assertEquals(State.ENABLED, closedT.getLifecycleState());
        assertEquals(State.ENABLED, reportT.getLifecycleState());	        
        assertEquals(State.ACTIVE, openT.getLifecycleState());	        
        
        qac.checkConstraint(); //switches this constraint to fulfilled
        openT.signalEvent(Events.OUTPUTCONDITIONS_FULFILLED); // as there is no output, thats the way to signal completion
        assertEquals(State.ENABLED, closedT.getLifecycleState());
        assertEquals(State.ENABLED, reportT.getLifecycleState());	        
        assertEquals(State.COMPLETED, openT.getLifecycleState());
        
        // closed Step signal completion
        reportT.addOutput(new ArtifactOutput(rl, ParallelNoCondWorkflowWithDatamapping.ROLE_DOC, new ArtifactType(ArtifactTypes.ARTIFACT_TYPE_RESOURCE_LINK)));
        assertEquals(State.COMPLETED, reportT.getLifecycleState());
        
        // check that final DNI is not complete yet, needs to wait for CLOSED task to complete
        DecisionNodeInstance dniClosed2End = wfi.getDecisionNodeInstance("closed2end#"+ID);
        assertEquals(States.AVAILABLE, dniClosed2End.getState());
        
        closedT.signalEvent(Events.OUTPUTCONDITIONS_FULFILLED); 
        assertEquals(State.COMPLETED, closedT.getLifecycleState());
        
        assertEquals(States.PROGRESSED_OUTBRANCHES, dniClosed2End.getState());
        
        //        
//        assertEquals(1, closedT.getInput().size());
         assertEquals(1, wfi.getOutput().size());
    }
}
