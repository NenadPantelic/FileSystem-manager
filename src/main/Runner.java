/*
 * 
 * Design and implementation of virtual File System interface. This application actually represents API that internally uses
 * FileSystem ADT objects (hierarchy tree, file system nodes, file system objects ...). It can be used as FileManager -
 * virtual objects actually control physical File System objects and enable operations on them - CRUD operations, 
 * as well as primitive search and copy/move  operations.
 * Main View class - FileManager.java is central GUI class of this project and more about it you can read in its header 
 * description. Only FileManager.java is modified file - apache 2.0 licence. Other files, complete structure and logics of this project 
 * are my product. In this file I added backend logics through FSTreeService and I also redesigned or implmented GUI logics - operation on
 * JTree and TreeNode(s).
 * 
 * Project was designed as some type of MVC or MVVM architectural pattern. View clasess interact with user, and use service classes
 * as service and data providers - most part of FileSystem operations that manipulate the physical system on disk are stored here.
 * 
 * FSCreatorService - creates new FileSystem instance ---> FSTree instance
 * FSTreeService - provides FSTree functional operations - create new node (FileSystem object - file/folder), deletion, copying/moving
 * search, rename, size details, file details...
 * 
 * Independent class that has only static methods is Interceptor class - encapsulate exception handling methods.
 * Builder, Factory and Singleton patterns are used in this application.
 * 
 * BUGS AND POSSIBLE IMPROVEMENTS:
 * 
 * 1) Abstract FileSystem instance is not secured properly - you can easily access or even delete any file/folder ---> you can do this
 * even with real physical file system if you have permission for those activities.
 * 2) Partitioning table could be added so user can make even his personal virtual disk partition
 * 3) GUI can act sometimes unpredictably - after deletion or copy/move operation, object on physical file system is deleted
 * , but GUI Swing JTree could still contain this/those file/files
 * 4) System is not tested enough - there are dozens of possible situations that were not tested
 * 5) Possible exception hanlding problem in FileManager class - originally exception hanlding is part of this class, so I didn't refactor
 * it to Interceptor class
 * 6) FileManager class is a little bit messy and confusing - lack of setters/getters (still all fields are private), huge methods
 * - so that makes FileManager class a little bit spaghetti coded. Modularity and abstraction should be improved in that class.
 * 7) Externalization of strings would provide higher abstraction
 * 8) All business logic from FileManager should be moved to FSTreeService class. Also FileTableModel class and FileTreeCellRenderer class should be moved
 * to in its own java files.
 * 
 * @author Nenad Pantelic
 * @version 2018-09-11
 * 
 */

package main;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import exceptions.FilenameException;
import exceptions.InvalidFSOperationException;
import exceptions.NotEnoughSpaceException;
import exceptions.RootException;
import fstree.FSTree;
import services.FSCreatorService;
import services.FSTreeService;
import views.FSInitView;
import views.FileManagerView;

public class Runner {

    public static final String APP_TITLE = "File System simulator";
    private static FSCreatorService creatorService = new FSCreatorService();
    private static boolean fsCreation = true;

    public static void main(String[] args) {

	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		try {

		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception weTried) {
		}
		FSTree tree = null;
		JFrame f = new JFrame(APP_TITLE);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FSInitView init = new FSInitView();
		f.setContentPane(init.getGUI());
		int dialogRootFlag = init.makeRootDialog();
		if (dialogRootFlag == 0) {
		    int dialogSizeFlag = init.makeSizeDialog();
		    if (dialogSizeFlag == 0) {

			try {
			    tree = creatorService.createFS(init.getRootDir().getText(), init.getSize());
			} catch (InvalidFSOperationException | IOException | NotEnoughSpaceException | RootException
				| FilenameException e) {
			    e.printStackTrace();
			    String msg = "FileSystem cannot be created! " + e.getMessage();
			    fsCreation = false;
			    JOptionPane.showMessageDialog(init.getGUI(), msg, "Action Failed!",
				    JOptionPane.ERROR_MESSAGE);
			}
			if (fsCreation) {
			    FSTreeService treeService = new FSTreeService(tree);
			    FileManagerView fileManager = new FileManagerView(treeService);
			    f.setContentPane(fileManager.getGui());

			    f.pack();
			    f.setLocationByPlatform(true);
			    f.setMinimumSize(f.getSize());
			    f.setVisible(true);

			    fileManager.showRootFile();
			}
		    } else if (dialogSizeFlag == 1) {
			String msg = "You entered wrong file system size!";
			JOptionPane.showMessageDialog(init.getGUI(), msg, "Action Failed!", JOptionPane.ERROR_MESSAGE);
		    }
		} else if (dialogRootFlag == 1) {
		    String msg = "You entered wrong root directory absolute path!";
		    JOptionPane.showMessageDialog(init.getGUI(), msg, "Action Failed!", JOptionPane.ERROR_MESSAGE);
		}
	    }
	});
    }
}
