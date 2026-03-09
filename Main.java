import java.util.*;
/*Linked List → store complaints

Hashing → quick complaint lookup by complaint ID and username check

Queue → pending complaint/work queue

Priority Queue / Heap → urgent complaints first

Linear Search → complaints by user / role-based filtering

Merge Sort → sort complaints by date or priority when needed*/

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Register reg = new Register();
        Complaint complaints = new Complaint(reg);
        Dashboard dash = new Dashboard(reg, complaints);
        Status status = new Status(complaints);

        addDemoUsers(reg);
        addDemoComplaints(complaints);

        while (true) {
            Register.User currentUser = reg.getCurrentUser();

            if (currentUser == null) {
                System.out.println("\n==== GROWCLEAN PUBLIC SERVICE COMPLAINT SYSTEM ====");
                System.out.println("1. Register Citizen");
                System.out.println("2. Login");
                System.out.println("3. Track Complaint Status");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");

                int ch = readChoice(sc);
                if (ch == -1) continue;

                switch (ch) {
                    case 1:
                        reg.registerCitizen(sc);
                        break;
                    case 2:
                        reg.login(sc);
                        break;
                    case 3:
                        status.trackComplaint(sc);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
                continue;
            }

            String role = Register.normalizeRole(currentUser.role);

            if (role.equals("citizen")) {
                System.out.println("\n==== CITIZEN MENU ====");
                System.out.println("1. File Complaint");
                System.out.println("2. Dashboard");
                System.out.println("3. Track Complaint Status");
                System.out.println("4. Logout");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");

                int ch = readChoice(sc);
                if (ch == -1) continue;

                switch (ch) {
                    case 1:
                        complaints.fileComplaint(sc);
                        break;
                    case 2:
                        dash.openDashboard(sc);
                        break;
                    case 3:
                        status.trackComplaint(sc);
                        break;
                    case 4:
                        reg.logout();
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } else if (role.equals("worker")) {
                System.out.println("\n==== WORKER MENU ====");
                System.out.println("1. Worker Dashboard");
                System.out.println("2. Track Complaint Status");
                System.out.println("3. Logout");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");

                int ch = readChoice(sc);
                if (ch == -1) continue;

                switch (ch) {
                    case 1:
                        dash.openDashboard(sc);
                        break;
                    case 2:
                        status.trackComplaint(sc);
                        break;
                    case 3:
                        reg.logout();
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } else if (role.equals("administrator")) {
                System.out.println("\n==== ADMINISTRATOR MENU ====");
                System.out.println("1. Administrator Dashboard");
                System.out.println("2. Track Complaint Status");
                System.out.println("3. Logout");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");

                int ch = readChoice(sc);
                if (ch == -1) continue;

                switch (ch) {
                    case 1:
                        dash.openDashboard(sc);
                        break;
                    case 2:
                        status.trackComplaint(sc);
                        break;
                    case 3:
                        reg.logout();
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } else {
                System.out.println("Unknown role. Logging out for safety.");
                reg.logout();
            }
        }
    }

    private static int readChoice(Scanner sc) {
        try {
            return Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid input.");
            return -1;
        }
    }

    private static void addDemoUsers(Register reg) {
        reg.addUserDirect(new Register.User("sriram", "sriram@gmail.com", "1234", "citizen"));
        reg.addUserDirect(new Register.User("worker1", "worker1@gmail.com", "1234", "worker"));
        reg.addUserDirect(new Register.User("admin1", "admin1@gmail.com", "1234", "administrator"));
    }

    private static void addDemoComplaints(Complaint complaints) {
        complaints.addComplaintDirect(new Complaint.ComplaintNodeData(
                "GC-2026-001", "drainage", "KPHB", "Drain water overflowing on road",
                "9876543210", "sriram", "pending", 10, false
        ));

        complaints.addComplaintDirect(new Complaint.ComplaintNodeData(
                "GC-2026-002", "garbage", "Miyapur", "Garbage not collected for 3 days",
                "9876543211", "sriram", "in-progress", 50, true
        ));
    }
}
