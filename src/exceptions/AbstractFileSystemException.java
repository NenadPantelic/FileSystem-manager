package exceptions;

public abstract class AbstractFileSystemException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String _message;

    public AbstractFileSystemException(String message) {
	setMessage(message);
    }
    public String getMessage() {
	return _message;
    }

    public void setMessage(String message) {
	_message = message;
    }
}
