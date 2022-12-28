package kse.utilclass.misc;

/*
*  File: Log.java
* 
*  Project JUtilClasses
*  @author Wolfgang Keller
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

import java.io.PrintStream;

/**
 *  Log in org.jpws.pwslib.global
 *  <p>Protocol, log and debug printing class.
 */
public class Log
{
   public static final int DEFAULT_DEBUGLEVEL = 2;
   public static final int DEFAULT_LOGLEVEL = 2;
   
   public static PrintStream out = System.out;
   public static PrintStream err = System.err;
   
   private static int debugLevel = DEFAULT_DEBUGLEVEL;
   private static int logLevel = DEFAULT_LOGLEVEL;
   private static boolean debug = false;
   private static boolean logging = false;
   
/**
 * 
 */
private Log () {
}

public static void debug ( int level, Object obj ) {
   if ( debug && level <= debugLevel )
      out.println( "[jqb] DEB: " + String.valueOf(obj) );
}

public static void debug ( int level, String str ) {
   debug( level, (Object)str );
}

public static void setDebug ( boolean v ) {
   debug = v;
}

public static void setDebugLevel ( int v ) {
   debugLevel = v;
}

public static void log ( int level, String str )
{
   if ( logging && level <= logLevel )
      out.println( "[jqb] log: " + str );
}

public static void setLogging ( boolean v ) {
   logging = v;
}

public static void setLogLevel ( int v ) {
   logLevel = v;
}

/** The current logging report level.
 *  @since 0-3-0
 */
public static int getLogLevel () {
   return logLevel;
}

/** The current debugging report level.
 *  @since 0-3-0
 */
public static int getDebugLevel () {
   return debugLevel;
}

public static boolean getDebug () {return debug;}

public static boolean getLogging () {return logging;}

public static void error ( int level, Object obj ) {
   if ( debug && level <= debugLevel )
      err.println( "[jqb] ERR: *** " + String.valueOf(obj) );
}

public static void error ( int level, String str ) {
   error( level, (Object)str );
}


}
