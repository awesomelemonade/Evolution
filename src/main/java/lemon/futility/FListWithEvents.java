package lemon.futility;

import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.event.Event;
import lemon.engine.toolbox.Disposable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FListWithEvents<T> extends FCollection<T> implements List<T> {
    private final List<T> backingList;
    private final Event onChange = new Event();

    public FListWithEvents() {
        this(new ArrayList<>());
    }

    public FListWithEvents(List<T> backingList) {
        this.backingList = backingList;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean changed = backingList.addAll(c);
        if (changed) {
            onChange.callListeners();
        }
        return changed;
    }

    @Override
    public T get(int index) {
        return backingList.get(index);
    }

    @Override
    public T set(int index, T element) {
        T old = backingList.set(index, element);
        if (!Objects.equals(element, old)) {
            onChange.callListeners();
        }
        return old;
    }

    @Override
    public void add(int index, T element) {
        backingList.add(index, element);
        onChange.callListeners();
    }

    @Override
    public T remove(int index) {
        var old = backingList.remove(index);
        onChange.callListeners();
        return old;
    }

    @Override
    public int indexOf(Object o) {
        return backingList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return backingList.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        var backingIterator = backingList.listIterator(index);
        return new ListIterator<>() {
            @Override
            public boolean hasNext() {
                return backingIterator.hasNext();
            }

            @Override
            public T next() {
                return backingIterator.next();
            }

            @Override
            public boolean hasPrevious() {
                return backingIterator.hasPrevious();
            }

            @Override
            public T previous() {
                return backingIterator.previous();
            }

            @Override
            public int nextIndex() {
                return backingIterator.nextIndex();
            }

            @Override
            public int previousIndex() {
                return backingIterator.previousIndex();
            }

            @Override
            public void remove() {
                backingIterator.remove();
                onChange.callListeners();
            }

            @Override
            public void set(T t) {
                backingIterator.set(t);
                onChange.callListeners();
            }

            @Override
            public void add(T t) {
                backingIterator.add(t);
                onChange.callListeners();
            }
        };
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        var sublist = backingList.subList(fromIndex, toIndex);
        var list = new FListWithEvents<>(sublist);
        list.onChange(onChange::callListeners);
        return list;
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public boolean isEmpty() {
        return backingList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return backingList.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        var backingIterator = backingList.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return backingIterator.hasNext();
            }

            @Override
            public T next() {
                return backingIterator.next();
            }

            @Override
            public void remove() {
                backingIterator.remove();
                onChange.callListeners();
            }
        };
    }

    @Override
    public Object[] toArray() {
        return backingList.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return backingList.toArray(a);
    }

    @Override
    public boolean add(T t) {
        boolean changed = backingList.add(t);
        if (changed) {
            onChange.callListeners();
        }
        return changed;
    }

    @Override
    public boolean remove(Object o) {
        boolean changed = backingList.remove(o);
        if (changed) {
            onChange.callListeners();
        }
        return changed;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return backingList.containsAll(c);
    }

    @Override
    public void clear() {
        if (!backingList.isEmpty()) {
            backingList.clear();
            onChange.callListeners();
        }
    }

    public void forEachWithIndex(BiConsumer<Integer, ? super T> action) {
        for (int i = 0; i < backingList.size(); i++) {
            action.accept(i, backingList.get(i));
        }
    }

    @CheckReturnValue
    public Disposable onChange(Runnable listener) {
        return onChange.add(listener);
    }

    @CheckReturnValue
    public Disposable onChangeAndRun(Runnable listener) {
        listener.run();
        return onChange(listener);
    }

    public static <T> FListWithEvents<T> fromMultiset(FMultisetWithEvents<T> multiset, Consumer<Disposable> disposer) {
        var list = new FListWithEvents<T>();
        disposer.accept(multiset.onRaiseAboveZero(list::add));
        disposer.accept(multiset.onFallToZero(list::remove));
        return list;
    }
}
