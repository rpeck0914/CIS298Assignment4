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

    ProgressDialog mProgressDialog;

    public static final int CONNECTION_TIMEOUT = 1000 * 15;
    public static final String SERVER_ADDRESS = "http://barnesbrothers.homeserver.com/beverageapi";

    public ServerRequests(Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("Processing...");
        mProgressDialog.setMessage("Please Wait...");
    }

    public void fetchBeveragesAsyncTask(BeverageCallback beverageCallback) {
        mProgressDialog.show();
        new FetchBeveragesAsyncTask(beverageCallback).execute();
    }

    public class FetchBeveragesAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private BeverageCollection mBeverageCollection;
        private BeverageCallback mBeverageCallback;

        public FetchBeveragesAsyncTask(BeverageCallback beverageCallback) {
            mBeverageCollection = BeverageCollection.get();
            mBeverageCallback = beverageCallback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpGet httpGet = new HttpGet(SERVER_ADDRESS);

            try {
                HttpResponse httpResponse = client.execute(httpGet);

                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                JSONArray jArray = new JSONArray(result);

                for(int i = 0; i < jArray.length(); i++) {
                    JSONObject jObject = jArray.getJSONObject(i);

                    String id = jObject.getString("id");
                    String name = jObject.getString("name");
                    String pack = jObject.getString("pack");
                    Double price = Double.parseDouble(jObject.getString("price"));
                    int active = Integer.parseInt(jObject.getString("isActive"));

                    boolean isActive;
                    if(active == 0) {
                        isActive = false;
                    } else {
                        isActive = true;
                    }

                    Beverage newBeverage = new Beverage(id, name, pack, price, isActive);

                    mBeverageCollection.addBeverageToList(newBeverage);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mProgressDialog.dismiss();
            mBeverageCallback.beverageCallback(aBoolean);
        }
    }
}
