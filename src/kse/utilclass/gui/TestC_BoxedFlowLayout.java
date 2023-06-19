package kse.utilclass.gui;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.Test;

import kse.utilclass.dialog.ButtonBarModus;
import kse.utilclass.dialog.MessageDialog;
import kse.utilclass.dialog.MessageDialog.MessageType;
import kse.utilclass.misc.UnixColor;

public class TestC_BoxedFlowLayout {

	Object lock = new Object();
	
	public TestC_BoxedFlowLayout() {
	}

	@Test
	public void init () {
		BoxedFlowLayout ly;
		
		try {
			new BoxedFlowLayout(0);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		
		ly = new BoxedFlowLayout(1);
		assertTrue(ly.getCols() == 1);
		assertTrue(ly.getRows() == 0);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.LEFT);
		assertTrue(ly.getHgap() == 3);
		assertTrue(ly.getVgap() == 3);
		assertTrue(ly.getHorizontalFill() == false);
		assertTrue(ly.getVerticalFill() == false);

		ly = new BoxedFlowLayout(2);
		assertTrue(ly.getCols() == 2);
		assertTrue(ly.getRows() == 0);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.LEFT);
		assertTrue(ly.getHgap() == 3);
		assertTrue(ly.getVgap() == 3);
		assertTrue(ly.getHorizontalFill() == false);
		assertTrue(ly.getVerticalFill() == false);

		ly = new BoxedFlowLayout(10);
		assertTrue(ly.getCols() == 10);
		assertTrue(ly.getRows() == 0);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.LEFT);
		assertTrue(ly.getHgap() == 3);
		assertTrue(ly.getVgap() == 3);
		assertTrue(ly.getHorizontalFill() == false);
		assertTrue(ly.getVerticalFill() == false);

		ly = new BoxedFlowLayout(2, 8, 15);
		assertTrue(ly.getCols() == 2);
		assertTrue(ly.getRows() == 0);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.LEFT);
		assertTrue(ly.getHgap() == 8);
		assertTrue(ly.getVgap() == 15);
		assertTrue(ly.getHorizontalFill() == false);
		assertTrue(ly.getVerticalFill() == false);

		ly = new BoxedFlowLayout(2, 8, 15, true, false);
		assertTrue(ly.getCols() == 2);
		assertTrue(ly.getRows() == 0);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.LEFT);
		assertTrue(ly.getHgap() == 8);
		assertTrue(ly.getVgap() == 15);
		assertTrue(ly.getHorizontalFill());
		assertTrue(ly.getVerticalFill() == false);

		ly = new BoxedFlowLayout(2, 8, 15, false, true);
		assertTrue(ly.getCols() == 2);
		assertTrue(ly.getRows() == 0);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.LEFT);
		assertTrue(ly.getHgap() == 8);
		assertTrue(ly.getVgap() == 15);
		assertTrue(ly.getHorizontalFill() == false);
		assertTrue(ly.getVerticalFill());

		ly = new BoxedFlowLayout(2, 8, 15, true, true);
		assertTrue(ly.getCols() == 2);
		assertTrue(ly.getRows() == 0);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.LEFT);
		assertTrue(ly.getHgap() == 8);
		assertTrue(ly.getVgap() == 15);
		assertTrue(ly.getHorizontalFill());
		assertTrue(ly.getVerticalFill());
	}
	
	@Test
	public void setProperties () {
		BoxedFlowLayout ly;

		ly = new BoxedFlowLayout(3);
		
		ly.setHAlignment(BoxedFlowLayout.CENTER);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.CENTER);
		ly.setHAlignment(BoxedFlowLayout.RIGHT);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.RIGHT);
		ly.setHAlignment(BoxedFlowLayout.LEFT);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.LEFT);
		
		ly.setHgap(23);
		assertTrue(ly.getHgap() == 23);
		ly.setVgap(18);
		assertTrue(ly.getVgap() == 18);
		ly.setHorizontalFill(true);
		assertTrue(ly.getHorizontalFill());
		assertTrue(ly.getVerticalFill() == false);
		ly.setVerticalFill(true);
		assertTrue(ly.getHorizontalFill());
		assertTrue(ly.getVerticalFill());
		
		try {
			ly.setHgap(-1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		
		try {
			ly.setVgap(-1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}
	
	@Test
	public void panel_1 () {
		BoxedFlowLayout ly = new BoxedFlowLayout(2, 5, 5);
		JPanel pan = new JPanel(ly);
		
		JLabel lab1 = createJLabel("Hundename :");
		JLabel lab2 = createJLabel("Esel");
		pan.add(lab1);
		pan.add(lab2);
		
		ly.layoutContainer(pan);
		assertTrue(ly.getHAlignment() == BoxedFlowLayout.LEFT);
		assertTrue(ly.getHgap() == 5);
		assertTrue(ly.getVgap() == 5);
		assertTrue(ly.getRows() == 1);
		
		// simple case w/ one hgap
		Dimension dimL1 = lab1.getPreferredSize();
		Dimension dimL2 = lab2.getPreferredSize();
		Dimension dim = ly.preferredLayoutSize(pan);
		assertTrue(dim.width == dimL1.width + dimL2.width + 5);
		assertTrue(dim.height == dimL1.height);
		
		// panel gains border (insets not zero)
		pan.setBorder(BorderFactory.createEmptyBorder(20, 10, 15, 10));
		dim = ly.preferredLayoutSize(pan);
		assertTrue(dim.width == dimL1.width + dimL2.width + 5 + 20);
		assertTrue(dim.height == dimL1.height + 35);
		
		JLabel lab3 = createJLabel("Familienname :");
		JLabel lab4 = createJLabel("Hofhauser");
		Dimension dimL3 = lab3.getPreferredSize();
		Dimension dimL4 = lab4.getPreferredSize();
		pan.add(lab3);
		pan.add(lab4);
		
		// double row case, one hgap, one vgap
		dim = ly.preferredLayoutSize(pan);
		assertTrue(ly.getRows() == 2);
		assertTrue(dim.width == Math.max(dimL1.width, dimL3.width) + Math.max(dimL2.width, dimL4.width) + 5 + 20);
		assertTrue(dim.height == 2*dimL1.height + 5 + 35);
		
		// third row, incomplete setting
		JLabel lab5 = createJLabel("Adresse:");
		pan.add(lab5);
		dim = ly.preferredLayoutSize(pan);
		assertTrue(ly.getRows() == 3);
		assertTrue(dim.width == Math.max(dimL1.width, dimL3.width) + Math.max(dimL2.width, dimL4.width) + 5 + 20);
		assertTrue(dim.height == 3*dimL1.height + 10 + 35);
		
		// paint
//		ly.setHAlignment(BoxedFlowLayout.CENTER);
		pan.setBackground(UnixColor.Beige);
		pan.setOpaque(true);
		MessageDialog dlg = MessageDialog.createMessageDialog(null, "TEST PANEL", pan, MessageType.info, ButtonBarModus.OK, true);
		synchronized (lock) {
			dlg.show();
		}
	}
	
	@Test
	public void panel_2 () {
		BoxedFlowLayout ly = new BoxedFlowLayout(3, 10, 8);
		JPanel pan = new JPanel(ly);

		JPanel p1, p2, p3, p4, p5, p6;
		
		p1 = createPanel(50, 16, UnixColor.CadetBlue);
		p2 = createPanel(80, 35, UnixColor.BurlyWood);
		p3 = createPanel(30, 50, UnixColor.Salmon);
		p4 = createPanel(100, 50, UnixColor.CadetBlue);
		p5 = createPanel(40, 63, UnixColor.BurlyWood);
		p6 = createPanel(60, 33, UnixColor.Salmon);

		pan.add(p1);
		pan.add(p2);
		pan.add(p3);
		pan.add(p4);
		pan.add(p5);
		pan.add(p6);
		
		// paint
		pan.setBackground(UnixColor.CornSilk);
		pan.setOpaque(true);
		MessageDialog dlg = MessageDialog.createMessageDialog(null, "TEST PANEL", pan, MessageType.info, ButtonBarModus.OK, true);
		synchronized (lock) {
			ly.setHAlignment(BoxedFlowLayout.LEFT);
			ly.setVAlignment(BoxedFlowLayout.TOP);
			dlg.setTitle("BoxedFlow LEFT, TOP");
			dlg.show();
			ly.setHAlignment(BoxedFlowLayout.LEFT);
			ly.setVAlignment(BoxedFlowLayout.BOTTOM);
			dlg.setTitle("BoxedFlow LEFT, BOTTOM");
			dlg.show();
			ly.setHAlignment(BoxedFlowLayout.CENTER);
			ly.setVAlignment(BoxedFlowLayout.CENTER);
			dlg.setTitle("BoxedFlow CENTER, CENTER");
			dlg.show();
			ly.setHAlignment(BoxedFlowLayout.RIGHT);
			ly.setVAlignment(BoxedFlowLayout.CENTER);
			dlg.setTitle("BoxedFlow RIGHT, CENTER");
			ly.setHAlignment(BoxedFlowLayout.RIGHT);
			ly.setVAlignment(BoxedFlowLayout.BOTTOM);
			dlg.setTitle("BoxedFlow RIGHT, BOTTOM");
			dlg.show();
			
			ly.setHAlignment(BoxedFlowLayout.LEFT);
			ly.setVAlignment(BoxedFlowLayout.CENTER);
			ly.setHorizontalFill(true);
			dlg.setTitle("BoxedFlow LEFT, CENTER, hfill");
			dlg.show();
			ly.setHAlignment(BoxedFlowLayout.LEFT);
			ly.setVAlignment(BoxedFlowLayout.CENTER);
			ly.setHorizontalFill(false);
			ly.setVerticalFill(true);
			dlg.setTitle("BoxedFlow LEFT, CENTER, vfill");
			dlg.show();
			ly.setHAlignment(BoxedFlowLayout.LEFT);
			ly.setVAlignment(BoxedFlowLayout.CENTER);
			ly.setHorizontalFill(true);
			ly.setVerticalFill(true);
			dlg.setTitle("BoxedFlow LEFT, CENTER, hfill, vfill");
			dlg.show();
			
		}
	}
	
	JLabel createJLabel (String text) {
		JLabel l = new JLabel(text);
		l.setBackground(UnixColor.AliceBlue);
		l.setOpaque(true);
		return l;
	}
	
	JPanel createPanel (int width, int height, Color color) {
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(width, height));
		p.setBackground(color);
		p.setOpaque(true);
		return p;
	}
	
	
}
