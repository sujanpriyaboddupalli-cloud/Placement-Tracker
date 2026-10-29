import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

/** Part 1: Swing screens reached from a sidebar. Uses DataLayer (structures/algorithms) and DemoData. */
public class PlacementTracker extends DataLayer {
    static final List<Runnable> rf = new ArrayList<>();
    static JFrame frame;
    static final Color NAVY = new Color(20, 33, 61), BLUE = new Color(37, 99, 235), BG = new Color(244, 246, 250);
    static void refresh() { d.reindex(); for (Runnable r : rf) r.run(); }
    static void redExpired(JTable t, int col) {
        t.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sl, boolean f, int r, int c) {
                Component x = super.getTableCellRendererComponent(tb, v, sl, f, r, c);
                if (!sl) x.setForeground(String.valueOf(v).startsWith("EXPIRED") ? Color.RED : Color.BLACK);
                return x;
            }
        });
    }
    static void msg(String s) { JOptionPane.showMessageDialog(frame, s); }

    // ---------- UI helpers ----------
    static JButton btn(String t, boolean primary) {
        JButton b = new JButton(t);
        b.setFocusPainted(false);
        b.setBackground(primary ? BLUE : Color.WHITE);
        b.setForeground(primary ? Color.WHITE : Color.BLACK);
        return b;
    }
    static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    }
    static JTable table(DefaultTableModel m) { JTable t = new JTable(m); t.setRowHeight(26); t.setFillsViewportHeight(true); return t; }
    static JPanel page(String t, String s, JComponent body) {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JPanel h = new JPanel(new GridLayout(2, 1)); h.setOpaque(false);
        JLabel a = new JLabel(t); a.setFont(new Font("SansSerif", Font.PLAIN, 24));
        JLabel b = new JLabel(s); b.setForeground(Color.GRAY);
        h.add(a); h.add(b);
        p.add(h, BorderLayout.NORTH); p.add(body, BorderLayout.CENTER);
        return p;
    }
    static JPanel flow(Component... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); p.setOpaque(false);
        for (Component c : cs) p.add(c);
        return p;
    }
    static JPanel box(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 6)); p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(225, 228, 235)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        p.add(new JLabel(title), BorderLayout.NORTH); p.add(c, BorderLayout.CENTER);
        return p;
    }
    static String[] form(String title, String[] labels, String[] init) {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        JTextField[] f = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            p.add(new JLabel(labels[i])); f[i] = new JTextField(init == null ? "" : init[i], 18); p.add(f[i]);
        }
        if (JOptionPane.showConfirmDialog(frame, p, title, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return null;
        String[] r = new String[f.length];
        for (int i = 0; i < f.length; i++) r[i] = f[i].getText().trim();
        return r;
    }
    static int sel(JTable t) { int r = t.getSelectedRow(); if (r < 0) { msg("Select a row first."); return -1; } return r; }
    static int idOf(String combo) { return Integer.parseInt(combo.substring(1, combo.indexOf(' '))); }

    // ---------- Dashboard ----------
    static JPanel dashboard() {
        JLabel[] nums = new JLabel[4];
        String[] names = {"Total jobs", "Active applications", "Upcoming interviews", "Pending deadlines"};
        Color[] cols = {BLUE, new Color(22, 163, 74), new Color(217, 119, 6), new Color(220, 38, 38)};
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0)); cards.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            nums[i] = new JLabel("0"); nums[i].setFont(new Font("SansSerif", Font.BOLD, 30)); nums[i].setForeground(cols[i]);
            JPanel c = new JPanel(new GridLayout(2, 1)); c.setBackground(Color.WHITE);
            c.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            c.add(new JLabel(names[i])); c.add(nums[i]); cards.add(c);
        }
        DefaultTableModel pq = model("ID", "Student", "Company", "Role", "Deadline", "Package", "Status");
        DefaultTableModel up = model("Date", "Company", "Role");
        JPanel mid = new JPanel(new GridLayout(1, 2, 12, 0)); mid.setOpaque(false);
        JPanel left = box("Priority queue - nearest deadlines first", new JScrollPane(table(pq)));
        left.add(new JLabel("Ties are broken by higher package, then lower ID."), BorderLayout.SOUTH);
        mid.add(left); mid.add(box("Upcoming interviews", new JScrollPane(table(up))));
        JPanel body = new JPanel(new BorderLayout(0, 12)); body.setOpaque(false);
        body.add(cards, BorderLayout.NORTH); body.add(mid, BorderLayout.CENTER);
        rf.add(() -> {
            int ups = 0, pend = 0;
            for (Job j : d.jobs) {
                if (j.interview != null && !j.interview.isBefore(TODAY)) ups++;
                if (!j.deadline.isBefore(TODAY)) pend++;
            }
            nums[0].setText("" + d.jobs.size()); nums[1].setText("" + d.apps.size());
            nums[2].setText("" + ups); nums[3].setText("" + pend);
            PriorityQueue<App> q = new PriorityQueue<>((a, b) -> {
                Job x = d.jm.get(a.jid), y = d.jm.get(b.jid);
                int c = x.deadline.compareTo(y.deadline);
                if (c == 0) c = Double.compare(y.pkg, x.pkg);
                return c != 0 ? c : Integer.compare(a.id, b.id);
            });
            q.addAll(d.apps);
            pq.setRowCount(0);
            while (!q.isEmpty()) {
                App a = q.poll(); Job j = d.jm.get(a.jid);
                pq.addRow(new Object[]{a.id, d.sm.get(a.sid).name, j.company, j.title, j.deadline, j.pkg, a.status});
            }
            up.setRowCount(0);
            List<Job> l = new ArrayList<>();
            for (Job j : d.jobs) if (j.interview != null && !j.interview.isBefore(TODAY)) l.add(j);
            for (Job j : msort(l, Comparator.comparing((Job x) -> x.interview))) up.addRow(new Object[]{j.interview, j.company, j.title});
        });
        return page("Dashboard", "Your placement season at a glance", body);
    }

    // ---------- Jobs ----------
    static List<Job> shown = new ArrayList<>();
    static JPanel jobs() {
        DefaultTableModel m = model("ID", "Company", "Job Title", "Skills", "Package (LPA)", "Deadline", "Status", "Interview");
        JTable t = table(m); redExpired(t, 5);
        JComboBox<String> sort = new JComboBox<>(new String[]{"None", "Salary (high first)", "Deadline", "Company", "Title"});
        JComboBox<String> mode = new JComboBox<>(new String[]{"Linear: any field", "Binary: company", "Binary: title"});
        JTextField q = new JTextField(16);
        JButton add = btn("+ Add job", true), edit = btn("Edit", false), del = btn("Delete", false),
                go = btn("Search", false), clr = btn("Clear", false);
        Runnable load = () -> {
            List<Job> l = new ArrayList<>(d.jobs);
            switch (sort.getSelectedIndex()) {
                case 1 -> l = msort(l, (a, b) -> Double.compare(b.pkg, a.pkg));
                case 2 -> l = msort(l, Comparator.comparing((Job a) -> a.deadline));
                case 3 -> l = msort(l, (a, b) -> a.company.compareToIgnoreCase(b.company));
                case 4 -> l = msort(l, (a, b) -> a.title.compareToIgnoreCase(b.title));
                default -> { }
            }
            String s = q.getText().trim().toLowerCase();
            if (!s.isEmpty()) {
                List<Job> r = new ArrayList<>();
                if (mode.getSelectedIndex() == 0) {                       // linear search
                    for (Job j : l) if ((j.company + " " + j.title + " " + j.skills + " " + j.status).toLowerCase().contains(s)) r.add(j);
                } else {                                                    // binary search (sort by key first)
                    boolean co = mode.getSelectedIndex() == 1;
                    java.util.function.Function<Job, String> f = co ? x -> x.company : x -> x.title;
                    List<Job> ks = msort(new ArrayList<>(d.jobs), (a, b) -> f.apply(a).compareToIgnoreCase(f.apply(b)));
                    int i = bsearch(ks, s, f);
                    if (i >= 0) {
                        int lo = i, hi = i;
                        while (lo > 0 && f.apply(ks.get(lo - 1)).equalsIgnoreCase(s)) lo--;
                        while (hi < ks.size() - 1 && f.apply(ks.get(hi + 1)).equalsIgnoreCase(s)) hi++;
                        r.addAll(ks.subList(lo, hi + 1));
                    }
                }
                l = r;
            }
            shown = l; m.setRowCount(0);
            for (Job j : l) m.addRow(new Object[]{j.id, j.company, j.title, j.skills, j.pkg, dl(j.deadline), j.status,
                    j.interview == null ? "-" : j.interview.toString()});
        };
        rf.add(load);
        sort.addActionListener(e -> load.run()); go.addActionListener(e -> load.run());
        clr.addActionListener(e -> { q.setText(""); sort.setSelectedIndex(0); load.run(); });
        String[] lb = {"Company", "Job title", "Skills", "Package (LPA)", "Deadline (yyyy-MM-dd)", "Status (Open/Closed/Interview)", "Interview date (blank = none)"};
        add.addActionListener(e -> saveJob(null, lb));
        edit.addActionListener(e -> { int r = sel(t); if (r >= 0) saveJob(shown.get(r), lb); });
        del.addActionListener(e -> {
            int r = sel(t); if (r < 0) return;
            Job j = shown.get(r); snap();
            d.jobs.remove(j); d.apps.removeIf(a -> a.jid == j.id); refresh();
        });
        JPanel top = new JPanel(new GridLayout(2, 1)); top.setOpaque(false);
        top.add(flow(add, edit, del, new JLabel("Sort by:"), sort));
        top.add(flow(new JLabel("Search:"), q, mode, go, clr));
        JPanel body = new JPanel(new BorderLayout()); body.setOpaque(false);
        body.add(top, BorderLayout.NORTH); body.add(new JScrollPane(t), BorderLayout.CENTER);
        return page("Jobs", "Add, edit, sort and search placement opportunities", body);
    }
    static void saveJob(Job j, String[] lb) {
        String[] init = j == null ? null : new String[]{j.company, j.title, j.skills, "" + j.pkg, "" + j.deadline, j.status,
                j.interview == null ? "" : "" + j.interview};
        String[] v = form(j == null ? "Add job" : "Edit job", lb, init);
        if (v == null) return;
        try {
            double p = Double.parseDouble(v[3]); LocalDate dd = LocalDate.parse(v[4]);
            LocalDate iv = v[6].isEmpty() ? null : LocalDate.parse(v[6]);
            if (v[0].isEmpty() || v[1].isEmpty()) { msg("Company and title are required."); return; }
            if (dd.isBefore(TODAY)) v[5] = "Closed";   // expired jobs are Closed
            snap();
            if (j == null) d.jobs.add(new Job(d.nj++, v[0], v[1], v[2], p, dd, v[5].isEmpty() ? "Open" : v[5], iv));
            else { j.company = v[0]; j.title = v[1]; j.skills = v[2]; j.pkg = p; j.deadline = dd; j.status = v[5]; j.interview = iv; }
            refresh();
        } catch (Exception ex) { msg("Invalid number or date. Use e.g. 7.5 and 2026-12-31."); }
    }

    // ---------- Students ----------
    static List<Student> sshown = new ArrayList<>();
    static JPanel students() {
        DefaultTableModel m = model("ID", "Name", "Email", "Branch", "CGPA", "Skills", "Applications");
        JTable t = table(m);
        JTextField q = new JTextField(12), idf = new JTextField(5);
        JButton add = btn("+ Add student", true), edit = btn("Edit", false), del = btn("Delete", false),
                go = btn("Search", false), find = btn("Find by ID", false);
        Runnable load = () -> {
            String s = q.getText().trim().toLowerCase();
            sshown = new ArrayList<>();
            for (Student x : d.students)
                if (s.isEmpty() || (x.name + " " + x.email + " " + x.branch + " " + x.skills).toLowerCase().contains(s)) sshown.add(x);
            m.setRowCount(0);
            for (Student x : sshown) m.addRow(new Object[]{x.id, x.name, x.email, x.branch, x.cgpa, x.skills, d.appCount(x.id)});
        };
        rf.add(load);
        go.addActionListener(e -> load.run());
        find.addActionListener(e -> {
            try {
                Student x = d.sm.get(Integer.parseInt(idf.getText().trim()));          // O(1) HashMap lookup
                if (x == null) msg("No student with that ID.");
                else msg("#" + x.id + " " + x.name + "\n" + x.email + "\n" + x.branch + ", CGPA " + x.cgpa + "\nSkills: " + x.skills);
            } catch (NumberFormatException ex) { msg("Enter a numeric ID."); }
        });
        String[] lb = {"Name", "Email", "Branch", "CGPA", "Skills"};
        add.addActionListener(e -> saveStudent(null, lb));
        edit.addActionListener(e -> { int r = sel(t); if (r >= 0) saveStudent(sshown.get(r), lb); });
        del.addActionListener(e -> {
            int r = sel(t); if (r < 0) return;
            Student x = sshown.get(r); snap();
            d.students.remove(x); d.apps.removeIf(a -> a.sid == x.id); d.wait.remove(Integer.valueOf(x.id)); refresh();
        });
        JPanel body = new JPanel(new BorderLayout()); body.setOpaque(false);
        body.add(flow(add, edit, del, new JLabel("Search:"), q, go, new JLabel("ID:"), idf, find), BorderLayout.NORTH);
        body.add(new JScrollPane(t), BorderLayout.CENTER);
        return page("Students", "Student records with O(1) HashMap lookup by ID", body);
    }
    static void saveStudent(Student s, String[] lb) {
        String[] init = s == null ? null : new String[]{s.name, s.email, s.branch, "" + s.cgpa, s.skills};
        String[] v = form(s == null ? "Add student" : "Edit student", lb, init);
        if (v == null) return;
        try {
            double c = Double.parseDouble(v[3]);
            if (v[0].isEmpty() || c < 0 || c > 10) { msg("Name required and CGPA must be 0-10."); return; }
            snap();
            if (s == null) d.students.add(new Student(d.ns++, v[0], v[1], v[2], c, v[4]));
            else { s.name = v[0]; s.email = v[1]; s.branch = v[2]; s.cgpa = c; s.skills = v[4]; }
            refresh();
        } catch (NumberFormatException ex) { msg("CGPA must be a number."); }
    }

    // ---------- Applications ----------
    static void fill(JComboBox<String> c, List<String> items) {
        int i = c.getSelectedIndex();
        c.removeAllItems(); for (String s : items) c.addItem(s);
        if (c.getItemCount() > 0) c.setSelectedIndex(Math.min(Math.max(i, 0), c.getItemCount() - 1));
    }
    static JPanel applications() {
        DefaultTableModel m = model("ID", "Student", "Company", "Role", "Deadline", "Applied on", "Status");
        JTable t = table(m); redExpired(t, 4);
        JComboBox<String> sc = new JComboBox<>(), jc = new JComboBox<>(),
                st = new JComboBox<>(new String[]{"Applied", "Shortlisted", "Interview"});
        JButton apply = btn("Apply", true), set = btn("Set status", false), del = btn("Delete", false);
        rf.add(() -> {
            List<String> a = new ArrayList<>(), b = new ArrayList<>();
            for (Student s : d.students) a.add("#" + s.id + " " + s.name);
            for (Job j : d.jobs) b.add("#" + j.id + " " + j.company + " - " + j.title);
            fill(sc, a); fill(jc, b);
            m.setRowCount(0);
            for (App x : d.apps) {
                Job j = d.jm.get(x.jid);
                m.addRow(new Object[]{x.id, d.sm.get(x.sid).name, j.company, j.title, dl(j.deadline), x.on, x.status});
            }
        });
        apply.addActionListener(e -> {
            if (sc.getSelectedItem() == null || jc.getSelectedItem() == null) return;
            int s = idOf((String) sc.getSelectedItem()), j = idOf((String) jc.getSelectedItem());
            if (d.keys.contains(s + ":" + j)) { msg("Duplicate blocked: this student already applied to this job (HashSet)."); return; }
            Job job = d.jm.get(j);
            if (job.deadline.isBefore(TODAY) || job.status.equals("Closed")) { msg("This job is closed or expired."); return; }
            snap(); d.apps.add(new App(d.na++, s, j, TODAY, "Applied")); refresh();
        });
        set.addActionListener(e -> {
            int r = sel(t); if (r < 0) return;
            int id = (Integer) m.getValueAt(r, 0); snap();
            for (App a : d.apps) if (a.id == id) a.status = (String) st.getSelectedItem();
            refresh();
        });
        del.addActionListener(e -> {
            int r = sel(t); if (r < 0) return;
            int id = (Integer) m.getValueAt(r, 0); snap();
            d.apps.removeIf(a -> a.id == id); refresh();
        });
        JPanel top = new JPanel(new GridLayout(2, 1)); top.setOpaque(false);
        top.add(flow(new JLabel("Student:"), sc, new JLabel("Job:"), jc, apply));
        top.add(flow(new JLabel("Selected application:"), st, set, del));
        JPanel body = new JPanel(new BorderLayout()); body.setOpaque(false);
        body.add(top, BorderLayout.NORTH); body.add(new JScrollPane(t), BorderLayout.CENTER);
        return page("Applications", "HashSet blocks duplicate applications to the same job", body);
    }

    // ---------- Interviews ----------
    static JPanel interviews() {
        DefaultListModel<String> lm = new DefaultListModel<>();
        JList<String> list = new JList<>(lm); list.setFixedCellHeight(26);
        JComboBox<String> sc = new JComboBox<>();
        JLabel next = new JLabel(" ");
        JButton join = btn("Join queue", true), serve = btn("Serve next", false);
        DefaultTableModel m = model("Date", "Student", "Company", "Result", "Notes");
        JTable t = table(m);
        t.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                Component x = super.getTableCellRendererComponent(tb, v, s, f, r, c);
                if (!s) x.setForeground("Selected".equals(v) ? new Color(22, 163, 74) : new Color(220, 38, 38));
                return x;
            }
        });
        JButton log = btn("+ Log interview", true), rem = btn("Remove oldest", false);
        rf.add(() -> {
            List<String> a = new ArrayList<>();
            for (Student s : d.students) a.add("#" + s.id + " " + s.name);
            fill(sc, a);
            lm.clear(); int i = 1;
            for (int id : d.wait) lm.addElement(i++ + ".  " + d.sm.get(id).name);
            Integer h = d.wait.peek();
            next.setText(h == null ? "Queue is empty" : "Next up: " + d.sm.get(h).name);
            m.setRowCount(0);
            for (Hist h2 : d.hist) m.addRow(new Object[]{h2.date(), h2.student(), h2.company(), h2.result(), h2.notes()});
        });
        join.addActionListener(e -> {
            if (sc.getSelectedItem() == null) return;
            int id = idOf((String) sc.getSelectedItem());
            if (d.wait.contains(id)) { msg("Already waiting in the queue."); return; }
            snap(); d.wait.offer(id); refresh();
        });
        serve.addActionListener(e -> {
            if (d.wait.isEmpty()) { msg("Nobody is waiting."); return; }
            snap(); int id = d.wait.poll(); refresh(); msg("Now helping: " + d.sm.get(id).name);
        });
        log.addActionListener(e -> {
            String[] v = form("Log interview", new String[]{"Student", "Company", "Result (Selected/Rejected)", "Notes"}, null);
            if (v == null || v[0].isEmpty() || v[1].isEmpty()) return;
            snap(); d.hist.addFirst(new Hist(TODAY, v[0], v[1], v[2].isEmpty() ? "Rejected" : v[2], v[3])); refresh();
        });
        rem.addActionListener(e -> { if (!d.hist.isEmpty()) { snap(); d.hist.removeLast(); refresh(); } });
        JPanel l = box("Waiting list (Queue - FIFO)", new JScrollPane(list));
        JPanel ls = new JPanel(new GridLayout(2, 1)); ls.setOpaque(false);
        ls.add(flow(sc, join, serve)); ls.add(next); l.add(ls, BorderLayout.SOUTH);
        JPanel r = box("Interview history (LinkedList, newest first)", new JScrollPane(t));
        r.add(flow(log, rem), BorderLayout.SOUTH);
        JPanel body = new JPanel(new GridLayout(1, 2, 12, 0)); body.setOpaque(false);
        body.add(l); body.add(r);
        return page("Interviews", "Queue for mock-interview help, LinkedList for interview history", body);
    }

    // ---------- DSA Learning Lab ----------
    static JPanel lab() {
        String[][] rows = {
                {"ArrayList", "Job and student tables", "get(i) O(1), add O(1)*, remove O(n)", "Dynamic array; fast index access."},
                {"HashMap", "Find a record by ID", "get/put/remove O(1) avg", "Hashes the key to a bucket."},
                {"HashSet", "Block duplicate applications", "add/contains O(1) avg", "Stores unique 'studentId:jobId' keys."},
                {"Stack (ArrayDeque)", "Undo last change", "push/pop O(1)", "Last in, first out."},
                {"Queue (LinkedList)", "Interview help waiting list", "offer/poll/peek O(1)", "First in, first out."},
                {"LinkedList", "Interview history", "addFirst/removeLast O(1), search O(n)", "Nodes linked to each other."},
                {"PriorityQueue (heap)", "Nearest-deadline applications", "offer/poll O(log n), peek O(1)", "Min-heap by deadline, ties by package."},
                {"Merge sort", "Sort jobs by salary/deadline/etc.", "O(n log n) time, O(n) space", "Split, sort halves, merge. Stable."},
                {"Linear search", "Free-text search", "O(n)", "Checks every record; works unsorted."},
                {"Binary search", "Exact company/title search", "O(log n) after O(n log n) sort", "Halves the range each step; needs sorted data."},
                {"Recursion", "Count jobs needing a skill", "O(n) time, O(n) stack", "Calls itself on the rest of the list."}};
        DefaultTableModel m = model("Structure / Algorithm", "Used for", "Time complexity", "Idea");
        for (String[] r : rows) m.addRow(r);
        JComboBox<String> demo = new JComboBox<>(new String[]{"Stack (undo)", "Queue (FIFO)", "LinkedList", "PriorityQueue",
                "HashSet", "HashMap", "Merge sort", "Linear search", "Binary search", "Recursion"});
        JTextField in = new JTextField("java", 14);
        JTextArea out = new JTextArea("Pick a demo to see a sample operation step by step.", 6, 40);
        out.setEditable(false); out.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JButton run = btn("Run demo", true);
        run.addActionListener(e -> out.setText(runDemo((String) demo.getSelectedItem(), in.getText())));
        JPanel bottom = new JPanel(new BorderLayout(0, 6)); bottom.setOpaque(false);
        bottom.add(flow(new JLabel("Demo:"), demo, new JLabel("Input:"), in, run), BorderLayout.NORTH);
        bottom.add(new JScrollPane(out), BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout(0, 8)); body.setOpaque(false);
        body.add(new JScrollPane(table(m)), BorderLayout.CENTER); body.add(bottom, BorderLayout.SOUTH);
        return page("DSA Learning Lab", "Every data structure and algorithm used in this app, with live demos", body);
    }
    static String runDemo(String name, String text) {
        String[] w = text.trim().isEmpty() ? new String[]{"java"} : text.trim().split("[,\\s]+");
        StringBuilder sb = new StringBuilder();
        switch (name) {
            case "Stack (undo)" -> {
                Deque<String> s = new ArrayDeque<>();
                for (String x : w) { s.push(x); sb.append("push ").append(x).append("  -> ").append(s).append('\n'); }
                sb.append("pop  ").append(s.pop()).append("  -> ").append(s).append("   (last in, first out)");
            }
            case "Queue (FIFO)" -> {
                Queue<String> q = new LinkedList<>();
                for (String x : w) { q.offer(x); sb.append("offer ").append(x).append(" -> ").append(q).append('\n'); }
                sb.append("poll  ").append(q.poll()).append(" -> ").append(q).append("   (first in, first out)");
            }
            case "LinkedList" -> {
                LinkedList<String> l = new LinkedList<>();
                for (String x : w) { l.addFirst(x); sb.append("addFirst ").append(x).append(" -> ").append(l).append('\n'); }
                sb.append("removeLast ").append(l.removeLast()).append(" -> ").append(l);
            }
            case "HashSet" -> {
                Set<String> s = new HashSet<>();
                for (String x : w) sb.append("add ").append(x).append(s.add(x) ? " -> added" : " -> duplicate rejected").append('\n');
                sb.append("size = ").append(s.size());
            }
            case "HashMap" -> {
                Map<String, Integer> mp = new HashMap<>();
                for (String x : w) { mp.put(x, x.length()); sb.append("put(").append(x).append(", ").append(x.length()).append(")\n"); }
                sb.append("get(").append(w[0]).append(") = ").append(mp.get(w[0])).append("  in O(1) average");
            }
            case "PriorityQueue", "Merge sort" -> {
                try {
                    List<Integer> nums = new ArrayList<>();
                    for (String x : w) nums.add(Integer.parseInt(x));
                    if (name.equals("PriorityQueue")) {
                        PriorityQueue<Integer> pq = new PriorityQueue<>(nums);
                        sb.append("Heap order: ").append(pq).append('\n').append("poll order: ");
                        while (!pq.isEmpty()) sb.append(pq.poll()).append(' ');
                    } else sb.append("Input : ").append(nums).append('\n').append("Sorted: ").append(msort(nums, Integer::compare));
                } catch (NumberFormatException ex) { sb.append("Enter numbers separated by commas, e.g. 5, 3, 9, 1"); }
            }
            case "Linear search" -> {
                int steps = 0;
                for (int i = 0; i < d.jobs.size(); i++) {
                    steps++;
                    boolean hit = d.jobs.get(i).company.equalsIgnoreCase(w[0]);
                    sb.append("check ").append(d.jobs.get(i).company).append(hit ? "  <- found" : "").append('\n');
                    if (hit) break;
                }
                sb.append("steps: ").append(steps).append("  (searching company names)");
            }
            case "Binary search" -> {
                List<Job> s = msort(new ArrayList<>(d.jobs), (a, b) -> a.company.compareToIgnoreCase(b.company));
                int lo = 0, hi = s.size() - 1, steps = 0; boolean found = false;
                while (lo <= hi) {
                    int mid = (lo + hi) >>> 1, c = s.get(mid).company.compareToIgnoreCase(w[0]); steps++;
                    sb.append("range [").append(lo).append(",").append(hi).append("] mid=").append(s.get(mid).company).append('\n');
                    if (c == 0) { found = true; break; }
                    if (c < 0) lo = mid + 1; else hi = mid - 1;
                }
                sb.append(found ? "found" : "not found").append(" in ").append(steps).append(" steps (company names, sorted)");
            }
            default -> sb.append("Jobs needing '").append(w[0]).append("': ").append(countSkill(d.jobs, 0, w[0])).append("  (recursive count)");
        }
        return sb.toString();
    }

    // ---------- main ----------
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> {
            d = seed();
            frame = new JFrame("Placement Tracker - DSA in Java");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            CardLayout cl = new CardLayout(); JPanel cards = new JPanel(cl);
            String[] names = {"Dashboard", "Jobs", "Students", "Applications", "Interviews", "DSA Learning Lab"};
            JPanel[] ps = {dashboard(), jobs(), students(), applications(), interviews(), lab()};
            JPanel side = new JPanel(new BorderLayout()); side.setBackground(NAVY);
            side.setPreferredSize(new Dimension(210, 0));
            side.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));
            JPanel nav = new JPanel(); nav.setOpaque(false); nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("<html><div style='font-size:16px'>Placement<br>Tracker</div></html>");
            title.setForeground(Color.WHITE); JLabel sub = new JLabel("DSA in Java"); sub.setForeground(Color.LIGHT_GRAY);
            nav.add(title); nav.add(sub); nav.add(Box.createVerticalStrut(16));
            JButton[] nb = new JButton[names.length];
            for (int i = 0; i < names.length; i++) {
                final int k = i; cards.add(ps[i], names[i]);
                nb[i] = new JButton(names[i]); nb[i].setMaximumSize(new Dimension(300, 34));
                nb[i].setHorizontalAlignment(SwingConstants.LEFT); nb[i].setForeground(Color.WHITE);
                nb[i].setBackground(i == 0 ? BLUE : NAVY); nb[i].setBorderPainted(false); nb[i].setFocusPainted(false);
                nb[i].addActionListener(e -> {
                    cl.show(cards, names[k]);
                    for (int j = 0; j < nb.length; j++) nb[j].setBackground(j == k ? BLUE : NAVY);
                });
                nav.add(nb[i]); nav.add(Box.createVerticalStrut(4));
            }
            JButton un = btn("Undo last change", false), rs = btn("Reset demo data", false);
            un.addActionListener(e -> { if (undo.isEmpty()) msg("Nothing to undo."); else { d = undo.pop(); refresh(); } });
            rs.addActionListener(e -> { undo.clear(); d = seed(); refresh(); });
            JPanel foot = new JPanel(new GridLayout(2, 1, 0, 6)); foot.setOpaque(false); foot.add(un); foot.add(rs);
            side.add(nav, BorderLayout.NORTH); side.add(foot, BorderLayout.SOUTH);
            frame.add(side, BorderLayout.WEST); frame.add(cards, BorderLayout.CENTER);
            refresh();
            frame.setSize(1200, 760); frame.setLocationRelativeTo(null); frame.setVisible(true);
        });
    }
}
