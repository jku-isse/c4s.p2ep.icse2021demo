package passiveprocessengine.instance;

import com.github.oxo42.stateless4j.StateMachine;
import lombok.extern.slf4j.Slf4j;
import passiveprocessengine.persistance.neo4j.DNIStatemachineConverter;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import passiveprocessengine.definition.*;
import passiveprocessengine.definition.DecisionNodeDefinition.Events;
import passiveprocessengine.definition.DecisionNodeDefinition.States;
import passiveprocessengine.instance.IBranchInstance.BranchState;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DecisionNodeInstance extends AbstractWorkflowInstanceObject {

	@Relationship(type="SPECIFIED_BY")
	private DecisionNodeDefinition ofType;
	
	@Relationship(type="BRANCH_INSTANCE", direction=Relationship.INCOMING)
	private HashSet<IBranchInstance> inBranches = new HashSet<IBranchInstance>();
	
	@Relationship(type="BRANCH_INSTANCE", direction=Relationship.OUTGOING)
	private HashSet<IBranchInstance> outBranches = new HashSet<IBranchInstance>();

	@Property
	boolean taskCompletionConditionsFullfilled = false;
	@Property
	boolean taskActivationConditionsFullfilled = false;
	@Property
	boolean contextConditionsFullfilled = false;
	@Property
	boolean activationPropagationCompleted = false;
	@Convert(DNIStatemachineConverter.class)
	private StateMachine<DecisionNodeDefinition.States, DecisionNodeDefinition.Events> sm;

	private List<MappingReport> mappingReports = new ArrayList<>();

	public List<MappingReport> getMappingReports() {
		return mappingReports;
	}

	@Deprecated
	public DecisionNodeInstance() {
		super();
	}
	
	public DecisionNodeInstance(DecisionNodeDefinition ofType, WorkflowInstance workflow, StateMachine<DecisionNodeDefinition.States, DecisionNodeDefinition.Events> sm) {
		//super(ofType.getId()+"#"+UUID.randomUUID().toString(), workflow);
		super(ofType.getId()+"#"+workflow.getId().toString(), workflow);
		this.ofType = ofType;
		this.sm = sm;
		if (!ofType.hasExternalInBranchRules  || ofType.getInBranches().isEmpty()) {
			this.taskCompletionConditionsFullfilled = true;
			fireIfPossible(DecisionNodeDefinition.Events.INBRANCHES_FULFILLED);
		}
		if (!ofType.hasExternalContextRules) {
			this.contextConditionsFullfilled = true;
			fireIfPossible(DecisionNodeDefinition.Events.CONTEXT_FULFILLED); // only when also inbranch conditions were already fulfilled, other wise transition triggered later
		}
		if (!ofType.hasExternalOutBranchRules) {
			this.taskActivationConditionsFullfilled = true;
			 // only when also context conditions were already fulfilled, other wise transition triggered later
			fireIfPossible(Events.OUTBRANCHES_FULFILLED);
		}
	}
	
	public void addInBranches(Collection<IBranchInstance> inBranches) {
		this.inBranches.addAll(inBranches);
	}
	
	public void addOutBranches(Collection<IBranchInstance> outBranches) {
		this.outBranches.addAll(outBranches);
	}

	public Set<IBranchInstance> getOutBranches() {
		return Collections.unmodifiableSet(outBranches);
	}

	public Set<IBranchInstance> getInBranches() {
		return Collections.unmodifiableSet(inBranches);
	}
	
	public DecisionNodeDefinition getDefinition() {
		return ofType;
	}
	
	public States getState() {
		return sm.getState();
	}

	public long getInConditionsOpenUnfullfilled() {
		return inBranches.stream()
			.filter(b -> (b.getState() == IBranchInstance.BranchState.Waiting))
			.count();
	}
		
//	public void setInConditionsFullfilled() {
//		inBranches.values().stream()
//			.forEach(b -> b.setConditionsFulfilled());
//		tryInConditionsFullfilled();
//	}
	
	public Set<AbstractWorkflowInstanceObject> tryInConditionsFullfilled() {
		switch(ofType.getInBranchingType()) {
		case AND: 
			if (inBranches.stream()
					.filter(b -> b.getState()!= BranchState.Disabled) // we ignore those
					.allMatch(b -> b.getState()==BranchState.TransitionEnabled)) {
				inBranches.stream()
					.filter(b -> b.getState()!=BranchState.Disabled) // we ignore those
					.forEach(b -> b.setBranchUsedForProgress()); // not sure if we should set progress already here (wait for outbranch activiation)
				// this only fires once, when all non-disabled branches are enabled, and then they are marked for progress
				return this.setTaskCompletionConditionsFullfilled();
			}
			break;
		case OR:
			if (inBranches.stream()
					.filter(b -> b.getState()!= BranchState.Disabled) // we ignore those
					.anyMatch(b -> b.getState()==BranchState.TransitionEnabled)) {
				inBranches.stream()
					.filter(b -> b.getState()==BranchState.TransitionEnabled) // we use all ready ones
					.forEach(b -> b.setBranchUsedForProgress()); // not sure if we should set progress already here (wait for outbranch activiation)
				// this will be called several times, if branches become gradually enabled, for each new branch enabled, we need to check if data transfer has to happen
				return this.setTaskCompletionConditionsFullfilled();
			}
			break;
		case XOR:
			if (inBranches.stream()
					//.filter(b -> b.getState()!=BranchState.Disabled) // we ignore those
					.anyMatch(b -> b.getState()==BranchState.TransitionEnabled)) {
					Optional<IBranchInstance> selectedB = inBranches.stream()
						.filter(b -> b.getState()==BranchState.TransitionEnabled)
						.findAny() 
						.map(b -> {
							b.setBranchUsedForProgress();
							return b;
						});
					if (selectedB.isPresent()) {
						// we need to mark other branch tasks as: disabled (if they are available or active), or canceled otherwise 
						inBranches.stream()
						    .filter(b -> b.getState()!=BranchState.Disabled) // we ignore those	
						    .filter(b -> b.getState()!=BranchState.TransitionPassed) // filter out the branch we just tagged as used
							.map(b -> b.getTask()) // we dont change other branch states to be able to detect when another step is ready but should not have been executed
							.forEach(t -> { 
								t.signalEvent(TaskLifecycle.Events.IGNORE_FOR_PROGRESS);
//								switch(t.getLifecycleState()) {
//								case AVAILABLE:
//								case ENABLED:
//									t.setLifecycleState(TaskLifecycle.State.DISABLED);
//									break;
//								case DISABLED:
//								case CANCELED:
//									break;
//								default:
//									t.setLifecycleState(TaskLifecycle.State.CANCELED);
//									break;
//								}
							}); 
						return this.setTaskCompletionConditionsFullfilled();
					}
			}
			break;
		default:
			break;
		}
		return Collections.emptySet();
	}
	
	public Set<AbstractWorkflowInstanceObject> setTaskCompletionConditionsFullfilled() {
	// set externally from Rule Engine, or internally once in branches signal sufficient
		this.taskCompletionConditionsFullfilled = true;
		// if we have progressed, and we call this here again, two options: either wrongfully called from external, or
		// we are in an OR inflow --> then a new branch is ready
		// we are in an XOR inflow --> then an alternative is ready, that shoulds not be --> all this should be checked earlier
		// advance state machine
		fireIfPossible(Events.INBRANCHES_FULFILLED);
		return tryContextConditionsFullfilled();
	}
	
	public Set<AbstractWorkflowInstanceObject> setTaskCompletionConditionsNoLongerHold() {
		// set externally from Rule Engine, or internally once in branches signal sufficient
			this.taskCompletionConditionsFullfilled = false;
			// advance state machine
			fireIfPossible(Events.INCONDITIONS_NO_LONGER_HOLD);
			// TODO: check what needs to be done
			//return tryContextConditionsFullfilled();
			return Collections.emptySet();
		}

	public Set<AbstractWorkflowInstanceObject> tryContextConditionsFullfilled() {
		if (isContextConditionsFullfilled() || !ofType.hasExternalContextRules) {// if context conditions already evaluated to true, OR DND claims there are no external rules
			return setContextConditionsFullfilledInternal(); 
		}
		return Collections.emptySet();
	}
	
	public boolean isTaskCompletionConditionsFullfilled() {
		return taskCompletionConditionsFullfilled;
	}

	
	
//	public void setOutConditionsFullfilled() {
//		outBranches.values().stream()
//			.forEach(b -> b.setConditionsFulfilled());
//	}
	
	public boolean isActivationPropagationCompleted() {
		return activationPropagationCompleted;
	}

	public boolean isContextConditionsFullfilled() {
		return contextConditionsFullfilled;
	}

	public Set<AbstractWorkflowInstanceObject> setContextConditionsFullfilled() { 
		this.contextConditionsFullfilled = true; // in any case set context to true again
		if (sm.canFire(Events.CONTEXT_FULFILLED)) { // only when in right state, do we progress, e.g., if just to signal context ok, but in task completion not, then dont progress
			sm.fire(Events.CONTEXT_FULFILLED);
			return tryOutConditionsFullfilled(); 
		}
		return Collections.emptySet();
	}
	
	private Set<AbstractWorkflowInstanceObject> setContextConditionsFullfilledInternal() { 
		this.contextConditionsFullfilled = true; // in any case set context to true again
		fireIfPossible(Events.CONTEXT_FULFILLED); // this allows to trigger more data transfer even when we have passed context conditions previously 
		return tryOutConditionsFullfilled();
	}

	public Set<AbstractWorkflowInstanceObject> setContextConditionsNoLongerHold() {
		this.contextConditionsFullfilled = false;
		// only when in right state, i.e., when transition matters
		fireIfPossible(Events.CONTEXT_NO_LONGER_HOLD);
		// TODO: check what needs to be done
		//return tryOutConditionsFullfilled(); 
		return Collections.emptySet();
	}
	
	public Set<AbstractWorkflowInstanceObject> tryOutConditionsFullfilled() {
		switch(ofType.getOutBranchingType()) {
		case SYNC: 
			if (outBranches.stream()
					.filter(b -> b.getState()!=BranchState.Disabled) // we ignore those
					.allMatch(b -> b.getState()==BranchState.TransitionEnabled || // either we are enabled, or we are waiting for a never active activation conditions
									(b.getState()==BranchState.Waiting && !b.getBranchDefinition().hasActivationCondition()) ) ) {
				// this will only be called once, as either all steps are used for progress or none
				this.taskActivationConditionsFullfilled = true;
				fireIfPossible(Events.OUTBRANCHES_FULFILLED);
				return tryActivationPropagation();
			}
			break;
		case ASYNC:
			if (outBranches.stream()
					.filter(b -> b.getState()!=BranchState.Disabled )
					.anyMatch(b -> b.getState()==BranchState.TransitionEnabled || // either we are enabled, or we are waiting for a never active activation conditions
							(b.getState()==BranchState.Waiting && !b.getBranchDefinition().hasActivationCondition()) ) ) {
				this.taskActivationConditionsFullfilled = true;
				fireIfPossible(Events.OUTBRANCHES_FULFILLED);
				return tryActivationPropagation();
				// this enables all Steps that are ready at this state, later available steps need to be supported as well
			}
			break;
		}
		return Collections.emptySet();
	}
	
	public boolean isTaskActivationConditionsFullfilled() {
		return taskActivationConditionsFullfilled;
	}

	public Set<AbstractWorkflowInstanceObject> activateOutBranch(String branchId) {
		//outBranches.getOrDefault(branchId, dummy).setConditionsFulfilled();
		outBranches.stream()
		.filter(b -> b.getBranchDefinition().getName().equals(branchId))
		.findAny()
		.ifPresent(b -> b.setConditionsFulfilled());
		return tryOutConditionsFullfilled();
	}
	
	public Set<AbstractWorkflowInstanceObject> activateOutBranches(String... branchIds) {
		for (String id : branchIds) {
			outBranches.stream()
			.filter(b -> b.getBranchDefinition().getName().equals(id))
			.findAny()
			.ifPresent(b -> b.setConditionsFulfilled());
		}
		return tryOutConditionsFullfilled();
	}
	
	public Set<AbstractWorkflowInstanceObject> activateInBranch(String branchId) {
		//inBranches.getOrDefault(branchId, dummy).setConditionsFulfilled();
		inBranches.stream()
		.filter(b -> b.getBranchDefinition().getName().equals(branchId))
		.findAny()
		.ifPresent(b -> b.setConditionsFulfilled());
		switch(sm.getState()) {
		case AVAILABLE:
			return tryInConditionsFullfilled();
		case PASSED_INBRANCH_CONDITIONS: // we have an OR, have now additional branches active, but context not yet ok
			return tryContextConditionsFullfilled();	
		case PASSED_CONTEXT_CONDITIONS: // we have context ok, but not outbranch conditions fulfilled, typically a SYNC start
			return tryOutConditionsFullfilled();
		case PASSED_OUTBRANCH_CONDITIONS: // we have activated a few out steps already, lets see if we can map some more output data, with automated data mapping, we dont remain here, but progress to progressed out branches
			return tryActivationPropagation();
		case PROGRESSED_OUTBRANCHES: // we have also mapped some data, 
			return tryActivationPropagation();
		default:
			log.warn(String.format("Decision node %s in unsupported state %s to process inflow activation event for branch %s", this.getId(), sm.getState(), branchId));
		
		}
		
		return tryInConditionsFullfilled();
	}
	
	// used for initial instntiation of steps
//	public Map<IBranchInstance, WorkflowTask> calculatePossibleActivationPropagationUponWorkflowInstantiation() {
//		// checks whether output tasks can be activated
//		// can only activate tasks but not provide dataflow
//		if (inBranches.stream()
//			.allMatch(b -> b.getState() == BranchState.TransitionEnabled)
//			&& this.contextConditionsFullfilled) { // only if input and context conditions fulfilled
//			return outBranches.stream() // return for each enabled outbranch the respective workflow task passiveprocessengine.instance (not part of process yet)
//				.filter(b -> b.getState() == BranchState.TransitionEnabled && !b.hasTask() ) // && !b.getBranchDefinition().hasDataFlow()
//				.collect(Collectors.toMap(b -> b, b -> workflow.prepareTask(b.getBranchDefinition().getTask())));
//		}
//		return Collections.emptyMap();	
//	}
	
	public Set<AbstractWorkflowInstanceObject> tryActivationPropagation() { 
		// checks whether output tasks can be activated
		if ((sm.isInState(States.PASSED_OUTBRANCH_CONDITIONS) || sm.isInState(States.PROGRESSED_OUTBRANCHES) ))// &&  // only if input, context, and activation conditions fulfilled
			// THIS ACTIVATES ALL TASKS ON ENABLED BRANCHES, EVEN FOR XOR: the user can then choose what to execute, thus deactivation needs to happen upon activation/work output by user
			// WHILE NO USER WORKS/ACTIVATES, remaining branches might still be activated
			//outBranches.stream()
			//		.anyMatch(b -> b.getState() == BranchState.TransitionEnabled && !b.hasTask())
					//.allMatch(b -> !b.getBranchDefinition().hasDataFlow())
		    //) 
		{		
			Set<AbstractWorkflowInstanceObject> awfos =
				outBranches.stream() // return for each enabled outbranch the respective workflow task passiveprocessengine.instance (not part of process yet)
				.filter(b -> ( b.getState() == BranchState.TransitionEnabled || // either we are enabled, or we are waiting for a never active activation conditions
						(b.getState()==BranchState.Waiting && !b.getBranchDefinition().hasActivationCondition()) )
							&& !b.hasTask() ) // WE NO LONGER CHECK IF HAS DATAFLOW &&  !b.getBranchDefinition().hasDataFlow())
				.flatMap(b -> { List<AbstractWorkflowInstanceObject> awos = new ArrayList<>();
								WorkflowTask wft = workflow.instantiateTask(b.getBranchDefinition().getTask());
								awos.add(wft);
								awos.addAll(workflow.activateDecisionNodesFromTask(wft));
								consumeTaskForUnconnectedInBranch(wft);
								b.setBranchUsedForProgress(); // because we activate now, regardless whether datamapping successful
								return awos.stream();
					})
				.collect(Collectors.toSet());
			if (!awfos.isEmpty()) {
				executeSimpleDataflow(false);
				if (!areAnyBranchesExceptDisabledOrTransitionCompletedLeft()) {
					this.activationPropagationCompleted = true; //alternatively we have triggered progress, but some outbranches are still waiting and could fire later
				}
				fireIfPossible(Events.PROGRESS_TRIGGERED);
				return awfos;
			} // special case if this is the last/ending DNI
			else if (this.getDefinition().getOutBranches().size() == 0 ) {
				// then we are done with this workflow and execute any final mappings into the workflows output
				executeEndOfProessDataflow();
				fireIfPossible(Events.PROGRESS_TRIGGERED);
				return Collections.emptySet();
			}
		}
		return Collections.emptySet();	
	}
	
	private boolean areAnyBranchesExceptDisabledOrTransitionCompletedLeft() {
		return outBranches.stream() 
		.filter(b -> b.getState() != BranchState.Disabled)
		.filter(b -> b.getState() != BranchState.TransitionPassed)
		.count() > 0;
	}
	
	public void completedDataflowInvolvingActivationPropagation() {
		outBranches.stream()
			.forEach(IBranchInstance::setBranchUsedForProgress); // TODO do we need to filter for deactivated ones?
		// not necessary for outbranches as we set them via task assignment --> no we dont
		activationPropagationCompleted = true;
		fireIfPossible(Events.PROGRESS_TRIGGERED);

//		inBranches.values().stream()
//			.filter(b -> b.getState() != BranchState.Disabled)
//			.forEach(b -> b.setBranchUsedForProgress());
		// not necessary as inbranches progress set when checking inbranch conditions
	}
	
//	public boolean acceptsTaskForUnconnectedInBranch(WorkflowTask wti) {
//		// checks if any inBranch yet has not an associated Task,
//		// this check is specific to the DecisionNodeDefinition or the DecisionNodeInstance,
//		// for now we only assume one tasktype per branch, and fixed branch number
//		Optional<AbstractBranchInstance> branch = inBranches.values().stream()
//			.filter(b -> b.hasTask())
//			.filter(b -> b.bd.getTask().equals(wti.getTaskType()))
//			.findFirst();
//		return branch.isPresent();
//	}
	
	public DecisionNodeInstance consumeTaskForUnconnectedInBranch(WorkflowTask wti) {
		// checks if any inBranch yet has not an associated Task,
		// this check is specific to the DecisionNodeDefinition or the DecisionNodeInstance,
		// for now we only assume one tasktype per branch, and fixed branch number
		Optional<IBranchInstance> branch = inBranches.stream()
			.filter(b -> !b.hasTask())
			.filter(b -> b.getBranchDefinition().getTask().equals(wti.getType()))
			.findFirst();
		branch.ifPresent(b -> { b.setTask(wti); 
								this.getWorkflow().registerTaskAsInToDNI(this, wti); 
							  });
		// REQUIRES CHANGE LISTENER TO LET THE RULE ENGINE KNOW, WE UPDATEd THE BRANCH
		if (branch.isPresent())
			return this;
		else
			return null;
	}
	
	public DecisionNodeInstance consumeTaskForUnconnectedOutBranch(WorkflowTask wti) {
		Optional<IBranchInstance> branch = outBranches.stream()
				.filter(b -> b.getState() != BranchState.Disabled)
				.filter(b -> !b.hasTask())
				.filter(b -> b.getBranchDefinition().getTask().getId().equals(wti.getType().getId()))
				.findFirst();
			branch.ifPresent(b -> { 
					b.setTask(wti);
					//b.setBranchUsedForProgress(); 
					this.getWorkflow().registerTaskAsOutOfDNI(this, wti);
				});
			// REQUIRES CHANGE LISTENER TO LET THE RULE ENGINE KNOW, WE UPDATEd THE BRANCH
			if (branch.isPresent())
				return this;
			else
				return null;
	}
	
//	public void defineInBranch(String branchName, WorkflowTask wft) {
//		inBranches.put(branchName, new Branch(branchName, wft));
//	}
//	
//	public void defineOutBranch(String branchName, WorkflowTask wft) {
//		outBranches.put(branchName, new Branch(branchName, wft));
//	}
	
	public List<TaskDefinition> getTaskDefinitionsForNonDisabledOutBranchesWithUnresolvedTasks() {
		return outBranches.stream()
				.filter(b -> b.getState()!=BranchState.Disabled)				
				.filter(b -> b.getTask() == null)
				.map(b -> b.getBranchDefinition().getTask())
				.filter(td -> td != null)
				.collect(Collectors.toList());
	}

	public List<TaskDefinition> getTaskDefinitionsForFulfilledOutBranchesWithUnresolvedTasks() {
		List<TaskDefinition> tds = outBranches.stream()
				.filter(b -> b.getState()==BranchState.TransitionPassed)
				.filter(b -> b.getTask() == null)
				.map(b -> b.getBranchDefinition().getTask())
				.filter(td -> td != null)
				.collect(Collectors.toList());
		return tds;
	}
	
	public List<WorkflowTask> getNonDisabledTasksByInBranchName(String branchName) {
		return inBranches.stream()
				.filter(b -> b.getState()!=BranchState.Disabled)
				.filter(b -> b.getBranchDefinition().getName().equals(branchName))
				.filter(b -> b.hasTask())
				.map(b -> b.getTask())
				.collect(Collectors.toList());
	}
	
	public List<WorkflowTask> getNonDisabledTasksByOutBranchName(String branchName) {
		return outBranches.stream()
				.filter(b -> b.getState()!=BranchState.Disabled)
				.filter(b -> b.getBranchDefinition().getName().equals(branchName))
				.filter(b -> b.hasTask())
				.map(b -> b.getTask())
				.collect(Collectors.toList());
	}
	
	public String getInBranchIdForWorkflowTask(IWorkflowTask task) {
		Optional<IBranchInstance> branch = inBranches.stream()
				.filter(b -> b.getTask() != null)
				.filter(b -> b.getTask().equals(task))
				.findFirst();
		return branch.isPresent() ? branch.get().getBranchDefinition().getName() : null;
	}
	
	public String getOutBranchIdForWorkflowTask(IWorkflowTask task) {
		Optional<IBranchInstance> branch = outBranches.stream()
			.filter(b -> b.getTask().equals(task))
			.findFirst();
		return branch.isPresent() ? branch.get().getBranchDefinition().getName() : null;
	}

	public IBranchInstance getInBranchForWorkflowTask(IWorkflowTask task) {
		Optional<IBranchInstance> branch = inBranches.stream()
				.filter(b -> b.getTask().equals(task))
				.findFirst();
		return branch.orElse(null);
	}

	public IBranchInstance getOutBranchForWorkflowTask(IWorkflowTask task) {
		Optional<IBranchInstance> branch = outBranches.stream()
				.filter(b -> b.getTask().equals(task))
				.findFirst();
		return branch.orElse(null);
	}

	private void executeEndOfProessDataflow() {
		executeSimpleDataflow(true);
	}
	
	private void executeSimpleDataflow(boolean isEndOfProcess) {
		// this will take from each mapping only the first from and to taskids
		getDefinition().getMappings().stream()
			.forEach(m -> {
				mapSingle(m.getFrom().get(0).getFirst(), m.getFrom().get(0).getSecond(),
						m.getTo().get(0).getFirst(), m.getTo().get(0).getSecond(), isEndOfProcess);	
			});
	}
	
	private void mapSingle(String taskFrom, String roleFrom, String taskTo, String roleTo, boolean isEndOfProcess) {
		Optional<Artifact> fromArt = getArtifactFromTaskOrWorkflow(taskFrom, roleFrom);
		Optional<IWorkflowTask> toTask = getTaskOrWorkflow(taskTo);
		fromArt.ifPresent(out -> toTask.ifPresent(task -> { 
			if (isEndOfProcess && task.getAnyOneOutputByRole(roleTo) == null) {
				task.addOutput(new ArtifactOutput(out, roleTo, out.getType()));
				log.info(String.format("Data mapped from %s:%s to %s:%s", taskFrom, roleFrom, taskTo, roleTo));
			} else
			if (task.getAnyOneInputByRole(roleTo) == null) {	
				task.addInput(new ArtifactInput(out, roleTo, out.getType()));
				log.info(String.format("Data mapped from %s:%s to %s:%s", taskFrom, roleFrom, taskTo, roleTo));
			}
			} ));
	}

	private Optional<Artifact> getArtifactFromTaskOrWorkflow(String taskId, String artRoleId) {
		return Optional.ofNullable(getWorkflow().getWorkflowTasksReadonly().stream()	
		.filter(wft -> wft.getType().getId().equals(taskId) )
		.map(wft -> wft.getAnyOneOutputByRole(artRoleId))
		.filter(Objects::nonNull)
		.findAny()
		.orElseGet(()-> { if (getWorkflow().getType().getId().equals(taskId)) 
							return this.getWorkflow().getAnyOneInputByRole(artRoleId);
					  	else 
					  		return null; }  ));
	}
	
	private Optional<IWorkflowTask> getTaskOrWorkflow(String id) {
		return Optional.ofNullable(getWorkflow().getWorkflowTasksReadonly().stream()	
		.filter(wft -> wft.getType().getId().equals(id) )
		.map(wft -> (IWorkflowTask)wft)
		.findAny()
		.orElseGet(()-> { if (getWorkflow().getType().getId().equals(id)) 
							return this.getWorkflow();
					  	else 
					  		return null; }  ));
	}
	
	public Map<IWorkflowTask, ArtifactInput> executeMapping() {
		log.debug("execute mapping");
		Map<IWorkflowTask, ArtifactInput> mappedInputs = new HashMap<>();
		for (MappingDefinition m : getDefinition().getMappings()) {
			List<IWorkflowTask> fromTasks = getWorkflowTasksFromTaskDefinitionIds(m.getFrom());
			List<IWorkflowTask> toTasks = getWorkflowTasksFromTaskDefinitionIds(m.getTo());
			fromTasks = filterWrongRole(fromTasks, m.getFrom());
			toTasks = filterWrongExpectedRole(toTasks, m.getTo());
			if (toTasks.size() == 0) break;
			for (IWorkflowTask preWft : fromTasks) {
				for (ArtifactOutput ao : preWft.getOutput()) {
					if (m.getMappingType().equals(MappingDefinition.MappingType.ANY)) {
						// fitting toTask is selected (if possible)
						IWorkflowTask subWft = findBestFit(ao, toTasks);
						if (subWft != null) {
							ArtifactInput ai = executeMappingIfNotMappedPrior(preWft, ao, subWft);
							if (ai != null) {
								mappedInputs.put(subWft, ai);
							}
						}
					} else if (m.getMappingType().equals(MappingDefinition.MappingType.ALL)) {
						// every toTask is selected
						for (IWorkflowTask subWft : toTasks) {
							ArtifactInput ai = executeMappingIfNotMappedPrior(preWft, ao, subWft);
							if (ai != null) {
								mappedInputs.put(subWft, ai);
							}
						}
					}
				}
			}
		}
		return mappedInputs;
	}

	private ArtifactInput executeMappingIfNotMappedPrior(IWorkflowTask preWft, ArtifactOutput ao, IWorkflowTask subWft) {
		if (mappingReports.stream().noneMatch(r -> r.getFrom().equals(preWft.getId()) && r.getTo().equals(subWft.getId()))) {
			ArtifactInput ai = new ArtifactInput(ao);
			subWft.addInput(ai);
			mappingReports.add(new MappingReport(preWft.getId(), ao.getArtifactType(), ao.getRole(), subWft.getId(), subWft.getType().getExpectedInput()));
			return ai;
		}
		return null;
	}

	private List<IWorkflowTask> getWorkflowTasksFromTaskDefinitionIds(List<MappingDefinition.Pair<String, String>> tdIds) {
		List<IWorkflowTask> iwfts = new ArrayList<>();
		if (tdIds.stream().map(MappingDefinition.Pair::getFirst).anyMatch(id -> id.contains(this.getWorkflow().getType().getId()))) {
			iwfts.add(this.getWorkflow());
		}
		iwfts.addAll(getWorkflow().getWorkflowTasksReadonly().stream()
				.filter(wft -> tdIds.stream().map(MappingDefinition.Pair::getFirst).anyMatch(id -> id.contains(wft.getType().getId())))
				.map(wft -> (IWorkflowTask) wft)
				.collect(Collectors.toList()));
		return iwfts;

	}

	private List<IWorkflowTask> filterWrongRole(List<IWorkflowTask> wfts, List<MappingDefinition.Pair<String, String>> tdIds) {
		return wfts.stream() // only select those which have an input or output with the correct role
				.filter(wft -> wft.getInput().stream()
						.anyMatch(in -> tdIds.stream()
								.map(MappingDefinition.Pair::getSecond)
								.anyMatch(role -> role.equals(in.getRole()))) || wft.getOutput().stream()
						.anyMatch(out -> tdIds.stream()
								.map(MappingDefinition.Pair::getSecond)
								.anyMatch(role -> role.equals(out.getRole()))))
				.collect(Collectors.toList());
	}

	private List<IWorkflowTask> filterWrongExpectedRole(List<IWorkflowTask> wfts, List<MappingDefinition.Pair<String, String>> tdIds) {
		return wfts.stream() // only select those which have an input or output with the correct role
				.filter(wft -> wft.getType().getExpectedInput().keySet().stream()
						.anyMatch(in -> tdIds.stream()
								.map(MappingDefinition.Pair::getSecond)
								.anyMatch(role -> role.equals(in))) || wft.getType().getExpectedOutput().keySet().stream()
						.anyMatch(out -> tdIds.stream()
								.map(MappingDefinition.Pair::getSecond)
								.anyMatch(role -> role.equals(out))))
				.collect(Collectors.toList());
	}

	private IWorkflowTask findBestFit(ArtifactOutput ao, List<IWorkflowTask> subsequentTasks) {
		for (IWorkflowTask wft : subsequentTasks) {
			for (Map.Entry<String, ArtifactType> entry : wft.getType().getExpectedInput().entrySet()) {
				if (ao.getArtifactType() != null && entry.getValue().getArtifactType().equals(ao.getArtifactType().getArtifactType())) {
					if (entry.getKey().equals(ao.getRole())) {
						return wft; // type and role matched
					}
				}
			}
		}
		return null;
	}

	public boolean isConnected(String workflowTaskID) {
		return inBranches.stream()
			.anyMatch(b -> b.getTask().getId().equals(workflowTaskID))
				||
				outBranches.stream()
				.anyMatch(b -> b.getTask().getId().equals(workflowTaskID));

	}
	
	private boolean fireIfPossible(Events e) {
		if (sm.canFire(e)) {
			sm.fire(e);
			return true;
		} else {
			log.debug(String.format("Unable to fire event %s in state %s", e, sm.getState()));
			return false;
		}
	}

	@Override
	public String toString() {
		return "DNI ("+getState()+") + [" + ofType + ","+  workflow + ", "
				+ ", taskCompletionConditionsFullfilled=" + taskCompletionConditionsFullfilled + ", contextConditionsFullfilled=" + contextConditionsFullfilled 
				+ ", taskActivationConditionsFullfilled=" + taskActivationConditionsFullfilled + ", activationPropagationCompleted=" + activationPropagationCompleted
				+ " inBranches=" + inBranches + ", outBranches=" + outBranches  				
				+ "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DecisionNodeInstance)) return false;
		if (!super.equals(o)) return false;
		DecisionNodeInstance that = (DecisionNodeInstance) o;
		return taskCompletionConditionsFullfilled == that.taskCompletionConditionsFullfilled &&
				taskActivationConditionsFullfilled == that.taskActivationConditionsFullfilled &&
				contextConditionsFullfilled == that.contextConditionsFullfilled &&
				activationPropagationCompleted == that.activationPropagationCompleted;
	}


}
