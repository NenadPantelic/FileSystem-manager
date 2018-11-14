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