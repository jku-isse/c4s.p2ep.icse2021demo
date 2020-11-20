package passiveprocessengine.instance;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import passiveprocessengine.definition.*;
import passiveprocessengine.persistance.neo4j.BasicServices.ArtifactTypeService;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@NodeEntity
@Slf4j
public class WorkflowInstance extends AbstractWorkflowInstanceObject implements java.io.Serializable, IWorkflowTask {

    /**
     *
     */
    private static final long serialVersionUID = -5679379946036530198L;

    @Relationship(type="SPECIFIED_BY")
    private WorkflowDefinition workflowDefinition;

    @Relationship(type="DECISIONNODE_INSTANCES")
    private Set<DecisionNodeInstance> dnInst = new HashSet<>();
    @Relationship(type="TASK_INSTANCES")
    private Set<WorkflowTask> taskInst = new HashSet<>();
    //private Set<DecisionNodeDefinition> dnDefs = new HashSet<DecisionNodeDefinition>(); // available via workflowdefinition

    @Properties
    private Map<String,String> wfProps = new HashMap<>();

    private transient Map<WorkflowTask, DecisionNodeInstance> taskIntoDNI = new HashMap<>();
    private transient Map<WorkflowTask, DecisionNodeInstance> taskOutOfDNI = new HashMap<>();

    private transient TaskStateTransitionEventPublisher pub;

    List<ArtifactOutput> output = new ArrayList<>();
    List<ArtifactInput> input = new ArrayList<>();

    @Deprecated
    public WorkflowInstance() {
        super();
    }

    public WorkflowInstance(String workflowId, WorkflowDefinition workflowDefinition, TaskStateTransitionEventPublisher pub) {
        super(workflowId, null);
        this.pub = pub;
        this.workflowDefinition = workflowDefinition;
    }

    public void initAfterPersistance(TaskStateTransitionEventPublisher pub, WorkflowDefinition wfd) {
        this.pub = pub; 
        this.workflowDefinition = wfd;
        // // workflow passiveprocessengine.instance uplink and task dnd mappings
        taskInst.stream().forEach(t -> {
            t.setWorkflow(this);
            taskIntoDNI.put(t, null);
            taskOutOfDNI.put(t, null);}
        );
        dnInst.stream().forEach(dni -> {
            dni.setWorkflow(this);
            dni.getInBranches().stream().forEach(b -> {
                if (b.hasTask()) {
                    taskIntoDNI.put(b.getTask(), dni);
                }
            });
            dni.getOutBranches().stream().forEach(b -> {
                if (b.hasTask()) {
                    taskOutOfDNI.put(b.getTask(), dni);
                }
            });
        });

    }

    @Override
    public String toString() {
        return "[WorkflowInstance: " + id + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowInstance)) return false;
        if (!super.equals(o)) return false;
        WorkflowInstance that = (WorkflowInstance) o;
        return Objects.equals(workflowDefinition, that.workflowDefinition) &&
                Objects.equals(dnInst, that.dnInst) &&
                Objects.equals(taskInst, that.taskInst) &&
                Objects.equals(wfProps, that.wfProps) &&
                Objects.equals(taskIntoDNI, that.taskIntoDNI) &&
                Objects.equals(taskOutOfDNI, that.taskOutOfDNI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), workflowDefinition, dnInst, taskInst, wfProps, taskIntoDNI, taskOutOfDNI);
    }

    public WorkflowTask prepareTask(TaskDefinition td) {
        WorkflowTask tf;
        if (td instanceof WorkflowWrapperTaskDefinition) {
            WorkflowWrapperTaskDefinition wwtd = (WorkflowWrapperTaskDefinition) td;
            tf = new WorkflowWrapperTaskInstance(td.getId()+"#"+getId(), this, TaskLifecycle.buildStatemachine(), pub, wwtd.getSubWfdId());
        // } else if (td instanceof NoOpTaskDefinition) {
        } else {
            tf = new WorkflowTask(td.getId() + "#" + getId(), this, TaskLifecycle.buildStatemachine(), pub);
        }
        tf.setType(td);
        return tf;
    }

    public WorkflowTask instantiateTask(TaskDefinition td) {
        if (taskInst.stream().noneMatch(t -> t.getType().getId().equals(td.getId()))) {
            WorkflowTask tf = prepareTask(td);
            taskInst.add(tf);
            taskIntoDNI.put(tf, null);
            taskOutOfDNI.put(tf, null);
            return tf;
        }
        return null;
    }


    public WorkflowDefinition getType() {
        return workflowDefinition;
    }

    @Override
    public TaskLifecycle.State getLifecycleState() {
        log.warn("NOT IMPLEMENTED");
        return null;
    }

    @Override
    public Set<AbstractWorkflowInstanceObject> signalEvent(TaskLifecycle.Events event) {
        log.warn("NOT IMPLEMENTED");
        return Collections.emptySet();
    }

    @Override
    public List<ArtifactOutput> getOutput() {
        return Collections.unmodifiableList(output);
    }

    @Override
    public boolean removeOutput(ArtifactOutput ao) {
        return output.remove(ao);
    }

    @Override
    public Set<AbstractWorkflowInstanceObject> addOutput(ArtifactOutput ao) {
        output.add(ao);
        return Collections.emptySet();
    }

    @Override
    public List<ArtifactInput> getInput() {
        return Collections.unmodifiableList(input);
    }

    public boolean containsInput(String id) {
        return contains(input, id);
    }

    public boolean containsOutput(String id) {
        return contains(output, id);
    }

    private <T extends ArtifactIO> boolean contains(List<T> io, String id) {
        return io.stream()
                .filter(x -> x.getArtifact() instanceof ArtifactWrapper)
                .anyMatch(x -> x.getArtifact().getId().contains(id));
    }

    public boolean containsInputOrOutput(String id) {
        return containsInput(id) || containsOutput(id);
    }

    @Override
    public boolean removeInput(ArtifactInput ai) {
        return input.remove(ai);
    }

    @Override
    public void addInput(ArtifactInput ai) {
        input.add(ai);
    }

    private Optional<WorkflowTask> getFirstTaskByType(TaskDefinition td) {
    	return this.taskInst.stream()
    			.filter(wft -> wft.getType().equals(td))
    			.findFirst();
    }
    
    public List<WorkflowTask> getWorkflowTasksReadonly() {
        return this.getType().getWorkflowTaskDefinitions().stream()
        	.map(td -> getFirstTaskByType(td))
        	.filter(optWFT -> !optWFT.isEmpty())
        	.map(optWFT -> optWFT.get())
        	.collect(Collectors.toList());
 //   	return Collections.unmodifiableSet(this.taskInst);
    }

    public IWorkflowTask getWorkflowTask(String id) {
        return this.taskInst.stream()
                .filter(x -> x.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Set<DecisionNodeInstance> getDecisionNodeInstancesReadonly() {
        return Collections.unmodifiableSet(this.dnInst);
    }

    public DecisionNodeInstance getDecisionNodeInstance(String id) {
        return this.dnInst.stream()
                .filter(x -> x.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
//	public void setWorkflowDefinitionId(WorkflowDefinition workflowDefinition) {
//		this.workflowDefinition = workflowDefinition;
//	}

    public List<AbstractWorkflowInstanceObject> enableWorkflowTasksAndDecisionNodes() {
        // enable tasks only via DNIs
        // check which dnd to enable: for now those without in-branch
        return this.workflowDefinition.getDecisionNodeDefinitions().stream()
                .filter(dnd -> dnd.getInBranches().size() == 0)
                .map(dnd -> dnd.createInstance(this))
                .flatMap(dni -> { registerDecisionNodeInstance(dni);
                    List<AbstractWorkflowInstanceObject> awos = new ArrayList<AbstractWorkflowInstanceObject>();
                    awos.add(dni);
                    //awos.addAll(activateTasksFromDecisionNode(dni));
                    awos.addAll(dni.tryContextConditionsFullfilled());
                    return awos.stream();
                })
                .collect(Collectors.toList());
    }

//    private Set<AbstractWorkflowInstanceObject> activateTasksFromDecisionNode(DecisionNodeInstance dni) {
//    	return dni.calculatePossibleActivationPropagationUponWorkflowInstantiation().entrySet().stream()
//               // .filter(e -> !e.getKey().getBranchDefinition().hasDataFlow())
//                .flatMap(e -> { taskInst.add(e.getValue()); // add to workflow managed elements
//                    e.getKey().setTask(e.getValue()); // set to branch
//                    registerTaskAsOutOfDNI(dni, e.getValue());
//                    // TODO: set responsible engineer
//                    // propagate beyond this initial task(s) : other Decision Nodes
//                    List<AbstractWorkflowInstanceObject> awos = new ArrayList<AbstractWorkflowInstanceObject>();
//                    awos.add(e.getValue());
//                    awos.addAll(activateDecisionNodesFromTask(e.getValue()));
//                    return awos.stream();
//                })
//                .collect(Collectors.toSet());
//    }

    public Set<AbstractWorkflowInstanceObject> activateDecisionNodesFromTask(WorkflowTask wft) {
        // AIMING TO ACTIVATE DECISION NODES DOWNSTREAM: i.e., which decision nodes now become active as this task matches some of their inbranches
        Set<AbstractWorkflowInstanceObject> awos = new HashSet<AbstractWorkflowInstanceObject>();
        // only for WFT that have no outbranch connection
        if (this.taskIntoDNI.get(wft) != null) {
            return awos;
        }
        // find any dni or dnd with an accepting in branch
        Set<DecisionNodeInstance> dnis = dnInst.stream()
                .map(d -> d.consumeTaskForUnconnectedInBranch(wft)) // return the decision nodes that have a branch accepted the artifact
                .filter(dni -> dni != null)  // filter out null values
                .distinct() // unique decision nodes (should not happen anyway as each dni should only have one single inBranch per Tasktype
                .map(dni -> consumePrematureTasksForUnconnectedOutBranch(dni))
                //.map(dni -> this.registerTaskAsInToDNI(dni, wft))
                .collect(Collectors.toSet());
        if (dnis.size() > 1) {
            log.error("Activated more than one decision node passiveprocessengine.instance from DNIs with task: "+ wft);
        }
        // a single DND should only be have a singel DNI per process (no loops supported yet)
        // to make sure, now we check which DND we could instantiate and take only these, that have no DNIs already active
        awos.addAll(dnis);
        Set<DecisionNodeInstance> dnis2 = this.workflowDefinition.getDecisionNodeDefinitions().stream()
                .filter(dnd -> dnd.acceptsWorkflowTaskForInBranches(wft))
                .filter(dnd -> { return dnis.stream()
                        .noneMatch(dni -> dni.getDefinition().getId().equals(dnd.getId())); // TODO: FIXME: filter dni of same type, needed when we have parallel in, otherwise we would generate another passiveprocessengine.instance of this DNI whenever there second task becomes created
                })
                .map(dnd -> { DecisionNodeInstance dni = dnd.createInstance(this); // we would need to check for any task that already exists (prematurely executed)
                    registerDecisionNodeInstance(dni);
                    return dni;
                })
                .map(dni -> consumePrematureTasksForUnconnectedOutBranch(dni))
                .map(dni -> dni.consumeTaskForUnconnectedInBranch(wft))
                .filter(dni -> dni != null)
                //.map(dni -> this.registerTaskAsInToDNI(dni, wft))
                .collect(Collectors.toSet());
        if (dnis2.size() > 1) {
            log.error("Activated more than one decision node passiveprocessengine.instance from DNDs with task: "+ wft);
        }
        awos.addAll(dnis2);
        // In any case, not just if empty!!! ///////if awos is empty, it means this task didn;t activate any decision nodes, thus check for output
        //if (awos.isEmpty()) {
        Optional<DecisionNodeInstance> optDNI = consumePrematureTaskForUnconnectedOutBranch(wft);
        optDNI.orElseGet( () -> { if (awos.isEmpty())
        { log.warn("Premature task not usable for any DNI-branch: "+ wft); }
            return null;} );
        optDNI.ifPresent( dni -> awos.add(dni) );
        //}
        return awos;
    }

    public void registerDecisionNodeInstance(DecisionNodeInstance dni) {
        dnInst.add(dni);
    }

    protected void registerTaskAsInToDNI(DecisionNodeInstance dni, WorkflowTask wt) {
        taskIntoDNI.put(wt, dni);
        //return dni;
    }

    protected void registerTaskAsOutOfDNI(DecisionNodeInstance dni, WorkflowTask wt) {
        taskOutOfDNI.put(wt, dni);
        //return dni;
    }

    private Optional<DecisionNodeInstance> consumePrematureTaskForUnconnectedOutBranch(WorkflowTask wt) {
        return dnInst.stream()
                .map(d -> d.consumeTaskForUnconnectedOutBranch(wt)) // return the decision nodes that have a branch accepted the artifact
                .filter(dni -> dni != null)  // filter out null values
                .distinct() // unique decision nodes (should not happen anyway as each dni should only have one single inBranch per Tasktype
                .findAny();
    }

    private DecisionNodeInstance consumePrematureTasksForUnconnectedOutBranch(DecisionNodeInstance dni) {
        // determine which tasks can be used for this DecisionNode passiveprocessengine.instance, and map accordingly
        taskOutOfDNI.entrySet().stream()
                .filter( tuple -> tuple.getValue() == null) // only tasks that are not yet assigned to an outbranch
                .forEach(tuple -> { if (dni.consumeTaskForUnconnectedOutBranch(tuple.getKey()) != null) {
                    tuple.setValue(dni); // basically registeringTaskAsOutOfDNI
                }
                });
//			.map( tuple -> tuple.getKey())
//			.map(t -> dni.consumeTaskForUnconnectedOutBranch(t) == null ? null : t) // doing this as a filter would be ugly as not sideeffect free
//			.collect(Collectors.toSet());
        return dni;
    }

    public WorkflowTask getNewPlaceHolderTask(TaskDefinition td) {
        WorkflowTask placeHolder = new PlaceHolderTask(this, td, "PLACEHOLDER#"+UUID.randomUUID().toString());
        return placeHolder;
    }

    public String addOrReplaceProperty(String key, String value) {
        return wfProps.put(key, value);
    }

    public String getEntry(String key) {
        return wfProps.get(key);
    }

    public Set<Entry<String,String>> getPropertiesReadOnly() {
        return Collections.unmodifiableSet(wfProps.entrySet());
    }

    // return the last decision node in the workflow that finishes the workflow
    public DecisionNodeInstance getFinish() {
        throw new RuntimeException("not implemented");
    }

    // return the first decision node in the workflow that starts the workflow
    public DecisionNodeInstance getKickOff() {
        throw new RuntimeException("not implemented");
    }

 // considers only artifacts that are not removedAtOrigin
 	@Override
 	public Artifact getAnyOneInputByType(String artifactType) {
 		Optional<ArtifactInput> opArt = getInput().stream()
 			.filter(input -> input.getArtifact().getType().getArtifactType().equals(artifactType))
 			.filter(io -> !io.getArtifact().isRemovedAtOrigin())
 			.findAny();
 		if (opArt.isPresent())
 			return opArt.get().getArtifact();
 		else 
 			return null; 
 	}
 	
 	// considers only artifacts that are not removedAtOrigin	
 	@Override
 	public Artifact getAnyOneInputByRole(String inputRole) {
 		Optional<ArtifactInput> opArt = getInput().stream()
 				.filter(input -> input.getRole().equals(inputRole))
 				.filter(io -> !io.getArtifact().isRemovedAtOrigin())
 				.findAny();
 			if (opArt.isPresent())
 				return opArt.get().getArtifact();
 			else 
 				return null;
 	}
 	
 	// considers only artifacts that are not removedAtOrigin
 	@Override
 	public Artifact getAnyOneOutputByType(String artifactType) {
 		Optional<ArtifactOutput> opArt = getOutput().stream()
 			.filter(io -> io.getArtifact().getType().getArtifactType().equals(artifactType))
 			.filter(io -> !io.getArtifact().isRemovedAtOrigin())
 			.findAny();
 		if (opArt.isPresent())
 			return opArt.get().getArtifact();
 		else 
 			return null; 
 	}
 	
 	// considers only artifacts that are not removedAtOrigin
 	@Override
 	public Artifact getAnyOneOutputByRole(String outputRole) {
 		Optional<ArtifactOutput> opArt = getOutput().stream()
 				.filter(io -> io.getRole().equals(outputRole))
 				.filter(io -> !io.getArtifact().isRemovedAtOrigin())
 				.findAny();
 			if (opArt.isPresent())
 				return opArt.get().getArtifact();
 			else 
 				return null;
 	}
 	

}
