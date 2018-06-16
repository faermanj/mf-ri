package mf.validation;

import static mf.model.Attribute.key;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mf.model.Attribute;
import mf.model.Visitor;
public class AttributesValidator extends Visitor {

	@SuppressWarnings("serial")
	public static Set<String> known = new HashSet<String>() {
		{
			Stream<String> map = Arrays	.asList(Attribute.values())
										.stream()
										.map(val -> val.toString());
			addAll(map.collect(Collectors.toList()));
		}
	};

	protected void visit(Map<String,Object> t) {
		t.forEach((k, v) -> {
			if (isUnknown(k)) {
				addError("Propriedade desconhecido [" + k + "] no descritor [" + key.get(t) + "]");
				return;
			} else {
				Attribute valueOf = Attribute.valueOf(k);
				AttributeValidator validator = valueOf.getValidator();
				if (validator != null)
					validator.validate(execution, t, v);
			}

		});
	}

	private boolean isUnknown(String k) {
		return !known.contains(k);
	}

}
