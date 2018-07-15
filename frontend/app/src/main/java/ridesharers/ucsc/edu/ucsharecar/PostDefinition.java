package ridesharers.ucsc.edu.ucsharecar;

import java.util.ArrayList;

public class PostDefinition {

    //vars
    private String mStart;
    private String mEnd;
    private String mDepartureTime;
    private String mTotalSeats;
    private ArrayList<String> mPassengers;
    private String mMemo;
    private boolean mDriverNeeded;
    //private String mTempArrival;

    public PostDefinition(String startingLocation, String endingLocation, String departure, String seats, ArrayList<String> names, String memo, boolean driverNeeded) {
        mStart = startingLocation;
        mEnd = endingLocation;
        mDepartureTime = departure;
        mTotalSeats = seats;
        mPassengers = names;
        mMemo = memo;
        mDriverNeeded = driverNeeded;
    }

    public String getmStart() {
        return mStart;
    }

    public String getmEnd() {
        return mEnd;
    }

    public String getmDepartureTime() {
        return mDepartureTime;
    }

    public String getmTotalSeats() {
        return mTotalSeats;
    }

    public ArrayList<String> getmPassengers() {
        return mPassengers;
    }

    public String getmMemo() {
        return mMemo;
    }

    public boolean ismDriverNeeded() {
        return mDriverNeeded;
    }
}
