package example.course.com.geocoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private TextView text = null;

    //messages from background thread contain data for UI
    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            String title =(String) msg.obj;
            text.append(title + "\n" +"\n");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text=(TextView)findViewById(R.id.texter);

        Thread t = new Thread(background);
        t.start();
    }

    //thread connects to Google Geocoder Api, gets response code, JSON search results,
    //places data into Log and sends messages to display data on UI
    Runnable background = new Runnable() {
        public void run(){

            StringBuilder builder = new StringBuilder();

            //String Url = "https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyCJsvtCg4Nma9eCunBVBTAjZHjD06sKqhQ"; //forward
            //String Url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=71.334,-23.99&key=AIzaSyCJsvtCg4Nma9eCunBVBTAjZHjD06sKqhQ";  //reverse
             String Url = "https://maps.googleapis.com/maps/api/geocode/json?address=White+House&key=AIzaSyCS5oTZdCQwzz1lYbcM1qGxWVejYczjl7M";  //forward
            InputStream is = null;

            try {
                URL url = new URL(Url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.e("JSON", "The response is: " + response);
                //if response code not 200, end thread
                if (response != 200) return;
                is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            }	catch(IOException e) {}
            finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch(IOException e) {}
                }
            }

            //convert StringBuilder to String
            String readJSONFeed = builder.toString();
            Log.e("JSON", readJSONFeed);

            //decode JSON
            try {
                JSONObject obj = new JSONObject(readJSONFeed);
                String addr = obj.getString("formatted_address");
                Log.i("JSON", "formatted_address " + addr);
                String total = obj.getString("totalItems");
                Log.i("JSON", "totalItems " + total);
                JSONArray jsonArray = new JSONArray();
                jsonArray = obj.getJSONArray("items");
                Log.i("JSON",
                        "Number of entries " + jsonArray.length());

                //for each array item get title and date
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject volumeInfo = jsonObject.getJSONObject("volumeInfo");
                    String title = volumeInfo.getString("title");

                    Message msg = handler.obtainMessage();
                    msg.obj = title;
                    handler.sendMessage(msg);

                    Log.i("JSON", title);
                    String date = volumeInfo.getString("publishedDate");
                    Log.i("JSON", date);
                }
            } catch (JSONException e) {e.getMessage();
                e.printStackTrace();
            }
        }

    };



}
