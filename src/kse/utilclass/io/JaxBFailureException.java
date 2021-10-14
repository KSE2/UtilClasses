package kse.utilclass.io;

import java.io.IOException;

public class JaxBFailureException extends IOException {

	public JaxBFailureException() {
	}

	public JaxBFailureException(String message) {
		super(message);
	}

	public JaxBFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public JaxBFailureException(Throwable cause) {
		super(cause);
	}

	
}
