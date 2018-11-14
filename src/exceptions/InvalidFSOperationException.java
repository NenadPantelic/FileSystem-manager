package exceptions;

public class InvalidFSOperationException extends AbstractFileSystemException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public InvalidFSOperationException(String message) {
	super(message);
    }

}
