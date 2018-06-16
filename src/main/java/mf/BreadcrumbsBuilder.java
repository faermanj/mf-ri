package mf;

import static mf.model.Attribute.breadcrumbs;
import static mf.model.Attribute.key;
import static mf.model.Attribute.parent;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import mf.model.Attribute;
import mf.model.Link;
import mf.model.Visitor;

public class BreadcrumbsBuilder extends Visitor {
	
	@Override
	protected void visit(Map<String, Object> self) {
		breadcrumbs(self);
		printResult(self);
		Map<String, Object> children = Attribute.children.getChildren(self);
		boolean has_children = children.size()>0;
		Attribute.has_children.put(self, has_children);
	}

	private void printResult(Map<String, Object> self) {
		String k = key.getString(self);
		Deque<Object> bs = breadcrumbs.getDeque(self);
		String bst = "NOCRUMB";
		if (bs != null) 
			bst = Arrays.toString(bs.toArray());
		//System.out.println("=== ["+k+"] !! ["+bst+"] ===");
	}

	private void breadcrumbs(Map<String, Object> self) {
		Map<String, Object> pt = parent.getMap(self);
		String p = key.getString(pt);
		while (pt != null){			
			printBreadcrumb(self,pt);
			pt = parent.getMap(pt);
			p = key.getString(pt);
		}
	}
	
	private void printBreadcrumb(Map<String, Object> self, Map<String, Object> current) {
		Deque<Object> bs = breadcrumbs.getDeque(self);
		if (bs == null) {
			bs = new LinkedList<Object>();
			breadcrumbs.put(self, bs);
		}
		bs.addLast(new Link(current));
		String k = key.getString(self);
		String c = key.getString(current);
		//System.out.println("=== ["+k+" += "+c+"] ===");
	}
}
