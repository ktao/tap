/***************************************************************
*  Copyright (c) 2010 by GRAPPLE Project (http://www.grapple-project.org)
*  Some rights reserved.
*
*  This file is part of the GRAPPLE Project. 
*  
*  Contact: http://www.grapple-project.org
*
*  This copyright notice MUST APPEAR in all copies of the file!
***************************************************************/
package nl.wisdelft.twinder.utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


/**
 * This class reads in the mypes.properties file.
 * 
 * @author Fabian Abel, <a href="mailto:abel@l3s.de">abel@l3s.de</a>
 * @author last edited by: $Author: fabian $
 * 
 * @version created on May 7, 2009
 * @version modified on April 28, 2014
 * @version $Revision: 1.1 $ $Date: 2011-01-25 18:46:40 $
 */
public class PropertyReader {
//	private static final String BUNDLE_NAME = "nl.wisdelft.twinder.utility.tal"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = null;
	
	static {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("./resources/tal.properties");
//			fis = new FileInputStream("/usr/local/twinder/resources/tal.properties");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			RESOURCE_BUNDLE = new PropertyResourceBundle(fis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		  try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
	}

	private PropertyReader() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static int getInt(String key) {
		try {
			return Integer.parseInt(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException e) {
			System.err.println("Missing value in the configuration file for the key " + key);
			return -1;
		} catch (NumberFormatException e) {
			System.err.println("Wrong value (not integer?) in the configuration file for the key " + key);
			return -1;
		}
	}
	
	public static void main(String args[]) {
		System.out.println(PropertyReader.getString("tal.apikey.opencalais.number"));
	}
}
