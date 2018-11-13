package org.ngoy;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.NgoyException.wrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;
import org.ngoy.Ngoy.Config;
import org.ngoy.core.Context;

public class RenderStringTest {

	private static class Out extends OutputStream {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		@Override
		public String toString() {
			try {
				return new String(out.toByteArray(), "UTF-8");
			} catch (Exception e) {
				throw wrap(e);
			}
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
		}
	}

	@Test
	public void test() {
		Config config = new Config();
		config.contentType = "text/plain";
		Context ctx = Context.of("all", asList(11, 22, 33));
		Out out = new Out();
		Ngoy.renderString("<ng-container *ngFor=\"let x of all; index as i\">hello {{x}}, {{i}}\n</ng-container>", ctx, out, config);
		assertThat(out.toString()).isEqualTo("hello 11, 0\nhello 22, 1\nhello 33, 2\n");
	}

	@Test
	public void testMicroSyntax() {
		Config config = new Config();
		config.contentType = "text/plain";
		Context ctx = Context.of("all", asList(11, 22, 33));
		Out out = new Out();
		Ngoy.renderString("*ngFor let x of all; index as i:hello {{x}}, {{i}}\n", ctx, out, config);
		assertThat(out.toString()).isEqualTo("hello 11, 0\nhello 22, 1\nhello 33, 2\n");
	}
}
