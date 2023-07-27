/*
*  File: HtmlDialogPanel.java
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

package kse.utilclass.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkListener;

/**
 *  A <code>JPanel</code> containing an optionally scrollable HTML browsing 
 *  facility based on <code>JEditorPane</code>. The default character set
 *  for the content-type is ISO-8859-1.
 */
public class HtmlDialogPanel extends JPanel {
   
   private JEditorPane editor;
   private JScrollPane scroll;
   private String charset = "ISO-8859-1";
   private boolean scrollable = true;
   
/**
 * Constructs an empty scrollable HTML display panel for the default 
 * character set. 
 */
public HtmlDialogPanel () {
   super();
   init();
}

/**
 * Constructs an empty HTML display panel whose scrollability can be set
 * by parameter.
 * 
 * @param scrollable boolean whether display will be scrollable
 */
public HtmlDialogPanel (boolean scrollable) {
	this(null, scrollable);
}

/** Constructs a scrollable HTML display panel with initial display text.
 * 
 * @param charset String name of text encoding character set, null for default
 */
public HtmlDialogPanel (String charset) {
	this(charset, true);
}

/** Constructs a scrollable HTML display panel with initial display text.
 * 
 * @param charset String name of text encoding character set, null for default
 * @param scrollable boolean whether display will be scrollable
 */
public HtmlDialogPanel (String charset, boolean scrollable) {
   super();
   this.scrollable = scrollable;
   if (charset != null) {
	   this.charset = charset;
   }
   init();
}

/** Constructs a scrollable HTML display panel with initial display text
 *  fetched from the given URL.
 * 
 * @param url URL initial page to be displayed
 * @throws IOException if the given url cannot be accessed
 */
public HtmlDialogPanel (URL url) throws IOException {
	this();
    editor.setPage(url);
}

private void init () {
	Component component;
	setLayout(new BorderLayout());
   
   editor = new JEditorPane();
   editor.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 0));
   editor.setEditable(false);
   editor.setBackground(Color.white);
   editor.setContentType( "text/html;charset=" + charset );
   component = editor;
   
   if (scrollable) {
      scroll = new JScrollPane( editor );
      scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); 
      component = scroll;
   }
   
   this.add(component, BorderLayout.CENTER);
}

public JScrollPane getScrollPane () {
   return scroll;
}

public JEditorPane getEditorPane () {
   return editor;
}

public void setText (String text) {
   editor.setText( text );
}

public void setPage (URL page) throws IOException {
   editor.setPage(page);
}

public void addHyperlinkListener( HyperlinkListener hyperListener ) {
   editor.addHyperlinkListener( hyperListener );
}

public void removeHyperlinkListener( HyperlinkListener hyperListener ) {
   editor.removeHyperlinkListener( hyperListener );
}

//@Override
//public void setBackground(Color bg) {
//	super.setBackground(bg);
//	if (editor != null) {
//	   editor.setBackground(bg);
//	}
//}
//
//@Override
//public void setForeground(Color bg) {
//	super.setForeground(bg);
//	if (editor != null) {
//	   editor.setForeground(bg);
//	}
//}


}
