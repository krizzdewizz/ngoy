package ngoy.parser;

import org.codehaus.janino.ExpressionEvaluator;
import org.junit.Test;

public class JaninoTest {

	@Test
	public void test() throws Exception {
		Object[] arguments = { new Double(20.0) };

		// Create "ExpressionEvaluator" object.
		ExpressionEvaluator ee = new ExpressionEvaluator();
		ee.setExpressionType(double.class);
		ee.setParameters(new String[] { "total" }, new Class[] { double.class });
		ee.cook("total >= 100.0 ? 0.0 : 7.95");

		// Evaluate expression with actual parameter values.
		Object res = ee.evaluate(arguments);

		// Print expression result.
		System.out.println("Result = " + String.valueOf(res));
	}

}
