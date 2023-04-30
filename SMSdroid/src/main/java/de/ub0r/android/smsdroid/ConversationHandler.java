package de.ub0r.android.smsdroid;

import android.telephony.SmsManager;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;

public class ConversationHandler extends Thread {

    public static int counter = 0;
    public static HashMap<String,Integer> Conversations = new HashMap<>();
    public static HashMap<String,UserStruct> UserValues = new HashMap<>();
    public String message , phoneNumber ;

    public ConversationHandler( String m , String p )
    {
        message = m;
        phoneNumber = p;
    }

    public static void HandleConversation( String message , String phoneNumber )
    {
        if( !Conversations.containsKey(phoneNumber) )
        {
            Conversations.put(phoneNumber,7);
            UserValues.put(phoneNumber,new UserStruct());
        }

        String smsResponse;

        switch( Conversations.get(phoneNumber) )
        {
            case 7:
                sendSMS( "OB : Welcome to OmniBook!\n\nEnter 1 to begin." , phoneNumber );
                Conversations.put(phoneNumber,0);
                break;
            case 0:
                if( message.compareToIgnoreCase("1")==0 )
                {
                    sendSMS( "OB : Enter Pick up Location (specific landmark)" , phoneNumber );
                    Conversations.put(phoneNumber,1);
                }
                else
                {
                    sendSMS( "OB : Invalid input.\n\nSession Reset." , phoneNumber );
                    Conversations.put(phoneNumber,7);
                }
                break;
            case 1:
                String pickup_location="-";
                APIHandler pickup_api = new APIHandler(APIHandler.LOCATION);
                pickup_api.Query = message;
                pickup_api.APIResponse = "ERROR";
                pickup_api.start();
                try {
                    pickup_api.join();
                    String[] res = pickup_api.APIResponse.split("_");
                    for( int i = 0 ; i < res.length ; i++)
                    {
                        Log.d("MESSAGE" , "-->"+res[i]);
                    }
                    pickup_location = res[0] ;
                    UserValues.get(phoneNumber).SLat = res[1].trim();
                    UserValues.get(phoneNumber).SLong = res[2].trim();
                }
                catch(Exception e)
                {
                    Log.d("MESSAGE", e.getMessage());
                    pickup_location = "Error Couldn't get Location";
                }

                if( pickup_api.APIResponse.compareTo("ERROR") != 0 ) {
                    Log.d("MESSAGE",pickup_location);
                    sendSMS( pickup_location , phoneNumber );
                    sendSMS( "OB : Enter 1 to accept pick up location\n\nEnter 2 to re-enter\n\nEnter 0 to exit" , phoneNumber );
                    Conversations.put(phoneNumber, 2);
                }
                else
                {
                    sendSMS( "OB : Couldn't Find Location.\n\nSession Reset." , phoneNumber );
                    Conversations.put(phoneNumber,7);
                }
                break;
            case 2:
                if( message.compareToIgnoreCase("1")==0 )
                {
                    sendSMS( "OB : Enter Drop off Location (specific landmark)" , phoneNumber );
                    Conversations.put(phoneNumber,3);
                }
                else if(message.compareToIgnoreCase("2")==0)
                {
                    sendSMS( "OB : Enter Pick up Location (specific landmark)" , phoneNumber );
                    Conversations.put(phoneNumber,1);
                }
                else if(message.compareToIgnoreCase("0")==0)
                {
                    sendSMS( "OB : Thanks for using Omnibook!" , phoneNumber );
                    Conversations.put(phoneNumber,7);
                }
                break;
            case 3:
                String drop_location="-";
                APIHandler drop_api = new APIHandler(APIHandler.LOCATION);
                drop_api.Query = message;
                drop_api.APIResponse = "ERROR";
                drop_api.start();
                try {
                    drop_api.join();
                    String[] res = drop_api.APIResponse.split("_");
                    drop_location = res[0];
                    UserValues.get(phoneNumber).DLat = res[1].trim();
                    UserValues.get(phoneNumber).DLong = res[2].trim();
                }
                catch(Exception e)
                {
                    drop_location = "Error Couldn't get Location";
                }

                if( drop_api.APIResponse != "ERROR" ) {
                    sendSMS( drop_location , phoneNumber );
                    sendSMS( "OB : Enter 1 to accept drop location\n\nEnter 2 to re-enter\n\nEnter 0 to exit" , phoneNumber );
                    Conversations.put(phoneNumber, 4);
                }
                else
                {
                    sendSMS( "OB : Couldn't Find Location.\n\nSession Reset." , phoneNumber );
                    Conversations.put(phoneNumber,7);
                }
                break;
            case 4:
                if( message.compareToIgnoreCase("1")==0 )
                {
                    try {
                        String cost;
                    APIHandler cost_api = new APIHandler(APIHandler.COST);
                    String query = "" ;
                    UserStruct data = UserValues.get(phoneNumber);
                    query += data.SLat + "|" ;
                    query += data.SLong + "|" ;
                    query += data.DLat + "|" ;
                    query += data.DLong + "|" ;
                    cost_api.Query = query;
                    cost_api.APIResponse = "ERROR";
                    cost_api.run();

                        cost_api.join();
                        cost = cost_api.APIResponse;

                        sendSMS( "OB : Expected cost of journey : INR " + cost + "\n\nEnter 1 to proceed\n\nEnter 0 to exit." , phoneNumber );
                        Conversations.put(phoneNumber,5);
                    }
                    catch(Exception e)
                    {
                        sendSMS( "OB : Couldn't Calculate Cost.\n\nSession Reset." , phoneNumber );
                        Conversations.put(phoneNumber,7);
                    }
                }
                else if(message.compareToIgnoreCase("2")==0)
                {
                    sendSMS( "OB : Enter Drop Location (specific landmark)" , phoneNumber );
                    Conversations.put(phoneNumber,3);
                }
                else if(message.compareToIgnoreCase("0")==0)
                {
                    sendSMS( "OB : Thanks for using Omnibook!" , phoneNumber );
                    Conversations.put(phoneNumber,7);
                }
                break;
            case 5:
                if( message.compareToIgnoreCase("1")==0 )
                {


                    try {
                        String drivers;
                        APIHandler driver_api = new APIHandler(APIHandler.DRIVERS);
                        driver_api.Query = message;
                        driver_api.APIResponse = "ERROR";
                        driver_api.run();

                        driver_api.join();
                        drivers = driver_api.APIResponse;

                        String driverList = "OB : List of Local Autos : \n";

                        for( String driver : drivers.split(",") )
                        {
                            String[] driver_data = driver.split(":");
                            driverList += driver_data[1] + " : " + driver_data[2] + "\n" ;
                        }

                        sendSMS( driverList , phoneNumber );
                        Conversations.put(phoneNumber,3);
                        sendSMS( "OB : Thanks for using Omnibook!" , phoneNumber );
                        Conversations.put(phoneNumber,7);
                    }
                    catch(Exception e)
                    {
                        sendSMS( "OB : Couldn't Find Drivers.\n\nSession Reset." , phoneNumber );
                        Conversations.put(phoneNumber,7);
                    }

                }
                else if(message.compareToIgnoreCase("0")==0)
                {
                    sendSMS( "OB : Thanks for using Omnibook!" , phoneNumber );
                    Conversations.put(phoneNumber,7);
                }
                else
                {
                    sendSMS( "OB : Invalid input.\n\nSession Reset." , phoneNumber );
                    Conversations.put(phoneNumber,7);
                }
                break;

        }


    }

    static void sendSMS( String smsBody , String phoneNumber )
    {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, smsBody, null, null);
    }


    @Override
    public void run() {
        HandleConversation(message,phoneNumber);
    }

}
