package protocol;

/**
 * Force to use a UnsignedLong 
 *
 *
 */
public class UnsignedLong {
	
	private Long value;
	
	public Long getValue(){
		return value;
	}
	
	public UnsignedLong()throws NumberFormatException {
		this.value=Long.parseUnsignedLong("0");
	}
	
	public UnsignedLong(String value) throws NumberFormatException{
		this.value=Long.parseUnsignedLong(value);
	}
	public String toUnsignedString(){
		return Long.toUnsignedString(value);
	}

	public int compareUnsigned(UnsignedLong a , UnsignedLong b){
		return Long.compareUnsigned(a.value,b.value);
	}
	
	public void setValue(UnsignedLong a){
		this.value=a.value;
	}
	
	public void setValue(String newValue) throws NumberFormatException {
		this.value=Long.parseUnsignedLong(newValue);
	}
	public static UnsignedLong divideUnsigned(UnsignedLong dividend, UnsignedLong divisor){
		UnsignedLong tmp=new UnsignedLong();
		tmp.value=Long.divideUnsigned(dividend.value, divisor.value);
		return tmp;
	}
	public static UnsignedLong remainderUnsigned(UnsignedLong dividend, UnsignedLong divisor){
		UnsignedLong tmp=new UnsignedLong();
		tmp.value=Long.remainderUnsigned(dividend.value, divisor.value);
		return tmp;
	}
}
