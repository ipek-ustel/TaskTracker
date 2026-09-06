import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskCli {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Path FILE_PATH = Paths.get("tasks.json");

    public static class Task {
        private int id;
        private String description;
        private String status;
        private String createdAt;
        private String updatedAt;

        public Task(int id, String description, String status, String createdAt, String updatedAt) {
            this.id = id;
            this.description = description;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public int getId() { return id; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        @Override
        public String toString() {
            return String.format("[%d] (%s) %s", id, status, description);
        }
    }

    // --- JSON Storage Helpers ---

    public static List<Task> loadTasks() {
        if (!Files.exists(FILE_PATH)) {
            return new ArrayList<>();
        }
        try {
            String content = Files.readString(FILE_PATH).trim();
            if (content.isEmpty() || content.equals("[]")) {
                return new ArrayList<>();
            }

            List<Task> tasks = new ArrayList<>();
            Pattern objectPattern = Pattern.compile("\\{[^\\{\\}]*\\}");
            Matcher objectMatcher = objectPattern.matcher(content);

            while (objectMatcher.find()) {
                String obj = objectMatcher.group();
                int id = Integer.parseInt(extractValue(obj, "id"));
                String description = unescapeJson(extractValue(obj, "description"));
                String status = extractValue(obj, "status");
                String createdAt = extractValue(obj, "createdAt");
                String updatedAt = extractValue(obj, "updatedAt");

                tasks.add(new Task(id, description, status, createdAt, updatedAt));
            }
            return tasks;
        } catch (IOException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveTasks(List<Task> tasks) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("  {\n");
            sb.append("    \"id\": ").append(t.getId()).append(",\n");
            sb.append("    \"description\": \"").append(escapeJson(t.getDescription())).append("\",\n");
            sb.append("    \"status\": \"").append(escapeJson(t.getStatus())).append("\",\n");
            sb.append("    \"createdAt\": \"").append(t.getCreatedAt()).append("\",\n");
            sb.append("    \"updatedAt\": \"").append(t.getUpdatedAt()).append("\"\n");
            sb.append("  }");
            if (i < tasks.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");

        try {
            Files.writeString(FILE_PATH, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }

    private static String extractValue(String jsonObject, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(?:\"([^\"]*)\"|([0-9]+))");
        Matcher m = pattern.matcher(jsonObject);
        if (m.find()) {
            return m.group(1) != null ? m.group(1) : m.group(2);
        }
        return "";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
    
    private static String unescapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }


    private static void handleAdd(String[] args) {
    // 1. Validation: check if a description argument was provided
        if (args.length < 2 || args[1].trim().isEmpty()) {
            System.err.println("Error: Description cannot be empty. Usage: task-cli add \"<description>\"");
            return;
        }

        String description = args[1].trim();

        // 2. Load existing tasks
        List<Task> tasks = loadTasks();

        // 3. Compute unique ID: find max existing ID and add 1
        int nextId = 1;
        for (Task t : tasks) {
            if (t.getId() >= nextId) {
                nextId = t.getId() + 1;
            }
        }

        // 4. Set current timestamp
        String now = LocalDateTime.now().format(FORMATTER);

        // 5. Create new task and add to list
        Task newTask = new Task(nextId, description, "todo", now, now);
        tasks.add(newTask);

        // 6. Save back to tasks.json
        saveTasks(tasks);

        // 7. Output success message
        System.out.println("Task added successfully (ID: " + nextId + ")");
    }


    private static void handleList(String[] args) {
        // 1. Load tasks from tasks.json
        List<Task> tasks = loadTasks();
    
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
    
        // 2. Case A: Listing all tasks (no filter argument provided)
        if (args.length == 1) {
            for (Task task : tasks) {
                System.out.println(task);
            }
            return;
        }
    
        // 3. Case B: Listing by status filter
        String filter = args[1].toLowerCase().trim();
    
        if (!filter.equals("todo") && !filter.equals("in-progress") && !filter.equals("done")) {
            System.err.println("Error: Invalid status filter '" + filter + "'. Allowed values: todo, in-progress, done");
            return;
        }
    
        boolean foundAny = false;
        for (Task task : tasks) {
            if (task.getStatus().equalsIgnoreCase(filter)) {
                System.out.println(task);
                foundAny = true;
            }
        }
    
        if (!foundAny) {
            System.out.println("No tasks found with status: " + filter);
        }
    }


    private static int parseId(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.err.println("Error: Task ID must be a valid number. Received: '" + input + "'");
            return -1;
        }
    }

    private static void handleUpdate(String[] args) {
        if (args.length < 3 || args[2].trim().isEmpty()) {
            System.err.println("Usage: task-cli update <id> \"<new_description>\"");
            return;
        }
    
        int id = parseId(args[1]);
        if (id == -1) return;
    
        List<Task> tasks = loadTasks();
        Task targetTask = null;
    
        for (Task task : tasks) {
            if (task.getId() == id) {
                targetTask = task;
                break;
            }
        }
    
        if (targetTask == null) {
            System.err.println("Error: Task with ID " + id + " not found.");
            return;
        }
    
        targetTask.setDescription(args[2].trim());
        targetTask.setUpdatedAt(LocalDateTime.now().format(FORMATTER));
    
        saveTasks(tasks);
        System.out.println("Task updated successfully (ID: " + id + ")");
    }

    private static void handleMarkStatus(String[] args, String newStatus) {
        if (args.length < 2) {
            System.err.println("Usage: task-cli " + args[0] + " <id>");
            return;
        }
    
        int id = parseId(args[1]);
        if (id == -1) return;
    
        List<Task> tasks = loadTasks();
        Task targetTask = null;
    
        for (Task task : tasks) {
            if (task.getId() == id) {
                targetTask = task;
                break;
            }
        }
    
        if (targetTask == null) {
            System.err.println("Error: Task with ID " + id + " not found.");
            return;
        }
    
        targetTask.setStatus(newStatus);
        targetTask.setUpdatedAt(LocalDateTime.now().format(FORMATTER));
    
        saveTasks(tasks);
        System.out.println("Task marked as " + newStatus + " (ID: " + id + ")");
    }


    private static void handleDelete(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: task-cli delete <id>");
            return;
        }
    
        int id = parseId(args[1]);
        if (id == -1) return;
    
        List<Task> tasks = loadTasks();
    
        // Use an Iterator to safely remove an item while iterating over the list
        boolean removed = false;
        Iterator<Task> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.getId() == id) {
                iterator.remove();
                removed = true;
                break;
            }
        }
    
        if (!removed) {
            System.err.println("Error: Task with ID " + id + " not found.");
            return;
        }
    
        saveTasks(tasks);
        System.out.println("Task deleted successfully (ID: " + id + ")");
    }

    // --- Main ---

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add" -> handleAdd(args);
            case "update" -> handleUpdate(args);
            case "delete" -> handleDelete(args);
            case "mark-in-progress" -> handleMarkStatus(args, "in-progress");
            case "mark-done" -> handleMarkStatus(args, "done");
            case "list" -> handleList(args);
            default -> {
                System.err.println("Unknown command: " + action);
                printHelp();
            }
        }
    }

    private static void printHelp() {
        System.out.println("""
            Usage:
              task-cli add <description>
              task-cli update <id> <description>
              task-cli delete <id>
              task-cli mark-in-progress <id>
              task-cli mark-done <id>
              task-cli list [todo|in-progress|done]
            """);
    }
}