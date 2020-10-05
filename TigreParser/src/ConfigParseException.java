class ConfigParseException extends Exception {
	ConfigParseException (String errorMessage) { super(errorMessage); }

	ConfigParseException (String errorMessage, Throwable cause) { super(errorMessage, cause); }

	static class ConfigParseExceptionBuilder {
		private String description;
		private String filePath;
		private int lineNumber;
		private Throwable cause;

		ConfigParseExceptionBuilder () {}

		ConfigParseExceptionBuilder appendDescription (String description) {
			this.description = description;
			return this;
		}

		ConfigParseExceptionBuilder appendFilePath (String filePath) {
			this.filePath = filePath;
			return this;
		}

		ConfigParseExceptionBuilder appendLineNumber (int lineNumber) {
			this.lineNumber = lineNumber;
			return this;
		}

		ConfigParseExceptionBuilder appendCause (Throwable cause) {
			this.cause = cause;
			return this;
		}

		ConfigParseException build () {
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
