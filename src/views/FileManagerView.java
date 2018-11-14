/* Note: This module is modified.
 * $Id$
 *
 * Copyright 2015 Valentyn Kolesnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import exceptions.InvalidPrivilegeException;
import filesystem.FileType;
import filesystem.UserType;
import fstree.FSNode;
import fstree.FSTree;
import services.FSTreeService;

/**
 * A basic File Manager. Requires 1.6+ for the Desktop &amp; SwingWorker
 * classes, amongst other minor things.
 * 
 * Includes support classes FileTableModel &amp; FileTreeCellRenderer.
 * 
 * TODO Bugs
 * <ul>
 * <li>Still throws occasional AIOOBEs and NPEs, so some update on the EDT must
 * have been missed.
 * <li>Fix keyboard focus issues - especially when functions like rename/delete
 * etc. are called that update nodes &amp; file lists.
 * <li>Needs more testing in general.
 * 
 * TODO Functionality
 * <li>Implement Read/Write/Execute checkboxes ----> removed (by Nenad Pantelic)
 * <li>Implement Copy ---> DONE + move (by Nenad Pantelic)
 * <li>Extra prompt for directory delete (camickr suggestion)
 * <li>Add File/Directory fields to FileTableModel ---> done( by Nenad Pantelic)
 * <li>Double clicking a directory in the table, should update the tree
 * <li>Move progress bar?
 * <li>Add other file display modes (besides table) in CardLayout?
 * <li>Menus + other cruft?
 * <li>Implement history/back
 * <li>Allow multiple selection
 * <li>Add file search ----> DONE (by Nenad Pantelic)
 * </ul>
 * 
 * @author Andrew Thompson
 * @version 2011-06-01
 */

/*
 * Modifications copyright (C) 2018
 * 
 * @author Nenad Pantelic
 * 
 * @version 2018-09-11
 * 
 * 
 * Original code designed by Valentyn Kolesnikov and redesigned by Andrew
 * Thompson is modified and redesigned so it uses now FileSystem service
 * implemented by myself. FileManager for business logic operations uses
 * FSTreeService that is actually just service class that operates and use
 * FSTree instance - combination of model (POJO class) and service class that
 * represents ADT implementation of File System Tree. More about structure of
 * the software you can find in header description of Runner.java
 * 
 */

public class FileManagerView {

    /** Title of the application */
    public static final String APP_TITLE = "File System simulator";
    /** currently selected File. */
    private File _currentFile;

    private FSTreeService _treeService;

    private boolean cellSizesSet = false;

    private JButton copyFile;
    private JPanel copyFilePanel;

    private JLabel date;
    private JButton deleteFile;
    /** Used to open/edit/print files. */
    private Desktop desktop;
    private JCheckBox executable;
    /* File details. */
    private JLabel fileName;
    /** Provides nice icons and names for files. */
    private FileSystemView fileSystemView;

    /** Table model for File[]. */
    private FileTableModel fileTableModel;
    /** Main GUI container */
    private JPanel gui;
    private JRadioButton isDirectory;
    private JRadioButton isFile;
    private JRadioButton isSystem;
    private JRadioButton isUser;
    private ListSelectionListener listSelectionListener;
    private JTextField name;
    private JRadioButton newCopyType;
    private JButton newFile;
    /* GUI options/containers for new File/Directory creation. Created lazily. */
    private JPanel newFilePanel;
    private JTextField newLocation;
    private JRadioButton newTypeFile;
    private JRadioButton newUserType;
    /* File controls. */
    private JButton openFile;
    private JTextField path;
    private JButton printFile;

    private JProgressBar progressBar;
    private JCheckBox readable;

    private int rowIconPadding = 6;
    private JButton searchFile;
    private JLabel size;

    /** Directory listing */
    private JTable table;
    /** File-system tree. Built Lazily */
    private JTree tree;
    private DefaultTreeModel treeModel;
    private JCheckBox writable;

    public FileManagerView(FSTreeService treeService) {
	_treeService = treeService;
    }

    /*
     * Copy and move operation - more about it in FSTreeService.
     */
    public void copyFile() throws IOException {

	if (getCurrentFile() == null || getTreeService().getCurrentNode() == null) {
	    showMessage("No location selected for new file.", "Select Location", 0);
	    return;
	}
	if (copyFilePanel == null) {
	    copyFilePanel = new JPanel(new BorderLayout(3, 3));

	    JPanel southRadio = new JPanel(new GridLayout(1, 0, 2, 2));
	    newCopyType = new JRadioButton("Copy", true);
	    JRadioButton newTypeMove = new JRadioButton("Move");
	    ButtonGroup bg = new ButtonGroup();
	    bg.add(newCopyType);
	    bg.add(newTypeMove);
	    southRadio.add(newCopyType);
	    southRadio.add(newTypeMove);

	    newLocation = new JTextField(15);

	    copyFilePanel.add(new JLabel("New Location"), BorderLayout.WEST);
	    copyFilePanel.add(newLocation);
	    copyFilePanel.add(southRadio, BorderLayout.SOUTH);
	}
	String globalMessage = "The file "
		+ " could not be copied - maybe you tried to copy system file. Check path you entered as location.";
	int result = JOptionPane.showConfirmDialog(gui, copyFilePanel, "Copy File", JOptionPane.OK_CANCEL_OPTION);
	/* true - copy op, false - move op */
	boolean copiedOrMoved = newCopyType.isSelected();
	FSNode copyParentNode = null;
	if (result == JOptionPane.OK_OPTION) {
	    /* backend logics */
	    try {
		boolean moved = false;

		FSNode newParentNode = getTreeService().searchByPath(getTreeService().getTreeRoot(),
			newLocation.getText());
		if (newCopyType.isSelected()) {
		    try {
			moved = getTreeService().copyFileNode(newParentNode, getTreeService().getCurrentNode());
		    } catch (Exception ex) {
			moved = false;

		    }
		} else {
		    try {
			copyParentNode = getTreeService().getCurrentNodeParent();
			moved = getTreeService().moveFileNode(getTreeService().getCurrentNodeParent(), newParentNode,
				getTreeService().getCurrentNode());

		    } catch (Exception ex) {
			moved = false;

		    }
		}
		/* Interface logics */
		if (moved) {
		    String nodeName = getTreeService().getCurrentNodeName();
		    File file = getTreeService().getSearchedFile(getTreeService().getTreeRoot(),
			    newParentNode.getNodePath() + "/" + nodeName);
		    TreePath parentPath = null;
		    TreePath newParentPath = findTreePath(getTreeService().getFileFromNode(newParentNode));

		    if (copyParentNode == null)
			parentPath = findTreePath(
				getTreeService().getFileFromNode(getTreeService().getCurrentNodeParent()));
		    else
			parentPath = findTreePath(getTreeService().getFileFromNode(copyParentNode));

		    try {
			DefaultMutableTreeNode newFileParentNode = (DefaultMutableTreeNode) newParentPath
				.getLastPathComponent();

			if (file.isDirectory()) {
			    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(file);
			    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath
				    .getLastPathComponent();
			    /* add node in new parrent directory */
			    treeModel.insertNodeInto(newNode, newFileParentNode, newFileParentNode.getChildCount());
			    /* delete from the old root directory - move operation */
			    if (!copiedOrMoved) {
				int index = getChildNode(parentNode, getCurrentFile());
				if (index != -1)
				    treeModel.removeNodeFromParent((MutableTreeNode) parentNode.getChildAt(index));
			    }
			}

			showChildren(newFileParentNode);
		    } catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
		    }
		} else {
		    showMessage(globalMessage, "Action Failed", 0);
		}
	    } catch (NullPointerException ex) {
		ex.printStackTrace();
		showMessage("The file is coruptive or cannot be recognized", "Action Failed", 0);
	    } catch (Throwable t) {
		showThrowable(t);
	    }
	}
	gui.repaint();
    }

    private void deleteFile() {
	if (getCurrentFile() == null || getTreeService().getCurrentNode() == null) {
	    showMessage("No file selected for deletion.", "Select File", 0);
	    return;
	}

	int result = JOptionPane.showConfirmDialog(gui, "Are you sure you want to delete this file?", "Delete File",
		JOptionPane.ERROR_MESSAGE);
	if (result == JOptionPane.OK_OPTION) {

	    try {

		TreePath parentPath = findTreePath(getTreeService().getParentFile(getTreeService().getCurrentNode()));
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();

		boolean directory = getCurrentFile().isDirectory();

		boolean deleted = getTreeService().removeFile(getTreeService().getCurrentNode());
		if (deleted) {
		    /* removes TreeNode from TreeModel */
		    if (directory) {
			int index = getChildNode(parentNode, getCurrentFile());
			if (index != -1)
			    treeModel.removeNodeFromParent((MutableTreeNode) parentNode.getChildAt(index));
		    }

		    showChildren(parentNode);
		} else {
		    String msg = "The file '" + getCurrentFile() + "' could not be deleted.";
		    showMessage(msg, "Action Failed", 0);
		}
	    } catch (InvalidPrivilegeException ex) {
		ex.printStackTrace();
		showMessage(ex.getMessage(), "Action Failed", 0);
	    } catch (Exception ex) {
		ex.printStackTrace();
		showMessage(ex.getMessage(), "Action Failed", 0);
	    }

	    catch (Throwable t) {
		showThrowable(t);
	    }
	}
	gui.repaint();
    }

    /* find three path of the file - returns path in JTree */
    private TreePath findTreePath(File find) {
	for (int ii = 0; ii < tree.getRowCount(); ii++) {
	    TreePath treePath = tree.getPathForRow(ii);
	    Object object = treePath.getLastPathComponent();
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
	    File nodeFile = (File) node.getUserObject();
	    if (nodeFile == find) {
		return treePath;
	    }
	}

	return null;
    }

    /*
     * Utility method - returns index of parent child node that contains file as
     * user object. If not found -1 is result
     */

    private int getChildNode(TreeNode parent, File file) {
	TreeNode element = null;
	Enumeration iter = parent.children();
	while (iter.hasMoreElements()) {
	    element = (TreeNode) iter.nextElement();
	    if (element.toString().equals(file.getAbsolutePath())) {
		return parent.getIndex(element);
	    }

	}
	return -1;
    }

    /* File delete - more about it in FSTreeService */

    public File getCurrentFile() {
	return _currentFile;
    }

    public Container getGui() {
	if (gui == null) {
	    gui = new JPanel(new BorderLayout(3, 3));
	    gui.setBorder(new EmptyBorder(5, 5, 5, 5));

	    fileSystemView = FileSystemView.getFileSystemView();
	    desktop = Desktop.getDesktop();

	    JPanel detailView = new JPanel(new BorderLayout(3, 3));

	    table = new JTable();
	    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    table.setAutoCreateRowSorter(true);
	    table.setShowVerticalLines(false);

	    listSelectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent lse) {
		    int row = table.getSelectionModel().getLeadSelectionIndex();
		    File file = ((FileTableModel) table.getModel()).getFile(row);
		    setFileDetails(file,
			    getTreeService().treeSearch(getTreeService().getTreeRoot(), file.getAbsolutePath()));
		}
	    };
	    table.getSelectionModel().addListSelectionListener(listSelectionListener);
	    JScrollPane tableScroll = new JScrollPane(table);
	    Dimension d = tableScroll.getPreferredSize();
	    tableScroll.setPreferredSize(new Dimension((int) d.getWidth(), (int) d.getHeight() / 2));
	    detailView.add(tableScroll, BorderLayout.CENTER);

	    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
	    treeModel = new DefaultTreeModel(root);

	    TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent tse) {
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();
		    String absPath = tse.getPath().getLastPathComponent().toString();
		    showChildren(node);
		    setFileDetails((File) node.getUserObject(),
			    getTreeService().treeSearch(getTreeService().getTreeRoot(), absPath));
		}
	    };

	    // show the file system roots.
	    File[] roots = { getTreeService().getRootFile() };
	    for (File fileSystemRoot : roots) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
		root.add(node);
		File[] files = fileSystemView.getFiles(fileSystemRoot, true);
		for (File file : files) {
		    if (file.isDirectory()) {
			node.add(new DefaultMutableTreeNode(file));
		    }
		}

	    }

	    tree = new JTree(treeModel);
	    tree.setRootVisible(false);
	    tree.addTreeSelectionListener(treeSelectionListener);
	    tree.setCellRenderer(new FileTreeCellRenderer());
	    tree.expandRow(0);
	    JScrollPane treeScroll = new JScrollPane(tree);

	    tree.setVisibleRowCount(15);

	    Dimension preferredSize = treeScroll.getPreferredSize();
	    Dimension widePreferred = new Dimension(200, (int) preferredSize.getHeight());
	    treeScroll.setPreferredSize(widePreferred);

	    JPanel fileMainDetails = new JPanel(new BorderLayout(4, 2));
	    fileMainDetails.setBorder(new EmptyBorder(0, 6, 0, 6));

	    JPanel fileDetailsLabels = new JPanel(new GridLayout(0, 1, 2, 2));
	    fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);

	    JPanel fileDetailsValues = new JPanel(new GridLayout(0, 1, 2, 2));
	    fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);

	    fileDetailsLabels.add(new JLabel("File", JLabel.TRAILING));
	    fileName = new JLabel();
	    fileDetailsValues.add(fileName);
	    fileDetailsLabels.add(new JLabel("Path/name", JLabel.TRAILING));
	    path = new JTextField(5);
	    path.setEditable(false);
	    fileDetailsValues.add(path);
	    fileDetailsLabels.add(new JLabel("Last Modified", JLabel.TRAILING));
	    date = new JLabel();
	    fileDetailsValues.add(date);
	    fileDetailsLabels.add(new JLabel("File size", JLabel.TRAILING));
	    size = new JLabel();
	    fileDetailsValues.add(size);
	    fileDetailsLabels.add(new JLabel("Type", JLabel.TRAILING));
	    JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
	    isDirectory = new JRadioButton("Directory");
	    isDirectory.setEnabled(false);
	    flags.add(isDirectory);

	    isFile = new JRadioButton("File");
	    isFile.setEnabled(false);
	    flags.add(isFile);

	    fileDetailsValues.add(flags);

	    int count = fileDetailsLabels.getComponentCount();
	    for (int ii = 0; ii < count; ii++) {
		fileDetailsLabels.getComponent(ii).setEnabled(false);
	    }

	    JToolBar toolBar = new JToolBar();
	    toolBar.setFloatable(false);

	    openFile = new JButton("Open");
	    openFile.setMnemonic('o');

	    openFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			desktop.open(getCurrentFile());
		    } catch (Throwable t) {
			showThrowable(t);
		    }
		    gui.repaint();
		}
	    });
	    toolBar.add(openFile);

	    searchFile = new JButton("Search");
	    searchFile.setMnemonic('s');
	    searchFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			searchFile();
		    } catch (Throwable t) {
			showThrowable(t);
		    }

		}
	    });
	    toolBar.add(searchFile);

	    printFile = new JButton("Print");
	    printFile.setMnemonic('p');
	    printFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			desktop.print(getCurrentFile());
		    } catch (Throwable t) {
			showThrowable(t);
		    }
		}
	    });
	    toolBar.add(printFile);

	    openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
	    searchFile.setEnabled(true);
	    printFile.setEnabled(desktop.isSupported(Desktop.Action.PRINT));

	    toolBar.addSeparator();

	    newFile = new JButton("New");
	    newFile.setMnemonic('n');
	    newFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    newFile();
		}
	    });
	    toolBar.add(newFile);

	    copyFile = new JButton("Copy");
	    copyFile.setMnemonic('c');
	    copyFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			copyFile();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    });
	    toolBar.add(copyFile);

	    JButton renameFile = new JButton("Rename");
	    renameFile.setMnemonic('r');
	    renameFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    renameFile();
		}
	    });
	    toolBar.add(renameFile);

	    deleteFile = new JButton("Delete");
	    deleteFile.setMnemonic('d');
	    deleteFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    deleteFile();
		}
	    });
	    toolBar.add(deleteFile);

	    toolBar.addSeparator();

	    readable = new JCheckBox("Read  ");
	    readable.setMnemonic('a');
	    readable.setEnabled(false);
	    toolBar.add(readable);

	    writable = new JCheckBox("Write  ");
	    writable.setMnemonic('w');
	    writable.setEnabled(false);

	    toolBar.add(writable);

	    executable = new JCheckBox("Execute");
	    executable.setMnemonic('x');
	    executable.setEnabled(false);

	    toolBar.add(executable);

	    isUser = new JRadioButton("User ");
	    isUser.setMnemonic('u');
	    isUser.setEnabled(false);

	    toolBar.add(isUser);

	    isSystem = new JRadioButton("System ");
	    isSystem.setMnemonic('y');
	    isSystem.setEnabled(false);

	    toolBar.add(isSystem);

	    JPanel fileView = new JPanel(new BorderLayout(3, 3));

	    fileView.add(toolBar, BorderLayout.NORTH);
	    fileView.add(fileMainDetails, BorderLayout.CENTER);

	    detailView.add(fileView, BorderLayout.SOUTH);

	    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, detailView);
	    gui.add(splitPane, BorderLayout.CENTER);

	    JPanel simpleOutput = new JPanel(new BorderLayout(3, 3));
	    progressBar = new JProgressBar();
	    simpleOutput.add(progressBar, BorderLayout.EAST);
	    progressBar.setVisible(false);

	    gui.add(simpleOutput, BorderLayout.SOUTH);

	}
	return gui;
    }

    /*
     * Utility helper method - returns all children of some TreeNode (Swing node) as
     * List - used for operations as copy or rename
     */
    private List<TreeNode> getTreeChildren(TreeNode node) {
	Enumeration children = node.children();
	List<TreeNode> childrenList = new ArrayList<TreeNode>();
	while (children.hasMoreElements()) {
	    childrenList.add((TreeNode) children.nextElement());

	}
	return childrenList;
    }

    private FSTreeService getTreeService() {
	return _treeService;
    }

    /*
     * Creation of new node (file) Handle input data - name, file type, user type
     * and calls FSTreeService creation method. Also add new TreeNode to JTree
     * hierarchy
     */
    private void newFile() {
	if (getCurrentFile() == null || getTreeService().getCurrentNode() == null) {
	    showMessage("No location selected for new file.", "Select Location", 0);
	    return;
	}

	if (newFilePanel == null) {
	    newFilePanel = new JPanel(new BorderLayout(3, 3));

	    JPanel southRadio = new JPanel(new GridLayout(2, 0, 2, 2));
	    newTypeFile = new JRadioButton("File", true);
	    JRadioButton newTypeDirectory = new JRadioButton("Directory");
	    ButtonGroup bg = new ButtonGroup();
	    bg.add(newTypeFile);
	    bg.add(newTypeDirectory);
	    southRadio.add(newTypeFile);
	    southRadio.add(newTypeDirectory);
	    ButtonGroup bg2 = new ButtonGroup();
	    JRadioButton newSystemType = new JRadioButton("System");
	    newUserType = new JRadioButton("User", true);

	    bg2.add(newUserType);
	    bg2.add(newSystemType);
	    southRadio.add(newUserType);
	    southRadio.add(newSystemType);

	    name = new JTextField(15);

	    newFilePanel.add(new JLabel("Name"), BorderLayout.WEST);
	    newFilePanel.add(name);
	    newFilePanel.add(southRadio, BorderLayout.SOUTH);
	}

	int result = JOptionPane.showConfirmDialog(gui, newFilePanel, "Create File", JOptionPane.OK_CANCEL_OPTION);
	if (result == JOptionPane.OK_OPTION) {
	    try {
		boolean created = false;
		File parentFile = getTreeService().getCurrentFile();
		FSNode parentFSNode = getTreeService().getCurrentNode();
		if (!(parentFSNode.getFileType().equals(FileType.DIRECTORY))) {

		    try {
			parentFSNode = parentFSNode.getParent();
			parentFile = parentFSNode.getFileFromNode();
		    } catch (Exception ex) {
			if (getTreeService().countRootChildren() == 0) {
			    parentFSNode = getTreeService().getTreeRoot();
			    parentFile = parentFSNode.getFileFromNode();
			}

		    }
		}

		FileType ft = null;
		UserType ut = null;
		if (newTypeFile.isSelected()) {
		    ft = FileType.FILE;
		} else {
		    ft = FileType.DIRECTORY;
		}

		if (newUserType.isSelected()) {
		    ut = UserType.USER;
		} else {
		    ut = UserType.SYSTEM;
		}
		FSNode createdNode = getTreeService().createFileNode(parentFSNode, name.getText(), ut, "", ft, null,
			null);
		if (createdNode != null)
		    created = true;
		File file = getTreeService().getFileFromNode(createdNode);
		if (created) {
		    /* Interace logics - add new TreeNode to TreeModel */
		    TreePath parentPath = findTreePath(parentFile);
		    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();

		    if (file.isDirectory()) {
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(file);
			treeModel.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
		    }

		    showChildren(parentNode);
		} else {
		    String msg = "The file '" + file + "' could not be created.";
		    showMessage(msg, "Action Failed", 0);
		}
	    } catch (NullPointerException ex) {
		ex.printStackTrace();

	    } catch (Throwable t) {
		showMessage("An error occured." + t.getMessage(), "Action Failed", 0);
	    }
	}
	gui.repaint();
    }

    /*
     * File rename - explained more in FSTreeService and FSTree After file rename on
     * disk, new TreeNode must be added to JTree - the old one is deleted, and new
     * one added
     */
    private void renameFile() {
	if (getCurrentFile() == null || getTreeService().getCurrentNode() == null) {
	    showMessage("No file selected to rename.", "Select File", 0);
	    return;
	}

	String renameTo = JOptionPane.showInputDialog(gui, "New Name");
	if (renameTo != null) {
	    try {

		boolean directory = getCurrentFile().isDirectory();
		TreePath parentPath = findTreePath(getTreeService().getParentFile(getTreeService().getCurrentNode()));

		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
		FSNode renamedNode = getTreeService().renameFileNode(getTreeService().getCurrentNodeParent(),
			getTreeService().getCurrentNode(), renameTo);
		if (renamedNode != null) {
		    if (directory) {
			/*
			 * Interface logics - remove old node (renamed one), and after creation of new
			 * file with new name, add it to TreeModel instance If old node had cildren -
			 * directory node, add all nodes to new, renamed node
			 */
			int index = getChildNode(parentNode, getCurrentFile());
			List<TreeNode> children = new ArrayList<TreeNode>();

			int newIndex = getChildNode(parentNode, getTreeService().getCurrentFile());

			MutableTreeNode copyNode = (MutableTreeNode) parentNode.getChildAt(newIndex);
			copyNode.setUserObject(renamedNode.getFileFromNode());
			if (index != -1) {
			    children = getTreeChildren((MutableTreeNode) parentNode.getChildAt(index));
			    treeModel.removeNodeFromParent((MutableTreeNode) parentNode.getChildAt(index));

			}

			treeModel.insertNodeInto(copyNode, parentNode, parentNode.getChildCount());
			for (TreeNode child : children) {
			    try {
				treeModel.insertNodeInto((MutableTreeNode) child, copyNode, copyNode.getChildCount());
			    } catch (Exception ex) {

			    }
			}

		    }

		    showChildren(parentNode);
		} else {
		    String msg = "The file '" + getCurrentFile() + "' could not be renamed.";
		    showMessage(msg, "Action Failed", 0);
		}
	    } catch (Exception ex) {
		showMessage(ex.getMessage(), "Action Failed", 0);
	    }
	}
	gui.repaint();
    }

    private void searchFile() {

	if (getCurrentFile() == null) {
	    showMessage("No root folder selected for search operation.", "Select File", 0);
	    return;
	}
	String searchWord = JOptionPane.showInputDialog(gui, "Enter the keyword");
	String message = null;
	if (searchWord != null) {

	    List<FSNode> nodes = new ArrayList<FSNode>();
	    message = getTreeService().search(getTreeService().getCurrentNode(), searchWord, nodes);
	    showMessage(message, "Search result", 1);

	}

    }

    /*
     * File/folder search - uses FSTreeService searchByWord that returns results as
     * String = all absolute paths that match the keyword. Current node is starting
     * point of the search - it is local root for the search operation and on its
     * subtree search is performed
     * 
     */

    private void setColumnWidth(int column, int width) {
	TableColumn tableColumn = table.getColumnModel().getColumn(column);
	if (width < 0) {
	    JLabel label = new JLabel((String) tableColumn.getHeaderValue());
	    Dimension preferred = label.getPreferredSize();
	    width = (int) preferred.getWidth() + 14;
	}
	tableColumn.setPreferredWidth(width);
	tableColumn.setMaxWidth(width);
	tableColumn.setMinWidth(width);
    }

    private void setCurrentFile(File currentFile) {
	this._currentFile = currentFile;
    }

    /** Update the File details view with the details of this File. */
    private void setFileDetails(File file, FSNode node) {
	setCurrentFile(file);
	getTreeService().setCurrentNode(node);
	Icon icon = fileSystemView.getSystemIcon(file);
	fileName.setIcon(icon);
	fileName.setText(fileSystemView.getSystemDisplayName(file));
	path.setText(file.getPath());
	date.setText(new Date(file.lastModified()).toString());
	size.setText(file.length() + " bytes");
	readable.setSelected(file.canRead());
	writable.setSelected(file.canWrite());
	executable.setSelected(file.canExecute());
	isDirectory.setSelected(file.isDirectory());

	isFile.setSelected(file.isFile());
	try {
	    isSystem.setSelected(getTreeService().getCurrentUType().equals(UserType.SYSTEM));
	    isUser.setSelected(getTreeService().getCurrentUType().equals(UserType.USER));
	} catch (NullPointerException ex) {

	}

	JFrame f = (JFrame) gui.getTopLevelAncestor();
	if (f != null) {
	    f.setTitle(APP_TITLE + " :: " + fileSystemView.getSystemDisplayName(file));
	}

	gui.repaint();
    }

    /** Update the table on the EDT */
    private void setTableData(final File[] files) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		if (fileTableModel == null) {
		    fileTableModel = new FileTableModel(getTreeService().getTree());
		    table.setModel(fileTableModel);
		}
		table.getSelectionModel().removeListSelectionListener(listSelectionListener);
		fileTableModel.setFiles(files);
		table.getSelectionModel().addListSelectionListener(listSelectionListener);
		if (!cellSizesSet) {
		    if (files.length > 0) {
			Icon icon = fileSystemView.getSystemIcon(files[0]);

			table.setRowHeight(icon.getIconHeight() + rowIconPadding);
		    }
		    setColumnWidth(0, -1);
		    setColumnWidth(3, 60);
		    table.getColumnModel().getColumn(3).setMaxWidth(120);
		    setColumnWidth(4, -1);
		    setColumnWidth(5, -1);
		    setColumnWidth(6, -1);
		    setColumnWidth(7, -1);
		    setColumnWidth(8, -1);
		    setColumnWidth(9, -1);

		    cellSizesSet = true;
		}
	    }
	});
    }

    /**
     * Add the files that are contained within the directory of this node. Thanks to
     * Hovercraft Full Of Eels.
     */
    private void showChildren(final DefaultMutableTreeNode node) {
	tree.setEnabled(false);
	progressBar.setVisible(true);
	progressBar.setIndeterminate(true);

	SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
	    @Override
	    public Void doInBackground() {
		File file = (File) node.getUserObject();
		if (file.isDirectory()) {
		    File[] files = fileSystemView.getFiles(file, true);
		    if (node.isLeaf()) {
			for (File child : files) {
			    if (child.isDirectory()) {
				publish(child);
			    }
			}
		    }
		    setTableData(files);
		}
		return null;
	    }

	    @Override
	    protected void done() {
		progressBar.setIndeterminate(false);
		progressBar.setVisible(false);
		tree.setEnabled(true);
	    }

	    @Override
	    protected void process(List<File> chunks) {
		for (File child : chunks) {
		    node.add(new DefaultMutableTreeNode(child));
		}
	    }
	};
	worker.execute();
    }

    private void showMessage(String errorMessage, String errorTitle, int messageType) {
	JOptionPane.showMessageDialog(gui, errorMessage, errorTitle, messageType);
    }

    public void showRootFile() {
	tree.setSelectionInterval(0, 0);
    }

    private void showThrowable(Throwable t) {
	t.printStackTrace();
	JOptionPane.showMessageDialog(gui, t.toString(), t.getMessage(), JOptionPane.ERROR_MESSAGE);
	gui.repaint();
    }

}

/**
 * A TableModel to hold File[]. Header line fields - data about files
 */
class FileTableModel extends AbstractTableModel {

    private FSTree _tree;
    private String[] columns = { "Icon", "File", "Path/name", "Size", "Last Modified", "R", "W", "X", "D", "F" };
    private File[] files;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    FileTableModel(File[] files, FSTree tree) {
	this.files = files;
	_tree = tree;
    }

    FileTableModel(FSTree tree) {
	this.files = new File[0];
	_tree = tree;
    }

    public Class<?> getColumnClass(int column) {
	switch (column) {
	case 0:
	    return ImageIcon.class;
	case 3:
	    return Long.class;
	case 4:
	    return Date.class;
	case 5:
	case 6:
	case 7:
	case 8:
	case 9:
	case 10:
	case 11:
	    return Boolean.class;
	}
	return String.class;
    }

    public int getColumnCount() {
	return columns.length;
    }

    public String getColumnName(int column) {
	return columns[column];
    }

    public File getFile(int row) {
	return files[row];
    }

    public int getRowCount() {
	return files.length;
    }

    public FSTree getTree() {
	return _tree;
    }

    public Object getValueAt(int row, int column) {
	File file = files[row];
	switch (column) {
	case 0:
	    return fileSystemView.getSystemIcon(file);
	case 1:
	    return fileSystemView.getSystemDisplayName(file);
	case 2:
	    return file.getPath();
	case 3:
	    return file.length();
	case 4:
	    return file.lastModified();
	case 5:
	    return file.canRead();
	case 6:
	    return file.canWrite();
	case 7:
	    return file.canExecute();
	case 8:
	    return file.isDirectory();
	case 9:
	    return file.isFile();

	default:
	    System.err.println("Logic Error");
	}
	return "";
    }

    public void setFiles(File[] files) {
	this.files = files;
	fireTableDataChanged();
    }
}

/** A TreeCellRenderer for a File. */
class FileTreeCellRenderer extends DefaultTreeCellRenderer {

    private FileSystemView fileSystemView;

    private JLabel label;

    FileTreeCellRenderer() {
	label = new JLabel();
	label.setOpaque(true);
	fileSystemView = FileSystemView.getFileSystemView();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
	    boolean leaf, int row, boolean hasFocus) {

	DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	File file = (File) node.getUserObject();
	label.setIcon(fileSystemView.getSystemIcon(file));
	label.setText(fileSystemView.getSystemDisplayName(file));
	label.setToolTipText(file.getPath());

	if (selected) {
	    label.setBackground(backgroundSelectionColor);
	    label.setForeground(textSelectionColor);
	} else {
	    label.setBackground(backgroundNonSelectionColor);
	    label.setForeground(textNonSelectionColor);
	}

	return label;
    }
}