package kse.utilclass.dialog;

/*
*  File: GUIService.java
* 
*  Project JUtilClasses
*  @author Wolfgang Keller
*  Created 
* 
*  Copyright (c) 2023 by Wolfgang Keller, Munich, Germany
* 
This program is not public domain software but copyright protected to the 
author(s) stated above. However, you can use, redistribute and/or modify it 
under the terms of the GNU Library or Lesser General Public License as 
published by the Free Software Foundation, version 3.0 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the License along with this program; if not,
write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, 
Boston, MA 02111-1307, USA, or go to http://www.gnu.org/copyleft/gpl.html.
*/

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import kse.utilclass.dialog.MessageDialog.MessageType;

/** A GUI message service with dialogs depending on class {@code GSDialog}.
 *  
 */
public class GUIService {
	/** Window used as the applications main frame. This is used
	 * for display of dialog of this service where no other parent
	 * component is given.
	 */
	private static Window mainFrame;

	public GUIService () {
	}

	public static void setMainFrame (Window window) {
		mainFrame = window;
	}
	
	public static Window getMainFrame () {return mainFrame;}
	
	/** Sets whether INFO and QUESTION type messages receive a standard 
	 * background coloured panel. By default the panels are coloured. If this
	 * option is turned off, the panels paint non-opaque (left to UI-Manager).
	 * 
	 * @param b boolean true = panel coloured, false = panel not coloured
	 */
	public static void setInfosColored (boolean b) {
		MessageDialog.setInfosColored(b);
	}

	/** Asks the user (dialog) to confirm a given question with "Yes" or "No".
	 * 
	 * @param owner the parent component for the dialog; if <b>null</b> the current
	 *        mainframe window is used
	 * @param text the question as text or token
	 * @return boolean user decision (false in case of thread interruption)
	 */ 
	public static boolean userConfirm (Component owner, String text) {
	   return userConfirm(owner, null, text);
	}

	/** Asks the user (dialog) to confirm a given question with "Yes" or "No".
	 * 
	 * @param owner the parent component for the dialog; if <b>null</b> the current
	 *        mainframe window is used
	 * @param title String the dialog title as text or token, may be null
	 * @param text String the question as text or token
	 * @return boolean user decision (false in case of thread interruption)
	 */ 
	public static boolean userConfirm (Component owner, String title, String text) {
	   boolean result = MessageDialog.showConfirmMessage( owner, title, text, 
	         ButtonBarModus.YES_NO );
	   return result;
	}

	/** Asks the user (dialog) to confirm a given question with "Yes" or "No". 
	 * 
	 * @param text the question as text or token
	 * @return boolean user decision (false in case of thread interruption)
	 */
	public static boolean userConfirm (String text)	{
	   return userConfirm( null, text );
	}

	/** Asks the user (dialog) to confirm a given question with "Yes", "No" or
	 *  "Cancel". The possible return values are JOptionPane.YES_OPTION,
	 *  JOptionPane.NO_OPTION, JOptionPane.CANCEL_OPTION. In case of broken
	 *  dialog thread, the cancel value is returned.  
	 * 
	 * @param owner the parent component for the dialog; if <b>null</b> the current
	 *        mainframe window is used
	 * @param text the question as text or token
	 * @return the user decision as DialogTerminationType
	 */
	public static DialogTerminationType userConfirmOption (Component owner, String text) {
	//   MessageDialog dlg;
	//   Window parent = getAncestorWindow( owner );
	
	//   dlg = new MessageDialog( parent, text, MessageType.question, 
	//         DialogButtonBar.YES_NO_CANCEL_BUTTON, true );
	//   dlg.setTitle( ResourceLoader.codeOrRealDisplay( "dlg.confirm" ) );
	//   dlg.pack();
	//   dlg.setVisible( true );
	
	   DialogTerminationType answer = MessageDialog.showMessage(owner, null, text, 
			   MessageType.question, ButtonBarModus.YES_NO_BREAK);
		   
//	   // translate result
//	   // do we need to have JOptionPane constants as result?
//	   int result;
//	   switch (answer) {
//	   case OK_PRESSED:
//		   result = JOptionPane.YES_OPTION;
//		   break;
//	   case NO_PRESSED:
//	       result = JOptionPane.NO_OPTION;
//	       break;
//	   default:   result = JOptionPane.CANCEL_OPTION;
//	   }
//	   return result;
	   return answer;
	}

	/** Asks the user to confirm a given question with "Yes", "No" or
	 *  "Cancel". The dialog is centered within the application's mainframe.
	 *  The possible return values are JOptionPane.YES_OPTION,
	 *  JOptionPane.NO_OPTION, JOptionPane.CANCEL_OPTION. In case of broken
	 *  dialog thread, the cancel value is returned.  
	 * 
	 * @param text the question as text or token
	 * @return the user decision as DialogTerminationType
	 */
	public static DialogTerminationType userConfirmOption (String text)	{
	   return userConfirmOption( null, text );
	}

	/** Displays an error message dialog referring to the parameter exception.
	 * <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 * 
	 * @param owner Component owner of the message dialog; if null the default
	 *        frame is used
	 * @param title String dialog title; if <b>null</b> a standard
	 * @param text String message text; if <b>null</b> a standard 
	 * @param e {@code Throwable} if not <b>null</b> this exception is reported 
	 *        in the dialog 
	 */ 
	public static void failureMessage (Component owner, String title, String text, Throwable e) {
	   if (title == null) {
		   title = "Operation Failure";
	   }
	   if ( text == null ) {
	      text = "Something bad happened:";
	   }
	   
	   String hstr = "<html><b>".concat(text); 
	   hstr = getExceptionMessage(hstr, e);
	   MessageDialog.showInfoMessage(owner, title, hstr, MessageType.error);
	}

	/** Displays an error message dialog with the given message and a 
	 * default title.
	 * <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 * 
	 * @param owner Component owner of the message dialog; if null the default
	 *        frame is used
	 * @param text String message text; if <b>null</b> a standard 
	 */ 
	public static void failureMessage (Component owner, String text) {
		failureMessage(owner, null, text, null);
	}
	
	/** Displays an error message dialog referring to the parameter exception.
	 * <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 * 
	 * @param owner Component owner of the message dialog; if null the default
	 *        frame is used
	 * @param title String dialog title; if <b>null</b> a standard
	 * @param text String message text; if <b>null</b> a standard 
	 */ 
	public static void failureMessage (Component owner, String title, String text) {
		failureMessage(owner, title, text, null);
	}
	
	/** Displays an error message dialog with the given text and additionally
	 * referring to the parameter exception.
	 * <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 * 
	 * @param owner Component owner of the message dialog; if null the default
	 *        frame is used
	 * @param text String message text; if <b>null</b> a standard 
	 * @param e {@code Throwable} if not <b>null</b> this exception is reported
	 *        in the dialog 
	 */ 
	public static void failureMessage (Component owner, String text, Throwable e) {
		failureMessage(owner, null, text, e);
	}
	
	/** Displays an error message dialog with the given text and additionally
	 * referring to the parameter exception.
	 * <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 * 
	 * @param text String message text; if <b>null</b> a standard 
	 * @param e {@code Throwable} if not <b>null</b> this exception is reported
	 *        in the dialog 
	 */ 
	public static void failureMessage (String text, Throwable e) {
	   failureMessage(mainFrame, text, e);
	}

	/** Displays an error message dialog with the given text message and dialog
	 * title. <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 * 
	 * @param title String dialog title; if <b>null</b> a standard
	 * @param text String message text; if <b>null</b> a standard 
	 */ 
	public static void failureMessage (String title, String text) {
	   failureMessage(mainFrame, title, text, null);
	}

	/** Displays an error message dialog with the given text message and a
	 * default dialog title. <p>Waits until the message is confirmed or the 
	 * calling thread is interrupted. 
	 * 
	 * @param text String message text; if <b>null</b> a standard 
	 */ 
	public static void failureMessage (String text) {
	   failureMessage(mainFrame, null, text, null);
	}

	/** Displays an information message without parent component. It will get 
	 *  centred within the application's mainframe.
	 * <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 */
	public static void infoMessage (String title, String text) {
	   infoMessage( null, title, text );
	}

	/** Displays a warning message centred within the given parent component.
	 * <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 * 
	 * @param owner {@code Component} parent component; if null the mainframe
	 * 				window is used
	 * @param title String title of dialog; null for a standard title 
	 * @param text String message text
	 * @return DialogTerminationType 
	 */
	public static DialogTerminationType warningMessage (Component owner, String title, String text) {
	   return MessageDialog.showMessage(owner, title, text, MessageType.warning, ButtonBarModus.OK_BREAK);
	}

	/** Displays a warning message with the given title and text, centred within
	 * the default frame.
	 * <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 *
	 * @param title String title of dialog; null for a standard title 
	 * @param text String message text
	 * @return DialogTerminationType 
	 */
	public static DialogTerminationType warningMessage (String title, String text) {
	   return warningMessage( null, title, text );
	}

	public static DialogTerminationType warningMessage(Component owner, String text, Throwable e) {
		return warningMessage(owner, null, getExceptionMessage(text, e));
	}
	
	public static void warningMessage(String text, Throwable e) {
		warningMessage(null, text, e);
	}
	
	/** Displays an information message centred within the given parent component.
	 * <p>Waits until the message is confirmed or the calling thread is 
	 * interrupted. 
	 * 
	 * @param owner {@code Component} parent component; if null the mainframe
	 * 				window is used
	 * @param title String title of dialog; may be null in which case a 
	 * 				standard title is used 
	 * @param text String message text
	 */
	public static void infoMessage (Component owner, String title, String text) {
	   MessageDialog.showInfoMessage(owner, title, text, MessageType.info);
	}

	/** Returns a concatenation of parameter <tt>text</tt> and an excerpt
	 *  from the given <tt>Exception</tt> making it informative to the user.
	 *  If <tt>e</tt> is <b>null</b>, <tt>text</tt> is returned. 
	 *  (<tt>text</tt> should be Html encoded.)
	 *      
	 * @param text String message leading text, may be null 
	 * @param e Exception; may be null
	 * @return String Html encoded message or null if both arguments are null 
	 */
	private static String getExceptionMessage (String text, Throwable e) {
	   String msg = text;
	   
	   while (e != null) {
	      msg = e.getMessage(); 
	      msg = (text == null ? "" : text) + "<br>" + e.getClass().getName() + 
	    		(msg == null ? "" : "<br><font color=#8B0000>".concat(msg).concat("</font>"));
	      e = e.getCause();
	   }
	   return msg;
	}

	/** Ensures the given task is executed on the EDT. If the task has to be
	 * queued instead of immediately performed, this thread waits or continues
	 * depending on the 'wait' parameter.
	 * 
	 * @param task <code>Runnable</code>
	 * @param wait boolean if true 'invokeAndWait' is executed, otherwise 
	 *             'invokeLater'
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public static void performOnEDT (Runnable task, boolean wait) 
			throws InvocationTargetException, InterruptedException {
		
		if (SwingUtilities.isEventDispatchThread()) {
//			System.out.println("--- (GUIManager.performOnEDT) performing immediately (EDT)");
			task.run();
		} else {
			if (wait) {
//				System.out.println("--- (GUIManager.performOnEDT) queueing on EDT and waiting)");
				SwingUtilities.invokeAndWait(task);
			} else {
//				System.out.println("--- (GUIManager.performOnEDT) queueing on EDT for later)");
				SwingUtilities.invokeLater(task);
			}
		}
	}

	/** Ensures the given task is executed on the EDT at a later time. 
	 * 
	 * @param task <code>Runnable</code>
	 */
	public static void performOnEDT (Runnable task) {
		if (SwingUtilities.isEventDispatchThread()) {
//			System.out.println("--- (GUIManager.performOnEDT) performing immediately (EDT)");
			task.run();
		} else {
//			System.out.println("--- (GUIManager.performOnEDT) queueing on EDT for later)");
			SwingUtilities.invokeLater(task);
		}
	}

	/** Prints a warning on System.err if the current thread is not the EDT.
	 * 
	 * @param log String logging comment; may be null
	 */
	public static void assumingEDT (String log) {
		if (!SwingUtilities.isEventDispatchThread()) {
			String text = "*** WARNING! EDT not provided but expected for action";
			if (log != null) {
				text += ": " + log;
			}
			System.err.println(text);
		}
	}
	
	/** Returns the first top level <code>Window</code> component
	 * that is the ancestor of the given component. 
	 *  
	 * @param c <code>Component</code>; may be <b>null</b>
	 * @return <code>Window</code> or <b>null</b> if parameter was <b>null</b>
	 */
	public static Window getAncestorWindow (Component c)	{
	   while (c != null && !(c instanceof Window) ) 
	      c = c.getParent();
	   return (Window)c;
	}

	/** Shows the given image in a separate dialog which has no parent.
	 * 
	 * @param img {@code Image}
	 */
	public static void test_show_image (Image img, boolean modal) {
		System.out.println("-- showing image of size (" + img.getWidth(null) + ", " + img.getHeight(null) + ")");
		
	    JLabel label = new JLabel(new ImageIcon(img));
	    JPanel panel = new JPanel(new BorderLayout());
	    panel.add(label);
	    DialogPerformBlock block = new DialogPerformBlock() {
	    	@Override
			public Container getContent () {return panel;} 
		};
	    GSDialog dlg = new GSDialog(null, ButtonBarModus.OK, modal);
	    dlg.setPerformBlock(block);
	    dlg.pack();
	    dlg.setVisible(true);
	}

	/** Reads a one-line string of a moderate length from GUI user input.
	 * 'text' is shown in the same line before the input field.
	 * There are two termination options for the user OK and BREAK.
	 * 
	 * @param owner Component parent component of display or null
	 * @param title String dialog box title or null
	 * @param text String message shown before before input field 
	 * @return String input text (can be empty) or null iff terminated
	 *         other than by OK pressed 
	 */
	public static String readName (Component owner, String title, String text) {
	   // create component to display text and input field
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.add(new JLabel(text));
		panel.add(Box.createHorizontalStrut(10));
		JTextField inputFld = new JTextField(20);
		panel.add(inputFld);
		
	   // show message dialog
	   DialogTerminationType reply =
	   MessageDialog.showMessage(owner, title == null ? "Input" : title, panel, 
			   MessageType.question, ButtonBarModus.OK_BREAK);

	   // digest reply type and return input
	   if (reply == DialogTerminationType.OK_PRESSED) {
		   return inputFld.getText();
	   } 
	   return null;
	}

}
