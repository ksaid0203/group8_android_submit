package com.example.location2;

import android.location.Location;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LocationRequest1 extends StringRequest {

    final static private String URL = "http://qkr1837425.cafe24.com/Location1.php";

    private Map<String, String> parameters;

    public LocationRequest1(String userID, Location location,  Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("latitude", location.getLatitude() + "");
        parameters.put("longitude", location.getLongitude() + "");
        parameters.put("userID",userID);
    }

    public Map<String, String> getParams(){
        return parameters;
    }


}
