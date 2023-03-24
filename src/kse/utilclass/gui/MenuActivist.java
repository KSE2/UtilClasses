package kse.utilclass.gui;

/*
*  File: AbstractByteChannel.java
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

import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

/** This interface describes a class which renders a {@code JMenu} or a
 * {@code JPopupMenu} from the parent {@code MenuActionSource} interface. 
 * The methods have default implementations.
 * 
 * @author Wolfgang Keller
  */
public interface MenuActivist extends MenuActionSource {

	/**
	 * Renders a {@code JPopupMenu} for the context of this interface or null
	 * if a popup menu is not defined. The default renders the results of 
	 * {@code getJMenu()} as a popup menu. 
	 * 
	 * @return <code>JPopupMenu</code> or null
	 */
	default JPopupMenu getPopupMenu () {
		JMenu menu = getJMenu();
		return menu == null ? null : menu.getPopupMenu();
	}

	/**
	 * Renders a named {@code JPopupMenu} for the context of this interface or 
	 * null if such a popup menu is not defined. The default renders the results
	 * of {@code getJMenu(name)} as a popup menu. 
	 * 
	 * @return <code>JPopupMenu</code> or null
	 */
	default JPopupMenu getPopupMenu (String name) {
		JMenu menu = getJMenu(name);
		return menu == null ? null : menu.getPopupMenu();
	}

	/** Renders a {@code JMenu} for the context of this interface or null
	 * if a menu is not defined. The default renders the results of 
	 * {@code getMenuActions()} contained in a {@code JMenu}.
	 *  
	 * @return {@code JMenu} or null
	 */
	default JMenu getJMenu () {
	   List<Action> alist = getMenuActions();
	   if (alist == null) return null;
	   JMenu menu = new JMenu();
	   for (Action a : alist) {
		   menu.add(a);
	   }
	   return menu;
	}
	
	/** Renders a named {@code JMenu} for the context of this interface or null
	 * if such a menu is not defined. The default renders the results of 
	 * {@code getMenuActions(name)} contained in a {@code JMenu}.
	 *  
	 * @return {@code JMenu} or null
	 */
	default JMenu getJMenu (String name) {
	   List<Action> alist = getMenuActions(name);
	   if (alist == null) return null;
	   JMenu menu = new JMenu();
	   for (Action a : alist) {
		   menu.add(a);
	   }
	   return menu;
	}
	
}
