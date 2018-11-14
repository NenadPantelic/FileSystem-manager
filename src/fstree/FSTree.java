package fstree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import exceptions.FilenameException;
import exceptions.Interceptor;
import exceptions.InvalidFSOperationException;
import exceptions.InvalidPrivilegeException;
import exceptions.NotEnoughSpaceException;
import exceptions.RootException;
import filesystem.FSFolder;
import filesystem.FSObject;
import filesystem.FileFactory;
import filesystem.FileType;
import filesystem.UserType;

public class FSTree {

    private static FSTree _tree = null;

    public static FSTree getFSTree() {
	if (_tree == null)
	    _tree = new FSTree();
	return _tree;
    }

    private long _capacity;
    private long _filledSpace;
    private FSNode _root;
    private String _rootPath;

    private FSTree() {

    }

    private FSTree(FSNode root, long capacity) {

	_root = root;
	_capacity = capacity;
    }

    /*
     * Adds node to FSTree and makes its physical file root - node where node is
     * added node - FSNode that we add to FSTree
     */
    public boolean addNode(FSNode root, FSNode node)
	    throws InvalidFSOperationException, NotEnoughSpaceException, IOException {
	FSNode copyRoot = (getRoot() == null) ? node : root;
	Interceptor.folderIntercept(copyRoot.getNodeValue());
	if (copyRoot == node) {
	    setRoot(node);

	} else {
	    if (!node.ifFileExists()) {
		node.makeFile(root);
	    }
	    node.setParent(root);
	    root.addChild(node);
	}
	useSpace(node);
	return true;
    }

    /* write content to file */
    public void appendContentToNode(FSNode file, String content) throws IOException, InvalidFSOperationException {

	file.writeNodeFile(content);
    }

    public boolean checkNodeType(FSNode node) {

	return (node.getNodeValue() instanceof FSFolder);
    }

    /*
     * Copies copyNode to dest and makes it new root of copyNode If copyNode is
     * directory, recursively copy all of its children. copyFromNode method that is
     * part of FSNOde class copies from one node to another - makes copy of its
     * content
     * 
     */
    public boolean copyNode(FSNode dest, FSNode copyNode) throws IOException, InvalidFSOperationException,
	    NotEnoughSpaceException, FilenameException, InvalidPrivilegeException {
	FSNode node = new FSNode();
	FSNode existingNode = searchFolder(dest, copyNode.getNodeName());
	Interceptor.existingFilenameIntercept(existingNode, copyNode.getNodeName());
	Interceptor.authorizationCheckIntercept(copyNode.getNodeType());

	node.copyFromNode(dest, copyNode, copyNode.getNodeName());
	useSpace(node);
	if (copyNode.getFileType() == FileType.DIRECTORY) {
	    for (FSNode n : copyNode.getChildList()) {
		copyNode(node, n);

	    }
	}
	if (copyNode.getFileType() == FileType.FILE) {

	    appendContentToNode(node, copyNode.getFileContent());

	}

	return true;
    }

    /* creates node */
    public boolean createNode(FSNode root, FileType ft, String name, UserType ut)
	    throws IOException, InvalidFSOperationException, NotEnoughSpaceException, RootException {
	FileFactory fileFactory = new FileFactory();
	if (getRoot() == null) {
	    Interceptor.fsRootIntercept(getRootPath());
	    FSObject object = fileFactory.createFSObject(ft, name, ut, getRootPath());
	    return addNode(root, NodeBuilder.build(object));

	}

	if (root != null)
	    Interceptor.folderIntercept(root.getNodeValue());

	FSObject object = fileFactory.createFSObject(ft, name, ut, root.getNodeValue());

	return addNode(root, NodeBuilder.build(object));

    }

    /*
     * Overload of createNode method - more complete one. Creates node - if root of
     * tree doesn't exist, make this node root of tree. Uses NodeBuilder to create
     * node from added parameters. Add node to tree at the end.
     */
    public FSNode createNode(FSNode root, String name, UserType ut, String path, FileType ft, FSNode parrent,
	    List<FSNode> children)
	    throws IOException, FilenameException, InvalidFSOperationException, NotEnoughSpaceException, RootException {

	FSNode createdNode = null;
	if (getRoot() == null) {
	    Interceptor.fsRootIntercept(getRootPath());
	    createdNode = NodeBuilder.build(name, ut, path, ft, null, new ArrayList<FSNode>());
	    if (addNode(root, createdNode))
		return createdNode;

	}
	Interceptor.folderIntercept(root.getNodeValue());
	FSNode node = searchFolder(root, name);
	Interceptor.existingFilenameIntercept(node, name);
	createdNode = NodeBuilder.build(name, ut, path, ft, parrent, children);
	if (addNode(root, createdNode))
	    return createdNode;
	return null;
    }

    public void freeSpace(FSNode node) {
	setFilledSpace(getFilledSpace() - node.getNodeSize());

    }

    public void freeSpace(long size) {
	setFilledSpace(getFilledSpace() - size);

    }

    public long getCapacity() {
	return _capacity;
    }

    public String getFileContent(FSNode node) throws IOException, InvalidFSOperationException {
	return node.getFileContent();
    }

    public FSObject getFileFromNodeFolder(FSNode folder, String name) throws InvalidFSOperationException {
	Interceptor.folderIntercept(folder.getNodeValue());
	return folder.getNodeFile(name);
    }

    public long getFilledSpace() {
	return _filledSpace;
    }

    public long getFreeSpace() {
	return getCapacity() - getFilledSpace();
    }

    public List<FSNode> getNodeChildren(FSNode parrent) {
	return parrent.getChildList();
    }

    public FSNode getRoot() {
	return _root;
    }

    public List<FSNode> getRootChildren() {
	return getRoot().getChildList();
    }

    public File getRootFile() {
	return getRoot().getFileFromNode();
    }

    public String getRootPath() {
	return _rootPath;
    }

    public boolean isEmpty() {
	return (getFilledSpace() == 0);
    }

    public boolean isFull() {
	return (getCapacity() == getFilledSpace());
    }

    /*
     * Move node from oldRoot to newRoot. Move operation = copy node to new
     * destination + removes node from the old root. This operation should be
     * atomic.
     * 
     */
    public boolean moveNode(FSNode oldRoot, FSNode newRoot, FSNode moveNode) throws InvalidFSOperationException,
	    IOException, InvalidPrivilegeException, NotEnoughSpaceException, RootException, FilenameException {
	Interceptor.unexistedFileIntercept(oldRoot, moveNode);

	boolean copyFlag = copyNode(newRoot, moveNode);
	boolean removedFlag = false;
	if (copyFlag) {
	    removedFlag = removeNode(moveNode);
	    if (removedFlag)
		return true;
	    removeNode(newRoot.getChildByName(moveNode.getNodeName()));
	}

	return false;

    }

    /*
     * removes node and all of its descendants - for directory. Also, free space
     * that node occupied.
     * 
     * 
     */
    public boolean removeNode(FSNode node) throws InvalidPrivilegeException, IOException {
	Interceptor.authorizationCheckIntercept(node.getNodeType());
	Interceptor.rootRemoveIntercept(node, getRoot());
	long size = node.getNodeSize();
	if (node.removeNodeFile()) {
	    freeSpace(size);
	    FSNode root = node.getParent();
	    root.getChildList().remove(node);
	    node.setParent(null);
	    node.getChildList().clear();

	    return true;
	}
	return false;

    }

    /*
     * Renames renamingNode in rootNode to new name - newName Makes new node with
     * new name and copies from renamingNode to it. Add all of renamingNode children
     * to this new node. Append content to new node and deletes the old one.
     */
    public FSNode renameNode(FSNode rootNode, FSNode renamingNode, String newName)
	    throws IOException, InvalidFSOperationException, InvalidPrivilegeException, NotEnoughSpaceException,
	    RootException, FilenameException {

	FSNode existingNode = searchFolder(rootNode, newName);
	Interceptor.existingFilenameIntercept(existingNode, newName);
	FSNode renamedNode = new FSNode();
	renamedNode.copyFromNode(rootNode, renamingNode, newName);

	if (renamedNode.getFileType().equals(FileType.DIRECTORY)) {
	    for (FSNode childNode : renamingNode.getChildList()) {

		copyNode(renamedNode, childNode);

	    }
	} else
	    appendContentToNode(renamedNode, renamingNode.getFileContent());
	removeNode(renamingNode);
	return renamedNode;

    }

    /*
     * Search node by its absolute path - location of node in tree and filesystem.
     */
    public FSNode search(FSNode root, String absPath) {
	if (root.getNodePath().equals(absPath)) {
	    return root;

	}
	for (FSNode child : root.getChildList()) {
	    if (child.getFileType().equals(FileType.DIRECTORY) && child.getChildList().size() > 0) {
		FSNode resultNode = search(child, absPath);
		if (resultNode != null)
		    return resultNode;
	    } else if (child.getNodePath().equals(absPath))
		return child;
	}
	return null;
    }

    /*
     * Search nodes by keyword - results that satisfy query store in foundNodes list
     */
    public void searchByWord(FSNode root, String keyWord, List<FSNode> foundNodes) {

	if (root.getNodeName().toLowerCase().contains(keyWord)) {
	    foundNodes.add(root);

	}
	for (FSNode child : root.getChildList()) {
	    if (child.getFileType().equals(FileType.DIRECTORY) && child.getChildList().size() > 0) {
		searchByWord(child, keyWord, foundNodes);

	    } else if (child.getNodeName().toLowerCase().contains(keyWord))
		foundNodes.add(child);

	}

    }

    public FSNode searchFolder(FSNode folder, String name) {
	return folder.getChildByName(name);

    }

    public void setCapacity(long capacity) throws InvalidFSOperationException {
	Interceptor.fsCapacityIntercept(getCapacity(), capacity);
	_capacity = capacity;
    }

    public void setFilledSpace(long filledSpace) {
	_filledSpace = filledSpace;
    }

    private void setRoot(FSNode root) throws InvalidFSOperationException {
	Interceptor.rootSetIntercept(getRoot(), root);
	_root = root;
    }

    public void setRootPath(String rootPath) {
	_rootPath = rootPath;
    }

    public void useSpace(FSNode node) throws NotEnoughSpaceException {
	Interceptor.outOfMemoryIntercept(node.getNodeSize(), getFreeSpace());
	setFilledSpace(getFilledSpace() + node.getNodeSize());
    }
}
