/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */
package haven;

import java.lang.ref.*;
import java.util.*;

public class CacheMap<K, V> extends AbstractMap<K, V> {

	private final Map<K, Reference<V>> back;
	private final ReferenceQueue<V> cleanq = new ReferenceQueue<V>();
	private final RefType reftype;

	/* Because multiple inheritence would be too good. */
	interface Ref<K> {

		K key();
	}

	static class SRef<K, V> extends SoftReference<V> implements Ref<K> {

		final K key;

		SRef(K key, V val, ReferenceQueue<V> queue) {
			super(val, queue);
			this.key = key;
		}

		@Override
		public K key() {
			return (this.key);
		}
	}

	static class WRef<K, V> extends WeakReference<V> implements Ref<K> {

		final K key;

		WRef(K key, V val, ReferenceQueue<V> queue) {
			super(val, queue);
			this.key = key;
		}

		@Override
		public K key() {
			return (this.key);
		}
	}

	public static enum RefType {

		SOFT {
							@Override
							public <K, V> Reference<V> mkref(K k, V v, ReferenceQueue<V> cleanq) {
								return (new SRef<K, V>(k, v, cleanq));
							}
						}, WEAK {
							@Override
							public <K, V> Reference<V> mkref(K k, V v, ReferenceQueue<V> cleanq) {
								return (new WRef<K, V>(k, v, cleanq));
							}
						};

		public abstract <K, V> Reference<V> mkref(K k, V v, ReferenceQueue<V> cleanq);
	}

	public CacheMap(RefType type) {
		this.reftype = type;
		this.back = new HashMap<K, Reference<V>>();
	}

	public CacheMap() {
		this(RefType.SOFT);
	}

	public CacheMap(Map<K, V> m) {
		this();
		putAll(m);
	}

	@Override
	public boolean containsKey(Object k) {
		return (get(k) != null);
	}

	private class IteredEntry implements Entry<K, V> {

		private final K k;
		private V v;

		private IteredEntry(K k, V v) {
			this.k = k;
			this.v = v;
		}

		@Override
		public K getKey() {
			return (k);
		}

		@Override
		public V getValue() {
			return (v);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object o) {
			return ((o instanceof CacheMap.IteredEntry) && k.equals(((IteredEntry) o).k));
		}

		@Override
		public int hashCode() {
			return (k.hashCode());
		}

		@Override
		public V setValue(V nv) {
			return (put(k, this.v = nv));
		}
	}

	private Set<Entry<K, V>> entries = null;

	@Override
	public Set<Entry<K, V>> entrySet() {
		if (entries == null) {
			entries = new AbstractSet<Entry<K, V>>() {
				@Override
				public int size() {
					clean();
					return (back.size());
				}

				@Override
				public Iterator<Entry<K, V>> iterator() {
					clean();
					final Iterator<Entry<K, Reference<V>>> iter = back.entrySet().iterator();
					return (new Iterator<Entry<K, V>>() {
						private K nk;
						private V nv;

						@Override
						public boolean hasNext() {
							while (true) {
								if (nv != null) {
									return (true);
								}
								if (!iter.hasNext()) {
									return (false);
								}
								Entry<K, Reference<V>> e = iter.next();
								K k = e.getKey();
								V v = e.getValue().get();
								if (v != null) {
									nk = k;
									nv = v;
									return (true);
								}
							}
						}

						@Override
						public Entry<K, V> next() {
							if (!hasNext()) {
								throw (new NoSuchElementException());
							}
							Entry<K, V> ret = new IteredEntry(nk, nv);
							nk = null;
							nv = null;
							return (ret);
						}

						@Override
						public void remove() {
							iter.remove();
						}
					});
				}

				@Override
				public void clear() {
					back.clear();
				}
			};
		}
		return (entries);
	}

	private void clean() {
		Reference<? extends V> ref;
		while ((ref = cleanq.poll()) != null) {
			Ref rr = (Ref) ref;
			remove(rr.key());
		}
	}

	@Override
	public V get(Object k) {
		clean();
		Reference<V> ref = back.get(k);
		return ((ref == null) ? null : (ref.get()));
	}

	@Override
	public V put(K k, V v) {
		clean();
		Reference<V> old = back.put(k, reftype.mkref(k, v, cleanq));
		return ((old == null) ? null : (old.get()));
	}

	@Override
	public V remove(Object k) {
		clean();
		Reference<V> ref = back.remove(k);
		return ((ref == null) ? null : (ref.get()));
	}
}
