/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package memoryproject;

/**
 *
 * @author orignal vip
 */
import java.util.*;


import java.util.*;

public class MemorySimulator {

    // The single list representing ALL of memory in order
    static List<MemoryBlock> memory = new ArrayList<>();
    
    // All processes that have been allocated
    static List<Process> processes = new ArrayList<>();
    
    static int totalMemorySize;

    // Initialize memory with holes
    public static void initializeMemory(int totalSize, List<MemoryBlock> holes) {
        totalMemorySize = totalSize;
        memory.clear();
        
        // Sort holes by start address first
        holes.sort(Comparator.comparingInt(b -> b.startAddress));
        
        // Fill gaps between holes automatically
        List<MemoryBlock> fullMemory = new ArrayList<>();
        int currentAddress = 0;
        
        for (MemoryBlock hole : holes) {
            if (hole.startAddress > currentAddress) {
                MemoryBlock occupied = new MemoryBlock(currentAddress,
                                        hole.startAddress - currentAddress);
                occupied.isFree = false;
                occupied.processName = "OCCUPIED";
                occupied.segmentName = "-";
                fullMemory.add(occupied);
            }
            fullMemory.add(hole);
            currentAddress = hole.startAddress + hole.size;
        }
        
        if (currentAddress < totalMemorySize) {
            MemoryBlock occupied = new MemoryBlock(currentAddress,
                                    totalMemorySize - currentAddress);
            occupied.isFree = false;
            occupied.processName = "OCCUPIED";
            occupied.segmentName = "-";
            fullMemory.add(occupied);
        }
        
        memory = fullMemory;
    }
    
    // Add a process to the list
    public static void addProcess(Process p) {
        processes.add(p);
    }
    
    // Remove a process from the list
    public static void removeProcess(String processName) {
        processes.removeIf(p -> p.name.equals(processName));
    }

    public static boolean allocateFirstFit(Process p) {
        List<MemoryBlock> memoryCopy = deepCopy(memory);
        boolean allPlaced = true;
        // Store original base addresses
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
                            block.size - seg.size));}
                    placed = true;
                    break;}
            }
            if (!placed) {
                allPlaced = false;
                break;}}
        if (allPlaced) {
            memory = memoryCopy;
            return true;
        } else {
            for (Segment seg : p.segments) {
                seg.baseAddress = originalBases.get(seg);
            }
            return false;}}

    public static boolean allocateBestFit(Process p) {
        List<MemoryBlock> memoryCopy = deepCopy(memory);
        boolean allPlaced = true;
        // Store original base addresses
        Map<Segment, Integer> originalBases = new HashMap<>();
        for (Segment seg : p.segments) {
            originalBases.put(seg, seg.baseAddress);}
        for (Segment seg : p.segments) {
            int bestIndex = -1;
            int bestSize = Integer.MAX_VALUE;
            for (int i = 0; i < memoryCopy.size(); i++) {
                MemoryBlock block = memoryCopy.get(i);
                if (block.isFree && block.size >= seg.size
                        && block.size < bestSize) {
                    bestSize = block.size;
                    bestIndex = i;}
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
                        block.size - seg.size));}} 
            else {
                allPlaced = false;
                break;}}
        if (allPlaced) {
            memory = memoryCopy;
            return true;
        } else {
            for (Segment seg : p.segments) {
                seg.baseAddress = originalBases.get(seg);}
            return false;}}

    public static List<MemoryBlock> deepCopy(List<MemoryBlock> original) {
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

    public static boolean deallocate(String processName) {
        boolean found = false;
        
        for (MemoryBlock block : memory) {
            if (!block.isFree && block.processName != null 
                    && block.processName.equals(processName)) {
                block.isFree = true;
                block.processName = null;
                block.segmentName = null;
                found = true;
            }
        }
        
        if (!found) {
            return false;
        }
        
        processes.removeIf(p -> p.name.equals(processName));
        mergeHoles();
        return true;
    }

    public static void mergeHoles() {
        int i = 0;
        while (i < memory.size() - 1) {
            MemoryBlock current = memory.get(i);
            MemoryBlock next = memory.get(i + 1);
            
            if (current.isFree && next.isFree) {
                current.size = current.size + next.size;
                memory.remove(i + 1);
            } else {
                i++;
            }
        }
    }
    
    // Getter methods for GUI
    public static List<MemoryBlock> getMemory() {
        return memory;
    }
    
    public static List<Process> getProcesses() {
        return processes;
    }
    
    public static int getTotalMemorySize() {
        return totalMemorySize;
    }
}