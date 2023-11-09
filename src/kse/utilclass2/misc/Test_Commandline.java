package kse.utilclass2.misc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import kse.utilclass.misc.Log;
import kse.utilclass2.misc.CommandlineHandler.Organisation;

public class Test_Commandline {

	public Test_Commandline() {
		Log.setLogLevel(10);
		Log.setDebugLevel(10);
		Log.setLogging(true);
		Log.setDebug(true);
	}

	@Test
	public void init () {
		CommandlineHandler hdl = new CommandlineHandler();
		assertTrue(hdl.getOrganisation() == Organisation.MIXED);
		List<String> list = hdl.getArguments();
		assertNotNull(list);
		assertTrue(list.isEmpty());
		
		assertNotNull("missing locale", hdl.getLocale());
		assertTrue(hdl.getCommandline().isEmpty());
		assertTrue("expected default locale", hdl.getLocale().equals(Locale.getDefault()));
		
		assertNotNull(hdl.getOriginalArgs());
		assertTrue(hdl.getOriginalArgs().length == 0);
		
		assertNull(hdl.getOption(""));
		assertNull(hdl.getOption("ee"));
		assertFalse(hdl.hasOption(""));
		assertFalse(hdl.hasOption("ee"));
		
		hdl = new CommandlineHandler(Organisation.LEADING);
		assertTrue(hdl.getOrganisation() == Organisation.LEADING);
		hdl = new CommandlineHandler(Organisation.TRAILING);
		assertTrue(hdl.getOrganisation() == Organisation.TRAILING);
	}
	
	@Test
	public void commandline_arguments () {
		CommandlineHandler hdl = new CommandlineHandler();
		String line = "-a -b arg1 arg2 -der kummar";
		hdl.digest(line);

		// test command-line
		String cmdL = hdl.getCommandline();
		assertTrue("commandline error: [" + cmdL +"]", line.equals(cmdL));
		String[] args = hdl.getOriginalArgs();
		assertTrue(args.length == 6);
		assertTrue("cmdline args error", Arrays.equals(args, line.split(" ")));

		// test plain arguments (non-option arguments)
		List<String> list = hdl.getArguments();
		assertNotNull(list);
		assertTrue("plain argument list error", list.size() == 1);
		assertTrue("plain argument value", "arg2".equals(list.get(0)));
		
		// plain arguments after setting of unary options
		hdl.setUnaryOptions("-b -der");
		list = hdl.getArguments();
		assertTrue("plain argument list error", list.size() == 3);
		assertTrue("plain argument value", "arg1".equals(list.get(0)));
		assertTrue("plain argument value", "arg2".equals(list.get(1)));
		assertTrue("plain argument value", "kummar".equals(list.get(2)));
		
		// command-line after setting of unary options
		cmdL = hdl.getCommandline();
		assertTrue("commandline error: [" + cmdL +"]", line.equals(cmdL));
		
		// failure: modify organisation to LEADING
		try {
			hdl.setOrganisation(Organisation.LEADING);
		} catch (IllegalArgumentException e) {
			assertTrue(hdl.getOrganisation() == Organisation.MIXED);
			cmdL = hdl.getCommandline();
			assertTrue("commandline error: [" + cmdL +"]", line.equals(cmdL));
		}
		
		// failure: modify organisation to TRAILING
		try {
			hdl.setOrganisation(Organisation.TRAILING);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertTrue(hdl.getOrganisation() == Organisation.MIXED);
			cmdL = hdl.getCommandline();
			assertTrue("commandline error: [" + cmdL +"]", line.equals(cmdL));
		}

		// tolerate incomplete argument line (binary option last)
		line = "-a -b arg1 arg2 -der";
		hdl.setUnaryOptions("-b");
		hdl.digest(line);
	}
	
	@Test
	public void getOption () {
		CommandlineHandler hdl = new CommandlineHandler();
		String line = "-a -b arg1 arg2 -der kummar -ber";
		hdl.digest(line);

		// interpret all as binary options
		String val = hdl.getOption("-b");
		assertNotNull("option not found: -b", val);
		assertTrue(val.equals("arg1"));
		assertTrue(hdl.hasOption("-b"));

		val = hdl.getOption("-der");
		assertNotNull("option not found: -der", val);
		assertTrue(val.equals("kummar"));
		assertTrue(hdl.hasOption("-der"));
		
		assertNull("unexpected option value for -a", hdl.getOption("-a"));
		assertNull("unexpected option value for -ber", hdl.getOption("-ber"));
		assertFalse(hdl.hasOption("-a"));
		assertFalse(hdl.hasOption("-ber"));

		// define some unary options
		hdl.setUnaryOptions("-b -ber");
		assertFalse(hdl.hasOption("-a"));
		assertTrue(hdl.hasOption("-b"));
		assertTrue(hdl.hasOption("-ber"));
		
		val = hdl.getOption("-der");
		assertNotNull("option not found: -der", val);
		assertTrue(val.equals("kummar"));
		assertTrue(hdl.hasOption("-der"));
		
		// try extended signal notation
		line = "--ver arg1 arg2 --tar 00";
		hdl.setUnaryOptions("--ver");
		hdl.digest(line);
		assertTrue(hdl.hasOption("--ver"));
		assertFalse(hdl.hasOption("-ver"));
		assertTrue(hdl.getArguments().size() == 2);
		assertTrue(hdl.hasOption("--tar"));
		assertFalse(hdl.hasOption("-tar"));
		assertTrue("00".equals(hdl.getOption("--tar")));
	}
	
	@Test
	public void getLocale () {
		CommandlineHandler hdl = new CommandlineHandler();
		Locale defLoc = Locale.getDefault();
		assertTrue(hdl.getLocale() == defLoc);
		
		String line = "-l FR -a -b kummar";
		hdl.digest(line);
		Locale loc = hdl.getLocale();
		assertNotNull(loc);
		assertTrue(loc.getLanguage().equals("fr"));
		assertTrue(loc.getCountry().equals(defLoc.getCountry()));
		
		line = "-l DK -c xz -a -b kummar";
		hdl.digest(line);
		loc = hdl.getLocale();
		assertNotNull(loc);
		assertTrue(loc.getLanguage().equals("dk"));
		assertTrue(loc.getCountry().equals("XZ"));
	}
	
	@Test
	public void reset () {
		CommandlineHandler hdl = new CommandlineHandler();
		String line = "-a -b arg1 arg2 -der kummar -ber -c DK -l en";
		hdl.digest(line);
		hdl.setUnaryOptions("-b -ber");
		
		hdl.reset();
		assertTrue(hdl.getCommandline().isEmpty());
		assertTrue(hdl.getArguments().isEmpty());
		assertTrue(hdl.getOriginalArgs().length == 0);
		assertTrue(hdl.getLocale().equals(Locale.getDefault()));
	}
}
