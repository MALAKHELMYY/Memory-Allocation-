/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package memoryproject;

/**
 *
 * @author orignal vip
 */
public class MemoryBlock {
    int startAddress;
    int size;
    boolean isFree;
    String processName;  // null if it's a hole
    String segmentName;  // null if it's a hole

    // Constructor for a hole
    public MemoryBlock(int startAddress, int size) {
        this.startAddress = startAddress;
        this.size = size;
        this.isFree = true;
        this.processName = null;
        this.segmentName = null;
    }

    // Constructor for an allocated block
    public MemoryBlock(int startAddress, int size, 
                       String processName, String segmentName) {
        this.startAddress = startAddress;
        this.size = size;
        this.isFree = false;
        this.processName = processName;
        this.segmentName = segmentName;
    }
}
