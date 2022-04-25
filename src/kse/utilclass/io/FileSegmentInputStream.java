/*
 *  FileSegmentInputStream in kse.com.jqblite.util
 *  file: FileSegmentInputStream.java
 * 
 *  Project JQuickBase
 *  @author Wolfgang Keller
 *  Created 01.06.2005
 *  Version 0.0.4F
 * 
 *  Copyright (c) 2005 by Wolfgang Keller, Munich, Germany
 * 
 This program is not freeware software but copyright protected to the author(s)
 stated above. However, you can use, redistribute and/or modify it under the terms 
 of the GNU General Public License as published by the Free Software Foundation, 
 version 2 of the License.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 Place - Suite 330, Boston, MA 02111-1307, USA, or go to
 http://www.gnu.org/copyleft/gpl.html.
 */

package kse.utilclass.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 *  FileSegmentInputStream in kse.com.jqblite.util
 */
public class FileSegmentInputStream extends InputStream {
	
   RandomAccessFile datF;
   long streamLength;
   long startPos;
   long markPos;
   long pos;
   
	/**
	 * 
	 */
	public FileSegmentInputStream ( File f, long start, long length )
					throws IOException {
	   long endPos;
	   
	   if ( start < 0 )
	      throw new IllegalArgumentException( "start < 0" );
	   
	   datF = new RandomAccessFile( f, "r" );
	   startPos = start;
	   endPos = length < 0 ? datF.length() : Math.min( datF.length(), start + length );
	   streamLength = Math.max( 0, endPos - startPos ); 
	   datF.seek( startPos );
    }

	@Override
	public int read () throws IOException
	{
	   if ( pos == streamLength )
	      return -1;
	   pos++;
	   return datF.read();
	}


   @Override
   public int available () throws IOException
   {
      return (int)Math.min( streamLength - pos, Integer.MAX_VALUE );
   }
   
   @Override
   public void close () throws IOException
   {
      datF.close();
   }
   
   @Override
   public synchronized void mark ( int readlimit )
   {
      markPos = pos;
   }
   
   @Override
   public boolean markSupported ()
   {
      return true;
   }
   
   @Override
   public synchronized void reset () throws IOException
   {
      pos = markPos;
      datF.seek( startPos + pos );
   }

   @Override
   public int read ( byte[] b, int off, int len ) throws IOException
   {
      len = Math.min( len, available() );
      pos += len;
      return datF.read( b, off, len );
   }

   @Override
   public long skip ( long n ) throws IOException
   {
      long oldPos;
      
      oldPos = pos;
      pos = Math.min( streamLength, pos + n );
      return pos - oldPos;
   }
}
