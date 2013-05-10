package org.lo.d.eclipseplugin.mcp.process;

import java.nio.charset.Charset;

public class OSSupport {

	private static class ConditionValue<T> {
		private final T trueValue;
		private final T falseValue;

		private ConditionValue(T trueValue, T falseValue) {
			this.trueValue = trueValue;
			this.falseValue = falseValue;
		}

		private T get(boolean b) {
			return b ? trueValue : falseValue;
		}
	}

	private static final ConditionValue<String> osShellCommand = new ConditionValue<String>("cmd.exe", "/bin/bash");
	private static final ConditionValue<Charset> osConsoleCharset = new ConditionValue<Charset>(Charset.forName("MS932"), Charset.forName("UTF-8"));

	public static Charset getOSConsoleCharset() {
		return getOSValue(osConsoleCharset);
	}

	public static String getOSShellCommand() {
		return getOSValue(osShellCommand);
	}

	private static <T> T getOSValue(ConditionValue<T> conditionValue) {
		return conditionValue.get(System.getProperty("os.name").startsWith("Windows"));
	}
}
