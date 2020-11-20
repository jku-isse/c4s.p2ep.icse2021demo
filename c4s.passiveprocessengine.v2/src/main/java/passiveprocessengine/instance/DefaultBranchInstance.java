package passiveprocessengine.instance;

import org.neo4j.ogm.annotation.RelationshipEntity;
import passiveprocessengine.definition.IBranchDefinition;

@RelationshipEntity
public class DefaultBranchInstance extends AbstractBranchInstance {

	public DefaultBranchInstance(WorkflowTask task, IBranchDefinition bd, WorkflowInstance wfi) {
		super(task, bd, wfi);
	}

	public DefaultBranchInstance(IBranchDefinition bd, WorkflowInstance wfi) {
		super(bd, wfi);
	}

}
