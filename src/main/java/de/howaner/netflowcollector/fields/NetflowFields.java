package de.howaner.netflowcollector.fields;

import static de.howaner.netflowcollector.fields.NetflowFieldConverters.*;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

// See https://www.cisco.com/en/US/technologies/tk648/tk362/technologies_white_paper09186a00800a3db9.html
public enum NetflowFields {
	IN_BYTES(1, true, READ_UNSIGNED_COUNTER),
	IN_PKTS(2, true, READ_UNSIGNED_COUNTER),
	FLOWS(3, true, READ_UNSIGNED_COUNTER),
	PROTOCOL(4, true, READ_BYTE),
	SRC_TOS(5, true, READ_BYTE),  // TODO
	TCP_FLAGS(6, true, READ_BYTE),  // TODO
	L4_SRC_PORT(7, true, READ_PORT),
	IPV4_SRC_ADDR(8, true, READ_IPv4),
	SRC_MASK(9, true, READ_BYTE),
	INPUT_INTERFACE(10, true, READ_UNSIGNED_COUNTER),  // Cisco: INPUT_SNMP
	L4_DST_PORT(11, true, READ_PORT),
	IPV4_DST_ADDR(12, true, READ_IPv4),
	DST_MASK(13, true, READ_BYTE),
	OUTPUT_INTERFACE(14, true, READ_UNSIGNED_COUNTER),  // Cisco: OUTPUT_SNMP
	IPV4_NEXT_HOP(15, true, READ_IPv4),
	SRC_AS(16, false, READ_UNSIGNED_COUNTER),
	DST_AS(17, false, READ_UNSIGNED_COUNTER),
	BGP_IPV4_NEXT_HOP(18, true, READ_IPv4),
	MUL_DST_PKTS(19, true, READ_UNSIGNED_COUNTER),
	MUL_DST_BYTES(20, true, READ_UNSIGNED_COUNTER),
	LAST_SWITCHED(21, true, READ_TIME),
	FIRST_SWITCHED(22, true, READ_TIME),
	OUT_BYTES(23, true, READ_UNSIGNED_COUNTER),
	OUT_PKTS(24, true, READ_UNSIGNED_COUNTER),
	MIN_PKT_LNGTH(25, true, READ_UNSIGNED_COUNTER),
	MAX_PKT_LNGTH(26, true, READ_UNSIGNED_COUNTER),
	IPV6_SRC_ADDR(27, true, READ_IPv6),
	IPV6_DST_ADDR(28, true, READ_IPv6),
	IPV6_SRC_MASK(29, true, READ_BYTE),
	IPV6_DST_MASK(30, true, READ_BYTE),
	IPV6_FLOW_LABEL(31, false, READ_IPv6_FLOW_LABEL),
	ICMP_TYPE(32, true, READ_ICMP_TYPE),
	MUL_IGMP_TYPE(33, true, READ_BYTE),
	SAMPLING_INTERVAL(34, false, READ_INT),
	SAMPLING_ALGORITHM(35, false, READ_BYTE),
	FLOW_ACTIVE_TIMEOUT(36, false, READ_SHORT),
	FLOW_INACTIVE_TIMEOUT(37, false, READ_SHORT),
	ENGINE_TYPE(38, true, READ_BYTE),
	ENGINE_ID(39, true, READ_BYTE),
	TOTAL_BYTES_EXP(40, true, READ_UNSIGNED_COUNTER),
	TOTAL_PKTS_EXP(41, true, READ_UNSIGNED_COUNTER),
	TOTAL_FLOWS_EXP(42, true, READ_UNSIGNED_COUNTER),
	// 43 is vendor proprietary ...
	IPV4_SRC_PREFIX(44, true, READ_IPv4),
	IPV4_DST_PREFIX(45, true, READ_IPv4),
	MPLS_TOP_LABEL_TYPE(46, true, READ_BYTE),
	MPLS_TOP_LABEL_IP_ADDR(47, true, READ_INT),
	FLOW_SAMPLER_ID(48, true, READ_BYTE),
	FLOW_SAMPLER_MODE(49, false, READ_BYTE),
	FLOW_SAMPLER_RANDOM_INTERVAL(50, false, READ_UNSIGNED_COUNTER),
	// 51 is vendor proprietary ...
	MIN_TTL(52, false, READ_UNSIGNED_COUNTER),
	MAX_TTL(53, false, READ_UNSIGNED_COUNTER),
	IPV4_IDENT(54, false, READ_UNSIGNED_COUNTER),
	DST_TOS(55, true, READ_BYTE),
	IN_SRC_MAC(56, true, READ_MAC),
	OUT_DST_MAC(57, true, READ_MAC),
	SRC_VLAN(58, true, READ_SHORT),
	DST_VLAN(59, true, READ_SHORT),
	IP_PROTOCOL_VERSION(60, true, READ_BYTE),
	DIRECTION(61, true, READ_BYTE),
	IPV6_NEXT_HOP(62, true, READ_IPv6),
	BPG_IPV6_NEXT_HOP(63, true, READ_IPv6),
	IPV6_OPTION_HEADERS(64, true, READ_UNSIGNED_COUNTER),
	// 65 - 69 is vendor proprietary ...
	// 70 - 79 is bullshit
	IN_DST_MAC(80, true, READ_MAC),
	OUT_SRC_MAC(81, true, READ_MAC),
	IF_NAME(82, true, READ_STRING),
	IF_DESC(83, true, READ_STRING),
	SAMPLER_NAME(84, true, READ_STRING),
	IN_PERMANENT_BYTES(85, false, READ_UNSIGNED_COUNTER),
	IN_PERMANENT_PKTS(86, false, READ_UNSIGNED_COUNTER),
	// 87 is vendor proprietary ...
	FRAGMENT_OFFSET(88, true, READ_UNSIGNED_COUNTER),
	FORWARDING_STATUS(89, false, READ_BYTE),
	// 90 is array bullshit
	MPLS_PREFIX_LEN(91, false, READ_UNSIGNED_COUNTER),
	SRC_TRAFFIC_INDEX(92, false, READ_UNSIGNED_COUNTER),
	DST_TRAFFIC_INDEX(93, false, READ_UNSIGNED_COUNTER),
	APPLICATION_DESCRIPTION(94, true, READ_STRING),
	APPLICATION_TAG(95, true, READ_UNSIGNED_COUNTER),
	APPLICATION_NAME(96, true, READ_STRING),

	MPLS_LABEL_STACK_OCTETS(201, false, READ_INT),

	// Mikrotik
	NAT_IPV4_SRC_ADDR(225, true, READ_IPv4),
	NAT_IPv4_DST_ADDR(226, true, READ_IPv4),
	NAT_L4_SRC_PORT(227, true, READ_PORT),
	NAT_L4_DST_PORT(228, true, READ_PORT)
	;

	@Getter private final int id;
	@Getter private final boolean relevant;
	@Getter private final NetflowFieldConverter converter;
	private static final Map<Integer, NetflowFields> fieldsMapping = new HashMap<>();

	static {
		for (NetflowFields field : values())
			fieldsMapping.put(field.getId(), field);
	}

	private NetflowFields(int id, boolean relevant, NetflowFieldConverter converter) {
		this.id = id;
		this.relevant = relevant;
		this.converter = converter;
	}

	public static NetflowFields getFieldById(int id) {
		return fieldsMapping.get(id);
	}

}
