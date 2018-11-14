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