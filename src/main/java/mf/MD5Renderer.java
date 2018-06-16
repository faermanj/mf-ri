package mf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import mf.model.Attribute;
import mf.model.Visitor;

public class MD5Renderer extends Visitor {
	@Override
	protected void visit(Map<String, Object> m) {
		File md5_f = MFParser.cousin_f(execution,m, "","md5");
		String md5_s = Attribute.md5.getString(m);
		try {
			Files.write(md5_f.toPath(), md5_s.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			execution.addError("Could not write hash ["+md5_s+"] to ["+md5_f.getName().toString()+"]");
		}
	}
}
