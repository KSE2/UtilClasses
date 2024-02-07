package kse.utilclass.gui;

/*
*  File: ColorChooserPanel.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

import kse.utilclass.misc.UnixColor;
import kse.utilclass.sets.ArraySet;

/** This JPanel allows to choose from a set of predefined colours (type 
 * {@code Color} by clicking on a corresponding button which is offered
 * in a vertical list or grid of options. Optionally a special button can
 * be activated which appears at the end of the panel and allows to call a
 * {@code JColorChooser} dialog to define an individual colour instead of 
 * selecting a predefined option.
 *   
 */
public class ColorChooserPanel extends JPanel {

	public static final Color[] DEFAULT_COLORS = new Color[] {
		Color.black, Color.white, Color.red, Color.green, Color.blue, Color.yellow, 
		Color.cyan, Color.magenta, Color.orange, Color.pink, UnixColor.Brown, 
		UnixColor.ForestGreen, Color.lightGray, Color.gray, Color.darkGray
	};
	
	private ArraySet<ActionListener> listenerSet = new ArraySet<>();
	ActionListener alistener;
	private Dimension swatDim;
	private Color[] colors;
	private Color selection;
	private Color selectorColor;
	private JButton selectorButton;
	private String selectorTitle;
	private int stapleSize;

	/** Creates a panel with 14 default colours, staple-size 8 and swat 
	 * size 60 x 30 pixels.
	 * 
	 */
	public ColorChooserPanel () {
		this(new Dimension(60, 30), DEFAULT_COLORS, 8);
	}

	
	public ColorChooserPanel (Dimension swat, Color[] colors, int stapleSize) {
		Objects.requireNonNull(swat, "swat is null");
		Objects.requireNonNull(colors, "colors is null");
		if (stapleSize < 1) 
			throw new IllegalArgumentException("illegal staple-size: " + stapleSize);
		
		this.swatDim = swat;
		this.colors = colors;
		this.stapleSize = stapleSize;
		init();
	}

	public ColorChooserPanel (Dimension swat, int[] colors) {
		this(swat, getColors(colors), 8);
	}

	private static Color[] getColors (int[] colors) {
		Color[] ca = new Color[colors.length];
		for (int i = 0; i < colors.length; i++) {
			ca[i] = new Color(colors[i]);
		}
		return ca;
	}
	
	private void init () {
		alistener = new ButtonActionListener();
		
		int columns = colors.length / stapleSize + (colors.length % stapleSize > 0 ? 1 : 0);
		setLayout(new BoxedFlowLayout(columns));
		
		for (Color c : colors) {
			JButton button = new JButton();
			button.setPreferredSize(swatDim);
			button.setBackground(c);
			button.setOpaque(true);
			button.setName(String.valueOf(c.getRGB()));
			button.addActionListener(alistener);

			add(button);
		}
	}
	
	/** Adds an {@code ActionListener} which is activated when a selection
	 * button is pressed. The chosen colour can then be obtained through the
	 * 'getSelectedColor()' method. 
	 * <p>The source of the event is this panel; the action command is named
	 * "chooser.color.selected".
	 * 
	 * @param l {@code ActionListener} to add
	 */
	public void addActionListener (ActionListener l) {
		Objects.requireNonNull(l);
		listenerSet.add(l);
	}
	
	/** Enables a button as last element of this panel's item table which 
	 * allows to call a JColorChooser dialog to select or define a color.
	 * 
	 * @param buttonName String title for the action button
	 * @param title String title for the JColorChooser dialog
	 * @param initialColor Color initial color for the JColorChooser dialog
	 */
	public void setSelectorActive (String buttonName, String title, Color initialColor) {
		if (selectorButton == null) {
			JButton b = new JButton(buttonName);
			b.setPreferredSize(swatDim);
			b.setFont(b.getFont().deriveFont(10f));
			b.setMargin(new Insets(2,2,2,2));
			b.addActionListener(alistener);
			add(b);
			selectorTitle = title;
			selectorButton = b;
			selectorColor = initialColor;
		}
	}

	/** Returns the user selected color or null if nothing was selected.
	 * 
	 * @return {@code Color} or null
	 */
	public Color getSelectedColor () {
		return selection;
	}
	
	private class ButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed (ActionEvent evt) {
			if (evt != null && evt.getSource() instanceof JButton) {
				JButton b = (JButton) evt.getSource();
				
				// attend selector button if present
				if (b == selectorButton) {
					selection = JColorChooser.showDialog(ColorChooserPanel.this, selectorTitle, selectorColor);
					if (selection != null) {
						fireSelectionEvent();
					}
					
				// attend color swatches
				} else {
					try {
						selection = Color.decode(b.getName());
						fireSelectionEvent();
					} catch (NumberFormatException e) {
						selection = null;
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void fireSelectionEvent () {
		ActionEvent evt = new ActionEvent(this, 0, "chooser.color.selected");
		for (ActionListener li : (ArraySet<ActionListener>) listenerSet.clone()) {
			li.actionPerformed(evt);
		}
	}
}
