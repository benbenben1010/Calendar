package com.android.rackspace.CalEvent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.view.View;
import android.widget.TextView;
import java.text.*;
import java.util.*;

import android.webkit.WebView;
import com.android.rackspace.CalEvent.RackerCalendar;
import com.android.rackspace.CalEvent.RackerEvent;

public class DockPanel extends Activity
{
    private TextView meetingStatusText;
    private TextView currentTimeText;
    private TextView currentMeetingTitleText;
    private TextView currentDurationText;
    private TextView nextMeeting;
    private TextView nextMeetingTime;

    private WebView mWebView;
    private RackerCalendar rc;
    private TreeSet eventList;
    private Runnable refreshProcess;
    private Handler refreshHandler;

    private int toggle = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        toggle = 0;
        rc = new RackerCalendar("http://apps.rackspace.com/a/feeds/ical/starwars@mailtrust.com/3::personal::633811/public/");
        // refreshMainView();

        refreshProcess = new Runnable() {
          public void run() {
            refreshMainView();
            refreshHandler.postDelayed(this, 30 * 1000);
          }
        };

        refreshHandler = new Handler();
        refreshHandler.postDelayed(refreshProcess, 1000);
        
    }

    public void alarmButtonHandler(View view) {
        if (toggle == 0) {
          meetingStatusText = (TextView) findViewById(R.id.meetingStatus);
          meetingStatusText.setText("FREE");
        
          currentTimeText = (TextView) findViewById(R.id.currentTime);
          currentTimeText.setText("15:09");

          currentMeetingTitleText = (TextView) findViewById(R.id.currentMeetingTitle);
          currentMeetingTitleText.setText("Room is empty");

          currentDurationText = (TextView) findViewById(R.id.currentDuration);
          currentDurationText.setText("14:30-15:30");


          toggle = 1;
        }
        else {
          setContentView(R.layout.main);
          toggle = 0;
        }
    }

    public void allEventsButtonHandler(View view) {
      setContentView(R.layout.all_events);

      mWebView = (WebView) findViewById(R.id.webview);
      mWebView.getSettings().setJavaScriptEnabled(true);
      mWebView.loadUrl("http://www.apps.rackspace.com");
    }

    public void otherCalendarsButtonHandler(View view) {
      //setContentView(R.layout.other_cals);
      return;
    }

    private RackerEvent findNextEvent(TreeSet events) {

      Date now = new Date();

      for (Object o : events) {
        RackerEvent e = (RackerEvent)o;
        Date start = e.getStartTime();
        if (start.after(now))
          return e;

      }
      return null;
    }

    private void refreshMainView() {
      rc.getFeed();

      TreeSet events = rc.getEvents();
      if (!events.isEmpty())
        eventList = events;

      RackerEvent event = rc.getCurrent();
      RackerEvent next = findNextEvent(events);

      nextMeeting = (TextView) findViewById(R.id.nextMeeting);
      nextMeetingTime = (TextView) findViewById(R.id.nextMeetingTime);
      meetingStatusText = (TextView) findViewById(R.id.meetingStatus);
      currentMeetingTitleText = (TextView) findViewById(R.id.currentMeetingTitle);
      currentDurationText = (TextView) findViewById(R.id.currentDuration);

      SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");

      if (event == null) {
          meetingStatusText.setText("FREE");
          currentMeetingTitleText.setText("Room is empty");
          currentDurationText.setText("");
      }
      else {
          meetingStatusText.setText("OCCUPIED");

          currentMeetingTitleText.setText(event.getSummary());

          String startTime = sdf.format(event.getStartTime());
          String stopTime = sdf.format(event.getStopTime());
          currentDurationText.setText(startTime + " - " + stopTime);
      }

      if  (next == null) {
        nextMeeting.setText("--");
        nextMeetingTime.setText("");
      } else {
        nextMeeting.setText(next.getSummary());
        String startTime = sdf.format(next.getStartTime());
        String stopTime = sdf.format(next.getStopTime());
        nextMeetingTime.setText(startTime + " - " + stopTime);
      }
    }

    public void refreshButtonHandler(View view) {
      refreshMainView(); 

      return;

    }

}
