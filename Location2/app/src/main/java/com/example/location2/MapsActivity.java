package com.example.location2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import org.json.JSONObject;

import java.io.File;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import static java.lang.Math.sqrt;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private LatLng[] array;
    private final LatLng john = new LatLng(35.23562093686465, 129.08138528466225 );
    private final LatLng james = new LatLng(35.23482649710967, 129.08108320087194);
    private final LatLng kim = new LatLng(35.23110093367368, 129.0799006819725);
    private final LatLng lee = new LatLng(35.23217283118824, 129.0812598913908);
    private final LatLng park = new LatLng(35.23452799825873, 129.07867290079594);
    private String userId;
    private GoogleMap mMap;

    private static final int REQUEST_USED_PERMISSION = 200; // 요청에 대한 응답코드 정의
    private static final String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    }; // 요청할 권한을 배열로 정의
    private TextView routeTextView;
    private Button button;
    private TextView weatherTextView;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 권한 요청에 대한 응답 처리
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionToLocationAccept = true;

        switch(requestCode) {
            case REQUEST_USED_PERMISSION:
                for(int result : grantResults) { // 배열에 담긴 요청 결과 확인
                    if(result!= PackageManager.PERMISSION_GRANTED) {
                        permissionToLocationAccept = false;
                        break;
                    }
                }
                break;
        }

        if(permissionToLocationAccept == false) {
            finish(); // 권한 요청이 실패했다면 앱 종료
        }
        else {
            getMyLocation();
        }
    }

    private static final long INTERVAL_TIME =60000;
    private static final long FASTEST_INTERVAL_TIME = 5000;

    private static final LatLng DEFAULT_LOCATION
            = new LatLng(37.56641923090, 126.9778741551);
    private static final  int DEFAULT_ZOOM = 15;

    private int pinNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for(String permission:needPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, needPermissions, REQUEST_USED_PERMISSION);
                break;
            }
        }
        setContentView(R.layout.activity_maps);
        array = new LatLng[5];
        array[0] = james;
        array[1]= john;
        array[2] = kim;
        array[3] = lee;
        array[4] = park;
        Intent intent = getIntent();

        userId = intent.getStringExtra("userID");

        routeTextView = (TextView)findViewById(R.id.route);
        weatherTextView = (TextView)findViewById(R.id.weatherView);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                for(int i = 0 ; i < 5 ; i++) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(array[i]);
                    markerOptions.title("핀  " + pinNumber);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_edit_location_black_24dp));
                    mMap.addMarker(markerOptions);
                }
                getMyLocation();

            }
        });

        button = (Button)findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeather();
            }
        });
    }
    /* json으로 null을 return 하기만 해서 주석처리함 */
    /*
    void getFriendLocation() {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if(success) {
                        friendLatLng = new LatLng(jsonResponse.getDouble("latitude"), jsonResponse.getDouble("longitude"));
                        //friendLatLng = new LatLng(Double.parseDouble(jsonResponse.getString("latitude")),
                        //        Double.parseDouble(jsonResponse.getString("longitude")));
                        friendName = jsonResponse.getString("userName");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        FriendLocationRequest friendLocationRequest = new FriendLocationRequest(userId, responseListener);
        RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
        queue.add(friendLocationRequest);
    }
    */
    void findFriends(Location location) {
        double dist = 500.0;
        for(int i =0;i<5; i++) {
            if(distance(location, array[i] ) < dist) {
                Toast.makeText(MapsActivity.this,"친구가 "+ dist + "m 내에 있습니다.",Toast.LENGTH_LONG).show();
                return;
            }
        }
        Toast.makeText(MapsActivity.this, "친구가 "+ dist + "m 내에 없습니다.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        getMyLocation();
    }

    public void getMyLocation() {
        if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        Task<Location> task = fusedLocationProviderClient.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),DEFAULT_ZOOM));
                //updateMyLocation();
                goDatabase();
                setMapLongClickEvent();
                findFriends(location);
                getAddress();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(ActivityCompat.checkSelfPermission(
                        MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(
                        MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });

    }
    public void goDatabase() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 정상적으로 toast가 실행되지 않아 주석처리 할수 밖에 없음 db에 update 되는것은 맞다
                        /*
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                Toast.makeText(MapsActivity.this, "내 위치 database에 업로드 성공", Toast.LENGTH_LONG);
                            } else {
                                Toast.makeText(MapsActivity.this, "database에 올리지 못하였습니다.", Toast.LENGTH_LONG);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        */
                    }
                };

                LocationRequest1 locationRequest1 = new LocationRequest1(userId, location, responseListener);
                RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
                queue.add(locationRequest1);
            }
        });
    }

    private void getAddress() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng point = marker.getPosition();
                GoogleGeocodingApi geocode = new GoogleGeocodingApi(MapsActivity.this, point, new GoogleRouteApi.EventListener() {
                    @Override
                    public void onApiResult(String result) {

                        MyAddress myAddress = GoogleGeocodingResultParser.getAddress(result);
                        showAddress(myAddress);
                    }

                    @Override
                    public void onApiFailed() {
                        Toast.makeText(MapsActivity.this, "요청을 실패하였습니다", Toast.LENGTH_LONG).show();
                    }
                });
                geocode.start();
                return true;
            }
        });
    }
    private void showAddress(MyAddress myAddress) {
        routeTextView.setText("");
        routeTextView.setText(myAddress.address + '\n' + myAddress.establishment);
    }
    private void setMapLongClickEvent() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("핀  "+ pinNumber);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_edit_location_black_24dp));
                mMap.addMarker(markerOptions);

                pinNumber = pinNumber + 1;
                getRoute(latLng);
            }
        });
    }
    private void getWeather() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Location lastLocation = location;
                LatLng point = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                WeatherApi weatherApi = new WeatherApi(MapsActivity.this, point, new GoogleRouteApi.EventListener() {
                    @Override
                    public void onApiResult(String result) {
                        Weather weather = WeatherResultParser.getWeather(result);
                        showWeather(weather);
                    }

                    @Override
                    public void onApiFailed() {
                        Toast.makeText(MapsActivity.this, "요청을 실패하였습니다", Toast.LENGTH_LONG).show();
                    }
                });
                weatherApi.start();
            }
        });
    }
    public void showWeather(Weather weather) {
        weatherTextView.setText("");
        weatherTextView.setText(String.format("%s %.3f",weather.weather, weather.temparature));
    }
    private void getRoute(final LatLng endPoint) {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Location lastLocation = location;
                LatLng startPoint = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                GoogleRouteApi googleRouteApi = new GoogleRouteApi(MapsActivity.this, startPoint, endPoint, new GoogleRouteApi.EventListener() {
                    @Override
                    public void onApiResult(String result) {
                        Toast.makeText(MapsActivity.this, "요청 성공 : ", Toast.LENGTH_LONG).show();
                        Route route = GoogleRouteResultParser.getRoute(result);
                        showRoute(route);
                    }

                    @Override
                    public void onApiFailed() {
                        Toast.makeText(MapsActivity.this, "요청을 실패하였습니다", Toast.LENGTH_LONG).show();
                    }
                });
                googleRouteApi.start();
            }
        });
    }

    public void showRoute(Route route) {
        if(route != null) {
            routeTextView.setText("");
            routeTextView.setText("총 걸리는 시간은 " + route.routeTime);

            for (Step step : route.steps) {
                routeTextView.append("\n\n");
                routeTextView.append(step.stepText);

                if (step.transitStopNumber > 0) {
                    routeTextView.append(" 승차 후 " + step.transitStopNumber + "개 정류소 에서 하차");

                } else {
                    routeTextView.append(" 승차");
                }
            }

        }
    }
    private double distance(Location location, LatLng latLng) {
        double theta = location.getLongitude() - latLng.longitude;
        double dist = Math.sin(deg2rad(location.getLatitude())) * Math.sin(deg2rad(latLng.latitude)) + Math.cos(deg2rad(location.getLatitude())) * Math.cos(deg2rad(latLng.latitude)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1609.344; // meter
        return dist;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private void updateMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create(); // 요청 설정 객체 생성
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL_TIME);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_TIME);

        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        }, null);
    }
}
