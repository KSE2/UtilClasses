
package kse.utilclass.gui;

/*
*  File: AmpleTextArea.java
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import kse.utilclass.dialog.GUIService;
import kse.utilclass.misc.Log;
import kse.utilclass.misc.Util;

/**
 * This extension of <code>JTextArea</code> adds the following features to the
 * editor field.
 * 
 * <p>1. Undo/Redo manager with 100 operations stack and a time based text
 * agglomeration function
 * <br>2. a context menu popping up at mouse right-click with commands to
 * handle clipboard exchange and deletion, undo/redo, printing, line-wrapping
 * and select-all
 * <br>3. keystroke support with these (additional) assignments: CTRL-W 
 * (select-word), CTRL-L (select line), CTRL-P (select paragraph), 
 * CTRL-Z (undo), CTRL-Y, CTRL-SHIFT-Z (redo), CTRL-D (current date), 
 * CTRL-T (current time), CTRL-U (universal date and time), F1 (help), 
 * CTRL-PLUS (increase font size), CTRL-MINUS (decrease font size). 
 * 
 * <p>With {@code getMenuActions()} the list of available menu actions can be
 * obtained and modified. This is the way to add individual items to the 
 * popup menu of this component. The F1 key is assigned to a "Help" action
 * with command-key "menu.help"; this class does not define such an action.
 */

public class AmpleTextArea extends JTextArea implements MenuActivist {
   private static final int DEFAULT_EDIT_AGGLO_TIME = 800; 
   private static HashMap<Object, Action> actionLookup;
   private static Timer timer = new Timer();
   
   protected ActionListener actions = new ATA_Action();
   private List<Action> menuActions;
   private ATA_UndoManager undoManager = new ATA_UndoManager();
   private PopupListener popupListener = new PopupListener();
   private Executor executor;
   
   private Window owner;
   private Dimension selection;
   private boolean isPopupActive = true;
   private boolean modified;

   /** Creates a AmpleTextArea with the given component name.
    * 
    * @param name String component name
    */
	public AmpleTextArea ( String name ) {
	   super();
	   init( name );
	}
	
	public AmpleTextArea ( String name, int rows, int columns ) {
	   super( rows, columns );
	   init( name );
	}
	
    /** Creates a AmpleTextArea with the given component name and
     * initial field text. 
     * 
     * @param name String component name
     * @param text String initial text
     */
	public AmpleTextArea ( String name, String text ) {
	   super( text );
	   init( name );
	}
	
	public AmpleTextArea ( String name, String text, int rows, int columns ) {
	   super( text, rows, columns );
	   init( name );
	}
	
	public AmpleTextArea ( String name, Document doc ) {
	   super( doc );
	   init( name );
	}
	
	public AmpleTextArea ( String name, Document doc, String text, 
			               int rows, int columns ) {
	   super( doc, text, rows, columns );
	   init( name );
	}
	
	private void init ( String name ) {
	   if ( actionLookup == null ) {
	      // put default actions in a Hashtable so we can retrieve them w/ Action.NAME
	      actionLookup = new HashMap<Object, Action>();
	      for (Action a : getActions()) {
	        actionLookup.put(a.getValue(Action.NAME), a);
//	        System.out.println( "-- TextArea Action: " + a.getValue(Action.NAME) );
	      }
	   }
	
	   setName( name );
	   addMouseListener( popupListener );
	   getDocument().addUndoableEditListener( undoManager );
	   getDocument().addDocumentListener(new DocListener());
	   modifyKeystrokes ();
	   
	   addFocusListener(new FocusAdapter() {

		   @Override
		   public void focusGained(FocusEvent e) {
			   if (selection == null) return;
			
			   String hs = "(AmplTextArea.focusGained) selected range: " + selection.width + 
					   " -- " + selection.height;
			   System.out.println("-- " + hs);
			   Log.debug(1, hs);

			   // perform a one-time text selection if returning to focus
			   select(selection.width, selection.height);
			   selection = null;
		   }
	   });
	
//	   setLineWrap( Options.isOptionSet( "editLineWrap" ) );
	   setWrapStyleWord( true );
//	   setFont( DisplayManager.getFont( "notes" ) );   
	   Log.debug(10, "(AmpleTextArea.init) text font = " + getFont());
	}
	
	private void modifyKeystrokes () {
	   Keymap parent, map;
	   Action action;
	   KeyStroke key;
	   
	   // create the keymap specific to this text area
	   parent = getKeymap();
	   map = JTextComponent.addKeymap( "AmpleTextArea_Keymap", parent );
	   
	   // add CTRL-W: select current word 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_W, InputEvent.CTRL_MASK );
	   action = actionLookup.get( DefaultEditorKit.selectWordAction );
	   map.addActionForKeyStroke(key, action);
	   
	   // add CTRL-L: select current line 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_L, InputEvent.CTRL_MASK );
	   action = actionLookup.get( DefaultEditorKit.selectLineAction );
	   map.addActionForKeyStroke(key, action);
	   
	   // add CTRL-P: select current paragraph 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_P, InputEvent.CTRL_MASK );
	   action = actionLookup.get( DefaultEditorKit.selectParagraphAction );
	   map.addActionForKeyStroke(key, action);
	   
	   // add CTRL-Z: undo action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_Z, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, new UndoAction() );
	   
	   // add SHIFT-CTRL-Z: redo action 
	   Action redoAction = new RedoAction();
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_Z, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, redoAction );
	      
	   // add CTRL-Y: redo action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_Y, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, redoAction );
	      
	   // add CTRL-D: insert current date (local) action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_D, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key,  new ATA_Action( "keystroke.CTRL-D" ));
	
	   // add CTRL-T: insert current time (local) action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_T, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key,  new ATA_Action( "keystroke.CTRL-T" ));
	
	   // add CTRL-U: insert current date+time (UT) action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_U, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, new ATA_Action( "keystroke.CTRL-U" ));
	
	   // add CTRL-U: insert current date+time (UT) action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_F1, 0 );
	   map.addActionForKeyStroke(key, new ATA_Action( "menu.help"));
	
	   // add CTRL-PLUS: use larger font action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_PLUS, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, new ATA_Action( "keystroke.CTRL-PLUS" ));
	
	   // add CTRL-MINUS: use larger font action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_MINUS, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, new ATA_Action( "keystroke.CTRL-MINUS" ));
	
	   // activate keymap for this text area
	   setKeymap( map );
	}  // modifyKeystrokes
	
	/** Removes all entries from the undo-manager. */
	public void clearUndoList () {
	   undoManager.discardAllEdits();
	}
	
	public UndoManager getUndoManager () {return undoManager;}
	
	/** Associates this editor with a text document. Additional to
	 * {@code JTextDocument} this version of the method extracts and takes
	 * over the first {@code UndoManager} it finds in the list of 
	 * {@code UndoableEditListeners} of the document. 
	 *
	 * @param doc {@code Document}
	 */
	@Override
	public void setDocument (Document doc) {
		ATA_UndoManager undoMan = null;
		
		// check whether there is an undo-manager in the document
		// we prefer to take this if present
		if (doc instanceof AbstractDocument) {
			AbstractDocument adc = (AbstractDocument) doc;
			for (UndoableEditListener eli : adc.getUndoableEditListeners()) {
				if (eli instanceof UndoManager) {
					// take over undo-manager from argument 
					undoMan = (ATA_UndoManager) eli;
					break;
				}
			}
		}

		// create and add a new undo-manager if not present
		if (undoMan == null && doc != null) {
			undoMan = new ATA_UndoManager();
			doc.addUndoableEditListener(undoMan);
		}
		undoManager = undoMan;
		
		super.setDocument(doc);
	}

	@Override
	public void select (int selectionStart, int selectionEnd) {
		super.select(selectionStart, selectionEnd);
		int start = super.getSelectionStart();
		int end = super.getSelectionEnd();
		selection = hasFocus() || start == end ? null : 
					new Dimension(super.getSelectionStart(), super.getSelectionEnd());
	}

	/** Whether the document has been modified since start or last call to
	 * <code>resetModified()</code>.
	 * 
	 * @return boolean true == modified
	 */
	public boolean isModified () {
		return modified;
	}
	
	/** Resets the MODIFIED status of the document to <b>false</b>.
	 */
	public void resetModified () {
		modified = false;
	}
	
	/**
	 * Sets the feature for popup menu active or inactive. (Default value is 
	 * <b>true</b>.) If the popup is active then a popup menu can be called by
	 * triggering the right mouse button within this component's display area. 
	 * 
	 * @param v boolean <b>true</b> == popup active
	 */
	public void setPopupEnabled ( boolean v ) {
	   isPopupActive = v;
	}
	
	/** Sets the time for which a text input will be agglomerated with the next 
	 * input for a single undo/redo action. There is a minimum of 0 and a 
	 * maximum of 5000. Value zero switches off agglomeration.
	 *  
	 * @param time int milliseconds
	 */
	public void setUndoAggloTime (int time) {
		if (undoManager != null) {
			undoManager.aggloTime = Math.min(0, Math.max(5000, time));
		}
	}
	
	/**
	 * Whether the popup menu feature is enabled for this text area.
	 *  
	 * @return boolean true == popup enabled 
	 */
	public boolean isPopupEnabled () {
	   return isPopupActive;
	}
	
	/** Returns the text of the line where the caret is currently positioned.
	 * 
	 *  @return String line text or <b>null</b> if unavailable 
	 */
	public String getCurrentLine () {
	   String text;
	   int cp, line, offs;
	   
	   cp = getCaretPosition();
	   try { 
	      line = getLineOfOffset(cp); 
	      offs = getLineStartOffset(line);
	      text = getText( offs, getLineEndOffset(line) - offs );
	      return text;
	   } 
	   catch (BadLocationException e) {
	      e.printStackTrace();
	      return null;
	   }
	}
	
	/** Renders the valid text range for operations that assume the 
	 * entire field if nothing is selected, but the user selection otherwise.
	 * 
	 * @return Dimension with width = start position, height = end position
	 */
	public Dimension getOperationSelection () {
	   int start, end;
	   Document doc = getDocument();
	   String selText = getSelectedText();
	   if ( selText != null ) {
	      start = getSelectionStart();
	      end = getSelectionEnd();
	   } else {
	      start = 0;
	      end = doc.getLength();
	   }
	   return new Dimension( start, end );
	}
	
	/** Renders the user selected text range for operations if there is a 
	 * selection or null otherwise. The dim-values are text positions.
	 * 
	 * @return Dimension (width = start, height = end) or null
	 */
	public Dimension getUserSelection () {
       int start = getSelectionStart();
       int end = getSelectionEnd();
       return start == end ? null : new Dimension(start, end);
	}
	
	/** Returns an international text for a given token.
	 * Currently we serve English.
	 * 
	 * @param token String symbolic name for the text
	 * @return String text
	 */
	protected String getIntl (String token ) {
		Objects.requireNonNull(token);
		String h = "* " + token;
		
		if (token.equals( ActionNames.PASTE )) {
			h = "Paste";
		} else if (token.equals( ActionNames.COPY )) {
			h = "Copy";
		} else if (token.equals( ActionNames.CUT )) {
			h = "Cut";
		} else if (token.equals( ActionNames.DELETE )) {
			h = "Delete";
		} else if (token.equals( ActionNames.LINE_WRAP )) {
			h = "Line Wrap";
		} else if (token.equals( ActionNames.SELECT_ALL )) {
			h = "Select All";
		} else if (token.equals( ActionNames.PRINT )) {
			h = "Print";
		} else if (token.equals( "msg.ask.longlineswrap" )) {
			h = "Apply Line-Wrap to improve rendering (recommended)?";
		} else if (token.equals( "msg.fail.noexecutor" )) {
			h = "There is no executor for the Print task!";
		}
		
		return h;
	}
	
	/** Returns a list with edit actions available in this editor for the 
	 * purpose of creating a menu or a popup menu. The elements of the list are 
	 * stable during a program session, i.e. they can be persistently modified. 
	 * Also the list itself can be modified.
	 * <p>The list contains at least the actions of the standard action names 
	 * defined in this interface with Undo and Redo at the leading places. (Undo
	 * and Redo are fix elements even when there is nothing to undo or redo.)
	 * 
	 * @return {@code List<Action>}
	 */
	@Override
	public List<Action> getMenuActions () {
		if (menuActions == null) {
			createMenuActions();
		}
		return menuActions;
	}
	
	/** Returns the editor action of the given action command token or null
	 * if such an action is not defined.
	 * 
	 * @param cmd String command token
	 * @return {@code Action} or null if not found
	 */
	@Override
	public Action getActionByCommand (String cmd) {
		Objects.requireNonNull(cmd);
		for (Action a : getMenuActions()) {
			if (cmd.equals(a.getValue(Action.ACTION_COMMAND_KEY))) return a;
		}
		return null;
	}
	
	/** Returns the editor action with the given command token from the 
	 * given action list or null if such an action is not defined.
	 * The list is reduced by the action returned.
	 *
	 * @param list {@code List<Action>}
	 * @param cmd String command token
	 * @return {@code Action} or null if not found
	 */
	private Action extractActionFromList (List<Action> list, String cmd) {
		Objects.requireNonNull(cmd);
		for (Action a : list) {
			if (cmd.equals(a.getValue(Action.ACTION_COMMAND_KEY))) {
				list.remove(a);
				return a;
			}
		}
		return null;
	}
	
	private void createMenuActions () {
		menuActions = new ArrayList<Action>();
		menuActions.add(new UndoAction());
		menuActions.add(new RedoAction());
		
		menuActions.add(new ATA_Action(ActionNames.CUT));
		menuActions.add(new ATA_Action(ActionNames.COPY));
		menuActions.add(new ATA_Action(ActionNames.PASTE));
		menuActions.add(new ATA_Action(ActionNames.DELETE));
		
		menuActions.add(new ATA_Action(ActionNames.LINE_WRAP));
		menuActions.add(new ATA_Action(ActionNames.PRINT));
		menuActions.add(new ATA_Action(ActionNames.SELECT_ALL));
	}
	
	@Override
	public JMenu getJMenu() {
	   JMenu menu = new JMenu();
	   JMenuItem item;
	
	   if ( undoManager.canUndo() & isEditable()) {
	      item = new JMenuItem( new UndoAction() );
	      item.setAccelerator( KeyStroke.getKeyStroke(
	            KeyEvent.VK_Z, ActionEvent.CTRL_MASK) );
	      menu.add( item );
	   }
	   
	   if ( undoManager.canRedo()  & isEditable()) {
	      item = new JMenuItem( new RedoAction() );
	      item.setAccelerator( KeyStroke.getKeyStroke(
	            KeyEvent.VK_Y, ActionEvent.CTRL_MASK) );
	      menu.add( item );
	   }
	
	   int menuSize = menu.getMenuComponentCount();
	   if ( menuSize > 0) {
	      menu.addSeparator();
	   }
	   
	   List<Action> alist = new ArrayList<Action>(getMenuActions());
	   extractActionFromList(alist, ActionNames.UNDO);
	   extractActionFromList(alist, ActionNames.REDO);
	   
	   // the standard CUT action (clipboard)
	   Action act = extractActionFromList(alist, ActionNames.CUT);
	   if (act != null & isEditable()) {
		   menu.add(act);
	   }
	
	   // the COPY action (clipboard)
	   act = extractActionFromList(alist, ActionNames.COPY);
	   if (act != null) {
		   menu.add(act);
	   }
	   
	   // the standard PASTE action (clipboard)
	   act = extractActionFromList(alist, ActionNames.PASTE);
	   if (act != null & isEditable()) {
		   menu.add(act);
	   }
	
	   // erases a text selection if present, otherwise the entire field 
	   act = extractActionFromList(alist, ActionNames.DELETE);
	   if (act != null & isEditable()) {
		   menu.add(act);
	   }
	
	   if (menu.getMenuComponentCount() > menuSize) {
		   menu.addSeparator();
		   menuSize = menu.getMenuComponentCount();
	   }
	
	   // line wrapping option
	   act = extractActionFromList(alist, ActionNames.LINE_WRAP);
	   if (act != null) {
		   item = new JCheckBoxMenuItem(act);
		   item.setSelected(getLineWrap());
		   menu.add( item );
	   }
	
	   // printing the text
	   act = extractActionFromList(alist, ActionNames.PRINT);
	   if (act != null && executor != null) {
		   item = makeMenuItem( ActionNames.PRINT );
		   menu.add( item );
	   }
	   
	   act = extractActionFromList(alist, ActionNames.SELECT_ALL);
	   if (act != null) {
		   menu.add(act);
	   }
	   
	   // user defined actions
	   if (!alist.isEmpty()) {
		   if (menu.getMenuComponentCount() > menuSize) {
			   menu.addSeparator();
		   }
		   for (Action a : alist) {
			   menu.add(a);
		   }
	   }
	   return menu;
	}

	/**
	 * Renders a popup menu for the context of this text area
	 * including current options of the UNDO manager.
	 * 
	 * @return <code>JPopupMenu</code>
	 */
	@Override
	public JPopupMenu getPopupMenu () {
		JMenu menu = getJMenu();
		return menu.getPopupMenu();
	}
	
	public void setDialogOwner (Window owner) {
	   this.owner = owner; 
	}
	
	/** Creates a new menu item which executes in this class'es 
	 * <code>ATA_Action</code>. The name of the item is drawn from <code>
	 * ResourceLoader.getCommand(token)</code> and the action
	 * command == token. Optional a keyboard mnemonic can be set.
	 * 
	 * @param token String key expression for the rendered menu item
	 * @param mnemonic int optional key value
	 * @return <code>JMenuItem</code> or <b>null</b> if <code>token</code> 
	 *         was <b>null</b> or empty 
	 */
	protected JMenuItem makeMenuItem ( String token, int mnemonic )	{
	   if (token == null || token.isEmpty()) return null;
	   
	   JMenuItem item = new JMenuItem(getIntl(token), mnemonic);
	   item.setActionCommand(token);
	   item.addActionListener(actions);
	   return item;
	}
	
	/** Creates a new menu item which executes in this class'es 
	 * <code>ATA_Action</code>. The name of the item is drawn from <code>
	 * ResourceLoader.getCommand(token)</code> and the action
	 * command == token. 
	 * 
	 * @param token String key expression for the rendered menu item
	 * @return <code>JMenuItem</code> or <b>null</b> if <code>token</code> 
	 *         was <b>null</b> or empty 
	 */
	protected JMenuItem makeMenuItem( String token ) {
	   return makeMenuItem(token, 0);
	}
	
	/** Sets the executor for tasks in separate threads for this element. 
	 * 
	 * @param e {@code Executor}
	 */
	public void setExecutor (Executor e) {
		executor = e;
	}
	
	public void startPrinting () {
	   String hstr, name;
	   int length;
	   if ( executor == null ) {
		   hstr = getIntl("msg.fail.noexecutor");
		   GUIService.failureMessage(hstr);
		   return;
	   }
	   
	   // set Attribute "LineWrap" to user option
	   boolean hasLongLine = false;
	   if ( !getLineWrap() ) {
	      // check for cut lines
	      int lines = getLineCount();
	      for ( int i = 0; i < lines; i++ ) {
	         try { length = getLineEndOffset(i) - getLineStartOffset(i); }
	         catch ( BadLocationException e1 ) 
	         { length = 0; }
	         hasLongLine |= length > 80;
	         if ( hasLongLine )
	            break;
	      }
	      
	      // ask user for line wrapping if we have a long line
	      name = getName() == null ? "?" : getName();
	      hstr = getIntl( "msg.ask.longlineswrap" );
	      hstr = Util.substituteText( hstr, "$name", name );
	      if ( hasLongLine && GUIService.userConfirm( owner, hstr ) ) {
	         setLineWrap( true );
	      }
	   }
	
	   // start a thread with the print job
       executor.execute( new PrintJob() );
	}

	
	//  *****************  inner classes  ****************
	
	private class ATA_Action extends AbstractAction implements ActionListener {
	   String command;
	   
	   public ATA_Action () {}
	   
	   /** Creates a new ATA-Action w/ the given action command.
	    * 
	    * @param command String action command
	    * @throws IllegalArgumentException if command is null or empty
	    */
	   public ATA_Action (String command) {
	      if (command == null || command.isEmpty())
	         throw new IllegalArgumentException("null or empty ACTION COMMAND");
	      
	      this.putValue(ACTION_COMMAND_KEY, command);
          this.putValue(NAME, getIntl(command));
          this.command = command;
	   }
	
	   /** Creates a new ATA-Action w/ the given action command and presentation
	    * name.
	    * 
	    * @param command String action command
	    * @param name String presentation name of the command (in menus, 
	    *             buttons, etc.)
	    * @throws IllegalArgumentException if command is null or empty
	    */
	   @SuppressWarnings("unused")
	   public ATA_Action (String command, String name) {
	      this(command);
	      if (name != null)
	         this.putValue(NAME, name);
	   }
	
	//   public ATA_Action ( String command, String name, int mnemonic )
	//   {
	//      this( command, name );
	//      if ( mnemonic > 0 )
	//         this.putValue( MNEMONIC_KEY, mnemonic );
	//   }
	   
//	   /** The command defined on this action or null if none was defined.
//	    * 
//	    * @return String or null
//	    */
//	   public String getCommand () {return command;}
	
	   @Override
	   public void actionPerformed (ActionEvent e)  {
	      String cmd = command == null ? e.getActionCommand() : command;
	      if (cmd == null) return;

	      try {
	      if (cmd.equals( ActionNames.LINE_WRAP )) {
	         setLineWrap(!getLineWrap());
	
	      } else if (cmd.equals( ActionNames.DELETE )) {
	         try {
	            Dimension adr = getUserSelection();
	            if (adr != null & isEditable()) {
	            	AmpleTextArea.this.getDocument().remove(adr.width, adr.height-adr.width);
		     	    selection = null;
	            }
	         } catch ( BadLocationException e1 ) { 
	        	 e1.printStackTrace(); 
	         }
	
	      } else if (cmd.equals( ActionNames.COPY )) {
	            Dimension adr = getOperationSelection();
	            setSelectionStart(adr.width);
	            setSelectionEnd(adr.height);
	     	    actionLookup.get( DefaultEditorKit.copyAction ).actionPerformed(null);

	      } else if ( cmd.equals( ActionNames.CUT ) ) {
	     	    actionLookup.get( DefaultEditorKit.cutAction ).actionPerformed(null);
	     	    selection = null;
	     	    
	      } else if ( cmd.equals( ActionNames.PASTE ) ) {
	     	    actionLookup.get( DefaultEditorKit.pasteAction ).actionPerformed(null);
	     	    
	      } else if ( cmd.equals( ActionNames.SELECT_ALL ) ) {
	     	    actionLookup.get( DefaultEditorKit.selectAllAction).actionPerformed(null);
	     	    
	      } else if ( cmd.equals( ActionNames.PRINT ) ) {
	         startPrinting();

	      } else if ( cmd.equals( "keystroke.CTRL-U" ) ) {
	         String dtext = Util.standardTimeString( System.currentTimeMillis(),
	               TimeZone.getTimeZone( "UTC" )).concat( " UT " );
	         AmpleTextArea.this.insert( dtext, AmpleTextArea.this.getCaretPosition() );

	      } else if ( cmd.equals( "keystroke.CTRL-D" ) ) {
	         String dtext = Util.standardTimeString( System.currentTimeMillis() );
	         dtext = dtext.substring( 0, 11 );
	         AmpleTextArea.this.insert( dtext, AmpleTextArea.this.getCaretPosition() );

	      } else if ( cmd.equals( "keystroke.CTRL-T" ) ) {
	         String dtext = Util.standardTimeString( System.currentTimeMillis() );
	         dtext = dtext.substring( 11 ).concat( " " );
	         AmpleTextArea.this.insert( dtext, AmpleTextArea.this.getCaretPosition() );

	      } else if ( cmd.equals( "keystroke.CTRL-PLUS" ) ) {
	    	  Font font = AmpleTextArea.this.getFont();
	    	  AmpleTextArea.this.setFont( font.deriveFont(font.getSize2D() + 1) );

	      } else if ( cmd.equals( "keystroke.CTRL-MINUS" ) ) {
	    	  Font font = AmpleTextArea.this.getFont();
	    	  AmpleTextArea.this.setFont( font.deriveFont( Math.max(4, font.getSize2D() - 1)) );
	      }

	   // uncaught exception during any command (protects the caller) 
	   } catch ( Exception e1 ) {
	      e1.printStackTrace();
	      GUIService.failureMessage( owner, "Unable to excute command: ".concat( cmd ), e1 );
	   }
	   }

	   @Override
	   public int hashCode() {
		   return command == null ? super.hashCode() : command.hashCode();
	   }

	   @Override
	   public boolean equals (Object obj) {
		   if (obj == null || !(obj instanceof ATA_Action)) return false;
		   ATA_Action oa = (ATA_Action)obj;
		   return command == null ? this == oa : command.equals(oa.command);
	   }

	   @Override
	   public String toString() {
		   return "ATA-Action " + command;
	   }
	}  // ATA_Action
	
	
	private class UndoAction extends TextAction	{

		public UndoAction () {
	      super( undoManager.getUndoPresentationName() );
	      putValue(ACTION_COMMAND_KEY, ActionNames.UNDO);
	   }
	
		@Override
		public void actionPerformed ( ActionEvent e ) {
	       if ( undoManager.canUndo() ) {
	          undoManager.undo();
	       }
	    }
	}
	
	private class RedoAction extends TextAction	{
		
	   public RedoAction () {
	      super( undoManager.getRedoPresentationName() );
	      putValue(ACTION_COMMAND_KEY, ActionNames.REDO);
	   }
	
	   @Override
	   public void actionPerformed ( ActionEvent e ) {
	      if ( undoManager.canRedo() )
	         undoManager.redo();
	   }
	}

	private class DocListener implements DocumentListener {
	
		@Override
		public void changedUpdate(DocumentEvent arg0) {
			modified = true;
		}
	
		@Override
		public void insertUpdate(DocumentEvent e) {
			modified = true;
		}
	
		@Override
		public void removeUpdate(DocumentEvent e) {
			modified = true;
		}
	}
	
	private class PopupListener extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
		   maybeShowPopup(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
		   maybeShowPopup(e);
		}
		
		private void maybeShowPopup(MouseEvent e) {
		    if (e.isPopupTrigger() && isPopupEnabled()) {
		 	   requestFocus();
		       getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
		    }
		}

	}  // PopupListener
	
	private class PrintJob implements Runnable {
	
	   @Override
	   public void run () {
	      try {
	         print();
	      } catch ( PrinterException e1 ) {
	         e1.printStackTrace();
	         GUIService.failureMessage( owner, "Unable to print the text!", e1 );
	      }
	   }
	}

	private class EditTimerTask extends TimerTask {
		CompoundEdit edit;
		
		EditTimerTask (CompoundEdit edit) {
			Objects.requireNonNull(edit);
			this.edit = edit;
		}
		
		@Override
		public void run() {
			edit.end();
//			Log.log(8, "(AmpleTextArea.EditTimerTask) ended compound edit, " + edit.getPresentationName());
		}
	}
	
	private class ATA_UndoManager extends UndoManager {
		private EditTimerTask editTimerTask;
		private long editTime;
	    /** wait time for a compound-edit to end its agglomeration phase (millisec) */
	    private int aggloTime = DEFAULT_EDIT_AGGLO_TIME;

		@Override
		public UndoableEdit editToBeUndone() {
			return super.editToBeUndone();
		}

		@Override
		public UndoableEdit editToBeRedone() {
			return super.editToBeRedone();
		}

		@Override
		public void undoableEditHappened (UndoableEditEvent e) {
			UndoableEdit edit = e.getEdit();
			if (aggloTime == 0) {
				undoManager.addEdit(edit);
				return;
			}
			
			CompoundEdit cEdit;
			long now = System.currentTimeMillis();
//			Log.log(8, "(AmpleTextArea.undoableEditHappened) undoable edit happened: " + edit.getPresentationName());
		
			UndoableEdit prevEdit = undoManager.editToBeUndone();
			if (now - editTime < aggloTime && prevEdit != null) {
//				Log.log(8, "(AmpleTextArea.undoableEditHappened) low time branch, previous = " + prevEdit.getPresentationName());
				if (!prevEdit.addEdit(edit)) {
					cEdit = new CompoundEdit();
					cEdit.addEdit(edit);
					undoManager.addEdit(cEdit);
//					Log.log(8, "(AmpleTextArea.undoableEditHappened) created new compound edit w/  " + edit.getPresentationName());
				} else {
					cEdit = (CompoundEdit) prevEdit;
//					Log.log(8, "(AmpleTextArea.undoableEditHappened) added edit to previous: " + edit.getPresentationName());
				}
		
				// cancel a previous end-edit task
				if (editTimerTask != null) {
					editTimerTask.cancel();
				}
				
			} else {
				cEdit = new CompoundEdit();
				cEdit.addEdit(edit);
				undoManager.addEdit(cEdit);
//				Log.log(8, "(AmpleTextArea.undoableEditHappened) high time branch, new compound w/ edit = " + edit.getPresentationName());
			}
			editTime = now;
		
			// initiate a task to end the compound edit
			editTimerTask = new EditTimerTask(cEdit);
			timer.schedule(editTimerTask, aggloTime);
		}
	}

	public static class ActionNames {
		public static final String UNDO = "menu.edit.undo";
		public static final String REDO = "menu.edit.redo"; 
		public static final String CUT = "menu.edit.cut";
		public static final String COPY = "menu.edit.copy";
		public static final String PASTE = "menu.edit.paste";
		public static final String DELETE = "menu.edit.delete"; 
		public static final String LINE_WRAP = "menu.edit.linewrap";
		public static final String PRINT = "menu.edit.print"; 
		public static final String SELECT_ALL = "menu.edit.selectall";
	}
	
//	/**
//		 * Renders a popup menu for the context of this text area
//		 * including actual options of the UNDO manager.
//		 * 
//		 * @return <code>JPopupMenu</code>
//		 */
//		public JPopupMenu getPopupMenu () {
//		   JMenuItem item;
//		   
//		   JPopupMenu menu = new JPopupMenu();
//	//	   // investigate current text selection or entire text line
//	//	   if ( (((hstr = getSelectedText()) != null || (hstr = getText()) != null))
//	//			 && hstr.length() < 100000 )
//	//	   {
//	//	      // investigate for browsing URLs (multiple occurrences enabled)
//	//	      if ( (urlArr = Util.extractURLs( hstr )) != null ) {
//	//	         if ( urlArr.length == 1 ) {
//	//	            // add browsing command if single url was found 
//	//	            item = menu.add( ActionHandler.getStartBrowserAction( urlArr[0] , false ));
//	//	            item.setForeground( MenuHandler.MENUITEM_MARKED_COLOR );
//	//	         } else {
//	//	            // create and add a submenu containing the URLs as item names
//	//	            subMenu = new JMenu( ResourceLoader.getCommand( "menu.edit.starturl" ) );
//	//	            subMenu.setForeground( MenuHandler.MENUITEM_MARKED_COLOR );
//	//	            menu.add( subMenu );
//	//	            for ( URL u : urlArr ) {
//	//	               item = subMenu.add( ActionHandler.getStartBrowserAction( u , true ));
//	//	               item.setForeground( MenuHandler.MENUITEM_MARKED_COLOR );
//	//	            }
//	//	         }
//	//	      }
//	//	      
//	//	      // investigate for EMAIL ADDRESSES (multiple occurrences enabled)
//	//	      if ( (addArr = Util.extractMailAddresses( hstr )) != null ) {
//	//	         if ( addArr.length == 1 ) {
//	//	            // add browsing command if single url was found 
//	//	            item = menu.add( ActionHandler.getStartEmailAction( addArr[0], false ) );
//	//	            item.setForeground( MenuHandler.MENUITEM_MARKED_COLOR );
//	//	         } else {
//	//	            // create and add a submenu containing the URLs as item names
//	//	            subMenu = new JMenu( ResourceLoader.getCommand( "menu.edit.startmail" ) );
//	//	            subMenu.setForeground( MenuHandler.MENUITEM_MARKED_COLOR );
//	//	            menu.add( subMenu );
//	//	            for ( String h : addArr ) {
//	//	               item = subMenu.add( ActionHandler.getStartEmailAction( h , true ));
//	//	               item.setForeground( MenuHandler.MENUITEM_MARKED_COLOR );
//	//	            }
//	//	         }
//	//	      }
//	//	   }
//		
//		   // TODO menu entry "Help"
//	//	   menu.addSeparator();
//	//	   item = makeMenuItem( "menu.help" );
//	//	   menu.add( item );
//		   return menu;
//		}
	
}
