package ngoy.translate;

import ngoy.core.NgModule;

/**
 * Provides translation support.
 * <p>
 * See also {@link ngoy.Ngoy.Builder#translateBundle(String)}, which configures
 * the translate module automatically.
 * 
 * @author krizz
 */
@NgModule(declarations = { TranslatePipe.class, TranslateDirective.class }, providers = { TranslateService.class })
public final class TranslateModule {
	private TranslateModule() {
	}
}
