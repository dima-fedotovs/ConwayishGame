package guru.bug.gonwayish.model;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Dimitrijs Fedotovs <a href="http://www.bug.guru">www.bug.guru</a>
 * @version 1.0
 * @since 1.0
 */
public class Cell implements Runnable {
    private static final long LIFE_PERIOD = 2000; // milliseconds
    private final ReentrantLock lock = new ReentrantLock();
    private final Field field;
    private final Position position;
    private double size;
    private long birthtime;

    Cell(Field field, Position position, boolean initialAlive) {
        this.field = field;
        this.position = position;

        if (initialAlive) {
            this.birthtime = System.currentTimeMillis();
            this.size = 1;
        } else {
            this.birthtime = -1;
            this.size = 0;
        }
    }

    public Position getPosition() {
        return position;
    }

    public Field getField() {
        return field;
    }

    @Override
    public void run() {
        waitUntilFieldReady();
        while (field.isRunning()) {
            pause();
            lock();
            try {
                long bt = getBirthtime();
                long cur = System.currentTimeMillis();
                List<Cell> around = field.findAroundAndTryLock(position);
                if (around == null) {
                    continue;
                }
                try {
                    long liveCount = around.stream()
                            .map(Cell::getCellInfo)
                            .filter(CellInfo::isAlive)
                            .count();

                    if (bt == -1 && liveCount == 3) {
                        bt = System.currentTimeMillis();
                        updateCellInfo(bt, 1);
                    }

                    long age = cur - bt;

                    if (age > LIFE_PERIOD && bt != -1) {
                        updateCellInfo(-1, 0);
                    }
                } finally {
                    field.releaseAround(position);
                }
            } finally {
                unlock();
            }

        }
        System.out.println("Cell " + position + " finished");
    }

    private void waitUntilFieldReady() {
        synchronized (field) {
            while (!field.isRunning()) {
                try {
                    field.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    private void pause() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private synchronized void updateCellInfo(long birthtime, double size) {
        setBirthtime(birthtime);
        setSize(size);
    }

    private synchronized void setSize(double size) {
        this.size = size;
    }

    private synchronized void setBirthtime(long birthtime) {
        this.birthtime = birthtime;
    }

    private synchronized long getBirthtime() {
        return birthtime;
    }

    public synchronized CellInfo getCellInfo() {
        return new CellInfo(position, birthtime > -1, size);
    }

    public void lock() {
        lock.lock();
    }

    public boolean tryLock() {
        return lock.tryLock();
    }

    public void unlock() {
        lock.unlock();
    }
}
