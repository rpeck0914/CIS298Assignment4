package edu.kvcc.cis298.cis298assignment4;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

/**
 * Created by David Barnes on 11/3/2015.
 * Class that contains some reusable code for activities that use a single fragment
 */
public abstract class SingleFragmentActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the content to the activity_fragment layout
        setContentView(R.layout.activity_fragment);

        //create a new fragment manager, which is required to attach a new fragment
        FragmentManager fm = getSupportFragmentManager();

        //find the fragment that is already in teh container. Might be null.
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        //if the fragment is null, we can create a new one and add it to the container
        if (fragment == null) {
            //create the fragment
            fragment = createFragment();
            //start a transaction to put it in the container
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit(); //must commit the transaction to make it complete
        }
        //Calls the method to make a server request.
        retrieveBeveragesFromHTTP();
    }

    //Private method that makes a server request and has the callbacks override method
    private void retrieveBeveragesFromHTTP(){
        //Creates a new instance of server request and sends over the activities context.
        ServerRequests serverRequests = new ServerRequests(this);
        //Calls the method to fetch the beverages and sends a new BeverageCallback.
        serverRequests.fetchBeveragesAsyncTask(new BeverageCallback() {
            //Override method that's called when the callback is triggered.
            @Override
            public void beverageCallback(boolean status) {
                //Checks to see if the return is true.
                if (status) {
                    //Creates a new fragment manager variable.
                    FragmentManager manager = getSupportFragmentManager();
                    //Grabs the fragment from the container.
                    BeverageListFragment f2 = (BeverageListFragment) manager.findFragmentById(R.id.fragment_container);
                    //Calls the runUpdate method in the fragments code.
                    f2.runUpdate();
                //If the return is false this will execute.
                } else {
                    //Calls the toast Error method.
                    toastError();
                }
            }
        });
    }

    private void toastError() {
        //Private method to toast that there was an error in retrieving the beverage data from the web.
        Toast.makeText(this, "Error Loading Beverages From Web", Toast.LENGTH_SHORT).show();
    }

    //Define a abstract method that must be overridden in child classes to return a fragment to use.
    protected abstract Fragment createFragment();

}
