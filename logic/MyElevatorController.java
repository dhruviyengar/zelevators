package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import game.ElevatorController;
import game.Game;

public class MyElevatorController implements ElevatorController {
    // Private member data
    private Game game;

    private double time = 0;

    // Students should implement this function to return their name
    public String getStudentName() {
        return "Dhruv";   // <-- TODO: Replace with your name
    }
    public int getStudentPeriod() {
        return 1;              // <-- TODO: Replace with your class period
    }

    // Event: Game has started
    public void onGameStarted(Game game) {
        this.game = game;
    }

    // Event: "outside-the-elevator" request, requesting an elevator.
    //  The event will be triggered with the request is created/enabled & when it is cleared (reqEnable indicates which).

    Map<Integer, Direction> requests = new HashMap<Integer, Direction>();
    List<Integer> targets = new ArrayList<>();

    public void onElevatorRequestChanged(int floorIdx, Direction dir, boolean reqEnable) {
        System.out.println("onElevatorRequestChanged(" + floorIdx + ", " + dir + ", " + reqEnable + ")");
        if (reqEnable) {
            requests.put(floorIdx, dir);
        } else if (!reqEnable) {
            requests.put(floorIdx, null);
        }
    }

    // Event: "inside-the-elevator" request, requesting to go to a floor.
    //  The event will be triggered with the request is created/enabled & when it is cleared (reqEnable indicates which).
    public void onFloorRequestChanged(int elevatorIdx, int floorIdx, boolean reqEnable) {
        

        

    }

    // Event: Elevator has arrived at the floor & doors are open.
    public void onElevatorArrivedAtFloor(int elevatorIdx, int floorIdx, Direction travelDirection) {
        targets.remove(floorIdx);

        
    }

    // Event: Called each frame of the simulation (i.e. called continuously)
    public void onUpdate(double deltaTime) {
        if (game == null) {
            return;
        }
        time += deltaTime;
        for (int i = 0; i < game.getFloorCount(); i++) {
            if (!requests.containsKey(i)) {
                requests.put(i, null);
            }
        }
    }
}