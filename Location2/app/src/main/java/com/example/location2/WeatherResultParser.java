package com.example.location2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherResultParser {
    public static Weather getWeather(String result) {
        Weather myweather = new Weather();
        try {
            JSONObject root = new JSONObject(result);
            JSONArray weather = root.getJSONArray("weather");
            JSONObject weather1 = weather.getJSONObject(0);
            //JSONArray main = weather1.getJSONArray("main");
            String currentweather = weather1.getString("main");
            myweather.weather = currentweather;

            JSONObject main = root.getJSONObject("main");
            /*
            JSONObject main1 = main.getJSONObject(0);
            String currentTemparature = main1.getString("temp");
            */
            String currentTemparature = main.getString("temp");
            double AbsoluteTemparature = Double.parseDouble(currentTemparature);
            myweather.temparature = AbsoluteTemparature - 273.0;
        } catch(JSONException e) {
            e.printStackTrace();
            return null;
        }
        return myweather;
    }
}
