import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.IOException;

public class Complaint {

    static class ComplaintNodeData {
        String complaintId;
        String category;
        String location;
        String description;
        String phone;
        String username;
        String status;
        int progress;
        boolean urgent;
        ArrayList<String> updates;

        ComplaintNodeData(String complaintId, String category, String location, String description,
                          String phone, String username, String status, int progress, boolean urgent) {
            this.complaintId = complaintId;
            this.category = category;
            this.location = location;
            this.description = description;
            this.phone = phone;
            this.username = username;
            this.status = status;
            this.progress = progress;
            this.urgent = urgent;
            this.updates = new ArrayList<>();
            this.updates.add("Complaint filed.");
        }
    }

    static class Node {
        ComplaintNodeData data;
        Node next;

        Node(ComplaintNodeData data) {
            this.data = data;
        }
    }

    private Node head;
    private int idCounter = 3;

    private LinkedList<ComplaintNodeData>[] tab;
    private int sz;
    private int cnt;

    private Queue<ComplaintNodeData> pendingQueue;
    private PriorityQueue<ComplaintNodeData> urgentQueue;

    private Register reg;

    @SuppressWarnings("unchecked")
    public Complaint(Register reg) {
        this.reg = reg;
        sz = 11;
        tab = new LinkedList[sz];
        for (int i = 0; i < sz; i++) tab[i] = new LinkedList<>();
        cnt = 0;

        pendingQueue = new LinkedList<>();
        urgentQueue = new PriorityQueue<>(new Comparator<ComplaintNodeData>() {
            public int compare(ComplaintNodeData a, ComplaintNodeData b) {
                return a.complaintId.compareTo(b.complaintId);
            }
        });
    }

    private int hf(String s) {
        long v = 0;
        for (int i = 0; i < s.length(); i++) {
            v = (v * 31 + s.charAt(i)) % sz;
        }
        return (int) v;
    }

    private void rehash() {
        LinkedList<ComplaintNodeData>[] old = tab;
        sz = sz * 2 + 1;

        @SuppressWarnings("unchecked")
        LinkedList<ComplaintNodeData>[] nt = new LinkedList[sz];
        for (int i = 0; i < sz; i++) nt[i] = new LinkedList<>();

        tab = nt;
        cnt = 0;

        for (LinkedList<ComplaintNodeData> bucket : old) {
            for (ComplaintNodeData c : bucket) putHash(c);
        }
    }

    private void putHash(ComplaintNodeData c) {
        if ((double) cnt / sz > 0.75) rehash();
        int id = hf(c.complaintId);
        tab[id].add(c);
        cnt++;
    }

    public ComplaintNodeData getById(String complaintId) {
        int id = hf(complaintId);
        for (ComplaintNodeData c : tab[id]) {
            if (c.complaintId.equalsIgnoreCase(complaintId)) return c;
        }
        return null;
    }

    public void fileComplaint(Scanner sc) {
        Register.User user = reg.getCurrentUser();
        if (user == null) {
            System.out.println("Please login first.");
            return;
        }

        if (!user.role.equals("citizen")) {
            System.out.println("Only citizens can file complaints.");
            return;
        }

        System.out.print("Enter category: ");
        String cat = sc.nextLine().trim().toLowerCase();

        System.out.print("Enter location: ");
        String loc = sc.nextLine().trim();

        System.out.print("Enter description: ");
        String des = sc.nextLine().trim();

        System.out.print("Enter phone number: ");
        String ph = sc.nextLine().trim();

        System.out.print("Is it urgent? (yes/no): ");
        String x = sc.nextLine().trim().toLowerCase();
        boolean urgent = x.equals("yes") || x.equals("y");

        String id = String.format("GC-2026-%03d", idCounter++);

        ComplaintNodeData c = new ComplaintNodeData(
                id, cat, loc, des, ph, user.username, "pending", 0, urgent
        );

        addComplaintDirect(c);
        saveToFile();

        System.out.println("Complaint filed successfully.");
        System.out.println("Your Complaint ID: " + id);
    }

    public void addComplaintDirect(ComplaintNodeData c) {
        Node nn = new Node(c);
        if (head == null) head = nn;
        else {
            Node t = head;
            while (t.next != null) t = t.next;
            t.next = nn;
        }

        putHash(c);

        if (c.urgent) urgentQueue.offer(c);
        else pendingQueue.offer(c);
    }

    public ComplaintNodeData linearSearchById(String complaintId) {
        Node t = head;
        while (t != null) {
            if (t.data.complaintId.equalsIgnoreCase(complaintId)) {
                return t.data;
            }
            t = t.next;
        }
        return null;
    }

    public boolean deleteComplaintById(String complaintId) {
        Node prev = null;
        Node cur = head;
        while (cur != null) {
            if (cur.data.complaintId.equalsIgnoreCase(complaintId)) {
                break;
            }
            prev = cur;
            cur = cur.next;
        }
        if (cur == null) {
            return false;
        }

        if (prev == null) head = cur.next;
        else prev.next = cur.next;

        removeFromHash(complaintId);
        rebuildQueues();
        saveToFile();
        return true;
    }

    public ArrayList<ComplaintNodeData> getComplaintsByUser(String username) {
        ArrayList<ComplaintNodeData> ans = new ArrayList<>();
        Node t = head;

        while (t != null) {
            if (t.data.username.equalsIgnoreCase(username)) {
                ans.add(t.data);
            }
            t = t.next;
        }
        return ans;
    }

    public ArrayList<ComplaintNodeData> getAllComplaints() {
        ArrayList<ComplaintNodeData> ans = new ArrayList<>();
        Node t = head;
        while (t != null) {
            ans.add(t.data);
            t = t.next;
        }
        return ans;
    }

    public int size() {
        int n = 0;
        Node t = head;
        while (t != null) {
            n++;
            t = t.next;
        }
        return n;
    }

    public ComplaintNodeData[] toArray() {
        ComplaintNodeData[] arr = new ComplaintNodeData[size()];
        int i = 0;
        Node t = head;
        while (t != null) {
            arr[i++] = t.data;
            t = t.next;
        }
        return arr;
    }

    public ComplaintNodeData getNextWorkItem() {
        while (!urgentQueue.isEmpty()) {
            ComplaintNodeData c = urgentQueue.poll();
            if (!c.status.equalsIgnoreCase("resolved")) return c;
        }

        while (!pendingQueue.isEmpty()) {
            ComplaintNodeData c = pendingQueue.poll();
            if (!c.status.equalsIgnoreCase("resolved")) return c;
        }

        return null;
    }

    public void updateComplaint(String id, String newStatus, int newProgress, String note) {
        ComplaintNodeData c = getById(id);
        if (c == null) {
            System.out.println("Complaint not found.");
            return;
        }

        c.status = newStatus.toLowerCase();
        c.progress = newProgress;
        c.updates.add(note);
        saveToFile();

        System.out.println("Complaint updated successfully.");
    }

    public void reverseInPlace() {
        Node prev = null;
        Node cur = head;
        while (cur != null) {
            Node next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
        }
        head = prev;
    }

    public boolean hasCycle() {
        Node slow = head;
        Node fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) return true;
        }
        return false;
    }

    private boolean removeFromHash(String complaintId) {
        int id = hf(complaintId);
        Iterator<ComplaintNodeData> it = tab[id].iterator();
        while (it.hasNext()) {
            ComplaintNodeData c = it.next();
            if (c.complaintId.equalsIgnoreCase(complaintId)) {
                it.remove();
                cnt--;
                return true;
            }
        }
        return false;
    }

    private void rebuildQueues() {
        pendingQueue.clear();
        urgentQueue.clear();
        Node t = head;
        while (t != null) {
            if (!t.data.status.equalsIgnoreCase("resolved")) {
                if (t.data.urgent) urgentQueue.offer(t.data);
                else pendingQueue.offer(t.data);
            }
            t = t.next;
        }
    }

    public void saveToFile() {
        Path dir = Paths.get("data");
        Path file = dir.resolve("complaints.txt");
        try {
            Files.createDirectories(dir);
            List<String> lines = new ArrayList<>();
            Node t = head;
            while (t != null) {
                ComplaintNodeData c = t.data;
                String updatesJoined = joinEscaped(c.updates, ';');
                String line = escape(c.complaintId) + "|" + escape(c.category) + "|" + escape(c.location) + "|" +
                        escape(c.description) + "|" + escape(c.phone) + "|" + escape(c.username) + "|" +
                        escape(c.status) + "|" + c.progress + "|" + c.urgent + "|" + updatesJoined;
                lines.add(line);
                t = t.next;
            }
            Files.write(file, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to save complaints: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        Path file = Paths.get("data").resolve("complaints.txt");
        if (!Files.exists(file)) return;
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            int maxId = 0;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                List<String> parts = splitEscaped(line, '|');
                if (parts.size() < 10) continue;
                String id = unescape(parts.get(0));
                String cat = unescape(parts.get(1));
                String loc = unescape(parts.get(2));
                String des = unescape(parts.get(3));
                String ph = unescape(parts.get(4));
                String user = unescape(parts.get(5));
                String st = unescape(parts.get(6));
                int pr = parseIntSafe(parts.get(7), 0);
                boolean urgent = Boolean.parseBoolean(parts.get(8));
                List<String> updates = splitUpdates(parts.get(9));

                ComplaintNodeData c = new ComplaintNodeData(id, cat, loc, des, ph, user, st, pr, urgent);
                c.updates.clear();
                c.updates.addAll(updates);
                addComplaintDirect(c);

                int seq = extractSeq(id);
                if (seq > maxId) maxId = seq;
            }
            if (maxId >= idCounter) idCounter = maxId + 1;
        } catch (IOException e) {
            System.out.println("Failed to load complaints: " + e.getMessage());
        }
    }

    private static int parseIntSafe(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static int extractSeq(String id) {
        int idx = id.lastIndexOf('-');
        if (idx < 0 || idx + 1 >= id.length()) return 0;
        return parseIntSafe(id.substring(idx + 1), 0);
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '|' || c == ';' || c == '\n' || c == '\r') sb.append('\\');
            if (c == '\n') sb.append('n');
            else if (c == '\r') sb.append('r');
            else sb.append(c);
        }
        return sb.toString();
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                if (c == 'n') sb.append('\n');
                else if (c == 'r') sb.append('\r');
                else sb.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else {
                sb.append(c);
            }
        }
        if (esc) sb.append('\\');
        return sb.toString();
    }

    private static List<String> splitEscaped(String s, char delim) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                cur.append('\\').append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == delim) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (esc) cur.append('\\');
        out.add(cur.toString());
        return out;
    }

    private static String joinEscaped(List<String> items, char delim) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(delim);
            sb.append(escape(items.get(i)));
        }
        return sb.toString();
    }

    private static List<String> splitUpdates(String s) {
        List<String> raw = splitEscaped(s, ';');
        List<String> out = new ArrayList<>();
        for (String r : raw) out.add(unescape(r));
        return out;
    }
}
