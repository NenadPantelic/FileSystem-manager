package services;

import java.io.IOException;

import exceptions.FilenameException;
import exceptions.InvalidFSOperationException;
import exceptions.NotEnoughSpaceException;
import exceptions.RootException;
import filesystem.FileType;
import filesystem.UserType;
import fstree.FSTree;

public class FSCreatorService {

    private FSTree _tree;

    /* get only name from path - the last field in separation*/
    public String getName(String path) {
	String[] dirs = path.split("/");
	return dirs[dirs.length - 1];
    }

    /* creates FileSystem Tree instance */
    public FSTree createFS(String rootPath, long size)
	    throws InvalidFSOperationException, IOException, NotEnoughSpaceException, RootException, FilenameException {
	_tree = FSTree.getFSTree();
	_tree.setCapacity(size);
	_tree.setRootPath(rootPath);
	_tree.createNode(null, getName(rootPath), UserType.USER, _tree.getRootPath(), FileType.DIRECTORY, null, null);

	// _tree.createNode(null, FileType.DIRECTORY, getName(rootPath), UserType.USER);
	return _tree;
    }

}
