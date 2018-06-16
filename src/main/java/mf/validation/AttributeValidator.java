package mf.validation;

import java.util.Map;

import mf.Execution;

public interface AttributeValidator {
	void validate(Execution e, Map<String,Object> t, Object v);
} 
