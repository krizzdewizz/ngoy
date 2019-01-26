package ngoy.hyperml.base;

/**
 * Clients may subclass to add custom behaviour.
 * 
 * @author krizzdewizz
 * @param <T> type of subclass
 */
public abstract class XmlBase<T extends XmlBase<?>> extends BaseMl<T> {
	@Override
	protected boolean isVoidElement(String name) {
		return false;
	}

	@Override
	protected boolean escapeText() {
		return true;
	}
}