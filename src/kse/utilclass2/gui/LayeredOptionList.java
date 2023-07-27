package kse.utilclass2.gui;

/*
*  File: LayeredOptionList.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

/**
 * A subclass of <code>LayeredList</code> to organise a list of check-box
 * selectable list items in a tree-enabled, layered list.
 *
 * <p><b>Elements and Selection</b></code>
 * <p>List elements have to be instances of <code>LayeredOptionList.ListElement
 * </code> or a custom subclass thereof. This class allows reading and setting
 * of an item selection state via <code>isCheckSelected()</code> and
 * <code>setCheckSelected()</code>. A call-back method <code>checkBoxSelected()
 * </code> enables the user to react smartly to selection events. 
 * Any custom overriding of the <code>mouseClicked()</code> method must 
 * necessarily call the super method!
 *  
 * <p><b>Fine Grain</b></code>
 * <p>On list level, custom settings are feasible to determine whether 
 * check-boxes have to be precisely hit by mouse clicks in order to trigger 
 * selection action, and whether check-boxes are to be shown on FOLDER elements.
 */

public class LayeredOptionList extends LayeredList {

   private ListCellRenderer customCellRenderer = super.getCustomCellRenderer();
   private ListCellRenderer defaultRenderer = customCellRenderer;
   private boolean checkboxFolders;
   private boolean preciseHit;
   
   /** Creates an empty layered option list.
    */
   public LayeredOptionList() {
      init();
   }

   /** Creates a new layered option list with the given array of initial 
    * elements.
    * 
    * @param vector array of <code>ListElement</code>
    */
   public LayeredOptionList (ListElement[] vector) {
      super(vector);
      init();
   }

   private void init () {
      super.setCustomCellRenderer(new CheckListRenderer());
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
//      setFont(getFont().deriveFont((float)20.0));
//      setBackground(Color.lightGray);
      setForeground(Color.blue);
//      setEnabled(false);
   }
   
   
   @Override
   public ListCellRenderer getCustomCellRenderer () {
      return customCellRenderer;
   }

   @Override
   public void setCustomCellRenderer (ListCellRenderer cellRenderer) {
      customCellRenderer = cellRenderer == null ? defaultRenderer : cellRenderer;
   }

   @Override
   public LayeredList.ListElement addListEntry (LayeredList.ListElement entry) {
      if (!(entry instanceof ListElement)) { 
         throw new IllegalArgumentException(
            "entry must be of type LayeredOptionList.ListElement: ".concat(entry.getHierarchy()));
      }
      return super.addListEntry(entry);
   }

   @Override
   public ListElement[] getValues () {
      LayeredList.ListElement[] sup = super.getValues();
      ListElement[] arr = new ListElement[sup.length];
      int i = 0;
      for (LayeredList.ListElement el : sup) {
         arr[i++] = (ListElement)el;
      }
      return arr;
   }


public static class ListElement extends LayeredList.ListElement {

   private boolean selected;
   
   /** Creates a new list element. The Hierarchy parameter must contain a 
    * number with length of a multiple of 3. Each 3-digit
    * subsegment of this value represents an order level of the element tree. 
    * A list element is thus bound to an order level.
    * 
    * @param hierarchy String
    * @param data Object, may be null
    */
   public ListElement (String hierarchy, Object data) {
      super(hierarchy, data);
   }
   
   /** Creates a new list element with an initial check-box selection state. 
    * The Hierarchy parameter must contain a 
    * number with length of a multiple of 3. Each 3-digit
    * subsegment of this value represents an order level of the element tree. 
    * A list element is thus bound to an order level.
    * 
    * @param hierarchy String
    * @param data Object, may be null
    * @param selected boolean check-box selection state
    */
   public ListElement (String hierarchy, Object data, boolean selected) {
      super(hierarchy, data);
      this.selected = selected;
   }

   /** Returns this element's check-box selection state.
    * 
    * @return boolean check-box selection state
    */
   public boolean isCheckSelected () {
      return selected;
   }

   /** Sets this element's check-box selection state to assume the parameter
    * value.
    * 
    * @param selected boolean selection state
    */
   public void setCheckSelected (boolean selected) {
      if (this.selected != selected) {
         this.selected = selected;
         modified();
         checkBoxSelected(selected);
      }
   }

  /** Call-back method to allow application a simple way to react to 
   * selection actions on the element's check-box. 
   * 
   * @param selected boolean true == checked, false == unchecked 
   */
   public void checkBoxSelected (boolean selected) {
      if (debug) {
      System.out.println("Checkbox selected: " + getHierarchy() + " == " + selected);
      }
   }
   
   @Override
   public void mouseClicked (MouseEvent e) {
      super.mouseClicked(e);
      LayeredOptionList opList = (LayeredOptionList)getList(); 
      boolean haveCheckbox = opList.isCheckboxFolders() || !isFolder();
      
      if (haveCheckbox) {
         // determine mouse click success
         boolean targetHit = !opList.preciseHit; 
         if (!targetHit) {
            // calculation of mouse hit range when PRECISE-HIT enabled
            int indent = opList.getIndentPixels() * (getLayer()-1);
            int leftBound = opList.getInsets().left + indent + getFolderIconSize() -1;
            int rightBound = leftBound + 32;
            targetHit = e.getX() > leftBound && e.getX() < rightBound;
         }
         
         // determine button action
         boolean actionIsEnabled = isEnabled() & opList.isEnabled();
         if (targetHit & actionIsEnabled) {
            setCheckSelected(!isCheckSelected());
         }
      }
   }
}

/** Our cell renderer is installed as custom-cell-renderer at the super class
 * and covers the display area after line indentation and folder logic.
 */
private static class CheckListRenderer extends JPanel implements ListCellRenderer  {
   final static JCheckBox checkbox = new JCheckBox();

   public CheckListRenderer () {
      super(new BorderLayout());
      
      setOpaque(false);
      checkbox.setOpaque(false);
      checkbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
      
   }
   
   @Override
   public Component getListCellRendererComponent(
         JList list, Object value, int index,
         boolean isSelected, boolean hasFocus) {
      
      LayeredOptionList opList = (LayeredOptionList)list;
      ListElement element = (ListElement)value;
      boolean isEnabled = element.isEnabled() & opList.isEnabled();
      
      // panel init
      removeAll();
      setBackground(list.getBackground());
      
      // add the check-box if element is not a folder or the list
      // mandates showing check-boxes on folders
      if (opList.checkboxFolders || !element.isFolder()) {
         add(checkbox, BorderLayout.LINE_START);
         checkbox.setSelected(element.isCheckSelected());
         checkbox.setEnabled(isEnabled);
      } else {
      }

      // content rending after check-box (may be custom content)
      Component renderedComp = opList.customCellRenderer.getListCellRendererComponent(
            opList, value, index, isSelected, hasFocus);
      add(renderedComp, BorderLayout.CENTER);
      renderedComp.setBackground(list.getBackground());
      renderedComp.setForeground(list.getForeground());
      renderedComp.setEnabled(isEnabled);

      return this;
   }
}

/** Whether check-boxes are shown on FOLDER elements. If this is 
 * false, check-boxes are only shown on non-folder elements. Default value
 * is <b>false</b>.
 * 
 * @return boolean true == show check boxes
 */
public boolean isCheckboxFolders () {
   return checkboxFolders;
}

/** Sets whether check-boxes shall be shown on FOLDER elements. If this is set
 * to false, check-boxes are only shown on non-folder elements. Default value
 * is <b>false</b>.
 * 
 * @param show boolean true == show check boxes; false == don's show check boxes
 */
public void setCheckboxFolders (boolean show) {
   this.checkboxFolders = show;
}    

/** Whether a check-box has to be precisely hit by a mouse click in order to
 * trigger a button action. If this is false, a mouse click anywhere in the 
 * display area of an element leads to button action.  Default value is 
 * <b>false</b>.
 * 
 * @return boolean true == hit precisely; 
 *                 false == hit anywhere in element display
 */
public boolean isPreciseHit () {
   return preciseHit;
}

/** Sets whether a check-box has to be precisely hit by a mouse click in order
 *  to trigger an action. If this is set to false, a mouse click anywhere in 
 *  the display area of an element leads to button action.  Default value
 *  is <b>false</b>.
 */
public void setPreciseHit (boolean preciseHit) {
   this.preciseHit = preciseHit;
}

}
