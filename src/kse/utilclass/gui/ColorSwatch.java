package kse.utilclass.gui;

/*
*  File: ColorSwatch.java
* 
*  Project JUtilClasses
*  @author Christopher Bach
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Objects;

import javax.swing.Icon;

/**
 * An icon for painting a square swatch of a specified Color.
 * 
 * This code piece is taken from the Java Tutorial.Swing.Icon 
 * (http://www.java2s.com) on Aug. 2014.
 */
public class ColorSwatch implements Icon, java.io.Serializable {
	
  private static final long serialVersionUID = 656000327512472674L;

  private Color ourSwatchColor = Color.white;

  private Color ourBorderColor = Color.black;

  private boolean ourBorderPainted = true;

  private boolean ourSwatchIsMultiColor = false;

  private boolean ourSwatchIsVoid = false;

  private int ourSwatchSize = 14;

  /**
   * Creates a standard 14 x 14 swatch with a black border and white background.
   */
  public ColorSwatch() {
  }

  /**
   * Creates a swatch of the specified size with a black border and white
   * background.
   * 
   * @param size int swatch size in pixel 
   */
  public ColorSwatch (int size) {
     setSwatchSize(size);
  }

  /**
   * Creates a swatch of the specified size with a black border and white
   * background and determines whether or n not the border should be painted.
   * 
   * @param size int swatch size in pixel 
   * @param borderPainted boolean true = border is painted, false = border 
   *        is not painted
   */
  public ColorSwatch (int size, boolean borderPainted) {
     setSwatchSize(size);
     setBorderPainted(borderPainted);
  }

  /**
   * 
   * @param color {@code Color} colour this swatch represents
   */
  public ColorSwatch (Color color) {
     setColor(color);
  }

  /**
   * Creates a swatch of the specified size and showing the given colour.
   * 
   * @param size int swatch size in pixel 
   * @param color {@code Color} colour this swatch represents
   */
  public ColorSwatch (int size, Color color) {
     setSwatchSize(size);
     setColor(color);
  }

  /**
   * Creates a swatch of the specified size and showing the given colours
   * for body and border.
   * 
   * @param size int swatch size in pixel 
   * @param color {@code Color} colour this swatch represents
   * @param borderColor {@code Color} colour of the border
   */
  public ColorSwatch (int size, Color color, Color borderColor) {
     setSwatchSize(size);
     setColor(color);
     setBorderColor(borderColor);
     setBorderPainted(true);
  }

  /**
   * Sets the size of this swatch.
   * 
   * @param size int size of the box in pixel 
   */
  public void setSwatchSize (int size) {
    if (size > 0)
      ourSwatchSize = size;
    else
      ourSwatchSize = 14;
  }

  /**
   * Returns the size of this swatch.
   */
  public int getSwatchSize() {
    return ourSwatchSize;
  }

  /**
   * Determines whether or not this swatch's border should be painted.
   * 
   * @param borderPainted boolean true = border is painted, false = border 
   *        is not painted
   */
  public void setBorderPainted (boolean borderPainted) {
    ourBorderPainted = borderPainted;
  }

  /**
   * Returns whether or not this swatch's border is painted.
   * 
   * @return boolean true = border is painted
   */
  public boolean isBorderPainted() {
    return ourBorderPainted;
  }

  /**
   * Sets the color of this swatch's border. With {@code null} border
   * painting is switched off.
   * 
   * @param color {@code Color} border color
   */
  public void setBorderColor (Color color) {
	 Objects.requireNonNull(color);
	 ourBorderColor = color;
  }

  /**
   * Returns the color of this swatch's border.
   * 
   * @return {@code Color}
   */
  public Color getBorderColor() {
    return ourBorderColor;
  }

  /**
   * Sets the color that this swatch represents.
   * 
   * @param color {@code Color} 
   */
  public void setColor (Color color) {
	Objects.requireNonNull(color);
    ourSwatchIsMultiColor = false;
    ourSwatchColor = color;
  }

  /**
   * Returns the color that this swatch represents.
   * 
   * @return {@code Color}
   */
  public Color getColor() {
    return ourSwatchColor;
  }

  /**
   * Sets this swatch to represent more than one color.
   */
  public void setMultiColor () {
    ourSwatchIsMultiColor = true;
  }

  /**
   * Returns whether or not this swatch represents more than one color.
   * 
   * @return boolean true = swatch is multi-colored 
   */
  public boolean isMultiColor() {
    return ourSwatchIsMultiColor;
  }

  /**
   * Determines whether or not this swatch is void. If the swatch is void, it
   * will not be painted at all.
   * 
   * @param isVoid boolean true = not painted, false = painted 
   */
  public void setVoid (boolean isVoid) {
    // When true, this icon will not be painted at all.
    ourSwatchIsVoid = isVoid;
  }

  /**
   * Returns whether this swatch is void. If the swatch is void, it will not be
   * painted at all.
   * 
   * @return boolean  true = swatch is not painted, false = swatch is painted 
   */
  public boolean isVoid() {
    return ourSwatchIsVoid;
  }

  // // Icon implementation ////

  /**
   * Returns the width of this Icon.
   */
  public int getIconWidth() {
    return ourSwatchSize;
  }

  /**
   * Returns the height of this Icon.
   */
  public int getIconHeight() {
    return ourSwatchSize;
  }

  /**
   * Paints this Icon into the provided graphics context.
   */
  public void paintIcon(Component c, Graphics g, int x, int y) {
    if (ourSwatchIsVoid)
      return;

    Color oldColor = g.getColor();

    if (ourSwatchIsMultiColor) {
      g.setColor(Color.white);
      g.fillRect(x, y, ourSwatchSize, ourSwatchSize);
      g.setColor(ourBorderColor);
      for (int i = 0; i < ourSwatchSize; i += 2) {
        g.drawLine(x + i, y, x + i, y + ourSwatchSize);
      }
    }

    else if (ourSwatchColor != null) {
      g.setColor(ourSwatchColor);
      g.fillRect(x, y, ourSwatchSize, ourSwatchSize);
    }

    else {
      g.setColor(Color.white);
      g.fillRect(x, y, ourSwatchSize, ourSwatchSize);
      g.setColor(ourBorderColor);
      g.drawLine(x, y, x + ourSwatchSize, y + ourSwatchSize);
      g.drawLine(x, y + ourSwatchSize, x + ourSwatchSize, y);
    }

    if (ourBorderPainted) {
      g.setColor(ourBorderColor);
      g.drawRect(x, y, ourSwatchSize, ourSwatchSize);
    }

    g.setColor(oldColor);
  }

}