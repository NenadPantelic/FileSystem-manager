package exceptions;

public class NotEnoughSpaceException extends AbstractFileSystemException {

    private static final long serialVersionUID = 1L;

    public NotEnoughSpaceException(String message) {
	super(message);
    }

}
