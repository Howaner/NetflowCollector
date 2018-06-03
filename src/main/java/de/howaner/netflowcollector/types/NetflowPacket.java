package de.howaner.netflowcollector.types;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NetflowPacket {
	private final List<FlowSet> flowSets = new ArrayList<>();

	/**
	 * Netflow version. Only 9 is supported.
	 */
	private final short version;
	/**
	 * Flows count (maybe ...)
	 */
	private final int count;
	/**
	 * System uptime in milliseconds since the device was first booted
	 */
	private final long uptime;
	/**
	 * Current unix timestamp in seconds
	 */
	private final long timestamp;
	/**
	 * Sequence number, but I think this isn't needed ...
	 */
	private final long flowSequence;
	/**
	 * The Source ID field is a 32-bit value that is used to guarantee uniqueness for all flows exported from a particular device.
	 * (The Source ID field is the equivalent of the engine type and engine ID fields found in the NetFlow Version 5 and Version 8 headers).
	 * The format of this field is vendor specific. In the Cisco implementation, the first two bytes are reserved for future expansion, and will always be zero.
	 * Byte 3 provides uniqueness with respect to the routing engine on the exporting device.
	 * Byte 4 provides uniqueness with respect to the particular line card or Versatile Interface Processor on the exporting device.
	 * Collector devices should use the combination of the source IP address plus the Source ID field to associate an incoming NetFlow export packet with a unique instance of NetFlow
	 * on a particular device.
	 */
	private final long sourceId;

	/**
	 * Sender IP of the packet
	 */
	private InetSocketAddress sender;

}
