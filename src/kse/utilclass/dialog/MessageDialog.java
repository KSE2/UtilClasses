/*
 *  MessageDialog in org.jpws.front
 *  file: MessageDialog.java
 * 
 *  Project Jpws-0-4-0
 *  @author Wolfgang Keller
 *  Created 11.04.2010
 *  Version
 * 
 *  Copyright (c) 2010 by Wolfgang Keller, Munich, Germany
 * 
 This program is not freeware software but copyright protected to the author(s)
 stated above. However, you can use, redistribute and/or modify it under the terms 
 of the GNU General Public License as published by the Free Software Foundation, 
 version 2 of the License.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 Place - Suite 330, Boston, MA 02111-1307, USA, or go to
 http://www.gnu.org/copyleft/gpl.html.
 */

package kse.utilclass.dialog;

/*
*  File: MessageDialog.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import kse.utilclass.misc.UnixColor;


public class MessageDialog extends GSDialog {
   public static enum MessageType { noIcon, info, question, warning, error }
   private static final Color WARNING_COLOR = UnixColor.Coral;
   private static final Color ERROR_COLOR = UnixColor.LightCoral;
   private static final Color QUESTION_COLOR = UnixColor.LemonChiffon;
   private static final Color INFO_COLOR = UnixColor.PaleGreen;

   private static boolean infosColored = true;
   
   private MessageContentPanel   dialogPanel;
   
/**
 * Creates an empty, non-modal info message dialog with the active mainframe
 * as owner and OK + CANCEL buttons. 
 */ 
public MessageDialog () throws HeadlessException {
   super(GUIService.getMainFrame(), ButtonBarModus.OK_BREAK, false);
   init( MessageType.info );
}

/**
 * Creates an empty info message dialog 
 * of the given dialog type and modality.
 *  
 * @param owner <code>Window</code> the parent window (Dialog or Frame);
 *        may be <b>null</b> for global active frame
 * @param dlgType int, dialog type declares standard buttons used 
 *        (values from class <code>DialogButtonBar</code>,  
 *        use <code>DialogButtonBar.BUTTONLESS</code> for void button bar) 
 * @param modal boolean, whether this dialog is operating modal
 * 
 * @throws HeadlessException
*/ 
public MessageDialog (Window owner, ButtonBarModus dlgType, boolean modal)
      throws HeadlessException {
   this(owner, null, MessageType.info, dlgType, true);
}

/**
 * Creates a message dialog with given content and settings.
 * 
 * @param owner <code>Window</code> the parent window (Dialog or Frame);
 *        may be <b>null</b> for global active frame
 * @param text Object the text content; may be <b>null</b> 
 * @param msgType <code>MessageType</code> determines display outfit (e.g. icon)
 * @param dlgType int, dialog type declares standard buttons used 
 *        (values from class <code>DialogButtonBar</code>,  
 *        use <code>DialogButtonBar.BUTTONLESS</code> for void button bar) 
 * @param modal boolean, whether this dialog is operating modal
 * 
 * @throws HeadlessException
 */
public MessageDialog (Window owner, 
		Object text, 
		MessageType msgType, 
		ButtonBarModus dlgType, 
		boolean modal)    throws HeadlessException {
	
   super(owner == null ? GUIService.getMainFrame() : owner, dlgType, modal);
   init(msgType);
   if (text != null) {
      setText(text);
   }
}

/**
 * Creates a modal info message dialog with OK_BUTTON
 * and the given text content.
 *   
 * @param owner <code>Window</code> the parent window (Dialog or Frame);
 *        may be <b>null</b> for global active frame
 * @param text <code>Object</code> the text content (<code>String</code> 
 *        or <code>Component</code>); may be <b>null</b>
 *        
 * @throws HeadlessException
 */
public MessageDialog (Window owner, Object text)  throws HeadlessException  {
   this(owner, text, MessageType.info, ButtonBarModus.OK, true);
}

private void init (MessageType mType) {
   // body panel (base)
   dialogPanel = new MessageContentPanel() ;
   setMessageType( mType );
   setPerformBlock( new MessagePerformBlock() );
}

/** Sets the message display type for this dialog.
 * 
 * @param type <code>MessageType</code> new message type
 */
public void setMessageType ( MessageType type ) {
   // switch coloured panels according to user setting
   if (!infosColored) {
	   dialogPanel.setInfoBgdColor(null);
	   dialogPanel.setQuestionBgdColor(null);
   }

   dialogPanel.setMessageType( type );
}

/** Sets whether INFO and QUESTION type messages receive a background coloured 
 * panel. By default the panels are coloured.
 * 
 * @param b boolean true = panel coloured, false = panel not coloured
 */
public static void setInfosColored (boolean b) {
	infosColored = b; 
}

/** Returns the <code>MessageType</code> of this dialog.
 *  
 * @return <code>MessageType</code>
 */
public MessageType getMessageType () {
   return dialogPanel.getMessageType();
}

/**
 * Sets a text label to this message dialog and resizes
 * it if required.
 * 
 * @param text <code>Object</code> new text content
 *        (an object of type <code>String</code> or <code>Component</code>)
 */
public void setText (Object text) {
   dialogPanel.setText( text );
   if ( isShowing() )
      pack();
}

/**
 * Sets an icon for the message display.
 * 
 * @param icon <code>Icon</code> new message logo, use <b>null</b> to clear
 */
public void setIcon (Icon icon) {
   dialogPanel.setIcon( icon );
   if ( isShowing() )
      pack();
}

// ****************** STATIC SERVICE OFFER *****************

/** Returns a <code>JPanel</code> with a message display format and content.
 *   
 * @param text <code>Object</code> String or Component
 * @param type <code>MessageType</code> layout appearance
 * @return <code>MessageContentPanel</code>
 */
public static MessageContentPanel createMessageContentPanel ( Object text, MessageType type ) {
   MessageContentPanel p = new MessageContentPanel();

   // switch coloured panels according to user setting
   if (!infosColored) {
	   p.setInfoBgdColor(null);
	   p.setQuestionBgdColor(null);
   }

   p.setMessageType( type );
   p.setText( text );
   return p;
}

public static JLabel createMessageTextLabel (String text) {
   if (!text.toLowerCase().startsWith("<html>")) {
	   text = "<html>" + text;
   }
   JLabel label = new JLabel(text);
   label.setOpaque( false );
   return label;
}

/** Displays a simple modal info message that the user can click away
 * through "Ok" button. Display is guaranteed to run on the EDT. The calling 
 * thread blocks until user input is available or the thread is interrupted.
 *  
 * @param parent <code>Component</code> the component this message's 
 *               display is related to; may be null
 * @param title <code>String</code> dialog title; may be <b>null</b>              
 * @param text <code>Object</code> text to display (<code>Component</code> 
 *             or <code>String</code>)
 * @param type <code>MessageType</code> appearance quality            
 */
public static void showInfoMessage (Component parent, String title, 
		Object text, MessageType type) {
   showMessage(parent, title, text, type, ButtonBarModus.OK);
}

/** Displays a modal question message which the user can answer with "Yes" 
 * or "No". Display is guaranteed to run on the EDT. The calling thread blocks 
 * until user input is available or the thread is interrupted.
 * 
 * @param parent <code>Component</code> the component this message's 
 *               display is related to; may be null
 * @param title <code>String</code> dialog title; may be <b>null</b>              
 * @param text <code>Object</code> text to display (<code>Component</code> 
 *             or <code>String</code>)
 * @param dlgType int dialog type, one of DialogButtonBar.YES_NO_BUTTON, DialogButtonBar.OK_CANCEL_BUTTON           
 * @return boolean <b>true</b> == "Yes" (confirmed), <b>false</b> == "No" (rejected/unanswered) 
 */
public static boolean showConfirmMessage ( final Component parent, 
		                                   final String title, 
                                           final Object text, 
                                           final ButtonBarModus dlgType ) {

	return showMessage(parent, title, text, MessageType.question, dlgType) == DialogTerminationType.OK_PRESSED;
}

/** Displays a modal message box with user interaction as designed by given
 * parameters. Display is guaranteed to run on the EDT. The calling thread 
 * blocks until user input is available or the thread is interrupted.
 * 
 * @param parent <code>Component</code> the component this message's 
 *               display is related to; may be null
 * @param title <code>String</code> dialog title; may be null      
 * @param text <code>Object</code> text to display (<code>Component</code> 
 *             or <code>String</code>); may be null
 * @param msgType <code>MessageType</code> appearance quality            
 * @param dlgType {@code ButtonBarModus} dialog type for button setup 
 * @return boolean[] with values indicating what action was taken to terminate
 *         the dialog: 0 = confirmed, 1 = Cancel, 2 = No, 3 = Escape 
 */
public static DialogTerminationType showMessage ( 
					final Component parent, 
		            final String title, 
                    final Object text, 
                    final MessageType msgType,
                    final ButtonBarModus dlgType )
{
   final DialogTerminationType[] result = new DialogTerminationType[1];
   Window owner = parent == null ? GUIService.getMainFrame() 
		   					: GUIService.getAncestorWindow(parent);
   
   Runnable run = new Runnable() {
		@Override
		public void run() {
		   MessageDialog dlg = new MessageDialog(owner, text, msgType, dlgType, true);
		   boolean singleButton = dlgType == ButtonBarModus.OK | 
				   dlgType == ButtonBarModus.CONTINUE |
				   dlgType == ButtonBarModus.SINGLE;
		   String defTitle = singleButton ? "Information" : "Please Confirm";
		   String hstr = title == null ? defTitle : title;
		   dlg.setTitle(hstr);
		   dlg.pack();
		   dlg.setVisible(true);
		   
		   result[0] = dlg.getTerminationType();
		}
   };

   try {
   	  GUIService.performOnEDT(run, true);
	} catch (InvocationTargetException e) {
		e.printStackTrace();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}

   return result[0];
}

/** Creates a message dialog of the given properties without showing it.
 *  
 * @param parent <code>Component</code> the component this message's 
 *               display is related to; may be null
 * @param title <code>String</code> dialog title; may be <b>null</b> or a code           
 * @param text <code>Object</code> text to display (<code>Component</code> 
 *             or <code>String</code>)
 * @param msgType <code>MessageType</code> appearance quality            
 * @param dlgType {@code ButtonBarModus} dialog type for button setup 
 * @param modal boolean 
 * @return
 */
public static MessageDialog createMessageDialog ( 
		final Component parent, 
        final String title, 
        final Object text, 
        final MessageType msgType,
        final ButtonBarModus dlgType,
        final boolean modal )
{
	final MessageDialog[] dlgA = new MessageDialog[1];
	Window owner = parent == null ? GUIService.getMainFrame() 
						: GUIService.getAncestorWindow(parent);
	
	Runnable run = new Runnable() {
		@Override
		public void run() {
			MessageDialog dlg = new MessageDialog(owner, text, msgType, dlgType, modal);
			boolean singleButton = dlgType == ButtonBarModus.OK | 
				   dlgType == ButtonBarModus.BREAK |
				   dlgType == ButtonBarModus.CONTINUE |
				   dlgType == ButtonBarModus.SINGLE;
			String defTitle = singleButton ? "Information" : "Please Confirm";
			String hstr = title == null ? defTitle : title;
			dlg.setTitle(hstr);
			dlg.pack();
			dlgA[0] = dlg; 
		}
	};

	try {
		GUIService.performOnEDT(run, true);
	} catch (InvocationTargetException | InterruptedException e) {
		e.printStackTrace();
	}
	return dlgA[0];
}

/** Runs "setVisible(true)" on the given dialog guaranteed on the EDT.
 * @param dialog JDialog
 */
private static void setDialogVisible ( final JDialog dialog ) {
	
    Runnable run = new Runnable() {
		@Override
		public void run() {
	       try { dialog.setVisible(true); }
	       catch ( Exception e )
	       {}
		}
    };

    try {
     	  GUIService.performOnEDT(run, true);
	  } catch (InvocationTargetException e) {
		e.printStackTrace();
	  } catch (InterruptedException e) {
		e.printStackTrace();
	  }
}

// ***********  INNER CLASS  ******************

private class MessagePerformBlock extends DialogPerformBlock {

	@Override
	public Container getContent() {
		return dialogPanel;
	}

	@Override
	public boolean perform_button (int index, ButtonType type, JButton button) {
		switch (type) {
		case YES_BUTTON: setUserConfirmed(true); break;
		case CANCEL_BUTTON: setUserConfirmed(false); break;
		case NO_BUTTON: setNoTerminated(true); break;
		default:
		}
		return true;
	}
}

/**
 * A JPanel for holding message display data and layout
 * consisting of an Icon on the left side and a centred 
 * text block on the right. The text may be represented
 * by either a string or a <code>Component</code>.
 * 
 */
public static class MessageContentPanel extends JPanel {

   private JPanel   textPanel;
   private JLabel   iconLabel;
   private MessageType messageType = MessageType.noIcon;
   private Color infoColor = INFO_COLOR;
   private Color questionColor = QUESTION_COLOR;

   /** Creates a new message panel of type <code>noIcon</code>
    * and which is empty of content.
    */
   public MessageContentPanel () {
      super(new BorderLayout(6, 6));
      init();
   }

   private void init () {
      // body panel (base)
      setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 25));
   
      // prepare TEXT label (CENTER of display)
      textPanel = new JPanel( new BorderLayout() ) ;
      textPanel.setOpaque( false );
      add( textPanel, BorderLayout.CENTER );
   
      // prepare ICON label (WEST side of display)
      iconLabel = new JLabel();
      iconLabel.setIconTextGap( 0 );
      iconLabel.setHorizontalAlignment( SwingConstants.CENTER );
      iconLabel.setBorder(BorderFactory.createEmptyBorder( 10, 10, 10, 10 ));
      add( iconLabel, BorderLayout.WEST );
   }

   /** Sets the background color of the INFO-type message display.
    * 
    * @param c Color; may be null for no color 
    */
   public void setInfoBgdColor (Color c) {
	   infoColor = c;
   }
   
   /** Sets the background color of the QUESTION-type message display.
    * 
    * @param c Color; may be null for no color 
    */
   public void setQuestionBgdColor (Color c) {
	   questionColor = c;
   }
   
   /** Sets the message display type for this dialog.
    * 
    * @param type <code>MessageType</code> new message type
    */
   public void setMessageType ( MessageType type ) {
      String hstr;
      Icon icon;
      
      // avoid unnecessary action
      if ( type == messageType ) return;
      
      // set text background color
      setOpaque(true);
      switch (type) { 
      case warning:
         setBackground( WARNING_COLOR );
         break;
      case error:
         setBackground( ERROR_COLOR );
         break;
      case question:
    	  if (questionColor != null) {
    		 setBackground(questionColor);
    	  } else {
    	     setOpaque(false);
    	  }
          break;
      case info:
    	  if (infoColor != null) {
    		 setBackground(infoColor);
    	  } else {
    	     setOpaque(false);
    	  }
          break;
      default:
         setOpaque(false);
      }
      
      // select icon from message type
      if ( type == MessageType.info )
         hstr =  "OptionPane.informationIcon";
      else if ( type == MessageType.question )
         hstr = "OptionPane.questionIcon";
      else if ( type == MessageType.error )
         hstr = "OptionPane.errorIcon";
      else
         hstr = "OptionPane.warningIcon";
      
      // obtain UI standard icon for messages
      // and set icon
      if ( type != MessageType.noIcon ) {
         icon = UIManager.getIcon( hstr );
         setIcon( icon );
      }
      else
         setIcon( null );
      
      messageType = type;
   }

   /**
    * Sets an icon for the message display.
    * 
    * @param icon <code>Icon</code> new message logo, use <b>null</b> to clear
    */
   public void setIcon (Icon icon) {
      JLabel icL = getIconLabel();
      icL.setIcon( icon );
   }

   /** Returns the <code>MessageType</code> of this dialog.
    *  
    * @return <code>MessageType</code>
    */
   public MessageType getMessageType () {
      return messageType;
   }

   private JPanel getTextPanel () {
      return textPanel;
   }

   private JLabel getIconLabel () {
      return iconLabel;
   }

   /**
    * Sets a text label to this message dialog and resizes
    * it if required.
    * 
    * @param text <code>Object</code> new text content
    *        (an object of type <code>String</code> or <code>Component</code>)
    */
   public void setText (Object text) {
      JPanel panel;
      JLabel label;
   
      panel = getTextPanel();
      panel.removeAll();
      
      if ( text != null ) {
         if ( text instanceof String ) {
            label = createMessageTextLabel( (String)text );
            panel.add( label );

         } else if ( text instanceof Component ) {
            panel.add( (Component)text );
         }
      }
   }

}

}
