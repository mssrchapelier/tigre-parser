class ConfigParseException extends Exception {
	ConfigParseException (String errorMessage) { super(errorMessage); }

	ConfigParseException (String errorMessage, Throwable cause) { super(errorMessage, cause); }
}
