package mf.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mf.Execution;

public class Visitor {
	protected enum Visitation {
		CONTINUE,
		STOP;
	}

	protected Execution execution;
	protected Visitation visitation = Visitation.CONTINUE;

	protected void hello() {
	}

	protected void visit(Map<String, Object> m) {
	}

	protected void goodbye() {
	}

	public static void visit(Execution e, Visitor v) {
		v.execution = e;
		v.hello();
		if (v.canContinue())
			e	.getForest()
				.forEach((k, t) -> {
					if (v.canContinue())
						v.visit(t);
				});
		if (v.canContinue()) v.goodbye();		
	}

	protected boolean canContinue() {
		return Visitation.CONTINUE.equals(visitation);
	}
	
	protected void stop(){
		visitation=Visitation.STOP;
	} 

	protected void addError(String string) {
		execution.addError(string);
	}

	protected List<Object> listWith(Map<String, Object> map, String... args) {
		List<Object> result = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (map.containsKey(arg))
				result.add(map.get(arg));
		}
		return result ;
	}
}
