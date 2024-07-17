package com.example.test_api.GetWeather;
//From WeatherElement to ElementValue
import java.util.List;


public class Time {
    public String startTime;
    public String endTime;
    public List<ElementValue> elementValue;

    public List<ElementValue> getElementValue() {
        return elementValue;
    }


    public void setElementValue(List<ElementValue> elementValue) {
        this.elementValue = elementValue;
    }
}

