package fstree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import exceptions.Interceptor;
import exceptions.InvalidFSOperationException;
import exceptions.InvalidPrivilegeException;
import filesystem.FSFile;
import filesystem.FSFolder;
import filesystem.FSObject;
import filesystem.FileFactory;
import filesystem.FileType;
import filesystem.UserType;

public class FSNode {

    private List<FSNode> _childList;
    private FSObject _nodeValue;
    private FSNode _parent;

    public FSNode() {
	_childList = new ArrayList<FSNode>();
    }

    public FSNode(FSObject node, FSNode parent, List<FSNode> children) {
	_nodeValue = node;
	_parent = parent;
	_childList = children;
    }

    public void addChild(FSNode child) {
	_childList.add(child);
	addFileToFolder(child);

    }

    public void addFileToFolder(FSNode node) {
	FSFolder folder = (FSFolder) getNodeValue();
	folder.addFile(node.getNodeValue());
    }

    public void copyFromNode(FSNode root, FSNode node, String nameOfNode)
	    throws IOException, InvalidFSOperationException {
	setParent(root);
	FileFactory fileFact = new FileFactory();
	FSObject obj = fileFact.createFSObject(node.getFileType(), nameOfNode, node.getNodeType(),
		(FSFolder) root.getNodeValue());

	setNode(obj);
	root.addChild(this);

	makeFile(root);

    }

    public boolean extendChildList(List<FSNode> chList) {
	for (FSNode child : chList)
	    addChild(child);
	return true;
    }

    public FSNode getChildByName(String name) {
	FSNode retNode = null;
	for (FSNode node : getChildList()) {
	    if (node.getNodeName().equals(name)) {
		retNode = node;
		break;
	    }
	}
	return retNode;
    }

    public List<FSNode> getChildList() {
	return _childList;
    }

    public String getFileContent() throws IOException, InvalidFSOperationException {
	Interceptor.fileOpIntercept(getNodeValue(), "r");
	FSFile file = (FSFile) getNodeValue();
	return file.read();
    }

    public File getFileFromNode() {
	return getNodeValue().getFile();
    }

    public FileType getFileType() {
	return (getNodeValue() instanceof FSFolder) ? FileType.DIRECTORY : FileType.FILE;
    }

    public FSObject getNodeFile(String name) throws InvalidFSOperationException {
	Interceptor.folderIntercept(getNodeValue());
	FSFolder folder = (FSFolder) getNodeValue();
	return folder.getFile(name);
    }

    public String getNodeName() {
	return getNodeValue().getName();
    }

    public String getNodePath() {
	return getNodeValue().getAbsolutePath();
    }

    public long getNodeSize() {
	return getNodeValue().getSize();
    }

    public UserType getNodeType() {
	return getNodeValue().getFileType();
    }

    public FSObject getNodeValue() {
	return _nodeValue;
    }

    public FSNode getParent() {
	return _parent;
    }

    public boolean hasChild(FSNode child) {
	if (getChildList().contains(child))
	    return true;
	return false;
    }

    public boolean hasName(String name) {
	return getNodeName() == name;
    }

    public boolean ifFileExists() {
	return getFileFromNode().exists();
    }

    public boolean makeFile(FSNode root) throws IOException {
	getNodeValue().setFile(new File(root.getNodeValue().getAbsolutePath() + "/" + getNodeName()));
	if (getFileType().equals(FileType.FILE)) {
	    return getNodeValue().getFile().createNewFile();
	} else {
	    return getNodeValue().getFile().mkdir();

	}
    }

    public boolean removeNodeFile() throws InvalidPrivilegeException, IOException {
	return getNodeValue().remove();

    }

    public FSObject searchLocally(String name) throws InvalidFSOperationException {
	Interceptor.folderIntercept(getNodeValue());
	FSObject obj = (((FSFolder) getNodeValue()).getFile(name));
	return obj;
    }

    public void setChildList(List<FSNode> childList) {
	_childList = childList;
    }

    public void setNode(FSObject node) {
	_nodeValue = node;
    }

    public void setParent(FSNode parent) {
	_parent = parent;
    }

    @Override
    public String toString() {
	return "Node: " + getNodeName();
    }

    public void writeNodeFile(String content) throws IOException, InvalidFSOperationException {
	Interceptor.fileOpIntercept(getNodeValue(), "w");
	FSFile file = (FSFile) getNodeValue();
	file.write(content);

    }
}
