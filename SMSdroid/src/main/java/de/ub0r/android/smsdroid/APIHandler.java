package de.ub0r.android.smsdroid;

import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class APIHandler extends Thread{

    public String phoneNumber=null;
    public int type;
    public static String APIResponse;
    public static String Query;

    public static final int LOCATION = 0, COST = 1, DRIVERS = 2;

    public APIHandler( int Type )
    {
        type = Type;
    }


    @Override
    public void run() {


        if( type == LOCATION ) {


            HttpURLConnection urlConnection = null;
            try {
                String url_str = "https://nominatim.openstreetmap.org/search.php?q=";
                for (String str : Query.split(" ")) {
                    url_str += "+" + str;
                }
                url_str += "&format=jsonv2";
                URL url = new URL(url_str);
                urlConnection = (HttpURLConnection) url.openConnection();
                String smsBody = ""; //urlConnection.getResponseMessage();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String strCurrentLine;

                while ((strCurrentLine = br.readLine()) != null) {
                    smsBody += (strCurrentLine);
                }

                if (smsBody.length() < 5) {
                    throw new Exception();
                }

                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) ((JsonArray) jsonParser.parse(smsBody)).get(0);
                //arr.toString();
                APIResponse = jsonObject.get("display_name").toString() +"_"+ jsonObject.get("lat").toString() +"_"+ jsonObject.get("lon").toString();

                String temp = "";
                for( char c : APIResponse.toCharArray() )
                {
                    if( c == '"' )
                    {
                        continue;
                    }
                    temp += c ;
                }
                APIResponse = temp;
                Log.d("MESSAGE", "APIResponse"+APIResponse);
            } catch (Exception e) {
                Log.d("MESSAGE", e.getMessage());
                APIResponse = "ERROR";
            } finally {
                urlConnection.disconnect();
            }


        }
        else if(type == COST)
        {

            HttpURLConnection urlConnection = null;
            try {
                String url_str = "https://omnibook.azurewebsites.net/api/Cost/";
                for (String str : Query.split(" ")) {
                    url_str += str + "|";
                }
                URL url = new URL(url_str);
                urlConnection = (HttpURLConnection) url.openConnection();
                String smsBody = ""; //urlConnection.getResponseMessage();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String strCurrentLine;

                while ((strCurrentLine = br.readLine()) != null) {
                    smsBody += (strCurrentLine);
                }

                if (smsBody.length() < 5) {
                    throw new Exception();
                }

                APIResponse = smsBody;
            } catch (Exception e) {
                Log.d("MESSAGE", e.getMessage());
                APIResponse = "ERROR";
            } finally {
                urlConnection.disconnect();
            }

        }
        else if( type == DRIVERS )
        {

            HttpURLConnection urlConnection = null;
            try {
                String url_str = "https://omnibook.azurewebsites.net/api/QueryHandler/0";
                URL url = new URL(url_str);
                urlConnection = (HttpURLConnection) url.openConnection();
                String smsBody = ""; //urlConnection.getResponseMessage();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String strCurrentLine;

                while ((strCurrentLine = br.readLine()) != null) {
                    smsBody += (strCurrentLine);
                }

                if (smsBody.length() < 5) {
                    throw new Exception();
                }

                APIResponse = smsBody;
            } catch (Exception e) {
                Log.d("MESSAGE", e.getMessage());
                APIResponse = "ERROR";
            } finally {
                urlConnection.disconnect();
            }
        }
    }
}
