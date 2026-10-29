import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

/** Part 2: data-structure layer - collections (ArrayList, HashMap, HashSet, Stack, Queue, LinkedList,
 *  PriorityQueue is used by the dashboard) and algorithms (merge sort, binary search, recursion). */
// ---------- model ----------
class Job {
    int id; String company, title, skills, status; double pkg; LocalDate deadline, interview;
    Job(int id, String c, String t, String s, double p, LocalDate d, String st, LocalDate iv) {
        this.id = id; company = c; title = t; skills = s; pkg = p; deadline = d; status = st; interview = iv;
    }
    Job copy() { return new Job(id, company, title, skills, pkg, deadline, status, interview); }
}
class Student {
    int id; String name, email, branch, skills; double cgpa;
    Student(int id, String n, String e, String b, double c, String s) {
        this.id = id; name = n; email = e; branch = b; cgpa = c; skills = s;
    }
    Student copy() { return new Student(id, name, email, branch, cgpa, skills); }
}
class App {
    int id, sid, jid; LocalDate on; String status;
    App(int id, int sid, int jid, LocalDate on, String st) { this.id = id; this.sid = sid; this.jid = jid; this.on = on; status = st; }
    App copy() { return new App(id, sid, jid, on, status); }
}
record Hist(LocalDate date, String student, String company, String result, String notes) {}

class Data {
    ArrayList<Job> jobs = new ArrayList<>();
    ArrayList<Student> students = new ArrayList<>();
    ArrayList<App> apps = new ArrayList<>();
    LinkedList<Integer> wait = new LinkedList<>();        // Queue (LinkedList, FIFO)
    LinkedList<Hist> hist = new LinkedList<>();          // newest first
    HashMap<Integer, Job> jm = new HashMap<>();
    HashMap<Integer, Student> sm = new HashMap<>();
    HashSet<String> keys = new HashSet<>();               // "studentId:jobId"
    int nj = 1, ns = 1, na = 1;
    void reindex() {
        jm.clear(); sm.clear(); keys.clear();
        for (Job j : jobs) jm.put(j.id, j);
        for (Student s : students) sm.put(s.id, s);
        for (App a : apps) keys.add(a.sid + ":" + a.jid);
    }
    Data copy() {
        Data c = new Data();
        for (Job j : jobs) c.jobs.add(j.copy());
        for (Student s : students) c.students.add(s.copy());
        for (App a : apps) c.apps.add(a.copy());
        c.wait.addAll(wait); c.hist.addAll(hist);
        c.nj = nj; c.ns = ns; c.na = na; c.reindex();
        return c;
    }
    int appCount(int sid) { int n = 0; for (App a : apps) if (a.sid == sid) n++; return n; }
}


public class DataLayer extends DemoData {
    static Data d;
    static final Deque<Data> undo = new ArrayDeque<>();     // Stack
    static void snap() { undo.push(d.copy()); }
    static String dl(LocalDate x) {
        long n = ChronoUnit.DAYS.between(TODAY, x);
        return n < 0 ? "EXPIRED " + x : x + "  (" + n + "d left)";
    }

    // ---------- algorithms ----------
    static <T> List<T> msort(List<T> l, Comparator<T> c) {          // merge sort (stable)
        if (l.size() <= 1) return l;
        int m = l.size() / 2;
        List<T> a = msort(new ArrayList<>(l.subList(0, m)), c), b = msort(new ArrayList<>(l.subList(m, l.size())), c);
        List<T> r = new ArrayList<>();
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) r.add(c.compare(a.get(i), b.get(j)) <= 0 ? a.get(i++) : b.get(j++));
        while (i < a.size()) r.add(a.get(i++));
        while (j < b.size()) r.add(b.get(j++));
        return r;
    }
    static int bsearch(List<Job> l, String key, java.util.function.Function<Job, String> f) {
        int lo = 0, hi = l.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1, c = f.apply(l.get(mid)).compareToIgnoreCase(key);
            if (c == 0) return mid;
            if (c < 0) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }
    static int countSkill(List<Job> l, int i, String s) {            // recursion
        if (i >= l.size()) return 0;
        return (l.get(i).skills.toLowerCase().contains(s.toLowerCase()) ? 1 : 0) + countSkill(l, i + 1, s);
    }

}
