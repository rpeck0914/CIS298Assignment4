package edu.kvcc.cis298.cis298assignment4;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by Robert Peck on 12/5/2015.
 */
public class ServerRequests {
    //Class to run server requests.

    //Creates a locale variable for a progress dialog.
    ProgressDialog mProgressDialog;

    //Static variables for setting the connection timeout and address to connect to.
    public static final int CONNECTION_TIMEOUT = 1000 * 15;
    public static final String SERVER_ADDRESS = "http://barnesbrothers.homeserver.com/beverageapi";

    //Constructor that sets the parameters of the progress dialog.
    public ServerRequests(Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("Processing...");
        mProgressDialog.setMessage("Please Wait...");
    }

    //Public method to execute the async class.
    public void fetchBeveragesAsyncTask(BeverageCallback beverageCallback) {
        //Displays the progress dialog.
        mProgressDialog.show();
        //Creates a new instance of the FetchBeveragesAsync class and executes it.
        new FetchBeveragesAsyncTask(beverageCallback).execute();
    }

    private class FetchBeveragesAsyncTask extends AsyncTask<Void, Void, Boolean> {
        //Class to fetch the beverages from the web, class extends Async Task.

        //Private variable for the beverage singleton.
        private BeverageCollection mBeverageCollection;
        //Private variable for the interface call back.
        private BeverageCallback mBeverageCallback;

        //Constructor that takes the sent over call back and sets it to the class variable.
        public FetchBeveragesAsyncTask(BeverageCallback beverageCallback) {
            mBeverageCollection = BeverageCollection.get();
            mBeverageCallback = beverageCallback;
        }

        //Override method to execute the http requests to pull the beverages from the web.
        @Override
        protected Boolean doInBackground(Void... voids) {
            //Sets the parameters for the http request.
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpGet httpGet = new HttpGet(SERVER_ADDRESS);

            try {
                //Calls to get the response from the web.
                HttpResponse httpResponse = client.execute(httpGet);

                //Gets the entity.
                HttpEntity entity = httpResponse.getEntity();
                //Sets the entity to a string variable.
                String result = EntityUtils.toString(entity);
                //Converts the string into a JSONArray variable.
                JSONArray jArray = new JSONArray(result);

                //Loops through the length of the JSONArray.
                for(int i = 0; i < jArray.length(); i++) {
                    //Grabs the object at the current index in the loop and stores it in a JSONObject.
                    JSONObject jObject = jArray.getJSONObject(i);

                    //Picks out each piece of data from each JSONObject and sets them in local variables.
                    String id = jObject.getString("id");
                    String name = jObject.getString("name");
                    String pack = jObject.getString("pack");
                    Double price = Double.parseDouble(jObject.getString("price"));
                    int active = Integer.parseInt(jObject.getString("isActive"));

                    //Sets the boolean value of the active state of the beverage.
                    boolean isActive;
                    if(active == 0) {
                        isActive = false;
                    } else {
                        isActive = true;
                    }

                    //Creates a new beverage from the current index.
                    Beverage newBeverage = new Beverage(id, name, pack, price, isActive);

                    //Sends the newly created beverage over to the singleton to be added to the array list.
                    mBeverageCollection.addBeverageToList(newBeverage);
                }

            } catch (Exception e) {
                //Catches all exceptions that may occur.
                e.printStackTrace();
                //Returns false to show there was an error.
                return false;
            }
            //If no errors occur then true is returned.
            return true;
        }

        //Override method that's called at the end of the background task.
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //Dismisses the progress dialog.
            mProgressDialog.dismiss();
            //Calls the callback interface.
            mBeverageCallback.beverageCallback(aBoolean);
        }
    }
}
