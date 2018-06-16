package mf;

import java.io.IOException;
import java.util.logging.Logger;

import mf.model.Constants;

public class GerarSite extends App implements Constants{
	static final Logger logger = Logger.getLogger(GerarSite.class.getName());

	public static void main(String[] args) {
		GerarSite gerarSite = new GerarSite();
		gerarSite.run();
		if (LIVE) gerarSite.watch();
	}

	private void watch() {
		try {
			new WatchDir(ingestDir, true, this).processEvents();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		Execution exec = new Execution(ingestDir, outgestDir);
		exec.execute();
		//upload();
	}

}
