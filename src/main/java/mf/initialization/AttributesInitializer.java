package mf.initialization;

import static mf.model.Attribute.key;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import mf.model.Attribute;
import mf.model.Visitor;

public class AttributesInitializer extends Visitor {
	
	@Override
	protected void hello() {
		execution.append("== Initialization ==");
	}

	@Override
	protected void visit(Map<String, Object> t) {
		Stream<Attribute> inits = Arrays	.stream(Attribute.values())
															.filter(a -> a.canInitialize(execution,t));
		execution.lfiner("Initializing [%s]",key.get(t));
		inits.forEach(a -> initialize(t,a, a.getInitializer()));
	}

	private void initialize(Map<String, Object> t, Attribute a, AttributeInitializer i) {
		execution.lfiner("             [%s].[%s]",key.get(t),a.name());
		i.initialize(execution, t);
	}
}
