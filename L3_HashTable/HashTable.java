package aed.hashtable;

import java.util.NoSuchElementException;
import java.util.Iterator;

import es.upm.aedlib.Entry;
import es.upm.aedlib.EntryImpl;
import es.upm.aedlib.map.*;
import es.upm.aedlib.InvalidKeyException;
import es.upm.aedlib.indexedlist.*;

/**
 * A hash table implementing using open addressing to handle key collisions.
 */
public class HashTable<K, V> implements Map<K, V> {
  Entry<K, V>[] buckets;
  int size;

  public HashTable(int initialSize) {
    this.buckets = createArray(initialSize);
    this.size = 0;
  }

  @Override
  public Iterator<Entry<K, V>> iterator() { // O(n)
    return entries().iterator();
  }

  @Override
  public boolean containsKey(Object arg0) throws InvalidKeyException { //O(n)
    if (arg0 == null) {
      throw new InvalidKeyException();
    } 
    boolean contiene = false; // arg0 siempre va a ser key, no otro objeto
    for (int i = 0; i < buckets.length && !contiene; i++) {
      if (buckets[i] != null) {
        contiene = buckets[i].getKey().equals(arg0);
      }
    }
    return contiene;
  }

  @Override
  public Iterable<Entry<K, V>> entries() { // O(n)
    IndexedList<Entry<K, V>> entries = new ArrayIndexedList<Entry<K, V>>();

    for (Entry<K, V> bucket: buckets) {
      if (bucket != null) {
        entries.add(entries.size(), bucket);
      }
    }
    return entries;
  }

  @Override
  public V get(K arg0) throws InvalidKeyException { //O(2n)
    if (arg0 == null) {
      throw new InvalidKeyException();
    } else {
      V res = null;
      if (containsKey(arg0)) {
        int indiceLocalizado = search(arg0);
        res = buckets[indiceLocalizado].getValue();
      }
      return res;
    }
  }

  @Override
  public boolean isEmpty() { //O(1)
    return size() == 0;
  }

  @Override
  public Iterable<K> keys() { // O(n)
    IndexedList<K> claves = new ArrayIndexedList<K>();
    for (Entry<K, V> e : buckets) {
      if (e != null) {
        claves.add(claves.size(), e.getKey());
      }
    }
    return claves;
  }

  @Override
  public V put(K arg0, V arg1) throws InvalidKeyException { // TERMINAR DE CORREGIR
    if (arg0 == null) {
      throw new InvalidKeyException();
    }
    if (buckets.length == size()) {
      rehash();
    }
    
    Entry<K, V> nuevaEntrada = new EntryImpl<K, V>(arg0, arg1); // Entrada a insertar
    int indice = search(arg0);
    V value = null;

    if (buckets[indice] == null) {
      buckets[indice] = nuevaEntrada;
      size++;
    } else {
      value = buckets[indice].getValue();
      // hacemos busqueda circular para insertar la entry (es seguro que hay hueco)
      buckets[indice] = nuevaEntrada;

    }
    return value;
  }

  @Override
  public V remove(K arg0) throws InvalidKeyException { //If contains la clave?????
    V res = null;
    if (containsKey(arg0)) { // si K esta en buckets, borramos esa entry
      int posK = search(arg0); //seguro que no es -1
      res = buckets[posK].getValue();
      buckets[posK] = null;
      size--;
      // Una vez borrada, debemos colapsar huecos
      // Comprobamos si los siguientes elem posK podemos recolocarlo
      int contador = 0;
      while (contador < size() && (buckets[posK++] != null)) {
        int indicePreferido = index(buckets[posK++].getKey());
        if (indicePreferido == posK) {
          buckets[posK] = buckets[posK++];
          buckets[posK++] = null;
        }
        posK++;
      }
    }
    return res;
  }

  @Override
  public int size() { //O(1)
    return size;
  }
  /*
   * Debe tener en cuenta que podr´ıa estar en lugar diferente de su bucket
   * \preferido": podr´ıa haber habido colisiones en la inserci´on.
   * Algoritmo:
   * I Calcular index, el bucket preferido de la clave.
   * I Asignar un variable i = index.
   * I Repetir:
   * F Si buckets[i] est´a vac´ıo, la clave no est´e en la tabla.
   * F Si la clave de buckets[i] es la que buscamos, hemos encontrado la
   * Entry que necesitamos. Devolver su valor.
   * F Si no, ir al siguiente i (circularmente).
   * Si i = index termina la b´usqueda sin ´exito.
   *  
   * El procedimiento de colapsar huecos termina cuando encontramos un
   * bucket vac´ıo no creado por el procedimiento mismo o cuando
   * llegamos al principio (al bucket originalmente borrado).
   */

  // Examples of auxilliary methods: IT IS NOT REQUIRED TO IMPLEMENT THEM

  @SuppressWarnings("unchecked")
  private Entry<K, V>[] createArray(int size) {
    Entry<K, V>[] buckets = (Entry<K, V>[]) new Entry[size];
    return buckets;
  }

  // Returns the bucket index of an object
  private int index(Object obj) { // object es un Key
    return Math.abs(obj.hashCode()) % buckets.length;
  }

  /**
   *  Returns the index where an entry with the key is located 
   *  or if no such entry exists, the "next" bucket with no entry,
   *  or if all buckets stores an entry, -1 is returned. */
  /*
   * El ´ındice donde reside una entrada con la clave key, si est´a en la tabla.
I Si no hay entrada con la clave key en buckets, devuelve, si hay
buckets libres, el ´ındice del siguiente \bucket" libre (con elemento
null) dentro buckets
I Si no hay entrada con clave key, ni existe un bucket libre, devuelve -1.
   */
  private int search(Object obj) { //O(n)

    //Obj es un key. buscamos primero si esta la key
    int indicePreferido = index(obj);
    int contador = 0;

    int n = -1;
    while (contador < buckets.length) { // buscamos posicion de key
      if (buckets[indicePreferido] == null || buckets[indicePreferido].getKey().equals(obj)) { // hay algun bucket libre
        return indicePreferido;
      }
      indicePreferido = (indicePreferido + 1) % buckets.length;
      contador++;
    }
    return n;
  }

  // Doubles the size of the bucket array, and inserts all entries present
  // in the old bucket array into the new bucket array, in their correct
  // places. Remember that the index of an entry will likely change in
  // the new array, as the size of the array changes.

  private void rehash() { //O(n^2)
    Entry<K, V>[] newBuckets = createArray(buckets.length * 2);
    Entry<K, V>[] oldBuckets = buckets;
    buckets = newBuckets;

    for (Entry<K, V> bucket : oldBuckets) {
      if (bucket != null) {
        // Ahora hay espacio para insertar
        buckets[search(bucket.getKey())] = bucket;
      }
    }
  }

  /*
   * --------------------------------------------------
   * TESTS
   * --------------------------------------------------
   */
  public static void main(String[] args) {
    System.out.println(new HashTable<Integer, Integer>(5).iterator());
  }
}
