import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.IOException;

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
        saveToFile();

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

    public int size() {
        return cnt;
    }

    public void saveToFile() {
        Path dir = Paths.get("data");
        Path file = dir.resolve("users.txt");
        try {
            Files.createDirectories(dir);
            List<String> lines = new ArrayList<>();
            for (LinkedList<User> bucket : tab) {
                for (User u : bucket) {
                    lines.add(escape(u.username) + "|" + escape(u.email) + "|" + escape(u.password) + "|" + escape(u.role));
                }
            }
            Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to save users: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        Path file = Paths.get("data").resolve("users.txt");
        if (!Files.exists(file)) return;
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                List<String> parts = splitEscaped(line, '|');
                if (parts.size() < 4) continue;
                User u = new User(unescape(parts.get(0)), unescape(parts.get(1)),
                        unescape(parts.get(2)), unescape(parts.get(3)));
                addUserDirect(u);
            }
        } catch (IOException e) {
            System.out.println("Failed to load users: " + e.getMessage());
        }
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
}
