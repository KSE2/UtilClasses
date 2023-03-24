package kse.utilclass2.misc;

/*
*  File: Test_MirrorFileManager.java
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

import static org.junit.Assert.assertTrue;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import kse.utilclass.misc.Util;
import kse.utilclass2.misc.MirrorFileManager.Mirrorable;
import kse.utilclass2.misc.MirrorFileManager.OperationEvent;
import kse.utilclass2.misc.MirrorFileManager.OperationListener;


public class Test_MirrorFileManager  {
    File homeDir;
    File baseDir; 
    File testADir, testBDir;

    public Test_MirrorFileManager () {
        homeDir = new File( System.getProperty("user.home") );
        baseDir = new File( homeDir, "Test_Mirrors");
        testADir = new File( baseDir, "A" );
        testBDir = new File( baseDir, "B" );
        Util.ensureDirectory(baseDir, null);
    }
    
@Test    
public void test_instantiate_errors () {

    // failing no root supplied
    try {
        new MirrorFileManager( null, 60 );
        assertTrue( "instantiate error missing: NullPointer on RootDir", false );
    } catch ( Exception e ) {
        assertTrue( "instantiate error incorrect, 0 ", e instanceof NullPointerException );
    }

    // failing incorrect period supplied 1
    try {
        new MirrorFileManager( baseDir, 0 );
        assertTrue( "instantiate error missing: IllegalArgument on period=0", false );
    } catch ( Exception e ) {
        assertTrue( "instantiate error incorrect, 1", e instanceof IllegalArgumentException );
    }

    // failing incorrect period supplied 2
    try {
        new MirrorFileManager( baseDir, 0 );
        assertTrue( "instantiate error missing: IllegalArgument on period=-1", false );
    } catch ( Exception e ) {
        assertTrue( "instantiate error incorrect, 2", e instanceof IllegalArgumentException );
    }

    // failing false thread priority LOW
    try {
        new MirrorFileManager( baseDir, 30, Thread.MIN_PRIORITY-1 );
        assertTrue( "instantiate error missing: IllegalArgument on threadPriority < Bot", false );
    } catch ( Exception e ) {
        assertTrue( "instantiate error incorrect, 3", e instanceof IllegalArgumentException );
    }

    // failing false thread priority HIGH
    try {
        new MirrorFileManager( baseDir, 30, Thread.MAX_PRIORITY+1 );
        assertTrue( "instantiate error missing: IllegalArgument on threadPriority > Top", false );
    } catch ( Exception e ) {
        assertTrue( "instantiate error incorrect, 4", e instanceof IllegalArgumentException );
    }

}
    
@Test    
public void test_instantiate_1 () {
    int threadPriority = Thread.currentThread().getPriority();
    MirrorFileManager mm = new MirrorFileManager( baseDir, 30 );
    assertTrue( "false root directory reported", mm.getMirrorRootDirectory().equals(baseDir) );
    assertTrue( "false check period after create", mm.getCheckPeriod() == 30 );
    assertTrue( "false thread priority after create", mm.getThreadPriority() == threadPriority );
    assertTrue( "false mirror-file prefix after create", mm.getMirrorFilePrefix().equals(mm.DEFAULT_MIRROR_PREFIX) );
    assertTrue( "false mirror-file suffix after create", mm.getMirrorFileSuffix().equals(mm.DEFAULT_MIRROR_SUFFIX) );
    assertTrue( "file iterator falsely with value after create", mm.iterator().hasNext() == false );
}

@Test    
public void test_instantiate_2 () {
    int threadPriority = Thread.MIN_PRIORITY ;
    MirrorFileManager mm = new MirrorFileManager( baseDir, 30, threadPriority );
    assertTrue( "false root directory reported", mm.getMirrorRootDirectory().equals(baseDir) );
    assertTrue( "false check period after create", mm.getCheckPeriod() == 30 );
    assertTrue( "false thread priority after create", mm.getThreadPriority() == threadPriority );
    assertTrue( "false mirror-file prefix after create", mm.getMirrorFilePrefix().equals(mm.DEFAULT_MIRROR_PREFIX) );
    assertTrue( "false mirror-file suffix after create", mm.getMirrorFileSuffix().equals(mm.DEFAULT_MIRROR_SUFFIX) );
    assertTrue( "file iterator falsely with value after create", mm.iterator().hasNext() == false );
}

@Test
public void test_minor_settings () {
    MirrorFileManager mm = new MirrorFileManager( baseDir, 30, Thread.MIN_PRIORITY );
    mm.setCheckPeriod(200);
    assertTrue( "false check period after setting", mm.getCheckPeriod() == 200 );
    mm.setThreadPriority( Thread.MAX_PRIORITY );
    assertTrue( "false thread priority after Setting", mm.getThreadPriority() == Thread.MAX_PRIORITY );
    String newPrefix = "XXXFILE";
    mm.setMirrorFilePrefix( newPrefix );
    assertTrue( "false mirror-file prefix after Setting", mm.getMirrorFilePrefix().equals( newPrefix ) );
    String newSuffix = ".Xanadu";
    mm.setMirrorFileSuffix( newSuffix );
    assertTrue( "false mirror-file suffix after Setting", mm.getMirrorFileSuffix().equals( newSuffix ) );
    
    // failing incorrect suffix setting
    try {
        mm.setMirrorFileSuffix( "FILE" );
        assertTrue( "suffix setting error missing: IllegalArgument", false );
    } catch ( Exception e ) {
        assertTrue( "suffix setting error incorrect", e instanceof IllegalArgumentException );
    }
}

@Test
public void test_add_listeners () {
    MirrorFileManager mm = new MirrorFileManager( baseDir, 30 );
    OperationListener li1, li2;
    
    li1 = new OurOperationListener("Alpha");
    li2 = new OurOperationListener("Beta");
    
    mm.addOperationListener(li1);
    mm.fireErrorEvent(null, "test error msg : 1", new IllegalArgumentException() );
    assertTrue( "operation list size error", mm.getOperationListeners().size() == 1 );
    mm.addOperationListener(li2);
    mm.fireErrorEvent(null, "test error msg : 2", new IllegalArgumentException() );
    assertTrue( "operation list size error", mm.getOperationListeners().size() == 2 );
    mm.addOperationListener(li2);
    assertTrue( "operation list size error (double entry)", mm.getOperationListeners().size() == 2 );
    mm.addOperationListener(li1);
    assertTrue( "operation list size error (double entry)", mm.getOperationListeners().size() == 2 );
}

@Test
public void test_remove_listeners () {
    MirrorFileManager mm = new MirrorFileManager( baseDir, 30 );
    OperationListener li1, li2, li3;
    
    li1 = new OurOperationListener("Alpha");
    li2 = new OurOperationListener("Beta");
    li3 = new OurOperationListener("Gamma");
    mm.addOperationListener(li1);
    mm.addOperationListener(li2);
    mm.addOperationListener(li3);

    assertTrue( "operation list size error", mm.getOperationListeners().size() == 3 );
    mm.removeOperationListener(li2);
    assertTrue( "operation list size error", mm.getOperationListeners().size() == 2 );
    mm.removeOperationListener(li1);
    assertTrue( "operation list size error", mm.getOperationListeners().size() == 1 );
    mm.removeOperationListener(li3);
    assertTrue( "operation list size error", mm.getOperationListeners().size() == 0 );
    mm.fireErrorEvent(null, "test error msg : zero", new IllegalArgumentException() );
}

private boolean knowsIterable ( Iterator<?> it, Object obj ) {
    boolean ok = false;
    while ( it.hasNext() & !ok ) {
        ok |= it.next() == obj;
    }
    return ok;
}

private int iteratorSize ( Iterator<?> it ) {
    int i = 0;
    while ( it.hasNext() ) {
        i++; it.next();
    }
    return i;
}

private byte[] getFileData ( File f ) throws IOException {
    InputStream in = new FileInputStream( f );
    byte[] buf = new byte[(int)f.length()];
    in.read(buf);
    in.close();
    return buf;
}

@Test
public void test_add_mirrorables () throws InterruptedException {
    MirrorFileManager mm = new MirrorFileManager( baseDir, 30 );
    mm.addOperationListener(new OurOperationListener("Alpha"));
    
    TestMirrorable1 dataF1 = new TestMirrorable1(256);
    TestMirrorable1 dataF2 = new TestMirrorable1(256);
    TestMirrorable1 dataF3 = new TestMirrorable1(26);
    assertTrue( "start value Mirrorables not 0", iteratorSize(mm.iterator()) == 0 );

    // attempting first entries
    
    mm.addMirrorable(dataF1);
    assertTrue( "value Mirrorables not 1", iteratorSize(mm.iterator()) == 1 );
    assertTrue( "list does not hold Mirrorable, 1", knowsIterable( mm.iterator(), dataF1) );
    
    mm.addMirrorable(dataF2);
    assertTrue( "value Mirrorables not 2", iteratorSize(mm.iterator()) == 2 );
    assertTrue( "list does not hold Mirrorable, 2", knowsIterable( mm.iterator(), dataF2 ) );
    
    mm.addMirrorable(dataF3);
    assertTrue( "value Mirrorables not 3", iteratorSize(mm.iterator()) == 3 );
    assertTrue( "list does not hold Mirrorable, 3", knowsIterable( mm.iterator(), dataF3) );
    
    // attempting double entry
    mm.addMirrorable(dataF1);
    assertTrue( "value Mirrorables not 3", iteratorSize(mm.iterator()) == 3 );
    assertTrue( "list does not hold Mirrorable, 1", knowsIterable( mm.iterator(), dataF1) );
    assertTrue( "list does not hold Mirrorable, 2", knowsIterable( mm.iterator(), dataF2 ) );
    
//    dataF1.update(828);
//    Util.sleep(120*1000);
}

@Test
public void test_remove_mirrorables () throws InterruptedException {
    MirrorFileManager mm = new MirrorFileManager( baseDir, 30 );
    mm.addOperationListener(new OurOperationListener("Alpha"));
    
    TestMirrorable1 dataF1 = new TestMirrorable1(256);
    TestMirrorable1 dataF2 = new TestMirrorable1(256);
    TestMirrorable1 dataF3 = new TestMirrorable1(26);
    mm.addMirrorable(dataF1);
    mm.addMirrorable(dataF2);
    mm.addMirrorable(dataF3);
    assertTrue( "start value Mirrorables not 3", iteratorSize(mm.iterator()) == 3 );

    // attempting removes
    
    mm.removeMirrorable(dataF1.getIdentifier());
    assertTrue( "value Mirrorables not 2", iteratorSize(mm.iterator()) == 2 );
    assertTrue( "list holds removed Mirrorable", !knowsIterable( mm.iterator(), dataF1) );
    assertTrue( "list does not hold Mirrorable, 2", knowsIterable( mm.iterator(), dataF2) );
    assertTrue( "list does not hold Mirrorable, 3", knowsIterable( mm.iterator(), dataF3) );

    // attempting void remove
    mm.removeMirrorable(dataF1.getIdentifier());
    assertTrue( "value Mirrorables not 2", iteratorSize(mm.iterator()) == 2 );
    assertTrue( "list does not hold Mirrorable, 2", knowsIterable( mm.iterator(), dataF2) );
    assertTrue( "list does not hold Mirrorable, 3", knowsIterable( mm.iterator(), dataF3) );
    
    mm.removeMirrorable(dataF3.getIdentifier());
    assertTrue( "value Mirrorables not 1", iteratorSize(mm.iterator()) == 1 );
    assertTrue( "list holds removed Mirrorable", !knowsIterable( mm.iterator(), dataF3) );
    assertTrue( "list does not hold Mirrorable, 2", knowsIterable( mm.iterator(), dataF2) );

    mm.removeMirrorable(dataF2.getIdentifier());
    assertTrue( "value Mirrorables not 0 (remove)", iteratorSize(mm.iterator()) == 0 );
    assertTrue( "list holds removed Mirrorable", !knowsIterable( mm.iterator(), dataF2) );

    // test total remove
    mm.addMirrorable(dataF1);
    mm.addMirrorable(dataF2);
    mm.addMirrorable(dataF3);
    mm.removeAllMirrorables();
    assertTrue( "value Mirrorables not 0 (removeAll)", iteratorSize(mm.iterator()) == 0 );

}

@Test
public void test_current_mirrors () throws InterruptedException {
    MirrorFileManager mm = new MirrorFileManager( baseDir, 1 );
    mm.addOperationListener(new OurOperationListener("Alpha"));
    
    TestMirrorable1 dataF1 = new TestMirrorable1(256);
    TestMirrorable1 dataF2 = new TestMirrorable1(256);
    TestMirrorable1 dataF3 = new TestMirrorable1(26);
    
    assertTrue( "illegal returned mirror file object", mm.getCurrentMirror(dataF1.getIdentifier()) == null );

    // TEST: Add and modify Mirrorables (test mirrors as well)

    // one mirrorable
    mm.addMirrorable(dataF1);
    Util.sleep(2000);
    assertTrue( "illegal returned mirror file object", mm.getCurrentMirror(dataF1.getIdentifier()) == null );
    
    dataF1.update();
    Util.sleep(2000);
    File mir1 = mm.getCurrentMirror(dataF1.getIdentifier());
    assertTrue( "falsely void mirror file, 1", mir1 != null );
    assertTrue( "mirror file does not exist, 1", mir1.isFile() );
    try {
        assertTrue( "mirror content does not match", Util.equalArrays( dataF1.data, getFileData(mir1)));
    } catch (IOException e) {
        e.printStackTrace();
        assertTrue( "IO exception thrown: " + e, false );
    }
    
    // more mirrorables
    mm.addMirrorable(dataF2);
    mm.addMirrorable(dataF3);
    Util.sleep(2000);
    assertTrue( "illegal returned mirror file object", mm.getCurrentMirror(dataF2.getIdentifier()) == null );
    assertTrue( "illegal returned mirror file object", mm.getCurrentMirror(dataF3.getIdentifier()) == null );
    
    dataF2.update();
    dataF3.update(329);
    Util.sleep(2000);
    File mir2 = mm.getCurrentMirror(dataF2.getIdentifier());
    File mir3 = mm.getCurrentMirror(dataF3.getIdentifier());
    
    assertTrue( "falsely void mirror file, 2", mir2 != null );
    assertTrue( "mirror file does not exist, 2", mir2.isFile() );
    assertTrue( "falsely void mirror file, 3", mir3 != null );
    assertTrue( "mirror file does not exist, 3", mir3.isFile() );
    try {
        assertTrue( "mirror content does not match", Util.equalArrays( dataF2.data, getFileData(mir2)));
        assertTrue( "mirror content does not match", Util.equalArrays( dataF3.data, getFileData(mir3)));
    } catch (IOException e) {
        e.printStackTrace();
        assertTrue( "IO exception thrown: " + e, false );
    }
    
    // remove mirror org.kse.outsource.files
    mm.removeCurrentMirror(dataF1.getIdentifier());
    assertTrue( "failed to remove current mirror, 1", !mir1.isFile() );
    mir1 = mm.getCurrentMirror(dataF1.getIdentifier());
    assertTrue( "falsly non-void mirror file, 1", mir1 == null );
    
    // renewed update
    dataF1.update();
    Util.sleep(2000);
    mir1 = mm.getCurrentMirror(dataF1.getIdentifier());
    assertTrue( "falsely void mirror file, 1", mir1 != null );
    assertTrue( "mirror file does not exist, 1", mir1.isFile() );
    try {
        assertTrue( "mirror content does not match", Util.equalArrays( dataF1.data, getFileData(mir1)));
    } catch (IOException e) {
        e.printStackTrace();
        assertTrue( "IO exception thrown: " + e, false );
    }

    // remove more org.kse.outsource.files
    mm.removeCurrentMirror(dataF2.getIdentifier());
    mm.removeCurrentMirror(dataF3.getIdentifier());
    assertTrue( "failed to remove current mirror, 2", !mir2.isFile() );
    assertTrue( "failed to remove current mirror, 3", !mir3.isFile() );
    mir2 = mm.getCurrentMirror(dataF2.getIdentifier());
    assertTrue( "falsly non-void mirror file, 2", mir2 == null );
    mir3 = mm.getCurrentMirror(dataF3.getIdentifier());
    assertTrue( "falsly non-void mirror file, 3", mir3 == null );

    // finally remove no. 1
    mm.removeCurrentMirror(dataF1.getIdentifier());
    assertTrue( "failed to remove current mirror, 1", !mir1.isFile() );
    mir1 = mm.getCurrentMirror(dataF1.getIdentifier());
    assertTrue( "falsly non-void mirror file, 1", mir1 == null );
}

@Test
public void test_invokeActivity () throws IOException, InterruptedException {
    MirrorFileManager mm = new MirrorFileManager( baseDir, Integer.MAX_VALUE );
    mm.addOperationListener(new OurOperationListener("Test-Activity"));
    TestMirrorable1 dataF1 = new TestMirrorable1(561);
    
    mm.addMirrorable(dataF1);
    assertTrue( "illegal returned mirror file object", mm.getCurrentMirror(dataF1.getIdentifier()) == null );
    
    dataF1.update();
    Util.sleep(2000);
    File mir1 = mm.getCurrentMirror(dataF1.getIdentifier());
    assertTrue( "falsely non-void mirror file, 1", mir1 == null );
    
    // test invoke activity: one mirror save
    mm.invokeMirrorActivity();
    Util.sleep(2000);
    mir1 = mm.getCurrentMirror(dataF1.getIdentifier());
    byte[] block1 = getFileData(mir1);
    assertTrue( "mirror file does not exist", mir1.isFile() );
    assertTrue( "mirror content does not match, 1", Util.equalArrays( dataF1.data, block1));
    
    // test invoke activity: secondary file modification
    dataF1.update();
    mm.invokeMirrorActivity();
    Util.sleep(2000);
    byte[] block2 = getFileData(mir1);
    assertTrue( "mirror content does not match, 2", Util.equalArrays( dataF1.data, block2));
    assertTrue( "content unmodified", !Util.equalArrays( block1, block2));
    
    // test invoke activity: file modification during PAUSE
    mm.pause();
    dataF1.update();
    mm.invokeMirrorActivity();
    Util.sleep(2000);
    block1 = getFileData(mir1);
    assertTrue( "file content modified during PAUSE", Util.equalArrays( block1, block2));
    
    // test invoke activity: file modification after RESUME
    mm.resume();
    mm.invokeMirrorActivity();
    Util.sleep(2000);
    block1 = getFileData(mir1);
    assertTrue( "file content not modified after RESUME", !Util.equalArrays( block1, block2));
    
    // clean up
    mm.removeCurrentMirror(dataF1.getIdentifier());
}

@Test
public void test_pause () throws IOException, InterruptedException {
    MirrorFileManager mm = new MirrorFileManager( baseDir, 1 );
    mm.addOperationListener(new OurOperationListener("Test-PAUSE"));
    
    TestMirrorable1 dataF1 = new TestMirrorable1(529);
    mm.pause();
    mm.addMirrorable(dataF1);
    assertTrue( "failed to register mirrorable in PAUSE", knowsIterable( mm.iterator(), dataF1 ) );
    mm.resume();
    
    dataF1.update();
    Util.sleep(2000);
    File mir1 = mm.getCurrentMirror(dataF1.getIdentifier());
    byte[] dat1 = getFileData(mir1);
    
    mm.pause();
    TestMirrorable1 dataF2 = new TestMirrorable1(390);
    mm.addMirrorable(dataF2);
    dataF2.update();
    
    dataF1.update();
    Util.sleep(2000);
    dataF1.update();
    Util.sleep(2000);
    assertTrue( "mirrorable content was falsly not updated", !Util.equalArrays( dataF1.data, dat1));
    assertTrue( "mirror content was falsly updated", Util.equalArrays( dat1, getFileData(mir1)));

    mm.resume();
    Util.sleep(2000);
    assertTrue( "mirror content failure F1", Util.equalArrays( dataF1.data, getFileData(mir1)) );
    File mir2 = mm.getCurrentMirror(dataF2.getIdentifier());
    assertTrue( "mirror content failure F2", Util.equalArrays( dataF2.data, getFileData(mir2)) );
    
    mm.removeCurrentMirror(dataF1.getIdentifier());
    mm.removeCurrentMirror(dataF2.getIdentifier());
}

@Test
public void test_history_mirrors () throws IOException, InterruptedException {
    MirrorFileManager mm = new MirrorFileManager( baseDir, 1 );
    mm.addOperationListener(new OurOperationListener("Gamma"));
    
    TestMirrorable1 dataF1 = new TestMirrorable1(256);
    TestMirrorable1 dataF2 = new TestMirrorable1(256);
    TestMirrorable1 dataF3 = new TestMirrorable1(256);
    
    // TEST: HISTORY mirrors for one Mirrorable

    // create a mirror for a Mirrorable (F1)
    mm.addMirrorable(dataF1);
    dataF1.update();
    Util.sleep(2000);
    File mir1 = mm.getCurrentMirror(dataF1.getIdentifier());
    assertTrue( "falsely void mirror file, 1", mir1 != null );
    List<File> histMirs = mm.getHistoryMirrors(dataF1.getIdentifier());
    assertTrue( "falsely non-void history mirrors, 1", histMirs == null || histMirs.size() == 0 );
    
    // restart a manager session for F1
    mm.removeAllMirrorables();
    assertTrue( "falsely non-void mirror file, 1", mm.getCurrentMirror(dataF1.getIdentifier()) == null );
    mm.addMirrorable(dataF1);
    
    // test exists history directory / not exists current mirror
    File hdir1 = new File( mm.getMirrorRootDirectory(), mm.getMirrorName(dataF1.getIdentifier()) );
    assertTrue( "history directory does not exist, 1", hdir1.isDirectory() );
    
    // look for that one history mirror 
    histMirs = mm.getHistoryMirrors(dataF1.getIdentifier());
    assertTrue( "falsely void history mirrors, 1", histMirs != null && !histMirs.isEmpty() );
    assertTrue( "false iterator size: history mirrors, 1", histMirs.size() == 1 );
    histMirs = mm.getHistoryMirrors(dataF1.getIdentifier());
    assertTrue( "history mirror content failure, 1", Util.equalArrays( dataF1.data, getFileData( histMirs.get(0) )) );
    
    // test if hist-mirror was correctly reported
    assertTrue( "no history mirror reported, 1", dataF1.getNrReportedMirrors() == 1 );
    List<File> reportHistMirs = dataF1.getHistoryMirrors();
    assertTrue( "no reported history mirror listed, 1", reportHistMirs.size() == 1 );
    assertTrue( "reported history mirror content failure, 1", Util.equalArrays( dataF1.data, 
                 getFileData( reportHistMirs.get(0) )) );

    // TEST: HISTORY mirrors for multiple Mirrorables
    
    // create mirrors for 2 more Mirrorables (F2, F3)
    mm.addMirrorable(dataF2);
    mm.addMirrorable(dataF3);
    dataF2.update();
    dataF3.update();

    // add a new mirror to F1
    dataF1.update();
    
    Util.sleep(2000);
    mir1 = mm.getCurrentMirror(dataF1.getIdentifier());
    File mir2 = mm.getCurrentMirror(dataF2.getIdentifier());
    File mir3 = mm.getCurrentMirror(dataF3.getIdentifier());
    assertTrue( "falsely void mirror file, 1b", mir1 != null );
    assertTrue( "falsely void mirror file, 2", mir2 != null );
    assertTrue( "falsely void mirror file, 3", mir3 != null );
    assertTrue( "falsely non-void history mirrors, 2", mm.getHistoryMirrors(dataF2.getIdentifier()).isEmpty() );
    assertTrue( "falsely non-void history mirrors, 3", mm.getHistoryMirrors(dataF3.getIdentifier()).isEmpty() );

    // second mod for F3 
    dataF1.update();
    Util.sleep(2000);
    mir3 = mm.getCurrentMirror(dataF3.getIdentifier());
    assertTrue( "falsely void mirror file, 3b", mir3 != null );
    
    // restart a manager session for F1, F2, F3
    mm.removeAllMirrorables();
    assertTrue( "falsely non-void mirror file, 1", mm.getCurrentMirror(dataF1.getIdentifier()) == null );
    assertTrue( "falsely non-void mirror file, 2", mm.getCurrentMirror(dataF2.getIdentifier()) == null );
    assertTrue( "falsely non-void mirror file, 3", mm.getCurrentMirror(dataF3.getIdentifier()) == null );
    mm.addMirrorable(dataF1);
    mm.addMirrorable(dataF2);
    mm.addMirrorable(dataF3);
    
    // test exists history directory / not exists current mirror
    File hdir2 = new File( mm.getMirrorRootDirectory(), mm.getMirrorName(dataF2.getIdentifier()) );
    File hdir3 = new File( mm.getMirrorRootDirectory(), mm.getMirrorName(dataF3.getIdentifier()) );
    assertTrue( "history directory does not exist, 1b", hdir1.isDirectory() );
    assertTrue( "history directory does not exist, 2", hdir2.isDirectory() );
    assertTrue( "history directory does not exist, 3", hdir3.isDirectory() );
    
    // look for history mirrors of F1 (should be 2 now) 
    histMirs = mm.getHistoryMirrors(dataF1.getIdentifier());
    assertTrue( "falsely void history mirrors, 1b", !histMirs.isEmpty() );
    assertTrue( "false list size: history mirrors, 1b", histMirs.size() == 2 );

    // evaluate history mirrors of F1 (one identical, one not)
    histMirs = mm.getHistoryMirrors(dataF1.getIdentifier());
    int countOK = 0, countFalse = 0;
    for ( File mir : histMirs ) {
        if ( Util.equalArrays( dataF1.data, getFileData( mir ))) {
            countOK++;
        } else {
            countFalse++;
        }
    }
    assertTrue( "history mirror content failure, 1b", countOK == 1 );
    assertTrue( "history mirror content failure, 1c", countFalse == 1 );
    
    // look for history mirrors of F2 and F3 
    histMirs = mm.getHistoryMirrors(dataF2.getIdentifier());
    assertTrue( "falsely void history mirrors, 2", !histMirs.isEmpty() );
    assertTrue( "false iterator size: history mirrors, 2", histMirs.size() == 1 );
    histMirs = mm.getHistoryMirrors(dataF2.getIdentifier());
    assertTrue( "history mirror content failure, 2", Util.equalArrays( dataF2.data, getFileData( histMirs.get(0) )) );
    histMirs = mm.getHistoryMirrors(dataF3.getIdentifier());
    assertTrue( "falsely void history mirrors, 3", !histMirs.isEmpty() );
    assertTrue( "false iterator size: history mirrors, 3", histMirs.size() == 1 );
    histMirs = mm.getHistoryMirrors(dataF3.getIdentifier());
    assertTrue( "history mirror content failure, 3", Util.equalArrays( dataF3.data, getFileData( histMirs.get(0) )) );
    
    // test if hist-mirror was correctly reported F2 and F3
    assertTrue( "no history mirror reported, 1b", dataF1.getNrReportedMirrors() == 3 );
    assertTrue( "no history mirror reported, 2", dataF2.getNrReportedMirrors() == 1 );
    assertTrue( "no history mirror reported, 3", dataF3.getNrReportedMirrors() == 1 );
    reportHistMirs = dataF1.getHistoryMirrors();
    assertTrue( "no reported history mirror listed, 1", reportHistMirs.size() == 2 );
    reportHistMirs = dataF2.getHistoryMirrors();
    assertTrue( "no reported history mirror listed, 2", reportHistMirs.size() == 1 );
    reportHistMirs = dataF3.getHistoryMirrors();
    assertTrue( "no reported history mirror listed, 3", reportHistMirs.size() == 1 );
    
    // remove the history mirrors
    mm.removeHistoryMirrors(dataF1.getIdentifier());
    assertTrue( "failed to remove history mirrors: F1", !hdir1.isDirectory() );
    mm.removeHistoryMirrors(dataF2.getIdentifier());
    assertTrue( "failed to remove history mirrors: F2", !hdir2.isDirectory() );
    mm.removeHistoryMirrors(dataF3.getIdentifier());
    assertTrue( "failed to remove history mirrors: F3", !hdir3.isDirectory() );
}

//****************  INNER CLASSES  ****************

private class TestMirrorable1 implements MirrorFileManager.Mirrorable {

    String ID;
    int modNumber;
    int dataLength;
    int mirrorReported;
    byte[] data;
    
    List<File> historyMirrors = new ArrayList<File>();
    
    public TestMirrorable1 ( int dataLength ) {
        data = Util.randBytes(dataLength);
        this.dataLength = dataLength;
        ID = Util.bytesToHex(Util.randBytes(16)).toUpperCase();
    }

    public TestMirrorable1 ( String name, int dataLength ) {
        if ( name == null || name.isEmpty() ) {
            throw new IllegalArgumentException("name is void/empty");
        }
        data = Util.randBytes(dataLength);
        this.dataLength = dataLength;
        ID = name;
    }

    
    public synchronized void update ( int length ) {
        data = Util.randBytes(length);
        this.dataLength = length;
        this.modNumber++;
    }
    
    public synchronized void update () {
        data = Util.randBytes(dataLength);
        this.modNumber++;
    }

    /** The number of history mirror org.kse.outsource.files for this Mirrorable
     *  that have been reported since it was created.
     *    
     * @return int nr of history mirrors
     */
    public int getNrReportedMirrors () {
        return mirrorReported;
    }
    
    /** The history mirrors known for this Mirrorable. 
     * 
     * @return List of File 
     */
    public List<File> getHistoryMirrors () {
        return historyMirrors;
    }
    
    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public int getModifyNumber() {
        return modNumber;
    }

    @Override
    public Mirrorable getMirrorableClone() {
        return null;
    }

    @Override
    public synchronized void mirrorWrite(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        dout.write(data);
    }

    @Override
    public void mirrorsDetected( List<File> mlist ) {
        for ( File mirror : mlist ) {
            System.out.println( "-- mirror file detected for: " + ID );
            System.out.println( "   file: " + mirror.getAbsolutePath() );
        
            mirrorReported++;
            if ( !historyMirrors.contains(mirror) ) {
                historyMirrors.add(mirror);
            }
        }
    }
    
}

private class OurOperationListener implements OperationListener {

    private String myName;
    
    public OurOperationListener ( String name ) {
        name.lastIndexOf('.');
        myName = name;
    }
    
    private String eventTypeStr ( OperationEvent evt ) {
        String text = "??";
        switch ( evt.getEventType() ) {
        case OperationEvent.ERROR_EVENT: text = "ERROR-EVENT";
        break;
        case OperationEvent.LISTCHANGE_EVENT: text = "LIST-CHANGE-EVENT " + (evt.elementAdded() ? ", ADDED" : ", REMOVED");
        break;
        case OperationEvent.SAVESTART_EVENT: text = "SAVE-START-EVENT";
        break;
        case OperationEvent.SAVEREADY_EVENT: text = "SAVE-READY-EVENT";
        break;
        }
        return text + ", msg= " + evt.getErrorMessage();
    }
    
    private void reportEvent ( OperationEvent evt ) {
        System.out.println( "+++ Operation Event (" + myName + "): " + eventTypeStr( evt ));
    }
    
    @Override
    public void saveStarted( OperationEvent evt ) {
        System.out.print( "+++ Operation Event (" + myName + "): " + eventTypeStr( evt ));
        System.out.println( ", mirror=" + evt.getMirrorFile() );
    }

    @Override
    public void fileListChanged( OperationEvent evt ) {
        reportEvent(evt);
        if ( evt.getMirrorable() != null ) {
            System.out.println( "   Mirrorable=" + evt.getMirrorable().getIdentifier() );
        }
    }

    @Override
    public void saveTerminated( OperationEvent evt ) {
        System.out.print( "+++ Operation Event (" + myName + "): " + eventTypeStr( evt ));
        System.out.println( ", mirror=" + evt.getMirrorFile() );
    }

    @Override
    public void errorOccurred( OperationEvent evt ) {
        reportEvent(evt);
        System.out.println( "   Exception=" + evt.getException() );
    }
    
}

}
