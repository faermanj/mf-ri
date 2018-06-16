package mf.rendering;

import static mf.model.Attribute.has_facsimile;
import static mf.model.Attribute.has_reading;
import static mf.model.Attribute.is_category;
import static mf.model.Attribute.key;
import static mf.model.Attribute.link_target;
import static mf.model.Attribute.template_key;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import mf.model.Constants;
import mf.model.Visitor;
public class MustacheRenderer extends Visitor implements Constants {
	Logger logger = Logger.getLogger(MustacheRenderer.class.getName());
	
	TemplateLoader templateLoader;
	Path templateDir;
	Handlebars handlebars;
	Template categoryTemplate;
	Template pdfTemplate;
	Template htmlTemplate;
	AtomicInteger count = new AtomicInteger();
	
	@Override
	protected void hello() {
		if(execution.getErrors().isEmpty()){
			initHandlebars();
			copyTemplate();
			renderHome();
			renderCategories();
			renderDocuments();
		}else stop();
	}
	
	@Override
	protected void goodbye() {
		logger.info("["+count.get()+"] mustaches rendered.");
	}

	
	private void renderCategories() {		
		execution.getCategories().forEach(t -> render(t,"inner1",categoryTemplate));
	}
	
	private void renderDocuments() {
		execution.getDocuments().forEach(t -> renderDocument(t));
	}
	
	public void renderAny(Map<String, Object> t){
		boolean is_cat = is_category.getBoolean(t);
		if(is_cat){
			render(t,"inner1",categoryTemplate);
		}else{
			renderDocument(t);
		}
	}

	private void renderDocument(Map<String, Object> t) {
		boolean has_fac = has_facsimile.getBoolean(t);
		boolean has_read = has_reading.getBoolean(t);
		String out = link_target.getString(t);
		if(has_fac || has_read) 
			renderPDF(t,out);
		else renderHTML(t,out);
	}


	private void renderHTML(Map<String, Object> t,String out) {
		render(t,"inner htmldoc",htmlTemplate,out);
	}

	private void renderPDF(Map<String, Object> t,String out) {
		render(t,"inner",pdfTemplate,out);
	}



	private void render( Map<String, Object> c, String templateKey,Template template) {		
		String k = key.getString(c);
		String out = k+".html";
		render(c, templateKey,template, out);
	}


	private void render(Map<String, Object> c, String templateKey,Template template, String out) {		
		String k = key.getString(c);
		if(out== null) 
			execution.addError("No output defined for ["+k+"]");
		execution.lfiner("Rendering [%s] with [%s] to [%s]", k, template.filename(),out);
		Path outputDir = execution.getOutputDir();
		Path category_p = outputDir.resolve(out);
		File category_f = category_p.toFile();
		try(FileWriter writer = new FileWriter(category_f)) {
			c.put("generation_date", df.format(new Date()));
			template_key.put(c, templateKey);
			template.apply(c, writer);
			writer.flush();
			count.incrementAndGet();
			if(LIVE){
				upload(k);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}		
	}


	private void upload(String k) {
		AmazonS3Client S3 = new AmazonS3Client();
		String file = k + ".html";
		File source = execution.getOutputDir().resolve(file).toFile();
		S3.putObject("laura.marcosfaerman.jor.br", k, source);
	}

	private void initHandlebars() {
		templateDir = execution.getInputDir().resolve("__template");
		templateLoader = new FileTemplateLoader(templateDir.toFile());
		templateLoader.setSuffix(".html");
		handlebars = new Handlebars(templateLoader);
		try {			
			 categoryTemplate = handlebars.compile("inner1");
			 pdfTemplate = handlebars.compile("pdfvis");
			 htmlTemplate = handlebars.compile("inner");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void copyTemplate() {
		File srcDir = templateDir.toFile();
		File destDir = execution.getOutputDir().toFile();
		try {
			FileUtils.copyDirectory(srcDir, destDir,
					(File f) -> (! f.getName().endsWith(".ini")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void renderHome() {
		if(execution.getForest().isEmpty())return;
		try {			
			Template homeTemplate = handlebars.compile("index");
			Path home_htmlp = execution.getOutputDir().resolve("index.html");
			File home_html = home_htmlp.toFile();
			FileWriter homeWriter = new FileWriter(home_html);
			Map<String, Object> home = execution.getHomeMap();
			template_key.put(home, "home");
			Object menusg = execution.getGlobals().get("menu");
			Object menusl = home.get("menu");
			
			Set<Entry<String, Object>> globalEntries = execution.getGlobals().entrySet();
			for (Entry<String, Object> entry : globalEntries) {
				String gk = entry.getKey();
				if(!home.containsKey(gk)){
					Object gv = entry.getValue();
					home.put(gk, gv);
				}
			}
			
			homeTemplate.apply(home, homeWriter);
			homeWriter.flush();
			homeWriter.close();
			count.incrementAndGet();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
