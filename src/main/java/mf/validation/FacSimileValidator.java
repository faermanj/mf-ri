package mf.validation;

import static mf.model.Attribute.descriptor;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import mf.model.Attribute;
import mf.model.Visitor;

public class FacSimileValidator extends Visitor {
	
	@Override
	protected void visit(Map<String,Object> t) {
		if (isIgnored(t)) return;
		
		Path path = descriptor.getPath(t);
		Path parent = path.getParent();
		String fname = path.getFileName().toString();
		
		String facSimilePath = fname.replaceAll("\\.mf", ".pdf");
		Path facPath = parent.resolve(facSimilePath);
		File facFile = facPath.toFile();
		boolean fac_exists = facFile.exists();

		String txtPathStr = fname.replaceAll("\\.mf", "_TX.pdf");
		Path txtPath = parent.resolve(txtPathStr);
		File txtFile = txtPath.toFile();
		boolean txt_exists = txtFile.exists();

		if (!(fac_exists || txt_exists)) {
			addError("PDF FacSimile ["+facFile.exists()+"] ou TX ["+txtFile.exists()+"] não encontrado para o descritor [" + path + "]");
		}

	}

	private boolean isIgnored(Map<String,Object> t) {
		return Attribute.nopdf.contains(t);
	}
}
