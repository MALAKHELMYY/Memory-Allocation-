/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package memoryproject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author orignal vip
 */
public class Process {
    String name;
    List<Segment> segments;

    public Process(String name) {
        this.name = name;
        this.segments = new ArrayList<>();
    }

    public void addSegment(Segment s) {
        segments.add(s);
    }
    
}
