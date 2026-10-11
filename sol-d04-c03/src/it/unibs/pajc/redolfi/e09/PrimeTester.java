package it.unibs.pajc.redolfi.e09;

/** Il test di primalita' per divisioni successive della dispensa (capitolo 1). */
public class PrimeTester {
    public static boolean isPrime(long n) {
        if (n < 2) return false;
        if (n % 2 == 0) return n == 2;
        for (long d = 3; d * d <= n; d += 2)
            if (n % d == 0) return false;
        return true;
    }

    public static long count(long from, long to) {
        long n = 0;
        for (long i = from; i <= to; i++)
            if (isPrime(i)) n++;
        return n;
    }
}
