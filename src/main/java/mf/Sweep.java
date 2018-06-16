package mf;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import mf.model.Attribute;
import mf.model.Visitor;

public class Sweep extends Visitor {
	static final Logger logger = Logger.getLogger(Sweep.class.getName());
	@Override
	protected void hello() {
		Map<String, Map<String, Object>> pruned = execution.forest.entrySet().stream().filter( (e) -> isDirty(e) ) .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		logger.info("Pruned to ["+pruned.size()+"] dirty files.");
		execution.forest = pruned;
	}

	private boolean isDirty(Entry<String, Map<String, Object>> e) {
		Map<String, Object> m = e.getValue();
		boolean contains = Attribute.dirty.contains(m);
		boolean isDirty = contains && Attribute.dirty.getBoolean(m);
		return isDirty;
	}
}
