
# Placement Tracker (DSA in Java)

A desktop application written in **Java (Swing)** that helps manage a campus placement season. It keeps track of **jobs, students, applications and interviews**, and every major feature is built on a specific data structure or algorithm.

Developed as the individual project for the **Student Internship Program - Data Structure and Algorithms using Java**, offered by the Council for Skills and Competencies (CSC India) under the APSCHE Student Internship Initiative (04 May 2026 - 27 June 2026).

## Features

| Screen | What it offers | Data structure / algorithm |
| --- | --- | --- |
| Dashboard | Four summary cards, a priority queue of applications by nearest deadline, upcoming interviews | PriorityQueue (min-heap) |
| Jobs | Add, edit, delete, sort, search (linear or binary), clear | ArrayList, merge sort, linear search, binary search |
| Students | Add, edit, delete, text search, Find by ID | HashMap, ArrayList |
| Applications | Apply, set status, delete, days left to deadline, duplicate blocking | HashSet, ArrayList |
| Interviews | Waiting list (Join queue, Serve next); history (Log interview, Remove oldest) | Queue (LinkedList), LinkedList |
| DSA Learning Lab | Catalogue of every structure/algorithm used, with a live demo runner | All structures; recursion |
| Sidebar | Undo last change, Reset demo data | Stack (ArrayDeque) |

## Data structures and algorithms used

| Structure / Algorithm | Used for | Time complexity |
| --- | --- | --- |
| ArrayList | Job and student tables | get(i) O(1), add O(1) amortised, remove O(n) |
| HashMap | Find a record by ID | get / put / remove O(1) average |
| HashSet | Block duplicate applications ("studentId:jobId") | add / contains O(1) average |
| Stack (ArrayDeque) | Undo last change | push / pop O(1) |
| Queue (LinkedList) | Interview help waiting list | offer / poll / peek O(1) |
| LinkedList | Interview history (newest first) | addFirst / removeLast O(1), search O(n) |
| PriorityQueue (heap) | Nearest-deadline applications (ties: higher package, then lower ID) | offer / poll O(log n), peek O(1) |
| Merge sort | Sort jobs by package, deadline, company or title | O(n log n) time, O(n) space |
| Linear search | Free-text search | O(n) |
| Binary search | Exact company / title search | O(log n) after an O(n log n) sort |
| Recursion | Count jobs needing a skill | O(n) time, O(n) stack |

## Project structure

```
PlacementTracker.java   Part 1 - Swing screens and sidebar navigation
DataLayer.java          Part 2 - model classes, collections, undo stack, sorting/searching/recursion
DemoData.java           Part 3 - in-memory demonstration data (4 students, 7 jobs, 7 applications)
```

## How to run

**Requirements:** JDK 17 or newer.

```bash
javac *.java
java PlacementTracker
```

Or open the folder in VS Code (with the *Extension Pack for Java*), open `PlacementTracker.java` and click **Run** above `main`.

## Demonstration data

The app starts with fictional demonstration data: 4 students, 7 jobs, 7 applications, a 2-person waiting list and 2 interview history entries. The date is fixed to **2026-10-29** so the deadlines and "days left" values are always the same. To use the real current date, change the `TODAY` constant in `DemoData.java` to `LocalDate.now()`. The **Reset demo data** button restores the original records at any time.

## Limitations

- Single-user desktop program; no login and no database (all data is held in memory and is lost when the app closes).
- Works on demonstration data only; not validated on real placement data.
- Tested manually; no automated unit tests.
- Complexities in the Learning Lab are theoretical; no benchmarks were run.

## Author

**Boddupalli Sujan Priya**
Roll No: 23761A05D9
Computer Science and Engineering, Lakireddy Bali Reddy College of Engineering (A), JNTU Kakinada

Internship certificate ID: CSCIndia-HISXTAJK (verify at https://verify.cscindia.org.in/)
