package passiveprocessengine.persistance.json;

import passiveprocessengine.definition.DecisionNodeDefinition;
import passiveprocessengine.definition.DefaultBranchDefinition;

@SuppressWarnings("deprecation")
public class PersistableBranchDefinition extends DefaultBranchDefinition {

    public void setDecisionNodeDefinition(DecisionNodeDefinition dnd) {
        this.dnd = dnd;
    }
}
