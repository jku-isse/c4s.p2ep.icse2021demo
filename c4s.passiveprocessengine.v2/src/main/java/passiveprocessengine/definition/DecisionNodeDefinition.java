package passiveprocessengine.definition;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import org.neo4j.ogm.annotation.Relationship;
import passiveprocessengine.instance.DecisionNodeInstance;
import passiveprocessengine.instance.WorkflowInstance;

import java.util.*;
import java.util.stream.Collectors;

public class DecisionNodeDefinition extends AbstractWorkflowDefinitionObject {

	public static final boolean NO_EXTERNAL_RULE = false;
	public static final boolean HAVING_EXTERNAL_RULE = true;
	
	@Relationship(type="BRANCH", direction=Relationship.INCOMING)
	private Set<IBranchDefinition> inB = new HashSet<IBranchDefinition>();
	//@Transient
	//private HashMap<String, IBranchDefinition> inBranches = new HashMap<String, IBranchDefinition>();
	
	@Relationship(type="BRANCH", direction=Relationship.OUTGOING)
	private Set<IBranchDefinition> outB = new HashSet<IBranchDefinition>();
	//@Transient
	//private HashMap<String, IBranchDefinition> outBranches = new HashMap<String, IBranchDefinition>();
	
	// TODO continue here, how to properly make use of this info, and how to manage it.
	public boolean hasExternalContextRules = false;
	public boolean hasExternalInBranchRules = false;
	public boolean hasExternalOutBranchRules = false;
	
	private InFlowType inBranchingType = InFlowType.AND;
	private OutFlowType outBranchingType = OutFlowType.SYNC;

	private List<MappingDefinition> mappings = new ArrayList<>();
	public List<MappingDefinition> getMappings() {return mappings;}
	
	@Deprecated
	public DecisionNodeDefinition() {
		super();
	}
	
	public DecisionNodeDefinition(String id, WorkflowDefinition wfd, boolean hasExternalInBranchRules, boolean hasExternalContextRules,
								  boolean hasExternalOutBranchRules) {
		super(id, wfd);
		this.hasExternalContextRules = hasExternalContextRules;
		this.hasExternalInBranchRules = hasExternalInBranchRules;
		this.hasExternalOutBranchRules = hasExternalOutBranchRules;
	}

	public DecisionNodeDefinition(String definitionId, WorkflowDefinition wfd, boolean hasExternalContextRules) {
		super(definitionId, wfd);		
		this.hasExternalContextRules = hasExternalContextRules;
	}		
	
	public DecisionNodeDefinition(String definitionId, WorkflowDefinition wfd) {
		super(definitionId, wfd);
	}
	
	public void setHasExternalContextRules(boolean hasExternalContextRules) {
		this.hasExternalContextRules = hasExternalContextRules;
	}
			
	public void setHasExternalInBranchRules(boolean hasExternalInBranchRules) {
		this.hasExternalInBranchRules = hasExternalInBranchRules;
	}

	public void setHasExternalOutBranchRules(boolean hasExternalOutBranchRules) {
		this.hasExternalOutBranchRules = hasExternalOutBranchRules;
	}

	public void addInBranchDefinition(IBranchDefinition bd) {
		//IBranchDefinition oldABD = inBranches.put(bd.getName(), bd);
		//if (oldABD != null) {
		//	inB.remove(oldABD);
		//}
		inB.add(bd);
	}

	public void addOutBranchDefinition(IBranchDefinition bd) {
		//IBranchDefinition oldABD = outBranches.put(bd.getName(), bd);
		//if (oldABD != null) {
		//	outB.remove(oldABD);
		//}
		outB.add(bd);
	}
	
	public Set<IBranchDefinition> getInBranches() {
		return inB;
	}

	public Set<IBranchDefinition> getOutBranches() {
		return outB;
	}
	
	public InFlowType getInBranchingType() {
		return inBranchingType;
	}

	public void setInBranchingType(InFlowType inBranchingType) {
		this.inBranchingType = inBranchingType;
	}

	public OutFlowType getOutBranchingType() {
		return outBranchingType;
	}

	public void setOutBranchingType(OutFlowType outBranchingType) {
		this.outBranchingType = outBranchingType;
	}

	public boolean acceptsWorkflowTask(IWorkflowTask wti) {
		return acceptsWorkflowTaskForInBranches(wti) || acceptsWorkflowTaskForOutBranches(wti);
	}
	
	public boolean acceptsWorkflowTaskForInBranches(IWorkflowTask wti) {
		Optional<IBranchDefinition> optinB = inB.stream()
			.filter(b -> b.getTask().equals(wti.getType()))
			.findFirst();
		return optinB.isPresent();			
	}

	public boolean acceptsWorkflowTaskForOutBranches(IWorkflowTask wti) {
		Optional<IBranchDefinition> optoutB = outB.stream()
			.filter(b -> b.getTask().equals(wti.getType()))
			.findFirst();
		return optoutB.isPresent();			
	}

	public void addMapping(String fromId, String fromRole, String toId, String toRole) {
		mappings.add(new MappingDefinition(fromId, fromRole, toId, toRole));
	}
	public void addMapping(List<MappingDefinition.Pair<String, String>> from, String toId, String toRole) {
		mappings.add(new MappingDefinition(from, toId, toRole));
	}
	public void addMapping(List<MappingDefinition.Pair<String, String>> from, String toId, String toRole, MappingDefinition.MappingType mappingType) {
		mappings.add(new MappingDefinition(from, toId, toRole));
	}
	public void addMapping(String fromId, String fromRole, List<MappingDefinition.Pair<String, String>> to) {
		mappings.add(new MappingDefinition(fromId, fromRole, to));
	}
	public void addMapping(String fromId, String fromRole, List<MappingDefinition.Pair<String, String>> to, MappingDefinition.MappingType mappingType) {
		mappings.add(new MappingDefinition(fromId, fromRole, to));
	}
	public void addMapping(List<MappingDefinition.Pair<String, String>> from, List<MappingDefinition.Pair<String, String>> to) {
		mappings.add(new MappingDefinition(from, to));
	}
	public void addMapping(List<MappingDefinition.Pair<String, String>> from, List<MappingDefinition.Pair<String, String>> to, MappingDefinition.MappingType mappingType) {
		mappings.add(new MappingDefinition(from, to, mappingType));
	}


	public DecisionNodeInstance createInstance(WorkflowInstance wfi) {
		DecisionNodeInstance dni = new DecisionNodeInstance(this, wfi, getStateMachine());
		dni.addInBranches(
				inB.stream()
					.map(b -> b.createInstance(wfi))
					.map(bi -> {bi.setDecisionNodeInstance(dni); 
								bi.setTask(wfi.getNewPlaceHolderTask(bi.getBranchDefinition().getTask())); 
								return bi; })
					.collect(Collectors.toList()));
		dni.addOutBranches(
				outB.stream()
					.map(b -> b.createInstance(wfi))
					.map(bi -> {bi.setDecisionNodeInstance(dni); 
								bi.setTask(wfi.getNewPlaceHolderTask(bi.getBranchDefinition().getTask()));
								return bi; })
					.collect(Collectors.toList())); 		
		return dni;
	}

	@Override
	public String toString() {
		return "DND [id=" + id + " ->"+inBranchingType+"-"+outBranchingType+"-> inB=" + inB + ", outB=" + outB + ", hasExternalContextRules="
				+ hasExternalContextRules + ", hasExternalInBranchRules=" + hasExternalInBranchRules
				+ ", hasExternalOutBranchRules=" + hasExternalOutBranchRules + "]";
	}

	public static enum InFlowType {
		AND, OR, XOR;
	}
	
	public static enum OutFlowType {
		SYNC, ASYNC;
	}
	
	
	public static enum States {
		AVAILABLE, PASSED_INBRANCH_CONDITIONS, PASSED_CONTEXT_CONDITIONS, PASSED_OUTBRANCH_CONDITIONS, PROGRESSED_OUTBRANCHES,
		NESSESSARY_REWORK, PASSED_INBRANCH_CONDITIONS_UPON_REWORK, PASSED_CONTEXT_CONDITIONS_UPON_REWORK, PASSED_OUTBRANCH_CONDITIONS_UPON_REWORK, SIGNALED_REWORK_BEYOND_OUTBRANCHES,
		SUPERSTATE_REGULAR, SUPERSTATE_REWORK
	}
	
	public static enum Events {
		INBRANCHES_FULFILLED, CONTEXT_FULFILLED, OUTBRANCHES_FULFILLED, PROGRESS_TRIGGERED,
		INCONDITIONS_NO_LONGER_HOLD, CONTEXT_NO_LONGER_HOLD, OUTCONDITIONS_NO_LONGER_HOLD;
		
	}
	
	private static StateMachineConfig<States, Events> smc;
	
	public static StateMachineConfig<States, Events> getStateMachineConfig() {
		if (smc == null) {
			smc = new StateMachineConfig<>();
			smc.configure(States.AVAILABLE)
				.substateOf(States.SUPERSTATE_REGULAR)
				.permit(Events.INBRANCHES_FULFILLED, States.PASSED_INBRANCH_CONDITIONS);
			smc.configure(States.PASSED_INBRANCH_CONDITIONS)
				.substateOf(States.SUPERSTATE_REGULAR)
				.permit(Events.CONTEXT_FULFILLED, States.PASSED_CONTEXT_CONDITIONS)
				.permit(Events.INCONDITIONS_NO_LONGER_HOLD, States.AVAILABLE); // as long as we don't progress, we can always go back, no work is done yet
			smc.configure(States.PASSED_CONTEXT_CONDITIONS)
				.substateOf(States.SUPERSTATE_REGULAR)
				.permit(Events.OUTBRANCHES_FULFILLED, States.PASSED_OUTBRANCH_CONDITIONS)
				.permit(Events.INCONDITIONS_NO_LONGER_HOLD, States.AVAILABLE)
				.permit(Events.CONTEXT_NO_LONGER_HOLD, States.PASSED_INBRANCH_CONDITIONS);
			smc.configure(States.PASSED_OUTBRANCH_CONDITIONS)
				.substateOf(States.SUPERSTATE_REGULAR)
				.permit(Events.PROGRESS_TRIGGERED, States.PROGRESSED_OUTBRANCHES)
				.permit(Events.INCONDITIONS_NO_LONGER_HOLD, States.AVAILABLE)
				.permit(Events.CONTEXT_NO_LONGER_HOLD, States.PASSED_INBRANCH_CONDITIONS)
				.permit(Events.OUTCONDITIONS_NO_LONGER_HOLD, States.PASSED_CONTEXT_CONDITIONS);
			smc.configure(States.PROGRESSED_OUTBRANCHES)
				.substateOf(States.SUPERSTATE_REGULAR)
				.permit(Events.INCONDITIONS_NO_LONGER_HOLD, States.NESSESSARY_REWORK)
				//.permit(Events.CONTEXT_NO_LONGER_HOLD, States.PASSED_INBRANCH_CONDITIONS_UPON_REWORK) // context conditions are only required to hold for progressing, not once outbranches have fired
				.permit(Events.OUTCONDITIONS_NO_LONGER_HOLD, States.PASSED_CONTEXT_CONDITIONS_UPON_REWORK);
			
			smc.configure(States.NESSESSARY_REWORK)
				.substateOf(States.SUPERSTATE_REWORK)
				.permit(Events.INBRANCHES_FULFILLED, States.PASSED_INBRANCH_CONDITIONS_UPON_REWORK); 
			smc.configure(States.PASSED_INBRANCH_CONDITIONS_UPON_REWORK)
				.substateOf(States.SUPERSTATE_REWORK)
				.permit(Events.CONTEXT_FULFILLED, States.PASSED_CONTEXT_CONDITIONS_UPON_REWORK)
				.permit(Events.INCONDITIONS_NO_LONGER_HOLD, States.NESSESSARY_REWORK); 
			smc.configure(States.PASSED_CONTEXT_CONDITIONS_UPON_REWORK)
				.substateOf(States.SUPERSTATE_REWORK)
				.permit(Events.OUTBRANCHES_FULFILLED, States.PASSED_OUTBRANCH_CONDITIONS_UPON_REWORK)
				.permit(Events.INCONDITIONS_NO_LONGER_HOLD, States.NESSESSARY_REWORK) 
				.permit(Events.CONTEXT_NO_LONGER_HOLD, States.PASSED_INBRANCH_CONDITIONS_UPON_REWORK);
			smc.configure(States.PASSED_OUTBRANCH_CONDITIONS_UPON_REWORK)
				.substateOf(States.SUPERSTATE_REWORK)
				.permit(Events.PROGRESS_TRIGGERED, States.SIGNALED_REWORK_BEYOND_OUTBRANCHES)
				.permit(Events.INCONDITIONS_NO_LONGER_HOLD, States.NESSESSARY_REWORK)
				.permit(Events.CONTEXT_NO_LONGER_HOLD, States.PASSED_INBRANCH_CONDITIONS_UPON_REWORK)
				.permit(Events.OUTCONDITIONS_NO_LONGER_HOLD, States.PASSED_CONTEXT_CONDITIONS_UPON_REWORK);
			smc.configure(States.SIGNALED_REWORK_BEYOND_OUTBRANCHES)
				.substateOf(States.SUPERSTATE_REWORK)
				.permit(Events.INCONDITIONS_NO_LONGER_HOLD, States.NESSESSARY_REWORK)
				//.permit(Events.CONTEXT_NO_LONGER_HOLD, States.PASSED_INBRANCH_CONDITIONS_UPON_REWORK) // context conditions are only required to hold for progressing, not once outbranches have fired
				.permit(Events.OUTCONDITIONS_NO_LONGER_HOLD, States.PASSED_CONTEXT_CONDITIONS_UPON_REWORK);
		}
		return smc;
	}
	
	public static StateMachine<States, Events> getStateMachine() {
		StateMachine<States, Events> sm = new StateMachine<>(States.AVAILABLE, getStateMachineConfig());
		return sm;
	}




}
