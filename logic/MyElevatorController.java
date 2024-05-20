package logic;

import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    class AutonomousElevator {

        private final int selfIdx;
        private int minFloor;
        private int maxFloor;

        private final List<Request> requests = new ArrayList<>();
        private final List<Integer> floorQueue = new ArrayList<>();

        boolean fulfillingRequest = false;

        public AutonomousElevator(int selfIdx, int minFloor, int maxFloor) {
            this.selfIdx = selfIdx;
            this.minFloor = minFloor;
            this.maxFloor = maxFloor;
        }

        private int getElevatorFloor() {
            return (int) game.getElevatorFloor(selfIdx);
        }

        private boolean isIdle() {
            return game.isElevatorIdle(selfIdx);
        }

        private void fulfillRequest(Request request) {
            if (getElevatorFloor() != request.getFloor())
                gotoFloor(selfIdx, request.getFloor());
            requests.remove(request);
            fulfillingRequest = true;
        }

        private void evaluatePosition() {
            Request closest = null;
            for (Request request : requests) {
                if (closest == null) {
                    closest = request;
                } else {
                    if (request.getTimeStamp() < closest.getTimeStamp()) {
                        closest = request;
                    }
                }
            }
            if (closest != null)
                fulfillRequest(closest);
        }

        public void onElevatorCall(int floorIdx, Direction dir) {
            requests.add(new Request(floorIdx, System.currentTimeMillis(), dir));
        }

        public void onFloorSelect(int floorIdx) {
            if (isIdle()) {
                floorQueue.add(floorIdx);
                gotoFloor(selfIdx, floorIdx);
            } else {
                floorQueue.add(floorIdx);
            }
        }

        public void onElevatorArrive() {
            System.out.println(selfIdx + " " + floorQueue);
            if (fulfillingRequest) {
                fulfillingRequest = false;
            } else {
                if (floorQueue.size() > 1) {
                    for (int i = 0; i < floorQueue.size(); i++) {
                        if (floorQueue.get(i) == getElevatorFloor()) {
                            floorQueue.remove(i);
                            break;
                        }
                    }
                    gotoFloor(selfIdx, floorQueue.get(0));
                } else if (floorQueue.size() == 1){
                    for (int i = 0; i < floorQueue.size(); i++) {
                        if (floorQueue.get(i) == getElevatorFloor()) {
                            floorQueue.remove(i);
                            break;
                        }
                    }
                    evaluatePosition();
                }
            } 
        }

        public void onIdle() {
            if (!fulfillingRequest && floorQueue.size() <= 0) evaluatePosition();
        }

        public void initalize() {
            evaluatePosition();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AutonomousElevator) {
                AutonomousElevator other = (AutonomousElevator) obj;
                return selfIdx == other.selfIdx;
            }
            return false;
        }

        public int getRequestCount() {
            return requests.size();
        }

    }

    List<AutonomousElevator> elevators = new ArrayList<>();

    // Event: "outside-the-elevator" request, requesting an elevator.
    // The event will be triggered with the request is created/enabled & when it is
    // cleared (reqEnable indicates which).
    public void onElevatorRequestChanged(int floorIdx, Direction dir, boolean reqEnable) {
        if (reqEnable) {
            AutonomousElevator min = null;
            for (AutonomousElevator elevator : elevators) {
                if (floorIdx >= elevator.minFloor && floorIdx <= elevator.maxFloor) { // imma change this to
                                                                                      // redistribute it to the least
                                                                                      // requests TODO
                    if (min == null || Math.abs(floorIdx - elevator.getElevatorFloor()) < Math
                            .abs(floorIdx - min.getElevatorFloor())) {
                        min = elevator;
                    }
                }
            }
            min.onElevatorCall(floorIdx, dir);
        }
    }

    // Event: "inside-the-elevator" request, requesting to go to a floor.
    // The event will be triggered with the request is created/enabled & when it is
    // cleared (reqEnable indicates which).
    public void onFloorRequestChanged(int elevatorIdx, int floorIdx, boolean reqEnable) {
        if (reqEnable) {
            for (AutonomousElevator elevator : elevators) {
                if (elevatorIdx == elevator.selfIdx) {
                    elevator.onFloorSelect(floorIdx);
                    return;
                }
            }
        }
    }

    // Event: Elevator has arrived at the floor & doors are open.
    public void onElevatorArrivedAtFloor(int elevatorIdx, int floorIdx, Direction travelDirection) {
        for (AutonomousElevator elevator : elevators) {
            if (elevatorIdx == elevator.selfIdx) {
                elevator.onElevatorArrive();
                return;
            }
        }
    }

    // Event: Called each frame of the simulation (i.e. called continuously)

    public void onUpdate(double deltaTime) {
        if (game == null) {
            return;
        }
        time += deltaTime;
        if (!game.hasGameHadFirstUpdate()) {
            int elevatorCount = game.getElevatorCount();
            int floorCount = game.getFloorCount();
            for (int i = 0; i < elevatorCount; i++) {
                AutonomousElevator elevator = new AutonomousElevator(i, (floorCount / elevatorCount) * i,
                        ((floorCount / elevatorCount) * (i + 1) - 1));
                elevators.add(elevator);
            }
        }
        for (AutonomousElevator elevator : elevators) {
            if (game.isElevatorIdle(elevator.selfIdx)) {
                elevator.onIdle();
            }
        }
    }
}