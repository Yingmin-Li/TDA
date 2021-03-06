/* TweetTracker. Copyright (c) Arizona Board of Regents on behalf of Arizona State University
 * @author shamanth
 */
package Visualization.trends;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ControlChartExample
{
    static final String DEF_INFILENAME = "file/BarackObama.json";
    public static final SimpleDateFormat SDM = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
   	public static final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.US);
   	public static final long HOUR = 8*3600*1000; // in milli-seconds.
   	
    public JSONArray GenerateDataTrend(String inFilename) throws ParseException
    {
        BufferedReader br = null;
        JSONArray result = new JSONArray();
        HashMap<String,Integer> datecount = new HashMap<String,Integer>();
        try{
            br= new BufferedReader(new InputStreamReader(new FileInputStream(inFilename),"UTF-8"));
            String temp = "";
            while((temp = br.readLine())!=null)
            {
                try {
                    JSONObject jobj = new JSONObject(temp);
                    String orgstrdate = jobj.getString("created_at").toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
                    Date date = sdf.parse(orgstrdate);         
                    String strdate = SDM.format(new Date(date.getTime() - HOUR));
                    if(datecount.containsKey(strdate))
                    {
                        datecount.put(strdate, datecount.get(strdate)+1);
                    }
                    else
                    {
                        datecount.put(strdate, 1);
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(ControlChartExample.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ArrayList<DateInfo> dinfos = new ArrayList<DateInfo>();
            Set<String> keys = datecount.keySet();
            for(String key:keys)
            {
                DateInfo dinfo = new DateInfo();
                try {
                    dinfo.d = SDM.parse(key);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    continue;
                }
                dinfo.count = datecount.get(key);
                dinfos.add(dinfo);
            }
            double mean = this.GetMean(dinfos);
            double stddev = this.GetStandardDev(dinfos, mean);
            Collections.sort(dinfos);
            //Normalize the trend by subtracting the mean and dividing by standard deviation to get a distribution with 0 mean and a standard deviation of 1
            for(DateInfo dinfo:dinfos)
            {
                try{
                    JSONObject jobj = new JSONObject();
                    jobj.put("date", SDM.format(dinfo.d));
                    jobj.put("count", (dinfo.count-mean)/stddev);
                    jobj.put("mean", 0);
                    jobj.put("stdev+3", 3);
                    jobj.put("stdev-3", -3);
                    result.put(jobj);
                }catch(JSONException ex)
                {
                    ex.printStackTrace();
                }
            }
        }catch(IOException ex)
        {
            ex.printStackTrace();
        }finally{
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(ControlChartExample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

     public double GetStandardDev(ArrayList<DateInfo> dateinfos,double mean)
    {
        double intsum = 0;
        int numperiods = dateinfos.size();
        for(DateInfo dinfo:dateinfos)
        {
            intsum+=Math.pow((dinfo.count - mean),2);
        }
//        System.out.println(Math.sqrt((double)intsum/timePeriodCounts.size()));
        return Math.sqrt((double)intsum/numperiods);
    }

    public double GetMean(ArrayList<DateInfo> dateinfos)
    {       
        int numperiods = dateinfos.size();
        int sum = 0;
        for(DateInfo dinfo:dateinfos)
        {
            sum +=dinfo.count;
        }
//        System.out.println((double)sum/numPeriods);
        return ((double)sum/numperiods);
    }

    public static void main(String[] args) throws ParseException
    {
        ControlChartExample cce = new ControlChartExample();
        String infilename = DEF_INFILENAME;
        if(args!=null)
        {
            if(args.length>=1&&!args[0].isEmpty())
            {
                File fl = new File(args[0]);
                if(fl.exists())
                {
                    infilename = args[0];
                }
            }
        }
        System.out.println(cce.GenerateDataTrend(infilename));
    }

}
