package org.teinelund.freshmavenproject.action;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ListOfAction implements Action, Iterable<Action> {

    private List<Action> listOfAction = new LinkedList<>();


    @Override
    public Iterator<Action> iterator() {
        return listOfAction.listIterator();
    }

    @Override
    public void forEach(Consumer<? super Action> action) {
        listOfAction.forEach(action);
    }

    @Override
    public Spliterator<Action> spliterator() {
        return this.listOfAction.spliterator();
    }

    public void addAction(Action action) {
        this.listOfAction.add(action);
    }

}
