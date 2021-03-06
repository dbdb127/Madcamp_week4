package com.example.travel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;


import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.travel.items.SavePathInput;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,RoutingListener,GoogleApiClient.OnConnectionFailedListener{

    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private Integer mainflag = MainActivity.mainflag;
    private Integer mainchangeflag = MainActivity_ImageChange.mainchangeflag;

    private GoogleMap mMap;
    private Geocoder geocoder;

    private FloatingActionButton button ;

    private EditText editText;
    private String title;

    Polyline polyline = null;
    List<LatLng> latLngList = new ArrayList<>();
    private ArrayList<UserLocation> clickedPath = new ArrayList<>();


    private List<Polyline> polylines = null;

    private String place; // ?????? ?????????????????? ?????? ????????? ??????????????? ?????? ??????
    private ArrayList<String> flist;

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    public static String BASE_URL = LoginActivity.BASE_URL;
    private String useremail = MainActivity.useremail;



    private FloatingActionButton mainbtn, bt_searchpath , bt_savepath, tocal;
    private Animation fabOpen, fabClose, rotateForward, rotateBackward;
    private boolean isOpen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        place = intent.getStringExtra("place");
        flist = (ArrayList<String>)intent.getSerializableExtra("friendlist");

        mainbtn = findViewById(R.id.mainbtn);
        tocal = findViewById(R.id.toCalendar);

        editText = (EditText) findViewById(R.id.editText);
        button=(FloatingActionButton) findViewById(R.id.button);
        bt_searchpath = findViewById(R.id.bt_pathsearch);
        bt_savepath = findViewById(R.id.bt_pathsave);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);

        mainbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        if(mGeoApiContext == null){
//            mGeoApiContext = new GeoApiContext.Builder()
//                    .apiKey("AIzaSyDCsK1Y92o6guzI4h0jmFHPc6Yz43EUENE")
//                    .build();
//        }

        mainbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();
            }
        });

        //?????? ?????? ??????
        bt_searchpath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();

                if(polyline != null) polyline.remove(); // ?????? ?????? ??????????????? ???????????? ??????
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(latLngList).clickable(true)
                        .color(Color.parseColor("#767676"));
                polyline = mMap.addPolyline(polylineOptions);
//                if(latLngList.size() >= 3) {
//                    for (int i = 0; i < latLngList.size() - 2; i++) {
//                        calculateDirections(latLngList.get(i) , latLngList.get(i+1));
//                        //Findroutes(latLngList.get(i), latLngList.get(i + 1));
//                    }
//                }else if(latLngList.size() == 2){
//                    //Findroutes(latLngList.get(0) , latLngList.get(1));
//                    calculateDirections(latLngList.get(0) , latLngList.get(1));
//                }else if(latLngList.size() == 1){
//                    Toast.makeText(MapActivity.this, "????????? ?????? ?????? ??????????????????", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        bt_savepath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();

                entertitle();
            }
        });

        tocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();
                Intent intent = new Intent(getApplicationContext(), JoinCalendarActivity.class);
                intent.putExtra("friendlist" , flist);
                intent.putExtra("place" ,place );
                startActivity(intent);

                overridePendingTransition(R.anim.anim_slide_in_bottom, 0);
            }
        });
    }

    private void animateFab() {
        if(isOpen){
            mainbtn.startAnimation(rotateForward);
            bt_savepath.startAnimation(fabClose);
            bt_searchpath.startAnimation(fabClose);
            tocal.startAnimation(fabClose);

            bt_searchpath.setVisibility(View.GONE);
            bt_savepath.setVisibility(View.GONE);
            tocal.setVisibility(View.GONE);

            isOpen=false;
        }
        else{
            mainbtn.startAnimation(rotateBackward);

            bt_savepath.startAnimation(fabOpen);
            bt_searchpath.startAnimation(fabOpen);
            tocal.startAnimation(fabOpen);

            bt_searchpath.setVisibility(View.VISIBLE);
            bt_savepath.setVisibility(View.VISIBLE);
            tocal.setVisibility(View.VISIBLE);

            isOpen=true;
        }

    }

//    private void calculateDirections(LatLng start , LatLng end){
//        Log.d(TAG, "calculateDirections: calculating directions.");
//
//        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
//                end.latitude,
//                end.longitude
//        );
//        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
//
//        directions.alternatives(true);
//        directions.origin(
//                new com.google.maps.model.LatLng(
//                        start.latitude,
//                        start.longitude
//                )
//        );
//        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
//        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
//            @Override
//            public void onResult(DirectionsResult result) {
//                Log.d(TAG, "calculate ???");
//                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
//                Log.d(TAG, "onResult: duration :" + result.routes[0].legs[0].duration);
//                Log.d(TAG,  "onResult: distance :" +result.routes[0].legs[0].distance);
//                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
//
//                addPolylinesToMap(result);
//            }
//
//            @Override
//            public void onFailure(Throwable e) {
//                Log.e(TAG, "onFailure: " + e.getMessage() );
//            }
//        });
//    }
//    private void addPolylinesToMap(final DirectionsResult result){
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "run: result routes: " + result.routes.length);
//
//                for(DirectionsRoute route: result.routes){
//                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
//                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
//
//                    List<LatLng> newDecodedPath = new ArrayList<>();
//
//                    // This loops through all the LatLng coordinates of ONE polyline.
//                    for(com.google.maps.model.LatLng latLng: decodedPath){
//
////                        Log.d(TAG, "run: latlng: " + latLng.toString());
//
//                        newDecodedPath.add(new LatLng(
//                                latLng.lat,
//                                latLng.lng
//                        ));
//                    }
//                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
//                    polyline.setColor(R.color.colorPrimaryDark);
//                    polyline.setClickable(true);
//
//                }
//            }
//        });
//    }

    private AlertDialog ad;
    private void entertitle() {
        View view = getLayoutInflater().inflate(R.layout.enter_title, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        ad = builder.create();
        builder.show();

        Button finalsave = view.findViewById(R.id.bt_finalsavepath);
        final EditText pathTitle = view.findViewById(R.id.inputTitle);

        finalsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //????????? ?????? ???????????????, ?????? , ??????????????? ??????
//                Log.d("Check" , useremail);
                ArrayList<String> tmpParti = new ArrayList<>();
                //tmpParti.add(MainActivity.useremail);
                for(int i=0;i<flist.size();i++){
                    tmpParti.add(flist.get(i));
                    Log.d("kyung", i+flist.get(i)+"");
                }
                SavePathInput savePathInput = new SavePathInput(tmpParti, pathTitle.getText().toString() ,place , String.valueOf(clickedPath.size()), clickedPath);
                title = pathTitle.getText().toString();

                Call<Void> call = retrofitInterface.executeSavePath(savePathInput);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.code() == 200) {
                            ad.cancel();

                            ActivityManager activity_manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

                            List<ActivityManager.RunningTaskInfo> task_info = activity_manager.getRunningTasks(9999);

                            for(int i=0; i<task_info.size(); i++) {

                                Log.d("log", "[" + i + "] activity:"+ task_info.get(i).topActivity.getPackageName() + " >> " + task_info.get(i).topActivity.getClassName());

                            }



                            Intent intent = new Intent(MapActivity.this, MainActivity_ImageChange.class);
                            startActivity(intent);
                            MainActivity mainActivity = MainActivity.mainActivity;
                            MainActivity_ImageChange mainActivity_imageChange = MainActivity_ImageChange.mainActivity_imageChange;

                            Log.d("main", mainflag+" "+mainchangeflag);
                            if(mainflag==1){
                                mainActivity.finish();
                            }
                            if(mainchangeflag==1){
                                mainActivity_imageChange.finish();
                            }

                            finish();
                            ad.dismiss();
                        } else if (response.code() == 400) {

                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                });

            }
        });
    }

//    public void Findroutes(LatLng Start, LatLng End)
//    {
//        if(Start==null || End==null) {
//            Toast.makeText(MapActivity.this ,"Unable to get location", Toast.LENGTH_LONG).show();
//        }
//        else
//        {
//
//            Routing routing = new Routing.Builder()
//                    .travelMode(AbstractRouting.TravelMode.DRIVING)
//                    .withListener(this)
//                    .alternativeRoutes(true)
//                    .waypoints(Start, End)
//                    .key("AIzaSyDCsK1Y92o6guzI4h0jmFHPc6Yz43EUENE")  //also define your api key here.
//                    .build();
//            routing.execute();
//        }
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);
        // ??? ?????? ????????? ?????? //
//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
//            @Override
//            public void onMapClick(LatLng point) {
//                MarkerOptions mOptions = new MarkerOptions();
//                // ?????? ?????????
//                mOptions.title("????????? ??????");
//                Double latitude = point.latitude; // ??????
//                Double longitude = point.longitude; // ??????
//                // ????????? ?????????(????????? ?????????) ??????
//                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
//                // LatLng: ?????? ?????? ?????? ?????????
//                mOptions.position(new LatLng(latitude, longitude));
//                // ??????(???) ??????
//                googleMap.addMarker(mOptions);
//            }
//        });
        // ????????? ???????????? ????????? ????????? ??? ????????? ????????? ?????????
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                String str = editText.getText().toString();
                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocationName(
                            str, // ??????
                            10); // ?????? ?????? ?????? ??????
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                if(addressList.size() != 0) {
                    System.out.println(addressList.get(0).toString());
                    // ????????? ???????????? split
                    String[] splitStr = addressList.get(0).toString().split(",");
                    String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // ??????
                    System.out.println(address);

                    String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // ??????
                    String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // ??????
                    System.out.println(latitude);
                    System.out.println(longitude);

                    // ??????(??????, ??????) ??????
                    LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                    MarkerOptions mOptions2 = new MarkerOptions();
                    mOptions2.title(str);
                    mOptions2.snippet(address);
                    mOptions2.position(point);

                    Random random = new Random();
                    mOptions2.icon(BitmapDescriptorFactory.defaultMarker(random.nextFloat() * 360));
                    mMap.addMarker(mOptions2);

                    //????????? ????????? ????????? ??????
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));

                    //????????? ?????? clickedPath??? ??????.(?????????, ??????, ??????) ?????? db??? ?????? ?????????
                    //Location ??? ????????? ?????? ????????????
                    UserLocation location = new UserLocation(str, latitude, longitude);
                    clickedPath.add(location);

                    //?????????(???????????????) ????????? ?????? (??????,??????) ???????????? ?????????. LatLng??? ?????? ????????? ????????????
                    LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    latLngList.add(latLng);
                }else{
                    Toast.makeText(MapActivity.this, "???????????? ????????? ??????????????????. ( ex : ????????? , ????????? ?????? )", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Add a marker in Sydney and move the camera
//        LatLng Seoul = new LatLng(36.5680281276506, 127.68838295507976);
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Seoul, 7));
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocationName(
                    place, // ??????
                    10); // ?????? ?????? ?????? ??????
            //Log.d("tag", "?????? ?????? ?????? : " + addressList.size());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if(addressList.size() != 0) {
            // ????????? ???????????? split
            String[] splitStr = addressList.get(0).toString().split(",");
            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // ??????
            String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1);
            LatLng startCity = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startCity, 13));
        }else{
            Toast.makeText(this, "????????? ???????????? ????????????", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(MapActivity.this,"Finding Route...",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
//        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
//        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if(polylines!=null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(getResources().getColor(R.color.colorPrimary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);

            }
            else {

            }

        }

        //Add Marker on route starting position
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("My Location");
        mMap.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        mMap.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
        Toast.makeText(this, "onRoutingCancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    private long time= 0;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "????????? ????????? ??????????????????????", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {
            finish();
        }
    }

}