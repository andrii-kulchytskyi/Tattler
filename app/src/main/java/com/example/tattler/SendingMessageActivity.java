package com.example.tattler;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class SendingMessageActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtMsg;
    private Button btnSend;
    private ListView listView;
    private Button btnViewTattlers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_message);

        edtMsg = findViewById(R.id.edtMsg);
        btnSend = findViewById(R.id.btnSendTattle);
        listView = findViewById(R.id.listView);
        btnViewTattlers = findViewById(R.id.btnViewTattlers);

        btnViewTattlers.setOnClickListener(this);


        edtMsg.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnSend.callOnClick();
                }
                return false;
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ParseObject myTattle = new ParseObject("MyTattle");
                myTattle.put("tattle", edtMsg.getText().toString());
                myTattle.put("user", ParseUser.getCurrentUser().getUsername());
                final ProgressDialog progressDialog = new ProgressDialog(SendingMessageActivity.this);
                progressDialog.setMessage("Loading...");
                progressDialog.show();

                myTattle.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            FancyToast.makeText(SendingMessageActivity.this, myTattle.get("user") + "'s tattle (" + myTattle.get("tattle") + ") is saved!", Toast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                        } else {
                            FancyToast.makeText(SendingMessageActivity.this, e.getMessage(), Toast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        final ArrayList<HashMap<String, String>> tattleList = new ArrayList<>();
        final SimpleAdapter adapter = new SimpleAdapter(SendingMessageActivity.this, tattleList, android.R.layout.simple_list_item_2, new String[]{"tattleUserName", "tattleValue"}, new int[]{android.R.id.text1, android.R.id.text2});

        try {
            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("MyTattle");
            parseQuery.whereContainedIn("user", ParseUser.getCurrentUser().getList("fanOf"));
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects.size() > 0 && e == null) {
                        for (ParseObject tattleObject : objects) {
                            HashMap<String, String> userTattle = new HashMap<>();
                            userTattle.put("tattleUserName", tattleObject.getString("user"));
                            userTattle.put("tattleValue", tattleObject.getString("tattle"));
                            tattleList.add(userTattle);
                        }
                        listView.setAdapter(adapter);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
