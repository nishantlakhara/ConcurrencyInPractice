package com.concurrencyinpractice.chapter2;

import net.jcip.annotations.GuardedBy;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class CachedFactorizer {
    @GuardedBy("this")
    private BigInteger lastNumer;
    @GuardedBy("this")
    private BigInteger[] lastFactors;
    @GuardedBy("this")
    private long hits;
    @GuardedBy("this")
    private long cacheHits;

    public void getFactors(BigInteger i) {
        BigInteger[] factors = null;

        synchronized(this) {
            hits++;
            if(i.equals(lastNumer)) {
                cacheHits++;
                factors = lastFactors;
            }
        }
        if(factors == null) {
            factors = allFactors(i.intValue())
                    .stream()
                    .map(BigInteger::valueOf)
                    .toArray(BigInteger[]::new);
            synchronized ("this") {
                lastNumer = i;
                lastFactors = factors;
            }
        }
        System.out.println("Integer i == " + i.intValue() + "\tFactors == " + factorArrayToString(factors));
    }

    public String factorArrayToString(BigInteger[] array) {
        List<Integer> list = Arrays.stream(array).map(i -> i.intValue())
                .collect(Collectors.toList());
        return list.toString();
    }

    public static ArrayList<Integer> allFactors(int a) {
        int upperlimit = (int) (Math.sqrt(a));
        ArrayList<Integer> factors = new ArrayList<Integer>();
        for (int i = 1; i <= upperlimit; i += 1) {
            if (a % i == 0) {
                factors.add(i);
                if (i != a / i) {
                    factors.add(a / i);
                }
            }
        }
        Collections.sort(factors);
        return factors;
    }

    public static void main(String[] args) {
        CachedFactorizer factorizer = new CachedFactorizer();
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 100; i++) {
            Runnable worker = () -> {
                factorizer.getFactors(new BigInteger("11"));
                //factorizer.getFactors(new BigInteger("12"));
            };
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
        System.out.println(factorizer.getHits());
        System.out.println(factorizer.getCacheHits());
    }

    public synchronized long getHits() {
        return hits;
    }

    public synchronized long getCacheHits() {
        return cacheHits;
    }
}