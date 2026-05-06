# Memory Allocation Simulator — Segmentation

## About

This project simulates how an operating system manages memory using **segmentation** — a technique that divides a process into logical units (Code, Data, Stack) each with a variable size. Unlike paging, segmentation reflects the programmer's view of memory.

The simulator allows you to define memory holes, create processes with multiple segments, and allocate/deallocate them using either First Fit or Best Fit — all visualized in real time.

---

## Supported Allocation Strategies

| Strategy | Description |
|----------|-------------|
| **First Fit** | Places each segment in the first hole large enough to fit it — fast but may leave gaps |
| **Best Fit** | Scans all holes and picks the smallest sufficient one — minimizes leftover fragments |

---

## Features

- 🧠 **Segmentation-based allocation** — each process has named segments (Code, Data, Stack, etc.) with individual sizes
- 🎨 **Live memory layout** — proportional vertical bar with unique colors per process, green for free holes
- 📋 **Segment table** — shows Process, Segment, Base, Size, and Limit for every allocated segment
- ➕ **Dynamic process management** — define processes and segments at runtime
- 🗑️ **Deallocation with hole merging** — frees segments and automatically merges adjacent free holes
- ❌ **Allocation failure handling** — displays an error if a process cannot be fully placed in memory

---

## Tech Stack

- **Java** — core logic and data structures
- **JavaFX** — GUI, Canvas-based memory visualization, and TableView
- **OOP Design** — clean separation across `Segment`, `Process`, `MemoryBlock`, and `MemoryGUI` classes

---

## Project Structure

```
├── Segment.java          # Holds segment name, size, and base address
├── Process.java          # Groups a process name with its list of segments
├── MemoryBlock.java      # Represents a free hole or allocated partition in memory
├── MemorySimulator.java  # Core allocation and deallocation logic
└── MemoryGUI.java        # JavaFX interface and visualization
```

---

## How to Use

1. Set the **total memory size** and click *Initialize Memory*
2. Add **initial free holes** with start address and size, then click *Build Memory*
3. Create a **new process** and add its segments (name + size)
4. Choose an allocation strategy (**First Fit** or **Best Fit**)
5. Click **Allocate Process** — the memory layout and segment table update instantly
6. To free memory, enter a process name and click **Deallocate** — adjacent holes are merged automatically

---

## What I Learned

- Implementing segmentation-based memory allocation from scratch
- Comparing First Fit vs Best Fit strategies and understanding their trade-offs
- Building a proportional visual memory map using JavaFX Canvas
- Managing dynamic data structures to track free holes and allocated blocks
- Handling edge cases like allocation failure and adjacent hole merging

---

## Author

**Malak Mostafa Helmy**
