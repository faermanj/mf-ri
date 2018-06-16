package mf.model;
import static mf.model.Attribute.link_target;
import static mf.model.Attribute.title_short;

import java.util.Map;
public class Link {
	private String text;
	private String href;


	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public Link(String text, String href) {
		super();
		this.text = text;
		this.href = href;
	}

	public Link(Map<String,Object> p) {
		this(title_short.getString(p), link_target.getString(p));
	}

	@Override
	public String toString() {
		return "Breadcrumb [text=" + text + ", href=" + href + "]";
	}
	
	

}
