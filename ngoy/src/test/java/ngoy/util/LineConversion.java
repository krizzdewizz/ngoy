package ngoy.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class LineConversion {
	public static void main(String[] args) throws Exception {
		new LineConversion().run();
	}

	private void run() throws Exception {
		Path base = Paths.get("d:/data/");

		String[] all = new String[] { //
				"ngoy", //
				"ngoy-examples", //
				"ngoy-starter-web", //
				"ngoy-tour-of-heroes", //
				"ngoy-website", //
		};

		Stream.of(all)
				.map(dir -> base.resolve(dir))
				.flatMap(dir -> {
					try {
						return Files.walk(dir, 1);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.filter(f -> f.getFileName()
						.toString()
						.equals("ngoy") && Files.isRegularFile(f))
				.forEach(this::convert);
		;
	}

	void convert(Path path) {
		try {
			String content = new String(Files.readAllBytes(path));
			String converted = Files.readAllLines(path)
					.stream()
					.collect(joining("\n"));
			if (!content.equals(converted)) {
				System.out.println("xx" + path.toAbsolutePath());
			}
			Files.write(path, converted.getBytes());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
