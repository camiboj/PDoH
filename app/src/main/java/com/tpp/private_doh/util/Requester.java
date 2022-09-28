package com.tpp.private_doh.util;

import com.tpp.private_doh.doh.Response;

public interface Requester {
    Response executeRequest(String name, int type);
}
