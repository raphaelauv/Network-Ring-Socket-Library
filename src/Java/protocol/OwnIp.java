package protocol;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedList;

public class OwnIp {
	public static LinkedList<String> getAllIp() {
		try {
			LinkedList<String> allIp = new LinkedList<String>();
			Enumeration<NetworkInterface> listNi = NetworkInterface.getNetworkInterfaces();
			while (listNi.hasMoreElements()) {
				NetworkInterface nic = listNi.nextElement();
				Enumeration<InetAddress> listIa = nic.getInetAddresses();
				while (listIa.hasMoreElements()) {
					InetAddress iac = listIa.nextElement();

					if (iac instanceof Inet4Address) {
						if (iac.isLoopbackAddress()) {}
						allIp.add(iac.getHostAddress());
					}
				}
			}
			return allIp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}