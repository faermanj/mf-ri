package mf.rendering;

import mf.model.Visitor;

public class MFRenderer extends Visitor {

	@Override
	protected void hello() {
		//Path outpath = execution.getInputDir();
		//renderAll(e, outpath);
	}
	/*
	private void renderAll(Execution e, Path outpath) {
		Set<Entry<Path, Map<String, String>>> ms = e.getMetadata().entrySet();
		for (Entry<Path, Map<String, String>> m : ms) {
			Path key = m.getKey();
			String absPath = key.toAbsolutePath().toString();
			String mfhtml = absPath.replaceAll("\\.mf", ".mfhtml");
			Map<String, String> metadata = m.getValue();
			try {
				Path mfhtmlpath = Paths.get(mfhtml);
				File mfhtmlfile = mfhtmlpath.toFile();
				if (mfhtmlfile.exists()) {
					mfhtmlfile.delete();
				}
				mfhtmlfile.createNewFile();
				renderOne(metadata, mfhtmlpath);
				System.out.println("Rendered [" + mfhtml + "]");
			} catch (IOException ex) {
				System.out.println(ex);
				e.addError(ex);
			}
		}
	}

	private void renderOne(Map<String, String> metadata, Path created) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(created.toFile()));
		Set<Entry<String, String>> entries = metadata.entrySet();
		for (Entry<String, String> e : entries) {
			String k = e.getKey();
			String v = e.getValue();
			writer.append(k);
			writer.append("=");
			if (isHTML(k))
				writer.append(toHTML(e.getValue()));
			else
				writer.append(e.getValue());
			writer.append("\n");
		}
		writer.flush();
		writer.close();
	}

	private boolean isHTML(String k) {
		return k.startsWith("description");
	}

	PegDownProcessor processor = new PegDownProcessor();

	private CharSequence toHTML(String value) {
		return processor.markdownToHtml(value);
	}

	private Path createRenderTarget(Execution e) {
		Path outpath = e.getOutputDir().resolve("out-mf");
		File out = outpath.toFile();
		if (!out.exists())
			out.mkdirs();
		return outpath;
	}
	*/
}
