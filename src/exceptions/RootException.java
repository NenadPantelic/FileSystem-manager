package exceptions;

public class RootException extends AbstractFileSystemException {

    private static final long serialVersionUID = 1L;

    public RootException(String message) {
	super(message);
    }

}
