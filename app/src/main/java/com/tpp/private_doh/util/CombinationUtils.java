package com.tpp.private_doh.util;

import com.tpp.private_doh.doh.DoHRequester;

import org.paukov.combinatorics3.Generator;

import java.util.List;
import java.util.stream.Collectors;

public class CombinationUtils {

    public static List<List<DoHRequester>> combination(List<DoHRequester> doHRequesters, int k) {
        return Generator.combination(doHRequesters)
                .simple(k)
                .stream()
                .collect(Collectors.toList());
    }
}
