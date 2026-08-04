package com.example.slidesave;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient locationClient;

    private static final int LOCATION_REQUEST = 100;
    private static final String API_KEY = "Enter your Google Place API key here";

    private RecyclerView rvAuthorities;
    private AuthorityAdapter authorityAdapter;
    private List<Authority> authorityList = new ArrayList<>();

    private LatLng currentLatLng;
    private Circle userRangeCircle;
    private EditText etSearchLocation;
    private ImageButton btnSearch;
    private ImageButton btnClearSearch;
    private com.google.android.gms.maps.model.Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        rvAuthorities = findViewById(R.id.rvAuthorities);
        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        setupRecyclerView();

        btnSearch.setOnClickListener(v -> searchLocation());

        btnClearSearch.setOnClickListener(v -> {
            etSearchLocation.setText("");
            etSearchLocation.clearFocus();
            goToCurrentLocation();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_maps);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (id == R.id.nav_maps) {
                return true;
            }
            return false;
        });
    }

    // Initialize map and obtain user's current location
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 160, 0, 260);

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true); //Enable user's location on map
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLatLng = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        //Display 1 km monitoring radius
                        userRangeCircle = mMap.addCircle(
                                new CircleOptions()
                                        .center(currentLatLng)
                                        .radius(1000)
                                        .strokeColor(0xFF87CEEB)
                                        .fillColor(0x5587CEEB)
                                        .strokeWidth(3)
                        );
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                        searchNearbyAuthorities();
                    }
                });
    }

    //Search for a user-specified location
    private void searchLocation() {
        String locationName = etSearchLocation.getText().toString().trim();
        etSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchLocation();  // trigger function
                return true;
            }
            return false;
        });

        if (locationName.isEmpty()) {
            Toast.makeText(this, "Enter location", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocationName(locationName, 1);

            if (list != null && !list.isEmpty()) {
                Address address = list.get(0);
                currentLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.clear();
                authorityList.clear();

                if (userRangeCircle != null) {
                    userRangeCircle.remove();
                }

                mMap.addMarker(new MarkerOptions().position(currentLatLng).title(locationName));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                userRangeCircle = mMap.addCircle(
                        new CircleOptions()
                                .center(currentLatLng)
                                .radius(1000)
                                .strokeColor(0xFF87CEEB)
                                .fillColor(0x5587CEEB)
                                .strokeWidth(3)
                );
                searchNearbyAuthorities();
            } else {
                Toast.makeText(this,"Location not found",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) return;
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            authorityList.clear();

            if (userRangeCircle != null) {
                userRangeCircle.remove();
            }

            userRangeCircle = mMap.addCircle(
                    new CircleOptions()
                            .center(currentLatLng)
                            .radius(1000)
                            .strokeColor(0xFF87CEEB)
                            .fillColor(0x5587CEEB)
                            .strokeWidth(3)
            );

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            searchNearbyAuthorities();
        });
    }

    //Recycler
    private void setupRecyclerView() {
        authorityAdapter = new AuthorityAdapter(authorityList,
                new AuthorityAdapter.OnAuthorityClickListener() {
                    //Dial selected authorities' phone number
                    @Override
                    public void onAuthoritySelected(Authority authority) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        // if null, place 999
                        String phone = authority.phoneNumber != null
                                ? authority.phoneNumber
                                : "999";
                        intent.setData(Uri.parse("tel:" + phone));
                        startActivity(intent);
                    }

                    @Override
                    public void onAuthorityFocused(Authority authority) {
                        if (selectedMarker != null) {
                            selectedMarker.hideInfoWindow();
                        }
                        selectedMarker = authority.marker;

                        if (selectedMarker != null) {
                            selectedMarker.showInfoWindow();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    authority.location,17f),
                                    700,
                                    null
                            );
                        }
                    }
                });

        rvAuthorities.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        rvAuthorities.setAdapter(authorityAdapter);
        rvAuthorities.setNestedScrollingEnabled(true);
        rvAuthorities.setHasFixedSize(true);
        rvAuthorities.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
    }

    // Search nearby authorities location
    private void searchNearbyAuthorities() {
        if (currentLatLng == null) return;
        authorityList.clear();
        String[] types = {"police", "fire_station", "hospital"};

        for (String type : types) {
            String url =
                    "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                            + "location=" + currentLatLng.latitude + "," + currentLatLng.longitude
                            + "&radius=3000&type=" + type
                            + "&key=" + API_KEY;

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,null,response -> {
                        try {
                            JSONArray results = response.getJSONArray("results");

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject obj = results.getJSONObject(i);
                                JSONObject loc = obj.getJSONObject("geometry").getJSONObject("location");
                                LatLng latLng = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

                                String name = obj.getString("name");
                                System.out.println(name);
                                String lowerName = name.toLowerCase();

                                if (type.equals("police")) {
                                    if (!lowerName.contains("police")
                                            && !lowerName.contains("polis")) {
                                        continue;
                                    }
                                }else if (type.equals("fire_station")) {
                                    if (!lowerName.contains("fire")
                                            && !lowerName.contains("bomba")
                                            && !lowerName.contains("rescue")) {
                                        continue;
                                    }
                                }else if (type.equals("hospital")) {
                                    if (!lowerName.contains("hospital")
                                            && !lowerName.contains("medical")
                                            && !lowerName.contains("clinic")
                                            && !lowerName.contains("klinik")
                                            && !lowerName.contains("pusat kesihatan")) {
                                        continue;
                                    }
                                }

                                String placeId = obj.getString("place_id");
                                float color;
                                switch (type) {
                                    case "police":
                                        color = BitmapDescriptorFactory.HUE_BLUE;
                                        break;

                                    case "fire_station":
                                        color = BitmapDescriptorFactory.HUE_RED;
                                        break;

                                    case "hospital":
                                        color = BitmapDescriptorFactory.HUE_VIOLET;
                                        break;

                                    default:
                                        color = BitmapDescriptorFactory.HUE_ORANGE;
                                }

                                Marker marker = mMap.addMarker(
                                        new MarkerOptions()
                                                .position(latLng)
                                                .title(name)
                                                .icon(BitmapDescriptorFactory.defaultMarker(color))
                                );

                                float[] dist = new float[1];

                                Location.distanceBetween(
                                        currentLatLng.latitude,currentLatLng.longitude,
                                        latLng.latitude,latLng.longitude,dist
                                );

                                Authority authority = new Authority(
                                        name,type,latLng,dist[0] / 1000f,placeId
                                );
                                authority.marker = marker;
                                authorityList.add(authority);
                                getAuthorityPhone(authority);
                            }

                            if (mMap == null) return;
                            authorityAdapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Failed to load authorities", Toast.LENGTH_SHORT)
                            .show());
            queue.add(request);
        }
    }

    private void getAuthorityPhone(Authority authority) {
        String url =
                "https://maps.googleapis.com/maps/api/place/details/json?"
                        + "place_id=" + authority.placeId
                        + "&fields=formatted_phone_number"
                        + "&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        response -> {

                            try {
                                JSONObject result = response.getJSONObject("result");

                                if(result.has("formatted_phone_number")){
                                    authority.phoneNumber = result.getString("formatted_phone_number");
                                }
                                authorityAdapter.notifyDataSetChanged();
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        },
                        error -> { }
                );
        queue.add(request);
    }
    private int getMarkerIcon(String type) {
        switch (type) {
            case "police":
                return R.drawable.ic_marker_police;
            case "fire_station":
                return R.drawable.ic_marker_fire;
            case "hospital":
                return R.drawable.ic_marker_hospital;
            default:
                return R.drawable.ic_marker_police;
        }
    }
}