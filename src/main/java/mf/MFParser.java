package mf;

import static mf.model.Attribute.descriptor;
import static mf.model.Attribute.dirty;
import static mf.model.Attribute.key;
import static mf.model.Attribute.md5;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.pegdown.PegDownProcessor;

import mf.validation.AttributesValidator;

public class MFParser {
	static final Logger logger = Logger.getLogger(MFParser.class.getName());

	public static String keyOf(Path path) {
		if (path == null)
			return "";
		String fileName = path.getFileName().toString();
		String treeKey = fileName;
		if (treeKey.indexOf(".") > 0)
			treeKey = treeKey.substring(0, treeKey.lastIndexOf("."));
		return treeKey;
	}

	public static boolean isCategory(Path path) {
		Path parent = path.getParent().getFileName();
		boolean isCategory = parent.endsWith("_categories");
		return isCategory;
	}

	public static Map<String, Object> parse(Execution execution, Path filePath) {
		Map<String, Object> result = mkMap();
		if (filePath == null)
			return result;
		descriptor.put(result, filePath);
		String k = keyOf(filePath);
		key.put(result, k);
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			System.exit(-3);
		}
		try (InputStream is = Files.newInputStream(filePath);
				DigestInputStream dis = new DigestInputStream(is, md);
				InputStreamReader isReader = new InputStreamReader(dis);
				BufferedReader reader = new BufferedReader(isReader)) {
			parseFile(execution, filePath, result, reader);
			byte[] md5_hash = md.digest();
			String md5_str = bytesToHex(md5_hash).toLowerCase();
			md5.put(result, md5_str);
			File md5_f = MFParser.cousin_f(execution, result, "", "md5");
			if (md5_f.exists()) {
				Scanner fileReader = new Scanner(md5_f);
				fileReader.useDelimiter("\\Z"); // \Z means EOF.
				if (fileReader.hasNext()){
					String old_md5 = fileReader.next();				
					boolean sameHash = md5_str.equals(old_md5);
					if (!sameHash) {
						dirty.put(result, true);
						logger.info("Marking [" + k + "] as dirty");
					}
				}
				fileReader.close();
			}
		} catch (Exception e) {
			execution.addError("Exception reading [" + k +"]");
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, Object> mkMap() {
		//return new ConcurrentHashMap<>();
		return new HashMap();
	}

	private static void parseFile(Execution execution, Path filePath, Map<String, Object> result,
			BufferedReader reader) {
		StringBuilder buf = new StringBuilder();
		StringBuilder prop = new StringBuilder();
		reader.lines().forEach(line -> {
			if (line.contains("=")) {
				String[] split = line.split("=");
				if (split.length > 1) {
					// key and value
					String key = split[0].trim();
					if (isKnown(key)) {
						String value = split[1];
						output(result, buf, prop, key, value);
					} else {
						if (isTag(line)) {
							buf.append(line);
						} else {
							execution.addError("[" + filePath.toString() + "]: Unknown key [" + key + "]");
						}
					}
				} else {
					String key = split[0];
					output(result, buf, prop, key, "");
				}
			} else
				buf.append(line);
			buf.append("\n");
		});
		if (buf.length() > 0) {
			result.put(prop.toString(), buf.toString());
		}
	}

	private static boolean isTag(String line) {
		return line.contains("<") || line.contains(">");
	}

	static final Pattern sequentialName = Pattern.compile("D\\d?\\d?");

	private static void output(Map<String, Object> result, StringBuilder buf, StringBuilder prop, String key,
			String value) {
		if (prop.length() > 0) {
			String eKey = prop.toString();
			String eVal = buf.toString();

			if (eVal.endsWith("\n"))
				eVal = eVal.substring(0, eVal.length() - 1);

			result.put(eKey, eVal);
			prop.delete(0, prop.length());
			buf.delete(0, buf.length());
		}
		prop.append(key);
		buf.append(value);
	}

	private static boolean isKnown(String key) {
		return AttributesValidator.known.contains(key);
	}

	public static String sibling_s(Map<String, Object> t, String suffix, String ext) {
		String sep = (suffix == null || suffix.isEmpty()) ? "" : "_";
		String expected = key.get(t) + sep + suffix + "." + ext;
		return expected;
	}

	static final PegDownProcessor pegpro = new PegDownProcessor();

	public static String md2html(String md) {
		return pegpro.markdownToHtml(md);
	}

	public static void main(String[] args) {
		String str = "<p>uala soala *coala*</p>";
		System.out.println(md2html(str));
	}

	public static boolean exists(Map<String, Object> t, String suffix, String ext) {
		File expected = sibling_f(t, suffix, ext);
		return expected != null && expected.exists();
	}

	public static File sibling_f(Map<String, Object> t, String suffix, String ext) {
		Path desc = descriptor.getPath(t);
		Path parent = desc.getParent();
		return openFile(t, suffix, ext, parent);
	}

	private static File openFile(Map<String, Object> t, String suffix, String ext, Path parent) {
		String path = sibling_s(t, suffix, ext);
		File expected = parent.resolve(path).toFile();
		return expected;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static File cousin_f(Execution e, Map<String, Object> t, String suffix, String ext) {
		return openFile(t, suffix, ext, e.getOutputDir());
	}

}
