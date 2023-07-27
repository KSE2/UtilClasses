package kse.utilclass.gui;

import java.awt.Color;
import java.awt.Font;

import kse.utilclass.dialog.GUIService;
import kse.utilclass.misc.UnixColor;

public class TestAppl1 {

	public static void main (String[] args) {
		
		Color[] colors = new Color[] {Color.blue, Color.gray, Color.green, Color.red, Color.yellow, Color.magenta};
		Color initColor = UnixColor.Aquamarine;
		ColorChooserDialog dlg = new ColorChooserDialog(colors, initColor);
//		dlg.setVisible(true);
//		System.out.println("The answer is " + dlg.getSelectedColor());
//		
//		dlg = new ColorChooserDialog(null, "Chooser Dialog", colors, initColor);
//		dlg.setVisible(true);
//		System.out.println("The answer is " + dlg.getSelectedColor());
		
		dlg = new ColorChooserDialog(null, "Chooser Dialog", colors, initColor);
//		dlg.setStapleSize(4);
		dlg.setVisible(true);
		System.out.println("The answer is " + dlg.getSelectedColor());

		Font font = Font.getFont("Dialog-PLAIN-12");
		font = FontChooser.showDialog(null, "Font X", font);
		System.out.println("The answer is " + font);
		
		GUIService.infoMessage(null, "Hier wurde ein Haus gebaut!");
		GUIService.warningMessage(null, "Achtung, ein Spaten liegt noch im Garten!");
		boolean ok = GUIService.userConfirm("Haben Sie schon zu Mittag gegessen?");
		System.out.println("The answer is " + ok);
		
		System.exit(0);
	}
}
