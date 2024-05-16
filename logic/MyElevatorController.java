package logic;

import java.util.HashMap;
import java.util.Map;

import game.ElevatorController;
import game.Game;

public class MyElevatorController implements ElevatorController {

    class Request {

        private final long timestamp;
        private final Direction dir;

        public Request(long timestamp, Direction dir) {
            this.timestamp = timestamp;
            this.dir = dir;
        }

        public long getTimeStamp() {
            return this.timestamp;
        }

        public Direction getDir() {
            return this.dir;
        }

    }

    // Private member data
    private Game game;

    private double time = 0;

    // Students should implement this function to return their name
    public String getStudentName() {
        return "Dhruv";
    }

    public int getStudentPeriod() {
        return 1;
    }

    // Event: Game has started
    public void onGameStarted(Game game) {
        this.game = game;
    }

    // Event: "outside-the-elevator" request, requesting an elevator.
    // The event will be triggered with the request is created/enabled & when it is
    // cleared (reqEnable indicates which).

    Map<Integer, Request> requests = new HashMap<>();

    boolean droppingOff0 = false;
    boolean droppingOff1 = false;

    private int goToClosest(int elevatorIdx, boolean sortByTime) {
        int closestFloor = -1;
        int floorIdx = (int) game.getElevatorFloor(elevatorIdx);
        for (Integer integer : requests.keySet()) { // finding closest floor
            if (closestFloor == -1) { //sorting by time
                closestFloor = integer;
            } else {
                boolean sort = requests.get(integer).getTimeStamp() < requests.get(closestFloor).getTimeStamp() ? sortByTime : Math.abs(closestFloor - floorIdx) > Math.abs(integer - floorIdx);
                if (sort) {
                    closestFloor = integer;
                }
            }
        }
        if (closestFloor != -1) {
            if (closestFloor != floorIdx)
                gotoFloor(elevatorIdx, closestFloor);
            requests.remove(closestFloor);
        }
        return closestFloor;
    }

    public void onElevatorRequestChanged(int floorIdx, Direction dir, boolean reqEnable) {
        if (reqEnable)
            requests.put(floorIdx, new Request(System.currentTimeMillis(), dir)); // adding direction request
    }

    // Event: "inside-the-elevator" request, requesting to go to a floor.
    // The event will be triggered with the request is created/enabled & when it is
    // cleared (reqEnable indicates which).
    public void onFloorRequestChanged(int elevatorIdx, int floorIdx, boolean reqEnable) {
        if (reqEnable) {
            gotoFloor(elevatorIdx, floorIdx); //going to drop off 
            if (elevatorIdx == 0) {
                System.out.println("0 is dropping off " + floorIdx + " " + time);
                droppingOff0 = true;
            } else {
                System.out.println("1 is dropping off " + floorIdx + " " + time);
                droppingOff1 = true;
            } //storing that we're dropping off so onArrived knows to queue another movement
        }
    }

    // Event: Elevator has arrived at the floor & doors are open.
    public void onElevatorArrivedAtFloor(int elevatorIdx, int floorIdx, Direction travelDirection) {
        if (elevatorIdx == 0) {
            if (droppingOff0) {
                droppingOff0 = false;
                System.out.println("0 dropped off " + floorIdx + ", now going to " + goToClosest(elevatorIdx, false) + " "
                        + time);
            }
        } else {
            if (droppingOff1) {
                droppingOff1 = false;
                System.out.println("1 dropped off " + floorIdx + ", now going to " + goToClosest(elevatorIdx, false) + " "
                        + time);
            }
        } //going to the next pickup
    }

    // Event: Called each frame of the simulation (i.e. called continuously)

    public void onUpdate(double deltaTime) {
        if (game == null) {
            return;
        }
        time += deltaTime;
        if (requests.size() > 0 && game.isElevatorIdle(0)) {
            System.out.println("moving to " + goToClosest(0, false) + " " + time);
        }

        if (requests.size() > 0 && game.isElevatorIdle(1)) {
            System.out.println("moving to " + goToClosest(1, false) + " " + time);
        } //for intiialization
    }
}