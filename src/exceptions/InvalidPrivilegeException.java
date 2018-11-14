package exceptions;

public class InvalidPrivilegeException extends AbstractFileSystemException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public InvalidPrivilegeException(String message) {
	super(message);
    }

}
