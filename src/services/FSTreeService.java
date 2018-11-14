package services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import exceptions.FilenameException;
import exceptions.InvalidFSOperationException;
import exceptions.InvalidPrivilegeException;
import exceptions.NotEnoughSpaceException;
import exceptions.RootException;
import filesystem.FileType;
import filesystem.UserType;
import fstree.FSNode;
import fstree.FSTree;

public class FSTreeService {

    private FSNode _currentNode;
    private FSTree _tree;

    public FSTreeService(FSTree tree) {
	_tree = tree;
    }

    public boolean copyFileNode(FSNode destinationNode, FSNode nodeForCopy) throws InvalidFSOperationException,
	    NotEnoughSpaceException, FilenameException, InvalidPrivilegeException, IOException {
	return getTree().copyNode(destinationNode, nodeForCopy);
    }

    public int countRootChildren() {
	return getTree().getRootChildren().size();
    }

    public FSNode createFileNode(FSNode root, String name, UserType ut, String path, FileType ft, FSNode parent,
	    List<FSNode> children)
	    throws FilenameException, InvalidFSOperationException, NotEnoughSpaceException, RootException, IOException {
	return getTree().createNode(root, name, ut, path, ft, parent, children);
    }

    public File getCurrentFile() {
	return getCurrentNode().getFileFromNode();
    }

    public FSNode getCurrentNode() {
	return _currentNode;
    }

    public String getCurrentNodeName() {
	return getCurrentNode().getNodeName();
    }

    public FSNode getCurrentNodeParent() {
	return getCurrentNode().getParent();
    }

    public UserType getCurrentUType() {
	return getCurrentNode().getNodeType();
    }

    public File getFileFromNode(FSNode node) {
	return node.getFileFromNode();
    }

    public File getParentFile(FSNode node) {
	return getParrent(node).getFileFromNode();
    }

    public FSNode getParrent(FSNode node) {
	return node.getParent();
    }

    public File getRootFile() {
	return getTree().getRootFile();
    }

    public File getSearchedFile(FSNode root, String absPath) {
	return treeSearch(root, absPath).getFileFromNode();
    }

    public FSTree getTree() {
	return _tree;
    }

    public long getTreeCapacity() {
	return getTree().getCapacity();
    }

    public FSNode getTreeRoot() {
	return getTree().getRoot();
    }

    public long getUsedSpace() {
	return getTree().getFilledSpace();
    }

    public boolean moveFileNode(FSNode currentParent, FSNode newParent, FSNode movingNode)
	    throws InvalidFSOperationException, InvalidPrivilegeException, NotEnoughSpaceException, RootException,
	    FilenameException, IOException {
	return getTree().moveNode(currentParent, newParent, movingNode);
    }

    public boolean removeFile(FSNode node) throws InvalidPrivilegeException, IOException {
	return getTree().removeNode(node);

    }

    public FSNode renameFileNode(FSNode root, FSNode newNode, String newName) throws InvalidFSOperationException,
	    InvalidPrivilegeException, NotEnoughSpaceException, RootException, FilenameException, IOException {
	return getTree().renameNode(root, newNode, newName);
    }

    public String search(FSNode folderNode, String word, List<FSNode> resNodes) {
	getTree().searchByWord(folderNode, word, resNodes);
	StringBuilder paths = null;

	if (resNodes.size() > 0) {
	    paths = new StringBuilder();
	    paths.append("The following files satisfy search query\n");
	    for (FSNode node : resNodes) {
		paths.append(node.getNodePath() + "\n");

	    }
	}
	String message = "No files were found!";
	if (paths != null)
	    message = paths.toString();
	return message;
    }

    public FSNode searchByPath(FSNode root, String absolutePath) {
	return getTree().search(root, absolutePath);
    }

    public void setCurrentNode(FSNode currentNode) {
	_currentNode = currentNode;
    }

    public void setTree(FSTree tree) {
	_tree = tree;
    }

    public FSNode treeSearch(FSNode root, String absPath) {
	return getTree().search(root, absPath);
    }

}
