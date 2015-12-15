package edu.kvcc.cis298.cis298assignment4;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.widget.Toast;

/**
 * Created by David Barnes on 11/3/2015.
 */
public class BeverageFragment extends Fragment implements View.OnClickListener {

    //String key that will be used to send data between fragments
    private static final String ARG_BEVERAGE_ID = "crime_id";

    //Static variable for opening the contact picker.
    private static final int CONTACT_PICKER_RESULT = 1001;

    //private class level vars for the model properties
    private EditText mId;
    private EditText mName;
    private EditText mPack;
    private EditText mPrice;
    private CheckBox mActive;
    private Button mSelectContactButton, mSendDetailsButton;
    private String mSelectedEmail, mSelectedName;
    private String mEmailSubject = "Beverage App Item Information";

    //Private var for storing the beverage that will be displayed with this fragment
    private Beverage mBeverage;

    //Public method to get a properly formatted version of this fragment
    public static BeverageFragment newInstance(String id) {
        //Make a bungle for fragment args
        Bundle args = new Bundle();
        //Put the args using the key defined above
        args.putString(ARG_BEVERAGE_ID, id);

        //Make the new fragment, attach the args, and return the fragment
        BeverageFragment fragment = new BeverageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //When created, get the beverage id from the fragment args.
        String beverageId = getArguments().getString(ARG_BEVERAGE_ID);
        //use the id to get the beverage from the singleton
        mBeverage = BeverageCollection.get().getBeverage(beverageId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Use the inflator to get the view from the layout
        View view = inflater.inflate(R.layout.fragment_beverage, container, false);

        mSelectContactButton = (Button) view.findViewById(R.id.select_contact_button);
        mSelectContactButton.setOnClickListener(this);
        mSendDetailsButton = (Button) view.findViewById(R.id.send_beverage_details_button);
        mSendDetailsButton.setOnClickListener(this);

        //Get handles to the widget controls in the view
        mId = (EditText) view.findViewById(R.id.beverage_id);
        mName = (EditText) view.findViewById(R.id.beverage_name);
        mPack = (EditText) view.findViewById(R.id.beverage_pack);
        mPrice = (EditText) view.findViewById(R.id.beverage_price);
        mActive = (CheckBox) view.findViewById(R.id.beverage_active);

        //Set the widgets to the properties of the beverage
        mId.setText(mBeverage.getId());
        mId.setEnabled(false);
        mName.setText(mBeverage.getName());
        mPack.setText(mBeverage.getPack());
        mPrice.setText(Double.toString(mBeverage.getPrice()));
        mActive.setChecked(mBeverage.isActive());

        //Text changed listenter for the id. It will not be used since the id will be always be disabled.
        //It can be used later if we want to be able to edit the id.
        mId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBeverage.setId(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Text listener for the name. Updates the model as the name is changed
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBeverage.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Text listener for the Pack. Updates the model as the text is changed
        mPack.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBeverage.setPack(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Text listener for the price. Updates the model as the text is typed.
        mPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //If the count of characters is greater than 0, we will update the model with the
                //parsed number that is input.
                if (count > 0) {
                    mBeverage.setPrice(Double.parseDouble(s.toString()));
                    //else there is no text in the box and therefore can't be parsed. Just set the price to zero.
                } else {
                    mBeverage.setPrice(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Set a checked changed listener on the checkbox
        mActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBeverage.setActive(isChecked);
            }
        });

        //Lastly return the view with all of this stuff attached and set on it.
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_contact_button:
                selectContact();
                break;
            case R.id.send_beverage_details_button:
                sendDetails();
                break;
        }
    }

    //Private method to launch the contact picker.
    private void selectContact() {
        doLaunchContactPicker();
    }

    //Creates a new intent and starts the contact picker activity.
    private void doLaunchContactPicker() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    //Composes the data to sent over for the email.
    private void sendDetails() {
        String[] addresses = new String[1];
        addresses[0] = mSelectedEmail;
        //Calls compose email method to send over the data to be put in the email.
        composeEmail(addresses, mEmailSubject, composeEmailBody());
        //Enables the button to send the email.
        mSendDetailsButton.setEnabled(false);
    }

    //Method to write out the body of the email to be sent.
    private String composeEmailBody() {
        String isActiveString;
        //Checks to see if the item is active or not and sets appropriate string.
        if(mBeverage.isActive()) {
            isActiveString = "Currently Active";
        } else {
            isActiveString = "Currently InActive";
        }

        //Writes the body and save it all as one string to send over to the email activity.
        String body = mSelectedName + ",\n\n" +
                     "Please Review the Following Beverage.\n\n" +
                      mBeverage.getId() + "\n" +
                      mBeverage.getName() + "\n" +
                      mBeverage.getPack() + "\n" +
                      mBeverage.getPrice() + "\n" +
                      isActiveString;

        return body;
    }

    //Private method to create a new intent to open the email app and sends over the subject and body
    //of the email.
    private void composeEmail(String[] address, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        //Sets the email address to send the email to.
        intent.putExtra(Intent.EXTRA_EMAIL, address);
        //Sets the subject of the email.
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        //Sets the body of the email.
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }

    }

    //onActivityResult is called once the user selects a contact to send an email to.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Checks to make sure the resultCode is OK.
        if(resultCode == Activity.RESULT_OK) {
            //Switch statement to see if the request code was from the contact picker.
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    //Creates a cursor to go through the contact database on the user's phone
                    Cursor cursor = null;
                    // Creates strings to hold the data that will be pulled from the contacts database.
                    String email = "";
                    String contactName = "";
                    try {
                        //Gets the data and saves it in a Uri variable.
                        Uri result = data.getData();

                        // get the contact id from the Uri
                        String id = result.getLastPathSegment();

                        // query for everything email
                        cursor = getActivity().getContentResolver().query(Email.CONTENT_URI,
                                null, Email.CONTACT_ID + "=?", new String[] { id },
                                null);

                        //Stores the id for the email and name.
                        int emailIdx = cursor.getColumnIndex(Email.DATA);
                        int nameIdx = cursor.getColumnIndex(Contacts.DISPLAY_NAME);

                        //Sets the cursor to the first set of data.
                        if (cursor.moveToFirst()) {
                            //grabs the strings at the index we set for our selected contact.
                            email = cursor.getString(emailIdx);
                            contactName = cursor.getString(nameIdx);
                        }
                    } catch (Exception e) {
                        //Catches any exceptions that may occur.
                        e.printStackTrace();
                    } finally {
                        if (cursor != null) {
                            //Closes the cursor.
                            cursor.close();
                        }
                        if(email.length() > 1) {
                            //Sets the selected contacts to class level variables to be used in setting up the email.
                            mSelectedEmail = email;
                            mSelectedName = contactName;
                            //enables the SendDetailsButton.
                            mSendDetailsButton.setEnabled(true);
                        }
                        if (email.length() == 0) {
                            //If the selected contact didn't have an email a toast will display saying there was no email.
                            Toast.makeText(getActivity(), "No email found for contact.", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        }
    }
}
