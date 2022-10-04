package com.tpp.private_doh.dns;

import com.tpp.private_doh.util.Requester;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.util.concurrent.CompletableFuture;

public class PublicDnsRequester implements Requester {
    private final String resolver;

    public PublicDnsRequester(String resolver) {
        this.resolver = resolver;
    }

    @Override
    public CompletableFuture<Message> executeRequest(String name, int type) {
        try {
            Record queryRecord = Record.newRecord(Name.fromString(name), Type.A, DClass.IN); // TODO: map class from type parameter to Type
            Message queryMessage = Message.newQuery(queryRecord);

            // TODO: this is super harcoded change this
            //queryMessage.addRecord(Record.newRecord(Name.fromString("fiubaMap."), Type.A, DClass.IN), Section.QUESTION);

            Resolver r = new SimpleResolver(resolver);
            return r.sendAsync(queryMessage).toCompletableFuture();
            //return PublicDnsToDnsMapper.map(message);
        } catch (Exception e) {
            throw new RuntimeException("There was an error executing the request in DnsRequester", e);
        }
    }
}
