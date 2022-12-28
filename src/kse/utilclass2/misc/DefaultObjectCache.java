package kse.utilclass2.misc;

/*
*  File: DefaultObjectCache.java
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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Objects;

import kse.utilclass.misc.Log;

/**
 * Offers a general purpose, scalable object cache service by implementing the Map interface.
 * This cache can hold any amount of any type objects V mapped from keys K, while excluding <b>null</b>
 * references. It is backed by <code>LinkedHashMap</code> and mapped objects are ordered by access. 
 * Value objects V have to be sub-classes of {@code DefaultObjectCache.CacheObject<E>} which is a
 * wrapper for the user's object.
 * <p>Peculiarity of this class is that cache containment can be limited by three parameters: 
 * 1) number of entries, 2) cached data size, and 3) object idle time (unaccessed). These parameters 
 * can be set during instance creation or modified at any later time of cache existence.        
 * 
 * @author Wolfgang Keller
 *
 * @param <K> map key class
 * @param <V extends CacheObject> map value class
 */

public class DefaultObjectCache <K, V extends DefaultObjectCache.CacheObject<?>> 
       extends LinkedHashMap<K, V> 
{
   /** maximum number of entries in the cache (0=unlimited) */ 
   private int maxEntries;
   /** maximum cached data size (bytes; 0=unlimited) */ 
   private int maxVolume;
   /** maximum entry idle time in milliseconds (0=unlimited) */ 
   private long maxTime;
   /** current total cached data volume (bytes) */
   private int actVolume;
   /** whether the set of entries has changed since start or shutdown() */
   private boolean modified;

   public DefaultObjectCache () {
      super(128, (float)0.5, true);
   }
   
   /** Creates a new object cache with optional size and time limitations.
    * 
    * @param maxEntries int maximum entries allowed in the cache (0 for unlimited)
    * @param maxVolume int maximum total data volume allowed in the cache (0 for unlimited)
    * @param maxTime int maximum time in seconds allowed for an entry to remain idle in the 
    *        cache (0 for unlimited)
    * @throws IllegalArgumentException
    */
   public DefaultObjectCache ( int maxEntries, int maxVolume, int maxTime ) {
      this();
      if ( maxEntries < 0 | maxVolume < 0 | maxTime < 0 ) 
         throw new IllegalArgumentException("illegal negative argument");
      
      this.maxEntries = maxEntries;
      this.maxVolume = maxVolume;
      this.maxTime = (long)maxTime * 1000;
   }

   /** Returns the current data volume claimed by the entirety of
    * all cache objects.
    *  
    * @return int cached data size in bytes
    */
   public int getVolume () {
      return actVolume;
   }
   
   @Override
   protected boolean removeEldestEntry(Entry<K, V> eldest) {
      boolean r1, r2, r3, res = false;
      int diff = 0;
   
      // investigate violation of cache limitations
      r1 = maxEntries > 0 && (diff = size()-maxEntries) > 0;
      r2 = maxVolume > 0 && actVolume > maxVolume;
      r3 = maxTime > 0 && System.currentTimeMillis() - 
           eldest.getValue().accessTime > maxTime;
      
      // if multiple removals possible, perform refresh
      if ( r2 | r3 | diff > 1 ) {
         refresh();
         
      // if only one removal   
      } else if ( r1 ) {
         Log.log(10, "(DefaultObjectCache.removeEldestEntry) --- REMOVE E == "
                      + r1 + r2 + r3 + ",  Vol == " + actVolume);
         CacheObject<?> obj = eldest.getValue();
         actVolume -= obj.getDataSize();
         objectReleased(obj);
         res = true;
      }
      return res; 
   }

   /** Runs through the ordered list of cache objects (least accessed
    * first) and removes all entries which don't fit the cache's 
    * set of limitation conditions.
    * <p>NOTE: Normally it is not required to call this routine, it is supplied 
    * for special purpose only. Also note that in most cases not the entire set
    * of entries needs to be addressed in order to achieve this target. 
    */
   public void refresh () {
      boolean r1, r2, r3, cont;
      boolean entryLim, volumeLim, timeLim;
      Iterator<Entry<K,V>> it;
      
      // investigate cache limitations
      entryLim = maxEntries > 0;
      volumeLim = maxVolume > 0;
      timeLim = maxTime > 0;
      
      // only operate if limitation is set
      if ( entryLim | volumeLim | timeLim ) {
         Log.log(8, "(DefaultObjectCache.refresh) running cache refresh ");
         cont = true;
         // run through entries from eldest to youngest accessed
         for ( it = this.entrySet().iterator(); it.hasNext() & cont; ) {
            CacheObject<?> obj = it.next().getValue();
            Log.log(10, "(DefaultObjectCache.refresh) --- investigating object: " + obj);
   
            // check cache state and entry condition for "remove entry"
            r1 = entryLim && size() > maxEntries;
            r2 = volumeLim && actVolume > maxVolume;
            r3 = timeLim && obj.getAccessAge() > maxTime;
            Log.log(10, "(DefaultObjectCache.refresh) --- investigating object: R3 == " 
                        + r3 + " AT=" + obj.getAccessAge());
            
            // remove entry if cache limitations are violated
            cont = r1;
            if ( r1 | r2 | r3 ) {
               Log.log(10, "(DefaultObjectCache.refresh) --- REMOVE R == " 
                           + r1 + r2 + r3 + ",  Vol == " + actVolume);
               it.remove();
            }
         }
      }
   }
   
   @Override
   public V get (Object key) {
      V v = super.get(key);
      if ( v != null ) {
         v.accessTime = System.currentTimeMillis();
         Log.log(10, "(DefaultObjectCache.get) --- GETTING OBJECT:  " + key );
      }
      return v;
   }

   @Override
   public void clear() {
      super.clear();
      actVolume = 0;
      modified();
   }

   /** Operates like method <code>clear()</code> but notifies all contained 
    * objects via method <code>objectReleased()</code> before they are removed
    * from this cache. Resets the "modified" marker of this cache.
    */
   public void shutdown () {
      for ( CacheObject<?> o : super.values() ) {
         objectReleased(o);
      }
      clear();
      resetModified();
   }
   
   /** Resets the "modified" marker of this object cache. */
   public void resetModified() {
      modified = false;
   }
   
   private void modified() {
      modified = true;
   }

   /** Returns whether the set of entries of this object cache has been modified
    * since initialisation or last call to <code>shutdown()</code> or <code>
    * resetModified()</code>. (This does not
    * reflect modifications within the objects held by this cache!)
    *  
    * @return boolean modified
    */
   public boolean isModified () {
      return modified;
   }
   
   @Override
   public V put(K key, V value) {
      // entry control
      if ( key == null ) 
         throw new NullPointerException("key == null");
      if ( value == null ) 
         throw new NullPointerException("value == null");
      if ( maxVolume > 0 && value.getDataSize() > maxVolume ) 
         throw new IllegalArgumentException("cannot insert entry, value data size exceeds cache maximum");
      
      V v = null;
      actVolume += value.getDataSize();
      
      // we put "super.put" in a TRY because we executed volume update occur before "put"
      // otherwise space reduction becomes incorrect during "refresh" method
      // via "removeEldestEntry"
      try {
         v = super.put(key, value);
         Log.log(10, "(DefaultObjectCache.put) --- ENTER OBJECT:  " + key );
         modified();
      } catch ( RuntimeException e ) {
         actVolume -= value.getDataSize();
         throw e;
      }
      value.accessTime = System.currentTimeMillis();
      if ( v != null ) {
         actVolume -= v.getDataSize();
      }
      return v; 
   }

   @Override
   public V remove(Object key) {
      Log.log(10, "(DefaultObjectCache.remove) --- REMOVE OBJECT:  " + key );
      V v = super.remove(key);
      if ( v != null ) {
         objectReleased(v);
         actVolume -= v.getDataSize();
         modified();
      }
      return v;
   }

   /** This method may be overridden by a subclass and indicates that the 
    * parameter cache object has been scheduled for release from the cache. 
    * Thus application can engage in activity connected to object removal 
    * as e.g. release or saving of associated resources. The method of this 
    * implementation does nothing.
    * 
    * @param obj CacheObject cache object to be released
    */
   protected void objectReleased ( CacheObject<?> obj ) {
   }
   
   /** Maximum number of entries allowed in this cache.
    * 
    * @return int maximum cache entries
    */
   public int getMaxEntries() {
      return maxEntries;
   }

   /** Sets maximum number of entries x in this cache. Any entry x+1 will cause
    * the eldest entry to be released from the cache. Zero sets unlimited number.
    * 
   * @param maxEntries int maximum entries allowed in the cache (0 for unlimited)
   * @throws IllegalArgumentException
    */
   public void setMaxEntries(int maxEntries) {
      if ( maxEntries < 0 ) 
         throw new IllegalArgumentException("illegal negative argument");
      this.maxEntries = maxEntries;
   }

   /** The maximum cache data size in bytes. 
    * @return int maximum data size
    */
   public int getMaxVolume () {
      return maxVolume;
   }

   /** Sets maximum size of data in bytes x in this cache. Entry causing at least 
    * x+1 total cache data size will trigger the eldest entry to be released from 
    * the cache. Zero sets unlimited size.
    * 
    * @param maxVolume int maximum data volume allowed in the cache 
    *        (0 for unlimited)
    * @throws IllegalArgumentException
    */
   public void setMaxVolume (int maxVolume) {
      if ( maxVolume < 0 ) 
         throw new IllegalArgumentException("illegal negative argument");
      this.maxVolume = maxVolume;
   }

   /** The maximum entry idle time in seconds (time since most recent access event
    * on an entry).
    * 
    * @return int cache entry maximum idle time
    */
   public int getMaxTime () {
      return (int)(maxTime / 1000);
   }

   /** Sets maximum time in seconds that an entry is allowed to remain in cache 
    * without an access event. Any entry causes the removal of one or more of the
    * eldest entries in this cache if their age is found to exceed the given 
    * time limit. Zero sets unlimited time.
    * 
    * @param maxTime int maximum entry idle time in seconds (0 for unlimited)
    * @throws IllegalArgumentException
    */
   public void setMaxTime (int maxTime) {
      if ( maxTime < 0 ) 
         throw new IllegalArgumentException("illegal negative argument");
      this.maxTime = (long)maxTime * 1000;
   }
   
   // -------------  INNER CLASSES  --------------
   
   /** Defines the wrapper cache object type. It can be seen as a set containing
    * only one element, the user object of type E.
    * 
    */
   public static class CacheObject<E> {
      
      protected E object;
      protected int size;
      protected long accessTime;
      
      /** Creates a new wrapper object for a given user object of type E
       * and a nominal data size of zero. This constructor can be used if the
       * cache is not limited by data volume.
       *  
       * @param obj E user object
       * @throws NullPointerException
       */
      public CacheObject ( E obj ) {
    	  this(obj, 0);
      }
      
      /** Creates a new wrapper object for a given user object of type E
       * and its current nominal data size. Any positive size value including
       * zero can be given.
       *  
       * @param obj E user object
       * @param volume int non-negative data size in bytes
       * @throws NullPointerException
       * @throws IllegalArgumentException
       */
      public CacheObject ( E obj, int volume ) {
    	 Objects.requireNonNull(obj, "object is null");
         if ( volume < 0 )
            throw new IllegalArgumentException("negative volume value");
         
         object = obj;
         size = volume;
         accessTime = System.currentTimeMillis();
      }

      /** Returns the wrapped user object. 
       * 
       * @return Object wrapped user object (cached)
       */
      public E getObject() {
         return object;
      }

      /** Returns the user object data size (as reported).
       * 
       * @return int object data size
       */
      public int getDataSize() {
         return size;
      }

      /** Returns the absolute time (epoch time) which marks
       * the last access event on this cache object.
       * 
       * @return long epoch time (milliseconds)
       */
      public long getAccessTime() {
         return accessTime;
      }

      /** Returns the time elapsed since last access occurred
       *  to this cache object.
       *  
       * @return long time in milliseconds
       */
      public long getAccessAge () {
         return System.currentTimeMillis() - accessTime;
      }
      
      @Override
      public int hashCode() {
         return object.hashCode();
      }

      /** Two cache objects are equal if their wrapped objects
       * are equal.
       */
      @Override
      public boolean equals (Object obj) {
         return obj != null && obj instanceof CacheObject &&
        		object.equals(((CacheObject<?>)obj).object);
      }

      @Override
      public String toString () {
         return "CacheObject: ".concat(object.toString());
      }
      
   }

}
