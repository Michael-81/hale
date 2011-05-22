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

package eu.esdihumboldt.hale.ui.service.instance.crs;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * CRS definition based on WKT
 * @author Simon Templer
 */
public class WKTDefinition implements CRSDefinition {

	private String wkt;
	private CoordinateReferenceSystem crs;

	/**
	 * Constructor
	 * 
	 * @param wkt the WKT defining the CRS
	 * @param crs the coordinate reference system, may be <code>null</code>
	 */
	public WKTDefinition(String wkt, CoordinateReferenceSystem crs) {
		this.wkt = wkt;
		this.crs = crs;
	}

	/**
	 * @see CRSDefinition#getCRS()
	 */
	@Override
	public CoordinateReferenceSystem getCRS() {
		if (crs == null) {
			try {
				crs = CRS.parseWKT(wkt);
			} catch (Exception e) {
				throw new IllegalStateException("Invalid WKT for defining a CRS", e);
			}
		}
		
		return crs;
	}

	/**
	 * Get the WKT
	 * 
	 * @return the wkt
	 */
	public String getWkt() {
		return wkt;
	}

}
