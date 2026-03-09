import java.util.*;

public class Dashboard {

    private Register reg;
    private Complaint complaints;

    public Dashboard(Register reg, Complaint complaints) {
        this.reg = reg;
        this.complaints = complaints;
    }

    public void openDashboard(Scanner sc) {
        Register.User user = reg.getCurrentUser();

        if (user == null) {
            System.out.println("Please login first.");
            return;
        }

        String role = Register.normalizeRole(user.role);
        System.out.println("\n==== DASHBOARD (" + role.toUpperCase() + ") ====");

        if (role.equals("citizen")) {
            citizenDashboard(sc, user);
        } else if (role.equals("worker")) {
            workerDashboard(sc, user);
        } else if (role.equals("administrator")) {
            adminDashboard(sc, user);
        } else {
            System.out.println("Unknown role.");
        }
    }

    private void citizenDashboard(Scanner sc, Register.User user) {
        while (true) {
            System.out.println("\n1. View My Complaints");
            System.out.println("2. Back");
            System.out.print("Enter choice: ");
            int ch = Integer.parseInt(sc.nextLine());

            if (ch == 1) {
                ArrayList<Complaint.ComplaintNodeData> list = complaints.getComplaintsByUser(user.username);

                if (list.isEmpty()) {
                    System.out.println("No complaints filed.");
                    continue;
                }

                for (Complaint.ComplaintNodeData c : list) {
                    System.out.println(c.complaintId + " | " + c.category + " | " + c.location +
                            " | " + c.status + " | " + c.progress + "%");
                }
            } else if (ch == 2) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void workerDashboard(Scanner sc, Register.User user) {
        while (true) {
            System.out.println("\n1. View Next Assigned Work");
            System.out.println("2. Update Complaint");
            System.out.println("3. Back");
            System.out.print("Enter choice: ");
            int ch = Integer.parseInt(sc.nextLine());

            if (ch == 1) {
                Complaint.ComplaintNodeData c = complaints.getNextWorkItem();
                if (c == null) {
                    System.out.println("No pending complaints.");
                } else {
                    System.out.println("Next Work Item:");
                    System.out.println(c.complaintId + " | " + c.category + " | " + c.location +
                            " | " + c.description + " | urgent=" + c.urgent);
                }
            } else if (ch == 2) {
                updateComplaintFlow(sc, user);
            } else if (ch == 3) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void adminDashboard(Scanner sc, Register.User user) {
        while (true) {
            System.out.println("\n1. View All Complaints");
            System.out.println("2. Update Complaint");
            System.out.println("3. Analytics");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");
            int ch = Integer.parseInt(sc.nextLine());

            if (ch == 1) {
                ArrayList<Complaint.ComplaintNodeData> list = complaints.getAllComplaints();
                if (list.isEmpty()) {
                    System.out.println("No complaints found.");
                    continue;
                }

                for (Complaint.ComplaintNodeData c : list) {
                    System.out.println(c.complaintId + " | " + c.username + " | " + c.category +
                            " | " + c.status + " | " + c.progress + "% | urgent=" + c.urgent);
                }
            } else if (ch == 2) {
                updateComplaintFlow(sc, user);
            } else if (ch == 3) {
                showAnalytics();
            } else if (ch == 4) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void updateComplaintFlow(Scanner sc, Register.User user) {
        String role = Register.normalizeRole(user.role);
        if (!role.equals("worker") && !role.equals("administrator")) {
            System.out.println("Only admin and worker can update complaint status.");
            return;
        }

        System.out.print("Enter complaint ID: ");
        String id = sc.nextLine().trim();

        System.out.print("Enter new status (pending/in-progress/resolved): ");
        String st = sc.nextLine().trim();

        System.out.print("Enter new progress (0-100): ");
        int pr = Integer.parseInt(sc.nextLine());

        System.out.print("Enter update note: ");
        String nt = sc.nextLine().trim();

        complaints.updateComplaint(id, st, pr, nt);
    }

    private void showAnalytics() {
        ArrayList<Complaint.ComplaintNodeData> list = complaints.getAllComplaints();

        int p = 0, ip = 0, r = 0, u = 0;

        for (Complaint.ComplaintNodeData c : list) {
            if (c.status.equalsIgnoreCase("pending")) p++;
            else if (c.status.equalsIgnoreCase("in-progress")) ip++;
            else if (c.status.equalsIgnoreCase("resolved")) r++;
            if (c.urgent) u++;
        }

        System.out.println("\n=== ANALYTICS ===");
        System.out.println("Total complaints: " + list.size());
        System.out.println("Pending: " + p);
        System.out.println("In Progress: " + ip);
        System.out.println("Resolved: " + r);
        System.out.println("Urgent: " + u);
    }
}
