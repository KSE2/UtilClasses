package kse.utilclass.dialog;

/*
*  File: DialogPerformBlock.java
* 
*  Project JUtilClasses
*  @author Wolfgang Keller
*  Created 
* 
*  Copyright (c) 2022 by Wolfgang Keller, Munich, Germany
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
