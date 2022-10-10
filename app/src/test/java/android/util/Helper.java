package android.util;

import static org.junit.Assert.assertEquals;

import com.tpp.private_doh.dns.DnsHeader;
import com.tpp.private_doh.dns.DnsPacket;
import com.tpp.private_doh.protocol.IP4Header;
import com.tpp.private_doh.protocol.Packet;
import com.tpp.private_doh.protocol.TcpHeader;
import com.tpp.private_doh.protocol.TransportProtocol;
import com.tpp.private_doh.protocol.UdpHeader;
import com.tpp.private_doh.util.ByteBufferPool;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Helper {
    public static byte VERSION = (byte) 4;
    public static byte IHL = (byte) 5;
    public static int HEADER_LENGTH = 1;
    public static short TYPE_OF_SERVICE = 1;
    public static int TOTAL_LENGTH = 1;
    public static int ID_AND_FLAGS_AND_FRAGMENT_OFFSET = 1;
    public static short TTL = 1;
    public static int TCP_PROTOCOL_NUM = 6;
    public static int UDP_PROTOCOL_NUM = 17;
    public static TransportProtocol TCP_PROTOCOL = TransportProtocol.TCP;
    public static TransportProtocol UDP_PROTOCOL = TransportProtocol.UDP;
    public static int HEADER_CHECKSUM = 1;
    public static InetAddress SOURCE_ADDRESS = buildAddress("121.122.123.124");
    public static InetAddress DESTINATION_ADDRESS = buildAddress("1.2.3.4");
    public static int DNS_SOURCE_PORT = 53;
    public static int SOURCE_PORT = 3;
    public static int DESTINATION_PORT = 2;
    public static int IDENTIFICATION = 1;
    public static int FLAGS = 1;
    public static int N_QUESTIONS = 0;
    public static int N_ANSWERS = 0;
    public static int N_AUTHORITY_RESOURCE_RECORDS = 0;
    public static int N_ADDITIONAL_RRS = 0;
    public static String QUESTION_NAME = "questionName";
    public static String ANSWER_NAME = "answerName";
    public static int QUESTION_TYPE = 1;
    public static int ANSWER_TYPE = 1;
    public static String DATA = "129.130.131.132";

    protected static InetAddress buildAddress(String name) {
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed test");
        }
    }

    protected Packet buildUdpAndIp4Header() {
        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        int optionsAndPadding = 1;
        IP4Header ip4Header = new IP4Header(VERSION, IHL, HEADER_LENGTH, TYPE_OF_SERVICE, TOTAL_LENGTH,
                ID_AND_FLAGS_AND_FRAGMENT_OFFSET, TTL, UDP_PROTOCOL_NUM, UDP_PROTOCOL, HEADER_CHECKSUM,
                SOURCE_ADDRESS, DESTINATION_ADDRESS, optionsAndPadding);
        ip4Header.fillBuffer(byteBuffer);
        UdpHeader udpHeader = new UdpHeader(SOURCE_PORT, DESTINATION_PORT);
        udpHeader.fillBuffer(byteBuffer);

        return new Packet(ip4Header, udpHeader, byteBuffer);
    }

    protected Packet buildTcpAndIp4Packet() {
        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        int optionsAndPadding = 1;
        IP4Header ip4Header = new IP4Header(VERSION, IHL, HEADER_LENGTH, TYPE_OF_SERVICE, TOTAL_LENGTH,
                ID_AND_FLAGS_AND_FRAGMENT_OFFSET, TTL, TCP_PROTOCOL_NUM, TCP_PROTOCOL, HEADER_CHECKSUM,
                SOURCE_ADDRESS, DESTINATION_ADDRESS, optionsAndPadding);
        ip4Header.fillBuffer(byteBuffer);

        long sequenceNumber = 1;
        long acknowledgementNumber = 1;
        byte dataOffsetAndReserved = (byte) 0;
        int headerLength = 1;
        byte flags = (byte) 0;
        int window = 1;
        int checksum = 1;
        int urgentPointer = 1;
        byte[] optionsAndPaddingTcp = new byte[1];

        TcpHeader tcpHeader = new TcpHeader(SOURCE_PORT, DESTINATION_PORT, sequenceNumber, acknowledgementNumber,
                dataOffsetAndReserved, headerLength, flags, window, checksum, urgentPointer, optionsAndPaddingTcp);
        tcpHeader.fillBuffer(byteBuffer);

        return new Packet(ip4Header, tcpHeader, byteBuffer);
    }

    protected DnsPacket buildDnsAndIp4HeaderPacket() {
        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        int optionsAndPadding = 1;
        IP4Header ip4Header = new IP4Header(VERSION, IHL, HEADER_LENGTH, TYPE_OF_SERVICE, TOTAL_LENGTH,
                ID_AND_FLAGS_AND_FRAGMENT_OFFSET, TTL, UDP_PROTOCOL_NUM, UDP_PROTOCOL, HEADER_CHECKSUM,
                SOURCE_ADDRESS, DESTINATION_ADDRESS, optionsAndPadding);
        ip4Header.fillBuffer(byteBuffer);
        UdpHeader udpHeader = new UdpHeader(DNS_SOURCE_PORT, DESTINATION_PORT);
        udpHeader.fillBuffer(byteBuffer);
        DnsHeader dnsHeader = new DnsHeader(IDENTIFICATION, FLAGS, N_QUESTIONS, N_ANSWERS,
                N_AUTHORITY_RESOURCE_RECORDS, N_ADDITIONAL_RRS);
        dnsHeader.putOn(byteBuffer);
        byteBuffer.position(ip4Header.getHeaderSize() + Packet.UDP_HEADER_SIZE);

        DnsPacket dnsPacket = new DnsPacket(ip4Header, udpHeader, byteBuffer);
        dnsPacket.addQuestion(QUESTION_NAME, QUESTION_TYPE);
        dnsPacket.addAnswer(ANSWER_NAME, ANSWER_TYPE, TTL, DATA);
        dnsPacket.fillBackingBuffer();
        return dnsPacket;
    }

    protected void assertNBytes(byte[] firstArray, byte[] secondArray, int n) {
        for (int i = 0; i < n; i++) {
            assertEquals(firstArray[i], secondArray[i]);
        }
    }
}
