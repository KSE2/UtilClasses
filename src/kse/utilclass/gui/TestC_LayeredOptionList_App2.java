package kse.utilclass.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.junit.Test;

import kse.utilclass.gui.LayeredOptionList.ListElement;


public class TestC_LayeredOptionList_App2 {

   static public LayeredOptionList testList;
   private JPanel primaryPanel;
   
 private JFrame createTestFrame () {
    JFrame frame = new JFrame();
    frame.setTitle("LayeredList TEST-APP-1");
    
    primaryPanel = new JPanel(new BorderLayout());
    frame.getContentPane().add( primaryPanel, BorderLayout.CENTER );

    return frame;
 }
   
private Component getListComponent (LayeredList list, Dimension dim) {
   JScrollPane scp = new JScrollPane(list);
   scp.setPreferredSize(dim);
   return scp;
}


@Test
 public void test_application_1 () {
    JFrame frame = createTestFrame();
    LayeredOptionList list = getTestList_1();
    testList = list;

    primaryPanel.add(getListComponent(list, new Dimension(400, 500)), BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
    delay(60000);
 }

private LayeredOptionList getTestList_1 () {
   LayeredOptionList list = new LayeredOptionList();
//   list.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
   Border bo2 = BorderFactory.createMatteBorder(10, 10, 10, 10, Color.cyan);
   Border bo1 = BorderFactory.createEmptyBorder(10, 10, 10, 10);
   list.setBorder(BorderFactory.createCompoundBorder(bo2, bo1));
   list.setPrintHierarchy(true);
   list.setCheckboxFolders(true);
   list.setPreciseHit(true);
//   list.setFolderIcon(null, false);
//   list.setEnabled(false);
   
   ListElement e1, e2, e3, e4, e5, e6, e7, e8, e9, e10;
   
   e1 = new ListElement("000", null);
   e2 = new ListElement("001", null);
   e3 = new ListElement("000004", "Hans im Glück");
   e4 = new ListElement("000003", "Der Wurzelsepp");       
   e5 = new ListElement("005", null);
   e5.setFolding(true);
   e6 = new ListElement("000001", "Erste Zeile anspitzen", true);
   e7 = new ListElement("000003002", "Nachträge erfassen");
   e8 = new ListElement("002", "Allgemeine Einträge");
   e9 = new ListElement("000003003", "Nachträge verbessern");
   e10 = new ListElement("005001", "Eine Sultana in Whana", true);
   
   // add new visible elements
   list.addListEntry(e1);
   list.addListEntry(e2);
   list.addListEntry(e3);
   list.addListEntry(e4);
   list.addListEntry(e5);
   list.addListEntry(e6);
//   list.addListEntry(e7);
//   list.addListEntry(e8);
//   list.addListEntry(e9);
//   list.addListEntry(e10);

   LayeredOptionList.ListElement[] arr = new LayeredOptionList.ListElement[]
   { e5, e7, e8, e9, e10 };
   list.addValues(arr);
   
   // set ENABLED features
   e3.setEnabled(false);
   e4.setEnabled(false);
   
   return list;
}
 
private void delay (int time) {
   try { Thread.sleep(time); }
   catch (Exception e) {
      e.printStackTrace();
   }
}
}
