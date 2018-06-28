package com.example.location2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleGeocodingResultParser {
    public static MyAddress getAddress(String result) {
        MyAddress myAddress = new MyAddress();
        try {
            JSONObject root = new JSONObject(result);
            JSONArray results = root.getJSONArray("results");
            JSONObject results1 = results.getJSONObject(0);
            String testAddress = results1.getString("formatted_address");
            JSONArray address_components = results1.getJSONArray("address_components");
            myAddress.address = testAddress;
            JSONObject address_components1 = address_components.getJSONObject(0);
            String establishment = address_components1.getString("long_name");
            myAddress.establishment = establishment;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return myAddress;
    }
}
