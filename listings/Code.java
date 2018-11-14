
/* Adds node to FSTree and makes its physical file root - node where node is added node - FSNode that we add to FSTree*/
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
    
    
        public boolean makeFile(FSNode root) throws IOException {
            getNodeValue().setFile(new File(root.getNodeValue().getAbsolutePath() + "/" + getNodeName()));
            if (getFileType().equals(FileType.FILE)) {
        	return getNodeValue().getFile().createNewFile();
            } else {
        	return getNodeValue().getFile().mkdir();

            }
        }

    
        
        
        /*
        * clone node content, except file content - parent, child list, file object
        */
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
    
    
        /* write content to file */
        public void appendContentToNode(FSNode file, String content) throws IOException, InvalidFSOperationException {

            file.writeNodeFile(content);
    }
