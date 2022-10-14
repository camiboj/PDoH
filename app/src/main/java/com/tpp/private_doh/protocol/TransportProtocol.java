package com.tpp.private_doh.protocol;

public enum TransportProtocol {
    TCP(6),
    UDP(17),
    ICMP(1),
    Other(0xFF);

    private final int protocolNumber;

    TransportProtocol(int protocolNumber) {
        this.protocolNumber = protocolNumber;
    }

    public static TransportProtocol numberToEnum(int protocolNumber) {
        if (protocolNumber == 6)
            return TCP;
        else if (protocolNumber == 17)
            return UDP;
        else if (protocolNumber == 1)
            return ICMP;
        else
            return Other;
    }

    public int getNumber() {
        return this.protocolNumber;
    }
}