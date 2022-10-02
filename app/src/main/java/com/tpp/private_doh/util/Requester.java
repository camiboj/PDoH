package com.tpp.private_doh.util;

import com.tpp.private_doh.dns.Response;

public interface Requester {
    Response executeRequest(String name, int type);
}
