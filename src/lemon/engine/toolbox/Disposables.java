package lemon.engine.toolbox;

import java.util.ArrayList;
import java.util.List;

public class Disposables implements Disposable {
    private final List<Disposable> list;
    public Disposables() {
        list = new ArrayList<>();
    }
    public void add(Disposable disposable) {
        list.add(disposable);
    }
    @Override
    public void dispose() {
        for (Disposable disposable : list) {
            disposable.dispose();
        }
        list.clear();
    }
}
