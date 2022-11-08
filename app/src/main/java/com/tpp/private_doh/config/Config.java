package com.tpp.private_doh.config;

import java.util.UUID;

public class Config {
    public static final String DNS_PROVIDER = "114.114.114.114";
    public static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    public static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    public static final Integer QUEUE_CAPACITY = 1000;
    public static final String STOP_SIGNAL = "stop_kill";
    public static final String STOP_SIGNAL_FOR_INTERNET = "stop_kill_internet";
    public static final String SENTINEL = UUID.randomUUID().toString();
    public static final String PING_QUESTION = UUID.randomUUID().toString();
    public static final Integer PING_TIMEOUT = 5;
    public static final Integer SLEEP_PING = 600000;
    public static final Integer INTERNET_PERIOD = 1000;
    public static final String BUG_LINK = "https://forms.gle/NgBzJEcUoBJkSfuL6";
    public static final Integer MIN_RACING_AMOUNT = 2;
    public static final Integer TCP_BUFFER_BYTES = 1000 * 1024;
}





