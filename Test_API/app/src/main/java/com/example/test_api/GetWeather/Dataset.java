package com.example.test_api.GetWeather;
//From CwaOpenData to Location
import java.util.List;

public class Dataset {
    public List<Location> location;

    public List<Location> getLocation() {
        return location;
    }

    public void setLocation(List<Location> location) {
        this.location = location;
    }
}
