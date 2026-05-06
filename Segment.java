/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package memoryproject;

/**
 *
 * @author orignal vip
 */
public class Segment {
    String name;
    int size;
    int baseAddress; 

    public Segment(String name, int size) {
        this.name = name;
        this.size = size;
        this.baseAddress = -1;
    }
    
}
