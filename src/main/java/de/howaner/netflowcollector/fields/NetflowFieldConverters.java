package de.howaner.netflowcollector.fields;

import de.howaner.netflowcollector.exception.ConvertException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class NetflowFieldConverters {

	public static final NetflowFieldConverter READ_UNSIGNED_COUNTER = (buf, field) -> {
		switch (field.getLength()) {
			case 1: return buf.readUnsignedByte();
			case 2: return buf.readUnsignedShort();
			case 4: return buf.readUnsignedInt();
			case 8: return buf.readLong();  // Unsigned not supported :(
			default: throw new ConvertException(buf, field, "Not supported counter length");
		}
	};

	public static final NetflowFieldConverter READ_BYTE = (buf, field) -> {
		return buf.readByte();
	};

	public static final NetflowFieldConverter READ_SHORT = (buf, field) -> {
		return buf.readShort();
	};

	public static final NetflowFieldConverter READ_INT = (buf, field) -> {
		return buf.readInt();
	};

	public static final NetflowFieldConverter READ_PORT = (buf, field) -> {
		return buf.readUnsignedShort();
	};

	public static final NetflowFieldConverter READ_IPv4 = (buf, field) -> {
		byte[] data = new byte[4];
		buf.readBytes(data);

		try {
			return Inet4Address.getByAddress(data).getHostAddress();
		} catch (UnknownHostException ex) {
			throw new ConvertException(buf, field, "Can't convert ipv4 address");
		}
	};

	public static final NetflowFieldConverter READ_TIME = (buf, field) -> {
		// TODO
		return buf.readUnsignedInt();
	};

	public static final NetflowFieldConverter READ_IPv6 = (buf, field) -> {
		byte[] data = new byte[16];
		buf.readBytes(data);

		try {
			return Inet6Address.getByAddress(data).getHostAddress();
		} catch (UnknownHostException ex) {
			throw new ConvertException(buf, field, "Can't convert ipv6 address");
		}
	};

	public static final NetflowFieldConverter READ_IPv6_FLOW_LABEL = (buf, field) -> {
		// See RFC-2460. Flow label is 20 bits long ...
		byte[] data = new byte[3];
		buf.readBytes(data);

		return (((data[0] & 0xF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF));
	};

	public static final NetflowFieldConverter READ_MAC = (buf, field) -> {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			byte b = buf.readByte();
			if (builder.length() > 0)
				builder.append(':');
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	};

	public static final NetflowFieldConverter READ_STRING = (buf, field) -> {
		byte[] data = new byte[field.getLength()];
		buf.readBytes(data);

		return new String(data, StandardCharsets.US_ASCII);
	};

	public static final NetflowFieldConverter READ_ICMP_TYPE = (buf, field) -> {
		int number = buf.readUnsignedShort();
		int icmpType = number / 256;
		int code = number % 256;

		return "Type:" + icmpType + "|Code:" + code;
	};

}
