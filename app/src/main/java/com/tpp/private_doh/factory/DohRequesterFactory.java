package com.tpp.private_doh.factory;

import com.tpp.private_doh.doh.CloudflareDoHRequester;
import com.tpp.private_doh.doh.DoHRequester;
import com.tpp.private_doh.doh.GoogleDoHRequester;
import com.tpp.private_doh.doh.Quad9DoHRequester;

public class DohRequesterFactory {

    public static DoHRequester build(String name) {
        if (name.equals(CloudflareDoHRequester.class.getName())) {
            return new CloudflareDoHRequester();
        } else if (name.equals(GoogleDoHRequester.class.getName())) {
            return new GoogleDoHRequester();
        } else if (name.equals(Quad9DoHRequester.class.getName())) {
            return new Quad9DoHRequester();
        }
        throw new IllegalArgumentException("The name is not valid");
    }
}
