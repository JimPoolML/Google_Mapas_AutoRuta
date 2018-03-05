package appjpm4everyone.mapas_google;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int LOCALIZAR_OK = 500;

    //Creo un array que contegnga todas loas posiciones de las marcas
    ArrayList<LatLng> puntosCardinales;

    //Creo las instancias de los objetos de XML u otros
    Button Bmapa;
    Button Bterreno;
    Button Bhibrido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Casting de objetos de XML, y los objetos crados para los mapas
        Bmapa = (Button) findViewById(R.id.btnmapa);
        Bterreno = (Button) findViewById(R.id.btnterreno);
        Bhibrido = (Button) findViewById(R.id.btnhibrido);
        puntosCardinales = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        /*
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        LatLng sydney = new LatLng(4.590830, -74.174142);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */

        //Obtiene la posicion de mi dispositivo
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //Añado esto al código, para obtener permisos de localización
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCALIZAR_OK);

            return;
        }
        mMap.setMyLocationEnabled(true);


        //Listener para el método LongClickListener
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                //Reset marker cuando hay más de dos marcas
                if (puntosCardinales.size() == 2) {
                    puntosCardinales.clear();
                    mMap.clear();
                }

                //Forma de añadir una marca personalisada, coloca distintos iconos
                if(puntosCardinales.size() % 2 == 0) {
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .anchor(0.0f, 1.0f)
                            .position(latLng));
                }
                else{
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.embarazada))
                            .anchor(0.0f, 1.0f)
                            .position(latLng));
                }

                //Añado a la lista la marca creada
                puntosCardinales.add(latLng);
                //En un Toast digo el numero de la marca añadida
                Toast.makeText(getApplicationContext(),"Marca # "+ puntosCardinales.size(),Toast.LENGTH_LONG).show();

                //Creo la URL para obtener la distancia entra la marca1 y la marca 2
                if(puntosCardinales.size() >= 2) {
                    String url = getRequestUrl(puntosCardinales.get(0), puntosCardinales.get(1));
                    TaskRequestDirections taskRequestDirection = new TaskRequestDirections();
                    taskRequestDirection.execute(url);
                }

            }
        });

        //Listener cuando presiono una marca
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(), "Has pulsado una marca", Toast.LENGTH_LONG).show();
                return false;
            }
        });

    }//Final void onMapReady

    private String getRequestUrl(LatLng origen, LatLng destino) {
        //Valor del origen
        String str_org = "origin=" + origen.latitude +","+ origen.longitude;
        //Valor del destino
        String str_dest = "destination=" + destino.latitude +","+ destino.longitude;
        //Valor del sensor
        String sensor = "sensor=false";
        //Modo de encontrat la direccion (automovil, bicicleta, caminando)
        String mode = "mode=driving";
        //Construyo la cadena completa
        String parametros = str_org +"&"+ str_dest +"&"+ sensor +"&"+ mode;
        //Formato de salida
        String output = "json";
        //Formato de la direccion url
        //https://maps.googleapis.com/maps/api/directions/
        // enlace="https://maps.googleapis.com/maps/api/geocode/json?latlng=";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output +"?"+ parametros;
        return  url;

    }

    private  String respuestaDireccion(String respURL) throws IOException {
        String respuestaString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        //Método try--Catch
        try{
            URL url = new URL(respURL);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Obtiene respuesta de la conexion
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String linea = "";

            while ((linea = bufferedReader.readLine())  !=  null){
                stringBuffer.append(linea);
            }

            respuestaString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null){
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return respuestaString; //Retorna el String para tratarlo ucon un Json
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            String respuestaString = "";
            try{
                respuestaString = respuestaDireccion(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return respuestaString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //parse Json aquí; se analiza el Json obtenido por la respuesta de la URL enviada
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }


    }//Final public class TaskRequestDirections


    public class TaskParser extends  AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            //Tratamiento del Objeto Json
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> rutas = null;
            try{
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                rutas = directionsParser.parse(jsonObject);
            }catch (JSONException e) {
                e.printStackTrace();
            }
            //Retorna la ruta entre las dos marcas
            return  rutas;
        }//Final doInBackground

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Obtiene la ruta entre las 2 marcas y la dibuja en el MAPA

            ArrayList puntos = null;
            //Polilineas para trazar la ruta
            PolylineOptions polylineOptions = null;

            //Ciclo for prara trazar linea por linea
            for ( List<HashMap<String, String>> paht : lists ) {
                puntos = new ArrayList();
                polylineOptions = new PolylineOptions();

                //For anidado para obtener las distintas cordenadas entre las lineas conectadas
                for ( HashMap<String, String> points : paht) {
                    double lat = Double.parseDouble(points.get("lat"));
                    double lon = Double.parseDouble(points.get("lon"));
                    puntos.add(new LatLng(lat,lon));
                }//Final 2do for

                polylineOptions.addAll(puntos);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);

            } //Final 1er for

            if (polylineOptions!=null) {
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Dirección no encontrada!", Toast.LENGTH_SHORT).show();
            }

        }//Final PostExecute


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCALIZAR_OK:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true); //Agrego un MissingPermission
                }
            break;

        }
    }//Final void onRequestPermissionsResult

    public void ClickVista(View view) {
        //switch case para cada boton
        switch (view.getId()){

            case R.id.btnmapa:
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            break;

            case R.id.btnhibrido:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            case R.id.btnterreno:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

            case R.id.btninterior:
                //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                //Mueve el mapa la la sigueinte posición
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        //(Latitud, Longitud), Profundidad edificio
                     new LatLng(-33.86997, 151.2089), 18));
                break;


        }


    }
}
