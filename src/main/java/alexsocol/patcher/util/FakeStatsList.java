package alexsocol.patcher.util;

import net.minecraft.stats.StatBase;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings("ALL") // STFU
public class FakeStatsList implements List<StatBase> {
	public StatBase element;
	public FakeStatsList(StatBase e) { element = e; }
	public int size() { return 1; }
	public boolean isEmpty() { return false; }
	public boolean contains(Object o) { return o == element; }
	public Iterator<StatBase> iterator() { return new FakeIterator(); }
	public Object[] toArray() { return new StatBase[] { element }; }
	public <T> T[] toArray(T[] a) { return (T[]) toArray(); }
	public boolean add(StatBase statBase) { return false; }
	public boolean remove(Object o) { return false; }
	public boolean containsAll(Collection<?> c) { return false; }
	public boolean addAll(Collection<? extends StatBase> c) { return false; }
	public boolean addAll(int index, Collection<? extends StatBase> c) { return false; }
	public boolean removeAll(Collection<?> c) { return false; }
	public boolean retainAll(Collection<?> c) { return false; }
	public void clear() {}
	public StatBase get(int index) { return element; }
	public StatBase set(int index, StatBase element) { return null; }
	public void add(int index, StatBase element) {}
	public StatBase remove(int index) { return null; }
	public int indexOf(Object o) { return 0; }
	public int lastIndexOf(Object o) { return 0; }
	public ListIterator<StatBase> listIterator() { return new FakeListIterator(); }
	public ListIterator<StatBase> listIterator(int index) { return listIterator(); }
	public List<StatBase> subList(int fromIndex, int toIndex) { return this; }
	private static class FakeIterator implements Iterator<StatBase> {
		public boolean hasNext() { return false; }
		public StatBase next() { return null; }
		public void remove() {}
	}
	private static class FakeListIterator implements ListIterator<StatBase> {
		public boolean hasNext() { return false; }
		public StatBase next() { return null; }
		public boolean hasPrevious() { return false; }
		public StatBase previous() { return null; }
		public int nextIndex() { return 0; }
		public int previousIndex() { return 0; }
		public void remove() {}
		public void set(StatBase statBase) {}
		public void add(StatBase statBase) {}
	}
}
