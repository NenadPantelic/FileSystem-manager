package filesystem;

import java.io.File;
import java.io.IOException;

import exceptions.InvalidPrivilegeException;

public abstract class FSObject {

    private File _file;
    private String _name;
    private String _path;
    private UserType _userType;

    public FSObject(String name, UserType ut, String path) {
	_name = name;
	_userType = ut;
	_path = path;

    }

    public String getAbsolutePath() {
	return getFile().getAbsolutePath();
    }

    public File getFile() {
	return _file;
    }

    public UserType getFileType() {

	return _userType;
    }

    public String getName() {

	return _name;
    }

    public String getPath() {
	return _path;
    }

    public abstract long getSize();

    public boolean isOperationPermitted() {
	boolean res = true;
	if (getFileType().equals(UserType.SYSTEM))
	    res = false;
	return res;
    }

    public abstract boolean remove() throws InvalidPrivilegeException, IOException;

    public void setFile(File file) {
	_file = file;
    }

    public void setFileType(UserType ut) {

	_userType = ut;
    }

    public void setName(String name) {

	_name = name;
    }

    public void setPath(String path) {
	_path = path;
    }

    @Override
    public String toString() {
	return "File: " + getName();
    }

}
