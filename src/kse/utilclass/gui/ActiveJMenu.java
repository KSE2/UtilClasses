package kse.utilclass.gui;

/*
*  File: ActiveJMenu.java
* 
*  Project UtilClasses
*  @author Wolfgang Keller
*  Created 
* 
*  Copyright (c) 2023 by Wolfgang Keller, Munich, Germany
* 
This program is not public domain software but copyright protected to the 
author(s) stated above. However, you can use, redistribute and/or modify it 
under the terms of the The GNU General Public License (GPL) as published by
the Free Software Foundation, version 2.0 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the License along with this program; if not,
write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, 
Boston, MA 02111-1307, USA, or go to http://www.gnu.org/copyleft/gpl.html.
*/

import java.awt.Color;
import java.awt.Component;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import kse.utilclass.misc.Log;

/** This class can be used as super-class to any JMenu which loads its contents
 * dynamically when the button (JMenu) is activated. The sub-classes can 
 * render the current menu content in two different ways: as an Action array or
 * as another JMenu.
 */
public abstract class ActiveJMenu extends JMenu {

   private Color itemTextColor;
   private Color itemBgdColor;

	public ActiveJMenu () {
		init();
	}

	public ActiveJMenu (String s) {
		super(s);
		init();
	}

	public ActiveJMenu (Action a) {
		super(a);
		init();
	}

	private void init () {
		addMenuListener(new OurMenuListener());
		Log.log(5, "(ActiveJMenu.init) new ActiveJMenu: " + getText());
	}
	
	/** The subclass can render the ordered set of currently usable actions 
	 * of this menu.
	 * <p>NOTE: If {@code getCurrentMenu()} returns a value != null then this
	 * action array is ignored.
	 *  
	 * @return {@code Action[]}
	 */
	protected Action[] getCurrentActions () {return new Action[0];}
	
	/** The subclass can render the ordered set of currently usable menu items 
	 * of this menu in the containment of another {@code JMenu}. 
	 *  
	 * @return {@code JMenu}
	 */
	protected JMenu getCurrentMenu () {return null;}
	

    public Color getItemTextColor () {
	   return itemTextColor;
	}

    public void setItemTextColor (Color color) {
	   this.itemTextColor = color;
	}

    public Color getItemBgdColor () {
	   return itemBgdColor;
	}

    public void setItemBgdColor (Color color) {
	   this.itemBgdColor = color;
	}

    private class OurMenuListener implements MenuListener {

      @Override
      public void menuSelected (MenuEvent e) {
         // prepare generics
    	 JMenu menu = ActiveJMenu.this;
         JMenu cMenu = getCurrentMenu();
         Action[] actions = cMenu != null ? null : getCurrentActions();
         menu.removeAll();
//         Log.log(5, "(ActiveJMenu.menuSelected) menu selected, menu = " + (cMenu != null) 
//        		    + ", actions = " + (actions == null ? 0 : actions.length));
         
         // add menu elements from the menu
         if (cMenu != null) {
        	 for (Component c : cMenu.getMenuComponents()) {
        		 if (c instanceof JSeparator) {
        			 menu.addSeparator();
        		 } else if (c instanceof JMenuItem) {
        			 JMenuItem item = menu.add((JMenuItem)c);

        			 if (itemTextColor != null) {
                         item.setForeground(itemTextColor);
                     }
                     if (itemBgdColor != null) {
                        item.setBackground(itemBgdColor);
                     }
        		 }
        	 }

         // add menu elements from the Action array
         } else if (actions != null) {
        	 for (Action a : actions) {
        		 JMenuItem item = menu.add(a);

    			 if (itemTextColor != null) {
                    item.setForeground(itemTextColor);
                 }
                 if (itemBgdColor != null) {
                    item.setBackground(itemBgdColor);
                 }
        	 }
         }
      }

      @Override
      public void menuDeselected (MenuEvent e) {
         removeAll();
      }

      @Override
      public void menuCanceled (MenuEvent e) {
         removeAll();
      }
   }
}
