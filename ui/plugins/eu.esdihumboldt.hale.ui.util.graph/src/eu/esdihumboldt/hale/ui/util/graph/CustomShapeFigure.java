/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2011.
 */
package eu.esdihumboldt.hale.ui.util.graph;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.zest.core.widgets.ILabeledFigure;
import org.eclipse.zest.core.widgets.IStyleableFigure;

/**
 * A custom shaped figure for use in graphs.
 * 
 * @author Simon Templer
 */
public class CustomShapeFigure extends Shape implements IStyleableFigure,
		ILabeledFigure {

	/**
	 * Paints fill and outline of a shape and provides corresponding insets.
	 */
	public interface ShapePainter {

		/**
		 * Fills the interior of the shape.
		 * 
		 * @param graphics
		 *            the graphics object
		 * @param bounds
		 *            the figure bounds, may not be modified
		 */
		public void fillShape(Graphics graphics, Rectangle bounds);

		/**
		 * Outlines the shape.
		 * 
		 * @param graphics
		 *            the graphics object
		 * @param bounds
		 *            the figure bounds, may not be modified
		 */
		public void outlineShape(Graphics graphics, Rectangle bounds);

		/**
		 * Get the shape insets.
		 * 
		 * @return the shape insets
		 */
		public Insets getInsets();

	}

	private Label textLabel;

	private Label iconLabel;

	private String text = "";

	private Image icon;

	private Color borderColor;

	/**
	 * The shape painter used for the figure
	 */
	protected final ShapePainter painter;

	/**
	 * Create a custom shaped figure.
	 * 
	 * @param painter
	 *            the painter drawing the figure shape
	 */
	public CustomShapeFigure(ShapePainter painter) {
		super();

		if (painter == null) {
			throw new IllegalArgumentException("The painter may not be null");
		}

		this.painter = painter;
	}

	/**
	 * Set the label that displays the text.
	 * 
	 * @param textLabel
	 *            the label to display the text, may be the same as the icon
	 *            label or <code>null</code> to ignore the text
	 * 
	 * @see #setIconLabel(Label)
	 */
	public void setTextLabel(Label textLabel) {
		this.textLabel = textLabel;

		if (text != null) {
			textLabel.setText(text);
		}
	}

	/**
	 * Set the label that displays the icon.
	 * 
	 * @param iconLabel
	 *            the label to display the icon, may be the same as the text
	 *            label or <code>null</code> to ignore the icon
	 * 
	 * @see #setTextLabel(Label)
	 */
	public void setIconLabel(Label iconLabel) {
		this.iconLabel = iconLabel;

		if (icon != null) {
			iconLabel.setIcon(icon);
		}
	}

	@Override
	public Insets getInsets() {
		return painter.getInsets();
	}

	@Override
	protected void fillShape(Graphics graphics) {
		// delegate filling the shape to the painter
		painter.fillShape(graphics, getBounds());
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		if (borderColor != null) {
			graphics.setForegroundColor(borderColor);
		}

		// delegate outlining the shape to the painter
		painter.outlineShape(graphics, getBounds());
	}

	/**
	 * Adjust the figure size. The default implementation sets the size to the
	 * preferred size.
	 */
	protected void adjustSize() {
		setSize(getPreferredSize());
	}

	/**
	 * Get the border color
	 * 
	 * @return the border color, <code>null</code> if it was not set using
	 *         {@link #setBorderColor(Color)}
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	@Override
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	@Override
	public void setBorderWidth(int borderWidth) {
		setLineWidth(borderWidth);
	}

	@Override
	public void setText(String text) {
		this.text = text;

		if (textLabel != null) {
			textLabel.setText(text);
			adjustSize();
		}
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setIcon(Image icon) {
		this.icon = icon;

		if (iconLabel != null) {
			iconLabel.setIcon(icon);
			adjustSize();
		}
	}

	@Override
	public Image getIcon() {
		return icon;
	}

}