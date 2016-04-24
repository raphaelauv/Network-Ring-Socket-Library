package protocol;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class OwnIp {
	public static void main(String[] args) {
		try {
			Enumeration<NetworkInterface> listNi = NetworkInterface.getNetworkInterfaces();
			while (listNi.hasMoreElements()) {
				NetworkInterface nic = listNi.nextElement();				
				
				Enumeration<InetAddress> listIa = nic.getInetAddresses();
				while (listIa.hasMoreElements()) {
					InetAddress iac = listIa.nextElement();
					
					if (iac instanceof Inet4Address) {
						
						if (iac.isLoopbackAddress()) {
						
						}else{
						}
						System.out.println("++++++ InetAddress :");
						System.out.println("++++++ " + iac.toString());
					}
					
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}