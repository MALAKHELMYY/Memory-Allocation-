/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package memoryproject;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.util.*;

public class MemoryGUI extends Application {

    // Input fields
    TextField memorySizeField = new TextField();
    TextField holeStartField = new TextField();
    TextField holeSizeField = new TextField();
    TextField processNameField = new TextField();
    TextField segmentNameField = new TextField();
    TextField segmentSizeField = new TextField();
    TextField deallocField = new TextField();
    ComboBox<String> methodBox = new ComboBox<>();

    // Lists
    ListView<String> holeListView = new ListView<>();
    ListView<String> segListView = new ListView<>();

    // Memory bar
    Canvas memoryCanvas = new Canvas(200, 600);

    // Segment table
    TableView<SegmentRow> segmentTable = new TableView<>();

    // Data
    List<MemoryBlock> memory = new ArrayList<>();
    List<Process> processes = new ArrayList<>();
    List<int[]> pendingHoles = new ArrayList<>();
    Process currentProcess = null;
    int totalMemorySize = 0;
    boolean memoryInitialized = false;

    // Colors for processes
    Color[] processColors = {
        Color.web("#FFB3BA"), // pink
        Color.web("#BAE1FF"), // blue
        Color.web("#BAFFC9"), // green
        Color.web("#FFFFBA"), // yellow
        Color.web("#D8B4FF"), // purple
        Color.web("#FFD5B0"), // orange
    };
    Map<String, Color> processColorMap = new HashMap<>();
    int colorIndex = 0;

    // SegmentRow class for TableView
    public static class SegmentRow {
        private String process, segment;
        private int base, size, limit;

        public SegmentRow(String process, String segment,
                int base, int size, int limit) {
            this.process = process;
            this.segment = segment;
            this.base = base;
            this.size = size;
            this.limit = limit;
        }

        public String getProcess() { return process; }
        public String getSegment() { return segment; }
        public int getBase() { return base; }
        public int getSize() { return size; }
        public int getLimit() { return limit; }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(
            "Memory Allocation Simulator - Segmentation");

        methodBox.getItems().addAll("First-Fit", "Best-Fit");
        methodBox.setValue("First-Fit");

        BorderPane root = new BorderPane();
        root.setLeft(buildLeftPanel());
        root.setCenter(buildRightPanel());
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ============ LEFT PANEL ============
    ScrollPane buildLeftPanel() {
    VBox panel = new VBox(6);
    panel.setPadding(new Insets(10));
    panel.setPrefWidth(260);

    Button resetBtn = new Button("🔄 RESET ALL");
    resetBtn.setMaxWidth(Double.MAX_VALUE);
    resetBtn.setStyle("-fx-background-color: #E74C3C; "
            + "-fx-text-fill: white; "
            + "-fx-font-weight: bold; "
            + "-fx-background-radius: 5; "
            + "-fx-font-size: 13;");
    resetBtn.setOnAction(e -> resetAll());

    holeListView.setPrefHeight(70);
    segListView.setPrefHeight(70);

    panel.getChildren().addAll(
        resetBtn,
        sectionLabel("1. Memory Setup"),
        labeledRow("Total Memory Size:", memorySizeField),
        styledButton("Initialize Memory", "#4A90D9",
            e -> initializeMemory()),
        sectionLabel("2. Add Holes"),
        labeledRow("Start Address:", holeStartField),
        labeledRow("Size:", holeSizeField),
        styledButton("Add Hole", "#4A90D9", e -> addHole()),
        new Label("Pending Holes:"),
        holeListView,
        styledButton("Build Memory", "#27AE60", e -> buildMemory()),
        sectionLabel("3. New Process"),
        labeledRow("Process Name:", processNameField),
        styledButton("New Process", "#4A90D9", e -> newProcess()),
        sectionLabel("4. Add Segments"),
        labeledRow("Segment Name:", segmentNameField),
        labeledRow("Size:", segmentSizeField),
        styledButton("Add Segment", "#4A90D9", e -> addSegment()),
        new Label("Segments:"),
        segListView,
        sectionLabel("5. Allocate"),
        labeledRow("Method:", methodBox),
        styledButton("Allocate Process", "#27AE60",
            e -> allocateProcess()),
        sectionLabel("6. Deallocate"),
        labeledRow("Process Name:", deallocField),
        styledButton("Deallocate", "#E74C3C", e -> {
            deallocate(deallocField.getText().trim());
            deallocField.clear();
        })
    );

    ScrollPane scroll = new ScrollPane(panel);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    return scroll;
}

    // ============ RIGHT PANEL ============
    HBox buildRightPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));

        // Memory Bar
        VBox barBox = new VBox(5);
        barBox.setPrefWidth(220);
        Label barTitle = new Label("Memory Layout");
        barTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ScrollPane canvasScroll = new ScrollPane(memoryCanvas);
        canvasScroll.setPrefHeight(650);
        barBox.getChildren().addAll(barTitle, canvasScroll);

        // Segment Table
        VBox tableBox = new VBox(5);
        HBox.setHgrow(tableBox, Priority.ALWAYS);
        Label tableTitle = new Label("Segment Tables");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        TableColumn<SegmentRow, String> colProcess =
            new TableColumn<>("Process");
        colProcess.setCellValueFactory(
            new PropertyValueFactory<>("process"));
        colProcess.setPrefWidth(80);

        TableColumn<SegmentRow, String> colSeg =
            new TableColumn<>("Segment");
        colSeg.setCellValueFactory(
            new PropertyValueFactory<>("segment"));
        colSeg.setPrefWidth(80);

        TableColumn<SegmentRow, Integer> colBase =
            new TableColumn<>("Base");
        colBase.setCellValueFactory(
            new PropertyValueFactory<>("base"));
        colBase.setPrefWidth(80);

        TableColumn<SegmentRow, Integer> colSize =
            new TableColumn<>("Size");
        colSize.setCellValueFactory(
            new PropertyValueFactory<>("size"));
        colSize.setPrefWidth(80);

        TableColumn<SegmentRow, Integer> colLimit =
            new TableColumn<>("Limit");
        colLimit.setCellValueFactory(
            new PropertyValueFactory<>("limit"));
        colLimit.setPrefWidth(80);

        segmentTable.getColumns().addAll(
            colProcess, colSeg, colBase, colSize, colLimit);
        segmentTable.setPrefHeight(650);
        VBox.setVgrow(segmentTable, Priority.ALWAYS);

        // Color rows by process
        segmentTable.setRowFactory(tv -> new TableRow<SegmentRow>() {
            @Override
            protected void updateItem(SegmentRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    Color c = processColorMap.getOrDefault(
                        item.getProcess(), Color.WHITE);
                    setStyle("-fx-background-color: "
                        + toHex(c) + ";");
                }
            }
        });

        tableBox.getChildren().addAll(tableTitle, segmentTable);
        panel.getChildren().addAll(barBox, tableBox);
        return panel;
    }

    // ============ DRAW MEMORY BAR ============
    void drawMemoryBar() {
        GraphicsContext gc = memoryCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0,
            memoryCanvas.getWidth(), memoryCanvas.getHeight());

        if (memory.isEmpty() || totalMemorySize == 0) return;

        double canvasHeight = 580;
        double canvasWidth = 150;
        double x = 50;
        double yStart = 10;

        memoryCanvas.setHeight(canvasHeight + 30);
        memoryCanvas.setWidth(x + canvasWidth + 10);

        for (MemoryBlock block : memory) {
            double blockHeight = ((double) block.size / totalMemorySize)
                    * canvasHeight;
            if (blockHeight < 25) blockHeight = 25;

            // Choose color
            Color blockColor;
            if (block.isFree) {
                blockColor = Color.web("#C8E6C9");
            } else if ("OCCUPIED".equals(block.processName)) {
                blockColor = Color.web("#B0B0B0");
            } else {
                blockColor = processColorMap.getOrDefault(
                    block.processName, Color.WHITE);
            }

            // Fill block
            gc.setFill(blockColor);
            gc.fillRect(x, yStart, canvasWidth, blockHeight);

            // Border
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(1);
            gc.strokeRect(x, yStart, canvasWidth, blockHeight);

            // Tick mark on left
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(1);
            gc.strokeLine(x - 5, yStart, x, yStart);

            // Address on left — clean and aligned
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 10));
            String addr = String.valueOf(block.startAddress);
            gc.fillText(addr,
                x - 8 - (addr.length() * 6), yStart + 4);

            // Label inside block
            String label;
            if (block.isFree) {
                label = "FREE (" + block.size + ")";
            } else if ("OCCUPIED".equals(block.processName)) {
                label = "OCCUPIED";
            } else {
                label = block.processName + "-"
                        + block.segmentName
                        + " (" + block.size + ")";
            }

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
            double textX = x + 5;
            double textY = yStart + blockHeight / 2 + 4;
            gc.fillText(label, textX, textY);

            yStart += blockHeight;
        }

        // End address with tick
        gc.setStroke(Color.DARKGRAY);
        gc.strokeLine(x - 5, yStart, x, yStart);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 10));
        String endAddr = String.valueOf(totalMemorySize);
        gc.fillText(endAddr,
            x - 8 - (endAddr.length() * 6), yStart + 4);
    }

    // ============ ACTIONS ============
    void initializeMemory() {
        try {
            totalMemorySize = Integer.parseInt(
                memorySizeField.getText().trim());
            memory.clear();
            processes.clear();
            pendingHoles.clear();
            holeListView.getItems().clear();
            segListView.getItems().clear();
            processColorMap.clear();
            colorIndex = 0;
            memoryInitialized = false;
            refreshDisplay();
            showInfo("Memory size set to " + totalMemorySize
                + ". Now add your holes.");
        } catch (NumberFormatException e) {
            showError("Invalid memory size!");
        }
    }

    void addHole() {
        try {
            int start = Integer.parseInt(
                holeStartField.getText().trim());
            int size = Integer.parseInt(
                holeSizeField.getText().trim());
            pendingHoles.add(new int[]{start, size});
            holeListView.getItems().add(
                "Hole: start=" + start + "  size=" + size);
            holeStartField.clear();
            holeSizeField.clear();
        } catch (NumberFormatException e) {
            showError("Invalid hole values!");
        }
    }

    void buildMemory() {
        if (totalMemorySize == 0) {
            showError("Initialize memory size first!");
            return;
        }
        memory.clear();
        for (int[] hole : pendingHoles) {
            memory.add(new MemoryBlock(hole[0], hole[1]));
        }
        memory.sort(Comparator.comparingInt(b -> b.startAddress));

        List<MemoryBlock> fullMemory = new ArrayList<>();
        int currentAddress = 0;
        for (MemoryBlock hole : memory) {
            if (hole.startAddress > currentAddress) {
                MemoryBlock occ = new MemoryBlock(currentAddress,
                        hole.startAddress - currentAddress);
                occ.isFree = false;
                occ.processName = "OCCUPIED";
                occ.segmentName = "-";
                fullMemory.add(occ);
            }
            fullMemory.add(hole);
            currentAddress = hole.startAddress + hole.size;
        }
        if (currentAddress < totalMemorySize) {
            MemoryBlock occ = new MemoryBlock(currentAddress,
                    totalMemorySize - currentAddress);
            occ.isFree = false;
            occ.processName = "OCCUPIED";
            occ.segmentName = "-";
            fullMemory.add(occ);
        }
        memory = fullMemory;
        memoryInitialized = true;
        refreshDisplay();
        showInfo("Memory built! Now add processes.");
    }

    void newProcess() {
        String name = processNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Enter a process name!");
            return;
        }
        currentProcess = new Process(name);
        segListView.getItems().clear();
        if (!processColorMap.containsKey(name)) {
            processColorMap.put(name,
                processColors[colorIndex % processColors.length]);
            colorIndex++;
        }
        showInfo("Process " + name
            + " created. Now add segments.");
    }

    void addSegment() {
        if (currentProcess == null) {
            showError("Create a process first!");
            return;
        }
        try {
            String name = segmentNameField.getText().trim();
            int size = Integer.parseInt(
                segmentSizeField.getText().trim());
            currentProcess.addSegment(new Segment(name, size));
            segListView.getItems().add(name + "  (" + size + ")");
            segmentNameField.clear();
            segmentSizeField.clear();
        } catch (NumberFormatException e) {
            showError("Invalid segment size!");
        }
    }

    void allocateProcess() {
        if (!memoryInitialized) {
            showError("Build memory first!");
            return;
        }
        if (currentProcess == null
                || currentProcess.segments.isEmpty()) {
            showError("Create a process with segments first!");
            return;
        }
        boolean success;
        if (methodBox.getValue().equals("First-Fit")) {
            success = allocateFirstFit(currentProcess);
        } else {
            success = allocateBestFit(currentProcess);
        }
        if (success) {
            processes.add(currentProcess);
            showInfo("Process " + currentProcess.name
                + " allocated successfully!");
        } else {
            showError("Process " + currentProcess.name
                + " could NOT be fully allocated!");
        }
        currentProcess = null;
        processNameField.clear();
        segListView.getItems().clear();
        refreshDisplay();
    }

    void deallocate(String processName) {
        if (processName.isEmpty()) {
            showError("Enter a process name!");
            return;
        }
        boolean found = false;
        for (MemoryBlock block : memory) {
            if (!block.isFree
                    && processName.equals(block.processName)) {
                block.isFree = true;
                block.processName = null;
                block.segmentName = null;
                found = true;
            }
        }
        if (!found) {
            showError("Process " + processName + " not found!");
            return;
        }
        processes.removeIf(p -> p.name.equals(processName));
        processColorMap.remove(processName);
        mergeHoles();
        refreshDisplay();
        showInfo("Process " + processName
            + " deallocated successfully!");
    }

    void resetAll() {
        memory.clear();
        processes.clear();
        pendingHoles.clear();
        holeListView.getItems().clear();
        segListView.getItems().clear();
        processColorMap.clear();
        colorIndex = 0;
        totalMemorySize = 0;
        memoryInitialized = false;
        currentProcess = null;
        memorySizeField.clear();
        holeStartField.clear();
        holeSizeField.clear();
        processNameField.clear();
        segmentNameField.clear();
        segmentSizeField.clear();
        deallocField.clear();
        segmentTable.getItems().clear();
        GraphicsContext gc = memoryCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0,
            memoryCanvas.getWidth(), memoryCanvas.getHeight());
        showInfo("Reset complete! Start fresh.");
    }

    // ============ ALLOCATION LOGIC ============
    // ============ FIXED ALLOCATION LOGIC (All-or-Nothing) ============
boolean allocateFirstFit(Process p) {
    // Work on a copy of memory first
    List<MemoryBlock> memoryCopy = deepCopy(memory);
    boolean allPlaced = true;
    
    // Store original base addresses to restore if allocation fails
    Map<Segment, Integer> originalBases = new HashMap<>();
    for (Segment seg : p.segments) {
        originalBases.put(seg, seg.baseAddress);
    }
    
    for (Segment seg : p.segments) {
        boolean placed = false;
        for (int i = 0; i < memoryCopy.size(); i++) {
            MemoryBlock block = memoryCopy.get(i);
            if (block.isFree && block.size >= seg.size) {
                seg.baseAddress = block.startAddress;
                MemoryBlock allocated = new MemoryBlock(
                    block.startAddress, seg.size, p.name, seg.name);
                memoryCopy.set(i, allocated);
                if (block.size > seg.size) {
                    memoryCopy.add(i + 1, new MemoryBlock(
                        block.startAddress + seg.size,
                        block.size - seg.size));
                }
                placed = true;
                break;
            }
        }
        if (!placed) {
            allPlaced = false;
            break;
        }
    }
    
    // Only apply to real memory if ALL segments were placed
    if (allPlaced) {
        memory = memoryCopy;
        return true;
    } else {
        // Reset all base addresses since process was not allocated
        for (Segment seg : p.segments) {
            seg.baseAddress = originalBases.get(seg);
        }
        return false;
    }
}

boolean allocateBestFit(Process p) {
    // Work on a copy of memory first
    List<MemoryBlock> memoryCopy = deepCopy(memory);
    boolean allPlaced = true;
    
    // Store original base addresses to restore if allocation fails
    Map<Segment, Integer> originalBases = new HashMap<>();
    for (Segment seg : p.segments) {
        originalBases.put(seg, seg.baseAddress);
    }
    
    for (Segment seg : p.segments) {
        int bestIndex = -1;
        int bestSize = Integer.MAX_VALUE;
        for (int i = 0; i < memoryCopy.size(); i++) {
            MemoryBlock block = memoryCopy.get(i);
            if (block.isFree && block.size >= seg.size
                    && block.size < bestSize) {
                bestSize = block.size;
                bestIndex = i;
            }
        }
        if (bestIndex != -1) {
            MemoryBlock block = memoryCopy.get(bestIndex);
            seg.baseAddress = block.startAddress;
            MemoryBlock allocated = new MemoryBlock(
                block.startAddress, seg.size, p.name, seg.name);
            memoryCopy.set(bestIndex, allocated);
            if (block.size > seg.size) {
                memoryCopy.add(bestIndex + 1, new MemoryBlock(
                    block.startAddress + seg.size,
                    block.size - seg.size));
            }
        } else {
            allPlaced = false;
            break;
        }
    }
    
    // Only apply to real memory if ALL segments were placed
    if (allPlaced) {
        memory = memoryCopy;
        return true;
    } else {
        // Reset all base addresses since process was not allocated
        for (Segment seg : p.segments) {
            seg.baseAddress = originalBases.get(seg);
        }
        return false;
    }
}

// Helper method to deep copy the memory list
List<MemoryBlock> deepCopy(List<MemoryBlock> original) {
    List<MemoryBlock> copy = new ArrayList<>();
    for (MemoryBlock block : original) {
        MemoryBlock newBlock = new MemoryBlock(
            block.startAddress, block.size);
        newBlock.isFree = block.isFree;
        newBlock.processName = block.processName;
        newBlock.segmentName = block.segmentName;
        copy.add(newBlock);
    }
    return copy;
}

    void mergeHoles() {
        int i = 0;
        while (i < memory.size() - 1) {
            MemoryBlock cur = memory.get(i);
            MemoryBlock next = memory.get(i + 1);
            if (cur.isFree && next.isFree) {
                cur.size += next.size;
                memory.remove(i + 1);
            } else {
                i++;
            }
        }
    }

    // ============ REFRESH ============
    void refreshDisplay() {
        segmentTable.getItems().clear();
        for (Process p : processes) {
            for (Segment seg : p.segments) {
                segmentTable.getItems().add(new SegmentRow(
                    p.name, seg.name, seg.baseAddress,
                    seg.size, seg.baseAddress + seg.size));
            }
        }
        drawMemoryBar();
    }

    // ============ HELPERS ============
    String toHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int)(c.getRed() * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue() * 255));
    }

    Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        label.setPadding(new Insets(8, 0, 2, 0));
        return label;
    }

    HBox labeledRow(String labelText, Control field) {
        Label label = new Label(labelText);
        label.setPrefWidth(120);
        field.setPrefWidth(120);
        HBox row = new HBox(5, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    Button styledButton(String text, String color,
        javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + color + "; "
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold; "
                + "-fx-background-radius: 5;");
        btn.setOnAction(action);
        return btn;
    }

    void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}