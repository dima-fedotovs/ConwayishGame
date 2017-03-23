package guru.bug.gonwayish.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Dimitrijs Fedotovs <a href="http://www.bug.guru">www.bug.guru</a>
 * @version 1.0
 * @since 1.0
 */
public class Field {
    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private Map<Position, Cell> fieldMap;
    private boolean running;

    public Map<Position, CellInfo> getSnapshot() {
        return fieldMap.values().stream()
                .map(Cell::getCellInfo)
                .collect(Collectors.toMap(CellInfo::getPosition, Function.identity()));
    }

    public void start(Function<Position, Boolean> initState) {
        if (running) {
            throw new IllegalStateException("Already running");
        }
        fieldMap = Position.all().stream()
                .map(p -> {
                    boolean alive = initState.apply(p);
                    Cell result = new Cell(this, p, alive);
                    executor.execute(result);
                    return result;
                })
                .collect(Collectors.toMap(Cell::getPosition, Function.identity()));
        setRunning(true);
    }

    public void stop() {
        setRunning(false);
    }

    private synchronized void setRunning(boolean running) {
        this.running = running;
        this.notifyAll();
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public Set<Cell> findAround(Position pos) {
        return pos.around().stream()
                .map(fieldMap::get)
                .peek(Cell::lock)
                .collect(Collectors.toSet());
    }

    public void releaseAround(Position pos) {
        pos.around().stream()
                .map(fieldMap::get)
                .forEach(Cell::unlock);
    }
}
