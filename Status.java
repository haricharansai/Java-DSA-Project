import java.util.*;

public class Status {

    private Complaint complaints;

    public Status(Complaint complaints) {
        this.complaints = complaints;
    }

    public void trackComplaint(Scanner sc) {
        System.out.print("Enter complaint ID: ");
        String id = sc.nextLine().trim();

        Complaint.ComplaintNodeData c = complaints.getById(id);

        if (c == null) {
            System.out.println("No complaint found with that ID.");
            return;
        }

        System.out.println("\n==== COMPLAINT STATUS ====");
        System.out.println("Complaint ID   : " + c.complaintId);
        System.out.println("Category       : " + c.category);
        System.out.println("Location       : " + c.location);
        System.out.println("Description    : " + c.description);
        System.out.println("Phone          : " + c.phone);
        System.out.println("Filed By       : " + c.username);
        System.out.println("Current Status : " + c.status);
        System.out.println("Progress       : " + c.progress + "%");
        System.out.println("Urgent         : " + (c.urgent ? "Yes" : "No"));

        System.out.println("\nStatus Updates:");
        for (int i = 0; i < c.updates.size(); i++) {
            System.out.println((i + 1) + ". " + c.updates.get(i));
        }
    }
}