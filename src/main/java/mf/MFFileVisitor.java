package mf;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class MFFileVisitor extends SimpleFileVisitor<Path> {
	private Execution execution;

	public MFFileVisitor(Execution execution) {
		this.execution = execution;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {		
		return isIgnoredDir(dir) ?  SKIP_SUBTREE : CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {		
		String filename = file.getFileName().toString();
		if (filename.endsWith(".mf"))
			execution.ingestFile(file);
		else{
			execution.bypass(file);
		}
		return FileVisitResult.CONTINUE;
	}

	private boolean isIgnoredDir(Path path) {
		File file = path.toFile();
		boolean isDir = file.isDirectory();
		boolean isHidden = file.getName().startsWith("__");
		return isDir && isHidden;
	}

}
