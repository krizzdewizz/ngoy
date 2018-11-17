package org.ngoy.core;

import jodd.jerry.Jerry;

public interface OnCompile {
	void ngOnCompile(Jerry el, String cmpClass);
}
