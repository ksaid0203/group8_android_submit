package com.example.location2;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;
/* 실행 되지 않음 */
public class FriendLocationRequest extends StringRequest {

    final static private String URL = "http://qkr1837425.cafe24.com/FriendLocation.php";

    private Map<String, String> parameters;

    public FriendLocationRequest(String userID, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("userID", userID);
    }

    public Map<String, String> getParams(){
        return parameters;
    }


}