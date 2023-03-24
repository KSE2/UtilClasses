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

/** This interface describes a class which renders {@code Action} objects for
 * the purpose of building a menu. 
 * The methods have default implementations.
 * 
 * @author Wolfgang Keller
  */
public interface MenuActionSource {

	/** Returns a list with edit actions available in this action source for the 
	 * purpose of creating a menu. The elements of the list shall be stable 
	 * during a program session, i.e. they can be persistently modified. Also 
	 * the list itself can be persistently modified. The default returns null.
	 * 
	 * @return {@code List<Action>} or null
	 */
	default List<Action> getMenuActions () {return null;}
	
	/** Returns a named list of edit actions available in this action source for 
	 * the purpose of creating a menu. The elements of the list shall be stable 
	 * during a program session, i.e. they can be persistently modified. Also 
	 * the list itself can be persistently modified. The default returns null.
	 * 
	 * @return {@code List<Action>} or null
	 */
	default List<Action> getMenuActions (String name) {return null;}
	
	
	/** Returns the action of the given action command token or null
	 * if such an action is not defined. The default searches the actions in
	 * {@code getMenuActions()}. 
	 * <p>This method assumes that the actions contained in this menu source can
	 * be uniquely identified by their property Action.ACTION_COMMAND_KEY.
	 * 
	 * @param cmd String command token
	 * @return {@code Action} or null if not found
	 */
	default Action getActionByCommand (String cmd) {
	   List<Action> alist = getMenuActions();
	   Action result = null;
	   if (alist != null && cmd != null) {
		   for (Action a : alist) {
			   if (cmd.equals(a.getValue(Action.ACTION_COMMAND_KEY))) {
				   result = a;
				   break;
			   }
		   }
	   }
	   return result;
	}
	
}
