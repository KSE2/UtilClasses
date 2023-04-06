/*
*  File: SystemProperties.java
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

package kse.utilclass.misc;

import java.beans.PropertyChangeListener;


public interface SystemProperties  {


   /**
    * Adds a <code>PropertyChange</code> listener. 
    *
    * @param listener  a <code>PropertyChangeListener</code> object
    */
   public void addPropertyChangeListener(PropertyChangeListener listener);
   /**
    * Removes a <code>PropertyChange</code> listener.
    *
    * @param listener  a <code>PropertyChangeListener</code> object
    */
   public void removePropertyChangeListener(PropertyChangeListener listener);

   /**
    * Gets one of the system properties using the associated key.
    * 
    * @param key String
    * @return Object the associated value
    */
   public Object getValue(String key);
   
   /**
    * Sets one of the system properties using the associated key. 
    * If the value has changed, a <code>PropertyChangeEvent</code> is sent
    * to listeners.
    *
    * @param key    a <code>String</code> containing the key
    * @param value  an <code>Object</code> value
    */
   public void putValue(String key, Object value);

   
}