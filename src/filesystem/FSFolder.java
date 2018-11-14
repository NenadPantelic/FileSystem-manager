package filesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import exceptions.Interceptor;
import exceptions.InvalidPrivilegeException;

public class FSFolder extends FSObject {

    private List<FSObject> _folderContent;

    public FSFolder(String name, UserType ut, FSFolder folder) {

	super(name, ut, folder.getPhysicalPath() + "/" + name);
	_folderContent = new ArrayList<FSObject>();
	setFile(new File(getPath()));
	getFile().mkdirs();

    }

    /*
     * UNIX - "/" Windows - "\"
     */
    public FSFolder(String name, UserType ut, String path) {
	super(name, ut, path + "/" + name);
	_folderContent = new ArrayList<FSObject>();

	setFile(new File(getPath()));
	getFile().mkdirs();

    }

    public void addFile(FSObject object) {
	getFolderContent().add(object);
    }

    public void extendFolder(FSFolder folder) {
	getFolderContent().addAll(folder.getFolderContent());
    }

    public File[] getAllPhysicalFiles() {
	return getFile().listFiles();
    }

    public FSObject getFile(String name) {
	for (FSObject object : getFolderContent()) {
	    if (object.getName().equals(name))
		return object;
	}
	return null;
    }

    public List<FSObject> getFolderContent() {

	return _folderContent;
    }

    public String getPhysicalPath() {
	return getFile().getAbsolutePath();
    }

    @Override
    public long getSize() {
	long folderSize = getFile().length();
	for (FSObject object : getFolderContent())
	    folderSize += object.getSize();
	return folderSize;
    }

    public boolean isFolderEmpty() {
	return getFolderContent().isEmpty();
    }

    @Override
    public boolean remove() throws InvalidPrivilegeException, IOException {

	Interceptor.authorizationCheckIntercept(getFileType());
	for (FSObject file : getFolderContent()) {
	    Interceptor.authorizationCheckIntercept(file.getFileType());
	    if (file instanceof FSFolder)
		((FSFolder) file).getFolderContent().clear();

	}
	getFolderContent().clear();
	FileUtils.deleteDirectory(getFile());
	return true;

    }

    public void removeFile(FSObject object) {
	getFolderContent().remove(object);
    }

    public void removeFiles() {
	getFolderContent().clear();
    }

    public void setFolderContent(List<FSObject> folderContent) {
	_folderContent = folderContent;
    }

}
