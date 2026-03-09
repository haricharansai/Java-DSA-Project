import java.util.*;

public class Register {

    public static String normalizeRole(String roleInput) {
        if (roleInput == null) return "";
        String role = roleInput.trim().toLowerCase();
        if (role.equals("a")) return "administrator";
        if (role.equals("c")) return "citizen";
        if (role.equals("w")) return "worker";
        if (role.equals("admin") || role.equals("administrator") || role.equals("adminstrator")) {
            return "administrator";
        }
        if (role.equals("citizen") || role.equals("worker")) {
            return role;
        }
        return role;
    }

    static class User {
        String username;
        String email;
        String password;
        String role;

        User(String username, String email, String password, String role) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.role = normalizeRole(role);
        }

        @Override
        public String toString() {
            return username + " | " + email + " | " + role;
        }
    }

    private LinkedList<User>[] tab;
    private int sz;
    private int cnt;

    private User currentUser;

    @SuppressWarnings("unchecked")
    public Register() {
        sz = 11;
        tab = new LinkedList[sz];
        for (int i = 0; i < sz; i++) {
            tab[i] = new LinkedList<>();
        }
        cnt = 0;
    }

    private int hf(String s) {
        long v = 0;
        for (int i = 0; i < s.length(); i++) {
            v = (v * 31 + s.charAt(i)) % sz;
        }
        return (int) v;
    }

    private void rehash() {
        LinkedList<User>[] old = tab;
        sz = sz * 2 + 1;

        @SuppressWarnings("unchecked")
        LinkedList<User>[] nt = new LinkedList[sz];
        for (int i = 0; i < sz; i++) nt[i] = new LinkedList<>();

        tab = nt;
        cnt = 0;

        for (LinkedList<User> bucket : old) {
            for (User u : bucket) {
                putUser(u);
            }
        }
    }

    private void putUser(User u) {
        if ((double) cnt / sz > 0.75) {
            rehash();
        }
        int id = hf(u.username);
        tab[id].add(u);
        cnt++;
    }

    public boolean existsUser(String username) {
        int id = hf(username);
        for (User u : tab[id]) {
            if (u.username.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public User getUser(String username) {
        int id = hf(username);
        for (User u : tab[id]) {
            if (u.username.equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    public void registerCitizen(Scanner sc) {
        System.out.print("Enter username: ");
        String un = sc.nextLine().trim();

        if (existsUser(un)) {
            System.out.println("Username already exists.");
            return;
        }

        System.out.print("Enter email: ");
        String em = sc.nextLine().trim();

        System.out.print("Enter password: ");
        String pw = sc.nextLine().trim();

        User u = new User(un, em, pw, "citizen");
        putUser(u);

        System.out.println("Citizen registered successfully.");
    }

    public void login(Scanner sc) {
        System.out.print("Enter username: ");
        String un = sc.nextLine().trim();

        System.out.print("Enter password: ");
        String pw = sc.nextLine().trim();

        System.out.print("Enter role (C=Citizen / W=Worker / A=Administrator): ");
        String rl = normalizeRole(sc.nextLine());

        User u = getUser(un);

        if (u == null) {
            System.out.println("User not found.");
            return;
        }

        if (!u.password.equals(pw) || !u.role.equals(rl)) {
            System.out.println("Invalid credentials or role.");
            return;
        }

        currentUser = u;
        System.out.println("Login successful. Welcome " + u.username + " (" + u.role + ")");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
        System.out.println("Logged out successfully.");
    }

    public void addUserDirect(User u) {
        if (!existsUser(u.username)) {
            putUser(u);
        }
    }
}
