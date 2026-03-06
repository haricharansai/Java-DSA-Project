import java.util.*;

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

        System.out.println("Complaint updated successfully.");
    }
}
