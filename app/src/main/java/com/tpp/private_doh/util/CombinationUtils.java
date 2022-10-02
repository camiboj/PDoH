package com.tpp.private_doh.util;

import org.paukov.combinatorics3.Generator;

import java.util.List;
import java.util.stream.Collectors;

public class CombinationUtils {

    public static List<List<Requester>> combination(List<Requester> requesters, int k) {
        return Generator.combination(requesters)
                .simple(k)
                .stream()
                .collect(Collectors.toList());
    }
}
