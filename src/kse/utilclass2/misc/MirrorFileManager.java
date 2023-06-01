package kse.utilclass2.misc;

/*
*  File: MirrorFileManager.java
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import kse.utilclass.misc.Util;


/** 
 * Class that allows automated mirroring of data files into copies or other kind of
 * representations on a separate storage area (currently on local disk space) on a 
 * frequent basis.
 * 
 * <p>Data handling classes which implement the <code>MirrorableFile</code> interface
 * can hereby create relation to automated data saving (mirroring mechanism) which runs
 * silently in the background. In the regular case this mechanism would function as a
 * safeguard parallel to user-triggered save operations, but other solutions are possible.
 */

public class MirrorFileManager {

    public static final String DEFAULT_MIRROR_SUFFIX = ".bak";
    public static final String DEFAULT_MIRROR_PREFIX = "mir-";

    private static final String CHECKTHREAD_NAME = "MirrorFileManager.filechecking";  
    private static final String SAVETHREAD_NAME = "MirrorFileManager.saving";
    
    private Hashtable<String, Mirrorable>  mfList = new Hashtable<>();
    private HashMap<String, MirrorFileAdminRecord> mfRecords = new HashMap<>(); 
    private ArrayList<OperationListener> operListeners;

    private CheckThread     checkThread; 
    
    private File            mirrorRootDir;
    private String          mirrorFilePrefix = DEFAULT_MIRROR_PREFIX;
    private String          mirrorFileSuffix = DEFAULT_MIRROR_SUFFIX;
    private int             checkPeriod;
    private boolean         terminated;
    

    /** Creates a new mirror file manager with the same background thread priority
     * as the calling thread.
     * 
     * @param rootDirectory <code>File</code> location of mirror files
     * @param period int time in seconds between file investigation loops
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public MirrorFileManager (File rootDirectory, int period) {
        this(rootDirectory, period, Thread.currentThread().getPriority());
    }
    
    /** Creates a new mirror file manager.
     * 
     * @param rootDirectory <code>File</code> location of mirror files
     * @param period int time in seconds (&gt;0) between file investigation loops
     * @param threadPriority int thread priority setting for this manager's background tasks
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public MirrorFileManager (File rootDirectory, int period, int threadPriority) {
        if ( rootDirectory == null ) {
            throw new NullPointerException( "root dir missing");
        }
        if ( period < 1 ) {
            throw new IllegalArgumentException( "illegal check period: " + period );
        }
        if ( threadPriority < Thread.MIN_PRIORITY | threadPriority > Thread.MAX_PRIORITY ) {
            throw new IllegalArgumentException( "illegal thread priority: " + threadPriority );
        }
            
        mirrorRootDir = rootDirectory.getAbsoluteFile();
        Util.ensureDirectory(mirrorRootDir, null);
        checkPeriod = period;
        init( threadPriority );
    }

    private void init (int threadPriority) {
        // create the file checking thread
        checkThread = new CheckThread(threadPriority);
        checkThread.start();
    }

    @Override
	public void finalize () {
        exit();
    }
    
    /** Sets the filename prefix for mirror files.
     * The prefix can be left void by setting it to "" or
     * <b>null</b>. It has a default value of <code>DEFAULT_MIRROR_PREFIX</code>.
     * 
     * @param prefix String, may be <b>null</b>
     */
    public void setMirrorFilePrefix ( String prefix ) {
        mirrorFilePrefix = prefix == null ? "" : prefix;
    }
    
    /** Sets the filename extension (suffix) for mirror files.
     * The suffix must not be void. It has a default value 
     * of <code>DEFAULT_MIRROR_SUFFIX</code>.
     * 
     * @param suffix String, may be <b>null</b>
     * @throws IllegalArgumentException
     */
    public void setMirrorFileSuffix ( String suffix ) {
        if ( suffix == null || suffix.length() < 2 || 
             suffix.indexOf('.') != 0  )
            throw new IllegalArgumentException("suffix is empty or improperly set (must start with '.')");
        
        mirrorFileSuffix = suffix;
    }
    
    /** Returns the service thread's file-check period in seconds.
     * @return int seconds
     */
    public int getCheckPeriod() {
        return checkPeriod;
    }
    
    /** Sets the service thread's file-check period in seconds.
     * 
     * @param period int seconds, greater 0
     */
    public void setCheckPeriod (int period) {
        if ( period < 1 ) {
            throw new IllegalArgumentException( "illegal check period: " + period );
        }
        this.checkPeriod = period;
    }
    
    /** Returns the mirror-file name prefix of this manager.
     * @return String prefix
     */
    public String getMirrorFilePrefix() {
        return mirrorFilePrefix;
    }
    
    /** Returns the mirror-file name suffix of this manager.
     * @return String suffix
     */
    public String getMirrorFileSuffix() {
        return mirrorFileSuffix;
    }

    /** Returns the priority setting of the file-check service thread.
     * @return int priority
     */
    public int getThreadPriority () {
        return checkThread.getPriority();
    }
    
    /** Sets the priority of the file-check service thread.
     * @param int threadPriority
     */
    public void setThreadPriority ( int threadPriority ) {
        checkThread.setPriority( threadPriority );
    }
    
    /** Causes the file-controlling thread of this manager to pause
     * execution until a call to <code>resume()</code> occurs.
     * (After calling this method it is still possible that an ongoing
     * mirror-save thread is executing, however it is guaranteed that 
     * no new thread is scheduled.)
     */
    public void pause () {
        checkThread.pause();
    }

    /** Causes the file-controlling thread of this manager to
     * resume execution after it has been paused.
     */
    public void resume () {
        checkThread.endPause();
    }

    /** Terminally stops execution of the file-controlling thread.
     * No further call-back functions or event dispatches will be 
     * executed.
     */
    public void exit () {
        checkThread.terminate();
        terminated = true;
    }
    
    /**
     * Triggers off mirror save activity for all registered Mirrorable files.
     * The save is only performed on files with an open modified marker.
     * This command is designed to provoke immediate action from the controller
     * thread and ends a possible sleeping state. CAUTION! This command 
     * is not required for regular execution of the manager's controller!
     */
    public void invokeMirrorActivity ()
    {
        if ( checkThread.isAlive() & !terminated ) {
            checkThread.kick();
        }   
    }

    /** Returns the root directory for the manager to
     * store and retrieve mirror files. The rendered
     * file is an absolute path.
     *  
     * @return <code>File</code> mirror manager root directory 
     */
    public File getMirrorRootDirectory () {
        return mirrorRootDir;
    }
    
    /** Returns the mirror-file of the current program session
     * for the given {@code Mirrorable} or <b>null</b> if it doesn't
     * exists. (For deleting the mirror file, method <code>
     * removeCurrentMirror()</code> should be used to allow the 
     * manager for proper state updates.)
     * 
     * @param identifier String identifier of a {@code Mirrorable}
     * @return <code>File</code> current session mirror file or <b>null</b>
     *         if unavailable
     */
    public File getCurrentMirror ( String identifier ) {
        MirrorFileAdminRecord rec = getFileAdminRec(identifier);
        return rec == null ? null : rec.mirrorFile;
    }
    
    /** Returns a list of all stored history mirrors for a given Mirrorable. 
     * History mirrors are mirror files of previous program sessions. They can 
     * amount to any number. The returned list is sorted after descending time
     * values of the files (youngest first).
     * 
     * <p>It is save to delete rendered files at any time as they do not 
     * interfere with the mirror saving mechanics.
     * <p>NOTE: An IO-error during read access to the history directory
     * is not recognised by this routine. In such a case an empty
     * list is returned.
     * 
     * @param identifier String identifier of a {@code Mirrorable}
     * @return {@code List<File>} history mirror files (may be empty)
     * @throws IllegalArgumentException if identifier is unknown
     */
    public List<File> getHistoryMirrors ( String identifier ) {
        MirrorFileAdminRecord rec = getFileAdminRec(identifier);
        if (rec == null)
        	throw new IllegalArgumentException("(MirrorFileManager) unknown Mirrorable name : " + identifier);
        
        // retrieve all mirror files in the specific history dir
        // MirrorFileAdminRecord serves as list filter
        File dir = rec.getHistoryDir();
        File[] fileArr = dir.listFiles(rec);
        if (fileArr == null) return new ArrayList<File>();

        // sort array after descending time value
        Comparator<File> comparator = new Comparator<File>() {
			@Override
			public int compare (File f1, File f2) {
				long t1 = f1.lastModified();
				long t2 = f2.lastModified();
				return t1 == t2 ? 0 : t1 > t2 ? -1 : 1;
			}
		};
        Arrays.sort(fileArr, comparator);

        return Arrays.asList(fileArr); 
    }

    /** Removes all history mirror files of the given Mirrorable file.
     * (This also removes the specific history directory for this file
     * if the directory is empty after deletion of the mirrors.)
     *  
     * @param identifier String identifier of a {@code Mirrorable}
     * @return boolean <b>true</b> if and only if all listed
     *         mirror files were deleted. On <b>false</b> the appl.
     *         should check <code>getHistoryMirrorIterator()</code>
     *         for files which could not get deleted.
     * @throws IllegalArgumentException if identifier is unknown
     */
    public boolean removeHistoryMirrors ( String identifier ) {
        boolean ok = true;
        MirrorFileAdminRecord rec = getFileAdminRec(identifier);
        if (rec == null) {
        	throw new IllegalArgumentException("(MirrorFileManager) unknown Mirrorable name : " + identifier);
        }
        
        // delete all history mirrors as known by the iterator
        List<File> hList = getHistoryMirrors(identifier); 
        for (File mir : hList) {
            ok &= mir.delete();
        }

        // delete the directory
        File dir = rec.getHistoryDir();
        dir.delete();
        return ok;
    }
    
    /** Removes the current session mirror of the given {@code Mirrorable}.
     * If a current mirror save thread is ongoing, deletion will take 
     * place immediately after saving has terminated. 
     * 
     * @param identifier String identifier of a {@code Mirrorable}
     */
    public void removeCurrentMirror ( String identifier ) {
    	Mirrorable f = getMirrorable(identifier);
        MirrorFileAdminRecord rec = getFileAdminRec(identifier);
        
        // operate if Mirrorable is registered and a mirror file saved
        if ( f != null & rec != null && rec.mirrorFile != null ) {
            // if a save-thread is running, just mark the file for later deletion
            if ( rec.isSavingActive() ) {
                rec.deleteMarked = true;
            // otherwise attempt delete and update save-number    
            } else if ( rec.mirrorFile.delete() ) {
                rec.fileSaveNumber = f.getModifyNumber();
                rec.mirrorFile = null;
            } else {
                fireErrorEvent(rec, "unable to erase mirror-file: "
                    .concat(rec.mirrorFile.getAbsolutePath()), null);
            }
        }
    }
    
    /** Iterator over contained Mirrorable objects.
     * 
     * @return <code>Iterator&lt;MirrorableFile&gt;</code>
     */
    @SuppressWarnings("unchecked")
    public Iterator<Mirrorable> iterator () {
         return ((Hashtable<String, Mirrorable>)mfList.clone()).values().iterator();
    }
    
    /** This method checks whether a mirror of the mirrorable file is present in the 
     * current mirror directory. If so, it moves the mirror file (renamed) into a special 
     * directory for the mirrorable.
     */
    protected void controlMirrors ( MirrorFileAdminRecord admin ) {
       File mdir, mir=null, copy;
       String hstr;
       
       try {
          mir = admin.getMirrorFileDef();
          mdir = admin.getHistoryDir();
    
          // if a MIRROR exists for this database (primary occurrence)
          if ( mir.exists() ) {
             // copy mirror file into private mirror directory
             // create private directory if needed
             Util.ensureDirectory( mdir, null );
             copy = File.createTempFile( mirrorFilePrefix, mirrorFileSuffix, mdir );
             Util.copyFile(mir, copy, true);
//             Log.debug( 7, "(MirrorFileManager.controlMirrors) created private MIRROR copy: "
//                   .concat( copy.getAbsolutePath() ));
    
             // delete original mirror file
             mir.delete();
          }
          
          
          // else check for erasing an empty history mirror directory 
          else if ( mdir.isDirectory() && mdir.listFiles().length == 0 ) {
             mdir.delete();
//             Log.debug( 7, "(PwsFileContainer.controlMirrors) removed private MIRROR directory: "
//                   .concat( mdir.getAbsolutePath() ));
          }
    
       } catch ( Exception e ) {
          e.printStackTrace();
          hstr = "WARNING! Cannot copy Mirror file<br><font color=\"green\">" + 
          mir.getAbsolutePath() + "</font>";

          fireErrorEvent( admin, hstr, e );
       }
    }  // controlMirrors

    /** This method lists all history mirror files of a specific 
     * {@code Mirrorable} and issues a "mirror file found" event 
     * (callback method of the {@code Mirrorable} object).
     * 
     * @param admin {@code MirrorFileAdminRecord}
     */
    protected void reportHistoryMirrors ( Mirrorable file ) {
       List<File> mirList = getHistoryMirrors(file.getIdentifier());
       if ( !mirList.isEmpty() ) {
          // fire event of "mirror-files found"
          fireMirrorFilesFound(file, mirList);
       }
    }  // controlMirrors

    /** Adds a Mirrorable to administration in this manager. Does nothing if
     * a Mirrorable of same identity has already been added.
     * <p>Also see the class description! 
     * 
     * @param f <code>Mirrorable</code>
     * @return boolean true = argument was added, false = nothing added
     * @throws IllegalArgumentException if there is no identifier defined 
     */
    public boolean addMirrorable ( Mirrorable f ) {
    	Objects.requireNonNull(f);
    	String id = f.getIdentifier();
    	if (id == null || id.isEmpty())
    		throw new IllegalArgumentException();
    	
        if ( !(terminated || mfList.containsKey(id)) ) {
            mfList.put(id, f);
            MirrorFileAdminRecord adminRec = new MirrorFileAdminRecord(f);
            mfRecords.put(f.getIdentifier(), adminRec);
            fireListModified(adminRec, true);
            controlMirrors(adminRec);
            reportHistoryMirrors(f);
            return true;
        }
        return false;
    }
    
    /** Removes the given Mirrorable from administration in
     * this mirror file manager. This does not remove
     * any mirror files! After the Mirrorable has been removed,
     * this manager cannot remove any of its mirror files.
     * (Also see the class description!) 
     * 
     * @param identifier String identifier of a {@code Mirrorable}
     */
    public void removeMirrorable ( String identifier ) {
        if ( mfList.remove(identifier) != null ) {
            MirrorFileAdminRecord rec = mfRecords.remove(identifier);
            fireListModified(rec, false);
        }
    }
    
    /** Removes all registered {@code Mirrorable} objects from administration in
     * this mirror file manager. This does not perform any removal
     * of mirror files! (Also see the class description!) 
     */
    public void removeAllMirrorables () {
        mfList.clear();
        mfRecords.clear();
        fireListModified(null, false);
    }
    
    /** Returns the file administration record for a specific 
     * {@code Mirrorable} or <b>null</b> if it does not exists.
     * 
     * @param identifier String identifier of a {@code Mirrorable}
     * @return <code>MirrorFileAdminRecord</code> or <b>null</b>
     */
    protected MirrorFileAdminRecord getFileAdminRec ( String identifier ) {
    	Objects.requireNonNull(identifier);
        return mfRecords.get(identifier);
    }

    
//  **************  EVENT HANDLING  ***************    

    /** Returns a clone of the current list of operation listeners
     * in this manager. If no member is available the empty list is 
     * returned.
     * 
     * @return List&lt;OperationListener&gt; 
     */
    @SuppressWarnings("unchecked")
    protected List<OperationListener> getOperationListeners () {
        if ( operListeners != null ) {
            ArrayList<OperationListener> list;
            synchronized (operListeners) {
                list = (ArrayList<OperationListener>) operListeners.clone();
            }
            return list;
        }
        return new ArrayList<OperationListener>();
    }
    
    private void dispatchOperationEvent( OperationEvent evt ) {
        if ( evt != null & operListeners != null ) {
            List<OperationListener> list = getOperationListeners();
            for (OperationListener li : list) {
                switch ( evt.getEventType() ) {
                    case OperationEvent.SAVESTART_EVENT: li.saveStarted(evt);
                    break;
                    case OperationEvent.SAVEREADY_EVENT: li.saveTerminated(evt);
                    break;
                    case OperationEvent.ERROR_EVENT: li.errorOccurred(evt);
                    break;
                    case OperationEvent.LISTCHANGE_EVENT: li.fileListChanged(evt);
                    break;
                }
            }
        }
    }
    
    protected void fireListModified( MirrorFileAdminRecord adminRec, boolean added ) {
        OperationEvent evt = new OperationEvent( this, added, adminRec );
        dispatchOperationEvent(evt);
    }

    protected void fireSaveStarted( MirrorFileAdminRecord adminRec ) {
        OperationEvent evt = new OperationEvent( this, OperationEvent.SAVESTART_EVENT, adminRec );
        dispatchOperationEvent(evt);
    }

    protected void fireSavePerformed(MirrorFileAdminRecord adminRec) {
        OperationEvent evt = new OperationEvent( this, OperationEvent.SAVEREADY_EVENT, adminRec );
        dispatchOperationEvent(evt);
    }
    
    protected void fireMirrorFilesFound( Mirrorable mir, List<File> files ) {
    	if (!files.isEmpty()) {
    		mir.mirrorsDetected(files);
    	}
    }
    
    protected void fireErrorEvent(MirrorFileAdminRecord adminRec, String hstr, Throwable e) {
        if ( e != null ) {
            e.printStackTrace();
            OperationEvent evt = new OperationEvent( this, e, hstr, adminRec );
            dispatchOperationEvent(evt);
        }
    }
    
    public void addOperationListener ( OperationListener oli ) {
        if ( operListeners == null ) {
            operListeners = new ArrayList<OperationListener>();
        }
        synchronized ( operListeners ) {
            if ( !operListeners.contains(oli) ) {
                operListeners.add(oli);
            }
        }
    }

    public void removeOperationListener ( OperationListener oli ) {
        if ( operListeners != null ) 
        synchronized ( operListeners ) {
            operListeners.remove(oli);
        }
    }
    
//  **************  INTERNAL CLASSES  ***************    
    
    public interface OperationListener {

        /** Called to indicate that a mirror save operation has started. 
         * Details about files can be obtained from the event object.
         *    
         * @param evt <code>OperationEvent</code>
         */
        public void saveStarted( OperationEvent evt );
        
        /** Called when a <code>MirrorableFile</code> is added or removed to/from
         * the manager. The file can be obtained from the event object.
         *  
         * @param evt <code>OperationEvent</code>
         */
        public void fileListChanged(OperationEvent evt);

        /** Called to indicate that a mirror save operation has just terminated successfully. 
         * Details about files can be obtained from the event object.
         *    
         * @param evt <code>OperationEvent</code>
         */
        public void saveTerminated( OperationEvent evt );
        
        
        /** Called to indicate that an IO-error has occurred in background operations. 
         * Details about the error and involved file can be obtained 
         * from the event object.
         *    
         * @param evt <code>OperationEvent</code>
         */
        public void errorOccurred ( OperationEvent evt );
    }
    
    public interface Mirrorable {

        /** Returns a string that identifies the mirrorable source object
         * in the application system in a way that is stable over 
         * different sessions of the user program. In the regular case 
         * an absolute file path of the file which is to be mirrored is sufficient.
         * If different media channels are used, a likewise stable identifier
         * of the channel can be added to the identifier.  
         * 
         * @return <code>String</code> persistent object identifier 
         */
        public String getIdentifier ();
        
        /** Returns the number associated with this file which marks
         * its latest modified state. All modification steps performed
         * on the mirrorable file should result in increment of this number.
         *  
         * @return int modify number
         */
        public int getModifyNumber ();
        
        /** Returns a <code>MirrorableFile</code> object which represents
         * a data clone of the source file. If this method returns not <b>null</b>
         * the mirror manager will perform IO activity in relation to this mirror file
         * through calling the clone instead of the original. (This is handy if the
         * clone is created much quicker than the mirror file can be written, and if
         * enough space can be assumed available in VM. IO to the mirror file is
         * always done in a background thread, but working on the clone will not block
         * other work on the original by synchronisation blocks.)
         * <p>Note: Only method <code>mirrorWrite()</code> will ever
         * be called from the manager on the clone.
         * @see mirrorWrite()
         *  
         * @return <code>MirrorableFile</code> "snapshot" clone or <b>null</b>
         */
        public Mirrorable getMirrorableClone ();
        
        /** Performs output of a persistent state of the mirrorable file to
         * the mirror. The implementing class must ensure that during performance 
         * of this routine no other thread can modify the relevant data 
         * content of this file.  
         *      
         * @param out <code>OutputStream</code>
         */
        public void mirrorWrite ( OutputStream out ) throws IOException;

        /**
         * This method is called by the manager in response to 
         * <code>addMirrorable()</code> indicating that at least one history 
         * mirror file for the added {@code Mirrorable} has been found.
         * The returned list is sorted after descending time values of the 
         * files (youngest first).
         * 
         * @param files {@code List<File>} history mirror files
         */
        public void mirrorsDetected ( List<File> files );
    }
    
    public static class OperationEvent extends EventObject  {
        private static final long serialVersionUID = 5386975459896213823L;
        public static final int SAVESTART_EVENT = 1;
        public static final int SAVEREADY_EVENT = 2;
        public static final int ERROR_EVENT = 3;
        public static final int LISTCHANGE_EVENT = 4;
        
        MirrorFileAdminRecord adminRec;
        Throwable exception;
        String message;
        int eventType;
        boolean elementAdded;
        
        /** Creates a new operation event.
         * 
         * @param source <code>Object</code> object where event occurred
         * @param type int event type (this class)
         * @param rec <code>MirrorFileAdminRecord</code> mirrorable admin record 
         *                 or <b>null</b> if unavailable
         * @throws IllegalArgumentException if source is null or type undefined
         */
        private OperationEvent(Object source, int type, MirrorFileAdminRecord rec) {
            super(source);
            this.eventType = type;

            // if we have an ADMIN-RECORD
            if ( rec != null ) {
                // for data security replace record by clone
                this.adminRec = rec.clone();
                // add the mirror file definition in SAVESTART event if not supplied by caller
                if ( type == SAVESTART_EVENT & this.adminRec.mirrorFile == null ) {
                    this.adminRec.mirrorFile = rec.getMirrorFileDef();
                }
            }
            
            // exclude false event types
            if ( type < 1 | type > 4 ) {
                throw new IllegalArgumentException("event type unknown: " + type);
            }
        }

        /** Creates a new operation event for the LISTCHANGE EVENT. 
         * 
         * @param source <code>Object</code> object where error occurred
         * @param elementAdded boolean whether an element was ADDED (REMOVED when <b>false</b>)
         * @param adminRec <code>MirrorFileAdminRecord</code> mirrorable admin record 
         *                 or <b>null</b> if unavailable
         * @throws IllegalArgumentException if source is null
         */
        public OperationEvent(Object source, boolean elementAdded, MirrorFileAdminRecord adminRec) {
            this(source, OperationEvent.LISTCHANGE_EVENT, adminRec );
            this.elementAdded = elementAdded;
        }

        /** Creates a new operation event with error information (ERROR EVENT). 
         * 
         * @param source <code>Object</code> object where error occurred
         * @param e <code>Throwable</code> or <b>null</b>
         * @param hstr <code>String</code> additional error message or <b>null</b>
         * @param adminRec <code>MirrorFileAdminRecord</code> mirrorable admin record 
         *                 or <b>null</b> if unavailable
         * @throws IllegalArgumentException if source is null
         */
        public OperationEvent(Object source, Throwable e, String hstr, MirrorFileAdminRecord adminRec) {
            this(source, OperationEvent.ERROR_EVENT, adminRec );
            this.exception = e;
            this.message = hstr;
        }

        /** The event type as defined by the constants of this class.
         * @return int event type
        public int getType() {
            return eventType;
        }
         */

        public Mirrorable getMirrorable () {
            return adminRec == null ? null : adminRec.file;
        }
        
        public File getMirrorFile () {
            return adminRec == null ? null : adminRec.mirrorFile;
        }

        public Throwable getException() {
            return exception;
        }

        public String getErrorMessage() {
            return message;
        }

        /** The event type as defined by the constants of this class.
         * @return int event type
         */
        public int getEventType() {
            return eventType;
        }
        
        /** Whether a <code>Mirrorable</code> object was added to the 
         * manager's LIST. This serves as a subtype to event type 
         * LISTCHANGE_EVENT.
         *  
         * @return boolean <code>Mirrorable</code> added
         */
        public boolean elementAdded () {
            return elementAdded;
        }
    }
    
    /** This thread regularly tests all Mirrorable entries for their modified
     * state and initiates file saving threads when required to mirror new
     * data states. It's a daemon thread which runs as long as this mirror-file-
     * manager is operative.
     */
    private class CheckThread extends Thread {

        private boolean terminate;
        private boolean pausing;
        
        public CheckThread( int priority ) {
            super( CHECKTHREAD_NAME );
            setDaemon(true);
            setPriority( priority );
        }

        @Override
        public void run() {
            System.out.println( "# MirrorFileManager started" );
            
            while ( !terminate ) {
                // sleep for the designed time period ("checkPeriod" seconds)
                try {
                    Thread.sleep(checkPeriod*1000L);
                } catch (InterruptedException e) {
                }
                
                // into wait-state when PAUSE is set
                if ( pausing ) synchronized (this) {
                    try { wait();
                    } catch (InterruptedException e) {
                    }
                }
                
                // investigate all registered Mirrorable files
                // and start mirror save-threads as required by files' current modify-number
                if ( !(pausing | terminate) ) {
                    @SuppressWarnings("unchecked")
                    Mirrorable[] marr = mfList.values().toArray(new Mirrorable[mfList.size()]);
                    for ( Mirrorable f : marr ) {
                        MirrorFileAdminRecord admin = getFileAdminRec(f.getIdentifier());
                        
                        // check if not another save thread is still running
                        // in this case ignore this Mirrorable
                        if ( admin.saveThread != null && admin.saveThread.isAlive() ) {
                            continue;
                        }
						admin.saveThread = null;

                        // create and start a file-save thread if modified marker is set
                        // for this Mirrorable
                        if ( f.getModifyNumber() != admin.fileSaveNumber ) {
                            admin.saveThread = new FileSaveThread( f, admin );
                            admin.saveThread.start();
                        }
                    }
                }
            }
            System.out.println( "# MirrorFileManager terminated" );
        }

        /** Causes this check-thread to pause execution until a call to <code>
         * endPause</code> occurs.
         */
        public void pause () {
            pausing = true;
        }
        
        /** Causes this check-thread to continue execution if it is in
         * PAUSING state.
         */
        public synchronized void endPause () {
            pausing = false;
            notify();
        }
        
        /** Causes this thread to terminally stop execution. */
        public void terminate () {
            terminate = true;
            pausing = false;
            interrupt();
        }
        
        /** If this thread is sleeping, this will attempt to immediately awake it.
         * Does nothing if this thread is PAUSED.
         */
        public void kick () {
            if ( !pausing) {
                interrupt();
                interrupted();
            }
        }
    }
    
    private class FileSaveThread extends Thread {

    	private Mirrorable file;
        private int modifyNumber;
        private MirrorFileAdminRecord adminRec;

        public FileSaveThread( Mirrorable f, MirrorFileAdminRecord admin ) {
            super( SAVETHREAD_NAME );
            file = f;
            modifyNumber = f.getModifyNumber();
            adminRec = admin;
            setDaemon(false);
        }

        @Override
        public void run() {
            fireSaveStarted( adminRec );
            
            // attempt working on file clone
            Mirrorable opFile = file.getMirrorableClone();
            opFile = opFile == null ? file : opFile;

            // determine name of the mirror file to be created
            // and ensure existence of the target directory
            File targetFile = adminRec.getMirrorFileDef();
            Util.ensureFilePath(targetFile, null);
            
            // define the temporary file which is written as a precursor to the target
            File tmpFile = new File( targetFile.getAbsolutePath() + ".tmp" );

            // let application write persistent file data
            OutputStream fileOut;
            try {
                fileOut = new BufferedOutputStream(new FileOutputStream(tmpFile));
                adminRec.mirrorFile = null;
                opFile.mirrorWrite(fileOut);
                fileOut.close();
                adminRec.mirrorFile = targetFile;
            } catch (FileNotFoundException e) {
                fireErrorEvent(adminRec, "cannot create mirror file", e);
            } catch (IOException e1) {
                fireErrorEvent(adminRec, "cannot write mirror file", e1);
            }
            
            // remove current mirror (if present) and rename temporary file
            try {
            	if (targetFile.exists() && !targetFile.delete()) {
            		throw new IOException("unable to delete file: " + targetFile);
            	}
            	if (!tmpFile.renameTo(targetFile)) {
            		throw new IOException("unable to rename file: " + targetFile);
            	}
            } catch (Exception e) {
                fireErrorEvent(adminRec, "error in renaming mirror temporary file", e);
			}

            // update save-state
            adminRec.fileSaveNumber = modifyNumber;
            fireSavePerformed( adminRec );
            
            // erase the mirror if delete-marked in admin record
            if ( adminRec.deleteMarked ) {
                boolean ok = targetFile.delete();
                adminRec.deleteMarked = false;
                if ( !ok ) {
                    fireErrorEvent(adminRec, "cannot delete mirror file!", null);
                }
            }
        }
    }

    
    protected class MirrorFileAdminRecord implements FileFilter, Cloneable {
        // always value
        Mirrorable file;
        String fileID;
        int fileSaveNumber;
        boolean deleteMarked;
        
        // may be null
        File mirrorFile;  // not null if current mirror file is assumed to exist
        FileSaveThread saveThread;  // not null if a mirror save has taken or is taking place
        
        public MirrorFileAdminRecord (Mirrorable f) {
        	Objects.requireNonNull(f);
            file = f;
            fileSaveNumber = f.getModifyNumber();

            // create mirrorable identifier string
            String path = f.getIdentifier();
            byte[] idcode;
            if ( path == null || path.isEmpty() ) {
                // create a random name if no path is supplied 
                idcode = Util.randBytes(8);
            } else {
                // otherwise create a fingerprint name of the file path 
                idcode = Util.fingerPrint(path);
                idcode = Util.arraycopy(idcode, 8);
            }
            fileID = Util.bytesToHex( idcode );
            
            // note mirror file if exists in defined location
            File mir = getMirrorFileDef();
            mirrorFile = mir.isFile() ? mir : null;
        }
        
        @Override
		public MirrorFileAdminRecord clone () {
            try {
                MirrorFileAdminRecord cl = (MirrorFileAdminRecord) super.clone();
                return cl;
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        /** Returns <b>true</b> if and only if there currently is a 
         * file-save thread running for the mirror file of this record.
         * 
         * @return boolean 
         */
        public boolean isSavingActive () {
            return saveThread != null && saveThread.isAlive();
        }

        /** Returns the unique file definition for the current mirror file
         * (absolute path). 
         * 
         * @param File mirror file definition
         */
        public File getMirrorFileDef () {
           String path = mirrorFilePrefix + fileID + mirrorFileSuffix;
           return new File( getMirrorRootDirectory(), path );
        }
        
        /** Returns the file definition for the directory 
         * within the mirror root directory that may hold history mirror 
         * files of the mirrorable file.
         * (Rendered file does not imply this directory exists!)
         * 
         * @return <code>File</code> directory (absolute path) 
         */
        public File getHistoryDir () {
            return new File(getMirrorRootDirectory(), fileID);
        }

        @Override
        /**
         * We filter mirror files of this manager as identified by
         * prefix and suffix.
         *  
         * @param pathname File candidate for a mirror-file
         * @return boolean true iff argument designates a valid mirror-file path
         */
        public boolean accept( File pathname ) {
            String filename = pathname.getName();
            return pathname.isFile() && filename.endsWith(mirrorFileSuffix) 
                   && filename.startsWith(mirrorFilePrefix);
        }

    }

    /** Returns the hex-based identifier for mirror files of the given 
     * {@code Mirrorable}.
     * <p>This ID showing a hex-number is used as the name-core of the current 
     * mirror file of the given Mirrorable and for its history sub-directory 
     * under the mirror root directory.
     *   
     * @param identifier String identifier of a {@code Mirrorable}
     * @return String mirror ID or null if identifier is unknown
     */
    public String getMirrorName( String identifier ) {
        MirrorFileAdminRecord adminRec = getFileAdminRec(identifier);
        return adminRec == null ? null : adminRec.fileID;
    }

    /** Returns the {@code Mirrorable} of the given name (identifier) or null
     * if such an object was not found.
     * 
     * @param name String identifier of a {@code Mirrorable} 
     * @return {@code Mirrorable}
     */
    public Mirrorable getMirrorable (String name) {
    	return name == null ? null : mfList.get(name);
    }
    
    /** Sets the valid modify number for a specific <code>Mirrorable</code>
     * to the currently supplied state. This sets the given <code>Mirrorable</code>
     * free of mirror file creation until the next increment of its modify number.
     * 
     * @param identifier String identifier of a <code>Mirrorable</code>
     */
    public void setMirrorableSaved ( String identifier ) {
       Mirrorable f = getMirrorable(identifier);
       MirrorFileAdminRecord adminRec = getFileAdminRec(identifier);
       if (adminRec != null) {
          adminRec.fileSaveNumber = f.getModifyNumber();
       }
    }

}
