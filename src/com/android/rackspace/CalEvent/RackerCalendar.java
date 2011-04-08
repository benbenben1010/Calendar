package com.android.rackspace.CalEvent;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;



public class RackerCalendar {

  private String urlString;
  private TreeSet eventList = null;
  private enum ICalState {
    IDLE,
      BEGINEVENT,
      STARTTIME,
      STOPTIME,
      DESCRIPTION,
      LOCATION,
      SUMMARY,
      RECUR
  }




  public RackerCalendar(String url) {
    urlString = url;
    eventList = new TreeSet();
  }

  public TreeSet getEvents() {
    return eventList;
  }

  private RackerEvent parseEvent(String start, String stop,
      String desc, String summ, String loc, String recur) {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'kkmmss");

    String[] startsplit = start.split(":");
    String[] stopsplit = stop.split(":");
    Date startDate;
    Date stopDate;
    String recurType = "";
    String byday = "";
    int interval = 0;

    try {
      startDate = sdf.parse(startsplit[startsplit.length - 1]);
      stopDate = sdf.parse(stopsplit[stopsplit.length - 1]);
    } catch (ParseException e) {
      System.out.println("Failed to parse");
      return null;
    }

    if (recur == null) {
      recur = "";
    } else {
      recurType = parseRecurrence("FREQ", recur);
      interval = Integer.parseInt(parseRecurrence("INTERVAL", recur));
      byday = parseRecurrence("BYDAY", recur);
    }

    RackerEvent re = new RackerEvent(startDate, stopDate, desc.substring(11),
        summ.substring(8), loc.substring(9), recurType, 
        interval, byday);

    return re;


  }

  private String parseRecurrence(String field, String line) {

    String[] list = line.substring(6).split(";");

    for (String part : list) {
      if (part.startsWith(field))  {
        String[] entryparts = part.split("=");
        return entryparts[1];
      }
    }
    return new String("");
  }
      



  private TreeSet parseFeed(BufferedReader br) {

    ICalState parserState = ICalState.IDLE;
    ICalState parserStateNext;
    String start = null;
    String stop = null; 
    String desc = null; 
    String loc = null;
    String summ = null;
    String recur = null;

    TreeSet eventList = new TreeSet();
    RackerEvent re;


    String line = null;

    /* Begin state machine */
    try {
      while ((line = br.readLine()) != null) {
        parserStateNext = parserState;

        /* Are we on a continued line? */
        if (line.startsWith(" ")) {
          switch (parserState) {
            case DESCRIPTION:
              desc += line;
              break;
            case LOCATION:
              loc += line;
              break;
            case SUMMARY:
              summ += line;
              break;
          }
        }

        /* Handle state transition */
        if (line.equals("BEGIN:VEVENT")) {
          parserStateNext = ICalState.BEGINEVENT;
          recur = start = stop = desc = loc = summ = null;
          re = null;
        }
        if (line.equals("END:VEVENT")) {
          parserStateNext = ICalState.IDLE;
          re = parseEvent(start, stop, desc, summ, loc, recur);
          if (re != null) {
            eventList.add(re);
          }
        }
        else if (parserState != ICalState.IDLE) {
          if (line.startsWith("DTSTART")) {
            parserStateNext = ICalState.STARTTIME;
            start = line;
          } else if (line.startsWith("DTEND")) {
            parserStateNext = ICalState.STOPTIME;
            stop = line;
          } else if (line.startsWith("DESCRIPTION")) {
            parserStateNext = ICalState.DESCRIPTION;
            desc = line;
          } else if (line.startsWith("LOCATION")) {
            parserStateNext = ICalState.LOCATION;
            loc = line;
          } else if (line.startsWith("SUMMARY")) {
            parserStateNext = ICalState.SUMMARY;
            summ = line;
          } else if (line.startsWith("RRULE")) {
            parserStateNext = ICalState.RECUR;
            recur = line;
          }

        }
        parserState = parserStateNext;
      }
    } catch (IOException e) {
      return null;
    }

    return eventList;

  }

  public int getFeed() {

    URL url;
    HttpURLConnection conn = null;
    BufferedReader br = null;

    Boolean inEvent = false;

    try {
      url = new URL(urlString);
      conn= (HttpURLConnection)url.openConnection();
      InputStream istream = conn.getInputStream();

      br = new BufferedReader(new InputStreamReader(istream));


      eventList = parseFeed(br);

      return 0;

    } catch (IOException e) {

      System.out.println("Oh no!");
      System.out.println(e);
      return -1;

    }


  }

  public RackerEvent getCurrent() {

    for (Object o : eventList) {
      RackerEvent re = (RackerEvent)o;
      Date start = re.getStartTime();
      Date stop = re.getStopTime();
      Date now = new Date();

      if (now.after(start) && now.before(stop)) {
        return re;
      }
    }
    return null;
  }



}



