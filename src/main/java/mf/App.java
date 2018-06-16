package mf;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class App {
	private static final String USER_HOME = System.getProperty("user.home");
	private static final Path HOME_P = Paths.get(USER_HOME);

	private static  void initLogging() {
		Logger logger = Logger.getLogger("");
		FileHandler fh;
		System.setProperty("java.util.logging.SimpleFormatter.format","%5$s \n"); 
		try {
			// This block configure the logger with handler and formatter
			fh = new FileHandler("execution.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (Exception e) {
			System.err.println("Could not init logging...");
			//System.exit(-2);
		}
	}
	static {
		initLogging();
	}

	static final Logger logger = Logger.getLogger(App.class.getName());

	
	static protected Path ingestDir = findIngestDir();
	static protected Path outgestDir = findOutgestDir();


	static private Path findOutgestDir() {
		String defaultOutgest = "target/mf_out/";
		Path path = FileSystems.getDefault().getPath(".").resolve(defaultOutgest);
		File file = path.toFile();
		if (! file.exists() ){
			file.mkdirs();
		}		
		if (! file.exists() && file.isDirectory() ){
			System.err.println("Cannot proceed without output dir");
			System.exit(1);
		}
		return path;
	}

	static private Path findIngestDir() {
		Path result = null;	
		if (result == null) {
			result = lookupFromHome("Google Drive/SharedDrive/ConteudoSite");
		}
		if (result == null) {
			result = lookupFromHome("Google Drive/EXECUCAO_ITAU_CULTURAL/ConteudoSite");
		}
		if (result == null) {
			result = lookupFromHome("ITAU/ConteudoSite");
		}
		if (result == null) {
			result = lookupFromHome("Google Drive/Marcos Faerman - Reportagens/EXECUCAO_ITAU_CULTURAL/ConteudoSite/");
		}
		if(result == null){
			result = lookupFromJar();
		}
		if (result == null) {
			logger.severe("Could not find ingest dir.");
			System.exit(-1);
		}else {
			String userDir = result.toAbsolutePath().toString();
			logger.info("Executing from [" + userDir + "]");
		}
		return result;
	}
	
	static private Path lookupFromHome(String dir){
		Path resolve = HOME_P.resolve(dir);
		File file = resolve.toFile();
		boolean exists = file.exists();
		boolean directory = file.isDirectory();
		if(exists && directory){
			logger.warning("Content found at ["+dir+"]");
			return resolve;
		}else {
			logger.info("Content not found at ["+dir+"]");
			return null;
		}
	}

	static private Path lookupFromJar() {
		try {
			URI jarURI = GerarSite.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			File jarFile = new File(jarURI);
			Path ingestDir = jarFile.toPath().getParent().resolve("../../ConteudoSite");
			if(isMFDir(ingestDir)) return ingestDir;
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return null;
	}

	static private boolean isMFDir(Path ingestDir) {		
		File file = ingestDir.resolve("_categories").toFile();
		boolean isMF = file.exists();
		logger.warning("File ["+file+"] exists ["+file.exists()+"]");
		return isMF;
	}
	
	protected static void promptWait() {
		System.out.println("Press [return] to exit.");
		try(Scanner reader = new Scanner(System.in)){
			reader.nextLine();
		};
	}

}
