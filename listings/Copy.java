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