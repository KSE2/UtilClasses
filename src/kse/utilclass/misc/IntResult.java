/* Ownership Statement
 * 
 * <p>Title: JQuickBase</p>
 * <p>Description: Java ISAM Database Management</p>
 * <p>Copyright: Copyright (c) 2022 Wolfgang Keller</p>
 * @author Wolfgang Keller
 * 
 This program is copyright protected software; you can use, redistribute and/or 
 modify it under the terms of the GNU General Public License as published by the
 Free Software Foundation; version 2 of the License.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 Place - Suite 330, Boston, MA 02111-1307, USA, or go to
 http://www.gnu.org/copyleft/gpl.html.
 */

package kse.utilclass.misc;

/**
 *  Wrapper class for a integer value (int) which may be referenced and modified.
 *  This can be used e.g. to pass a result value from a method to the caller
 *  via the parameter list. 
 */
public class IntResult {
	
    private int i;
   
	/**
	 * Empty Constructor defines a zero integer value. 
	 */
	public IntResult () {}
	
	/**
	 * Constructor which defines the argument integer value. 
	 */
	public IntResult ( int v ) {
	   i = v;
	}
	
	public int getValue () {return i;}
	
	public void setValue ( int v ) {
	   i = v;
	}
}
