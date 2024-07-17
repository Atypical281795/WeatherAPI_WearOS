package com.example.test_api.GetWeather;

public class WeatherResponse {
    public CwaOpenData cwaopendata;

    public CwaOpenData getCwaOpenData() {
        return cwaopendata;
    }

    public void setCwaOpenData(CwaOpenData cwaopendata) {
        this.cwaopendata = cwaopendata;
    }
}
