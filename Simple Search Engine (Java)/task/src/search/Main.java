package search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static Scanner sc = new Scanner(System.in);
    public static List<String> data;
    public static Map<String, List<Integer>> invertedIndex;

    /**
     * Finds the filename from the command line arguments.
     * If --data is not provided, it returns a default filename "names.txt".
     *
     * @param args command line arguments
     * @return the filename to be used for data
     */
    public static String findFilename(String[] args) {
        String filename = "";

        for (int i = 0; i < args.length; i++) {
            if ("--data".equals(args[i])) {
                if (i + 1 < args.length) {
                    filename = args[i + 1];
                    i++;
                } else {
                    System.out.println("Error: --data requires a filename.");
                    return "names.txt"; // Default filename if not provided
                }
            }
        }

        return filename;
    }

    /**
     * Initializes the search engine by reading data from a file and creating an inverted index.
     *
     * @param filename the name of the file containing the data
     * @throws IOException if an error occurs while reading the file
     */
    public static void initialize(String filename) throws IOException {
        data = Files.readAllLines(Paths.get(filename));
        invertedIndex = createdInvertedIndex();
    }

    /**
     * Creates an inverted index from the data.
     *
     * @return a map where keys are words and values are lists of indexes of records containing those words
     */
    public static Map<String, List<Integer>> createdInvertedIndex() {
        var invertedIndex = new HashMap<String, List<Integer>>();

        for (int i = 0; i < data.size(); i++) {
            var split = data.get(i).toLowerCase().split("\\s+");

            int finalI = i;
            Arrays.stream(split).forEach(splitElement -> {
                var indexes = invertedIndex.computeIfAbsent(splitElement, x -> new ArrayList<>());
                indexes.add(finalI);
            });
        }

        return invertedIndex;
    }

    /**
     * Searches for records that match the given query using the "ALL" strategy.
     *
     * @param input the search query
     * @return a set of indexes of records that match all words in the query
     */
    public static HashSet<Integer> findWithAll(String input) {
        var splitInput = input.split("\\s+");

        if(splitInput.length == 0) {
            return new HashSet<>();
        }

        var result = new HashSet<Integer>();

        if(invertedIndex.containsKey(splitInput[0])) {
            result.addAll(invertedIndex.get(splitInput[0]));
        }

        for (int i = 1; i < splitInput.length; i++) {
            if(invertedIndex.containsKey(splitInput[i])) {
                result.retainAll(invertedIndex.get(splitInput[i]));
            }
        }

        return  result;
    }

    /**
     * Searches for records that match the given query using the "ANY" strategy.
     *
     * @param input the search query
     * @return a set of indexes of records that match any word in the query
     */
    public static HashSet<Integer> findWithAny(String input) {
        var result = new HashSet<Integer>();

        var splitInput = input.split("\\s+");

        Arrays.stream(splitInput).forEach(inputItem -> {
            result.addAll(invertedIndex.get(inputItem));
        });

        return  result;
    }

    /**
     * Searches for records that do not match any words in the given query using the "NONE" strategy.
     *
     * @param input the search query
     * @return a set of indexes of records that do not contain any words from the query
     */
    public static HashSet<Integer> findWithNone(String input) {
        HashSet<Integer> allRecordIndices = new HashSet<>();
        for (int i = 0; i < data.size(); i++) {
            allRecordIndices.add(i);
        }

        if (data.isEmpty()) {
            return new HashSet<>();
        }

        var splitInput = input.toLowerCase().split("\\s+");

        if (splitInput.length == 0 || input.trim().isEmpty()) {
            return allRecordIndices;
        }

        // For each word in the search query, remove records that contain it
        for (String queryWord : splitInput) {
            if (invertedIndex.containsKey(queryWord)) {
                List<Integer> indicesContainingWord = invertedIndex.get(queryWord);
                indicesContainingWord.forEach(allRecordIndices::remove);
            }

            // If all records are removed, no need to continue
            if (allRecordIndices.isEmpty()) {
                break;
            }
        }

        return allRecordIndices;
    }

    /**
     * Prompts the user to enter a search query and matching strategy, then searches for records accordingly.
     */
    public static void searchForQuery() {
        System.out.println();
        System.out.println("Select a matching strategy: ALL, ANY, NONE");
        var matchingStrategy = sc.nextLine().trim().toUpperCase();

        System.out.println();
        System.out.println("Enter search query:");
        var searchQuery = sc.nextLine().trim().toLowerCase();

        var indexes = new HashSet<Integer>();

        switch (matchingStrategy) {
            case "ALL" -> indexes = findWithAll(searchQuery);
            case "ANY" -> indexes = findWithAny(searchQuery);
            case "NONE" -> indexes = findWithNone(searchQuery);
            default -> System.out.println("Invalid matching strategy");
        }

        if(!indexes.isEmpty()) {
            indexes
                    .stream()
                    .map(i -> data.get(i))
                    .forEach(System.out::println);
        }
        else {
            System.out.println("No records found");
        }
    }

    /**
     * Displays the menu options and returns the user's choice.
     *
     * @return the user's choice as an integer
     */
    public static int getMenuOption() {
        System.out.println();
        System.out.println("=== Menu ===");
        System.out.println("1. Find a record");
        System.out.println("2. Print all records");
        System.out.println("0. Exit");

        return sc.nextLine().charAt(0) - '0';
    }

    /**
     * Displays the menu and processes the user's choice.
     *
     * @return true if the user wants to continue, false if they want to exit
     */
    public static boolean menu() {
        int choice = getMenuOption();

        switch (choice) {
            case 0 -> {
                return false;
            }
            case 1 -> {
                searchForQuery();
            }
            case 2 -> {
                System.out.println();
                System.out.println("=== List of records ===");
                data.forEach(System.out::println);
            }
            default -> {
                System.out.println();
                System.out.println("Incorrect option! Try again.");
            }
        }

        return true;
    }

    public static void main(String[] args) throws IOException {
        initialize(findFilename(args));

        while(menu()) {}

        System.out.println();
        System.out.println("Bye!");
    }
}
