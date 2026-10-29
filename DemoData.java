import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

/** Part 3: in-memory demonstration data (4 students, 7 jobs, 7 applications, waiting list, history). */
public class DemoData {
    // Fixed demo date so the numbers always match the report (deadlines 2026-11-01 etc.).
    // For real use, change to LocalDate.now().
    static final LocalDate TODAY = LocalDate.of(2026, 10, 29);

    static LocalDate day(int off) { return TODAY.plusDays(off); }

    static Data seed() {
        Data x = new Data();
        String[][] st = {{"Aarav Sharma", "aarav@college.edu", "CSE", "8.7", "Java, DSA, SQL"},
                {"Diya Patel", "diya@college.edu", "IT", "9.1", "Python, ML, Java"},
                {"Rohan Mehta", "rohan@college.edu", "ECE", "7.8", "C++, Embedded, Java"},
                {"Sneha Iyer", "sneha@college.edu", "CSE", "8.2", "React, JavaScript, Java"}};
        for (String[] s : st) x.students.add(new Student(x.ns++, s[0], s[1], s[2], Double.parseDouble(s[3]), s[4]));
        Object[][] jb = {{"Infosys", "Systems Engineer", "Java, SQL", 4.5, 12, "Open", null},
                {"TCS", "Software Developer", "Java, Spring", 7.0, 5, "Open", 9},
                {"Google", "SWE Intern", "DSA, Java, Python", 18.0, 3, "Open", 7},
                {"Microsoft", "Software Engineer", "C++, DSA", 22.0, 3, "Open", null},
                {"Wipro", "Project Engineer", "Java, Testing", 3.8, -3, "Closed", null},
                {"Amazon", "SDE I", "Java, DSA, AWS", 26.0, 20, "Interview", 4},
                {"Zoho", "Member Technical Staff", "Java, JavaScript", 8.4, 8, "Open", null}};
        for (Object[] j : jb)
            x.jobs.add(new Job(x.nj++, (String) j[0], (String) j[1], (String) j[2], (Double) j[3], day((Integer) j[4]),
                    (String) j[5], j[6] == null ? null : day((Integer) j[6])));
        int[][] ap = {{1, 3}, {1, 6}, {2, 3}, {2, 4}, {3, 1}, {4, 7}, {4, 2}};
        String[] as = {"Applied", "Interview", "Applied", "Applied", "Shortlisted", "Applied", "Applied"};
        for (int i = 0; i < ap.length; i++) x.apps.add(new App(x.na++, ap[i][0], ap[i][1], TODAY, as[i]));
        x.wait.add(1); x.wait.add(4);
        x.hist.add(new Hist(day(-6), "Diya Patel", "Accenture", "Selected", "Strong DSA round"));
        x.hist.add(new Hist(day(-10), "Rohan Mehta", "Capgemini", "Rejected", "Practise system design"));
        x.reindex();
        return x;
    }

}
