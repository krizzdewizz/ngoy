package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class ProcessingInstructionTest extends ANgoyTest {

	@Component(selector = "test", template = "<!doctype html>\n<html></html>")
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<!DOCTYPE html>\n<html></html>");
	}
}
