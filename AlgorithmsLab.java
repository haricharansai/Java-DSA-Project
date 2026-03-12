import java.util.*;

public class AlgorithmsLab {

    private static class SearchResult {
        Complaint.ComplaintNodeData item;
        int steps;

        SearchResult(Complaint.ComplaintNodeData item, int steps) {
            this.item = item;
            this.steps = steps;
        }
    }

    private static class CharStack {
        private char[] data;
        private int top;

        CharStack(int cap) {
            data = new char[Math.max(1, cap)];
            top = -1;
        }

        void push(char c) {
            if (top + 1 == data.length) {
                data = Arrays.copyOf(data, data.length * 2);
            }
            data[++top] = c;
        }

        char pop() {
            return data[top--];
        }

        char peek() {
            return data[top];
        }

        boolean isEmpty() {
            return top < 0;
        }
    }

    private static class IntCircularQueue {
        private int[] data;
        private int head;
        private int tail;
        private int size;

        IntCircularQueue(int cap) {
            data = new int[Math.max(1, cap)];
            head = 0;
            tail = 0;
            size = 0;
        }

        boolean isEmpty() {
            return size == 0;
        }

        boolean isFull() {
            return size == data.length;
        }

        void enqueue(int v) {
            if (isFull()) {
                grow();
            }
            data[tail] = v;
            tail = (tail + 1) % data.length;
            size++;
        }

        int dequeue() {
            if (isEmpty()) {
                throw new NoSuchElementException("Queue empty");
            }
            int v = data[head];
            head = (head + 1) % data.length;
            size--;
            return v;
        }

        private void grow() {
            int[] nd = new int[data.length * 2];
            for (int i = 0; i < size; i++) {
                nd[i] = data[(head + i) % data.length];
            }
            data = nd;
            head = 0;
            tail = size;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < size; i++) {
                if (i > 0) sb.append(", ");
                sb.append(data[(head + i) % data.length]);
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public static void run(Scanner sc, Complaint complaints) {
        while (true) {
            System.out.println("\n==== ALGORITHMS LAB (DSA DEMO) ====");
            System.out.println("1. Linear Search (Complaint ID)");
            System.out.println("2. Binary Search (Complaint ID)");
            System.out.println("3. Selection Sort (by Progress)");
            System.out.println("4. Merge Sort (by Complaint ID)");
            System.out.println("5. Stack Demo (Parentheses Check)");
            System.out.println("6. Queue Demo (Circular Queue)");
            System.out.println("7. Linked List Demo (Reverse + Cycle Check)");
            System.out.println("8. Hashing Demo (Delete Complaint by ID)");
            System.out.println("9. Back");
            int ch = readInt(sc, "Enter choice: ");

            if (ch == 1) linearSearchDemo(sc, complaints);
            else if (ch == 2) binarySearchDemo(sc, complaints);
            else if (ch == 3) selectionSortDemo(complaints);
            else if (ch == 4) mergeSortDemo(complaints);
            else if (ch == 5) stackDemo(sc);
            else if (ch == 6) queueDemo(sc);
            else if (ch == 7) linkedListDemo(complaints);
            else if (ch == 8) hashDeleteDemo(sc, complaints);
            else if (ch == 9) return;
            else System.out.println("Invalid choice.");
        }
    }

    private static void linearSearchDemo(Scanner sc, Complaint complaints) {
        Complaint.ComplaintNodeData[] arr = complaints.toArray();
        if (arr.length == 0) {
            System.out.println("No complaints available for search.");
            return;
        }

        String id = readNonEmpty(sc, "Enter complaint ID: ");
        SearchResult r = linearSearchById(arr, id);

        if (r.item == null) {
            System.out.println("Not found (linear search).");
        } else {
            System.out.println("Found: " + r.item.complaintId + " | " + r.item.category +
                    " | " + r.item.status + " | " + r.item.progress + "%");
        }
        System.out.println("Steps checked: " + r.steps);
        System.out.println("Time Complexity: O(n)");
    }

    private static void binarySearchDemo(Scanner sc, Complaint complaints) {
        Complaint.ComplaintNodeData[] arr = complaints.toArray();
        if (arr.length == 0) {
            System.out.println("No complaints available for search.");
            return;
        }

        String id = readNonEmpty(sc, "Enter complaint ID: ");
        Complaint.ComplaintNodeData[] sorted = Arrays.copyOf(arr, arr.length);
        mergeSortById(sorted);
        SearchResult r = binarySearchById(sorted, id);

        if (r.item == null) {
            System.out.println("Not found (binary search).");
        } else {
            System.out.println("Found: " + r.item.complaintId + " | " + r.item.category +
                    " | " + r.item.status + " | " + r.item.progress + "%");
        }
        System.out.println("Steps checked: " + r.steps);
        System.out.println("Time Complexity: O(log n) after sorting (merge sort O(n log n))");
    }

    private static void selectionSortDemo(Complaint complaints) {
        Complaint.ComplaintNodeData[] arr = complaints.toArray();
        if (arr.length == 0) {
            System.out.println("No complaints available for sorting.");
            return;
        }

        Complaint.ComplaintNodeData[] copy = Arrays.copyOf(arr, arr.length);
        System.out.println("Before (first 10): " + summarize(copy, 15));
        selectionSortByProgress(copy);
        System.out.println("After  (first 10): " + summarize(copy, 15));
        System.out.println("Time Complexity: O(n^2)");
    }

    private static void mergeSortDemo(Complaint complaints) {
        Complaint.ComplaintNodeData[] arr = complaints.toArray();
        if (arr.length == 0) {
            System.out.println("No complaints available for sorting.");
            return;
        }

        Complaint.ComplaintNodeData[] copy = Arrays.copyOf(arr, arr.length);
        System.out.println("Before (first 10): " + summarize(copy, 15));
        mergeSortById(copy);
        System.out.println("After  (first 10): " + summarize(copy, 15));
        System.out.println("Time Complexity: O(n log n)");
    }

    private static void stackDemo(Scanner sc) {
        String s = readNonEmpty(sc, "Enter expression with brackets: ");
        boolean ok = isParenthesesBalanced(s);
        System.out.println("Balanced: " + (ok ? "YES" : "NO"));
        System.out.println("Time Complexity: O(n)");
    }

    private static void queueDemo(Scanner sc) {
        int cap = readInt(sc, "Enter initial queue capacity: ");
        if (cap <= 0) cap = 5;
        IntCircularQueue q = new IntCircularQueue(cap);

        while (true) {
            System.out.println("\n-- Queue Demo (Circular Queue) --");
            System.out.println("1. Enqueue");
            System.out.println("2. Dequeue");
            System.out.println("3. View Queue");
            System.out.println("4. Back");
            int ch = readInt(sc, "Enter choice: ");

            if (ch == 1) {
                int v = readInt(sc, "Enter value to enqueue: ");
                q.enqueue(v);
                System.out.println("Enqueued: " + v);
            } else if (ch == 2) {
                if (q.isEmpty()) {
                    System.out.println("Queue is empty.");
                } else {
                    int v = q.dequeue();
                    System.out.println("Dequeued: " + v);
                }
            } else if (ch == 3) {
                System.out.println("Queue contents (front -> rear): " + q);
            } else if (ch == 4) {
                System.out.println("Time Complexity: O(1) per enqueue/dequeue");
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private static void linkedListDemo(Complaint complaints) {
        if (complaints.size() == 0) {
            System.out.println("No complaints available.");
            return;
        }

        System.out.println("Cycle detection: " + (complaints.hasCycle() ? "CYCLE FOUND" : "no cycle"));

        complaints.reverseInPlace();
        Complaint.ComplaintNodeData[] arr = complaints.toArray();
        System.out.println("After reverse (first 10): " + summarize(arr, 10));
        complaints.reverseInPlace();
    }

    private static void hashDeleteDemo(Scanner sc, Complaint complaints) {
        if (complaints.size() == 0) {
            System.out.println("No complaints available.");
            return;
        }
        System.out.println("This will delete a complaint from the system.");
        String id = readNonEmpty(sc, "Enter complaint ID to delete: ");
        boolean ok = complaints.deleteComplaintById(id);
        if (ok) {
            System.out.println("Deleted successfully (linked list + hash table updated).");
        } else {
            System.out.println("Complaint ID not found.");
        }
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.println("Invalid number.");
            }
        }
    }

    private static String readNonEmpty(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("Input cannot be empty.");
        }
    }

    private static SearchResult linearSearchById(Complaint.ComplaintNodeData[] arr, String id) {
        int steps = 0;
        for (Complaint.ComplaintNodeData c : arr) {
            steps++;
            if (c.complaintId.equalsIgnoreCase(id)) return new SearchResult(c, steps);
        }
        return new SearchResult(null, steps);
    }

    private static SearchResult binarySearchById(Complaint.ComplaintNodeData[] arr, String id) {
        int steps = 0;
        int l = 0, r = arr.length - 1;
        while (l <= r) {
            steps++;
            int m = l + (r - l) / 2;
            int cmp = arr[m].complaintId.compareToIgnoreCase(id);
            if (cmp == 0) return new SearchResult(arr[m], steps);
            if (cmp < 0) l = m + 1;
            else r = m - 1;
        }
        return new SearchResult(null, steps);
    }

    private static void selectionSortByProgress(Complaint.ComplaintNodeData[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j].progress < arr[min].progress) {
                    min = j;
                }
            }
            if (min != i) {
                Complaint.ComplaintNodeData t = arr[i];
                arr[i] = arr[min];
                arr[min] = t;
            }
        }
    }

    private static void mergeSortById(Complaint.ComplaintNodeData[] arr) {
        if (arr.length < 2) return;
        Complaint.ComplaintNodeData[] tmp = new Complaint.ComplaintNodeData[arr.length];
        mergeSortById(arr, tmp, 0, arr.length - 1);
    }

    private static void mergeSortById(Complaint.ComplaintNodeData[] arr,
                                      Complaint.ComplaintNodeData[] tmp, int l, int r) {
        if (l >= r) return;
        int m = l + (r - l) / 2;
        mergeSortById(arr, tmp, l, m);
        mergeSortById(arr, tmp, m + 1, r);
        mergeById(arr, tmp, l, m, r);
    }

    private static void mergeById(Complaint.ComplaintNodeData[] arr,
                                  Complaint.ComplaintNodeData[] tmp, int l, int m, int r) {
        int i = l, j = m + 1, k = l;
        while (i <= m && j <= r) {
            if (arr[i].complaintId.compareToIgnoreCase(arr[j].complaintId) <= 0) {
                tmp[k++] = arr[i++];
            } else {
                tmp[k++] = arr[j++];
            }
        }
        while (i <= m) tmp[k++] = arr[i++];
        while (j <= r) tmp[k++] = arr[j++];
        for (int x = l; x <= r; x++) arr[x] = tmp[x];
    }

    private static boolean isParenthesesBalanced(String s) {
        CharStack st = new CharStack(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '{' || c == '[') {
                st.push(c);
            } else if (c == ')' || c == '}' || c == ']') {
                if (st.isEmpty()) return false;
                char top = st.pop();
                if (!matches(top, c)) return false;
            }
        }
        return st.isEmpty();
    }

    private static boolean matches(char open, char close) {
        return (open == '(' && close == ')') ||
                (open == '{' && close == '}') ||
                (open == '[' && close == ']');
    }

    private static String summarize(Complaint.ComplaintNodeData[] arr, int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int n = Math.min(arr.length, limit);
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr[i].complaintId).append(":").append(arr[i].progress);
        }
        if (arr.length > limit) sb.append(", ...");
        sb.append("]");
        return sb.toString();
    }
}
