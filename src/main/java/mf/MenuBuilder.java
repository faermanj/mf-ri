package mf;

import static mf.model.Attribute.menu;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import mf.model.Link;
import mf.model.Visitor;

public class MenuBuilder extends Visitor {
	SortedMap<String, Link> mmap = new TreeMap<>();
	
	@Override
	protected void visit(Map<String, Object> m) {
		if (menu.contains(m)){
			String value = menu.getString(m);
			mmap.put(value,new Link(m));
		}
	}
	
	@Override
	protected void goodbye() {
		execution.globals.put("menuList", mmap.values());
	}
}
