package kse.utilclass.dialog;

/*
*  File: DialogPerformBlock.java
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

import java.awt.Container;

import javax.swing.JButton;

import kse.utilclass.dialog.GSDialog.ButtonType;

/** Class to hold visible user content for a {@code GSDialog} and code for 
 * dealing with button-pressed events from the dialog's button-bar. 
 * The user content comprises all visible content except the button-bar, 
 * which is organised by the dialog class.
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

	/** This method is called by the dialog ({@code GSDialog}) and performs a
	 * pressed button's activity. The button can be a standard or a user 
	 * defined button. The implementation of {@code DialogPerformBlock} 
	 * calls methods 'setUserConfirmed()' or 'setNoTerminated()' w/ arguments
	 * depending on whether YES/OK, NO or CANCEL have been pressed. It then 
	 * returns 'true' in case a standard button or 'false' if a user button 
	 * has been pressed. The return value determines whether the dialog will
	 * be closed. 
	 * <p>The user can override this method in order to perform individual 
	 * tasks and intercept the decision to close the dialog.
	 * 
	 * @param index int index number of button
	 * @param type {@code ButtonType}
	 * @param button {@code JButton} button clicked
	 * @return boolean true = dispose dialog, false = continue dialog
	 */
	public boolean perform_button (int index, ButtonType type, JButton button) {
		switch (type) {
		case YES_BUTTON:  setUserConfirmed(true); setNoTerminated(false);
		break;
		case NO_BUTTON:	  setUserConfirmed(false); setNoTerminated(true); 
		break;
		case CANCEL_BUTTON:	 setUserConfirmed(false); setNoTerminated(false); 
		break;
		default: return false;
		}
		return true;
	}
	
	/** Whether the user terminated the dialog via the "YES" or "OK" button. 
	 * This property can be set programmatically by method 
	 * 'setUserConfirmed()'. It is automatically set by the default 
	 * implementation of class {@code DialogPerformBlock}.
	 * 
	 * @return boolean true = user confirmed, false = unconfirmed termination
	 */
	public boolean getUserConfirmed () {return userConfirmed;}

	/** Sets the "User Confirmed" property of this performance block.
	 * This method is automatically called by class {@code DialogPerformBlock}
	 * when a standard button of the button-bar is pressed which by default
	 * leads to the closing of the dialog.
	 * 
	 * @param confirmed boolean
	 */
	public void setUserConfirmed (boolean confirmed) {
		userConfirmed = confirmed;
	}

	/** Whether the user terminated the dialog via the "No" button. This 
	 * property can be set programmatically by method 'setNoTerminated()'.
	 * It is automatically set by the default implementation of class
	 * {@code DialogPerformBlock}.
	 * 
	 * @return boolean true = "No" termination, false = other state
	 */
	public boolean getNoTerminated () {return noTerminated;}

	/** Sets the "No-Terminated" property of this performance block.
	 * This method is automatically called by class {@code DialogPerformBlock}
	 * when a standard button of the button-bar is pressed which by default
	 * leads to the closing of the dialog.
	 * 
	 * @param v boolean true = "No" terminated
	 */
	public void setNoTerminated (boolean v) {
		noTerminated = v;
	}
}
