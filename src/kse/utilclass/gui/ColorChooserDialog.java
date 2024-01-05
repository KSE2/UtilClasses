package kse.utilclass.gui;

/*
*  File: ColorChooserDialog.java
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.Objects;

import javax.swing.BorderFactory;

import kse.utilclass.dialog.ButtonBarModus;
import kse.utilclass.dialog.DialogPerformBlock;
import kse.utilclass.dialog.GSDialog;
import kse.utilclass.dialog.GUIService;
import kse.utilclass.misc.Log;
import kse.utilclass.misc.Util;

/** Color chooser dialog which incorporates {@code ColorChooserPanel}.
 * The dialog offers to choose from a set of predefined colours (type 
 * {@code Color}) by clicking on a corresponding button which is offered
 * in a vertical list or grid of options. Optionally a special button can
 * be activated which appears at the end of the panel and enables to call a
 * {@code JColorChooser} dialog to define an individual colour instead of 
 * selecting a predefined option. Whether this option is available depends
 * on whether the 'initialColor' parameter of the constructor is defined. 
 * The initial colour is used inside of the {@code JColorChooser} dialog.
 */
public class ColorChooserDialog extends GSDialog {
	public static final int DEFAULT_STAPLE_SIZE = 8;

	protected Color[] colors;
	protected ColorChooserPanel cpanel;
	protected Color initialColor;
	protected Color selectedColor;
	private int stapleSize = DEFAULT_STAPLE_SIZE; 
	
	/** Creates a modal chooser dialog w/o title and owner.
	 * 
	 * @param colors Color[] set of color options
	 * @param initialColor Color initial color option, may be null
	 */
	public ColorChooserDialog (Color[] colors, Color initialColor) {
		this(null, null, colors, initialColor);
	}
	
	/** Creates a modal chooser dialog with the given title and owner.
	 * 
	 * @param owner Window
	 * @param title String dialog title, may be null
	 * @param colors Color[] set of color options
	 * @param initialColor Color initial color option, may be null
	 */
	public ColorChooserDialog (Window owner, String title, Color[] colors, Color initialColor) {
		super(owner, ButtonBarModus.BREAK, true);
		Objects.requireNonNull(colors, "colors is null");
		setTitle(title);
		this.colors = colors;
		this.initialColor = initialColor;
		init();
	}

	private void init() {
		cpanel = new ColorChooserPanel(new Dimension(70, 30), colors, stapleSize);
		cpanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		if (initialColor != null) {
			cpanel.setSelectorActive(getIntlText("button.colorchooser"), 
				   getIntlText("title.jcolorchooser"),	initialColor);
		}
		
		cpanel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedColor = cpanel.getSelectedColor();
				Log.debug(10, "(ColorChooserDialog) selected Color = " + selectedColor);
				dispose();
			}
		});
		
		DialogPerformBlock block = new DialogPerformBlock() {
			@Override
			public Container getContent() {
				return cpanel;
			}
		};
		
		setPerformBlock(block);
		setResizable(false);
		pack();
	}
	
	public static String getIntlText (String token) {
		String text = null;
		String language = Locale.getDefault().getLanguage();
		
		// default language English
		if ("button.colorchooser".equals(token)) {
			text = "Color";
		} else if ("title.jcolorchooser".equals(token)) {
			text = "Color Definition";
		} else if ("title.choose.color".equals(token)) {
			text = "Color Selection";
		}

		// language German
		if (Locale.GERMAN.getLanguage().equals(language)) {
			if ("button.colorchooser".equals(token)) {
				text = "Farbe";
			} else if ("title.jcolorchooser".equals(token)) {
				text = "Farb-Definition";
			} else if ("title.choose.color".equals(token)) {
				text = "Farb-Auswahl";
			}
			
		} else if (Locale.FRENCH.getLanguage().equals(language)) {
			if ("button.colorchooser".equals(token)) {
				text = "Couleur";
			} else if ("title.jcolorchooser".equals(token)) {
				text = "Définition des Couleur";
			} else if ("title.choose.color".equals(token)) {
				text = "Sélection des Couleur";
			}

		} else if (Locale.ITALIAN.getLanguage().equals(language)) {
			if ("button.colorchooser".equals(token)) {
				text = "Colore";
			} else if ("title.jcolorchooser".equals(token)) {
				text = "Definizione del Colore";
			} else if ("title.choose.color".equals(token)) {
				text = "Selezione del Colore";
			}
		}

		return text;
	}
	
//	/** Sets the number of color options in one column.
//	 * 
//	 * @param size int staple size (>= 0)
//	 */
//	public void setStapleSize (int size) {
//		if (size < 1) 
//			throw new IllegalArgumentException("illegal staple-size: " + size);
//		stapleSize = size;
//		init();
//	}
	
	/** Returns the user selected color or null if nothing was selected.
	 * 
	 * @return {@code Color} or null
	 */
	public Color getSelectedColor () {return selectedColor;}
	
	/** Shows a modal color chooser dialog and returns the user opted Color
	 * or null if no color was chosen. 
	 *   
	 * @param parent Component display relation of dialog, may be null
	 * @param title String title of dialog, may be null for default
	 * @param colors Color[] set of color options
	 * @param initialColor Color any color or null
	 * @return Color or null
	 */
	public static Color showDialog (Component parent, String title, Color[] colors, Color initialColor) {
		if (title == null) {
			title = getIntlText("title.choose.color");
		}
	    Window owner = parent == null ? GUIService.getMainFrame() : GUIService.getAncestorWindow(parent);
		ColorChooserDialog dlg = new ColorChooserDialog(owner, title, colors, initialColor);
		dlg.setLocationRelativeTo(parent == null ? dlg.getOwner() : parent);
		dlg.setVisible();
		return dlg.getSelectedColor();
	}
}
