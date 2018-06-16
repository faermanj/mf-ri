package mf;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mf.model.Visitor;

public class MergeGlobals extends Visitor {
	@Override
	protected void hello() {
		//merge(execution.home);
	}

	@Override
	protected void visit(Map<String, Object> m) {
		merge(m);
	}

	private void merge(Map<String, Object> m) {
		Set<Entry<String, Object>> globalEntries = execution.globals.entrySet();
		for (Entry<String, Object> entry : globalEntries) {
			String gk = entry.getKey();
			if(!m.containsKey(gk)){
				Object gv = entry.getValue();
				m.put(gk, gv);
			}
		}
	}
}
