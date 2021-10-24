package kse.utilclass.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Extension of <code>javax.swing.JList</code> which allows a graph ordering
 * of its elements and a folding feature of each tree node. The list behaves 
 * in a way typical to tree displays in that it knows branches that can be 
 * folded and unfolded. The component can be used like a JList except that it 
 * implements a fix data model, hence methods <code>setModel()<code> and 
 * <code>setListData()</code> are not supported. Index references into the
 * data model are unreliable in that index values of an element also refer to 
 * a set of folding states of tree nodes. Use of index values into the data 
 * model hence is discouraged, instead <code>getValues()</code> or <code>
 * getElement()</code> with parameter HIERARCHY can be used to access elements.  
 * 
 *  <p><b>Usage</b>
 *  <p>A list has to be fed by instances of <code>LayeredList.ListElement</code>
 *  or a user-supplied subclass thereof. A list element is characterised by
 *  a HIERARCHY and a RENDERDATA property. HIERARCHY is mandatory and unique
 *  in the set of elements of a list. It determines the place of the element
 *  in the list (sort value) and the parent-child relation of
 *  the tree of elements. The HIERARCHY value is a String containing a number
 *  with only digits and a length of a multiple of 3. Each digit-tripple 
 *  identifies an element in a layer and , to be more precise, under a tree
 *  node. This means the number of elements under each tree node, including
 *  root, is limited to 1000 entries. If higher numbers are required, the 
 *  code must be customised (which should not cause a big effort).
 */
public class LayeredList extends JList<LayeredList.ListElement> {
   protected static boolean debug;
   private static final int SWATCH_SIZE = 8;
   public final static Icon FOLDED_SWATCH = new ColorSwatch(SWATCH_SIZE, Color.yellow);
   public final static Icon UNFOLDED_SWATCH = new ColorSwatch(SWATCH_SIZE, Color.white);
   
   private SortedMap<String, ListElement> allMap; 
   private ListCellRenderer<? super ListElement> customCellRenderer = super.getCellRenderer();
   private Model model;
   private Icon foldedIcon = FOLDED_SWATCH;  // folded folder icon
   private Icon unfoldedIcon = UNFOLDED_SWATCH;  // unfolded folder icon
   private int indentPixels = 20;
   private boolean printHierarchy;

   /** Creates an empty layered list.
    */
   public LayeredList () {
      super( new Model() );
      model = (Model)getModel();
      allMap = new TreeMap<String, ListElement>();
      setCellRenderer(new OurCellRenderer());
      addListSelectionListener(model);

      // add the mouse listener logic for double-clicks == toggle folding
      // and clicking on the FOLDER ICON 
      MouseListener mouseListener = new MouseAdapter() {
         @Override
		public void mouseClicked(MouseEvent e) {
            // get circumstance
            Point point = e.getPoint();
            int clickCount = e.getClickCount();
            int index = LayeredList.this.locationToIndex(point);
            ListElement element = model.getElementAt(index);

            if (element.isFolder() & isEnabled()) {
               if (clickCount == 2) {
                  // toggle FOLDING on double-click
                  element.setFolding(!element.isFolding());
               } else {
                  // toggle FOLDING when hitting the switch icon
                  int indent = getIndentPixels() * (element.getLayer()-1);
                  int border = getInsets().left;
                  int leftIcon = border + indent -1;
                  int rightIcon = leftIcon + SWATCH_SIZE +2;
                  boolean hit = e.getX() > leftIcon && e.getX() < rightIcon;
                  if (hit) {
                     element.setFolding(!element.isFolding());
                  }
               }
            }
            
            // call item (external) for mouse click
            element.mouseClicked(e);
         }
      };
      addMouseListener(mouseListener);
   }

   /** Creates a layered list with the given array of initial list
    * elements.
    * 
    * @param vector array of <code>ListElement</code>
    */
   public LayeredList (ListElement[] vector) {
      this();
      addValues(vector);
   }

   /** Returns the amount of pixels by which display of a list element will
    * be indented depending on its hierarchy level value above 1. Defaults
    * to 20.
    * 
    * @return int indentation pixels
    */
   public int getIndentPixels () {
      return indentPixels;
   }

   /** Sets the amount of pixels by which display of a list element will
    * be indented depending on its hierarchy level value above 1. Defaults
    * to 20.
    */ 
   public void setIndentPixels (int indentPixels) {
      this.indentPixels = indentPixels;
   }

   @Override
   public void setModel (ListModel<ListElement> model) {
      throw new UnsupportedOperationException();
   }


   @Override
   public void setListData (ListElement[] listData) {
      throw new UnsupportedOperationException();
   }


   @Override
   public void setListData (Vector<? extends ListElement> listData) {
      throw new UnsupportedOperationException();
   }

   /** Returns the custom cell renderer of this list. By default this is a
    * <code>JLabel</code> renderer as used by the <code>JList</code> 
    * implementation.
    * 
    * @return <code>ListCellRenderer</code>
    */
   public ListCellRenderer<? super ListElement> getCustomCellRenderer () {
      return customCellRenderer;
   }

   /** Sets the custom cell renderer for this list. In any list line, the 
    * renderer given here paints <i>behind</i> the rendering logic of the 
    * layered list.
    * 
    * @param cellRenderer <code>ListCellRenderer</code>
    */
   public void setCustomCellRenderer (ListCellRenderer<? super ListElement> cellRenderer) {
      customCellRenderer = cellRenderer == null ? 
            super.getCellRenderer() : cellRenderer;
   }

   /** Sets the icon displayed in front of a list element if it is a FOLDER.
    * Two kinds of icons can be set: FOLDED and UNFOLDED, referring to
    * the FOLDING status of the list element involved.
    * 
    * @param folded boolean whether FOLDED or UNFOLDED icon is requested
    *               (true == FOLDED icon)
    * @return Icon the folder icon
    */
   public void setFolderIcon (Icon icon, boolean folded) {
      if (folded) {
         foldedIcon = icon;
      } else {
         unfoldedIcon = icon;
      }
   }

   /** Returns the icon displayed in front of a list element if it is a FOLDER.
    * Two kinds of icons can be returned: FOLDED and UNFOLDED, referring to
    * the FOLDING status of the list element involved.
    * 
    * @param folded boolean whether FOLDED or UNFOLDED icon is requested
    *               (true == FOLDED icon)
    * @return Icon the folder icon
    */
   public Icon getFolderIcon (boolean folded) {
      return folded ? foldedIcon : unfoldedIcon;
   }
   
 /** Returns a parent entry to the parameter list element based on a lookup
 * of its hierarchy value.
 *    
 * @param entry <code>ListElement</code> parent or null if not found
 * @return
 */
private ListElement findParentElement (ListElement entry) {
   ListElement found = null;

   // identify parent hierarchy notation
   String hi = entry.getHierarchy();
   if (hi.length() > 3) {
      hi = hi.substring(0, hi.length()-3);
      // look for parent in all-map
      found = allMap.get(hi);
   }
   return found;
}

/** Adds an element to this list.
 * 
 * @param entry <code>ListElement</code>
 * @return <code>ListElement</code> the previous element if it was replaced, 
 *         null otherwise
 */
public ListElement addListEntry (ListElement entry) {
   if (entry == null)
      throw new IllegalArgumentException("list entry may not be null");
   
   // detect a parent (folder) entry and set properties
   ListElement parentElement = findParentElement(entry);
   if (parentElement != null) {
      entry.setParent(parentElement);
      parentElement.setFolder(true);
   }

   // detect if children are present 
   // if so, set FOLDER property and all children's PARENT property
   ListElement[] arr = getFolderElements(this, entry.getHierarchy());
   if (arr.length > 0) {
      entry.setFolder(true);
      for (ListElement e : arr) {
         e.setParent(entry);
      }
   }
   
   // add entry and make showing in list (if that is opted in entry settings)
   ListElement wasElement = allMap.put(entry.getHierarchy(), entry);
   entry.setList(this);
   
   return wasElement != entry ? wasElement : null;
}

/** Removes all entries from this list. This includes update of the display.
 */
public void clear () {
   for (ListElement el : getValues()) {
      removeListEntry(el);
   }
}

/** Removes a list entry from this list.
 * 
 * @param entry <code>ListElement</code> list element to be removed
 * @return <b>true</b> if and only if a list element was actually removed
 */
public boolean removeListEntry (ListElement entry) {
   ListElement removed = allMap.remove(entry.getHierarchy());
   if (removed != null) {
      // remove from display
      removed.setShowing(false);

      // if the removed entry is a folder, remove PARENT property of all its children
      if (removed.isFolder()) {
         ListElement[] chi = getFolderElements(this, removed.getHierarchy());
         for (ListElement e : chi) {
            e.setParent(null);
         }
      }
      
      // remove list-related values
      removed.setList(null);
      removed.setParent(null);
      removed.setFolder(false);
   }
   return removed != null;
}

/** Depending on the boolean parameter, puts a list-element into the display 
 * model of the list or retracts it. 
 * 
 * @param listElement ListElement 
 * @param show boolean true = put to display-model; false = withdraw from model
 */
protected void setElementToModel (ListElement listElement, boolean show) {
   
   if (show) {
//      System.out.println("-- make element SHOWING: ".concat(listElement.getHierarchy()));
      model.addEntry(listElement);
   } else {
//      System.out.println("-- make element HIDING: ".concat(listElement.getHierarchy()));
      model.removeEntry(listElement);
   }
}

/** Whether the given list element is a member of this list.
 * 
 * @param element <code>ListElement</code>
 * @return boolean
 */
public boolean contains (ListElement element) {
   return allMap.containsValue(element);
}

/** Whether the given HIERARCHY value is represented in a list element of
 * this list.
 *  
 * @param hierarchy String 
 * @return boolean
 */
public boolean contains (String hierarchy) {
   return allMap.containsKey(hierarchy);
}

/** Returns the number of elements contained in this list.
 *  
 * @return int size of this list 
 */
public int getElementCount () {
   return allMap.size();
}

/** Returns the list element with the specified HIERARCHY value (key entry)
 * contained in this list or <b>null</b> if this key is unknown.
 * 
 * @param hierarchy String key entry (HIERARCHY)
 * @return <code>ListElement</code>
 */
public ListElement getElement (String hierarchy) {
   return allMap.get(hierarchy);
}

/** Returns all list elements contained in this list in an array ordered
 * after their HIERARCHY values.
 * 
 * @return array of <code>ListElement</code>
 */
public ListElement[] getValues () {
   return allMap.values().toArray(new ListElement[allMap.size()]);
}

/** Replaces the current content of this list with the elements given by
 * the parameter array. If the parameter is null the current list content is 
 * deleted.
 * 
 * @param vector array of <code>ListElement</code>, may be null
 */
public void setValues (ListElement[] vector) {
   clear();
   addValues(vector);
}

/** Adds the set of list elements as given by the parameter to the content of
 * this list. Previous entries under same HIERARCHY values are replaced.
 * 
 * @param vector array of <code>ListElement</code>, may be null
 */
public void addValues (ListElement[] vector) {
   if (vector != null) {
      for (ListElement e : vector) {
         addListEntry(e);
      }
   }
}

/** Returns all elements of <code>list</code> which are visible and a direct 
 * child or descendant of the element defined by parameter <code>hierarchy</code>.
 * 
* 
* @param list LayeredList
* @param hierarchy String
* @param recurse boolean if true then search includes sub-folders otherwise 
*        only children are returned
* @return array of <code>ListElement</code> or null if <code>list</code> was null
*/ 
public static ListElement[] getFolderElements (LayeredList list, 
       String hierarchy, boolean recurse) {
   // return all-map elements which are visible and are a direct child of
   // the parameter hierarchy (does not list grand-children)
   if (list != null) {
      int targetLayer = getHierarchyLayer(hierarchy) +1;
      SortedMap<String, ListElement> tmap = list.allMap.tailMap(hierarchy);
      ArrayList<ListElement> reslist = new ArrayList<ListElement>();

      for (ListElement value : tmap.values()) {
         // terminate if we get beyond parent hierarchy 
         if (!value.getHierarchy().startsWith(hierarchy)) break;
         // skip invisible elements or parent
         if (!value.isVisible() || value.getHierarchy().equals(hierarchy)) {
            continue;
         }
         // skip sub-folder content if RECURSE not opted
         if (!recurse && value.getLayer() != targetLayer) {
            continue;
         }
         reslist.add(value);
      }
      return reslist.toArray(new ListElement[reslist.size()]);
   }
   return null;
}

/** Returns all elements of <code>list</code> which are visible and a direct 
 * child of the element defined by parameter <code>hierarchy</code>.
* 
* @param list LayeredList
* @param hierarchy String
* @return array of <code>ListElement</code> or null if <code>list</code> was null
*/ 
public static ListElement[] getFolderElements (LayeredList list, 
       String hierarchy) {
   return getFolderElements(list, hierarchy, false);
}

/** Whether the parameter string is a valid hierarchy value. It is valid
 * if it is not null, contains only digits and has a length of a multiple of
 * 3.
 * 
 * @param hi String hierarchy candidate value
 * @return boolean true == valid, false == invalid
 */
protected static boolean testHierarchy (String hi) {
   return hi != null && hi.length() > 0 && 
          hi.length() % 3 == 0 && isAllNumbers(hi);
}
   
private static boolean isAllNumbers (String hi) {
   int len = hi.length();
   for (int i = 0; i < len; i++) {
      if (!Character.isDigit(hi.charAt(i))) {
         return false;
      }
   }
   return true;
}

/** Returns the LAYER (= indentation level) value as calculated from a
 *  hierarchy notation.
 *  
 * @param hierarchy String element hierarchy value
 * @return int layer value
 */
protected static int getHierarchyLayer (String hierarchy) {
   int len = hierarchy.length();
   return len/3;
}

//  ------------- SUBCLASSES ----------------

/** Whether the default cell renderer should print the HIERARCHY value
 * of an element is display. Defaults to <b>false</b>.
 * 
 * @return boolean true == print HIERARCHY value
 */
public boolean isPrintHierarchy () {
   return printHierarchy;
}

/** Sets whether the default cell renderer should print the HIERARCHY value
 * of an element is display. Function useful for testing.
 * 
 * @param printHierarchy boolean true == print HIERARCHY value
 */
public void setPrintHierarchy (boolean printHierarchy) {
   this.printHierarchy = printHierarchy;
}

/** Defines the basic behaviour of a list element. 
 * 
 * By default, new list elements are VISIBLE, not SHOWING and not FOLDING
 * their children.
 */
public static class ListElement implements Comparable<ListElement> {
   
   private LayeredList list;
   private ListElement parent;
   private String hierarchy;  
   private boolean visible = true;
   private boolean enabled = true;
   private boolean showing;
   private boolean folder;
   private boolean isFolding;
   private boolean itemSelected;
   
   private Object renderData;
   
   /** Creates a new list element. The Hierarchy parameter must contain a 
    * number with length of a multiple of 3. Each 3-digit
    * subsegment of this value represents an order level of the element tree. 
    * A list element is thus bound to an order level.
    * 
    * @param hierarchy String
    * @param data Object, may be null
    */
   public ListElement (String hierarchy, Object data) {
      setHierarchy(hierarchy);
      renderData = data;
   }
   
   /** Tests the argument and throws an IllegalArgumentException if the value 
    * is found to be illegal. Any value, including null, is illegal if its
    * length is not a multiple (1..n) of 3 or one of its characters outside 
    * the range '0'..'9'.
    *  
    * @param hierarchy String
    * @throws IllegalArgumentException
    */
   protected void controlHierarchy (String hierarchy) {
      if (!testHierarchy(hierarchy)) 
         throw new IllegalArgumentException("bad list element hierarchy notation: "
              + hierarchy);
   }

   /** Returns the PARENT element to this element if it exists or null
    * otherwise. <p><small>The parent has a LAYER property of -1 to this element.
    * </small>
    * 
    * @return <code>ListElement</code> or null
    */
   public ListElement getParent () {
      return parent;
   }

   private void setParent (ListElement parent) {
      this.parent = parent;
   }

   /** Returns the HIERARCHY value of this element. This is non-null and
    * valid.
    * 
    * @return String
    */
   public String getHierarchy () {
      return hierarchy;
   }

   /** Sets the HIERARCHY notation for this list element.
    *  
    * @param hierarchy String
    * @throws IllegalArgumentException if the given value is illegal, 
    *         including null
    */
   public void setHierarchy (String hierarchy) {
      controlHierarchy(hierarchy);
      
      // if hierarchy is a new value, assign it
      // if we have a list defined, re-insert this element to keep the  list
      // ordered
      if (!hierarchy.equals(this.hierarchy)) {
         LayeredList holdingList = list;
         if (holdingList != null) {
            holdingList.removeListEntry(this);
         }
         this.hierarchy = hierarchy;
         if (holdingList != null) {
            holdingList.addListEntry(this);
         }
      }
   }

   /** Whether this element has the VISIBLE status (true).
    * An INVISIBLE element (VISIBLE == false) is never shown in the list
    * but remains member of the list repository.
    *   
    * @return boolean true == element is VISIBLE
    */
   public boolean isVisible () {
      return visible;
   }

   /** Whether this element is ENABLED. 
    *   
    * @return boolean true == element is ENABLED
    */
   public boolean isEnabled () {
      return enabled;
   }

   /** Sets the ENABLED property of this element. A DISABLED element
    * is shown in a renderer specific way; the default renderer using
    * JLabel prints a string in a reduced colour.
    * <p><small>The list does not prevent user induced folding activity of a 
    * DISABLED folder element.</small>
    * 
    * @param enabled boolean ENABLED value
    */
   public void setEnabled (boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
      }
   }
   
   /** Sets the VISIBILITY property for this element.
    * An INVISIBLE element (VISIBLE == false) is never shown in the list
    * but remains member of the list repository.
    * 
    * @param visible boolean true == VISIBLE, false == INVISIBLE
    */
   public void setVisible (boolean visible) {
      if (visible == this.visible) return;
      this.visible = visible;
      
      // ensure non-visible is not showing
      if (!visible) {
         setShowing(false);

      // ensure visible element is showing if there is no excluding condition
      } else {
         showIfUnfoldedParentShows();
      }
   }

   /** Shows this list element if it has no parent (list level 1)
    * or the parent is SHOWING and not FOLDING.
    */
   private void showIfUnfoldedParentShows () {
      ListElement parent = getParent();
      if (getLayer() == 1 ||
          parent != null && parent.isShowing() && !parent.isFolding()) {
         setShowing(true);
      }
   }

   /** Returns the size in pixel of the FOLDER toggle icon currently on display 
    * for this element. This returns the X dimension of the icon. If this 
    * element is not a FOLDER, zero is returned.
    * 
    * @return int icon size
    */
   protected int getFolderIconSize () {
      LayeredList list = getList();
      if (list == null) {
         return 0;
      }
      return isFolder() ? list.getFolderIcon(isFolding()).getIconWidth() : 0;
   }
   
   /** Returns this element's data object which is used by the custom cell 
    * renderer (by default a JLabel renderer) to show individual content.
    *  
    * @return Object render data or null if undefined
    */
   public Object getRenderData () {
      return renderData;
   }

   /** Sets this element's data object which is used by the custom cell 
    * renderer (by default a JLabel renderer) to show individual content.
    * 
    * @param renderData Object element content, may be null
    */ 
   public void setRenderData (Object renderData) {
      this.renderData = renderData;
   }

   /** Whether this list element is actually showing in a LayeredList.
    * An element is SHOWING if and only if it is VISIBLE and not hidden in a
    * folded folder.
    *   
    * @return boolean 
    */
   public boolean isShowing () {
      return showing;
   }

   /** Sets whether this element is SHOWING in this list.
    * <p><small>Use of this method by application is inadequate. The affected
    * element may re-appear after being set to HIDE through folding/unfolding
    * activity. The proper way to 
    * determine VISIBILITY of an element is by use of the <code>setVisible()</code>
    * method.</small>
    * 
    * @param show boolean true == SHOW element, false == HIDE element
    */
   public void setShowing (boolean show) {
      if (show == showing | list == null | !visible & show ||
          parent == null && getLayer() > 1  ) {
         return;
      }
      showing = show;

      // ensure parent is showing and unfolded if child is to be shown
      if (show & parent != null) {
         parent.setShowing(true);
         parent.setFolding(false);
      }
      
      // set list element showing / not showing in list structure
      list.setElementToModel(this, show);

      // if FOLDER then transfer SHOWING quality to children
      // (do not show elements of folders which are FOLDING)
      if (folder & (!isFolding | !show)) {
         ListElement[] children = getFolderElements(list, hierarchy);
         for (ListElement e : children) {
            e.setShowing(show);
         }
      }
   }
   
   /** Whether this list element is a FOLDER. A list element is a FOLDER if
    * and only if there exist sub-elements to its hierarchy notation in the 
    * set of list elements. An element which is not a member of a list
    * therefore cannot be a FOLDER.
    * <p><small>Example: A hierarchy notation "000111002" defines a sub-element
    * to the FOLDER element with notation "000111".
    * 
    * @return boolean true == element is a FOLDER
    */
   public boolean isFolder () {
      return folder;
   }

   /** Sets the quality marker of this element to represent a FOLDER.
    * 
    * @param folder boolean true == this element is a FOLDER; 
    *               false == this element is not a FOLDER
    */
   private void setFolder (boolean folder) {
      this.folder = folder;
   }

   /** Whether this list element is or will be hiding its sub-elements. (A 
    * return value <b>true</b> does not imply that this element actually owns 
    * sub-elements.)
    *  
    * @return boolean true == this element is hiding its sub-elements;
    *                 false == this element is showing its sub-elements
    */
   public boolean isFolding () {
      return isFolding;
   }

   /** This methods sets whether this list element is FOLDING (=hiding) its 
    * sub-elements in case it owns some or will own some in future.
    * 
    * @param folding boolean true == hide sub-elements
    */
   public void setFolding (boolean folding) {
      if (folding == isFolding) return;
      isFolding = folding;

      // get the elements of the folder
      // and set their SHOWING attribute according to folding request
      ListElement[] elems = getFolderElements(list, hierarchy);
      if (elems != null) {
         for (ListElement e : elems) {
            e.setShowing(!folding);
         }
      }
   }

   /** Returns the <code>LayeredList</code> where this list element is 
    * integrated or null if it is not related to such a list. 
    * 
    * @return <code>LayeredList</code> or null
    */
   public LayeredList getList () {
      return list;
   }

   /** Sets the LIST reference for this list element. Does, however, not
    * enter this element into the parameter list!
    * <p><small>A list element can be part of only one list. If this method
    * assigns a list which is not the list where this element is 
    * currently part of, the element gets removed from the current list. 
    * </small>
    * 
    * @param list <code>LayeredList</code> containing list structure
    */
   private void setList (LayeredList list) {
      if (list == this.list) return;
      
      // remove this element from a previous list
      if (this.list != null) {
         this.list.removeListEntry(this);
      }

      // restore SHOWING of this entry in new list (entry must be added)
      this.list = list;
      if (list != null && isVisible()) {
         showIfUnfoldedParentShows();
         
         if (isFolder()) {
            // get the elements of the folder
            // and set their SHOWING attribute according to folding request
            boolean display = !isFolding();
            ListElement[] elems = getFolderElements(list, hierarchy);
            if (elems != null) {
               for (ListElement e : elems) {
                  e.setShowing(display);
               }
            }
         }
      }
   }

   /** Calls "external" method <code>itemSelected()</code> if and only if a
    * modification has occurred on this element's selection value.
    *   
    * @param selected boolean current item selection value
    */
   private void fireSelectionChanged (boolean selected) {
      if (selected != itemSelected) {
         itemSelected = selected;
         itemSelected(selected);
      }
   }
   
   /** To be called by application when rendering active content of this list
    * element has been modified. This causes a refresh of the GUI display of
    * this element.
    */
   public void modified () {
      if (list != null) {
         list.model.entryModified(this);
      }
   }
   
   /** Call-back method to allow application a simple way to react to element
    * selection events. 
    * 
    * @param selected boolean true == item selected, false = item de-selected 
    */
   public void itemSelected (boolean selected) {
      if (debug) {
      System.out.println("Item selected: " + hierarchy + " /value == " + selected);
      }
   }
   
   /** Whether this element is selected in the containing list's item 
    * selection model.
    *   
    * @return boolean true == element is selected
    */
   public boolean isItemSelected () {
      return itemSelected;
   }

   /** Call-back method to allow application a simple way to react to mouse
    * clicks on the GUI representation of this element. 
    * 
    * @param e <code>MouseEvent</code> mouse event
    */
   public void mouseClicked (MouseEvent e) {
      if (debug) {
      System.out.println("Clicked on Item " + hierarchy + " /point = " + e.getPoint());
      }      
   }
   
   /** Returns the HIERARCHY level of this element. The base level for an
    * element under root is 1 and corresponds to a 3-digit HIERARCHY notion. 
    * For each additional 3-digit parcel in the HIERARCHY, the layer value is
    * increased by 1.
    * 
    * @return int hierarchy level
    */
   public int getLayer () {
      return getHierarchyLayer(hierarchy);
   }
   
   @Override
   public String toString () {
      boolean printHier = list != null && list.isPrintHierarchy(); 
      String print = printHier ? getHierarchy().concat(" : ") : "";
      
      Object object = renderData;
      if (renderData == null) {
         object = "list element " + getHierarchy();
      }
      return print + object.toString();
   }

   @Override
   public int hashCode () {
      return hierarchy.hashCode();
   }

   /** Two list elements are equal if and only if their HIERARCHY values
    * are equal.
    */
   @Override
   public boolean equals (Object obj) {
      return obj != null && obj instanceof ListElement &&
             hierarchy.equals(((ListElement)obj).hierarchy);
   }
   
   @Override
   public int compareTo (ListElement o) {
      return hierarchy.compareTo(o.hierarchy);
   }

}

protected static class Model extends AbstractListModel<ListElement> implements ListSelectionListener {
   private List<ListElement> tsa = new ArrayList<ListElement>(); 

   /** Removes all list entries from this model. 
    */
   public void removeAll () {
      synchronized (tsa) {
         int size = tsa.size();
         if (size > 0) {
            tsa.clear();
            entryRemoved(0, size-1);
         }
      }      
   }

   /** Adds the specified list element to this model. If an entry with same
    * identification already exists prior to insertion, it is replaced by
    * the given entry and returned. Synchronised.
    * 
    * @param entry <code>ListElement</code> to be added
    * @return <code>ListElement</code> replaced entry or null
    */
   public ListElement addEntry (ListElement entry) {
      ListElement ret = null ;
      synchronized (tsa) {
         int index = getSortIndexPosition(entry);
         if (index < tsa.size() && tsa.get(index).equals(entry)) {
            tsa.set(index, entry);
            ret = entry;
            entryModified(index);
         } else {
            tsa.add(index, entry);
            entryAdded(index);
         }
         return ret;
      }
   }
   
   /** Removes the specified list element from this model. Synchronised.
    * 
    * @param entry <code>ListElement</code> element to remove
    * @return boolean true if this model contained the entry
    */
   public boolean removeEntry (ListElement entry) {
      synchronized (tsa) {
         int index = tsa.indexOf(entry);
         boolean removed = index > -1;
         if (removed) {
            tsa.remove(index);
            entryRemoved(index, index);
         }
         return removed;
      }
   }
   
   /** This can be called to cause a rendering refresh of the list element 
    * given. Synchronised.
    * 
    * @param element <code>ListElement</code>
    */
   public void entryModified (ListElement element) {
      synchronized (tsa) {
         entryModified(indexOf(element));
      }
   }
   
   private void entryAdded (int index) {
      this.fireIntervalAdded(this, index, index);
   }

   private void entryModified (int index) {
      this.fireContentsChanged(this, index, index);
   }

   private void entryRemoved (int index1, int index2) {
      this.fireIntervalRemoved(this, index1, index2);
   }
   
   /** Returns the sorting index position for the given list element to be
    * entered into this model.
    * 
    * @param entry ListElement
    * @return int index position (ranges from 0)
    */
   private int getSortIndexPosition (ListElement entry) {
      int i;
      for (i = 0; i < tsa.size(); i++) {
         if (tsa.get(i).compareTo(entry) >= 0) {
            break;
         }
      }
      return i;
   }
   
   /** Returns all current values in this model sorted after their 
    * HIERARCHY values.
    * 
    * @return array of <code>ListElement</code>
    */
   public ListElement[] getValues () {
      return tsa.toArray(new ListElement[tsa.size()]);
   }

   public boolean contains (ListElement element) {
      return tsa.contains(element);
   }
   
   /** Returns the index of the given list element in the model.
    * 
    * @param element <code>ListElement</code>
    * @return int index of element
    */
   public int indexOf (ListElement element) {
      return tsa.indexOf(element);
   }
   
   @Override
   public int getSize () {
      return tsa.size();
   }

   @Override
   public ListElement getElementAt (int index) {
      synchronized (tsa) {
         if (-1 < index & index < tsa.size()) {
            return tsa.get(index);
         }
      }
      return null;
   }

   /**
    * Implementation of the ListSelectionListener as we call elements 
    * individually for any selection change on them.
    * 
    * @param e
    */
   @Override
   public void valueChanged (ListSelectionEvent e) {
      if (!e.getValueIsAdjusting()) {
         for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
            ListElement elem = getElementAt(i);
            ListSelectionModel sm = elem.getList().getSelectionModel();
            elem.fireSelectionChanged(sm.isSelectedIndex(i));
         }
      }
   }
}

/** Our list cell renderer organises display of indentation (according to
 * elements' LAYER values), the switch icon and individual element content.
 * The latter is rendered by the CUSTOM CELL RENDERER which can be set by
 * the user, while the default thereof is a <code>JLabel</code> renderer. 
 */
private static class OurCellRenderer extends JPanel implements ListCellRenderer<ListElement> {
   final static JLabel iconLabel = new JLabel();
   final static JPanel prefix = new JPanel(new BorderLayout());
   final static JPanel indent = new JPanel(new BorderLayout());
   final static Dimension dim = new Dimension(0,0);

   // This is the only method defined by ListCellRenderer.
   // We just reconfigure the JLabel each time we're called.

   public OurCellRenderer () {
      super(new BorderLayout());
      
      prefix.setOpaque(false);
      indent.setOpaque(false);
      iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
      
   }
   
   @Override
   public Component getListCellRendererComponent(
     JList<? extends ListElement> list,              // the list
     ListElement value,            // value to display
     int index,               // cell index
     boolean isSelected,      // is the cell selected
     boolean cellHasFocus)    // does the cell have focus
   {
      LayeredList opList = (LayeredList)list;
      ListElement elem = value;
      boolean isFolder = elem.isFolder();
      boolean isEnabled = elem.isEnabled() & opList.isEnabled();

      removeAll();
      
      // calculate indentation pixels after hierarchy level
      int pix = opList.getIndentPixels() * (elem.getLayer()-1);
      
      // define and add prefix or remove if not required
      if (pix != 0 | isFolder) {
         add(prefix, BorderLayout.LINE_START);
         
         // indentation according to hierarchy level
         if (pix == 0) {
            prefix.remove(indent);
         } else {
            dim.setSize(pix, 3);
            indent.setPreferredSize(dim);
            prefix.add(indent, BorderLayout.LINE_START);
         }
         
         // add/remove folder icon
         if (isFolder) {
            iconLabel.setIcon(opList.getFolderIcon(elem.isFolding()));
            prefix.add(iconLabel, BorderLayout.CENTER);
         } else {
            prefix.remove(iconLabel);
         }
      }

      // content rending
      Component renderedComp = opList.customCellRenderer.getListCellRendererComponent(
            opList, value, index, isSelected, cellHasFocus);
      add(renderedComp, BorderLayout.CENTER);
      renderedComp.setEnabled(isEnabled);
      
      // prefix background colour setting after custom rendered component
      setBackground(renderedComp.getBackground());

//      String s = elem.toString();
//      label.setText(s);
//       
//      // selection colours
//      if (isSelected) {
//          label.setBackground(list.getSelectionBackground());
//          setBackground(list.getSelectionBackground());
//          label.setForeground(list.getSelectionForeground());
//      } else {
//          label.setBackground(list.getBackground());
//          setBackground(list.getBackground());
//          label.setForeground(isFolder ? Color.red : list.getForeground());
//      }
//
//       // others
//       label.setEnabled(list.isEnabled());
//       label.setFont(list.getFont());
//       label.setOpaque(true);

       return this;
   }
}

}
