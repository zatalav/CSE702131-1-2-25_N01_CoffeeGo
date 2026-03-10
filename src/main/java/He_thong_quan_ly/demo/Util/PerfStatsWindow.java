package He_thong_quan_ly.demo.Util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class PerfStatsWindow {

    private final int capacity;
    private final Deque<Long> samples = new ArrayDeque<>();

    public PerfStatsWindow(int capacity) {
        this.capacity = Math.max(10, capacity);
    }

    public synchronized Snapshot addAndSnapshot(long valueMs) {
        long safe = Math.max(0L, valueMs);
        if (samples.size() >= capacity) {
            samples.removeFirst();
        }
        samples.addLast(safe);
        return snapshotUnsafe();
    }

    private Snapshot snapshotUnsafe() {
        if (samples.isEmpty()) {
            return new Snapshot(0, 0, 0, 0);
        }
        List<Long> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        int n = sorted.size();
        long p50 = percentile(sorted, 0.50);
        long p95 = percentile(sorted, 0.95);
        long p99 = percentile(sorted, 0.99);
        return new Snapshot(n, p50, p95, p99);
    }

    private long percentile(List<Long> sorted, double p) {
        int idx = (int) Math.ceil(p * sorted.size()) - 1;
        idx = Math.max(0, Math.min(idx, sorted.size() - 1));
        return sorted.get(idx);
    }

    public static final class Snapshot {
        private final int count;
        private final long p50;
        private final long p95;
        private final long p99;

        public Snapshot(int count, long p50, long p95, long p99) {
            this.count = count;
            this.p50 = p50;
            this.p95 = p95;
            this.p99 = p99;
        }

        public int count() {
            return count;
        }

        public long p50() {
            return p50;
        }

        public long p95() {
            return p95;
        }

        public long p99() {
            return p99;
        }
    }
}
