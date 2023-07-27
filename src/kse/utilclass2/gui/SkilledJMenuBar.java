package kse.utilclass2.gui;

/*
*  File: SkilledJMenuBar.java
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import kse.utilclass.misc.Log;

/** Extension of {@code JMenuBar} which allows to identify (retrieve) menu items
 * by their technical names. The name of an item is defined via a property key 
 * named "MENUKEY" on the client-properties of {@code JMenuItem} objects.
 * 
 * <p>NOTE: An {@code SkilledJMenuBar} is created by class {@code JMenuBarReader}.
 * 
 * @author Wolfgang Keller
 */
// TODO : introduce ContainerListener and possibly change item analysis to cover all additions and removals

public class SkilledJMenuBar extends JMenuBar {
   private Map<String, JMenuItem> itemMap = new HashMap<>(48);
   
   public SkilledJMenuBar() {
   }

   /** Returns the menu item of the given name in the MENUKEY property of the 
    * item.
    * <p>NOTE: The property may have been assigned during the process of 
    * {@code JMenuBarReader.readMenu()}.
    * 
    * @param key String MENUKEY property name for menu item
    * @return <code>JMenuItem</code> or null if not found
    */
   public JMenuItem getMenuItem (String key) {
      JMenuItem item = itemMap.get(key);
      return item;
   }

   private void analyseMenu (JMenu menu, boolean adding) {
      int count = menu.getItemCount();
      for (int i = 0; i < count; i++) {
         JMenuItem item = menu.getItem(i);
         if ( item == null ) continue;
         if ( item instanceof JMenu ) {
            analyseMenu((JMenu)item, adding);
         } else {
            String key = (String)item.getClientProperty("MENUKEY");
            if (key != null) {
               if (adding) {
            	   itemMap.put(key, item);
            	   Log.debug(10, "(MightyMenuBar.analyseMenu) added item mapping: ".concat(key));
               } else {
            	   itemMap.remove(key);
            	   Log.debug(10, "(MightyMenuBar.analyseMenu) removed item mapping: ".concat(key));
               }
            }
         }
      }
   }
   
   @Override
   public JMenu add (JMenu c) {
      JMenu menu = super.add(c);
      analyseMenu(menu, true);
      return menu;
   }

   /** Removes a {@code JMenu} from this menu bar. This includes removal of
    * the associated MENUKEY bindings in this awareness menu bar.
    * 
    * @param menu {@code JMenu}
    */
   public void remove (JMenu menu) {
	  super.remove(menu);
      analyseMenu(menu, false);
   }

   /** The number of menu items accessible via property MENUKEY in this
    * {@code SkilledJMenuBar}.
    * 
    * @return int number of menu items
    */
   public int getItemCount () {
      return itemMap.size();
   }
   
   /** Iterator over all {@code JMenuItem} instances in this menu bar which own 
    * a MENUKEY property. 
    * 
    * @return {@code Iterator<JMenuItem>}
    */
   public Iterator<JMenuItem> itemIterator () {
      return itemMap.values().iterator();
   }
}
