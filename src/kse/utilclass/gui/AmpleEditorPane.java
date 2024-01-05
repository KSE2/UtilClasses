
package kse.utilclass.gui;

import java.awt.Color;

/*
*  File: AmpleTextArea.java
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
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
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TextAction;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import kse.utilclass.dialog.GUIService;
import kse.utilclass.lists.SortedArrayList;
import kse.utilclass.misc.Log;
import kse.utilclass.misc.Util;

/**
 * This extension of <code>JEditorPane</code> adds the following features to the
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
 * CTRL-T (current time), CTRL-U (universal date and time), CTRL-E (current
 * date localised), CTRL-F (current time localised)
 * CTRL-PLUS (increase font size), CTRL-MINUS (decrease font size). 
 * 
 * <p>
 * 
 * <p>With {@code getMenuActions()} the list of available menu actions can be
 * obtained and modified. This is the way to add individual items to the 
 * popup menu of this component. This class does not define a help action
 * but the user can add such an action with ACTION_COMMAND_KEY = 
 * ActionNames.HELP.
 */

public class AmpleEditorPane extends JEditorPane implements MenuActivist {

   /** Styled documents are shown either in their source code or as the
    * defined surface.
    */
   public enum StyledViewMode {SURFACE, SOURCE}
	
   protected static final int DEFAULT_EDIT_AGGLO_TIME = 1000; 
   private static final int[] FONT_SIZE_STEPS = new int[] {8, 10, 12, 14, 18, 24, 36, 48};
   private static HashMap<Object, Action> actionLookup;
   private static Timer timer = new Timer();
	
   
   protected ActionListener actions = new ATA_Action();
   private List<Action> menuActions;
   private ATA_UndoManager undoManager = new ATA_UndoManager();
   private PopupListener popupListener = new PopupListener();
   private Executor executor = new Executor() {
	   @Override
	   public void execute(Runnable r) {
		   r.run();
	   }
   };

   private Window owner;
//   private StyledViewMode viewMode = StyledViewMode.SURFACE; 
   private Dimension selection;
   private Color[] colorOptions;
   private boolean isPopupActive = true;
   private boolean modified;

   /** Creates an empty AmpleEditorPane with the given component name.
    * 
    * @param name String component name
    */
	public AmpleEditorPane (String name) {
	   super();
	   init( name );
	}
	
    /**
     * Creates a <code>AmpleEditorPane</code> based on a string containing
     * a URL specification.
     *
     * @param url URL of document
     * @exception IOException if the URL is <code>null</code> or cannot be 
     *            accessed
     */
    public AmpleEditorPane (String name, String url) throws IOException {
    	super(url);
    	init(name);
    }
    
    /** 
     * Creates a AmpleEditorPane with the given component name and initial text
     * of a specified type. This is a convenience constructor that calls the
     * <code>setContentType</code> and <code>setText</code> methods.
     * 
     * @param name String component name
     * @param type mime type of the given text
     * @param text String initial text
     */
	public AmpleEditorPane (String name, String type, String text) {
	   super(type, text);
	   init(name);
	}
	
    /**
     * Creates a <code>JEditorPane</code> based on a specified URL for input.
     *
     * @param name String component name
     * @param initialPage URL
     * @exception IOException if the URL is <code>null</code> or cannot be 
     *            accessed
     */
    public AmpleEditorPane (String name, URL initialPage) throws IOException {
    	super(initialPage);
    	init(name);
    }
    
	public AmpleEditorPane ( String name, Document doc ) {
	   super();
	   setDocument(doc);
	   init(name);
	}
	
	private void init ( String name ) {
	   // preset action lookup (static)
	   if ( actionLookup == null ) {
	      // put default actions in a Hashtable so we can retrieve them w/ Action.NAME
	      actionLookup = new HashMap<Object, Action>();
	      List<String> list = new SortedArrayList<>();
	      for (Action a : new HTMLEditorKit().getActions()) {
	    	    String hstr = (String) a.getValue(Action.NAME);
	    	    list.add(hstr);
		        actionLookup.put(hstr, a);
		  }
	      
	      // report
	      for (String s : list) {
		      System.out.println( "-- HTMLEditorKit Action: " + s );
	      }
	   }
	
	   setName(name);
	   setLocale(Locale.getDefault());
	   addMouseListener(popupListener);
	   getDocument().addUndoableEditListener(undoManager);
	   getDocument().addDocumentListener(new DocListener());
	   modifyKeystrokes ();
	   
	   addPropertyChangeListener( new PropertyChangeListener() {
		  @Override
		  public void propertyChange(PropertyChangeEvent evt) {
			  Log.debug(6, "(AmpleEditorPane) property change: " + evt.getPropertyName());
		  }
	   });
	   
	   addFocusListener(new FocusAdapter() {

		   @Override
		   public void focusGained(FocusEvent e) {
			   if (selection == null) return;
			
//			   String hs = "(AmplTextArea.focusGained) selected range: " + selection.width + 
//					   " -- " + selection.height;
//			   Log.debug(1, hs);

			   // perform a one-time text selection if returning to focus
			   select(selection.width, selection.height);
			   selection = null;
		   }
	   });
	
	   Log.debug(10, "(AmpleEditorPane.init) text font = " + getFont());
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
	      
	   // add CTRL-D: insert current date (local tm, univ expr) action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_D, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key,  new ATA_Action( "keystroke.CTRL-D" ));
	
	   // add CTRL-T: insert current time (local tm, univ expr) action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_T, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key,  new ATA_Action( "keystroke.CTRL-T" ));
	
	   // add CTRL-A: insert current date (local tm, local expr) action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_E, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key,  new ATA_Action( "keystroke.CTRL-E" ));
	
	   // add CTRL-B: insert current time (local tm, local expr) action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_F, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key,  new ATA_Action( "keystroke.CTRL-F" ));
	
	   // add CTRL-U: insert current date+time (UT) action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_G, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, new ATA_Action( "keystroke.CTRL-G" ));
	
	   // add CTRL-B: font style Bold action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_B, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, new ATA_Action( "keystroke.CTRL-B" ));
	
	   // add CTRL-U: font style Underlined action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_U, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, new ATA_Action( "keystroke.CTRL-U" ));
	
	   // add CTRL-I: font style Italic action 
	   key = KeyStroke.getKeyStroke( KeyEvent.VK_I, InputEvent.CTRL_MASK );
	   map.addActionForKeyStroke(key, new ATA_Action( "keystroke.CTRL-I" ));
	
//	   // add F1: Help - user supplied) action 
//	   key = KeyStroke.getKeyStroke( KeyEvent.VK_F1, 0 );
//	   map.addActionForKeyStroke(key, new ATA_Action( ActionNames.HELP ));
	
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

//	public void setStyledViewMode (StyledViewMode mode) {
//		Objects.requireNonNull(mode);
//   	 	Log.log(10, "(AmpleEditorPane) setting styled-view-mode = " + mode);
//		
//   	 	
//	}
//
//	public StyledViewMode getStyledViewMode () {return viewMode;}
//	
//	public void toggleStyledViewMode () {
//		StyledViewMode mode = viewMode == StyledViewMode.SOURCE ? 
//				StyledViewMode.SURFACE : StyledViewMode.SOURCE;
//		setStyledViewMode(mode);
//	}
	
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
	
	public boolean isHtmlType () {
		String hs = getContentType(); 
		return hs == null ? false : hs.indexOf("html") > -1;
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
	
//	/** Returns the text of the line where the caret is currently positioned.
//	 * 
//	 *  @return String line text or <b>null</b> if unavailable 
//	 */
//	public String getCurrentLine () {
//	   String text;
//	   int cp, line, offs;
//
//	   cp = getCaretPosition();
//	   try { 
//	      line = getLineOfOffset(cp); 
//	      offs = getLineStartOffset(line);
//	      text = getText( offs, getLineEndOffset(line) - offs );
//	      return text;
//	   } 
//	   catch (BadLocationException e) {
//	      e.printStackTrace();
//	      return null;
//	   }
//		return null;
//	}
	
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
	
	/** Returns the first component in the component hierarchy which is of
	 * type {@code Window}, i.e. the Dialog or the Frame containing this pane.
	 * 
	 * @return {@code Window}
	 */
	protected Window getOwner () {
		if (owner == null) {
			owner = GUIService.getAncestorWindow(this);
		}
		return owner;
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
		Locale locale = getLocale();
		
		if (Locale.GERMAN.getLanguage().equals(locale.getLanguage())) {
			if (token.equals( ActionNames.PASTE )) {
				h = "Einfügen";
			} else if (token.equals( ActionNames.COPY )) {
				h = "Kopieren";
			} else if (token.equals( ActionNames.CUT )) {
				h = "Ausschneiden";
			} else if (token.equals( ActionNames.DELETE )) {
				h = "Löschen";
			} else if (token.equals( ActionNames.BOLD )) {
				h = "Fett";
			} else if (token.equals( ActionNames.ITALIC )) {
				h = "Kursiv";
			} else if (token.equals( ActionNames.UNDERLINE )) {
				h = "Unterstrichen";
			} else if (token.equals( ActionNames.MONOSPACED )) {
				h = "Monospaced";
			} else if (token.equals( ActionNames.SERIF )) {
				h = "Serif";
			} else if (token.equals( ActionNames.SANS_SERIF )) {
				h = "Sans Serif";
			} else if (token.equals( ActionNames.FONT_FAMILY )) {
				h = "Familie";
			} else if (token.equals( ActionNames.FONT_COLOR )) {
				h = "Farbe";
			} else if (token.equals( ActionNames.FOREGROUND )) {
				h = "Vordergrund";
			} else if (token.equals( ActionNames.BACKGROUND )) {
				h = "Hintergrund";
			} else if (token.equals( ActionNames.INCREASE_FONT )) {
				h = "Vergrößern";
			} else if (token.equals( ActionNames.DECREASE_FONT )) {
				h = "Verkleinern";
			} else if (token.equals( ActionNames.SELECT_ALL )) {
				h = "Alles auswählen";
			} else if (token.equals( ActionNames.PRINT )) {
				h = "Drucken";
			} else if (token.equals( ActionNames.FONT )) {
				h = "Schrift";
			} else if (token.equals( ActionNames.HELP )) {
				h = "Hilfe";
			} else if (token.equals( "title.fontchooser" )) {
				h = "Schrift-Auswahl";
			} else if (token.equals( "msg.ask.longlineswrap" )) {
				h = "Zeilenumbruch anwenden für besseres Ergebnis? (empfohlen)";
			} else if (token.equals( "msg.fail.noexecutor" )) {
				h = "Es fehlt ein Executor für den Druckauftrag!";
			} else if (token.equals( "menu.style" )) {
				h = "Stil";
			}

		// English is default language
		} else {
			if (token.equals( ActionNames.PASTE )) {
				h = "Paste";
			} else if (token.equals( ActionNames.COPY )) {
				h = "Copy";
			} else if (token.equals( ActionNames.CUT )) {
				h = "Cut";
			} else if (token.equals( ActionNames.DELETE )) {
				h = "Delete";
			} else if (token.equals( ActionNames.BOLD )) {
				h = "Bold";
			} else if (token.equals( ActionNames.ITALIC )) {
				h = "Italic";
			} else if (token.equals( ActionNames.UNDERLINE )) {
				h = "Underline";
			} else if (token.equals( ActionNames.MONOSPACED )) {
				h = "Monospaced";
			} else if (token.equals( ActionNames.SERIF )) {
				h = "Serif";
			} else if (token.equals( ActionNames.SANS_SERIF )) {
				h = "Sans Serif";
			} else if (token.equals( ActionNames.FONT_FAMILY )) {
				h = "Family";
			} else if (token.equals( ActionNames.FONT_COLOR )) {
				h = "Color";
			} else if (token.equals( ActionNames.FOREGROUND )) {
				h = "Foreground";
			} else if (token.equals( ActionNames.BACKGROUND )) {
				h = "Background";
			} else if (token.equals( ActionNames.INCREASE_FONT )) {
				h = "Larger";
			} else if (token.equals( ActionNames.DECREASE_FONT )) {
				h = "Smaller";
//			} else if (token.equals( ActionNames.LINE_WRAP )) {
//				h = "Line Wrap";
			} else if (token.equals( ActionNames.SELECT_ALL )) {
				h = "Select All";
			} else if (token.equals( ActionNames.PRINT )) {
				h = "Print";
			} else if (token.equals( ActionNames.FONT )) {
				h = "Font";
			} else if (token.equals( ActionNames.HELP )) {
				h = "Help";
			} else if (token.equals( "title.fontchooser" )) {
				h = "Font Selection";
			} else if (token.equals( "msg.ask.longlineswrap" )) {
				h = "Apply Line-Wrap to improve rendering? (recommended)";
			} else if (token.equals( "msg.fail.noexecutor" )) {
				h = "There is no executor for the Print task!";
			} else if (token.equals( "menu.style" )) {
				h = "Style";
			}
		}
		
		return h;
	}
	
	/** Returns a list with edit actions available in this editor for the 
	 * purpose of creating a menu or a popup menu. The elements of the list are 
	 * stable during a program session, i.e. they can be persistently modified. 
	 * Also the list itself can be modified. This list does not represent set
	 * and order of the actions which appear in the popup menu, instead the 
	 * menu is created by internal rules; it forms the super-set of actions
	 * from which the menu is created.
	 * <p>The list contains at least the actions of the standard action names 
	 * defined in this interface, including Undo and Redo. All user added 
	 * actions are brought to display in the sequence of their index values.
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
	
//	private static class SizeRelatedName {
//		int size;
//		String name;
//		
//		SizeRelatedName (int size, String name) {
//			this.size = size;
//			this.name = name;
//		}
//	}
	
	private void setFontFamilyFunction () {
	   HTMLEditorKit kit = (HTMLEditorKit)getEditorKit();
	   MutableAttributeSet set = kit.getInputAttributes();
	   String name = StyleConstants.getFontFamily(set);
	   int size = StyleConstants.getFontSize(set);
	   Font font = Font.decode(name + "-" + size);
	   String hstr = font == null ? " -- font == null for " : ""; 
	   Log.debug(10, "(AmpleEditorPane) -- setting FONT FONT_FAMILY: " + hstr + name + " - " + size);

	   font = FontChooser.showDialog(getOwner(), getIntl("title.fontchooser"), font);
	   if (font != null) {
	   	  StyledEditorKit.FontFamilyAction a = new StyledEditorKit.FontFamilyAction("TEST", font.getFamily());
	      a.actionPerformed(null);
	   }
	}
	
	private void setFontColorFunction () {
	   HTMLEditorKit kit = (HTMLEditorKit)getEditorKit();
	   MutableAttributeSet set = kit.getInputAttributes();
	   Color color = StyleConstants.getForeground(set);

	   color = ColorChooserDialog.showDialog(getOwner(), null, colorOptions, color);
	   if (color != null) {
	   	  StyledEditorKit.ForegroundAction a = new StyledEditorKit.ForegroundAction("TEST", color);
	      a.actionPerformed(null);
	   }
	}
		
	public void increaseFont () {
	   if (isHtmlType()) {
//		   Dimension sel = getUserSelection();
//		   if (sel == null) return;

		   HTMLEditorKit kit = (HTMLEditorKit)getEditorKit();
		   MutableAttributeSet set = kit.getInputAttributes();
		   int size = StyleConstants.getFontSize(set);

		   // normalised next font size
		   int next = size;
		   for (int v : FONT_SIZE_STEPS) {
			   if (v > size) {
				   next = v;
				   break;
			   }
		   }
		   Log.debug(10, "(AmpleEditorPane.increaseFont) -- font size = " + size + ", next = " + next);

		   if (next > size) {
			   Action a = actionLookup.get( "font-size-" + next );
			   if (a != null) {
//			   Action a = new StyledEditorKit.FontSizeAction("TEST", next);
				   a.actionPerformed(new ActionEvent(this, 0, null));
			   }
		   }

	   } else {
		   // PLAIN-TEXT branch
		   Font font = getFont();
		   setFont( font.deriveFont(font.getSize2D() + 1) );
	   }
	}
	
	public void decreaseFont () {
	   if (isHtmlType()) {
//		   Dimension sel = getUserSelection();
//		   if (sel == null) return;

		   HTMLEditorKit kit = (HTMLEditorKit)getEditorKit();
		   MutableAttributeSet set = kit.getInputAttributes();
		   int size = StyleConstants.getFontSize(set);
		   Log.debug(10, "(AmpleEditorPane.decreaseFont) -- font size == " + size);

		   // normalised previous font size
		   int next = size;
		   for (int i = FONT_SIZE_STEPS.length; i > 0; i--) {
			   int v = FONT_SIZE_STEPS[i-1];
			   if (v < size) {
				   next = v;
				   break;
			   }
		   }
		   Log.debug(10, "(AmpleEditorPane.decreaseFont) -- font size = " + size + ", next = " + next);

		   if (next < size) {
			   Action a = actionLookup.get( "font-size-" + next );
			   if (a != null) {
				   a.actionPerformed(new ActionEvent(this, 0, null));
			   }
		   }
		   
	   } else {
		   // PLAIN-TEXT branch
		   Font font = getFont();
		   setFont( font.deriveFont(font.getSize2D() - 1) );
	   }
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
		menuActions.add(new ATA_Action(ActionNames.BOLD));
		menuActions.add(new ATA_Action(ActionNames.ITALIC));
		menuActions.add(new ATA_Action(ActionNames.UNDERLINE));
		menuActions.add(new ATA_Action(ActionNames.MONOSPACED));
		menuActions.add(new ATA_Action(ActionNames.SERIF));
		menuActions.add(new ATA_Action(ActionNames.SANS_SERIF));
		menuActions.add(new ATA_Action(ActionNames.FOREGROUND));
		menuActions.add(new ATA_Action(ActionNames.BACKGROUND));
		menuActions.add(new ATA_Action(ActionNames.FONT_FAMILY));
		menuActions.add(new ATA_Action(ActionNames.FONT_COLOR));
		menuActions.add(new ATA_Action(ActionNames.INCREASE_FONT));
		menuActions.add(new ATA_Action(ActionNames.DECREASE_FONT));
		
//		menuActions.add(new ATA_Action(ActionNames.LINE_WRAP));
		menuActions.add(new ATA_Action(ActionNames.PRINT));
		menuActions.add(new ATA_Action(ActionNames.FONT));
		menuActions.add(new ATA_Action(ActionNames.SELECT_ALL));
	}
	
	@Override
	public JMenu getJMenu() {
	   JMenu menu = new JMenu();
	   JMenuItem item;
	
	   List<Action> alist = new ArrayList<Action>(getMenuActions());
	   boolean canShowUndo = extractActionFromList(alist, ActionNames.UNDO) != null |
			                 extractActionFromList(alist, ActionNames.REDO) != null;
			   
	   if (canShowUndo) {
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
	   }
	
	   int menuSize = menu.getMenuComponentCount();
	   if ( menuSize > 0) {
	      menu.addSeparator();
	   }
	   
	   
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
	
	   // SUB-MENU "Style" (text formatting)
	   JMenu sub = new JMenu(getIntl("menu.style"));
	   act = extractActionFromList(alist, ActionNames.BOLD);
	   if (act != null & isEditable()) {
		   sub.add(act);
	   }
	   
	   act = extractActionFromList(alist, ActionNames.ITALIC);
	   if (act != null & isEditable()) {
		   sub.add(act);
	   }
	   
	   act = extractActionFromList(alist, ActionNames.UNDERLINE);
	   if (act != null & isEditable()) {
		   sub.add(act);
	   }

	   act = extractActionFromList(alist, ActionNames.MONOSPACED);
	   if (act != null & isEditable()) {
		   sub.addSeparator();
		   sub.add(act);
	   }

	   act = extractActionFromList(alist, ActionNames.SERIF);
	   if (act != null & isEditable()) {
		   sub.add(act);
	   }

	   act = extractActionFromList(alist, ActionNames.SANS_SERIF);
	   if (act != null & isEditable()) {
		   sub.add(act);
	   }

	   if (sub.getMenuComponentCount() > 0 && isHtmlType()) {
		   menu.addSeparator();
		   menu.add(sub);
	   }
	   
	   
	   // SUB-MENU "Font" (text formatting)
	   sub = new JMenu("Font");
	   act = extractActionFromList(alist, ActionNames.FONT_FAMILY);
	   if (act != null & isEditable()) {
		   sub.add(act);
	   }
	   
	   act = extractActionFromList(alist, ActionNames.FONT_COLOR);
	   if (act != null & isEditable() && colorOptions != null) {
		   sub.add(act);
	   }
	   
	   act = extractActionFromList(alist, ActionNames.INCREASE_FONT);
	   if (act != null & isEditable()) {
		   sub.add(act);
	   }
	   
	   act = extractActionFromList(alist, ActionNames.DECREASE_FONT);
	   if (act != null & isEditable()) {
		   sub.add(act);
	   }
	   
	   if (sub.getMenuComponentCount() > 0 && isHtmlType()) {
		   menu.add(sub);
	   }
	   
	   Action bgdAction = extractActionFromList(alist, ActionNames.BACKGROUND);
	   if (bgdAction != null & isEditable() && isHtmlType() && colorOptions != null) {
		   menu.add(bgdAction);
	   }
	   
//	   // line wrapping option
//	   act = extractActionFromList(alist, ActionNames.LINE_WRAP);
//	   if (act != null) {
//		   item = new JCheckBoxMenuItem(act);
//		   item.setSelected(getLineWrap());
//		   menu.add( item );
//	   }
	
	   if (menu.getMenuComponentCount() > menuSize) {
		   menu.addSeparator();
		   menuSize = menu.getMenuComponentCount();
	   }
	
	   // printing the text
	   act = extractActionFromList(alist, ActionNames.PRINT);
	   if (act != null) {
		   menu.add( act );
	   }
	   
	   // change font of editor (content type text/plain)
	   act = extractActionFromList(alist, ActionNames.FONT);
	   if (act != null && !isHtmlType()) {
		   menu.add( act );
	   }
	   
	   sub = new JMenu(getIntl(ActionNames.FONT_COLOR));
		
	   // editor foreground color
	   act = extractActionFromList(alist, ActionNames.FOREGROUND);
	   if (!isHtmlType() && act != null) {
		   sub.add( act );
	   }
	   
	   // editor background color
	   if (!isHtmlType() && bgdAction != null) {
		   sub.add( bgdAction );
	   }

	   if (sub.getMenuComponentCount() > 0) {
		   menu.add(sub);
	   }
	   
	   act = extractActionFromList(alist, ActionNames.SELECT_ALL);
	   if (act != null) {
		   menu.add(act);
	   }
	   
	   act = extractActionFromList(alist, ActionNames.HELP);
	   
	   // user defined actions
	   if (!alist.isEmpty()) {
		   if (menu.getMenuComponentCount() > menuSize) {
			   menu.addSeparator();
		   }
		   for (Action a : alist) {
			   menu.add(a);
		   }
	   }

	   // add at bottom: HELP action (user supplied)
	   if (act != null) {
		   menu.addSeparator();
		   menu.add(act);
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
	
//	/** Sets the owner for occasional message dialogs for the user.
//	 *  
//	 * @param owner Window
//	 */
//	public void setDialogOwner (Window owner) {
//	   this.owner = owner; 
//	}
	
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
	 * The executor is currently used only for the printing job. The default
	 * executor executes synchronously. 
	 * 
	 * @param e {@code Executor}
	 */
	public void setExecutor (Executor e) {
		Objects.requireNonNull(e);
		executor = e;
	}
	
	public void setColorOptions (Color[] c1) {
		colorOptions = c1;
	}
	
	public void startPrinting () {
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
	
//	   /** The command defined on this action or null if none was defined.
//	    * 
//	    * @return String or null
//	    */
//	   public String getCommand () {return command;}
	
	   @Override
	   public void actionPerformed (ActionEvent e)  {
	      String cmd = command == null ? e.getActionCommand() : command;
	      if (cmd == null) return;
	      boolean isHtml = isHtmlType();

	      try {
//	      if (cmd.equals( ActionNames.LINE_WRAP )) {
//	         setLineWrap(!getLineWrap());
//	
//	      } else 
	      if (cmd.equals( ActionNames.DELETE )) {
	         try {
	            Dimension adr = getUserSelection();
	            if (adr != null & isEditable()) {
	            	AmpleEditorPane.this.getDocument().remove(adr.width, adr.height-adr.width);
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
	     	    
	      } else if ( isHtml && cmd.equals( ActionNames.BOLD ) ) {
	     	    actionLookup.get( "font-bold" ).actionPerformed(null);
	     	    
	      } else if ( isHtml && cmd.equals( ActionNames.ITALIC ) ) {
	     	    actionLookup.get( "font-italic" ).actionPerformed(null);
	     	    
	      } else if ( isHtml && cmd.equals( ActionNames.UNDERLINE ) ) {
	     	    actionLookup.get( "font-underline" ).actionPerformed(null);
	     	    
	      } else if ( isHtml && cmd.equals( ActionNames.INCREASE_FONT ) ) {
	     	   increaseFont();
	     	    
	      } else if ( isHtml && cmd.equals( ActionNames.DECREASE_FONT ) ) {
	     	   decreaseFont();
	     	    
	      } else if ( isHtml && cmd.equals( ActionNames.MONOSPACED ) ) {
	     	    actionLookup.get( "font-family-Monospaced" ).actionPerformed(null);
	     	    
	      } else if ( isHtml && cmd.equals( ActionNames.SERIF ) ) {
	     	    actionLookup.get( "font-family-Serif" ).actionPerformed(null);
	     	    
	      } else if ( isHtml && cmd.equals( ActionNames.SANS_SERIF ) ) {
	     	    actionLookup.get( "font-family-SansSerif" ).actionPerformed(null);
	     	    
	      } else if ( isHtml && cmd.equals( ActionNames.FONT_FAMILY ) ) {
	    	  setFontFamilyFunction();
	    	  
	      } else if ( isHtml && cmd.equals( ActionNames.FONT_COLOR ) ) {
	    	  setFontColorFunction();
	    	  
	      } else if ( cmd.equals( ActionNames.BACKGROUND ) ) {
	    	  Color color = getBackground();
	   	   	  color = ColorChooserDialog.showDialog(getOwner(), null, colorOptions, color);
	   	   	  if (color != null) {
	   	   		  setBackground(color);
	   	   	  }
	    	  
	      } else if ( cmd.equals( ActionNames.FOREGROUND ) ) {
	   	   	  Color color = ColorChooserDialog.showDialog(getOwner(), null, colorOptions, getForeground());
	   	   	  if (color != null) {
	   	   		  setForeground(color);
	   	   	  }
	    	  
	      } else if ( cmd.equals( ActionNames.SELECT_ALL ) ) {
	     	  actionLookup.get( DefaultEditorKit.selectAllAction).actionPerformed(null);
	     	    
	      } else if ( cmd.equals( ActionNames.PRINT ) ) {
	    	  startPrinting();

	      } else if ( cmd.equals( ActionNames.FONT ) ) {
     		 Font font = FontChooser.showDialog(getOwner(), getIntl("title.fontchooser"), getFont());
     		 if (font != null) {
     			 setFont(font);
     		 }


//	      } else if ( cmd.equals( ActionNames.HELP ) ) {
//		     for (Action a : menuActions) {
//		    	 String acm = (String) a.getValue(ACTION_COMMAND_KEY);
//		    	 if (ActionNames.HELP.equals(acm)) {
//		    		 a.actionPerformed(e);
//		    		 return;
//		    	 }
//		     }

	      } else if ( isHtml && cmd.equals( "keystroke.CTRL-B" ) ) {
	     	    actionLookup.get( "font-bold" ).actionPerformed(null);

	      } else if ( isHtml && cmd.equals( "keystroke.CTRL-I" ) ) {
	     	    actionLookup.get( "font-italic" ).actionPerformed(null);

	      } else if ( isHtml && cmd.equals( "keystroke.CTRL-U" ) ) {
	     	    actionLookup.get( "font-underline" ).actionPerformed(null);

	      } else if ( cmd.equals( "keystroke.CTRL-G" ) ) {
		         String dtext = Util.standardTimeString( System.currentTimeMillis(),
		               TimeZone.getTimeZone( "UTC" )).concat( " UT " );
		         insert( dtext, getCaretPosition() );

	      } else if ( cmd.equals( "keystroke.CTRL-D" ) ) {
	         String dtext = Util.standardTimeString( System.currentTimeMillis() );
	         dtext = dtext.substring( 0, 11 );
	         insert( dtext, getCaretPosition() );

	      } else if ( cmd.equals( "keystroke.CTRL-T" ) ) {
	         String dtext = Util.standardTimeString( System.currentTimeMillis() );
	         dtext = dtext.substring( 11 ).concat( " " );
	         insert( dtext, getCaretPosition() );

	      } else if ( cmd.equals( "keystroke.CTRL-E" ) ) {
		     Date date = new Date( System.currentTimeMillis() );
		     String dtext = DateFormat.getDateInstance().format(date).concat( " " );
	         insert( dtext, getCaretPosition() );

	      } else if ( cmd.equals( "keystroke.CTRL-F" ) ) {
		     Date date = new Date( System.currentTimeMillis() );
		     String dtext = DateFormat.getTimeInstance().format(date).concat( " " );
	         insert( dtext, getCaretPosition() );

	      } else if ( cmd.equals( "keystroke.CTRL-PLUS" ) ) {
	    	  increaseFont();

	      } else if ( cmd.equals( "keystroke.CTRL-MINUS" ) ) {
	    	  decreaseFont();
	      }

	   // uncaught exception during any command (protects the caller) 
	   } catch ( Exception e1 ) {
	      e1.printStackTrace();
	      GUIService.failureMessage( AmpleEditorPane.this, "Unable to excute command: ".concat(cmd), e1 );
	   }
	   }

	   private void insert (String dtext, int caretPosition) {
		   // TODO Auto-generated method stub
		   try {
			   getDocument().insertString(caretPosition, dtext, null);
		   } catch (BadLocationException e) {
			   e.printStackTrace();
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
     	  Document doc = getDocument();
    	  String contentType = getContentType();
    	  Font font = getFont();
    	  JTextComponent textComp;
    	  String content;
    	  
	      try {
	    	  // we branch after content type into different auxiliary editors
	    	  if (contentType.indexOf("text/plain") > -1) {
	    		  // text-area for plain-text
	    		  JTextArea area = new JTextArea();
	              area.setLineWrap(true);
	              area.setWrapStyleWord(true);
	    		  textComp = area;
	    		  content = doc.getText(0, doc.getLength());
	    		  
	    	  } else {
		   	      // editor-pane for styled documents (we copy the raw text)
				  JEditorPane pane = new JEditorPane();
		     	  pane.setContentType(contentType);
		    	  textComp = pane;
		
		     	  EditorKit kit = JEditorPane.createEditorKitForContentType(contentType);
		     	  StringWriter writer = new StringWriter();
		     	  try {
					  kit.write(writer, doc, 0, doc.getLength());
				  } catch (IOException | BadLocationException e) {
					  e.printStackTrace();
				  }
		     	  content = writer.toString();
	    	  }
    	  
	    	  // print the auxiliary text component
	    	  textComp.setLocale(getLocale());
	    	  textComp.setBackground(Color.WHITE);
	    	  textComp.setFont(font.deriveFont(font.getSize2D()-3));
	    	  textComp.setText(content);
	    	  textComp.print();
	    	  
	      } catch ( Exception e1 ) {
	         e1.printStackTrace();
	         GUIService.failureMessage( AmpleEditorPane.this, "Unable to print the text!", e1 );
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
		public static final String BOLD = "menu.edit.bold"; 
		public static final String ITALIC = "menu.edit.italic"; 
		public static final String UNDERLINE = "menu.edit.underline"; 
		public static final String MONOSPACED = "menu.edit.monospaced"; 
		public static final String SERIF = "menu.edit.serif";
		public static final String SANS_SERIF = "menu.edit.sansserif";
		public static final String INCREASE_FONT = "menu.edit.font.increase";
		public static final String DECREASE_FONT = "menu.edit.font.decrease";
		public static final String FOREGROUND = "menu.edit.foreground";
		public static final String BACKGROUND = "menu.edit.background";
//		public static final String LINE_WRAP = "menu.edit.linewrap";
		public static final String FONT_FAMILY = "menu.edit.font.family";
		public static final String FONT_COLOR = "menu.edit.font.color"; 
		public static final String PRINT = "menu.edit.print"; 
		public static final String FONT = "menu.edit.font"; 
		public static final String SELECT_ALL = "menu.edit.selectall";
		public static final String HELP = "menu.edit.help"; 
	}

}
