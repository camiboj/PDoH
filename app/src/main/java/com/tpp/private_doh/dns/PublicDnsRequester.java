package com.tpp.private_doh.dns;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.tpp.private_doh.config.Config;
import com.tpp.private_doh.mapper.PublicDnsToDnsMapper;
import com.tpp.private_doh.util.Requester;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

public class PublicDnsRequester extends Requester {
    protected final static String TAG = PublicDnsRequester.class.getSimpleName();
    private Resolver resolver;
    private String resolverName;

    public PublicDnsRequester(String resolver) {
        this.resolverName = resolver;
        try {
            this.resolver = new SimpleResolver(resolver);
        } catch (UnknownHostException e) {
            Log.i(TAG, "Unknown resolver to build DnsRequester");
        }
    }

    @VisibleForTesting
    public PublicDnsRequester(SimpleResolver resolver) {
        this.resolver = resolver;
    }


    @Override
    public String getName() {
        return resolverName;
    }


    @Override
    public CompletableFuture<Response> executeRequest(String name, int type) {
        try {
            String queryName = name + "."; // This is a requirement of dns-java library
            Record queryRecord = Record.newRecord(Name.fromString(queryName), type, DClass.IN);
            Message queryMessage = Message.newQuery(queryRecord);

            // Sentinel to recognize this packet while capturing
            Log.i(TAG, "About to process message");
            queryMessage.addRecord(Record.newRecord(Name.fromString(Config.SENTINEL + "."), Type.A, DClass.IN), Section.QUESTION);
            return resolver.sendAsync(queryMessage).toCompletableFuture().thenApply((msg) -> this.processMessage(msg, System.nanoTime()));
        } catch (Exception e) {
            throw new RuntimeException("There was an error executing the request in DnsRequester", e);
        }
    }

    public CompletableFuture<Response> executePingRequest(String name, int type) {
        try {
            String queryName = name + "."; // This is a requirement of dns-java library
            Record queryRecord = Record.newRecord(Name.fromString(queryName), type, DClass.IN);
            Message queryMessage = Message.newQuery(queryRecord);
            return resolver.sendAsync(queryMessage).toCompletableFuture().thenApply(PublicDnsToDnsMapper::map);
        } catch (Exception e) {
            throw new RuntimeException("There was an error executing the request in DnsRequester", e);
        }
    }


    public String getIp() {
        return this.resolverName;
    }
}