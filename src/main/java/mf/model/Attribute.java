package mf.model;

import static mf.MFParser.sibling_f;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mf.Execution;
import mf.MFParser;
import mf.initialization.AttributeInitializer;
import mf.validation.AttributeValidator;

public enum Attribute implements Constants{
	// Parsed
	descriptor,
	key,
	md5,
	dirty,
	
	// Artificial Attributes
	is_category(
			(e, t) -> initIsCategory(e, t)),
	is_document(
			(e, t) -> initIsDocument(e, t)
	),
	has_facsimile(
			(e, t) -> initFacsimile(e, t)
	),
	facsimile_src,
	has_reading(
			(e, t) -> initReading(e, t)
			),
	reading_src(),
	has_alternative(
			(e,t) -> initHasAlternative(e,t)
	),
	document_reading,
	document_facsimile,
	children(
			(e, t) -> initChildren(e, t)),
	has_children,
	parent,
	
	breadcrumbs,
	nopdf(	(e, t) -> initNoPDF(e, t)),
	placements(
			(e, t) -> initPlacements(e, t)),
	home_thumb_type(
			(e, t) -> initHomeThumbType(e, t)),
	home_thumb_src(
			(e, t) -> initHomeThumbSrc(e, t)),
	home_thumb_height(
			(e, t) -> initHomeThumbHeight(e, t)),

	home_thumb_width(
			(e, t) -> initHomeThumbWidth(e, t)),
	home_hover_src(
			(e, t) -> initHomeHoverSrc(e, t)),

	category_thumb_type(
			(e, t) -> initCategoryThumbType(e, t)),
	category_thumb_src(
			(e, t) -> initCategoryThumbSrc(e, t)),
	category_thumb_height(
			(e, t) -> initCategoryThumbHeight(e, t)),

	category_thumb_width(
			(e, t) -> initCategoryThumbWidth(e, t)),
	category_hover_src(
			(e, t) -> initCategoryHoverSrc(e, t)),
	box_size(
			(e, t) -> initBoxSize(e, t)),
	link_target(
			(e, t) -> initLinkTarget(e, t)),
	
	// Natural Attributes
	categories(
			(e, t, v) -> isValidCategories(e, t, v)),
	description_short(
			(e, t) -> initDescriptionShort(e, t)),
	description(
		(e, t) -> initDescription(e, t)),
	description_thumb(),
	has_description_thumb(
		(e,t) -> initDescriptionThumb(e,t)
	),
	description_thumb_src(),
	home((e, t, v) -> isValidHome(e, t, v)),
	head_class,
	template_key,
	destaque,
	keywords, 
	menu,
	menuList,
	title,
	title_short( (e, t) -> initTitleShort(e, t) ),
	publication,
	publication_type,
	timeline,
	year,
	month,
	day,
	obs;

	private AttributeValidator validator;
	private AttributeInitializer initializer;

	private Attribute() {
		this(null, null);
	}


	private static void initHasAlternative(Execution e, Map<String, Object> t) {
		Boolean h_fac = has_facsimile.getBoolean(t);
		Boolean h_read = has_reading.getBoolean(t);
		Boolean h_alt = h_fac && h_read;
		has_alternative.put(t, h_alt);
	}


	private static void initDescriptionThumb(Execution e, Map<String, Object> t) {
		File expected = sibling_f(t, "description_T2", "jpg");
		if(expected.exists()){
			has_description_thumb.put(t, true);
			description_thumb_src.put(t, expected.getName());
		}
	}


	private static void initDescription(Execution e, Map<String, Object> t) {
		String desc = description.getString(t);
		if (desc != null) {			
			String html = MFParser.md2html(desc);
			description.put(t, html);
		}
	}

	private static void initReading(Execution e, Map<String, Object> t) {
		String k = key.getString(t);
		File expected = MFParser.sibling_f(t, "TX" , "pdf");
		boolean hasRead = expected != null && expected.exists();
		has_reading.put(t, hasRead);
		if(hasRead){
			String read_src = getURL(e,MFParser.sibling_s(t, "" , "html"));
			reading_src.put(t, read_src);
			String doc_read = getURL(e,MFParser.sibling_s(t, "TX" , "pdf"));
			document_reading.put(t, doc_read);
		}
	}

	private static String getURL(Execution e,String str) {
		return str.replaceAll(" ","%20");
	}

	private static void initFacsimile(Execution e, Map<String, Object> t) {		
		File expected = MFParser.sibling_f(t, "" , "pdf");
		boolean hasFac = expected != null && expected.exists();
		has_facsimile.put(t, hasFac);
		if(hasFac){
			String fac_src = getURL(e,MFParser.sibling_s(t, "" , "html"));
			facsimile_src.put(t, fac_src);
			String doc_fac = getURL(e,MFParser.sibling_s(t, "" , "pdf"));
			document_facsimile.put(t, doc_fac);
		}
	}

	private static void initIsDocument(Execution e, Map<String, Object> t) {
		boolean isCategory = is_category.getBoolean(t);
		is_document.put(t, ! isCategory);
	}

	private static void initPlacements(Execution e, Map<String, Object> t) {
		Map<String,String> ps = new HashMap<>();
		List<String> cs = categories.getStrings(t);
		List<String> ds = destaque.getStrings(t);
		if (cs!=null) for (int i = 0; i < cs.size(); i++) {
			String category = cs.get(i);
			String slot;
			if (i < ds.size()){
				slot = ds.get(i);
			} else if(! ds.isEmpty()){
				slot = ds.get(ds.size()-1);
			} else {
				slot = D01;
			}
			ps.put(category,slot);
		}
		placements.put(t, ps);
	}

	private List<String> getStrings(Map<String, Object> t) {
		List<String> result = null;
		String str = getString(t);
		if(str != null){
			result = toList(str);
		}
		return result;
	}

	private static List<String> toList(String str) {
		List<String> result;
		String[] split = str.split(",");
		Stream<String> result_s = Arrays.asList(split)
										.stream()
										.map(s -> s.trim())
										.filter(s -> ! s.isEmpty());
		result = result_s.collect(Collectors.toList());
		return result;
	}

	private static void  initIsCategory(Execution e, Map<String, Object> t) {
		Path desc = descriptor.getPath(t);
		is_category.put(t, MFParser.isCategory(desc));
	}


	private static void initChildren(Execution e, Map<String, Object> t) {
		t.put("children", new HashMap<String, Map<String, Object>>());
	}

	private static void isValidHome(Execution e, Map<String, Object> t, Object v) {		
		String h = home.getString(t);
		boolean valid = h.toString()
							.matches("D\\d\\d");
		if (!valid)
			e.addWarning("Invalid home position [%s]",  h);
	}

	private static void initCategoryHoverSrc(Execution e, Map<String, Object> t) {
		String sibling_s = getURL(e,MFParser.sibling_s(t, "T3_MO", "jpg"));
		category_hover_src.put(t, sibling_s);
	}

	private static void initCategoryThumbWidth(Execution e, Map<String, Object> t) {
		category_thumb_width.put(t, T2_WIDTH);
	}

	private static void initCategoryThumbHeight(Execution e, Map<String, Object> t) {
		category_thumb_height.put(t, T_HEIGHT);
	}

	private static void initCategoryThumbSrc(Execution e, Map<String, Object> t) {
		String expected = MFParser.sibling_s(t, T3, "jpg");
		String url = getURL(e,expected);
		category_thumb_src.put(t, url);
	}

	private static void initCategoryThumbType(Execution e, Map<String, Object> t) {
		category_thumb_type.put(t, T3);
	}

	private static void initHomeThumbWidth(Execution e, Map<String, Object> t) {
		String htt = home_thumb_type.getString(t);
		if (htt != null) {
			String width = "";
			switch (htt) {
			case T1:
				width = T1_WIDTH;
				break;
			case T2:
				width = T2_WIDTH;
				break;
			case T3:
				width = T3_WIDTH;
				break;
			}
			home_thumb_width.put(t, width);
		}
	}

	private static void initHomeThumbHeight(Execution e, Map<String, Object> t) {
		home_thumb_height.put(t, T_HEIGHT);
	}

	private static final int DESCRIPTION_SHORT_MAX = 350;

	private static void initDescriptionShort(Execution e, Map<String, Object> t) {
		String desc_short = description_short.getString(t);
		if(desc_short == null){
			String desc = ""+description.getString(t);
			desc_short = desc.substring(0, Math.min(desc.length(), DESCRIPTION_SHORT_MAX));
		}
		String html = MFParser.md2html(desc_short);
		description_short.put(t, html);
	}

	public String getString(Map<String, Object> t) {
		if (t == null) return null;
		String attr = this.name();
		Object result = t.get(attr);
		return (String) result;
	}

	private static void initHomeHoverSrc(Execution e, Map<String, Object> t) {
		String htt = home_thumb_type.getString(t);
		if (htt != null) {
			String suffix = htt + "_MO";
			String hhs = MFParser.sibling_s(t, suffix, "jpg");
			String url = getURL(e,hhs);

			home_hover_src.put(t, url);
		}
	}

	private static void initNoPDF(Execution e, Map<String, Object> t) {
		Path filePath = descriptor.getPath(t);
		Path parent = filePath.getParent();
		Path nopdf_p = parent.resolve(".nopdf");
		File nopdf_f = nopdf_p.toFile();
		Boolean isnopdf = nopdf_f.exists();
		nopdf.put(t, isnopdf.toString());
	}

	private static void initHomeThumbSrc(Execution e, Map<String, Object> t) {
		String htt = home_thumb_type.getString(t);
		String expectedThumbSrc = MFParser.sibling_s(t, htt, "jpg");
		String url = getURL(e,expectedThumbSrc);
		home_thumb_src.put(t, url);
	}

	private void put(Map<String, Object> t, String value) {
		t.put(this.name(), value);
	}

	public Object get(Map<String, Object> t) {
		return t.get(this.name());
	}

	private static void initTitleShort(Execution e, Map<String, Object> t) {
		String titleShort = title_short.getString(t);
		if (titleShort == null) {
			titleShort = "" + title.get(t);
		}
		title_short.put(t, titleShort);
	}

	private static void initLinkTarget(Execution e, Map<String, Object> t) {
		link_target.put(t, key.getString(t) + ".html");
	}

	private static void initBoxSize(Execution e, Map<String, Object> t) {
		String bs = null;
		String htt = home_thumb_type.getString(t);
		if (htt != null) {
			switch (htt) {
			case T0:
				bs = SMALL;
				break;
			case T1:
				bs = LARGE;
				break;
			case T2:
				bs = MEDIUM;
				break;
			case T3:
				bs = SMALL;
				break;
			default:
				throw new RuntimeException("Unknown home thumb type");
			}
			box_size.putString(t, bs);
		}
	}

	public void putString(Map<String, Object> t, String bs) {
		t.put(this.name(), bs);
	}

	private static void initHomeThumbType(Execution e, Map<String, Object> t) {
		String htt = "T3";
		if (MFParser.exists(t, "T1", "jpg")) {
			htt = "T1";
		} else if (MFParser.exists(t, "T2", "jpg")) {
			htt = "T2";
		}
		home_thumb_type.put(t, htt);
	}

	private Attribute(AttributeValidator validator) {
		this(null, validator);
	}

	private Attribute(AttributeInitializer initializer) {
		this(initializer, null);
	}

	private Attribute(AttributeInitializer initializer, AttributeValidator validator) {
		this.initializer = initializer;
		this.validator = validator;
	}

	public boolean hasInitializer() {
		return this.initializer != null;
	}

	public AttributeValidator getValidator() {
		return validator;
	}

	public void setValidator(AttributeValidator validator) {
		this.validator = validator;
	}

	private static void isValidCategories(Execution ex, Map<String, Object> t, Object value) {
		if (value == null) return;
		List<String> cs = toList(value.toString());
		String k = key.getString(t);
		cs.stream().filter(c -> ! ex.containsKey(c)).forEach(c -> ex.addError("Unknown category [%s] at [%s]",c,k));		
	}

	static final Pattern sequentialName = Pattern.compile("D\\d?\\d?");

	private static boolean descriptorExist(Execution ex, String category) {
		Path resolve = ex	.getInputDir()
							.resolve("_categories")
							.resolve(category + ".mf");
		boolean exists = resolve.toFile()
								.exists();
		return exists;
	}

	public AttributeInitializer getInitializer() {
		return initializer;
	}

	public void setInitializer(AttributeInitializer initializer) {
		this.initializer = initializer;
	}

	public boolean canInitialize(Execution e,Map<String, Object> t) {
		boolean contains = this.contains(t);
		if(contains && this != description && this != description_short){
			e.addWarning("             [%s]!?[%s]",key.get(t),this.name());
		}
		boolean hasInitializer = this.initializer != null;
		boolean canInit =  hasInitializer;
		return canInit;
	}

	public boolean contains(Map<String, Object> t) {
		boolean containsKey = t.containsKey(this.name());
		return containsKey;
	}

	public Path getPath(Map<String, Object> t) {
		return (Path) t.get(this.name());
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getStringMap(Map<String, Object> t) {
		return (Map<String, String>) t.get(this.name());
	}

	@SuppressWarnings("unchecked")
	public Map<String,Object> getChildren(Map<String, Object> parent) {
		return (Map<String, Object>) parent.get(this.name());
	}

	public boolean getBoolean(Map<String, Object> t) {
		Object value = t.get(this.name());
		if (value != null && value instanceof Boolean){
			return ((Boolean) value).booleanValue();
		}else return false;
	}

	public void put(Map<String, Object> result, Object obj) {
		result.put(this.name(), obj);
	}

	@SuppressWarnings("unchecked")
	public List<Object> getList(Map<String, Object> m) {
		return (List<Object>) m.get(this.name());
	}

	public Map<String, Object> getMap(Map<String, Object> m) {
		Map<String, Object> result=(Map<String, Object>) m.get(this.name());
		return result;
	}

	public Queue<Object> getQueue(Map<String, Object> m) {
		return (Queue<Object>) get(m,this);
	}
	

	private Object get(Map<String, Object> m, Attribute attr) {
		return m.get(attr.name());
	}


	@SuppressWarnings("unchecked")
	public Deque<Object> getDeque(Map<String, Object> m) {
		return (Deque<Object>) get(m,this);
	}
	
	public Set<Object> getSet(Map<String, Object> m) {
		@SuppressWarnings("unchecked")
		Set<Object> result = (Set<Object>) get(m,this);
		if (result == null){
			result = new TreeSet<>();
			put(m,result);
		}
		return result;
	}



	public void addToSet(Map<String, Object> m, Object o) {
		@SuppressWarnings("unchecked")
		Set<Object> os = (Set<Object>) this.get(m);		
		if(os == null){
			os = new HashSet<>();
			this.put(m, os);
		}
		os.add(o);		
	}


	

}