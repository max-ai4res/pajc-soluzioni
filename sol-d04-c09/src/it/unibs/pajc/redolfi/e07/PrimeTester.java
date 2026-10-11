package it.unibs.pajc.redolfi.e07;

/** Il test di primalita' della dispensa; count controlla la bandiera di interruzione ogni 100 000 numeri. */
public class PrimeTester {
    public static boolean isPrime(long n) {
        if (n < 2) return false;
        if (n % 2 == 0) return n == 2;
        for (long d = 3; d * d <= n; d += 2)
            if (n % d == 0) return false;
        return true;
    }

    /** Conta i primi in [from, to]; se il thread viene interrotto restituisce -1 entro centomila numeri. */
    public static long count(long from, long to) {
        long n = 0;
        for (long i = from; i <= to; i++) {
            if ((i & 0x1FFFF) == 0 && Thread.currentThread().isInterrupted()) return -1;
            if (isPrime(i)) n++;
        }
        return n;
    }
}
