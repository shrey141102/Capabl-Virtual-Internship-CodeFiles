import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class User {
    private static Map<String, User> users = new HashMap<>();

    private String username;
    private String password;

    public User(String username, String password) {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username format");
        }

        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Invalid password format");
        }

        this.username = username;
        this.password = password;
        users.put(username, this);
    }

    public static boolean isValidUsername(String username) {
        // Implement your username validation logic here.
        // For example, check for minimum length, allowed characters, etc.
        return username.matches("^[a-zA-Z0-9_-]{3,20}$");
    }

    public static boolean isValidPassword(String password) {
        // Implement your password validation logic here.
        // For example, check for minimum length, required characters, etc.
        return password.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$");
    }

    public static User getUserByUsername(String username) {
        return users.get(username);
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}


class Book {
    private String title;
    private String author;
    private double price;
    private int stock;

    public Book(String title, String author, double price, int stock) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}


class ShoppingCart {
    private List<Book> cartItems;

    public ShoppingCart() {
        this.cartItems = new ArrayList<>();
    }

    public void addBookToCart(Book book) {
        cartItems.add(book);
    }

    public void removeBookFromCart(Book book) {
        cartItems.remove(book);
    }

    public List<Book> getCartItems() {
        return cartItems;
    }
}


class InventoryManager {
    private Map<Book, Integer> bookStock;

    public InventoryManager() {
        this.bookStock = new HashMap<>();
    }

    public synchronized void addToInventory(Book book, int quantity) {
        int currentStock = bookStock.getOrDefault(book, 0);
        bookStock.put(book, currentStock + quantity);
    }

    public synchronized boolean removeFromInventory(Book book, int quantity) {
        int currentStock = bookStock.getOrDefault(book, 0);
        if (currentStock >= quantity) {
            bookStock.put(book, currentStock - quantity);
            return true;
        }
        return false;
    }

    public int getBookStock(Book book) {
        return bookStock.getOrDefault(book, 0);
    }

    public synchronized void displayInventory() {
        System.out.println("Current Inventory:");
        for (Map.Entry<Book, Integer> entry : bookStock.entrySet()) {
            Book book = entry.getKey();
            int stock = entry.getValue();
            System.out.println(book.getTitle() + " by " + book.getAuthor() + " - Stock: " + stock);
        }
        System.out.println();
    }
}

class Order {
    private static int orderCounter = 1;

    private int orderId;
    private User user;
    private List<Book> orderedBooks;

    public Order(User user, List<Book> orderedBooks) {
        this.orderId = orderCounter++;
        this.user = user;
        this.orderedBooks = orderedBooks;
    }

    public double calculateTotal() {
        double total = 0.0;
        for (Book book : orderedBooks) {
            total += book.getPrice();
        }
        return total;
    }
}

public class Main {
    public static void main(String[] args) {
        Book book1 = new Book("Java Programming", "John Doe", 25.99, 50);
        Book book2 = new Book("Python for Beginners", "Jane Smith", 19.99, 30);

        User user1 = new User("user1", "password1");
        User user2 = new User("user2", "password2");

        ShoppingCart cartUser1 = new ShoppingCart();
        ShoppingCart cartUser2 = new ShoppingCart();

        cartUser1.addBookToCart(book1);
        cartUser1.addBookToCart(book2);

        cartUser2.addBookToCart(book1);

        InventoryManager inventoryManager = new InventoryManager();
        inventoryManager.addToInventory(book1, 50);
        inventoryManager.addToInventory(book2, 30);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        // Check if the provided credentials are valid
        User user = User.getUserByUsername(username);
        if (user != null && user.checkPassword(password)) {
            System.out.println("Welcome, " + username + "!");
            // Proceed with the shopping cart and order processing here
            // ... (code related to shopping cart and order processing) ...
        } else {
            System.out.println("Invalid username or password. Please try again.");
            System.exit(0);
        }

        scanner.close();

        executorService.submit(() -> {
            Order order1 = new Order(user1, cartUser1.getCartItems());
            double total1 = order1.calculateTotal();

            synchronized (inventoryManager) {
                if (inventoryManager.removeFromInventory(book1, 1) && inventoryManager.removeFromInventory(book2, 1)) {
                    System.out.println("Order 1 Total: $" + total1);
                    inventoryManager.displayInventory();
                } else {
                    System.out.println("Order 1 failed. Not enough stock.");
                }
            }
        });

        executorService.submit(() -> {
            Order order2 = new Order(user2, cartUser2.getCartItems());
            double total2 = order2.calculateTotal();

            synchronized (inventoryManager) {
                if (inventoryManager.removeFromInventory(book1, 1)) {
                    System.out.println("Order 2 Total: $" + total2);
                    inventoryManager.displayInventory();
                } else {
                    System.out.println("Order 2 failed. Not enough stock.");
                }
            }
        });
        
        executorService.shutdown();
    }
}
    
