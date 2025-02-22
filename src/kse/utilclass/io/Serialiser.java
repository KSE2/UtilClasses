package kse.utilclass.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Objects;

/**
 * Class to perform object serialisation and de-serialisation along the
 * Java-Serialisation framework.
 * <p>This requires objects to comply with the {@code java.io.Serializable} 
 * interface. In their tree of super-class and local member data there
 * must not exist any non-transient, non-static and non-serialisable object.
 * For reliable external serialisations an object class should contain
 * a preferably {@code static long serialVersionUID} member with a unique value.
 */
public class Serialiser {

	public byte[] serialise (Object object) throws NotSerializableException {
	   Objects.requireNonNull(object);
	   if (!isSerialisableClass(object.getClass()))
		  throw new NotSerializableException("non-serialisable object-class: " + object.getClass().getName());
	  
	   ByteArrayOutputStream out = new ByteArrayOutputStream(128);
	   ObjectOutputStream oos = null;
	   try {
		  oos = new ObjectOutputStream(out);
    	  oos.writeObject(object);
    	  oos.close();
    	  byte[] result = out.toByteArray();
    	  return result;
	   } catch (NotSerializableException e) {
		  throw e;
	   } catch (IOException e) {
		  throw new IllegalStateException("serialisation write error", e);
	   }
	}

	public Object deserialiseObject (byte[] buffer) throws IOException {
		Objects.requireNonNull(buffer);
		InputStream input = new ByteArrayInputStream(buffer);
		Object object;
		try {
			ObjectInputStream ois = new ObjectInputStream(input);
			object = ois.readObject();
			ois.close();
		} catch (ClassNotFoundException | InvalidClassException e) {
		    throw new IOException("(deserialise) object class not available", e);
		} catch (StreamCorruptedException e) {
			throw new IOException("(deserialise) stream integrity error", e);
		} catch (Exception e) {
			throw new IOException("(deserialise) general error", e);
		}
		return object;
	}

	public boolean isSerialisableClass (Class<?> c) {
		try {
			c.asSubclass(Serializable.class);
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

}
