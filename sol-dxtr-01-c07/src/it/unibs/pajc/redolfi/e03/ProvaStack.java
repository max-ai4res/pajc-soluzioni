package it.unibs.pajc.redolfi.e03;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/** Una classe di test che il runner trova percorrendo la cartella bin. */
public class ProvaStack {
    private Deque<Integer> stack;

    @Prima public void prepara() { stack = new ArrayDeque<>(); stack.push(1); stack.push(2); }
    @Test public void popRestituisceUltimo() { verifica(stack.pop() == 2); }
    @Test public void dimensione() { verifica(stack.size() == 2); }
    @Test(atteso = NoSuchElementException.class) public void popSuVuoto() { stack.clear(); stack.pop(); }
    @Test public void questoFallisce() { verifica(stack.peek() == 1); }

    static void verifica(boolean c) { if (!c) throw new AssertionError("condizione falsa"); }
}
