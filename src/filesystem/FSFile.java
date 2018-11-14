package filesystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import exceptions.Interceptor;
import exceptions.InvalidPrivilegeException;

public class FSFile extends FSObject {

    /*
     * UNIX - "/" Windows - "\"
     */
    public FSFile(String name, UserType ut, FSFolder folder) throws IOException {
	super(name, ut, folder.getPhysicalPath() + "/" + name);
	setFile(new File(getPath()));
	getFile().createNewFile();
    }

    /*
     * UNIX - "/" Windows - "\"
     */
    public FSFile(String name, UserType ut, String path) throws IOException {
	super(name, ut, path + "/" + name);
	setFile(new File(getPath()));
	try {
	    getFile().createNewFile();
	} catch (Exception ex) {
	    System.out.println(ex.getMessage());
	}
    }

    public boolean clearContent() throws IOException {
	if (isOperationPermitted() && getFile().canRead()) {
	    FileWriter fileWriter = new FileWriter(getFile());
	    fileWriter.write("");
	    fileWriter.close();
	    return true;
	}
	return false;
    }

    public String getPhysicalPath() {
	return getFile().getAbsolutePath();
    }

    public long getSize() {
	return getFile().length();
    }

    public String read() throws IOException {
	String data = "";
	if (isOperationPermitted() && getFile().canRead()) {
	    data = new String(Files.readAllBytes(Paths.get(getFile().getAbsolutePath())));
	}

	return data;
    }

    @Override
    public boolean remove() throws InvalidPrivilegeException {
	Interceptor.authorizationCheckIntercept(getFileType());
	getFile().delete();
	return true;

    }

    public boolean write(String content) throws IOException {

	if (isOperationPermitted() && getFile().canWrite()) {
	    FileWriter fileWriter = new FileWriter(getFile(), true);
	    fileWriter.write(content);
	    fileWriter.close();
	    return true;
	}

	return false;
    }

}
