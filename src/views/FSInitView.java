package views;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class FSInitView {

    static final long MAX_SIZE = 50 * 1024 * 1024;
    private JTextField _fsSize;
    private JPanel _gui;
    private JTextField _rootDir;
    private JPanel _rootFSPanel;
    private JPanel _sizeFSPanel;

    public FSInitView() {
	_rootFSPanel = new JPanel(new BorderLayout(3, 3));
	_sizeFSPanel = new JPanel(new BorderLayout(3, 3));
	_rootDir = new JTextField(15);
	_fsSize = new JTextField(15);
	_rootFSPanel.add(new JLabel("Root directory (abs path)"), BorderLayout.WEST);
	_rootFSPanel.add(_rootDir);
	_sizeFSPanel.add(new JLabel("Filesystem size in MB"), BorderLayout.WEST);
	_sizeFSPanel.add(_fsSize);

	_gui = new JPanel(new BorderLayout(3, 3));
	_gui.setBorder(new EmptyBorder(5, 5, 5, 5));

	_rootFSPanel.add(new JPanel(new GridLayout(2, 0, 2, 2)), BorderLayout.SOUTH);
	_sizeFSPanel.add(new JPanel(new GridLayout(2, 0, 2, 2)), BorderLayout.SOUTH);
    }

    public JTextField getFsSize() {
	return _fsSize;
    }

    public JPanel getGUI() {
	return _gui;
    }

    public JTextField getRootDir() {
	return _rootDir;
    }

    public JPanel getRootFSPanel() {
	return _rootFSPanel;
    }

    public String getRootPath() {
	return getRootDir().getText();
    }

    public long getSize() {
	return Long.parseLong(getFsSize().getText()) * 1024 * 1024;
    }

    public JPanel getSizeFSPanel() {
	return _sizeFSPanel;
    };

    public boolean isValidPath(String path) {
	Path filepath = Paths.get(path);
	return Files.exists(filepath);

    }

    public int makeRootDialog() {
	int successfullFlag = 0;
	int result = JOptionPane.showConfirmDialog(getGUI(), getRootFSPanel(), "Create new File System",
		JOptionPane.OK_CANCEL_OPTION);

	if (result == JOptionPane.OK_OPTION) {
	    String root = getRootDir().getText();
	    boolean validationRoot = validateRoot(root);
	    System.out.println(validationRoot);
	    if (!validationRoot) {
		successfullFlag = 1;

	    }

	} else {
	    successfullFlag = -1;
	}

	return successfullFlag;
    }

    public int makeSizeDialog() {
	int successfullFlag = 0;
	int result = JOptionPane.showConfirmDialog(getGUI(), getSizeFSPanel(), "Create new File System",
		JOptionPane.OK_CANCEL_OPTION);

	if (result == JOptionPane.OK_OPTION) {
	    long size;
	    try {
		size = Long.parseLong(getFsSize().getText()) * 1024 * 1024;
	    } catch (Exception ex) {
		size = 0;
	    }
	    boolean validationRoot = validateSize(size);
	    System.out.println(validationRoot);
	    if (!validationRoot) {
		successfullFlag = 1;

	    }

	} else {
	    successfullFlag = -1;
	}

	return successfullFlag;
    }

    public void setFsSize(JTextField fsSize) {
	_fsSize = fsSize;
    }

    public void setRootDir(JTextField rootDir) {
	_rootDir = rootDir;
    }

    public void setRootFSPanel(JPanel rootFSPanel) {
	_rootFSPanel = rootFSPanel;
    }

    public void setSizeFSPanel(JPanel sizeFSPanel) {
	_sizeFSPanel = sizeFSPanel;
    }

    public int validateData(String rootPath, long size) {
	int res = 0;
	if (size == 0 && rootPath == null)
	    res = 1;
	else if (size <= 0 || size > MAX_SIZE)
	    res = 2;

	else if (!isValidPath(rootPath))
	    res = 3;

	return res;
    }

    public boolean validateRoot(String rootPath) {

	boolean res = true;
	if (rootPath == null || rootPath.equals("") || !isValidPath(rootPath))
	    res = false;

	return res;
    }

    public boolean validateSize(long size) {
	boolean res = true;
	if (size <= 0 || size > MAX_SIZE)
	    res = false;
	return res;
    }

}
