package kse.utilclass.misc;

public class TwoTuple implements Cloneable {
	public long v1, v2; 
	
	public TwoTuple (long v1, long v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public TwoTuple (int v1, int v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	@Override
	public Object clone() {
		TwoTuple c;
		try {
			c = (TwoTuple) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		return c;
	}
	
	@Override
	public int hashCode() {
		return (int) (v1 ^ v2);
	}

	@Override
	public boolean equals (Object obj) {
		if (!(obj instanceof TwoTuple)) return false;
		TwoTuple o = (TwoTuple)obj;
		return o.v1 == v1 && o.v2 == v2;
	}

	@Override
	public String toString() {
		return "(" + v1 + ", " + v2 + ")";
	}

	public int intVal1 () {return (int)v1;}

	public int intVal2 () {return (int)v2;}
	
	public long longVal1 () {return v1;}

	public long longVal2 () {return v2;}
	
}
