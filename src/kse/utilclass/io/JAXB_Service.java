package kse.utilclass.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JAXB_Service {

	private static JAXB_Service instance = new JAXB_Service();

	/** Returns the singleton JAXB_Service. 
	 * 
	 * @return {@code JAXB_Service}
	 */
	public static JAXB_Service get () {
		return instance;
	}
	

	private Unmarshaller unmarshaller;
	private Marshaller marshaller;
	private List<Class<?>> xmlClassList = new ArrayList<Class<?>>();

	
	private JAXB_Service () {
	}

	/**
	 * Registers a new class to be marshalled or unmarshalled.
	 * Registrations have to occur before {@code initXmlSystem()} is called.
	 * 
	 * @param type Class the class to be marshalled or unmarshalled 
	 */
	public void registerXmlClass (Class<?> type) {
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
	 * @param outFile File output file receiving the marshalled XML content   
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
			IOService.get().acquireFileAccess(file);
			marshaller.marshal(object, file);
		} catch (JAXBException e) {
			throw new JaxBFailureException("error in marshal object", e);
		} catch (InterruptedException e) {
			throw new IOException("interrupted while marshal IO locked", e);
		} finally {
			IOService.get().releaseFileAccess(file);
		}
	}

	/**
	 * Marshal an object into an output stream.
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
	 * Returns the marshalling of an object as a String text.
	 *
	 * <p>The xml file structure is created from the object's structure. This is
	 * done by annotating the object's class with the usual JAXB annotations.
	 * 
	 * @param object The Object to be marshalled
	 * @return String containing the XML serialisation
	 * @throws JAXBException If something goes wrong during marshalling.
	 * @throws IOException 
	 */
	public <T> String marshal (T object) throws IOException {
		Objects.requireNonNull(object, "no object supplied");
		if (marshaller == null)
			throw new IOException("XML-Manager not initialised");

		try {
			StringWriter writer = new StringWriter(128);
			marshaller.marshal(object, writer);
			return writer.toString();
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
			IOService.get().acquireFileAccess(file);
			T result = (T) unmarshaller.unmarshal(file);
			return result;
		} catch (JAXBException e) {
			throw new JaxBFailureException("error in XML unmarshal object", e);
		} catch (InterruptedException e) {
			throw new IOException("interrupted while unmarshal IO locked", e);
		} finally {
			IOService.get().releaseFileAccess(file);
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
	
}
