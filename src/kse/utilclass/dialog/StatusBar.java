/*
 *  StatusBar in org.jpws.front
 *  file: StatusBar.java
 * 
 *  Project Jpws-Front
 *  @author Wolfgang Keller
 *  Created 02.10.2005
 *  Version
 * 
 *  Copyright (c) 2011 by Wolfgang Keller, Munich, Germany
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *  A status bar component (JPanel) with multiple separately addressable display 
 *  elements. These elements are "Text", "File Format" and "Program Activity".
 *  <p>The status bar is a constitutional part of the program mainframe (
 *  <code>PwsafeJ</code>).
 *  
 *  <p>StatusBar operates display activity safely on the EDT. 

 */
public class StatusBar extends JPanel
{
   /** Message display duration time; 30 seconds. */
   public static final int STATUSTEXTDELAY = 30000;
   
   public static final int ACTIVE = 1;
   public static final int PASSIVE = 0;
   
   /** Internal marker for operating the SwingUtilities block. */
   private enum OperationType { message, counter, dataformat, activity, font }; 

   private JPanel          rightPanel;
   private JLabel          textField = new JLabel();
   private JLabel          activeLabel = new JLabel();
   private JLabel          formatLabel = new JLabel();
   private JLabel          counterLabel = new JLabel();
   private Timer           statusTimer;
   private TimerTask       statusTextRemover;
   private int			   activityCounter;

   
   public StatusBar () {
	   this(null);
   }
   
	public StatusBar (Timer timer) {
	   super(new BorderLayout());
	   statusTimer = timer;
	   if (timer == null) {
		   statusTimer = new Timer();
	   }
	   
	   init();
	}

private void init () {
   JPanel panel;
   
   // determine fonts
   setFont( textField.getFont() );
//   statusTimer = GameHandler.get().getGlobalTimer();
   
   // init message text output
   add( textField, BorderLayout.CENTER );
   textField.setBorder( BorderFactory.createCompoundBorder(  
         BorderFactory.createMatteBorder( 1, 1, 1, 1, Color.gray ),
         BorderFactory.createEmptyBorder( 3, 8, 3, 2 ) ));

   // init right side parts
   rightPanel = new JPanel( new BorderLayout() );
   add( rightPanel, BorderLayout.EAST );
   panel = new JPanel( new BorderLayout() );
   rightPanel.add( panel, BorderLayout.EAST );

   // program activity icon
   panel.add( activeLabel, BorderLayout.EAST );
   activeLabel.setPreferredSize( new Dimension( 22, 16 ) );
   activeLabel.setBorder( BorderFactory.createCompoundBorder(  
         BorderFactory.createMatteBorder( 1, 0, 1, 0, Color.gray ),
         BorderFactory.createEmptyBorder( 2, 3, 2, 0 ) ));
   
   // record counter cell
   panel.add( counterLabel, BorderLayout.CENTER );
   counterLabel.setVisible( false );
   counterLabel.setBorder( BorderFactory.createCompoundBorder(  
         BorderFactory.createMatteBorder( 1, 0, 1, 1, Color.gray ),
         BorderFactory.createEmptyBorder( 2, 4, 2, 4 ) ));

   // file format cell
   rightPanel.add( formatLabel, BorderLayout.CENTER );
   formatLabel.setVisible( false );
   formatLabel.setBorder( BorderFactory.createCompoundBorder(  
         BorderFactory.createMatteBorder( 1, 0, 1, 1, Color.gray ),
         BorderFactory.createEmptyBorder( 2, 4, 2, 4 ) ));
   
}  // init

/** Drops a text message into the status line. 
 *  
 * @param text String display code or real text 
 */
public void setMessage ( Object text ) {
	if (!isShowing()) return;
	
   // cancel a previous timer task for text removal
   if ( statusTextRemover != null ) {
      statusTextRemover.cancel();
   }
   
   // setup a new timer task for message removal
   statusTextRemover = new TimerTask() {
      @Override
	  public void run () {
         startOperation( OperationType.message, null );
      }
   };
   statusTimer.schedule( statusTextRemover, STATUSTEXTDELAY );

   // format the text
   String txt = text == null ? "" : text.toString();
   if ( txt.length() > 80 ) {
      txt = txt.substring( 0, 77 ) + " ..";
   }

   // start display of text on EDT
   startOperation( OperationType.message, text );
}

/** Sets the content of the "Format" cell of the status line. 
 *  If <b>null</b> the cell will be invisible. 
 * 
 *  @param text new content of Format cell
 */
public void setFormatCell ( String text ) {
//   Log.log( 10, "(StatusBar.setFormatCell) set FORMAT with " + text); 
   startOperation( OperationType.dataformat, text );
}

/** Sets the content of the "Text Encoding" cell of the status line. 
 *  If <b>null</b> the cell will be invisible. 
 * 
 *  @param text new content of Encoding cell
 */
public void setCounterCell ( String text ) {
//   Log.log( 10, "(StatusBar.setRecordCounterCell) set COUNTER with " + text ); 
   startOperation( OperationType.counter, text );
}

/** Informs StatusBar about the current program activity modus. This will set 
 *  the content of the "Program Activity" cell.
 * 
 *  @param activity program activity constant (ACTIVE or PASSIVE)   
 * */
public void setActivity ( boolean active ) {
//   Log.log( 10, "(StatusBar.setActivity) set ACTIVITY with " + activity );
   activityCounter += active ? 1 : -1;
   if ( activityCounter < 0 ) activityCounter = 0;

   // show or hide symbol on rising or falling flank of activityStack (0)
   if ( activityCounter == 0 & !active || 
		activityCounter == 1 & active ) {
      startOperation(OperationType.activity, active);
   }
}

@Override
public void setFont ( Font font ) {
   startOperation( OperationType.font, font );
}

protected void startOperation ( OperationType dataformat, Object param ) {
   GUIService.performOnEDT( new EDT_Operation( dataformat, param ) );
}

/** This class runs Swing relevant operations in a shell
 * in order to guarantee their performance on the EDT.  
 */
private class EDT_Operation implements Runnable {
   private OperationType operation;
   private Object param;

   EDT_Operation ( OperationType type, Object param ) {
      operation = type;
      this.param = param;
   }

   @Override
   public void run () {
      try {
         switch ( operation ) {
         case message:
            textField.setText( param == null ? null : param.toString() );
            break;
            
         case dataformat:
//            Log.log( 10, "(StatusBar.SwingOperation.run) DATAFORMAT with ".concat( 
//                  par == null ? "null" : par.toString() ));
            if ( param == null ) {
               formatLabel.setVisible( false );
            } else {
               formatLabel.setText( param.toString() );
               formatLabel.setVisible( true );
            }
            break;
            
         case counter:
//            Log.log( 10, "(StatusBar.SwingOperation.run) COUNTER with ".concat( 
//                  par == null ? "null" : par.toString() ));
            if ( param == null ) {
               counterLabel.setVisible( false );
            } else {
               counterLabel.setText( param.toString() );
               counterLabel.setVisible( true );
            }
            break;
            
         case activity:
            boolean activ = param != null && (Boolean) param;
////            Log.log( 10, "(StatusBar.SwingOperation.run) ACTIVITY with ".concat( String.valueOf( activ )));
//            Icon icon = activ ? ResourceLoader.getImageIcon( "activity" ) : null;
//            activeLabel.setIcon( icon );
            break;
            
         case font:
            if ( param != null ) {
               Font font = (Font)param;
//               Log.log( 10, "(StatusBar.SwingOperation.run) FONT with ".concat( font.getName() ));
               if ( font != null & counterLabel != null ) {
                  font = font.deriveFont( Font.PLAIN );
                  counterLabel.setFont( font );
                  formatLabel.setFont( font );
                  textField.setFont( font );
               }
               StatusBar.super.setFont( font );
            }
            break;
         }
      } catch ( Exception e )
      { e.printStackTrace(); }
   }
}

}
