package exceptions;

import filesystem.FSFile;
import filesystem.FSFolder;
import filesystem.FSObject;
import filesystem.UserType;
import fstree.FSNode;

/* Interceptor class - handle all exception in fstree and and filesystem package */
public class Interceptor {

    private static boolean isFile(FSObject object) {
	return (object instanceof FSFile);
    }

    private static boolean isFolder(FSObject object) {
	return (object instanceof FSFolder);
    }

    public static void fileOpIntercept(FSObject object, String op) throws InvalidFSOperationException {
	if (!(isFile(object))) {
	    if (op.equals("r"))
		throw new InvalidFSOperationException("Only files can be read!");
	    if (op.equals("w"))
		throw new InvalidFSOperationException("Only files can be writen!");

	}
    }

    public static void folderIntercept(FSObject object) throws InvalidFSOperationException {
	if (!(isFolder(object)))
	    throw new InvalidFSOperationException("Root node is not folder!");
    }

    public static void authorizationCheckIntercept(UserType type) throws InvalidPrivilegeException {
	if (type.equals(UserType.SYSTEM))
	    throw new InvalidPrivilegeException("You are not authorized to execute this action!");

    }

    public static void unexistedFileIntercept(FSNode root, FSNode file) throws InvalidFSOperationException {
	if (!root.getChildList().contains(file))
	    throw new InvalidFSOperationException("File with this name does not exists in marked folder!");
    }

    public static void copySourceAndDestIntecept(FSObject obj1, FSObject obj2) throws InvalidFSOperationException {
	if (!(isFolder(obj1) && isFolder(obj2)))
	    throw new InvalidFSOperationException("Source or destination node is not folder!");

    }

    public static void fsRootIntercept(String root) throws RootException {
	if (root == null)
	    throw new RootException("You didn't enter root directory of file system!");

    }

    public static void existingFilenameIntercept(FSNode node, String name) throws FilenameException {
	if (node != null) {
	    throw new FilenameException(
		    "The name \"" + name + "\" is already used in this location. Please use a different name.");
	}
    }

    public static void outOfMemoryIntercept(long size, long freeSpace) throws NotEnoughSpaceException {
	if (freeSpace < size)
	    throw new NotEnoughSpaceException("Not enough memory for this operation!");

    }

    public static void rootSetIntercept(FSNode exRoot, FSNode newRoot) throws InvalidFSOperationException {
	if (exRoot != null || newRoot == null)
	    throw new InvalidFSOperationException(
		    "Filesystem root cannot be changed!You entered wrong root value or FileSystem is already instantiated");
    }

    public static void fsCapacityIntercept(long exCapacity, long newCapacity) throws InvalidFSOperationException {
	if (exCapacity != 0 || newCapacity == 0)
	    throw new InvalidFSOperationException(
		    "Size of file system cannot be changed! You entered wrong size value or FileSystem is already instantiated");

    }

    public static void rootRemoveIntercept(FSNode node, FSNode root) throws InvalidPrivilegeException {
	if (node == root)
	    throw new InvalidPrivilegeException("You are not authorized to perform this action!");

    }

}
