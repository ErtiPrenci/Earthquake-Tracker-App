package com.example.erdbeben_projekt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MapView map = null;
    private GeoPoint city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = getApplicationContext();
        org.osmdroid.config.Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        city = new GeoPoint(42.062681, 19.515363);

        IMapController mapController = map.getController();
        mapController.setZoom(9.5);
        mapController.setCenter(city);

        CompassOverlay compassOverlay = new CompassOverlay(this, map);
        compassOverlay.enableCompass();

        map.getOverlays().add(compassOverlay);

        String url = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&latitude=42.062681&longitude=19.515363&maxradiuskm=100&starttime=2018-11-01&minmagnitude=5";

        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Gson gson = new Gson();
                    Erdbeben erdbeben = gson.fromJson(response.toString(), Erdbeben.class);
                    List<Feature> erdbebeninformationen = erdbeben.getFeatures();
                    double latitude = 0.0;
                    double longitude = 0.0;

                    for (int i = 0; i < erdbebeninformationen.size(); i++) {
                        latitude = erdbebeninformationen.get(i).getGeometry().getCoordinates().get(1);
                        longitude = erdbebeninformationen.get(i).getGeometry().getCoordinates().get(0);

                        Marker erbebenmarker = new Marker(map);
                        erbebenmarker.setTitle("Magnitude: " + erdbebeninformationen.get(i).getProperties().getMag());
                        erbebenmarker.setPosition(new GeoPoint(latitude, longitude));
                        map.getOverlays().add(erbebenmarker);
                    }
                    Log.d("APILOG", "" + longitude);
                } catch (Exception e) {
                    Log.d("APILOG", e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}