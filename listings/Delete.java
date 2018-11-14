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
