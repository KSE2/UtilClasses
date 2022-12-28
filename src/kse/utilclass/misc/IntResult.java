package kse.utilclass.misc;

/*
*  File: IntResult.java
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

/* $Id: HashMac.java,v 1.5 2005/05/27 07:33:10 somebody Exp $
 *
 * Copyright (C) 1995-2000 The Cryptix Foundation Limited.
 * All rights reserved.
 *
 * Use, modification, copying and distribution of this software is subject to
 * the terms and conditions of the Cryptix General Licence. You should have
 * received a copy of the Cryptix General Licence along with this library;
 * if not, you can download a copy from http://www.cryptix.org/ .
 *
 * Modified: Wolfgang Keller, 2004 
 */

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
