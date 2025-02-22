/*
*  File: CommandlineHandler.java
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

package kse.utilclass2.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import kse.utilclass.misc.Log;

/** This commandline handler allows to identify option values given in the 
 * commandline arguments of an application call or any similar use case based  
 * on key-value pairs or as unary options.
 * <p>Options can appear in unary or binary form. The binary form assumes
 * that two consecutive arguments (separated by a blank) in the commandline 
 * constitute a key-value pair, iff the key starts with a signal value. The 
 * signal value has a default of '-' (minus) but can be modified to any text.
 * The value part is assumed null (non-existing) in unary options. The set
 * of unary options is initially empty and if required needs to be defined
 * by the user with a method. 
 * <p><b>Locale Options</b>
 * <br>The handler knows two default options, namely signal+'l' and signal+'c'
 * which are binary options to request a specific locale in the commandline.
 * E.g. "-l de -c au" would request the language GERMAN and the country
 * AUSTRIA. 
 * 
 */
public class CommandlineHandler implements java.io.Serializable {
   private static final long serialVersionUID = 489011337858756818L;
	
   /** Signal that option code starts with */
   public static final String DEFAULT_OPTION_SIGNAL = "-";
   /** Command-line parameter organisation (leading, trailing, mixed) */
   public enum Organisation { LEADING, TRAILING, MIXED } 

   private Map<String, String> options = new HashMap<String, String>();
   private ArrayList<String> arguments = new ArrayList<String>();
   private ArrayList<String> unaries = new ArrayList<String>();

   private Organisation organisation = Organisation.MIXED;
   private Locale locale;
   private String[] args = new String[0];
   private String optionSignal = DEFAULT_OPTION_SIGNAL;

   String languageArg;
   String countryArg;

   /** Creates a new commandline handler in MIXED argument organisation
    * and without initial argument digestion.
    */
   public CommandlineHandler () {
   }

   /** Creates a new commandline handler with the given organisation
    * and without initial argument digestion.
    * 
    * @param org <code>Organisation</code> argument organisation in the line 
    *        (null for default)
    */
   public CommandlineHandler (Organisation org) {
      if (org != null) {
         organisation = org;
      }
   }

   public Organisation getOrganisation() {return organisation;}

   /** Sets the distribution mode for option and plain arguments expected
    * to be realised on the commandline. The possible values are LEADING, 
    * TRAILING and MIXED, which refers to the location of options in relation
    * to plain arguments. The default value is MIXED.
    *  
    * @param org {@code Organisation}
    * @throws IllegalArgumentException if list of arguments is not correctly 
    * 		  organised
    */
   public void setOrganisation (Organisation org) {
	   if (this.organisation != org) {
		   Organisation old = this.organisation;
		   try {
			   this.organisation = org;
			   digest(args);
		   } catch (IllegalArgumentException e) {
			   this.organisation = old;
			   digest(args);
			   throw e;
		   }
	   }
   }

/** Sets the list of unary options in the command line. The argument is a 
 * text string with a set of values separated by blanks. Unary options don't 
 * demand a following value argument. Option codes have to start with the option
 * signal and may not contain a blank. 
 * 
 * @param list String blank separated option codes
 */
public void setUnaryOptions (String list) {
   String[] vs = list.split(" ");
   unaries.clear();
   for (String s : vs) {
      unaries.add(s);
   }
   digest(args);
}

/** Returns the definition list for unary option codes. The list is  a copy.
 * 
 * @return {@code List<String>}
 */
@SuppressWarnings("unchecked")
public List<String> getUnaryOptions () {
	return (List<String>) unaries.clone();
}

/** Sets the signal by which option keys are identified in the commandline 
 * diet. The default value is '-' (minus).
 * 
 * @param signal String, null for default
 */
public void setOptionSignal (String signal) {
	if (signal == null) {
		optionSignal = DEFAULT_OPTION_SIGNAL;
	} else {
		if (signal.isEmpty()) {
			throw new IllegalArgumentException("signal is empty");
		}
		optionSignal = signal; 
	}
	digest(args);
}

/** Removes the results of the recent call to 'digest()'. This does not remove 
 * the definition list for unary options.
 */
public void reset () {
   arguments.clear();
   options.clear();
   locale = null;
   languageArg = null;
   countryArg = null;
   args = new String[0];
   Log.debug(8, "(CommandlineHandler.reset) digest reset");
}

/** The commandline given as diet in a single string (concatenation of 
 * arguments).
 * 
 * @return String, reconstructed argument commandline or empty string if 
 *                 unavailable
 */
public String getCommandline () {
   StringBuffer b = new StringBuffer();
   for (String a : args) {
      b.append( a );
      b.append(" ");
   }
   return b.toString().trim();
}

/** Returns a copy of the array of commandline arguments
 * as stated in the diet of 'digest()'. 
 * (Equivalent to 'getCommandline().split(" ")').
 * 
 * @return String[]
 */
public String[] getOriginalArgs () {
   return Arrays.copyOf(args, args.length);
}

/** Returns a list of the PLAIN arguments of the commandline
 * in the order of their appearance. PLAIN arguments are
 * commandline arguments that are neither an option code
 * nor an option value.
 * 
 * @return <code>List</code> of strings
 */
@SuppressWarnings("unchecked")
public List<String> getArguments () {
   return (List<String>) arguments.clone();
}

/** Returns the option value in the command line for the given option code
 * or <b>null</b> if no value was supplied for this option in the diet.
 * 
 * @param code String option code including the signal (e.g. "--verify")
 * @return String option value or null
 */
public String getOption ( String code ) {
   return options.get(code);
}

/** Returns whether the given option code is correctly defined in the 
 * command-line arguments. 
 * <p>For unary options the return value is true iff its code has been
 * stated in the commandline, for binary options it is true iff the code has
 * been stated and there is a following argument which is not another option 
 * code.  
 * 
 * @param code String option code
 * @return boolean true == option is well defined
 */
public boolean hasOption ( String code ) {
   return options.containsKey( code );
}

/** Digests a line of arguments. This replaces the results of any previous 
 * interpretation. 
 * 
 * @param args String containing all arguments ("diet")
 * @throws IllegalArgumentException if list of arguments is not
 *         correctly organised
 */
public void digest (String args) {
	Objects.requireNonNull(args);
	String[] arr = args.split(" ");
	digest(arr);
}

/**
 * Digests a given set of arguments. This replaces the results of any previous 
 * interpretation. 
 * 
 * @param args String[] all arguments
 * @throws IllegalArgumentException if list of arguments is not
 *         correctly organised
 */
public void digest (String[] args) {
   reset();
   if (args == null) return;
   this.args = args;
   
   String code=null;
   boolean startsPlain=false, startsOption=false;

   // fill our databases
   for (String a : args) {
      
      // react to new option code
      if ( a.startsWith(optionSignal) ) {
         // control line org
         if ( organisation == Organisation.LEADING & startsPlain ) 
            throw new IllegalArgumentException("improper command line organisation: ".concat(a));
         
         // if have previous option code (non-unary)
         if (code != null) {
            Log.debug(8, "(CommandlineHandler.digest) ignoring NULL-OPTION: ".concat(code));
         } 

         code = a;
         startsOption = true;

         // if new code is unary option, place null value in map
         if ( unaries.contains(code) ) {
            options.put(code, null);
            Log.debug(8, "(CommandlineHandler.digest) identified UNARY-OPTION: ".concat(code));
            code = null;
         }
      }

      // react to new value
      else {
         // control line org
         if ( organisation == Organisation.TRAILING & startsOption & code == null ) 
            throw new IllegalArgumentException("improper command line organisation: ".concat(a));
         
         // if have previous option code
         if ( code != null ) {
            options.put(code, a);
            Log.debug(8, "(CommandlineHandler.digest) identified VALUE-OPTION: " + code + " == " + a);
         }
         
         // if plain value (not option bound)
         else {
            arguments.add(a);
            Log.debug(8, "(CommandlineHandler.digest) identified ARGUMENT: ".concat(a));
         }
         code = null;
         startsPlain = true;
      }
   }

   // make a standard interpretation for a Locale
   languageArg = getOption( optionSignal.concat("l") );
   if (languageArg != null) {
       Log.debug(8, "(CommandlineHandler.digest) modified LOCALE language = ".concat(languageArg));
   }
   countryArg = getOption( optionSignal.concat("c") );
   if (countryArg != null) {
       Log.debug(8, "(CommandlineHandler.digest) modified LOCALE country = ".concat(countryArg));
   }
}

/** Returns a locale incorporating any locale specific arguments
 * the caller may have supplied with the diet. Unspecified elements of the 
 * locale are substituted from the VM default locale.
 * 
 * @return <code>Locale</code> locale implied by arguments 
 *         or the system default locale if no relevant argument was supplied
 */
public Locale getLocale () {
   if (locale != null) return locale;
   Locale defL = Locale.getDefault();
   
   // if we have some locale arguments, construct a new locale
   if ( languageArg != null | countryArg != null ) {
      locale = new Locale( 
            languageArg == null ? defL.getLanguage() : languageArg,
            countryArg == null ? defL.getCountry() : countryArg  );
   }
   return locale == null ? defL : locale;
}


}

