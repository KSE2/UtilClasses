package kse.utilclass.dialog;

import java.awt.Container;

import javax.swing.JButton;

import kse.utilclass.dialog.GSDialog.ButtonType;

/** Class to hold user content and code for dealing with button-pressed
 * events from the dialog's button-bar. The user content comprises all
 * visible content except the button-bar, which is organised by the 
 * dialog class ({@code GSDialog}).
 * 
 */
public abstract class DialogPerformBlock {
	
	private boolean userConfirmed;
	private boolean noTerminated;

	/** Returns the container which holds all display content of the dialog
	 * except the button bar.
	 *  
	 * @return {@code Container} may be null
	 */
	public abstract Container getContent ();

	/** Method triggered to perform a button's activity. This is user supplied
	 * code to deal with variants of button-pressed events. The return value
	 * 'true' can be used to dispose the dialog. The default value is 'true'.
	 * 
	 * @param index int index number of button
	 * @param type {@code ButtonType}
	 * @param button {@code JButton} button clicked
	 * @return boolean true = dispose dialog, false = continue dialog
	 */
	public boolean perform_button (int index, ButtonType type, JButton button) {return true;}
	
	/** Whether the user confirmed the result of editing the dialog via
	 * a confirmation button, like "OK", "Yes" or "Continue".
	 * This property has to be set programmatically by method 
	 * 'setUserConfirmed()' of this perform-block. The default value is 
	 * 'false'.
	 * 
	 * @return boolean true = user confirmed, false = broken termination
	 */
	public boolean getUserConfirmed () {return userConfirmed;}

	/** Sets the "User Confirmed" property of this performance block.
	 * 
	 * @param confirmed boolean
	 */
	public void setUserConfirmed (boolean confirmed) {
		userConfirmed = confirmed;
	}

	/** Whether the user terminated the dialog via the "No" button. This 
	 * property has to be set programmatically by method 'setNoTerminated()'
	 * of this perform-block. The default value is 'false'.
	 * 
	 * @return boolean true = "No" termination, false = other state
	 */
	public boolean getNoTerminated () {return noTerminated;}

	/** Sets the "No-Terminated" property of this performance block.
	 * 
	 * @param v boolean true = "No" terminated
	 */
	public void setNoTerminated (boolean v) {
		noTerminated = v;
	}
}
