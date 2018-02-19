package org.chat21.android.core.exception;

/**
 * Created by andrealeo on 05/12/17.
 */

public class ChatRuntimeException extends RuntimeException {

    public ChatRuntimeException() {
        super();
    }

    public ChatRuntimeException(String message) {
        super(message);
    }

    public ChatRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatRuntimeException(Throwable cause) {
        super(cause);
    }

}
