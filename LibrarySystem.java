import java.util.ArrayList;
import java.util.Scanner;

class Book {
    private String bookId;
    private String title;
    private String author;
    private boolean available;

    public Book(String bookId, String title, String author) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.available = true;
    }

    public String getBookId() {
        return bookId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String toString() {
        String status = available ? "Available" : "Borrowed";
        return bookId + " | " + title + " | " + author + " | " + status;
    }
}

class Member {
    private String memberId;
    private String name;
    private ArrayList<String> borrowedBooks;

    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
        this.borrowedBooks = new ArrayList<>();
    }

    public String getMemberId() {
        return memberId;
    }

    public ArrayList<String> getBorrowedBooks() {
        return borrowedBooks;
    }

    public String toString() {
        String borrowed = borrowedBooks.isEmpty() ? "None" : String.join(", ", borrowedBooks);
        return memberId + " | " + name + " | Borrowed: " + borrowed;
    }
}

class Library {
    private ArrayList<Book> books;
    private ArrayList<Member> members;

    public Library() {
        books = new ArrayList<>();
        members = new ArrayList<>();
    }

    public String addBook(String bookId, String title, String author) {
        if (findBook(bookId) != null) {
            return "Book ID already exists.";
        }
        books.add(new Book(bookId, title, author));
        return "Book added successfully.";
    }

    public String addMember(String memberId, String name) {
        if (findMember(memberId) != null) {
            return "Member ID already exists.";
        }
        members.add(new Member(memberId, name));
        return "Member added successfully.";
    }

    public String borrowBook(String memberId, String bookId) {
        Member member = findMember(memberId);
        Book book = findBook(bookId);

        if (member == null) {
            return "Member not found.";
        }
        if (book == null) {
            return "Book not found.";
        }
        if (!book.isAvailable()) {
            return "Book is already borrowed.";
        }

        book.setAvailable(false);
        member.getBorrowedBooks().add(bookId);
        return "Book borrowed successfully.";
    }

    public String returnBook(String memberId, String bookId) {
        Member member = findMember(memberId);
        Book book = findBook(bookId);

        if (member == null) {
            return "Member not found.";
        }
        if (book == null) {
            return "Book not found.";
        }
        if (!member.getBorrowedBooks().contains(bookId)) {
            return "This member did not borrow the selected book.";
        }

        member.getBorrowedBooks().remove(bookId);
        book.setAvailable(true);
        return "Book returned successfully.";
    }

    public void listBooks() {
        if (books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }
        for (Book book : books) {
            System.out.println(book);
        }
    }

    public void listMembers() {
        if (members.isEmpty()) {
            System.out.println("No members registered.");
            return;
        }
        for (Member member : members) {
            System.out.println(member);
        }
    }

    private Book findBook(String bookId) {
        for (Book book : books) {
            if (book.getBookId().equalsIgnoreCase(bookId)) {
                return book;
            }
        }
        return null;
    }

    private Member findMember(String memberId) {
        for (Member member : members) {
            if (member.getMemberId().equalsIgnoreCase(memberId)) {
                return member;
            }
        }
        return null;
    }
}

public class LibrarySystem {
    private static void seedLibrary(Library library) {
        library.addBook("B001", "Clean Code", "Robert C. Martin");
        library.addBook("B002", "Python Crash Course", "Eric Matthes");
        library.addMember("M001", "Nimal Perera");
        library.addMember("M002", "Asha Silva");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Library library = new Library();
        seedLibrary(library);

        while (true) {
            System.out.println("\nLibrary Management System - Version A");
            System.out.println("1. Add book");
            System.out.println("2. Add member");
            System.out.println("3. Borrow book");
            System.out.println("4. Return book");
            System.out.println("5. List books");
            System.out.println("6. List members");
            System.out.println("0. Exit");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Book ID: ");
                String bookId = scanner.nextLine().trim();
                System.out.print("Title: ");
                String title = scanner.nextLine().trim();
                System.out.print("Author: ");
                String author = scanner.nextLine().trim();
                System.out.println(library.addBook(bookId, title, author));
            } else if (choice.equals("2")) {
                System.out.print("Member ID: ");
                String memberId = scanner.nextLine().trim();
                System.out.print("Name: ");
                String name = scanner.nextLine().trim();
                System.out.println(library.addMember(memberId, name));
            } else if (choice.equals("3")) {
                System.out.print("Member ID: ");
                String memberId = scanner.nextLine().trim();
                System.out.print("Book ID: ");
                String bookId = scanner.nextLine().trim();
                System.out.println(library.borrowBook(memberId, bookId));
            } else if (choice.equals("4")) {
                System.out.print("Member ID: ");
                String memberId = scanner.nextLine().trim();
                System.out.print("Book ID: ");
                String bookId = scanner.nextLine().trim();
                System.out.println(library.returnBook(memberId, bookId));
            } else if (choice.equals("5")) {
                library.listBooks();
            } else if (choice.equals("6")) {
                library.listMembers();
            } else if (choice.equals("0")) {
                System.out.println("Exiting system.");
                break;
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }
}
