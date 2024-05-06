package main.core;

public class Stage {
    private int first;
    private int second;
    private int extra;
    private int count;
    private int total;

    private Stage(int first, int second, int extra, int count, int total) {
        this.first = first;
        this.count = count;
        this.extra = extra;
        this.second = second;
        this.total = total;
    }

    public String name() {
        return extra == 0
                ? "%d-%d".formatted(first, second)
                : "%d-%d-%d".formatted(first, second, extra);
    }

    public String toString() {
        return "%d-%d-%d-%d-%d".formatted(first, second, extra, count, total);
    }

    public void update(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int getCount() { return count; }
    public void addCount() { count++; }
    public void minusCount() { count = Math.max(--count, 0); }
    public int getTotal() { return total; }
    public void addTotal() { total++; }
    public void minusTotal() { total = Math.max(--total, 1); }

    public static Stage of(int first, int second, int extra, int count, int total) {
        return new Stage(first, second, extra, count, total);
    }
    public static Stage of(int first, int second, int count, int total) {
        return new Stage(first, second, 0, count, total);
    }
    public static Stage of(int first, int second) {
        return new Stage(first, second, 0, 0, 1);
    }
}
