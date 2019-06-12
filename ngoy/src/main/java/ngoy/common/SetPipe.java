package ngoy.common;

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.addAll;

/**
 * Outputs the input object array as a {@link java.util.Set}.
 *
 * @author krizz
 */
@Pipe("set")
public final class SetPipe implements PipeTransform {

    @Override
    public Set<Object> transform(Object obj, Object... params) {
        Set<Object> result = new LinkedHashSet<>();
        result.add(obj);
        addAll(result, params);
        return result;
    }
}
