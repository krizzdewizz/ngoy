package ngoy.hyperml.base;

/**
 * A CSS unit in the pairs array is merged with it's predecessor value.
 * <p>
 * Example:
 * 
 * <pre>
 * css("a", height, 12, px);
 * 
 * a{height:12px}
 * </pre>
 * 
 * @author krizz
 */
public class Unit {
	private String unit;

	public Unit(String unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return unit;
	}
}