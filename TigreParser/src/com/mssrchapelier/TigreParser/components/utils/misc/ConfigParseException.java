package com.mssrchapelier.TigreParser.components.utils.misc;

@SuppressWarnings("serial")
public class ConfigParseException extends Exception {
	public ConfigParseException (String errorMessage) { super(errorMessage); }

	public ConfigParseException (String errorMessage, Throwable cause) { super(errorMessage, cause); }

	public static class ConfigParseExceptionBuilder {
		private String description;
		private String filePath;
		private int lineNumber;
		private Throwable cause;

		public ConfigParseExceptionBuilder () {}

		public ConfigParseExceptionBuilder appendDescription (String description) {
			this.description = description;
			return this;
		}

		public ConfigParseExceptionBuilder appendFilePath (String filePath) {
			this.filePath = filePath;
			return this;
		}

		public ConfigParseExceptionBuilder appendLineNumber (int lineNumber) {
			this.lineNumber = lineNumber;
			return this;
		}

		public ConfigParseExceptionBuilder appendCause (Throwable cause) {
			this.cause = cause;
			return this;
		}

		public ConfigParseException build () {
			String errorMessage = this.description + "\n"
						+ String.format("file: %s", this.filePath) + "\n"
						+ String.format("lineNumber: %d", this.lineNumber);
			ConfigParseException exception = (this.cause == null) ?
					new ConfigParseException(errorMessage) :
					new ConfigParseException(errorMessage, this.cause);
			return exception;
		}
	}
}
