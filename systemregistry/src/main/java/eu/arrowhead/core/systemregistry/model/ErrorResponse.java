package eu.arrowhead.core.systemregistry.model;

import java.io.Serializable;

public class ErrorResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String message;

	public ErrorResponse(final String message) {
		this.message = message;
	}

	public ErrorResponse(final Throwable throwable) {
		this.message = throwable.getMessage();
	}

	public String getMessage() {
		return message;
	}
}
