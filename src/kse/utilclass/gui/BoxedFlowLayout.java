package kse.utilclass.gui;

/*
*  File: BoxedFlowLayout.java
* 
*  Project JUtilClasses
*  @author Wolfgang Keller et al.
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import kse.utilclass.misc.Log;
import kse.utilclass.misc.Util;

/**
 * A {@code Layout} which arranges added components in a grid style with a
 * user defined amount of columns, where each column has the width of its 
 * widest component and each row has the height of its tallest component. 
 * The columns of a layout can have different widths and the rows different 
 * heights. This makes the layout compact and looking clean, ordering
 * components in a table-kind design.
 * <p>Row are automatically added as needed by the amount of components given.
 * The layout adapts dynamically to modified properties both in the layed-out
 * container and the layout itself. The only fix parameter of a layout is the
 * number of columns.
 * 
 * <p>Features of the Layout:
 * <br>HGAP: int number of pixels between horizontal neighbouring components 
 * <br>VGAP: int number of pixels between vertical neighbouring components 
 * <br>VFILL (Vertical Fill): boolean if <b>true</b> all components of a row
 *           will get the same height
 * <br>HFILL (Horizontal Fill): boolean if <b>true</b> all components of a 
 *           column will get the same width.
 * <br>HALIGN (Alignment): horizontal orientation of components (LEFT, CENTER, 
 * RIGHT)
 * <br>VALIGN (Alignment): vertical orientation of components (TOP, CENTER, 
 * BOTTOM)
 * 
 * @author Wolfgang Keller
 */
public class BoxedFlowLayout implements LayoutManager {
	
   public static final int LEFT = 0;
   public static final int CENTER = 1;
   public static final int RIGHT = 2;
   public static final int TOP = 0;
   public static final int BOTTOM = 2;
   
   private int vgap;
   private int hgap;
   private int cols, rows;
   private boolean hfill, vfill;
   private int hAlign = LEFT;
   private int vAlign = CENTER;
   
   private int[] colWidth, rowHeight;
   private int[] colX, rowY;

  /**
   * Creates a BoxedFlowLayout with HALIGN=LEFT, VALIGN=CENTER, 
   * VGAP=3, HGAP=3 and no "Fill" functions.
   * 
   * @param cols int number of columns
   */
  public BoxedFlowLayout (int cols) {
    this(cols, 3, 3);
  }

  /**
   * Creates a BoxedFlowLayout with HALIGN=LEFT, VALIGN=CENTER, the 
   * specified HGAP and VGAP and no "Fill" functions.
   * 
   * @param cols int number of columns
   * @param hgap int gap between columns in pixel
   * @param vgap int gap between rows in pixel
   * @throws IllegalArgumentException if 'cols' is below 1
   */
  public BoxedFlowLayout (int cols, int hgap, int vgap) {
	 this(cols, hgap, vgap, false, false);  
  }
  
  /**
   * Creates a BoxedFlowLayout with HALIGN=LEFT, VALIGN=CENTER and the specified 
   * HGAP, VGAP and "Fill" functions.
   * 
   * @param cols int number of columns
   * @param hgap int gap between columns in pixel
   * @param vgap int gap between rows in pixel
   * @param hfill boolean whether horizontal fill is performed
   * @param vfill boolean whether vertical fill is performed
   * @throws IllegalArgumentException if 'cols' is below 1
   */
  public BoxedFlowLayout (int cols, int hgap, int vgap, boolean hfill, boolean vfill) {
	 if (cols < 1)
		 throw new IllegalArgumentException("illegal columns value: " + cols);
     this.cols = cols;
     this.hgap = Math.max(0, hgap);
     this.vgap = Math.max(0, vgap);
     this.hfill = hfill;
     this.vfill = vfill;
  }

  /**
   * Sets the VGAP property of this layout manager.
   *  
   * @param v int VGAP in pixels
   */
  public void setVgap (int v) {
	 Util.requirePositive(v);
     this.vgap = v;
  }

  /**
   * Sets the HGAP property of this layout manager.
   *  
   * @param v int HGAP in pixels
   */
  public void setHgap (int v) {
	 Util.requirePositive(v);
     this.hgap = v;
  }

  public int getVgap () {return vgap;}
  
  public int getHgap () {return hgap;}
  
  public int getCols () {return cols;}
  
  public int getRows () {return rows;}
  
  public void setHorizontalFill ( boolean v ) {
     hfill = v;
  }
	  
  public void setVerticalFill ( boolean v ) {
     vfill = v;
  }
  
  public boolean getHorizontalFill () {return hfill;}
  
  public boolean getVerticalFill () {return vfill;}
  
  /** Sets the horizontal alignment of components under this layout.
   * The default value is LEFT. 
   * 
   * @param align int LEFT, CENTER, RIGHT
   */
  public void setHAlignment (int align) {
	 if (align < LEFT | align > RIGHT)
		 throw new IllegalArgumentException("illegal ALIGN value: " + align);
     hAlign = align;
  }
	  
  /** Sets the vertical alignment of components under this layout.
   * The default value is TOP. 
   * 
   * @param align int TOP, CENTER, BOTTOM
   */
  public void setVAlignment (int align) {
	 if (align < TOP | align > BOTTOM)
		 throw new IllegalArgumentException("illegal ALIGN value: " + align);
	 vAlign = align;
  }
	  
  public int getHAlignment () {return hAlign;}

  public int getVAlignment () {return hAlign;}

  private void calculate (Container parent) {
     Component cps[] = parent.getComponents();
     Insets insets = parent.getInsets();
	 rows = cps.length / cols + (cps.length % cols != 0 ? 1 : 0); 
     colWidth = new int[cols];
     colX = new int[cols];
     rowHeight = new int[rows];
     rowY = new int[rows];
     if (rows == 0) return;
	 
     // calculate row and column dimensions
     int count = 0;
     for ( Component c : cps ) {
    	 int col = count % cols;
    	 int row = count / cols;
         Dimension dim = c.getPreferredSize();
      	 colWidth[col] = Math.max(colWidth[col], dim.width);
       	 rowHeight[row] = Math.max(rowHeight[row], dim.height);
         count++;
     }
     
     // calculate column x-addresses
     int x = insets.left;
     for (int i = 0; i < cols; i++) {
    	 colX[i] = i == 0 ? x : (colX[i-1] + colWidth[i-1] + hgap);
     }
     
     // calculate row y-addresses
     int y = insets.top;
     for (int i = 0; i < rows; i++) {
    	 rowY[i] = i == 0 ? y : (rowY[i-1] + rowHeight[i-1] + vgap);
     }
  }
  
  /**
   * layoutContainer method comment.
   */
  @Override
  public synchronized void layoutContainer (Container parent) {
     Component cps[] = parent.getComponents();
     if (cps.length == 0) return;
     int x, y, w, h;
     
     calculate(parent);
     
     int count = 0;
     for (Component c : cps) {
    	 int col = count % cols;
    	 int row = count / cols;
         if (c.isVisible()) {
             Dimension dim = c.getPreferredSize();
        	 int colW = colWidth[col];
        	 int rowH = rowHeight[row];
        	 
        	 // the default addressing
        	 x = colX[col];
        	 y = rowY[row];
             w = hfill ? colW : dim.width;
             h = vfill ? rowH : dim.height;

             // extend x for a special horizontal alignment
        	 if (!hfill) {
        		if (hAlign == RIGHT) {
        		   x += colW - dim.width;
        		} else if ( hAlign == CENTER ) {
        		   x += (colW - dim.width)/2;
        		}
             }
        	 
             // extend y for a special vertical alignment
        	 if (!vfill) {
        		if (vAlign == BOTTOM) {
        		   y += rowH - dim.height;
        		} else if ( vAlign == CENTER ) {
        		   y += (rowH - dim.height)/2;
        		}
             }
        	 
             c.setBounds(x, y, w, h);
             Log.debug(1, "(BoxedFlowLayout.layoutContainer) componant bounds := " + x + ", " + y +
            		      ", " + w + ", " + h);
         }
         count++;
     }
  } 

  /**
   * minimumLayoutSize method comment.
   */
  @Override
  public synchronized Dimension minimumLayoutSize (Container parent) {
    calculate(parent);
    
    int totalWidth = 0;
    for (int v : colWidth) {
    	totalWidth += v;
    }
    int totalHeight = 0;
    for (int v : rowHeight) {
    	totalHeight += v;
    }

    Insets insets = parent.getInsets();
    int width = totalWidth + insets.left + insets.right + (cols == 1 ? 0 : cols-1) * hgap; 
    int height = totalHeight+ insets.top + insets.bottom + (rows <= 1 ? 0 : rows-1) * vgap; 
    return new Dimension(width, height);
  } 

  /**
   * preferredLayoutSize method comment.
   */
  @Override
  public synchronized Dimension preferredLayoutSize (Container parent) {
     return minimumLayoutSize(parent);
  } 

  /**
   * addLayoutComponent method comment.
   */
  @Override
  public void addLayoutComponent(String name, Component comp) {
  } 

  /**
   * removeLayoutComponent method comment.
   */
  @Override
  public void removeLayoutComponent(Component comp) {
  } 
}