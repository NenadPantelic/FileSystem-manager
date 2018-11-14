package fstree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import filesystem.FSFolder;
import filesystem.FSObject;
import filesystem.FileFactory;
import filesystem.FileType;
import filesystem.UserType;

public class NodeBuilder {

    public static FSNode build(FSNode node) {
	return new FSNode(node.getNodeValue(), node.getParent(), node.getChildList());

    }

    public static FSNode build(FSObject object) {
	return new FSNode(object, null, new ArrayList<FSNode>());

    }

    public static FSNode build(FSObject object, FSNode parrent, List<FSNode> children) {
	if (children == null)
	    children = new ArrayList<FSNode>();
	return new FSNode(object, parrent, children);

    }

    public static FSNode build(String name, UserType ut, String path) {

	return build(new FSFolder(name, ut, path));
    }

    public static FSNode build(String name, UserType ut, String path, FileType ft, FSNode parrent,
	    List<FSNode> children) throws IOException {
	FileFactory fileFact = new FileFactory();
	return build(fileFact.createFSObject(ft, name, ut, path));
    }

}
