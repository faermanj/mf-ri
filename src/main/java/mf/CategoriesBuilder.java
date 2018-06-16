package mf;

import static mf.model.Attribute.children;
import static mf.model.Attribute.parent;
import static mf.model.Attribute.placements;

import java.util.Map;

import mf.model.Visitor;

public class CategoriesBuilder extends Visitor {

	@Override
	protected void visit(Map<String, Object> m) {
		Map<String, String> ps = placements.getStringMap(m);		
		ps.forEach((categoryKey,slot) -> place(slot,categoryKey,m));
	}

	private void place(String slot, String categoryKey, Map<String, Object> child) {
		if(execution.containsKey(categoryKey)){
			Map<String, Object> category = execution.get(categoryKey);
			place(slot,category,child);
		}else execution.addWarning("Uknown attribute ["+categoryKey+"]" );
	}

	private void place(String slot, Map<String, Object> category, Map<String, Object> child) {
		Map<String,Object> pt = parent.getMap(child);
		if (pt == null){
			parent.put(child,category);
		}
				
		Map<String,Object> kids = children.getChildren(category);
		kids.put(slot,child);
	}

}
