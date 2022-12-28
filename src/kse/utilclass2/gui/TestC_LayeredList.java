package kse.utilclass2.gui;

/*
*  File: TestC_LayeredList.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.swing.JPanel;

import org.junit.Test;

import kse.utilclass2.gui.LayeredList.ListElement;

public class TestC_LayeredList {

   
   @Test
   public void test_ListElement_ordering () {
      LayeredList.ListElement liEl1, liEl2, liEl3, liEl4;
      
      liEl1 = new ListElement("000", null);
      liEl2 = new ListElement("000", null);
      
      // test element logic
      assertTrue("same hierarchy list-element equals another", liEl1.equals(liEl2));
      assertTrue("neutral list-element hashcode equals another", liEl1.hashCode() ==
              liEl2.hashCode());
      assertTrue("same hierarchy list-element comparison equal", liEl1.compareTo(liEl2) == 0);
      
      // test element self-relation
      assertTrue("list-element equals itself", liEl1.equals(liEl1));
      assertTrue("list-element hashcode equals itself", liEl1.hashCode() ==
              liEl1.hashCode());
      assertTrue("list-element comparison self", liEl1.compareTo(liEl1) == 0);
      
      liEl3 = new ListElement("000", null);
      liEl2 = new ListElement("111", null);
      liEl4 = new ListElement("110", null);
      
      // test fully defined element in relation to some other
      assertFalse("list-element not equals other value", liEl1.equals(liEl2));
      assertFalse("list-element hashcode not equals other value", 
            liEl1.hashCode() == liEl2.hashCode());
      assertTrue("neutral list-element ranks lower than any fully defined", 
            liEl1.compareTo(liEl2) < 0);
      
      // test fully defined element self-relation
      assertTrue("fully defined list-element equals itself", liEl3.equals(liEl3));
      assertTrue("fully defined list-element hashcode equals itself", liEl3.hashCode() ==
              liEl3.hashCode());
      assertTrue("fully defined list-element comparison self", liEl3.compareTo(liEl3) == 0);

      // test fully defined element logic
      assertFalse("fully defined list-element not equals another", liEl3.equals(liEl2));
      assertFalse("fully defined list-element hashcode not equals another", liEl3.hashCode() ==
              liEl2.hashCode());
      assertTrue("fully defined list-element comparison 1", liEl3.compareTo(liEl2) < 0);
      assertTrue("fully defined list-element comparison 2", liEl2.compareTo(liEl3) > 0);
      assertTrue("fully defined list-element comparison 3", liEl2.compareTo(liEl4) > 0);
      assertTrue("fully defined list-element comparison 4", liEl4.compareTo(liEl2) < 0);

      liEl4 = new ListElement("000000", null);
      liEl2 = new ListElement("001000", null);
      
      // test element nesting comparison
      assertTrue("list-element nesting comparison 1", liEl3.compareTo(liEl4) < 0);
      assertTrue("list-element nesting comparison 2", liEl4.compareTo(liEl3) > 0);
      assertTrue("list-element nesting comparison 3", liEl2.compareTo(liEl4) > 0);
      assertTrue("list-element nesting comparison 4", liEl4.compareTo(liEl2) < 0);
   }
   
   @Test
   public void test_ListElement_init_and_failure () {
      LayeredList.ListElement liEl1, liEl2;

      String hiValue = "000001";
      liEl1 = new ListElement("000", null);
      liEl2 = new ListElement(hiValue, null);
      
      // test some initial values
      assertTrue("initial isVisible == false", liEl2.isVisible());
      assertFalse("initial isShowing == false", liEl2.isShowing());
      assertFalse("initial isFolder == false", liEl2.isFolder());
      assertFalse("initial isFolding == false", liEl2.isFolding());
      assertNull("initial getParent == null", liEl2.getParent());
      
      // test hierarchy and null render-data get-values
      assertEquals("getHierarchy neutral after init", liEl1.getHierarchy(), "000");
      assertEquals("getHierarchy value after init", liEl2.getHierarchy(), hiValue);
      assertNull("getData null after init 1", liEl1.getRenderData());
      assertNull("getData null after init 2", liEl2.getRenderData());
      
      Object obi1 = new JPanel();
      liEl1 = new ListElement("000", obi1);
      liEl2 = new ListElement(hiValue, obi1);

      // test render-data init values
      assertSame("getData value after init 1", liEl1.getRenderData(), obi1);
      assertSame("getData value after init 2", liEl2.getRenderData(), obi1);
      
      try {
         liEl1 = new ListElement(null, null);
         fail("missing exception on illegal hierarchy value == null");
      } catch (Exception e) {
         assertTrue("false exception thrown", e instanceof IllegalArgumentException);
      }

      try {
         liEl1 = new ListElement("", null);
         fail("missing exception on illegal hierarchy value, 1");
      } catch (Exception e) {
         assertTrue("false exception thrown", e instanceof IllegalArgumentException);
      }

      try {
         liEl1 = new ListElement("0", null);
         fail("missing exception on illegal hierarchy value, 2");
      } catch (Exception e) {
         assertTrue("false exception thrown", e instanceof IllegalArgumentException);
      }

      try {
         liEl1 = new ListElement("00011", null);
         fail("missing exception on illegal hierarchy value, 3");
      } catch (Exception e) {
         assertTrue("false exception thrown", e instanceof IllegalArgumentException);
      }
   }
   
   @Test
   public void test_ListElement_value_setting () {
      LayeredList list = createList();
      LayeredList.ListElement liEl1, liEl2, liEl3;

      String hiValue = "000001";
      liEl1 = new ListElement("000", null);
      list.addListEntry(liEl1);
      liEl2 = new ListElement(hiValue, null);
      list.addListEntry(liEl2);
      liEl2.setVisible(true);
      
      // test hierarchy setting method
      assertEquals("hierarchy value setting 1", liEl1.getHierarchy(), "000");
      assertNull("parent==null for level-0 element", liEl1.getParent());
      liEl1.setHierarchy("000002");
      assertEquals("hierarchy value setting 2", liEl1.getHierarchy(), "000".concat("002"));
      
      // test "visible" setting
      liEl1.setVisible(true);
      assertTrue("isVisible value setting 1", liEl1.isVisible());
      liEl1.setVisible(false);
      assertFalse("isVisible value setting 2", liEl1.isVisible());
      
      // test "showing" setting in INVISIBLE state
      liEl1.setShowing(true);
      assertFalse("isShowing should not be settable TRUE if invisible", liEl1.isShowing());
      liEl1.setShowing(false);
      assertFalse("isShowing FALSE value setting always allowed", liEl1.isShowing());

      // test "showing" setting in VISIBLE state
      liEl1.setHierarchy("002");
      liEl1.setVisible(true);
      liEl1.setShowing(true);
      assertTrue("isShowing value setting 1", liEl1.isShowing());
      liEl1.setShowing(false);
      assertFalse("isShowing value setting 2", liEl1.isShowing());
      
      // test "folding" setting (LEAF)
      liEl1.setFolding(true);
      assertTrue("isFolding value setting TRUE", liEl1.isFolding());
      liEl1.setFolding(false);
      assertFalse("isFolding value setting FALSE", liEl1.isFolding());

      // test "folding" setting (FOLDER)
      liEl3 = new ListElement("000", null);
      liEl3.setVisible(true);
      list.addListEntry(liEl3);
      assertTrue("isFolder value expected TRUE", liEl3.isFolder());
      liEl3.setFolding(true);
      assertTrue("isFolding value setting TRUE (folder)", liEl3.isFolding());
      liEl3.setFolding(false);
      assertFalse("isFolding value setting FALSE (folder)", liEl3.isFolding());
   }
   
   @Test
   public void test_list_integrity_adding_1 () {
   /* This tests that the following is true:
    * a) Random adding of VISIBLE list elements results in an ordered state of 
    *    repository and display model.
    * b) The methods LayeredList.contains(ListElement) and 
    *    LayeredList.contains(String) produce correct results.
    * c) List elements have correct properties set after insertion.   
    */  
      LayeredList list = createList();
      
      ListElement e1, e2, e3, e4, e5, e6, e7, e8, e9, a1;
      
      e1 = new ListElement("000", null);
      e2 = new ListElement("001", null);
      e3 = new ListElement("000004", "Hans im Glück");
      e4 = new ListElement("000003", "Der Wurzelsepp");       
      e5 = new ListElement("005", null);
      e6 = new ListElement("000001", "Erste Zeile anspitzen");
      e7 = new ListElement("000003002", "Nachträge erfassen");
      e8 = new ListElement("002", "Allgemeine Einträge");
      e9 = new ListElement("000003003", "Nachträge verbessern");
      
      // add new visible elements
      list.addListEntry(e1);
      list.addListEntry(e2);
      list.addListEntry(e3);
      list.addListEntry(e4);
      list.addListEntry(e5);
      list.addListEntry(e6);
      list.addListEntry(e7);
      list.addListEntry(e8);
      list.addListEntry(e9);

      // print out list model (display)
      LayeredList.Model model = (LayeredList.Model)list.getModel();
      for (ListElement e : model.getValues()) {
         System.out.println("   " + e + (e.isFolder() ? ", FOLDER" : ""));
      }

      // test integrity of list model
      assertTrue("list model contains all inserted values", list.getModel().getSize() == 9);
      assertTrue("list model integrity error", model.getElementAt(0) == e1);
      assertTrue("list model integrity error", model.getElementAt(1) == e6);
      assertTrue("list model integrity error", model.getElementAt(2) == e4);
      assertTrue("list model integrity error", model.getElementAt(3) == e7);
      assertTrue("list model integrity error", model.getElementAt(4) == e9);
      assertTrue("list model integrity error", model.getElementAt(5) == e3);
      assertTrue("list model integrity error", model.getElementAt(6) == e2);
      assertTrue("list model integrity error", model.getElementAt(7) == e8);
      assertTrue("list model integrity error", model.getElementAt(8) == e5);
      
      // test integrity of list map (all entries)
      ListElement[] map = list.getValues();
      assertTrue("list map contains all inserted values", map.length == 9);
      assertTrue("list length info", list.getElementCount() == 9);
      assertTrue("list map integrity error", map[0] == e1);
      assertTrue("list map integrity error", map[1] == e6);
      assertTrue("list map integrity error", map[2] == e4);
      assertTrue("list map integrity error", map[3] == e7);
      assertTrue("list map integrity error", map[4] == e9);
      assertTrue("list map integrity error", map[5] == e3);
      assertTrue("list map integrity error", map[6] == e2);
      assertTrue("list map integrity error", map[7] == e8);
      assertTrue("list map integrity error", map[8] == e5);
      
      // test list structure "contains" elements
      assertTrue("list contains element error", list.contains(e8));
      assertTrue("list contains element error", list.contains(e4));
      assertTrue("list contains element error", list.contains(e3));
      assertTrue("list contains element error", list.contains(e9));
      assertTrue("list contains element error", list.contains(e7));
      assertTrue("list contains element error", list.contains(e2));
      assertTrue("list contains element error", list.contains(e5));
      assertTrue("list contains element error", list.contains(e1));
      assertTrue("list contains element error", list.contains(e6));
      a1 = new ListElement("002101", null);
      assertFalse("list-contains must fail on random entry", list.contains(a1));
      
      // test list structure "contains" keys
      assertTrue("list contains key error", list.contains("000003002"));
      assertTrue("list contains key error", list.contains("000004"));
      assertTrue("list contains key error", list.contains("000003003"));
      assertTrue("list contains key error", list.contains("000001"));
      assertTrue("list contains key error", list.contains("000"));
      assertTrue("list contains key error", list.contains("002"));
      assertTrue("list contains key error", list.contains("000003"));
      assertTrue("list contains key error", list.contains("001"));
      assertTrue("list contains key error", list.contains("005"));
      assertFalse("list must not contain never registered key", list.contains("000000"));
      
      // test element properties
      for (ListElement e : map) {
         assertSame("element property LIST true", e.getList(), list);
         assertTrue("element property VISIBLE true", e.isVisible());
         assertTrue("element property SHOWING true", e.isShowing());
         assertFalse("element property FOLDING false", e.isFolding());
         String hi = e.getHierarchy();
         int eLayer = hi.length() / 3;
         assertTrue("element property LAYER", e.getLayer() == eLayer);
         if (eLayer > 1)
            assertNotNull("element property PARENT present: " + e, e.getParent());
         else
            assertNull("element property PARENT null: " + e, e.getParent());
         
      }
   }

   @Test
   public void test_list_integrity_adding_2 () {
   /* This tests that the following is true:
    * a set of new list elements with mixed VISIBILITY settings can be added
    * to a LayeredList with correct distribution to display model and 
    * list repository.
    */  
      LayeredList list = createList();
      
      ListElement e1, e2, e3, e4, e5, e6, a1;
      
      e1 = new ListElement("000", null);
      e2 = new ListElement("001", null);
      e3 = new ListElement("000004", "Hans im Glück");
      e3.setVisible(false);
      e4 = new ListElement("000003", "Der Wurzelsepp");
      e4.setVisible(false);
      e5 = new ListElement("005", null);
      e5.setVisible(false);
      e6 = new ListElement("002", "Allgemeine Einträge");
      
      // add new visible elements
      list.addListEntry(e1);
      list.addListEntry(e2);
      list.addListEntry(e3);
      list.addListEntry(e4);
      list.addListEntry(e5);
      list.addListEntry(e6);

      // print out list model (display)
      LayeredList.Model model = (LayeredList.Model)list.getModel();
      for (ListElement e : model.getValues()) {
         System.out.println("   " + e + (e.isFolder() ? ", FOLDER" : ""));
      }

      // test integrity of list model
      assertTrue("list model contains all inserted values", list.getModel().getSize() == 3);
      assertTrue("list model integrity error", model.getElementAt(0) == e1);
      assertTrue("list model integrity error", model.getElementAt(1) == e2);
      assertTrue("list model integrity error", model.getElementAt(2) == e6);
      
      // test integrity of list map (all entries)
      ListElement[] map = list.getValues();
      assertTrue("list map contains all inserted values", map.length == 6);
      assertTrue("list length info", list.getElementCount() == 6);
      assertTrue("list map integrity error", map[0] == e1);
      assertTrue("list map integrity error", map[1] == e4);
      assertTrue("list map integrity error", map[2] == e3);
      assertTrue("list map integrity error", map[3] == e2);
      assertTrue("list map integrity error", map[4] == e6);
      assertTrue("list map integrity error", map[5] == e5);
      
      // test list structure "contains" elements
      assertTrue("list contains element error", list.contains(e4));
      assertTrue("list contains element error", list.contains(e3));
      assertTrue("list contains element error", list.contains(e2));
      assertTrue("list contains element error", list.contains(e5));
      assertTrue("list contains element error", list.contains(e1));
      assertTrue("list contains element error", list.contains(e6));
      a1 = new ListElement("002101", null);
      assertFalse("list-contains must fail on random entry", list.contains(a1));
      
      // test list structure "contains" keys
//      assertTrue("list contains key error", list.contains("000003002"));
      assertTrue("list contains key error", list.contains("000004"));
//      assertTrue("list contains key error", list.contains("000001"));
      assertTrue("list contains key error", list.contains("000"));
      assertTrue("list contains key error", list.contains("002"));
      assertTrue("list contains key error", list.contains("000003"));
      assertTrue("list contains key error", list.contains("001"));
      assertTrue("list contains key error", list.contains("005"));
      assertFalse("list must not contain never registered key", list.contains("000000"));
      
      // test element properties
      for (ListElement e : map) {
         assertSame("element property LIST true", e.getList(), list);
         assertFalse("element property FOLDING false", e.isFolding());
         if (e == e1 | e == e2 | e == e6) {
            assertTrue("element property VISIBLE true", e.isVisible());
            assertTrue("element property SHOWING true", e.isShowing());
         } else {
            assertFalse("element property VISIBLE false", e.isVisible());
            assertFalse("element property SHOWING false", e.isShowing());
         }
         String hi = e.getHierarchy();
         int eLayer = hi.length() / 3;
         assertTrue("element property LAYER", e.getLayer() == eLayer);
         if (eLayer > 1)
            assertNotNull("element property PARENT present: " + e, e.getParent());
         else
            assertNull("element property PARENT null: " + e, e.getParent());
         
      }
   }

   private void printOut_listModel (LayeredList list) {
      LayeredList.Model model = (LayeredList.Model)list.getModel();
      for (ListElement e : model.getValues()) {
         System.out.println("   " + e + (e.isFolder() ? ", FOLDER" : ""));
      }
   }
   
   
   @Test
   public void test_list_switching_visibility () {
   /* This tests that the following is true:
    * a) changing VISIBILITY of an element to true puts it into the display model
    *    except if one of its ancestors is FOLDED
    * b) changing VISIBILITY of an element to false removes it from display model
    * c) changing V to true of a folder element also puts its children into
    *    the display model, if and only if their VISIBILITY property is true
    * d) changing V to false of a folder also removes its children from display
    *    model, without changing their VISIBILITY property
    * e) changing the VISIBILITY of a FOLDING element has no impact on the FOLDING
    *    property of itself or its children   
    */  
      LayeredList list = createList();
      LayeredList.Model model = (LayeredList.Model)list.getModel();

      ListElement e1, e2, e3, e4, e5, e6, e7, e8, e9;
      
      e1 = new ListElement("000", null); // folder
      e2 = new ListElement("001", null);
      e3 = new ListElement("000004", "Hans im Glück");
      e3.setVisible(false);
      e4 = new ListElement("000003", "Der Wurzelsepp"); // folder
      e4.setVisible(false);
      e5 = new ListElement("005", null); // folder, folding
      e5.setVisible(false);
      e5.setFolding(true);
      e6 = new ListElement("002", "Allgemeine Einträge");
      e7 = new ListElement("000003001", "In den Kordileren");
      e7.setVisible(false);
      e8 = new ListElement("005001", "Am Rio De La Plata");
      e9 = new ListElement("005002", "Winnetou III"); // folding
      e9.setFolding(true);
      
      // add new visible elements
      list.addListEntry(e1);
      list.addListEntry(e2);
      list.addListEntry(e3);
      list.addListEntry(e4);
      list.addListEntry(e5);
      list.addListEntry(e6);
      list.addListEntry(e7);
      list.addListEntry(e8);
      list.addListEntry(e9);

      // print out list model (display)
      printOut_listModel(list);
      
      // switching VISIBILITY 1
      e4.setVisible(true);
      e5.setVisible(true);
      
      // test avoid display of folded element
      e8.setVisible(false);
      e8.setVisible(true);
      assertFalse("model should not contain element (folded parent)", model.contains(e8));
      
      // test integrity of list model
      assertTrue("list model contains all inserted values", model.getSize() == 5);
      assertTrue("list model integrity error", model.getElementAt(0) == e1);
      assertTrue("list model integrity error", model.getElementAt(1) == e4);
      assertTrue("list model integrity error", model.getElementAt(2) == e2);
      assertTrue("list model integrity error", model.getElementAt(3) == e6);
      assertTrue("list model integrity error", model.getElementAt(4) == e5);
      
      // test integrity of list map (all entries)
      ListElement[] map = list.getValues();
      assertTrue("list map contains all inserted values", map.length == 9);
      assertTrue("list length info", list.getElementCount() == 9);
      assertTrue("list map integrity error", map[0] == e1);
      assertTrue("list map integrity error", map[1] == e4);
      assertTrue("list map integrity error", map[2] == e7);
      assertTrue("list map integrity error", map[3] == e3);
      assertTrue("list map integrity error", map[4] == e2);
      assertTrue("list map integrity error", map[5] == e6);
      assertTrue("list map integrity error", map[6] == e5);
      assertTrue("list map integrity error", map[7] == e8);
      assertTrue("list map integrity error", map[8] == e9);
      
      // test element properties
      for (ListElement e : map) {
         assertSame("element property LIST true", e.getList(), list);
         if (e == e5 | e == e9) {
            assertTrue("element property FOLDING true", e.isFolding());
         } else {
            assertFalse("element property FOLDING false", e.isFolding());
         }
         if (e == e1 | e == e4 | e == e5) {
            assertTrue("element property FOLDER true", e.isFolder());
         } else {
            assertFalse("element property FOLDER false", e.isFolder());
         }
         if (e == e3 | e == e7) {
            assertFalse("element property VISIBLE false", e.isVisible());
         } else {
            assertTrue("element property VISIBLE true", e.isVisible());
         }
         if (e == e3 | e == e7 | e == e8 | e == e9) {
            assertFalse("element property SHOWING false", e.isShowing());
         } else {
            assertTrue("element property SHOWING true", e.isShowing());
         }
         String hi = e.getHierarchy();
         int eLayer = hi.length() / 3;
         assertTrue("element property LAYER", e.getLayer() == eLayer);
         if (eLayer > 1)
            assertNotNull("element property PARENT present: " + e, e.getParent());
         else
            assertNull("element property PARENT null: " + e, e.getParent());
      }
      
      // print out list model (display)
      printOut_listModel(list);
      
      // switching VISIBILITY 2
      e3.setVisible(true);
      e7.setVisible(true);
      
      // test integrity of list model
      assertTrue("list model contains all inserted values", list.getModel().getSize() == 7);
      assertTrue("list model integrity error", model.getElementAt(0) == e1);
      assertTrue("list model integrity error", model.getElementAt(1) == e4);
      assertTrue("list model integrity error", model.getElementAt(2) == e7);
      assertTrue("list model integrity error", model.getElementAt(3) == e3);
      assertTrue("list model integrity error", model.getElementAt(4) == e2);
      assertTrue("list model integrity error", model.getElementAt(5) == e6);
      assertTrue("list model integrity error", model.getElementAt(6) == e5);
      
      // test integrity of list map (all entries)
      map = list.getValues();
      assertTrue("list map contains all inserted values", map.length == 9);
      assertTrue("list length info", list.getElementCount() == 9);
      assertTrue("list map integrity error", map[0] == e1);
      assertTrue("list map integrity error", map[1] == e4);
      assertTrue("list map integrity error", map[2] == e7);
      assertTrue("list map integrity error", map[3] == e3);
      assertTrue("list map integrity error", map[4] == e2);
      assertTrue("list map integrity error", map[5] == e6);
      assertTrue("list map integrity error", map[6] == e5);
      assertTrue("list map integrity error", map[7] == e8);
      assertTrue("list map integrity error", map[8] == e9);

      // print out list model (display)
      printOut_listModel(list);
      
      // switching VISIBILITY 3
      e4.setVisible(false);
      e6.setVisible(false);
      e5.setVisible(false);
      
      // test integrity of list model
      assertTrue("list model contains correct values", list.getModel().getSize() == 3);
      assertTrue("list model integrity error", model.getElementAt(0) == e1);
      assertTrue("list model integrity error", model.getElementAt(1) == e3);
      assertTrue("list model integrity error", model.getElementAt(2) == e2);
      
      // test integrity of list map (all entries)
      map = list.getValues();
      assertTrue("list map contains all inserted values", map.length == 9);
      assertTrue("list length info", list.getElementCount() == 9);
      assertTrue("list map integrity error", map[0] == e1);
      assertTrue("list map integrity error", map[1] == e4);
      assertTrue("list map integrity error", map[2] == e7);
      assertTrue("list map integrity error", map[3] == e3);
      assertTrue("list map integrity error", map[4] == e2);
      assertTrue("list map integrity error", map[5] == e6);
      assertTrue("list map integrity error", map[6] == e5);
      assertTrue("list map integrity error", map[7] == e8);
      assertTrue("list map integrity error", map[8] == e9);
      
      // print out list model (display)
      printOut_listModel(list);
      
      // test element properties
      for (ListElement e : map) {
         assertSame("element property LIST true", e.getList(), list);
         if (e == e5 | e == e9) {
            assertTrue("element property FOLDING true", e.isFolding());
         } else {
            assertFalse("element property FOLDING false", e.isFolding());
         }
         if (e == e1 | e == e4 | e == e5) {
            assertTrue("element property FOLDER true", e.isFolder());
         } else {
            assertFalse("element property FOLDER false", e.isFolder());
         }
         if (e == e4 | e == e5 | e == e6) {
            assertFalse("element property VISIBLE false", e.isVisible());
         } else {
            assertTrue("element property VISIBLE true", e.isVisible());
         }
         if (e == e4 | e == e5 | e == e6 | e == e7 | e == e8 | e == e9) {
            assertFalse("element property SHOWING false", e.isShowing());
         } else {
            assertTrue("element property SHOWING true", e.isShowing());
         }

         String hi = e.getHierarchy();
         int eLayer = hi.length() / 3;
         assertTrue("element property LAYER", e.getLayer() == eLayer);
         if (eLayer > 1)
            assertNotNull("element property PARENT present: " + e, e.getParent());
         else
            assertNull("element property PARENT null: " + e, e.getParent());
      }
      
      // switching VISIBILITY 4
      e4.setVisible(true);

      // test integrity of list model
      assertTrue("list model contains correct values", list.getModel().getSize() == 5);
      assertTrue("list model integrity error", model.getElementAt(0) == e1);
      assertTrue("list model integrity error", model.getElementAt(1) == e4);
      assertTrue("list model integrity error", model.getElementAt(2) == e7);
      assertTrue("list model integrity error", model.getElementAt(3) == e3);
      assertTrue("list model integrity error", model.getElementAt(4) == e2);
      
      // print out list model (display)
      printOut_listModel(list);
      
   }

   @Test
   public void test_list_switching_showing () {
   /* This tests that the following is true:
    * a) changing SHOWING of an element to true puts it into the display model
    *    and sets the ancestor of this element also to SHOWING 
    *    
    * b) changing SHOWING of an element to false removes it from display model
    * 
    * c) changing S to true of a folder element also puts its children into
    *    the display model, if and only if their VISIBILITY property is true
    *    
    * d) changing S to false of a folder also removes its children from display
    *    model
    *    
    * e) changing SHOWING property of an element with VISIBILITY set to false
    *    has no effect   
    *    
    * f) changing the SHOWING of a FOLDING element has no impact on the FOLDING
    *    property of itself or its children   
    */  
      LayeredList list = createList();
      LayeredList.Model model = (LayeredList.Model)list.getModel();
   
      ListElement e1, e2, e3, e4, e5, e6, e7, e8, e9;
      
      e1 = new ListElement("000", null); // folder
      e2 = new ListElement("001", null);
      e3 = new ListElement("000004", "Hans im Glück");
      e3.setVisible(false);
      e4 = new ListElement("000003", "Der Wurzelsepp"); // folder
      e5 = new ListElement("005", null); // folder, folding
      e5.setFolding(true);
      e6 = new ListElement("002", "Allgemeine Einträge");
      e7 = new ListElement("000003001", "In den Kordileren");
      e7.setVisible(false);
      e8 = new ListElement("005001", "Am Rio De La Plata");
      e9 = new ListElement("005002", "Winnetou III");  // folding
      e9.setFolding(true);
      
      // add new visible elements
      list.addListEntry(e1);
      list.addListEntry(e2);
      list.addListEntry(e3);
      list.addListEntry(e4);
      list.addListEntry(e5);
      list.addListEntry(e6);
      list.addListEntry(e7);
      list.addListEntry(e8);
      list.addListEntry(e9);
   
      // print out list model (display)
      printOut_listModel(list);
      
      // test statement e), b)
      assertFalse("SHOWING of an INVISIBLE element must be false", e3.isShowing());
      e3.setShowing(true);
      assertFalse("Set SHOWING of an INVISIBLE element does not take place", e3.isShowing());
      assertFalse("SHOWING of an INVISIBLE element must be false", e7.isShowing());
      e7.setShowing(true);
      assertFalse("Set SHOWING of an INVISIBLE element does not take place", e7.isShowing());
      
      // test showing of a simple element, b)
      assertTrue("condition not met", e2.isShowing());
      e2.setShowing(false);
      assertFalse("element NOT SHOWING fails", e2.isShowing());
      assertFalse("model should not contain element", model.contains(e2));
      e2.setShowing(true);
      assertTrue("element NOT SHOWING fails", e2.isShowing());
      assertTrue("model should contain element", model.contains(e2));
      
      // test f)
      assertTrue("element should be FOLDING (condition)", e5.isFolding());
      e5.setShowing(false);
      assertFalse("element should not be FOLDING", e8.isFolding());
      assertTrue("element should be FOLDING", e9.isFolding());
      e5.setShowing(true);
      assertTrue("element should be FOLDING (after SHOWING = true)", e5.isFolding());
      assertFalse("element should not be FOLDING", e8.isFolding());
      assertTrue("element should be FOLDING", e9.isFolding());
      
      // test a)
      e5.setShowing(false);
      assertTrue("element should be FOLDING (after SHOWING = false)", e5.isFolding());
      assertFalse("element should not be showing", e8.isShowing());
      assertFalse("model should not contain not-showing element", model.contains(e8));
      assertFalse("element should not be showing", e5.isShowing());
      assertFalse("model should not contain not-showing element", model.contains(e5));
      e8.setShowing(true);
      assertTrue("element should be showing after SHOWING = true", e8.isShowing());
      assertTrue("model should contain showing element", model.contains(e8));
      assertTrue("element should be showing (ancestor)", e5.isShowing());
      assertTrue("model should contain showing element (ancestor)", model.contains(e5));
      
      // test c)
      e1.setShowing(false);
      assertFalse("element should not be showing", e3.isShowing());
      assertFalse("element should not be showing", e4.isShowing());
      assertFalse("model should not contain not-showing element", model.contains(e3));
      assertFalse("model should not contain not-showing element", model.contains(e4));
      e1.setShowing(true);
      assertFalse("element should not be showing (INVISIBLE)", e3.isShowing());
      assertTrue("element should be showing", e4.isShowing());
      assertFalse("model should not contain INVISIBLE element", model.contains(e3));
      assertTrue("model should contain showing element", model.contains(e4));
      
      // test d)
      e7.setVisible(true);
      e7.setShowing(true);
      assertTrue("element should be showing", e7.isShowing());
      assertTrue("model should contain showing element", model.contains(e7));
      assertTrue("element should be showing", e4.isShowing());
      assertTrue("model should contain showing element", model.contains(e4));
      assertTrue("element should be showing", e1.isShowing());
      assertTrue("model should contain showing element", model.contains(e1));
      e1.setShowing(false);
      assertFalse("element should not be showing", e7.isShowing());
      assertFalse("model should not contain not-showing element", model.contains(e7));
      assertFalse("element should not be showing", e4.isShowing());
      assertFalse("model should not contain not-showing element", model.contains(e4));
      assertFalse("element should not be showing", e1.isShowing());
      assertFalse("model should not contain not-showing element", model.contains(e1));
      
      // test integrity of list map (all entries)
      ListElement[] map = list.getValues();
      assertTrue("list map contains all inserted values", map.length == 9);
      assertTrue("list length info", list.getElementCount() == 9);
      assertTrue("list map integrity error", map[0] == e1);
      assertTrue("list map integrity error", map[1] == e4);
      assertTrue("list map integrity error", map[2] == e7);
      assertTrue("list map integrity error", map[3] == e3);
      assertTrue("list map integrity error", map[4] == e2);
      assertTrue("list map integrity error", map[5] == e6);
      assertTrue("list map integrity error", map[6] == e5);
      assertTrue("list map integrity error", map[7] == e8);
      assertTrue("list map integrity error", map[8] == e9);
      
      // print out list model (display)
      printOut_listModel(list);
      
   }
   
   private LayeredList createList () {
      LayeredList list = new LayeredList();
      list.setPrintHierarchy(true);
      return list;
   }
   
   @Test
   public void test_list_folding () {
   /* This tests that the following is true:
    * a) changing FOLDING of an element to true removes all its descendant 
    *    elements from the display model, without changing their FOLDING 
    *    property
    *    
    * b) changing FOLDING of an element to false adds all its descendant elements
    *    to the display model if they have no ancestor with FOLDING == true, 
    *    and without changing their FOLDING property
    *    
    * c) the FOLDING property has no influence on the display status of the
    *    bearing element
    *    
    * d) adding an element with a hierarchy below a folded element does not
    *    put it into the display model
    *    
    * e) adding an element with FOLDING == true removes other elements from 
    *    display whose hierarchy is below the added element
    *    
    * f) adding an element with FOLDING == false puts other elements into the 
    *    display model whose hierarchy is below the added element and which 
    *    have no ancestor with FOLDING == true
    */  
      LayeredList list = createList();
      LayeredList.Model model = (LayeredList.Model)list.getModel();
   
      ListElement e1, e3, e4, e6, e7, e10, e11, e12;
      
      e1 = new ListElement("000", null); // folder, folding
      e1.setFolding(true);
      e3 = new ListElement("000004", "Hans im Glück");
      e4 = new ListElement("000003", "Der Wurzelsepp"); // folder
      e10 = new ListElement("000005", "Wagners Opern"); // folder, folding
      e10.setFolding(true);
      e6 = new ListElement("002", "Allgemeine Einträge");
      e7 = new ListElement("000003001", "In den Kordileren");
      e11 = new ListElement("000005001", "Rienzi");
      e12 = new ListElement("000005002", "Walküre");
      
      // add new visible elements
      list.addListEntry(e3);
      list.addListEntry(e4);
      list.addListEntry(e10);
      list.addListEntry(e11);
      list.addListEntry(e6);
      list.addListEntry(e7);
   
      // print out list model (display)
      printOut_listModel(list);
      
      // test e)
      assertTrue("initial element count", list.getElementCount() == 6);
      assertTrue("initial element display", model.getSize() == 1);

      list.addListEntry(e1);
      assertTrue("element count after insert FOLDED folder", list.getElementCount() == 7);
      assertTrue("display element count after insert FOLDED folder", model.getSize() == 2);
      assertTrue("contains e1 after insert FOLDED folder", model.contains(e1));
      assertTrue("contains e6 after insert FOLDED folder", model.contains(e6));
      
      // print out list model (display)
      printOut_listModel(list);
      
      // test b) c)
      e1.setFolding(false);
      assertTrue("folding should not modify display status", model.contains(e1));
      assertTrue("element count after setting folder FOLDING = true", list.getElementCount() == 7);
      assertTrue("element display after setting folder FOLDING = true", model.getSize() == 6);
      assertFalse("FOLDING property unchanged, test b)", e3.isFolding());
      assertFalse("FOLDING property unchanged, test b)", e4.isFolding());
      assertFalse("FOLDING property unchanged, test b)", e11.isFolding());
      assertTrue("FOLDING property unchanged, test b)", e10.isFolding());
      
      // test d)
      list.addListEntry(e12);
      assertFalse("entry should not be SHOWING", e12.isShowing());
      assertFalse("entry should not display", model.contains(e12));
      assertTrue("element count after setting folder FOLDING = false", list.getElementCount() == 8);
      assertTrue("element display after setting folder FOLDING = false", model.getSize() == 6);
      
      // print out list model (display)
      printOut_listModel(list);
      
      // test a) c)
      e1.setFolding(true);
      assertTrue("folding should not modify display status", model.contains(e1));
      assertTrue("element count after setting folder FOLDING = false", list.getElementCount() == 8);
      assertTrue("element display after setting folder FOLDING = false", model.getSize() == 2);
      assertFalse("FOLDING property unchanged, test a)", e3.isFolding());
      assertFalse("FOLDING property unchanged, test a)", e4.isFolding());
      assertFalse("FOLDING property unchanged, test a)", e11.isFolding());
      assertTrue("FOLDING property unchanged, test a)", e10.isFolding());
      
      // print out list model (display)
      printOut_listModel(list);
      
      // test f)
      list.removeListEntry(e1);
      assertTrue("element count after remove entry", list.getElementCount() == 7);
      assertTrue("element display count after remove entry (folded folder)", model.getSize() == 1);
      e1.setFolding(false);
      assertTrue("element display count", model.getSize() == 1);
      list.addListEntry(e1);
      assertTrue("element count after entry of FOLDER (not folding)", list.getElementCount() == 8);
      assertTrue("element display count after entry of FOLDER (not folding)", model.getSize() == 6);
      
      // print out list model (display)
      printOut_listModel(list);
   }
   
   @Test
   public void test_list_removing () {
      /* This tests that the following is true:
       * a) if an element is removed from the list it is subtracted from the
       *    display model and removed from the element repository
       *    
       * b) if a folder element is removed, any descendant elements are 
       *    subtracted from the display model (but remain in the repository)
       *    
       * c) if an element is removed, its 'parent' property is set to null and
       *    any children lose their 'parent' property to null
      */
      LayeredList list = createList();
      LayeredList.Model model = (LayeredList.Model)list.getModel();
   
      ListElement e1, e2, e3, e4, e5, e6, e7, e8, e9, e10;
      
      e1 = new ListElement("000", null); // folder
      e2 = new ListElement("001", null);
      e3 = new ListElement("000004", "Hans im Glück");
      e3.setVisible(false);
      e4 = new ListElement("000003", "Der Wurzelsepp"); // folder
      e5 = new ListElement("005", null); // folder, folding
      e5.setFolding(true);
      e6 = new ListElement("002", "Allgemeine Einträge");
      e7 = new ListElement("000003001", "In den Kordileren");
      e10 = new ListElement("000003002", "Der Ölprinz");
      e8 = new ListElement("005001", "Am Rio De La Plata");
      e9 = new ListElement("005002", "Winnetou III");  // folding
      e9.setFolding(true);
      
      // add new visible elements
      list.addListEntry(e1);
      list.addListEntry(e2);
      list.addListEntry(e3);
      list.addListEntry(e4);
      list.addListEntry(e5);
      list.addListEntry(e6);
      list.addListEntry(e7);
      list.addListEntry(e8);
      list.addListEntry(e9);
      list.addListEntry(e10);
   
      // print out list model (display)
      printOut_listModel(list);
      
//      assertTrue("folding should not modify display status", model.contains(e1));
      assertTrue("initial element count", list.getElementCount() == 10);
      assertTrue("initial element display", model.getSize() == 7);

      // test a)
      // remove a non-showing element (folded folder)
      list.removeListEntry(e8);
      assertTrue("initial element count", list.getElementCount() == 9);
      assertTrue("initial element display", model.getSize() == 7);
      assertFalse("element should not be in display", model.contains(e8));
      assertFalse("element should not be in list", list.contains(e8));
      assertFalse("element SHOWING property", e8.isShowing());
      
      // remove INVISIBLE element
      list.removeListEntry(e3);
      assertTrue("initial element count", list.getElementCount() == 8);
      assertTrue("initial element display", model.getSize() == 7);
      assertFalse("element should not be in display", model.contains(e3));
      assertFalse("element should not be in list", list.contains(e3));
      assertFalse("element SHOWING property", e3.isShowing());
      assertFalse("element VISIBLE property", e3.isVisible());
      
      // remove plain level-1 element
      list.removeListEntry(e2);
      assertTrue("initial element count", list.getElementCount() == 7);
      assertTrue("initial element display", model.getSize() == 6);
      assertFalse("element should not be in display", model.contains(e2));
      assertFalse("element should not be in list", list.contains(e2));
      assertFalse("element SHOWING property", e2.isShowing());
      assertTrue("element VISIBLE property", e2.isVisible());
      
      // remove showing level-3 element
      assertNotNull("element PARENT should not be null", e7.getParent());
      list.removeListEntry(e7);
      assertTrue("initial element count", list.getElementCount() == 6);
      assertTrue("initial element display", model.getSize() == 5);
      assertFalse("element should not be in display", model.contains(e7));
      assertFalse("element should not be in list", list.contains(e7));
      assertFalse("element SHOWING property", e7.isShowing());
      assertTrue("element VISIBLE property", e7.isVisible());
      assertNull("element PARENT should be null", e7.getParent());
      
      // test b)
      list.addListEntry(e7);
      assertTrue("element should be in display, condition", model.contains(e4));
      assertTrue("element should be in display, condition", model.contains(e7));
      assertTrue("element should be in display, condition", model.contains(e10));

      list.removeListEntry(e1);
      assertFalse("element should not be in display", model.contains(e4));
      assertTrue("element should be in list", list.contains(e4));
      assertFalse("element should not be in display", model.contains(e7));
      assertTrue("element should be in list", list.contains(e7));
      assertFalse("element should not be in display", model.contains(e10));
      assertTrue("element should be in list", list.contains(e10));
      
      
      // print out list model (display)
      printOut_listModel(list);
      
   }

   @Test
   public void test_list_getFolderElements () {
   /* This tests that the following is true:
    * The methods LayeredList.getFolderElements() return the sets
    * of list elements as described.
    */  
      LayeredList list = createList();
      ListElement e1, e2, e3, e4, e5, e6, e7, e8, e9, e10;
      
      e1 = new ListElement("000", null); // folder
      e2 = new ListElement("001", null);
      e3 = new ListElement("000004", "Hans im Glück");
      e3.setVisible(false);
      e4 = new ListElement("000003", "Der Wurzelsepp"); // folder
      e5 = new ListElement("005", null); // folder, folding
      e5.setFolding(true);
      e6 = new ListElement("002", "Allgemeine Einträge");
      e7 = new ListElement("000003001", "In den Kordileren");
      e7.setVisible(false);
      e8 = new ListElement("000003002", "Am Rio De La Plata");
      e9 = new ListElement("005002", "Winnetou III"); // folding
      e9.setFolding(true);
      e10 = new ListElement("000005", "Das Dekameron");
      
      // add new visible elements
      list.addListEntry(e1);
      list.addListEntry(e2);
      list.addListEntry(e3);
      list.addListEntry(e4);
      list.addListEntry(e5);
      list.addListEntry(e6);
      list.addListEntry(e7);
      list.addListEntry(e8);
      list.addListEntry(e9);
      list.addListEntry(e10);
   
      // test direct children scan I
      ListElement[] arr = LayeredList.getFolderElements(list, "000");
      assertTrue("getFolderElements direct children value", arr.length == 2);
      
      // test descendant scan I
      arr = LayeredList.getFolderElements(list, "000", true);
      assertTrue("getFolderElements all descendants value", arr.length == 3);
      
      // print out list model (display)
      printOut_listModel(list);
      
      // switching VISIBILITY 2
      e3.setVisible(true);
      e7.setVisible(true);
      
      // test direct children scan I
      arr = LayeredList.getFolderElements(list, "000");
      assertTrue("getFolderElements direct children value", arr.length == 3);
      
      // test descendant scan I
      arr = LayeredList.getFolderElements(list, "000", true);
      assertTrue("getFolderElements all descendants value", arr.length == 5);
      
      // print out list model (display)
      printOut_listModel(list);
   }

   @Test
   public void test_list_parentless_folder_element () {
   /* This tests that the following is true:
    * a) elements added to the list whose hierarchy do not match a parent
    *    in the repository are not put into the display model  
    * b) if a folder element is added to the list, any descendants that are
    *    enabled to display under this folder are put into the display model.
    * c) list elements in the repository cannot be brought to display model
    *    if they have no existing parent (in the repository).   
    */
      LayeredList list = createList();
      LayeredList.Model model = (LayeredList.Model)list.getModel();
   
      ListElement e1, e2, e3, e4, e5, e6, e7, e8, e9, e10;
      
      e1 = new ListElement("000", null); // folder
      e2 = new ListElement("001", null);
      e3 = new ListElement("000004", "Hans im Glück");
      e3.setVisible(false);
      e4 = new ListElement("000003", "Der Wurzelsepp"); // folder
      e5 = new ListElement("005", null); // folder, folding
      e5.setFolding(true);
      e6 = new ListElement("002", "Allgemeine Einträge");
      e7 = new ListElement("000003001", "In den Kordileren");
      e10 = new ListElement("000003002", "Der Ölprinz");
      e8 = new ListElement("005001", "Am Rio De La Plata");
      e9 = new ListElement("005002", "Winnetou III");  // folding
      e9.setFolding(true);
      
      // add new visible elements
//         list.addListEntry(e1);
      list.addListEntry(e2);
      list.addListEntry(e3);
      list.addListEntry(e4);
      list.addListEntry(e5);
      list.addListEntry(e6);
      list.addListEntry(e7);
      list.addListEntry(e8);
      list.addListEntry(e9);
      list.addListEntry(e10);
   
      // print out list model (display)
      printOut_listModel(list);
      
//      assertTrue("folding should not modify display status", model.contains(e1));
//         assertTrue("initial element count", list.getElementCount() == 10);
//         assertTrue("initial element display", model.getSize() == 7);

      // test a)
      assertTrue("element not in repository", list.contains(e3));
      assertFalse("element in display model", model.contains(e3));
      assertFalse("element SHOWING property", e3.isShowing());
      assertTrue("element not in repository", list.contains(e4));
      assertFalse("element in display model", model.contains(e4));
      assertFalse("element SHOWING property", e4.isShowing());
      assertTrue("element not in repository", list.contains(e7));
      assertFalse("element in display model", model.contains(e7));
      assertFalse("element SHOWING property", e7.isShowing());

      assertTrue("initial element count", list.getElementCount() == 9);
      assertTrue("initial element display", model.getSize() == 3);
      
      // test c)
      e3.setVisible(true);
      assertFalse("element in display model", model.contains(e3));
      assertFalse("element SHOWING property", e3.isShowing());
      e4.setShowing(true);
      assertFalse("element in display model", model.contains(e4));
      assertFalse("element SHOWING property", e4.isShowing());
      
      assertTrue("initial element count", list.getElementCount() == 9);
      assertTrue("initial element display", model.getSize() == 3);
      
      // test b)
      list.addListEntry(e1);
      assertTrue("element count after folder insertion", list.getElementCount() == 10);
      assertTrue("element display count after folder insertion", model.getSize() == 8);
  
         // print out list model (display)
      printOut_listModel(list);
      
   }
}
