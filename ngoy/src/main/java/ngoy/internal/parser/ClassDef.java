package ngoy.internal.parser;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.stream.Stream;

public class ClassDef {
	public static ClassDef of(Class<?> clazz) {
		return new ClassDef(clazz, null);
	}

	public static ClassDef of(Method meth) {
		return ofGeneric(meth.getReturnType(), meth.getGenericReturnType());
	}

	public static ClassDef of(Field field) {
		return ofGeneric(field.getType(), field.getGenericType());
	}

	private static ClassDef ofGeneric(Class<?> returnType, Type genericReturnType) {
		return new ClassDef(returnType, genericReturnType, genericReturnType instanceof ParameterizedType);
	}

	public final Class<?> clazz;
	public final boolean needsCast;
	public Type genericType;
	public int typeParamIndex;

	private ClassDef(Class<?> clazz, Type genericType) {
		this(clazz, genericType, false);
	}

	public ClassDef(Class<?> clazz, Type genericType, boolean needsCast) {
		this.clazz = clazz;
		this.genericType = genericType;
		this.needsCast = needsCast;
	}

	public boolean valid() {
		return clazz.isArray() || Iterable.class.isAssignableFrom(clazz) || Stream.class.isAssignableFrom(clazz);
	}

	public Class<?> getListItemType(ClassDef cd) {
		Class<?> itemType;
		if (cd.clazz.isArray()) {
			itemType = cd.clazz.getComponentType();
		} else if ((Iterable.class.isAssignableFrom(cd.clazz) || Stream.class.isAssignableFrom(cd.clazz)) && cd.needsCast) {
			itemType = cd.getTypeArgument();
		} else {
			itemType = Object.class;
		}
		return itemType;
	}

	public Class<?> getTypeArgument() {
		if (!needsCast) {
			return clazz;
		}

		if (genericType instanceof Class) {
			return (Class<?>) genericType;
		}

		Type type = ((ParameterizedType) genericType).getActualTypeArguments()[typeParamIndex];
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type rawType = pt.getRawType();
			if (rawType instanceof Class) {
				return (Class<?>) rawType;
			}
		} else if (type instanceof WildcardType) {
			WildcardType wct = (WildcardType) type;
			Type[] upperBounds = wct.getUpperBounds();
			if (upperBounds.length > 0 && upperBounds[0] instanceof Class) {
				return (Class<?>) upperBounds[0];
			}

		}
		return Object.class;
	}

	@Override
	public String toString() {
		return format("%s: %s", getClass().getSimpleName(), clazz);
	}

}