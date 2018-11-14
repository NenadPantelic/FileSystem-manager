package filesystem;

import java.io.IOException;

public class FileFactory {

    private FSObject _retFile;

    public FSObject createFSObject(FileType ft, String name, UserType ut, String path) throws IOException {
	if (ft.equals(FileType.FILE))
	    _retFile = new FSFile(name, ut, path);
	else
	    _retFile = new FSFolder(name, ut, path);

	return _retFile;
    }

    public FSObject createFSObject(FileType ft, String name, UserType ut, FSObject folder) throws IOException {
	if (ft.equals(FileType.FILE))
	    _retFile = new FSFile(name, ut, (FSFolder) folder);
	else
	    _retFile = new FSFolder(name, ut, (FSFolder) folder);

	return _retFile;
    }

    public FSObject copyObject(FileType ft, String name, UserType ut, String path, String content) throws IOException {
	if (ft.equals(FileType.FILE)) {
	    _retFile = new FSFile(name, ut, path);

	} else
	    _retFile = new FSFolder(name, ut, path);

	return _retFile;
    }
}
