package passiveprocessengine.verification;

import passiveprocessengine.definition.*;
import static passiveprocessengine.verification.WorkflowNode.NodeType.DND;
import static passiveprocessengine.verification.WorkflowNode.NodeType.TD;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WorkflowGraph {

    private Map<String, WorkflowNode> nodes;

    public Map<String, WorkflowNode> getNodeMap() {
        return nodes;
    }

    public Collection<WorkflowNode> getNodes() {
        return nodes.values();
    }

    public WorkflowGraph(AbstractWorkflowDefinition workflow) {
        nodes = new HashMap<>();
        List<DecisionNodeDefinition> dnds = workflow.getDecisionNodeDefinitions();
        DecisionNodeDefinition kickoff = dnds.stream()
                .filter(dnd -> dnd.getInBranches().size() == 0).findAny().get();
        WorkflowNode one = new WorkflowNode(kickoff.getId(), DND);
        connectLayer(dnds, kickoff, one);
    }

    private void connectLayer(List<DecisionNodeDefinition> dnds, DecisionNodeDefinition dnd, WorkflowNode one) {
        for (IBranchDefinition branch : dnd.getOutBranches()) {
            String tdID = branch.getTask().getId();
            WorkflowNode two = new WorkflowNode(tdID, TD);
            connectAndPut(one, two);
            dnds.stream()
                    .filter(d -> d.getInBranches().stream()
                            .anyMatch(x -> x.getTask().getId().equals(tdID)))
                    .forEach(d -> {
                        WorkflowNode three = new WorkflowNode(d.getId(), DND);
                        connectAndPut(two, three);
                        connectLayer(dnds, d, three);
                    });
        }
    }

    private void connectAndPut(WorkflowNode predecessor, WorkflowNode successor) {
        // use already present nodes if possible
        WorkflowNode pre = nodes.getOrDefault(predecessor.getId(), predecessor);
        WorkflowNode suc = nodes.getOrDefault(successor.getId(), successor);
        // connect nodes
        pre.addSuccessor(suc);
        suc.addPredecessor(pre);
        // put nodes into map
        nodes.put(pre.getId(), pre);
        nodes.put(suc.getId(), suc);
    }
}
