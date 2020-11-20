package passiveprocessengine;

import org.junit.Before;
import org.junit.Test;
import passiveprocessengine.definition.ArtifactType;
import passiveprocessengine.definition.ArtifactTypes;
import passiveprocessengine.definition.IWorkflowTask;
import passiveprocessengine.definition.TaskDefinition;
import passiveprocessengine.definition.TaskLifecycle;
import passiveprocessengine.definition.TaskLifecycle.Events;
import passiveprocessengine.exampleworkflows.SimpleWorkflow;
import passiveprocessengine.instance.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class WFTInputOutputMappingTest {

    private ResourceLink rl;
    private final String ID = "test";

    @Before
    public void setup() {
        rl = new ResourceLink("context", "href", "rel", "as", "linkType", "title");
    }

    /**
     * workflow: OPEN --> CLOSED
     * OPEN output: ResourceLink
     * CLOSED expected input: ResourceLink
     */
    @Test
    public void testMapOutputsToExpectedInputs() {
        // create a new workflow definition
        SimpleWorkflow workflow = new SimpleWorkflow();
        workflow.setTaskStateTransitionEventPublisher(event -> { System.out.println(String.format("%s : %s -> %s", event.getTask().getType().getId(), event.getFromState(), event.getToState())); /*No Op*/}); // publisher must be set to prevent NullPointer

        // create an instance out of the workflow definition
        WorkflowInstance wfi = workflow.createInstance(ID);
        wfi.enableWorkflowTasksAndDecisionNodes();

        wfi.getWorkflowTask("Open#test").addOutput(new ArtifactOutput(rl, SimpleWorkflow.ROLE_WPTICKET, new ArtifactType(ArtifactTypes.ARTIFACT_TYPE_QA_CHECK_DOCUMENT)));
        
//        // get first decision node (kickoff)
//        DecisionNodeInstance dniKickoff = wfi.getDecisionNodeInstance("workflowKickOff#"+ID);
//     //   dniKickoff.completedDataflowInvolvingActivationPropagation();
//
//        // get tasks from first decision node and complete dataflow
//        List<TaskDefinition> taskDefinitionsOpen = dniKickoff.getTaskDefinitionsForFulfilledOutBranchesWithUnresolvedTasks();
//        taskDefinitionsOpen.stream()
//                .forEach(td -> {
//                    WorkflowTask wft = wfi.instantiateTask(td);
//                    wft.addOutput(new ArtifactOutput(rl, SimpleWorkflow.ROLE_WPTICKET, new ArtifactType(ArtifactTypes.ARTIFACT_TYPE_QA_CHECK_DOCUMENT)));
//                    wft.signalEvent(TaskLifecycle.Events.INPUTCONDITIONS_FULFILLED);
//                    wfi.activateDecisionNodesFromTask(wft);
//                    dniKickoff.consumeTaskForUnconnectedOutBranch(wft);
//                });
//
//        // get second (and last) decision node (open2closed)
//        DecisionNodeInstance dniOpen2Closed = wfi.getDecisionNodeInstance("open2closed#"+ID);
//        dniOpen2Closed.activateInBranch("openOut");
//     //   dniOpen2Closed.completedDataflowInvolvingActivationPropagation();
//
//
//        // get tasks from first decision node and complete dataflow
//        List<TaskDefinition> taskDefinitionsClosed = dniOpen2Closed.getTaskDefinitionsForFulfilledOutBranchesWithUnresolvedTasks();
//        taskDefinitionsClosed.stream()
//                .forEach(td -> {
//                    WorkflowTask wft = wfi.instantiateTask(td);
//                    wft.signalEvent(TaskLifecycle.Events.INPUTCONDITIONS_FULFILLED);
//                    wfi.activateDecisionNodesFromTask(wft);
//                    dniOpen2Closed.consumeTaskForUnconnectedOutBranch(wft);
//                });

        IWorkflowTask wftClosed = wfi.getWorkflowTask("Closed#"+ID);
        wftClosed.signalEvent(Events.OUTPUTCONDITIONS_FULFILLED);
               
        // after the mapping workflow task "Closed" has one input
        assertEquals(1, wftClosed.getInput().size());
    }
}
