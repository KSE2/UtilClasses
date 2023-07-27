/*
 *  File: AutomoveDialog.java
 * 
 *  Project Jpws-Front
 *  @author Wolfgang Keller
 *  Created 25.11.2005
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

package kse.utilclass.gui;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/** 
 * Component adapter which realises movement of a target component in dependence
 * of another component, here called "parent". The source of the movement is
 * "parent".
 *  
 *  @author Wolfgang Keller, Sarath
 */

public class AutomoveAdapter extends ComponentAdapter {
   /** The parent frame. */
   Component frame;
   
   /** The movable frame. */
   Component target;
   
   /** Previous position of the parent frame. */
   int px=0, py=0;
   
   /** whether this function is operative. */
   boolean active;
   
   
   public AutomoveAdapter ( Component parent, Component target ) {
      if ( parent == null | target == null )
         throw new NullPointerException();
      
      this.frame = parent;
      this.target = target;
      
      // Store parent frame's location
      px=frame.getX();
      py=frame.getY();

      // register move listener  
      frame.addComponentListener( this );
   }
   
   /** Terminates the bondage of parent and target component. 
    */
   public void release () {
      if (frame != null) {
         frame.removeComponentListener(this);
      }
      frame = null;
      target = null;
   }
   
   /** Switches activity of this translation function.
    * 
    * @param v boolean true = active
    */
   public void setActive (boolean v) {
	   active = v;
   }
   
   private void moved ( Component c ) {
      int dx, dy, npx, npy;

      try{
         npx = c.getX();
         npy = c.getY();
         dx = px-npx;
         dy = py-npy;
         target.setLocation( target.getX()-dx, target.getY()-dy );
         //_instance.move(_instance.getX()+dx,_instance.getY()+dy);
         //Log.debug(Log.DEFAULT_DEBUGLEVEL,"dx=)
         px = npx;
         py = npy;
      } catch (Exception ex) { 
    	 ex.printStackTrace(); 
      }
   }

   @Override
   public void componentMoved( ComponentEvent e ) {
	  if (active) { 
		  moved( e.getComponent() );
	  }
   }

   @Override
   public void componentResized ( ComponentEvent e ) {
	  if (active) {
		  moved( e.getComponent() );
	  }
   }
}

