package passiveprocessengine.persistance.neo4j;

import com.github.oxo42.stateless4j.StateMachine;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import passiveprocessengine.definition.DecisionNodeDefinition;

public class DNIStatemachineConverter implements AttributeConverter<StateMachine<DecisionNodeDefinition.States, DecisionNodeDefinition.Events>,String>{

	@Override
	public StateMachine<DecisionNodeDefinition.States, DecisionNodeDefinition.Events> toEntityAttribute(String arg0) {
		DecisionNodeDefinition.States s = DecisionNodeDefinition.States.valueOf(arg0);
		StateMachine<DecisionNodeDefinition.States, DecisionNodeDefinition.Events> sm = new StateMachine<>(s, DecisionNodeDefinition.getStateMachineConfig());
		return sm;
	}

	@Override
	public String toGraphProperty(StateMachine<DecisionNodeDefinition.States, DecisionNodeDefinition.Events> arg0) {
		return arg0.getState().toString();
	}

}
