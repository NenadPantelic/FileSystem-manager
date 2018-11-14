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