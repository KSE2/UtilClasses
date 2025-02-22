package kse.utilclass.dialog;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/*
*  File: GSDialog.java
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

public class GSDialog extends JDialog {
	
	private static final long serialVersionUID = -99786334991923723L;
	public enum ButtonType {YES_BUTTON, NO_BUTTON, CANCEL_BUTTON, USER_BUTTON}
	
	private ButtonType[] buttonTypes;
	private DialogPerformBlock performBlock;
	private JPanel buttonBar;
	private ButtonBarModus buttonBarModus = ButtonBarModus.OK;
	private transient ActionListener buttonPerformer = new ButtonPerformer();
	private boolean closeableByEscape = true;
	private boolean closedByEscape;
	private boolean closedByButton;
	

	public GSDialog (Window owner, ButtonBarModus dlgType, boolean modal) {
		super(owner == null ? GUIService.getMainFrame() : owner);
		setModal(modal);
		Objects.requireNonNull(dlgType);
		buttonBarModus = dlgType;
		init();
	}

	/** Creates a dialog with OK and CANCEL buttons in the button bar.
	 * 
	 * @param owner Window, may be null 
	 * @param modal boolean
	 */
	public GSDialog (Window owner, boolean modal) {
		this(owner, ButtonBarModus.OK_BREAK, modal);
	}
	
	/** Returns the {@code ButtonBarModus} for the dialog to be created.
	 * This value determines appearance and number of buttons in the 
	 * button bar. The default value is OK_BREAK.
	 * 
	 * @return {@code ButtonBarModus}
	 */
	public ButtonBarModus getButtonBarModus () {return ButtonBarModus.OK_BREAK;}
	
	/** Sets the user content of this dialog via the dialog perform block.
	 * The content of a dialog may be changed while being visible. If the
	 * dialog is displayable, it is also packed.
	 * 
	 * @param pblock {DialogPerformBlock}, may be null
	 */
	public void setPerformBlock (DialogPerformBlock pblock) {
		// remove any previous user content
		if (performBlock != null && performBlock.getContent() != null) {
			getContentPane().remove(performBlock.getContent());
		}

		// set the new content
		if (pblock != null && pblock.getContent() != null) {
			getContentPane().add(pblock.getContent(), BorderLayout.CENTER);
			performBlock = pblock;
			if (isDisplayable()) {
				pack();
			}
		}
	}
	
	public DialogPerformBlock getPerformBlock () {return performBlock;}

	/** Returns the n-th button in the button bar of this dialog. Buttons count
	 * from left to right starting with 0.
	 * 
	 * @param index int
	 * @return {@code JButton} or null if unavailable
	 */
	public JButton getButton (int index) {
		return buttonBar.getComponentCount() <= index ? null : 
			(JButton)buttonBar.getComponent(index);
	}

	public ButtonType getButtonType (int index) {
		return buttonTypes[index];
	}
	
	/** The number of buttons in the button bar of this dialog.
	 * 
	 * @return int 
	 */
	public int getButtonCount () {
		return buttonBar.getComponentCount();
	}
	
	/** Returns the type of termination after this dialog has been disposed
	 * or null if the dialog is not disposed or never started.
	 * 
	 * @return {@code DialogTerminationType} or null
	 */
	public DialogTerminationType getTerminationType () {
		if (!isDisplayable()) {
			if (closedByEscape) return DialogTerminationType.CLOSE_BY_ESCAPE;
			if (!closedByButton) return DialogTerminationType.INTERRUPTED;
			if (performBlock != null) {
				if (performBlock.getNoTerminated()) return DialogTerminationType.NO_PRESSED;
				if (performBlock.getUserConfirmed()) return DialogTerminationType.OK_PRESSED;
				return DialogTerminationType.CANCEL_PRESSED;
			}

		// displayable
		} else if (isModal() && !isVisible()) {
			return DialogTerminationType.INTERRUPTED;
		}
		return null;
	}
	
	public boolean isOkPressed () {
		return getTerminationType() == DialogTerminationType.OK_PRESSED;
	}
	
	public boolean isCancelPressed () {
		return getTerminationType() == DialogTerminationType.CANCEL_PRESSED;
	}
	
	@Override
	public void dispose() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				GSDialog.super.dispose();
			}
		};
		
		try {
			GUIService.performOnEDT(r, true);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void init () {
		// set window close button
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
	    // register ESCAPE key as (optional) window close trigger
	    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	    getRootPane().registerKeyboardAction(
	          new KListener(), stroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

	    // set up the button bar
		buttonBar = new JPanel();
		getContentPane().add(buttonBar, BorderLayout.SOUTH);
		
		int nrbut = 0;
		switch (buttonBarModus) {
		case SINGLE: 
		case CONTINUE:
		case OK:
		case BREAK:
			nrbut = 1;
			break;
		case DOUBLE:
		case CONTINUE_BREAK:
		case OK_BREAK:
		case YES_NO:
			nrbut = 2;
			break;
		case TRIPLE:
		case YES_NO_BREAK:
			nrbut = 3;
			break;
		default:
		}
		if (nrbut == 0) return;
		
		buttonTypes = new ButtonType[nrbut];
		
		// define buttons (and link related actions)
		for (int i = 0; i < nrbut; i++) {
			JButton b = new JButton();
			b.addActionListener(buttonPerformer);
			buttonBar.add(b);
		}
		
		// setup button properties
		JButton b;
		// first button CONTINUE
		if (buttonBarModus == ButtonBarModus.CONTINUE || 
			buttonBarModus == ButtonBarModus.CONTINUE_BREAK) {
			b = getButton(0);
			b.setText("Weiter");
			buttonTypes[0] = ButtonType.YES_BUTTON;
		}
		
		// first button OK
		if (buttonBarModus == ButtonBarModus.OK || 
			buttonBarModus == ButtonBarModus.OK_BREAK) {
			b = getButton(0);
			b.setText("OK");
			buttonTypes[0] = ButtonType.YES_BUTTON;
		}
		
		// first button YES
		if (buttonBarModus == ButtonBarModus.YES_NO || 
			buttonBarModus == ButtonBarModus.YES_NO_BREAK) {
			b = getButton(0);
			b.setText("Ja");
			buttonTypes[0] = ButtonType.YES_BUTTON;
		}
		
		// first button BREAK
		if (buttonBarModus == ButtonBarModus.BREAK) {
			b = getButton(0);
			b.setText("Abbruch");
			buttonTypes[0] = ButtonType.CANCEL_BUTTON;
		}
		
		// second button BREAK
		if (buttonBarModus == ButtonBarModus.OK_BREAK || 
			buttonBarModus == ButtonBarModus.CONTINUE_BREAK) {
			b = getButton(1);
			b.setText("Abbruch");
			buttonTypes[1] = ButtonType.CANCEL_BUTTON;
		}
		
		// second button NO
		if (buttonBarModus == ButtonBarModus.YES_NO || 
			buttonBarModus == ButtonBarModus.YES_NO_BREAK) {
			b = getButton(1);
			b.setText("Nein");
			buttonTypes[1] = ButtonType.NO_BUTTON;
		}
		
		// third button BREAK
		if (buttonBarModus == ButtonBarModus.YES_NO_BREAK) {
			b = getButton(2);
			b.setText("Abbruch");
			buttonTypes[2] = ButtonType.CANCEL_BUTTON;
		}

		// set root-pane default button
		if (nrbut > 0) {
			getRootPane().setDefaultButton(getButton(0));
		}
	}
	
	@Override
	public void setVisible (boolean v) {
		if (v && !isShowing()) {
			setLocationRelativeTo(getOwner());
		}
		super.setVisible(v);
	}
	
	/** Sets this dialog visible (true) without relocating it.
	 */
	public void setVisible () {
		super.setVisible(true);
	}
	
    /** Sets whether this dialog can be closed by the ESCAPE button.
     * The default value is <b>true</b>.
     * <p>If <b>true</b>, the triggered action of the ESCAPE button is 
     * equivalent to the <tt>WindowClosing</tt> event (as called by a
     * frame decoration close, e.g.).
     *  
     * @param v boolean <b>true</b> == closeable by ESCAPE  
     */
    public void setCloseByEscape (boolean v) {
       closeableByEscape = v;
    }

	private class ButtonPerformer implements ActionListener {

		@Override
		public void actionPerformed (ActionEvent e) {
			JButton button = (JButton)e.getSource();

			// determine index of triggered button
			int count = getButtonCount();
			int i;
			for (i = 0; i < count; i++) {
				if (button == buttonBar.getComponent(i)) break;
			}
			if (i == count) {
				throw new IllegalStateException("(GSDialog.ButtonPerformer) illegal button reference");
			}

			// call user routine
			if (performBlock != null && performBlock.perform_button(i, getButtonType(i), button)) {
				closedByButton = true;
				dispose();
			}
		}
	}
	
	   /** Class listening to key events which were registered at the root pane. 
	    *  Currently only ESCAPE-key. */
	   private class KListener implements ActionListener {
	      @Override
	      public void actionPerformed (ActionEvent e) {
	         if (closeableByEscape) {
	            processWindowEvent(new WindowEvent(GSDialog.this, WindowEvent.WINDOW_CLOSING));
                closedByEscape = true;
	         }
	      }
	   }
	   

}
