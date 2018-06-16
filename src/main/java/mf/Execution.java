package mf;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static mf.model.Attribute.is_category;
import static mf.model.Attribute.is_document;
import static mf.model.Attribute.key;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

import mf.initialization.AttributesInitializer;
import mf.model.Constants;
import mf.model.Visitor;
import mf.rendering.MustacheRenderer;
import mf.validation.AttributesValidator;
import mf.validation.FacSimileValidator;

public class Execution implements Constants {
	static final MustacheRenderer renderer = new MustacheRenderer();
	
	Deque<String> messages = new LinkedBlockingDeque<>(20);
	Deque<String> updates  = new LinkedBlockingDeque<>(20);
	
	
	Map<String, Map<String, Object>> forest = new HashMap<>();
	Map<String, Object> globals = new HashMap<>();
	Map<String, Object> homeMap = mkHome();
	

	private HashMap<String, Object> mkHome() {
		HashMap<String, Object> home = new HashMap<>();
		home.put("generation_date", df.format(new Date()));
		return home;
	}

	AtomicInteger ingested = new AtomicInteger();
	AtomicInteger replaced = new AtomicInteger();
	AtomicInteger kept = new AtomicInteger();

	private StringBuilder summary = new StringBuilder();

	@SuppressWarnings("serial")
	static final List<Visitor> visitors = new ArrayList<Visitor>() {
		{
			add(new AttributesInitializer());
			add(new FacSimileValidator());
			add(new AttributesValidator());
			add(new CategoriesBuilder());
			add(new BreadcrumbsBuilder());
			add(new RowsBuilder());
			add(new HomeBuilder());
			add(new MenuBuilder());
			add(new MergeGlobals());
			add(new Mark());
			//add(new Sweep());
			add(new MD5Renderer());
			add(renderer);
		}
	};
	
	private static final Logger logger = Logger.getLogger(Execution.class.getName());

	private List<String> errors = new ArrayList<>();
	private Path inputDir;
	private Path outputDir;
	private Set<String> missingCategories = new HashSet<>();

	private Long t0;
	private Long t1;
	private long runtime;

	private Path categoriesPath;

	public Execution(Path ingestDir, Path outgestDir) {
		this.inputDir = ingestDir;
		this.outputDir = outgestDir;
	}

	public void addError(String error, Object... args) {
		errors.add(String.format(error, args));
	}

	public void addError(Exception e) {
		errors.add(e.getMessage());
	}

	public List<String> getErrors() {
		return errors;
	}

	public Path getInputDir() {
		return inputDir;
	}

	public Set<String> getMissingCategories() {
		return missingCategories;
	}

	public void setMissingCategories(Set<String> missingCategories) {
		this.missingCategories = missingCategories;
	}

	private void ingest() {
		createOutputDir();
		Path startingDir = getInputDir();
		boolean canIngest = startingDir.toFile().exists() && startingDir.toFile().isDirectory();
		if (!canIngest) {
			fatal("Could not ingest [" + getInputDir().toAbsolutePath().toString() + "]");
		}
		MFFileVisitor visitor = new MFFileVisitor(this);
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		try {
			Files.walkFileTree(startingDir, opts, Integer.MAX_VALUE, visitor);
		} catch (IOException e) {
			e.printStackTrace();
		}
		append("== Inggestion ==");
		append("Files ingested: %d = %d parsed", ingested.get(),forest.size());
		append("      bypassed: %d = %d replaced + %d kept", replaced.get()+ kept.get(), replaced.get(),kept.get());

	}

	public void append(String string, Object... args) {
		String msg = String.format(string, args);
		logger.info(msg);
	}

	private void fatal(String string) {
		System.err.println(string);
		System.exit(-1);
	}

	public void execute() {
		hello();
		ingest();
		visitWith(visitors);
		bye();
	}

	private void hello() {
		t0 = System.currentTimeMillis();		
		append("Execution started: " + nows());
	}


	private Calendar now() {
		return Calendar.getInstance(TIME_ZONE);
	}

	private void createOutputDir() {
		File outputDir_f = outputDir.toFile();
		if (!outputDir_f.exists()) {
			outputDir_f.mkdirs();
		}
	}

	private void visitWith(List<Visitor> vs) {
		if (vs!=null)
			vs.forEach((v) -> Visitor.visit(this, v));
	}

	private void bye() {
		t1 = System.currentTimeMillis();
		appendErrors();
		append("Execution finished: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", now());
		append("          time: %d ms", t1 - t0);
		System.out.println(summary.toString());
		File log = outputDir.resolve("execution.log").toFile();
		try (FileWriter fileWriter = new FileWriter(log)) {
			fileWriter.write(summary.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void appendErrors() {
		if (!errors.isEmpty()) {
			append("!!! ERRORS");
			errors.forEach(e -> append(e));
		}
	}

	public Path getOutputDir() {
		return outputDir;
	}

	public int incFileCount() {
		return ingested.getAndIncrement();
	}

	public void linfo(String string) {
		System.out.print("====> " + string);

	}

	@Override
	public String toString() {
		StringBuilder bud = new StringBuilder();
		bud.append("Runtime [");
		bud.append(runtime);
		bud.append("]");
		return bud.toString();
	}

	public Path pathOfCategory(String parentCategory) {
		String fileName = parentCategory + ".mf";
		return categoriesPath.resolve("_categories").resolve(fileName);
	}

	public void ingestFile(Path path) {
		lfiner("Ingesting [%s]", path.getFileName());
		if (path != null)
			ingested.incrementAndGet();
		Map<String, Object> tree = MFParser.parse(this, path);
		forest.put(key.getString(tree), tree);
	}

	public Path getCategoriesDir() {
		return getInputDir().resolve("_categories");
	}

	public Map<String, Map<String, Object>> getForest() {
		return forest;
	}

	public void bypass(Path file) {
		String filename = file.getFileName().toString();
		lfiner("Bypassing [%s]", filename);
		try {
			Path target = outputDir.resolve(filename);
			File target_f = target.toFile();
			boolean exists = target_f.exists();
			boolean diffSize = true;
			if (exists) {
				long target_z = target_f.getTotalSpace();
				long source_z = file.toFile().getTotalSpace();
				diffSize = source_z != target_z;
			}
			boolean replace = (!exists) || diffSize;
			if (replace) {
				Files.copy(file, target, REPLACE_EXISTING);
				replaced.incrementAndGet();
			} else {
				kept.incrementAndGet();
			}

		} catch (IOException e) {
			addError(e.getMessage());
		}
	}

	public Stream<Map<String, Object>> getCategories() {
		return forest.values().stream().filter(t -> is_category.getBoolean(t));
	}

	public void lfiner(String string, Object... args) {
		String msg = String.format(string, args);
		logger.finer(msg);
	}

	public void addWarning(String string, Object... args) {
		lfiner(string, args);
	}

	public boolean containsKey(String k) {
		return forest.containsKey(k);
	}

	public Map<String, Object> get(String k) {
		return forest.get(k);
	}

	public Stream<Map<String, Object>> getDocuments() {
		return forest.values().stream().filter(t -> is_document.getBoolean(t));
	}

	public Map<String, Object>  getGlobals() {
		return globals;
	}

	public boolean hasErrors() {
		return ! errors.isEmpty();
	}

	public void putHome(String k, Object v) {
		if (homeMap.containsKey(k) )
		{
			System.out.println("OVERRIDE");
		}		
		homeMap.put(k, v);
	}

	public Map<String, Object> getHomeMap() {
		return homeMap;
	}

	private String nows() {
		return df.format(new Date());
	}
}