package it.unibs.pajc.redolfi.e02;

/** Un comando al distributore: la sua esecuzione dipende dallo stato (doppio dispatch). */
public sealed interface Comando permits Moneta, Scelta, Resto, Guasto, Riparato {
}
