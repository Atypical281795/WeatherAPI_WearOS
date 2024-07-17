package com.example.test_api.GetWeather;
//From Location to Time
import java.util.List;

public class WeatherElement {
    public String elementName;
    public String description;
    public List<Time> time;

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Time> getTime() {
        return time;
    }

    public void setTime(List<Time> time) {
        this.time = time;
    }
}
