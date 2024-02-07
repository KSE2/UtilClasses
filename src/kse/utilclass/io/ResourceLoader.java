/*
*  File: ResourceLoader.java
* 
*  Project Ragna Scribe
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

package kse.utilclass.io;

import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import kse.utilclass.misc.Log;
import kse.utilclass.misc.Util;
import kse.utilclass.sets.ArraySet;

/**
 *  Multi-language text and image resources.
 *  There are three default bundle names: "action", "display" and "message".
 *  With "getString()" any bundle name can be used, otherwise "getCommand()",
 *  "getDisplay()" and "getMessage()" refer to the default bundle names.
 * 
 * <p><b>Locations</b>
 * <br>Resource path expressions can be given absolute (w/o starting '#') or
 * relative w/ starting '#'. The cross is a variable for any basic resource
 * path which can be set up with the constructor or added at a later time.
 * The empty basic path is implicitly defined. 
 * 
 * <p><b>Text Bundles</b>
 * <br>Text bundles are searched in folder "#bundles" and the user has to set up 
 * such a resource. The bundle files are ".properties" files, as usual. E.g.
 * for the bundle "action" in language German the resource path is 
 * "#bundles/action_de.properties", the bundle is referred to by its name or
 * by use of "getCommand()". The fall-back bundle, which should be always
 * defined, is "#bundles/action.properties".
 * 
 * <p><b>Other Resources</b>
 * <br>Beside text bundles, images and any resource file can be retrieved.
 * Images are loaded indirectly over an image table. The image table is locale
 * sensitive to language and must be located at "#system/imagemap.properties" 
 * (fall-back) or "#system/imagemap_xx.properties" for a language (xx) specific 
 * version. We don't discriminate countries. 
 * Within the table you map logical image names to a resource paths, e.g.
 * "splash-scene = #images/noturno-delight.jpeg". With this arrangement you
 * can set up different images for different languages and exchange images
 * during development without changing references in the program.
 * 
 *  @author Wolfgang Keller
 */

public class ResourceLoader {

/** Image name for the fail default image. */	
public static final String FAIL_IMAGE = "default_fail";	

private static Locale loaderLocale;
   
private Set<String> resPaths = new ArraySet<>(); 
private Hashtable<String, ResourceBundle> bundleTable = new Hashtable<>();
private Properties imageTable;

/** Creates a resource loader for the given locale and resource paths.
 * <p>Resource paths use '/' (slash) as an element separator and usually start 
 * w/o a leading slash. They refer to a folder in an application package (.jar) 
 * or in any element of the class-path. 
 * 
 * @param paths String[] array of the resource paths, may be null
 * @param locale Locale, may be null for default
 */
public ResourceLoader (String[] paths, Locale locale) {
	if (locale == null) locale = Locale.getDefault();
	loaderLocale = locale;
	init(paths);
}

/** Creates a resource loader for the JVM default locale and source-root as the
 * default resource path.
 */
public ResourceLoader () {
	this(null, Locale.getDefault());
}

/** Adds a resource path to this loader.
 * 
 * @param path String
 */
public void addResourcePath (String path) {
   if ( path != null ) {
      if ( !path.isEmpty() && !path.endsWith("/") ) {
         path += "/";
      }
      resPaths.add( path );
   }
}

/** Opens a bundle of resources at a given base name. The base name is
 * language independent and gets complemented by the given locale.
 * The bundle is searched in all defined resource paths of this loader.
 * 
 * @param bundle String bundle name
 * @param locale Locale language and country specification  
 * @return {@code ResourceBundle}
 * @throws FileNotFoundException if the bundle was not found 
 */
private ResourceBundle openBundle (String bundle) throws FileNotFoundException {
   ResourceBundle rbundle = null;
   
   for (Iterator<String>  it = resPaths.iterator(); it.hasNext() && rbundle == null;) {
      String bundlePath = it.next() + "bundles/" + bundle;
      try { 
         rbundle = ResourceBundle.getBundle(bundlePath, loaderLocale); 
      } catch ( Exception e ) {
//         System.out.println("*** failed bundle search: " + bundlePath ); 
      }
   } 

   if (rbundle == null) {
      Log.debug(1, "*** MISSING RESOURCE BUNDLE *** : " + bundle);
      throw new FileNotFoundException();
   }

   bundleTable.put(bundle, rbundle);
   return rbundle;
}

public String getString (String bundle, String key) {
   Objects.requireNonNull(bundle, "bundle is null");
   if (key == null) return "";
   
   String result;
   try {
	   ResourceBundle rbundle = bundleTable.get(bundle);
	   if (rbundle == null) {
	      rbundle = openBundle(bundle);
	   }

       result = rbundle.getString( key );
   } catch (MissingResourceException ex) {
      result = "FIXME";
      Log.debug(3, "** MISSING TEXT RESOURCE: " + key + " in " + bundle);
   } catch (Exception ex) {
      result = "FIXME";
   }
   return result;
} 

/** Returns a text resource from the "action" resource bundle.
 * 
 * @param key String resource name
 * @return String text resource or "FIXME" if not found
 */
public String getCommand (String key) {
   return getString( "action", key );
}  

/** Returns a text resource from the "display" resource bundle.
 * 
 * @param key String resource name
 * @return String text resource or "FIXME" if not found
 */
public String getDisplay ( String key ) {
   return getString( "display", key );
}  

/** Returns a text resource from the "message" resource bundle.
 * 
 * @param key String resource name
 * @return String text resource or "FIXME" if not found
 */
public String getMessage ( String key ) {
   return getString( "message", key );
}  

/** Tries to interpret <code>key</code> as string token of resource bundle 
 * "message" but renders the input if no such code is defined.
 * Returns null if the argument is null.
 *  
 * @param key String message token, may be null
 * @return the bundle contained text if <code>key</code> was a token,
 *         <code>key</code> as is otherwise. Possibly null.
 */
public String codeOrRealMsg ( String key ) {
	if (key == null) return null;
	String text = getString( "message", key );
	if ( text.equals( "FIXME" ) ) {
		text = key;
	}
	return text;
}


/** Tries to interpret <code>key</code> as string token of resource bundle 
 * "display" but renders the input if no such code is defined.
*  Returns null if the argument is null.
*  
 * @param key String message token, may be null
 * @return the bundle contained text if <code>key</code> was a token,
 *         <code>key</code> as is otherwise. Possibly null.
*/
public String codeOrRealDisplay ( String key ) {
	if (key == null) return null;
	String text = getString( "display", key );
	if ( text.equals("FIXME") ) {
		text = key;
	}
	return text;
}

/** Tries to interpret <code>key</code> as string token of resource bundle 
 * "action" but renders the input if no such code is defined.
*  Returns null if the argument is null.
*  
 * @param key String message token, may be null
 * @return the bundle contained text if <code>key</code> was a token,
 *         <code>key</code> as is otherwise. Possibly null.
*/
public String codeOrRealCommand ( String key ) {
	if (key == null) return null;
	String text = getString( "action", key );
	if ( text.equals("FIXME") ) {
		text = key;
	}
	return text;
}

// *****************  IMAGES SECTION  *************************

private void init (String[] paths) {
	// minimum resource-path (all "source" files)
	addResourcePath("");
	if (paths != null) {
		for (String p : paths) {
			addResourcePath(p);
		}
	}
	
	// try open the default text bundles
	try {
		openBundle("action");
	} catch (FileNotFoundException e) {
	}
	try {
		openBundle("display");
	} catch (FileNotFoundException e) {
	}
	try {
		openBundle("message");
	} catch (FileNotFoundException e) {
	}
	
	// try read the image key-table (locale relative)
    imageTable = new Properties();
    String path1 = "#system/imagemap_$loc.properties";
    try {
       path1 = Util.substituteText(path1, "$loc", loaderLocale.getLanguage());
       imageTable.load( getResourceStream(path1) );
    } catch ( Exception e ) {
       Log.debug(1, "*** MISSING RESOURCE *** : " + path1 + "\n" + e);

       path1 = "#system/imagemap.properties";
       try {
		   imageTable.load( getResourceStream(path1) );
	   } catch (Exception e1) {
	       Log.debug(1, "*** MISSING RESOURCE *** : " + path1 + "\n" + e);
	       Log.debug(1, "WARNING! No image resources available.");
	   }
    }
}

//******** FOLLOWS STANDARD RESOURCE RETRIEVAL (file or jar protocol) ***************

/** Returns the URL of an image resource which is defined in the image table
 * of this loader.
 *  
 * @param token String image name
 * @return {@code URL}
 */
public URL getImageURL ( String token ) {
   String path = imageTable.getProperty( token );
   if ( path == null ) {
      Log.debug(10, "*** missing image association: " + token);
      return null;
   }

   return getResourceURL(path);
}


/** Returns the image-icon of the given identifier or null if this name is
 * unknown.
 * 
 * @param token String image name
 * @return {@code ImageIcon} or null
 */
public ImageIcon getImageIcon (String token) {
   URL url = getImageURL( token );
   return url == null ? null : new ImageIcon(url);
}

/** Returns the image-icon of the given identifier (token) or an alternative 
 * (failcase) if this name is not found. Returns null if both names are unknown.
 * 
 * @param token String image name
 * @param failcase String alternative image name
 * @return {@code ImageIcon} or null
 */
public ImageIcon getImageIcon (String token, String failcase) {
   URL url = getImageURL(token);
   if (url == null) {
      url = getImageURL(failcase);;
   }
   return url == null ? null : new ImageIcon(url);
}

/** Returns an image-icon from the UIManager's defaults image set or an 
 * alternative (failcase) from this loader's image-list if this name was not 
 * found. Returns null if both names are unknown.
 * 
 * @param token String default image name (UIManager)
 * @param failcase String alternative image name
 * @return {@code ImageIcon} or null
 */
public ImageIcon getDefaultImageIcon (String token, String failcase) {
   ImageIcon icon = (ImageIcon) UIManager.getIcon(token);
   if (icon == null && failcase != null) {
      icon = getImageIcon(failcase);
   }
   return icon;
}

/** Returns the image of the given identifier or null if the name is unknown.
 * 
 * @param id String image name
 * @return {@code Image}
 */
public Image getImage( String id ) {
   ImageIcon icon = getImageIcon( id );
   return icon == null ? null : icon.getImage();
}

/** Returns the image of the given identifier (token) or an alternative image 
 * if this name is not found. Returns null if both names are unknown.
 * 
 * @param name String image name
 * @param failcase String alternative image name
 * @return {@code Image} or null
 */
public Image getImage( String name, String failcase ) {
   ImageIcon icon = getDefaultImageIcon(name, failcase);
   return icon == null ? null : icon.getImage();
}


// *****************  RESOURCE HANDLING  *******************

/** 
 * Returns the text resource from a given resource file denotation.
 * 
 * @param path the full path and filename of the resource requested. If
 * <code>path</code> begins with "#" it is resolved against the program's
 * standard resource folder after removing "#"
 * @param enc the character encoding to be applied for reading the file
 *        (if <b>null</b> the platform default is used)
 * @return String text decoded with the given character encoding or <b>null</b>
 *         if the resource couldn't be obtained
 * @throws IOException
 */
public String getResourceText (String path, String enc) throws IOException {
   InputStream in;
   String res = null;
   
   if ( (in = getResourceStream( path )) != null ) {
	  ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      try {
		 Util.transferData( in, bOut, 2048 );
      } catch (InterruptedException e1) {
		 e1.printStackTrace();
      } finally {
    	 in.close();
      }
      try { 
    	  res = bOut.toString( enc ); 
      } catch ( Exception e ) { 
    	  res = bOut.toString(); 
      }
   }
   return res;
}

/** General use resource InputStream getter.
 * @param path the full path and filename of the resource requested. If
 * <code>path</code> begins with "#" it is resolved against the program's
 * standard resource folder after removing "#"
 * @return an InputStream to read the resource data, or <b>null</b> if the resource
 * could not be obtained
 * @throws java.io.IOException if there was an error opening the input stream
 */
public InputStream getResourceStream ( String path ) throws java.io.IOException {
   URL url = getResourceURL(path);
   return url == null ? null : url.openStream();
}

/** General use resource URL getter.
 * 
 * @param path String full path of the resource requested. If
 * <code>path</code> begins with "#" it is resolved against the program's
 * standard resource folder after removing "#".
 * @return URL or null if the resource was not found
 */
public URL getResourceURL ( String path ) {
   Objects.requireNonNull(path, "path is null");
   URL url = null;

   if (path.startsWith("#")) {
	  // search in all defined resource paths
      for ( Iterator<String> it = resPaths.iterator(); it.hasNext() && url == null; ) {
         String rp = it.next() + path.substring(1);
         url = ResourceLoader.class.getClassLoader().getResource( rp );
//         url = ClassLoader.getSystemResource( rp );
      } 
   } else {
	  // search absolute path
      url = ResourceLoader.class.getClassLoader().getResource( path );
//      url = ClassLoader.getSystemResource( path );
   }

   if ( url == null ) {
	   Log.debug(6, "(ResourceLoader) ** failed locating resource: " + path);
//       System.err.println("(ResourceLoader) * failed locating resource: " + path);
//   } else {
//      System.out.println( "resource URL: " +url.getProtocol() + ", path: " + url.getPath() );
   }
   return url;
}

/** Returns the resource paths which were defined with 'addResource()'.
 * 
 * @return String[]
 */
public String[] getResourcePaths () {
    String[] s = resPaths.toArray(new String[resPaths.size()]);
    return s; 
}

}
