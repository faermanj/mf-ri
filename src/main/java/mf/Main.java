package mf;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

public class Main {

	public static void main(String[] args) {
		String ingestDir = "../ConteudoSite";
		String outgestDir = System.getProperty("user.home") + File.separator + "SiteMF";

		//execute(ingestDir, outgestDir);
		//tryWatch(ingestDir, outgestDir);
		//navigate(outgestDir, "JT.html");
	}

	private static void tryWatch(String ingestDir, String outgestDir) {
		try {
			watch(ingestDir, outgestDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void watch(String ingestDir, String outgestDir) throws IOException {
		WatchService watcher = FileSystems	.getDefault()
											.newWatchService();
		HashMap<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
		Path dir = Paths.get(ingestDir);
		System.out.format(">> Scanning %s ...\n", dir);
		registerAll(ingestDir, outgestDir, watcher, keys, dir);
		System.out.println(">> Scanned. Waiting...");
		processEvents(ingestDir, outgestDir, watcher, keys);
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	static void processEvents(String ingestDir, String outgestDir, WatchService watcher, HashMap<WatchKey, Path> keys) {
		for (;;) {
			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
				System.out.println(">> Key [" + key + "] taken");
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				@SuppressWarnings("rawtypes")
				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				// print out event
				System.out.format(">> FS %s: %s\n", event	.kind()
															.name(),
						child);

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (kind == ENTRY_CREATE) {
					try {
						if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
							registerAll(outgestDir, outgestDir, watcher, keys, child);
						}
					} catch (IOException x) {
						System.out.println(">> FS OPS: " + x.getMessage());
					}
				}
				execute(ingestDir, outgestDir);
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	private static void registerAll(String ingestDir, String outgestDir, WatchService watcher,
			HashMap<WatchKey, Path> keys, Path root) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path child, BasicFileAttributes attrs) throws IOException {
				register(ingestDir, outgestDir, watcher, keys, child);
				return FileVisitResult.CONTINUE;
			}

			private void register(String ingestDir, String outgestDir, WatchService watcher,
					HashMap<WatchKey, Path> keys, Path dir) throws IOException {
				WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

				Path prev = keys.get(key);
				if (prev == null) {
					System.out.format("register: %s\n", dir);
				} else {
					if (!dir.equals(prev)) {
						System.out.format("update: %s -> %s\n", prev, dir);
					}
				}

				keys.put(key, dir);

			}
		});

	}

	private static void execute(String ingestDir, String outgestDir) {
		//Execution execution = new Execution(ingestDir, outgestDir);
		//execution.execute();
	}

	private static void navigate(String outgestDir, String path) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				Path index = Paths	.get(outgestDir)
									.resolve(path);
				String url = "file://" + index	.toAbsolutePath()
												.toString();
				desktop.browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
