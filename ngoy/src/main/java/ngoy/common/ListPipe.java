package ngoy.common;

import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.addAll;

/**
 * Outputs the input object array as a {@link java.util.List}.
 *
 * @author krizz
 */
@Pipe("list")
public final class ListPipe implements PipeTransform {

    static List<Object> transformInternal(Object obj, Object... params) {
        List<Object> result = new ArrayList<>();
        result.add(obj);
        addAll(result, params);
        return result;
    }

    @Override
    public List<Object> transform(Object obj, Object... params) {
        return transformInternal(obj, params);
    }

}
