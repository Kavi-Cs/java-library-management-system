import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

// AI Suggestion: Use Java records for concise immutable data models.
record Book(String bookId, String title, String author, boolean available) {
    public Book withAvailability(boolean newAvailability) {
        return new Book(bookId, title, author, newAvailability);
    }

    public String display() {
        String status = available ? "Available" : "Borrowed";
        return String.format("%-6s %-28s %-22s %s", bookId, title, author, status);
    }
}

// AI Suggestion: Store member borrowing state as a record and replace it with updated copies.
record Member(String memberId, String name, List<String> borrowedBooks) {
    public Member {
        borrowedBooks = new ArrayList<>(borrowedBooks);
    }

    public Member withBorrowedBooks(List<String> updatedBorrowedBooks) {
        return new Member(memberId, name, updatedBorrowedBooks);
    }

    public String display() {
        String borrowed = borrowedBooks.isEmpty() ? "None" : String.join(", ", borrowedBooks);
        return String.format("%-6s %-24s Borrowed: %s", memberId, name, borrowed);
    }
}

// AI Suggestion: Use a custom exception to separate validation errors from menu logic.
class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}

class LibraryService {
    private static final Path DATA_FILE = Path.of("library_data.txt");
    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();

    public LibraryService() throws LibraryException {
        load();
    }

    public void addBook(String bookId, String title, String author) throws LibraryException {
        bookId = required(bookId, "Book ID").toUpperCase();
        title = required(title, "Title");
        author = required(author, "Author");

        if (books.containsKey(bookId)) {
            throw new LibraryException("Book ID " + bookId + " already exists.");
        }

        books.put(bookId, new Book(bookId, title, author, true));
        save();
    }

    public void addMember(String memberId, String name) throws LibraryException {
        memberId = required(memberId, "Member ID").toUpperCase();
        name = required(name, "Member name");

        if (members.containsKey(memberId)) {
            throw new LibraryException("Member ID " + memberId + " already exists.");
        }

        members.put(memberId, new Member(memberId, name, List.of()));
        save();
    }

    public void borrowBook(String memberId, String bookId) throws LibraryException {
        Member member = getMember(memberId);
        Book book = getBook(bookId);

        if (!book.available()) {
            throw new LibraryException(book.title() + " is already borrowed.");
        }
        if (member.borrowedBooks().contains(book.bookId())) {
            throw new LibraryException("The selected member already has this book.");
        }

        List<String> updatedBorrowedBooks = new ArrayList<>(member.borrowedBooks());
        updatedBorrowedBooks.add(book.bookId());
        members.put(member.memberId(), member.withBorrowedBooks(updatedBorrowedBooks));
        books.put(book.bookId(), book.withAvailability(false));
        save();
    }

    public void returnBook(String memberId, String bookId) throws LibraryException {
        Member member = getMember(memberId);
        Book book = getBook(bookId);

        if (!member.borrowedBooks().contains(book.bookId())) {
            throw new LibraryException("This member did not borrow the selected book.");
        }

        List<String> updatedBorrowedBooks = new ArrayList<>(member.borrowedBooks());
        updatedBorrowedBooks.remove(book.bookId());
        members.put(member.memberId(), member.withBorrowedBooks(updatedBorrowedBooks));
        books.put(book.bookId(), book.withAvailability(true));
        save();
    }

    // AI Suggestion: Use the Streams API to search by ID, title, or author.
    public List<Book> searchBooks(String keyword) throws LibraryException {
        String loweredKeyword = required(keyword, "Search keyword").toLowerCase();
        return books.values()
                .stream()
                .filter(book -> book.bookId().toLowerCase().contains(loweredKeyword)
                        || book.title().toLowerCase().contains(loweredKeyword)
                        || book.author().toLowerCase().contains(loweredKeyword))
                .sorted(Comparator.comparing(Book::bookId))
                .collect(Collectors.toList());
    }

    public List<Book> listBooks() {
        return books.values()
                .stream()
                .sorted(Comparator.comparing(Book::bookId))
                .collect(Collectors.toList());
    }

    public List<Member> listMembers() {
        return members.values()
                .stream()
                .sorted(Comparator.comparing(Member::memberId))
                .collect(Collectors.toList());
    }

    // AI Suggestion: Use simple standard File I/O persistence so the program remembers data between runs.
    private void save() throws LibraryException {
        List<String> lines = new ArrayList<>();

        for (Book book : books.values()) {
            lines.add(String.join("|",
                    "BOOK",
                    escape(book.bookId()),
                    escape(book.title()),
                    escape(book.author()),
                    Boolean.toString(book.available())));
        }

        for (Member member : members.values()) {
            lines.add(String.join("|",
                    "MEMBER",
                    escape(member.memberId()),
                    escape(member.name()),
                    escape(String.join(",", member.borrowedBooks()))));
        }

        try {
            Files.write(DATA_FILE, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException error) {
            throw new LibraryException("Could not save library data: " + error.getMessage());
        }
    }

    private void load() throws LibraryException {
        if (!Files.exists(DATA_FILE)) {
            seedData();
            save();
            return;
        }

        try {
            List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
            books.clear();
            members.clear();

            for (String line : lines) {
                String[] parts = splitEscaped(line);
                if (parts.length == 0 || parts[0].isBlank()) {
                    continue;
                }

                if (parts[0].equals("BOOK") && parts.length == 5) {
                    Book book = new Book(
                            unescape(parts[1]),
                            unescape(parts[2]),
                            unescape(parts[3]),
                            Boolean.parseBoolean(parts[4]));
                    books.put(book.bookId(), book);
                } else if (parts[0].equals("MEMBER") && parts.length == 4) {
                    String borrowedText = unescape(parts[3]);
                    List<String> borrowedBooks = borrowedText.isBlank()
                            ? List.of()
                            : List.of(borrowedText.split(","));
                    Member member = new Member(unescape(parts[1]), unescape(parts[2]), borrowedBooks);
                    members.put(member.memberId(), member);
                }
            }

            if (books.isEmpty() && members.isEmpty()) {
                seedData();
                save();
            }
        } catch (IOException error) {
            throw new LibraryException("Could not load library data: " + error.getMessage());
        }
    }

    private void seedData() {
        books.put("B001", new Book("B001", "Clean Code", "Robert C. Martin", true));
        books.put("B002", new Book("B002", "Python Crash Course", "Eric Matthes", true));
        books.put("B003", new Book("B003", "Design Patterns", "Gamma et al.", true));
        members.put("M001", new Member("M001", "Nimal Perera", List.of()));
        members.put("M002", new Member("M002", "Asha Silva", List.of()));
    }

    private Book getBook(String bookId) throws LibraryException {
        bookId = required(bookId, "Book ID").toUpperCase();
        Book book = books.get(bookId);
        if (book == null) {
            throw new LibraryException("Book ID " + bookId + " was not found.");
        }
        return book;
    }

    private Member getMember(String memberId) throws LibraryException {
        memberId = required(memberId, "Member ID").toUpperCase();
        Member member = members.get(memberId);
        if (member == null) {
            throw new LibraryException("Member ID " + memberId + " was not found.");
        }
        return member;
    }

    private String required(String value, String fieldName) throws LibraryException {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            throw new LibraryException(fieldName + " cannot be empty.");
        }
        return cleaned;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|");
    }

    private String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaping = false;

        for (char character : value.toCharArray()) {
            if (escaping) {
                result.append(character);
                escaping = false;
            } else if (character == '\\') {
                escaping = true;
            } else {
                result.append(character);
            }
        }

        if (escaping) {
            result.append('\\');
        }

        return result.toString();
    }

    private String[] splitEscaped(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaping = false;

        for (char character : line.toCharArray()) {
            if (escaping) {
                current.append('\\').append(character);
                escaping = false;
            } else if (character == '\\') {
                escaping = true;
            } else if (character == '|') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        if (escaping) {
            current.append('\\');
        }

        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }
}

public class LibrarySystemAI {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            LibraryService service = new LibraryService();
            runMenu(service);
        } catch (LibraryException error) {
            System.out.println("Startup error: " + error.getMessage());
        }
    }

    private static void runMenu(LibraryService service) {
        while (true) {
            System.out.println("\nLibrary Management System - Version B");
            System.out.println("1. Add book");
            System.out.println("2. Add member");
            System.out.println("3. Borrow book");
            System.out.println("4. Return book");
            System.out.println("5. Search books");
            System.out.println("6. List books");
            System.out.println("7. List members");
            System.out.println("0. Exit");
            System.out.print("Select option: ");

            String choice = SCANNER.nextLine().trim();

            try {
                if (choice.equals("1")) {
                    service.addBook(prompt("Book ID: "), prompt("Title: "), prompt("Author: "));
                    System.out.println("Book added and saved.");
                } else if (choice.equals("2")) {
                    service.addMember(prompt("Member ID: "), prompt("Name: "));
                    System.out.println("Member added and saved.");
                } else if (choice.equals("3")) {
                    service.borrowBook(prompt("Member ID: "), prompt("Book ID: "));
                    System.out.println("Book borrowed and saved.");
                } else if (choice.equals("4")) {
                    service.returnBook(prompt("Member ID: "), prompt("Book ID: "));
                    System.out.println("Book returned and saved.");
                } else if (choice.equals("5")) {
                    printBooks(service.searchBooks(prompt("Keyword: ")));
                } else if (choice.equals("6")) {
                    printBooks(service.listBooks());
                } else if (choice.equals("7")) {
                    printMembers(service.listMembers());
                } else if (choice.equals("0")) {
                    System.out.println("Exiting system.");
                    break;
                } else {
                    System.out.println("Invalid option. Please select a listed number.");
                }
            } catch (LibraryException error) {
                System.out.println("Error: " + error.getMessage());
            }
        }
    }

    private static String prompt(String message) {
        System.out.print(message);
        return SCANNER.nextLine().trim();
    }

    private static void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No matching books found.");
            return;
        }
        System.out.printf("%-6s %-28s %-22s %s%n", "ID", "Title", "Author", "Status");
        System.out.println("-".repeat(72));
        for (Book book : books) {
            System.out.println(book.display());
        }
    }

    private static void printMembers(List<Member> members) {
        if (members.isEmpty()) {
            System.out.println("No members registered.");
            return;
        }
        System.out.printf("%-6s %-24s %s%n", "ID", "Name", "Borrowing Details");
        System.out.println("-".repeat(72));
        for (Member member : members) {
            System.out.println(member.display());
        }
    }
}
