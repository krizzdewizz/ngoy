package ngoy.internal.parser;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ClassDef {

	public static class ListItemDef {
		public final Class<?> clazz;
		public final String typeName;

		public ListItemDef(Class<?> clazz, String typeName) {
			this.clazz = clazz;
			this.typeName = typeName;
		}
	}

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
	public final Type genericType;
	public final boolean needsCast;
	public int typeParamIndex;

	private ClassDef(Class<?> clazz, Type genericType) {
		this(clazz, genericType, false);
	}

	private ClassDef(Class<?> clazz, Type genericType, boolean needsCast) {
		this.clazz = clazz;
		this.genericType = genericType;
		this.needsCast = needsCast;
	}

	public boolean valid() {
		return clazz.isArray() || Iterable.class.isAssignableFrom(clazz);
	}

	public ListItemDef getListItemType(ClassDef cd) {
		Class<?> itemType = Object.class;
		String itemTypeName = "Object";
		if (cd.clazz.isArray()) {
			itemType = cd.clazz.getComponentType();
			itemTypeName = itemType.getName();
		} else if (Iterable.class.isAssignableFrom(cd.clazz) && cd.needsCast) {
			itemType = cd.getTypeArgument();
			itemTypeName = itemType.getName();
		}
		return new ListItemDef(itemType, itemTypeName);
	}

	public Class<?> getTypeArgument() {
		return needsCast ? (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[typeParamIndex] : clazz;
	}

	@Override
	public String toString() {
		return format("%s: %s", getClass().getSimpleName(), clazz);
	}

}