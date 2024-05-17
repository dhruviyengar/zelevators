package logic;

import java.util.ArrayList;
import java.util.List;

import game.ElevatorController;
import game.Game;

public class MyElevatorController implements ElevatorController {

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

    List<Request> currentRequests = new ArrayList<>();

    boolean droppingOff0 = false;
    boolean droppingOff1 = false;

    class Request {

        private final int floor;
        private final long timestamp;
        private final Direction dir;

        public Request(int floor, long timestamp, Direction dir) {
            this.floor = floor;
            this.timestamp = timestamp;
            this.dir = dir;
        }

        public int getFloor() {
            return this.floor;
        }

        public long getTimeStamp() {
            return this.timestamp;
        }

        public Direction getDir() {
            return this.dir;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Request) {
                Request other = (Request) obj;
                return other.getDir() == getDir() && other.getTimeStamp() == getTimeStamp()
                        && other.getFloor() == getFloor();
            }
            return false;
        }

    }

    private void fulfillRequest(int elevatorIdx, Request request) {
        if ((int) game.getElevatorFloor(elevatorIdx) != request.getFloor()) gotoFloor(elevatorIdx, request.getFloor());
        currentRequests.remove(request);
    }

    private Request goToClosest(int elevatorIdx, boolean sortByTime) {
        Request closest = null;
        int floorIdx = (int) game.getElevatorFloor(elevatorIdx);
        for (Request request : currentRequests) { // finding closest floor
            if (closest == null) { // sorting by time
                closest = request;
            } else {
                boolean sort = request.getTimeStamp() < closest.getTimeStamp()
                        ? sortByTime
                        : Math.abs(closest.getFloor() - floorIdx) > Math.abs(request.getFloor() - floorIdx);
                if (sort) {
                    closest = request;
                }
            }
        }
        if (closest != null) {
            fulfillRequest(elevatorIdx, closest);
        }
        return closest;
    }

    public void onElevatorRequestChanged(int floorIdx, Direction dir, boolean reqEnable) {
        if (reqEnable)
            currentRequests.add(new Request(floorIdx, System.currentTimeMillis(), dir)); // adding direction request
    }

    // Event: "inside-the-elevator" request, requesting to go to a floor.
    // The event will be triggered with the request is created/enabled & when it is
    // cleared (reqEnable indicates which).
    public void onFloorRequestChanged(int elevatorIdx, int floorIdx, boolean reqEnable) {
        if (reqEnable) {
            gotoFloor(elevatorIdx, floorIdx); // going to drop off
            if (elevatorIdx == 0) {
                System.out.println("0 is dropping off " + floorIdx + " " + time);
                droppingOff0 = true;
            } else {
                System.out.println("1 is dropping off " + floorIdx + " " + time);
                droppingOff1 = true;
            } // storing that we're dropping off so onArrived knows to queue another movement
        }
    }

    // Event: Elevator has arrived at the floor & doors are open.
    public void onElevatorArrivedAtFloor(int elevatorIdx, int floorIdx, Direction travelDirection) {
        if (elevatorIdx == 0) {
            if (droppingOff0) {
                droppingOff0 = false;
                System.out
                        .println("0 dropped off " + floorIdx + ", now going to " + goToClosest(elevatorIdx, false) + " "
                                + time);
            }
        } else {
            if (droppingOff1) {
                droppingOff1 = false;
                System.out
                        .println("1 dropped off " + floorIdx + ", now going to " + goToClosest(elevatorIdx, false) + " "
                                + time);
            }
        } // going to the next pickup
    }

    // Event: Called each frame of the simulation (i.e. called continuously)

    public void onUpdate(double deltaTime) {
        if (game == null) {
            return;
        }
        time += deltaTime;
        if (currentRequests.size() > 0 && game.isElevatorIdle(0)) {
            System.out.println("moving to " + goToClosest(0, false) + " " + time);
        }

        if (currentRequests.size() > 0 && game.isElevatorIdle(1)) {
            System.out.println("moving to " + goToClosest(1, false) + " " + time);
        } // for intiialization
    }
}