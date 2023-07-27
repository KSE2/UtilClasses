package kse.utilclass2.gui;

/*
*  File: JMenuBarReader.java
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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import kse.utilclass.misc.Log;

/**
    * JMenuBarReader is a tool to automate creation of a JMenuBar by 
    * interpreting a formatted text file defining its contents. The reader 
    * essentially takes reference into ActionHandler to derive actions
    * for the menu items where possible.
    * 
    * <p><b>Format of the Input Text File</b>
    * <p>The input stream consists of lines.
    * <br>A line has x elements, separated by a TAB (\009) character.
    * <br>A line defines a JMenuItem if it has no successor or a successor
    * with an indentation index equal to its own.
    * <br>A line defines a JMenu if it has a successor with indentation index
    * larger than its own. 
    * <br>Lines with indentation index == 0 are never considered JMenuItems
    * but always JMenus.
    * 
    * <p><b>Elements of an Input Line</b>
    * <p>-- title, text displayed on item
    * <br>-- tech-name, reference used for identifying Action, Icon    
    * (.icon) and Tooltip (.tooltip)
    * <br>-- execution modus, synchronous/asynchronous ('syn', 'asyn ')
    * <br>-- key-code, code used for hot-key to trigger associated action
    * <br>-- enabled, boolean, initial item enabled state
    * <br>-- colour, colour name (Unix) or colour-code 
    * 
    * <p><b>Policy on ENABLED Property of Items and Menus</b>
    * <p>Resulting 'enabled' status of items is always false if the reader 
    * cannot find a corresponding Action in the ActionHandler of the program. 
    * If an action can be assigned, the 'enabled' status depends on two things,
    * the reader's DEFAULT ENABLED value and an optional, user-provided 
    * value on the corresponding text line. The user option in the text 
    * overrides the default value. A menu (JMenu) as item on the menu bar is 
    * analysed by the reader and is assigned enabled=true if at least one of 
    * its elements is enabled.  
    */
   
   public abstract class JMenuBarReader { 
      private BufferedReader reader;
      private Charset charset;
      private String currentLine;
      private int lineCount;
      private boolean setIcons;
      private boolean setTooltips;
      private boolean defaultEnabled = true;
      
      /** Creates a new JMenuReader with text input from the given file,
       * using the given Charset to decode the stream.
       * 
       * @param file File input file
       * @param cs Charset
       * @param icons boolean whether icons are to be set in items
       * @param tooltips boolean whether tooltips are to be set on items
       * @throws FileNotFoundException
       */
      public JMenuBarReader (File file, Charset cs, boolean icons, boolean tooltips) 
            throws FileNotFoundException {
         this( new FileInputStream(file), cs, icons, tooltips );
      }
      
      /** Creates a new JMenuReader with text input from the given file,
       * using the given Charset to decode the stream.
       * 
       * @param input <code>InputStream</code> input file
       * @param cs Charset
       * @param icons boolean whether icons are to be set in items
       * @param tooltips boolean whether tooltips are to be set on items
       */
      public JMenuBarReader (InputStream input, Charset cs, 
            boolean icons, boolean tooltips) {
         // enhance / control parameters
         charset = cs;
         if ( charset == null ) {
            charset = Charset.defaultCharset();
         }
         if ( input == null )
            throw new IllegalArgumentException("input was null");
         
         setIcons = icons;
         setTooltips = tooltips;
         reader = new BufferedReader(new InputStreamReader(input, charset));
      }
      
      /** Returns the special menu of the given name from a subclass.
       * The method in {@code JMenuBarReader} returns null.
       * <p>This is called when function code 'menu' is encountered in an item
       * definition line of the source text. The item is then replaced by the
       * special menu if this method returns a value not null.
       * 
       * @param name String
       * @return <code>JMenu</code> or null
       */
      public JMenu getSpecialMenu (String name) {
         return null;
      }

      /** Returns an {@code Action} for definition and performance of a menu
       * item.
       *  
       * @param name String technical name of the action/menu item
       * @param async boolean indicates whether the menu wishes to perform the
       *        action in a separate thread (non-EDT)
       * @return {@code Action} or null if not defined
       */
      public abstract Action getItemAction (String name, boolean async);
      
      public void setDefaultEnabled (boolean enabled) {
         defaultEnabled = enabled;
      }
      
      /** The Charset used in decoding the user given input stream.
       * 
       * @return <code>Charset</code>
       */
      public Charset getCharset () {
         return charset;
      }
      
      /** Reads the next text-line from the data source (input stream) and 
       * returns it without line-end marking characters.
       * <p>NOTE: The input stream gets closed when end of stream is reached.
       * 
       * @return String text line or null if EOF reached
       * @throws IOException
       */
      private String readNextLine () throws IOException {
         String line;
         do {
            currentLine = reader.readLine();
            if (currentLine == null) {
               reader.close();
               return null;
            }
            lineCount++;
            line = currentLine.trim();
         } while (line.isEmpty() || line.charAt(0) == '#');
         
         Log.debug(8, "(JMenuReader.readJMenu) read line " + lineCount +
               ": \"" + currentLine + "\"");
         return currentLine;
      }
      
      /** Splits a text line around '\t' character (tabulator) and returns the
       * received parts.
       * 
       * @param line String input text
       * @return {@code String[]}
       */
      private String[] splitLine (String line) {
         String[] arr = line.split("\t", 100);
         int i = 0;
         for (String s : arr) {
            arr[i++] = s.trim();
         }
         return arr;
      }
      
      /** Creates a JMenu reading the source text from the current line.
       * Expects to read JMenuItems
       * starting with the next read line until index-1 token position in the 
       * read-line is non-empty. 
       * On termination "currentLine" is the next line to interpret after the 
       * menu, or null if EOF is reached.
       * 
       * @param headline String the source text line before the current line
       * @param index int tree indent position of menu head (ranging N from 0)
       * @return JMenu or null if the end of input is reached
       * @throws IOException 
       */
      private JMenu readMenu (String headline, final int index) throws IOException {
         // split the parts of the line defining the menu head
         String[] arr = splitLine(headline);
         int elementLevel = index + 1;

         // create JMenu from contents of head-line
         JMenu menu = new JMenu();
         menu = (JMenu) defineMenuItem(menu, arr, index, lineCount-1);
         JMenuItem item = null;
         
         String predLine = headline;
         int predLevel = index;
         while (currentLine != null) {
            arr = splitLine(currentLine);
            int level = indentLevelOf(arr);
            if ( level > predLevel+1 ) {
               throw new IllegalInputFormatException("illegal indent distance at line " + lineCount);
            }
            
            // terminate condition: same or lower indent level as head-line
            if (level <= index) {
               break;
            }

            // recurse condition: create sub-menu as menu item
            else if (level > elementLevel) {
               menu.remove(item);
               item = readMenu(predLine, elementLevel);
               if (hasEnabledItems((JMenu)item)) {
                  item.setEnabled(true);
               }
               menu.add(item);
               continue;
            }

            // menu item condition: create regular menu item from current line
            else if (level == elementLevel) {
               // check for SEPARATOR symbols
               String name = arr[level]; 
               if ( name.startsWith("---") ) {
                  // add separator
                  menu.addSeparator();
               } else {
                  // create JMenuItem
                  item = new JMenuItem();
                  item = defineMenuItem(item, arr, elementLevel, lineCount);
                  menu.add(item);
               }
            }
            
            predLevel = level;
            predLine = currentLine;
            readNextLine();
         }
         
         return menu;
      }
      
      /** Whether the given JMenu has at least one first level element
       * which is enabled.
       *   
       * @param item <code>JMenu</code>
       * @return boolean true == menu has enabled item
       */
      private boolean hasEnabledItems (JMenu menu) {
         JMenuItem item;
         int count = menu.getItemCount();
         
         for (int i = 0; i < count; i++) {
            item = menu.getItem(i);
            if (item != null && item.isEnabled()) {
               return true;
            }
         }
         return false;
      }

      /** Defines the properties of the given {@code JMenuItem} from text 
       * elements given in an array. The text elements are ordered and their 
       * semantics depend on their index value.
       * 
       * @param item {@code JMenuItem}
       * @param arr String[] text input elements
       * @param elementLevel int 
       * @param line int the line number
       * @return {@code JMenuItem} the argument item
       * @throws IllegalInputFormatException
       */
      private JMenuItem defineMenuItem (JMenuItem item, String[] arr, 
            int elementLevel, int line) throws IllegalInputFormatException {
//         if ( arr.length < elementLevel + 1 )
//            throw new IllegalInputFormatException(
//            "no  for menu item, line " + line);
         
         int i = elementLevel;
         int len = arr.length;
         boolean async = false;
         boolean enabled = false;
         boolean enforceIcon = false;
         boolean enforceNoIcon = false;
         String option, hstr, optArr[];
         KeyStroke keyStroke = null;
         Action action = null;
         
         // text appearance of entry (minimum definition)
         String text = arr[ i ];
         item.setText(text);
         
         // interpret 2nd line value (tech-name)
         if ( len > elementLevel + 1 ) {
            String techName = arr[ i+1 ];
            if ( !techName.isEmpty() ) {
               
               // read tech-name
               String cmd = techName;
               enabled = defaultEnabled;

               // read typological settings
               if ( len > elementLevel + 2 ) {
                  hstr = arr[ i+2 ];
                  if (!hstr.isEmpty() ) {
                     optArr = hstr.split(",");
                     for (String opt : optArr) {
	                     if (!opt.isEmpty() ) {
	                        if ( opt.equals("syn") || opt.equals("synchronous") ) {
	                        } else if ( opt.equals("asyn") || opt.equals("asynchronous") ) {
	                           async = true;
	                        } else if ( opt.equals("icon") ) {
	                           enforceIcon = true;
	                        } else if ( opt.equals("noicon") ) {
	                           enforceNoIcon = true;
	                        } else if ( opt.equals("check") ) {
	                           // create a checkbox menu-item
	                           if ( !(item instanceof JMenu) ) {
	                              item = new JCheckBoxMenuItem();
	                           }
	                        } else if ( opt.equals("radio") ) {
	                           if ( !(item instanceof JMenu) ) {
	                              // create a radio-button menu-item
	                              item = new JRadioButtonMenuItem();
	                           }
	                        } else if ( opt.equals("menu") ) {
	                           JMenu special = getSpecialMenu(techName);
	                           if (special != null) {
	                              item = special;
	                              enabled = true;
	                           }
	                        } else {
	                           throw new IllegalInputFormatException(
	                                 "illegal EXECUTION MODUS ('syn'/'asyn') definition: "
	                                 .concat(opt));
	                        }
	                     }
                     }
                  }
               }

               // DEFINE MENU-ITEM
               try {
                  // try get ACTION for techName from subclass
            	  action = getItemAction(techName,  async);
            	  if (action != null) {
            		  // define item by action
	                  item.setAction(action);
	                  
	                  // undo action settings if opted in source
	                  if ((!setIcons | enforceNoIcon) & !enforceIcon) {
	                     item.setIcon(null);
	                  }
	                  if (!setTooltips) {
	                     item.setToolTipText(null);
	                  }
            	  }
               } finally {
                  // repeat set item title 
                  item.setText(text);
                  item.putClientProperty("MENUKEY", techName);
               }
   
               // read shortcut-key
               if ( len > elementLevel + 3 ) {
                  option = arr[ i+3 ];
                  if (!option.isEmpty() && !(item instanceof JMenu)) {
                     keyStroke = KeyStroke.getKeyStroke(option);
                     if ( keyStroke == null ) {
                        hstr = "** illegal SHORT-KEY declaration: ";
                        throw new IllegalInputFormatException(
                              hstr.concat(option));
                     }
                     item.setAccelerator(keyStroke);
                  }
               }
               
               // read initially enabled 
               if ( len > elementLevel + 4 ) {
                  option = arr[ i+4 ];
                  if ( !option.isEmpty() ) {
                     hstr = option.toLowerCase();
                     enabled = hstr.equals("true");
                     if ( !enabled && !hstr.equals("false") ) {
                        throw new IllegalInputFormatException(
                              "illegal ENABLED definition (boolean value): "
                              .concat(option));
                     }
                  }
               }
   
               // read text colour
               if ( len > elementLevel + 5 ) {
                  option = arr[ i+5 ];
                  if ( !option.isEmpty() ) {
                     Color color = colorValueOf(option);
                     if ( color == null ) {
                        throw new IllegalInputFormatException(
                              "illegal COLOUR value: ".concat(option));
                     }
                     item.setForeground(color);
                  }
               }
            }
         }

         // set our ENABLED property iff item not defined by external Action
         if (action == null) {
        	 item.setEnabled(enabled);
         }
         return item;
      }

      /** Attempts to decode an integer colour value given in the parameter.
       * Input may consist of an octal and hexadecimal number.
       * 
       * @param option String coded colour integer value (RGB)
       * @return <code>Color</code> value or null if faulty or null parameter
       */
      private Color colorValueOf (String option) {
         try {
            Color c = Color.decode(option);
            return c; 
         } catch (NumberFormatException e) {
         }
         return null;
      }

      /** Returns the index value of the first non-empty string in the given
       * array of strings. (Returns arr.length if all elements are empty.)
       * 
       * @param arr String[]
       * @return int index in arr
       */
      private int indentLevelOf (String[] arr) {
         int i;
         for (i=0; i<arr.length; i++) {
            if ( !arr[i].isEmpty() ) break;
         }
         return i;
      }

      public SkilledJMenuBar readMenu () throws IOException {
         
         
         // verify input text
         
    	  SkilledJMenuBar mbar = new SkilledJMenuBar();

         // read all lines in input stream
         readNextLine();
         boolean eof = false;
         while (!eof) {
            // read one line of text
            String line = currentLine;
            eof = line == null;
            if (eof) break;
            
            // split the parts of contained in the text line
            String[] arr = splitLine(line);
            
            // interpret first token
            int index = 0;
            final String title = arr[index]; 
            if ( !title.isEmpty() ) {
               // start a new menu on a non-empty first token
               // recursively read the menu including sub-menus from input
               readNextLine();
               JMenu menu = readMenu(line, 0);
               if ( hasEnabledItems(menu) ) {
                  menu.setEnabled(true);
               }
               mbar.add(menu);
            
            } else {
               // input format error
               throw new IllegalInputFormatException(
                     "illegal empty first element at line " + lineCount);
            }
            
         }
         
         return mbar;
      }
      
// ------------------------------------------------------
      
      public static class IllegalInputFormatException extends IOException {

         public IllegalInputFormatException() {
            super();
         }

         public IllegalInputFormatException(String message, Throwable cause) {
            super(message, cause);
         }

         public IllegalInputFormatException(String message) {
            super(message);
         }
      }
      
   }