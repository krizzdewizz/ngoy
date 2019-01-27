package ngoy.core;

import static java.util.Arrays.asList;
import static ngoy.core.FlatList.flatten;
import static ngoy.core.Pair.of;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Test;

public class FlatListTest {

	@Test
	public void simple() throws Exception {
		assertThat(flatten("a", 1)).isEqualTo(new Object[] { "a", 1 });
	}

	@Test
	public void itemIsArray() throws Exception {
		assertThat(flatten("a", new int[] { 1 })).isEqualTo(new Object[] { "a", 1 });
	}

	@Test
	public void itemIsIterable() throws Exception {
		assertThat(flatten("a", asList(1))).isEqualTo(new Object[] { "a", 1 });
	}

	@Test
	public void itemIsMap() throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("color", "red");
		assertThat(flatten("a", map)).isEqualTo(new Object[] { "a", "color", "red" });
	}

	@Test
	public void itemIsEntry() throws Exception {
		assertThat(flatten("a", of("color", "red"), of(44, "none"))).isEqualTo(new Object[] { "a", "color", "red", 44, "none" });
	}

	@Test
	public void itemIsStream() throws Exception {
		assertThat(flatten("a", Stream.of(1, 2, 3))).isEqualTo(new Object[] { "a", 1, 2, 3 });
	}

	@Test
	public void deeplyNested() throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("0", false);
		map.put("1", true);
		assertThat(flatten("a", Stream.of(1, asList(4, of("color", "red")), map), "b")).isEqualTo(new Object[] { "a", 1, 4, "color", "red", "0", false, "1", true, "b" });
	}
}
