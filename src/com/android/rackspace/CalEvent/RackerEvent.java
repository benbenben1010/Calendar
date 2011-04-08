package com.android.rackspace.CalEvent;

import java.util.*;

public class RackerEvent implements Comparable<RackerEvent> {

  public enum Frequency {
    WEEKLY,
    DAILY,
    MONTHLY,
    YEARLY
  };


  private Date startTime;
  private Date stopTime;
  private String description;
  private String summary;
  private String location;
  private Frequency freq;
  private int interval;

  private Integer[] parseRecurList(String list) {

    if (list == null || list.equals(""))
      return null;

    String[] days = list.split(",");
    ArrayList<Integer> outlist = new ArrayList<Integer>();
    Integer[] a = new Integer[0];

    for (String day : days) {
      if (day.equals("SU"))
        outlist.add(Calendar.SUNDAY);
      else if (day.equals("MO"))
        outlist.add(Calendar.MONDAY);
      else if (day.equals("TU"))
        outlist.add(Calendar.TUESDAY);
      else if (day.equals("WE"))
        outlist.add(Calendar.WEDNESDAY);
      else if (day.equals("TH"))
        outlist.add(Calendar.THURSDAY);
      else if (day.equals("FR"))
        outlist.add(Calendar.FRIDAY);
      else if (day.equals("SA"))
        outlist.add(Calendar.SATURDAY);

    }

    return outlist.toArray(a);
  }


  public RackerEvent(Date start, Date stop, String desc, String summ, String loc,
      String freq, int interval, String byday) {
    startTime = start;
    stopTime = stop;
    description = desc;
    summary = summ;
    location = loc;
    Calendar cal = new GregorianCalendar();
    Calendar now = new GregorianCalendar();

    Date newStartTime;

    

    now.setTime(new Date());

    if (freq.equals("WEEKLY"))
      this.freq = Frequency.WEEKLY;
    else if (freq.equals("DAILY")) 
      this.freq = Frequency.DAILY;
    else if (freq.equals("MONTHLY"))
      this.freq = Frequency.MONTHLY;
    else if (freq.equals("YEARLY"))
      this.freq = Frequency.YEARLY;

    this.interval = interval;

    cal.setTime(start);

    if (freq.equals("")) 
      return;


    while (cal.before(now)) {
      if (this.freq == Frequency.DAILY)
        cal.add(Calendar.DAY_OF_MONTH, interval);
      else if (this.freq == Frequency.WEEKLY) {
        Integer[] daylist = parseRecurList(byday);
        cal.add(Calendar.WEEK_OF_YEAR, interval);
        if (daylist != null) {
          for (Integer i : daylist) {
            cal.set(Calendar.DAY_OF_WEEK, i.intValue());
            if (cal.after(now)) 
              break;
          }
        } 
      }
      else if (this.freq == Frequency.MONTHLY)
        cal.add(Calendar.MONTH, interval);
      else if (this.freq == Frequency.WEEKLY)
        cal.add(Calendar.YEAR, interval);
    }


    newStartTime = cal.getTime();
    stopTime.setTime(stop.getTime() + (cal.getTimeInMillis() - start.getTime()));
    startTime = cal.getTime();



  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getStopTime() {
    return stopTime;
  }

  public String getDescription() {
    return description;
  }

  public String getSummary() {
    return summary;
  }

  public String getLocation() {
    return location;
  }

  public int compareTo(RackerEvent ev) {
    return startTime.compareTo(ev.startTime);
  }
}
