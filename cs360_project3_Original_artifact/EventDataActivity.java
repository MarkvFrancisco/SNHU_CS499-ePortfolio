package com.example.cs360_projectthree;

import android.telephony.SmsManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;


public class EventDataActivity extends AppCompatActivity {

    //establishing datatypes for the UI elements
    EventDBHelper DB;
    EditText dateText, titleEventText, descriptionText, phoneText;
    Button buttonAddEvent;
    RecyclerView recyclerViewEvents;
    EventAdapter adapter;
    ArrayList<Event> eventList;
    private static final int SMS_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventdata);

        DB = new EventDBHelper(this);

        //Linking the UI elements from the activity_eventdata.xml
        dateText = findViewById(R.id.dateText);
        titleEventText = findViewById(R.id.titleEventText);
        descriptionText = findViewById(R.id.descriptionText);
        phoneText = findViewById(R.id.phoneText);
        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        //Loading the event table from the database
        loadEvents();

        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = dateText.getText().toString().trim();
                String title = titleEventText.getText().toString().trim();
                String description = descriptionText.getText().toString().trim();
                String phone = phoneText.getText().toString().trim();

                if (TextUtils.isEmpty(date) || TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(phone)) {
                    Toast.makeText(EventDataActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                } else {
                    long insertedId = DB.insertEvent(date, title, description, phone);

                    if (insertedId != -1) {
                        Toast.makeText(EventDataActivity.this, "Event added.", Toast.LENGTH_SHORT).show();

                        dateText.setText("");
                        titleEventText.setText("");
                        descriptionText.setText("");
                        phoneText.setText("");

                        loadEvents();

                        if (ContextCompat.checkSelfPermission(EventDataActivity.this, Manifest.permission.SEND_SMS)
                                == PackageManager.PERMISSION_GRANTED) {
                            checkUpcomingEvents();
                        }
                    } else {
                        Toast.makeText(EventDataActivity.this, "Failed to add event.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //SMS permission request
        showSmsPopup();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            checkUpcomingEvents();
        }
    }

    //Loading events from the database
    private void loadEvents() {
        eventList = DB.getEventsList();

        adapter = new EventAdapter(
                eventList,
                event -> {
                    boolean deleted = DB.deleteEvent(event.getId());

                    if (deleted) {
                        Toast.makeText(EventDataActivity.this, "Event deleted.", Toast.LENGTH_SHORT).show();
                        loadEvents();
                    } else {
                        Toast.makeText(EventDataActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                    }
                },
                (id, date, title, description) -> {
                    boolean updated = DB.updateEvent(id, date, title, description);

                    if (updated) {
                        Toast.makeText(EventDataActivity.this, "Event updated.", Toast.LENGTH_SHORT).show();
                        loadEvents();
                    } else {
                        Toast.makeText(EventDataActivity.this, "Failed to update event.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        recyclerViewEvents.setAdapter(adapter);
    }




    //SMS Permission upon opening the event data screen
    private void showSmsPopup() {
        new AlertDialog.Builder(this)
                .setTitle("SMS Notifications")
                .setMessage("Allow this app to send SMS alerts for upcoming events and updates?")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        enableSmsNotifications();
                    }
                })
                .setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EventDataActivity.this,
                                "SMS notifications disabled. App will continue normally.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    //SMS permission function
    private void enableSmsNotifications() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "SMS permission already granted.", Toast.LENGTH_SHORT).show();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    //SMS permission request
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();

                checkUpcomingEvents();

            } else {
                Toast.makeText(this,
                        "SMS permission denied. App still works without notifications.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //sms reminder function
    private void sendSmsReminder(Event event) {
        String phoneNumber = "5556";

        String message = "Heads up!: \"" + event.getTitle()
                + "\" is coming up on " + event.getDate() + ".";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent to 5554", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed", Toast.LENGTH_SHORT).show();
        }
    }

    //event date parser
    private java.util.Date parseEventDate(String dateString) {
        String[] formats = {"MM/dd/yyyy"};

        for (String format : formats) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, java.util.Locale.US);
                sdf.setLenient(false);
                return sdf.parse(dateString);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    //checks for upcoming events
    private void checkUpcomingEvents() {
        ArrayList<Event> events = DB.getEventsList();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for (Event event : events) {
            try {
                if (event.getDate() == null || event.getDate().isEmpty()) {
                    continue;
                }

                Date eventDate = parseEventDate(event.getDate());
                if (eventDate == null) continue;

                Calendar eventCal = Calendar.getInstance();
                eventCal.setTime(eventDate);

                long diff = eventCal.getTimeInMillis() - today.getTimeInMillis();
                long days = TimeUnit.MILLISECONDS.toDays(diff);

                if (days >= 0 && days <= 7 && event.getSmsSent() == 0) {
                    sendSmsReminder(event);
                    DB.markSmsSent(event.getId());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
