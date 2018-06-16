package mf.initialization;

import java.util.Map;

import mf.Execution;

public interface AttributeInitializer {
	void initialize(Execution e, Map<String,Object> t);
}
