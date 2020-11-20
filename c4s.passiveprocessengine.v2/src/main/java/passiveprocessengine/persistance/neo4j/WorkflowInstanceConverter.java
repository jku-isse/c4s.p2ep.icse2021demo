package passiveprocessengine.persistance.neo4j;

import org.neo4j.ogm.typeconversion.AttributeConverter;
import passiveprocessengine.instance.WorkflowInstance;

public class WorkflowInstanceConverter implements AttributeConverter<WorkflowInstance, String>{

	@Override
	public String toGraphProperty(WorkflowInstance value) {
		if (value!=null)
			return value.getId();
		else
			return "";
	}

	@Override
	public WorkflowInstance toEntityAttribute(String value) {
		if (value != null && value.length() > 0) {
			return new PlaceHolderWorkflowInstance(value);
		} else 
			return null;
	}
	
	public static class PlaceHolderWorkflowInstance extends WorkflowInstance {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("deprecation")
		public PlaceHolderWorkflowInstance(String id) {
			this.id = id;
		}
	}
}
