package kse.utilclass.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/** Singleton class providing file access synchronisation and XML marshalling /
 * unmarshalling using the Java JAXB package. For using XML-services the
 * application has to annotate the intended classes as described in
 * javax.xml.bind.
 */
public class IOService {

	private static IOService instance = new IOService();

	private Hashtable<File, Semaphore> semaphTable = new Hashtable<>();
	private Semaphore ioSemaphore = new Semaphore(1, true);

	private Unmarshaller unmarshaller;
	private Marshaller marshaller;
	private List<Class> xmlClassList = new ArrayList<Class>();
	
	/** Returns the singleton IOService. 
	 * 
	 * @return {@code IOService}
	 */
	public static IOService get () {
		return instance;
	}
	
	private IOService () {
	}
	
	/**
	 * Registers a new class to be marshalled or unmarshalled.
	 * Registrations have to occur before {@code initXmlSystem()} is called.
	 * 
	 * @param type Class the class to be marshalled or unmarshalled 
	 */
	public void registerXmlClass (Class type) {
		this.xmlClassList.add(type);
	}

	/**
	 * Initialises the XML system. This method has to be called before
	 * any marhalling or unmarshalling is performed.
	 * <p>Creates a Marshaller and an Unmarshaller and does the basic XML setup. 
	 * 
	 * @throws JAXBException if initialisation fails 
	 */
	public void initXmlSystem() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(xmlClassList.toArray(new Class[0]));

		unmarshaller = jc.createUnmarshaller();
		marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
	}

	/**
	 * Marshal an object into an XML file.
	 *
	 * <p>The XML file structure is created from the object's structure. This is
	 * done by annotating the object's class with the usual JAXB annotations.
	 * 
	 * <p>The current thread may be blocked until an access permit for the file
	 * becomes available or it gets interrupted.
	 * 
	 * @param object T object to be marshalled
	 * @param out File output file receiving the marshalled XML content   
	 * @throws JAXBException if something goes wrong during marshalling
	 * @throws IOException 
	 */
	public <T> void marshal (T object, File outFile) throws IOException {
		Objects.requireNonNull(object, "no object supplied");
		Objects.requireNonNull(outFile, "output file is null");
		if (marshaller == null)
			throw new IOException("XML-Manager not initialised");
		
		File file = outFile.getCanonicalFile();
		try {
			acquireFileAccess(file);
			marshaller.marshal(object, file);
		} catch (JAXBException e) {
			throw new JaxBFailureException("error in marshal object", e);
		} catch (InterruptedException e) {
			throw new IOException("interrupted while marshal IO locked", e);
		} finally {
			releaseFileAccess(file);
		}
	}

	/**
	 * Marshal objects into xml files.
	 *
	 * <p>The xml file structure is created from the object's structure. This is
	 * done by annotating the object's class with the usual JAXB annotations.
	 * 
	 * @param object The Object to be marshalled
	 * @param out the output stream containing the marshalled xml   
	 * @throws JAXBException If something goes wrong during marshalling.
	 * @throws IOException 
	 */
	public <T> void marshal (T object, OutputStream out) throws IOException {
		Objects.requireNonNull(object, "no object supplied");
		Objects.requireNonNull(out, "output stream is null");
		if (marshaller == null)
			throw new IOException("XML-Manager not initialised");

		try {
			marshaller.marshal(object, out);
		} catch (JAXBException e) {
			throw new JaxBFailureException("error in marshal object", e);
		}
	}

	/**
	 * Unmarshal objects from xml files.
	 *
	 * The XML file structure must match the object's structure. This is done
	 * annotating the object's class.
	 * 
	 * <p>The current thread may be blocked until an access permit for the file
	 * becomes available or it gets interrupted.
	 * 
	 * @param xmlFile File XML-file from which we want to create a new object
	 * @return T the unmarshalled object
	 * @throws JAXBException if something goes wrong in unmarshalling
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshal (File xmlFile) throws JAXBException, IOException {
		Objects.requireNonNull(xmlFile);
		if (unmarshaller == null)
			throw new IOException("XML-Manager not initialised");
		
		File file = xmlFile.getCanonicalFile();
		try {
			acquireFileAccess(file);
			T result = (T) unmarshaller.unmarshal(file);
			return result;
		} catch (JAXBException e) {
			throw new JaxBFailureException("error in XML unmarshal object", e);
		} catch (InterruptedException e) {
			throw new IOException("interrupted while unmarshal IO locked", e);
		} finally {
			releaseFileAccess(file);
		}
	}
	
	/**
	 * Unmarshal objects from xml streams.
	 *
	 * <p>The xml structure must match the object's structure. This is done
	 * annotating the object's class.
	 *
	 * @param xmlStream The xml stream from which we want to create a new object.
	 * @return T the unmarshalled object.
	 * @throws JAXBException If something goes wrong during unmarshalling.
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshal (InputStream xmlStream) throws IOException {
		Objects.requireNonNull(xmlStream);
		if (unmarshaller == null)
			throw new IOException("XML-Manager not initialised");
		
		T xmlObject;
		try {
			xmlObject = (T) unmarshaller.unmarshal(xmlStream);
//			xmlVerifier.verify(xmlObject);
		} catch (JAXBException e) {
			throw new JaxBFailureException("error in XML unmarshal object", e);
		}
		return xmlObject;
	}
	
	/** Holds the current thread until an access permit is available
	 * for the given file. When this method returns, access to the file
	 * is reserved until a corresponding releaseFileAccess() is called.
	 * Does nothing on a null argument.
	 * 
	 * @param file {@code File} permit reference; may be null
	 * @throws InterruptedException if the current thread is interrupted 
	 * 			while waiting for a permit
	 */
	public void acquireFileAccess (File file) throws InterruptedException {
		if (file == null) return;
		Semaphore sema = semaphTable.get(file);
		if (sema == null) {
			sema = new Semaphore(1, true);
			semaphTable.put(file, sema);
		}
		sema.acquire();
	}
	
	/** Returns a file access permit to the access pool.
	 * Does nothing on a null argument.
	 * 
	 * @param file {@code File} permit reference; may be null 
	 */
	public void releaseFileAccess (File file) {
		if (file == null) return;
		Semaphore sema = semaphTable.get(file);
		if (sema != null) {
			sema.release();
		}
	}
}
