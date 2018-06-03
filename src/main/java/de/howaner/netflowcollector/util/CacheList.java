package de.howaner.netflowcollector.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.Setter;

public class CacheList<T> implements List<T> {
	private final Lock lock = new ReentrantLock();
	private final int size;
	private final Class<T> clazz;
	@Getter @Setter private FullHandler<T> fullHandler = null;

	private T[] current;
	private int index;
	@Getter private long lastChange;

	public CacheList(Class<T> clazz, int size) {
		this.clazz = clazz;
		this.size = size;
		this.current = (T[]) Array.newInstance(clazz, size);
		this.onChange();
	}

	private void onChange() {
		this.lastChange = System.currentTimeMillis();
	}

	private T[] flush(boolean resizeArray) {
		T[] array;
		int index;

		this.onChange();
		this.lock.lock();
		try {
			array = this.current;
			index = this.index;
			this.current = (T[]) Array.newInstance(this.clazz, this.size);
			this.index = 0;
		} finally {
			this.lock.unlock();
		}

		if (resizeArray && index != array.length)
			return Arrays.copyOf(array, index);
		else
			return array;
	}

	private void full() {
		T[] fullArray = this.flush(true);
		if (this.fullHandler != null)
			this.fullHandler.onFull(fullArray);
	}

	public void forceFull() {
		this.full();
	}

	@Override
	public int size() {
		return this.index;
	}

	@Override
	public boolean isEmpty() {
		return (this.index == 0);
	}

	@Override
	public boolean contains(Object o) {
		for (int i = 0; i < this.index; i++) {
			if (this.current[i] == o)
				return true;
		}

		return false;
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		T[] array;
		int index;

		this.lock.lock();
		try {
			array = this.current;
			index = this.index;
		} finally {
			this.lock.unlock();
		}

		return Arrays.copyOf(array, index);
	}

	@Override
	public <U> U[] toArray(U[] newArray) {
		Object[] array;
		int index;

		this.lock.lock();
		try {
			array = this.current;
			index = this.index;
		} finally {
			this.lock.unlock();
		}

		if (newArray.length < index) {
			return (U[]) Arrays.copyOf(array, index, newArray.getClass());
		} else {
			System.arraycopy(array, 0, newArray, 0, index);
			if (newArray.length > index)
				newArray[index] = null;
			return newArray;
		}
	}

	@Override
	public boolean add(T e) {
		this.lock.lock();
		boolean unlocked = false;

		this.onChange();
		try {
			if (this.index + 1 >= this.size) {
				unlocked = true;
				this.lock.unlock();
				this.full();
			}

			this.current[this.index] = e;
			this.index++;
		} finally {
			if (!unlocked)
				this.lock.unlock();
		}

		return true;
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		this.lock.lock();
		Object[] toAdd = c.toArray();
		int toAddIndex = 0;

		this.onChange();
		try {
			while (this.index + (toAdd.length - toAddIndex) >= this.size) {
				int capacityToAdd = Math.min(toAdd.length - toAddIndex, this.size - this.index);
				System.arraycopy(toAdd, toAddIndex, this.current, this.index, capacityToAdd);

				toAddIndex += capacityToAdd;
				this.index += capacityToAdd;

				this.lock.unlock();
				this.full();
				this.lock.lock();
			}

			if (toAddIndex < toAdd.length) {
				int capacityToAdd = toAdd.length - toAddIndex;
				System.arraycopy(toAdd, toAddIndex, this.current, this.index, capacityToAdd);

				toAddIndex += capacityToAdd;
				this.index += capacityToAdd;
			}
		} finally {
			this.lock.unlock();
		}

		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		this.flush(false);
	}

	@Override
	public T get(int index) {
		this.lock.lock();
		try {
			return this.current[index];
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public T set(int index, T element) {
		T oldElem;

		this.onChange();
		this.lock.lock();
		try {
			oldElem = this.current[index];
			this.current[index] = element;
		} finally {
			this.lock.unlock();
		}

		return oldElem;
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		for (int i = 0; i < this.index; i++) {
			if (this.current[i] == o)
				return i;
		}

		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		for (int i = this.index - 1; i >= 0; i--) {
			if (this.current[i] == o)
				return i;
		}

		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	public static interface FullHandler<T> {
		public void onFull(T[] elements);
	}

}
