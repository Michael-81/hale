/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */
package eu.esdihumboldt.hale.htmlexporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import eu.esdihumboldt.cst.align.ICell;
import eu.esdihumboldt.cst.align.ext.IParameter;
import eu.esdihumboldt.goml.align.Alignment;
import eu.esdihumboldt.goml.omwg.FeatureClass;
import eu.esdihumboldt.goml.omwg.Property;
import eu.esdihumboldt.goml.omwg.Restriction;

import eu.esdihumboldt.hale.rcp.views.mappingGraph.MappingGraphView;
import eu.esdihumboldt.hale.rcp.wizards.io.mappingexport.MappingExportException;
import eu.esdihumboldt.hale.rcp.wizards.io.mappingexport.MappingExportProvider;

/**
 * Export a Mapping to HTML for documentation purposes.
 * 
 * @author Stefan Gessner
 * @version $Id$
 */
public class HtmlMappingExportFactory implements MappingExportProvider {
	
	/**
	 * The context which gets written in the template file
	 */
	private VelocityContext context;
	
	/**
	 * The alignment
	 */
	private Alignment alignment = null;

	/**
	 * The path with name
	 */
	private String path;
	
	/**
	 * The path of the export
	 */
	private String onlyPath;
	
	/**
	 * Set the PictureNames
	 */
	private String pictureNames = "image";


	/**
     * @param alignment
	 * @param path
	 * @throws MappingExportException
	 * @see eu.esdihumboldt.hale.rcp.wizards.io.mappingexport.MappingExportProvider#export(eu.esdihumboldt.goml.align.Alignment, java.lang.String)
	 */
	public void export(Alignment alignment, String path) throws MappingExportException {
		
		//Create the images of the cells
		new MappingGraphView(alignment,this.pictureNames);
		
		this.alignment = alignment;
		this.path = path;
		
		String[] pathSpilt = path.split("\\\\");
		this.onlyPath = path.replace(pathSpilt[pathSpilt.length-1] , "");
		
		StringWriter stringWriter = new StringWriter();
		this.context = new VelocityContext();
		
		//Gets the path to the template file and style sheet
		URL templatePath = this.getClass().getResource("template.html"); 
		URL cssPath = this.getClass().getResource("style.css"); 
		
		//generates a byteArray out of the template
		byte[] templateByteArray = null;
		try {
			templateByteArray = this.urlToByteArray(templatePath);
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
		//creates the temporary file
		File tempFile = null;
		try {
			tempFile = File.createTempFile("template", ".vm");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//writes the byteArray from the template into the temporary file
		try {
			this.byteArrayToFile(tempFile, templateByteArray);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//Set temporary template path
		String tempPath = tempFile.getPath().replace(tempFile.getName(), "");
		Velocity.setProperty("file.resource.loader.path", tempPath);
			try {
				//Initiate Velocity
				Velocity.init();
			} catch (Exception e) {
				e.printStackTrace();
			}
			//Fill the context-variables with data
			this.fillContext();
			Template template = null;
			
			try {
				//Load template
				if(tempFile!=null){
					//FIXME URLResourceLoader is not working. So the file can't
					//be loaded directly in the bundle. 
					//It has to be temporary saved to use it.
					template = Velocity.getTemplate(tempFile.getName());
				}
			} catch (ResourceNotFoundException e) {
				e.printStackTrace();
			} catch (ParseErrorException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				//Merge Content with template
				template.merge(this.context,stringWriter);
			} catch (ResourceNotFoundException e) {
				e.printStackTrace();
			} catch (ParseErrorException e) {
				e.printStackTrace();
			} catch (MethodInvocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//Create HTML export file
			 File htmlOutputFile = new File(path);
			 try {
				this.stringWriterToFile(htmlOutputFile, stringWriter);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//generates a byteArray out of the style sheet
			byte[] cssByteArray = null;
			try {
				cssByteArray = this.urlToByteArray(cssPath);
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			
			//Create CSS export file
			String cssPathFile = htmlOutputFile.getPath().replace(htmlOutputFile.getName(), "");
			File cssOutputFile = new File(cssPathFile+"style.css");
			 try {
				this.byteArrayToFile(cssOutputFile, cssByteArray);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//delete tempFile for cleanup
			tempFile.deleteOnExit();
			
	}
	
	 /**
	 * Create context-variables and fills them with data
	 */
	private void fillContext() {
		Date date = new Date();
		SimpleDateFormat dfm = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		this.context.put("title", "Mapping Export of "+ this.path.toString());
		this.context.put("mapping", "Mapping : "+ this.path.toString());
		this.context.put("project", "<h3>Project : </h3>"+ dfm.format(date));
		this.context.put("schema1", "<h3>Schema 1 : </h3>"+this.alignment.getSchema1().getLocation());
		this.context.put("schema2", "<h3>Schema 2 : </h3>"+this.alignment.getSchema2().getLocation());
		
		Vector<Vector> cellListVector = new Vector<Vector>();
		Vector<String> cellVector;
		
		//cell counter
		int i=1;
		
		for (Iterator<ICell> iterator = this.alignment.getMap().iterator();iterator.hasNext();) {
			ICell cell = iterator.next();
			cellVector = new Vector<String>();

			cellVector.addElement("<h2>Cell "+i+" : </h2>");
			cellVector.addElement("<h3>Relation : </h3>"+cell.getRelation());
			cellVector.addElement("<img src='"+this.pictureNames+(i-1)+".png'>");
//			cellVector.addElement("<h3>Typ : </h3>"+cell.getEntity1());
			
			//Filters
			/**
			 * For Entity 1
			 */
			// Filter strings are added to the Vector
			if (cell.getEntity2().getTransformation() == null) {
				cellVector.addElement("<h3>Entity1: </h3>"+cell.getEntity1().getAbout().getAbout());
				if (cell.getEntity1() instanceof FeatureClass) {
					if (((FeatureClass) cell.getEntity1())
							.getAttributeValueCondition() != null) {
						cellVector.addElement("<h3>Filter Rules: </h3>");
						for (Restriction restriction : ((FeatureClass) cell
								.getEntity1()).getAttributeValueCondition()) {
							cellVector.addElement(restriction.getCqlStr());
						}
					}
				} else if (cell.getEntity1() instanceof Property) {
					if (((Property) cell.getEntity1())
							.getValueCondition() != null) {
						cellVector.addElement("<h3>Filter Rules: </h3>");
						for (Restriction restriction : ((Property) cell
								.getEntity1()).getValueCondition()) {
							cellVector.addElement(restriction.getCqlStr());
						}
					}
				}
			}

			/**
			 * For Entity 2
			 */
			// Filter strings are added to the Vector
			else {
				cellVector.addElement("<h3>Entity2: </h3>"+cell.getEntity2().getAbout().getAbout());
				if (cell.getEntity2() instanceof FeatureClass) {
					if (((FeatureClass) cell.getEntity2())
							.getAttributeValueCondition() != null) {
						cellVector.addElement("<h3>Filter Rules: </h3>");
						for (Restriction restriction : ((FeatureClass) cell
								.getEntity2()).getAttributeValueCondition()) {
							cellVector.addElement(restriction.getCqlStr());
						}
					}
				} else if (cell.getEntity2() instanceof Property) {
					if (((Property) cell.getEntity2()).getValueCondition() != null) {
						cellVector.addElement("<h3>Filter Rules: </h3>");
						for (Restriction restriction : ((Property) cell
								.getEntity2()).getValueCondition()) {
							cellVector.addElement(restriction.getCqlStr());
						}
					}
				}
			}
			
			//Parameters
			List<IParameter> parameterList;
			/**
			 * For Entity 1
			 */
			if (cell.getEntity2().getTransformation() == null) {
				parameterList = cell.getEntity1().getTransformation().getParameters();
			}
			/**
			 * For Entity 2
			 */
			else {
				parameterList = cell.getEntity2().getTransformation().getParameters();
			}

			if (!parameterList.isEmpty()) {
				cellVector.addElement("<h3>Parameters: </h3>");
				for (IParameter parameter : parameterList) {
					cellVector.addElement("<h3>"+parameter.getName() + " : </h3>"+ parameter.getValue());
				}
			}
			
			cellListVector.addElement(cellVector);
			i++;
		}
		this.context.put("cellList", cellListVector);
		
	}

	/**
	 * @param url 
	 * @return The File as a Byte[]
	 * @throws Exception
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private byte[] urlToByteArray(URL url) throws Exception, IOException,
     UnsupportedEncodingException {
        URLConnection connection = url.openConnection();
        int contentLength = connection.getContentLength();
        InputStream inputStream = url.openStream();
		byte[] data = new byte[contentLength];
		inputStream.read(data);
		inputStream.close();
		return data;
	}
	
	/**
	 * @param file
	 * @param byteArray 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void byteArrayToFile(File file, byte [] byteArray) throws 
	 FileNotFoundException, IOException {
		if(byteArray!=null){
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(byteArray);
			fileOutputStream.close();
		}
	}
	
	/**
	 * @param file 
	 * @param writer 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void stringWriterToFile(File file, StringWriter writer) throws 
	 FileNotFoundException, IOException {
		if(writer!=null && file!=null){
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(writer.toString().getBytes());
			fileOutputStream.close();
		}
	}
}
