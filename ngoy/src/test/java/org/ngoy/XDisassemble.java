package org.ngoy;

import org.cojen.classfile.DisassemblyTool;
import org.junit.Test;

public class XDisassemble {

	@Test
	public void disassemble() throws Exception {
		DisassemblyTool.main(new String[] { "-f", "builder", X.class.getName() });
	}
}
