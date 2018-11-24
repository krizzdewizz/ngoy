package {{pack}};

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

@Pipe("{{ name }}")
public class {{ className }}Pipe implements PipeTransform {
	@Override
	public Object transform(Object obj, Object... params) {
		if (obj == null) {
			return null;
		}

		// transform
		return null;
	}
}
