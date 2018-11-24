package ngoy.common;

import ngoy.core.NgModule;

/**
 * Common pipes.
 * <p>
 * This module must not be imported as it is always available.
 * 
 * @author krizz
 */
@NgModule(declarations = { UpperCasePipe.class, LowerCasePipe.class, DatePipe.class, CapitalizePipe.class })
public class PipesModule {
}
