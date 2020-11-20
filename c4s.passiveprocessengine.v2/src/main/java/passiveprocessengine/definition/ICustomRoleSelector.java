package passiveprocessengine.definition;

public interface ICustomRoleSelector {
	public Role getRoleForTaskState(IWorkflowTask wt, TaskDefinition td);
}
