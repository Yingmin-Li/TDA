/**
 * 
 */
package Analyzer.centrality.examples;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import Analyzer.util.TweetFileToGraph;
import GraphElements.RetweetEdge;
import GraphElements.UserNode;
import edu.uci.ics.jung.graph.DirectedGraph;
import utils.RetweetObject;
import utils.TxtReader;
import utils.TxtWriter;

/**
 * @author Gingber
 *
 */
public class RetweetNetworkGraph {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		File tweetFile;
		
		if(args.length > 0){
			tweetFile = new File(args[0]);
		}
		else{
			tweetFile = new File("file/DreamD.json");
		}
		
		DirectedGraph<UserNode, RetweetEdge> retweetGraph = TweetFileToGraph.getRetweetNetwork(tweetFile);
		System.out.println(retweetGraph.toString());
		RetweetObject rtobj =null; 
		Vector<RetweetObject> rtobjs = new Vector<RetweetObject>();
		for(RetweetEdge rtedge : retweetGraph.getEdges()) {
			rtobj = new RetweetObject(rtedge.getTo().getUsername(), rtedge.getTo().getCreated_at(), rtedge.getTo().getId(), rtedge.getTo().getText(),
					rtedge.getFrom().getUsername(), rtedge.getFrom().getCreated_at(), rtedge.getFrom().getId(), rtedge.getFrom().getText());
			rtobjs.add(rtobj);
		}
		
		Comparator comp = new ComparatorImplementation();
		Collections.sort(rtobjs, comp);	//按message_id进行升序排列
		
		for(int i = 0; i < rtobjs.size(); i++) 
		{
			RetweetObject clusterRtObj =null;
			Vector<RetweetObject> clusterRtObjs = new Vector<RetweetObject>();
			Vector<Integer> SameContent = new Vector<Integer>();
			
			clusterRtObj = new RetweetObject(rtobjs.get(i).getFromMsgId(), rtobjs.get(i).getFromUserName(), rtobjs.get(i).getOriginalDate(), rtobjs.get(i).getToMsgId(), rtobjs.get(i).getToUserName(), rtobjs.get(i).getRetweetDate());
			clusterRtObjs.add(clusterRtObj);
			
			Vector<String> Node = new Vector<String>();
			
			Node.add(rtobjs.get(i).getFromMsgId());
			Node.add(rtobjs.get(i).getToMsgId());
			
			int RtNum = 0;
			for(int j = i+1; j < rtobjs.size(); j++) {
				if (Node.contains(rtobjs.get(j).getFromMsgId())) {
					
					clusterRtObj = new RetweetObject(rtobjs.get(j).getFromUserName(), rtobjs.get(j).getOriginalDate(), rtobjs.get(j).getToUserName(), rtobjs.get(j).getRetweetDate());
					clusterRtObjs.add(clusterRtObj);
					
					if(!Node.contains(rtobjs.get(j).getToMsgId())) {
						Node.add(rtobjs.get(j).getToMsgId());					
					}
					
					if(!SameContent.contains(i)) {
						SameContent.add(i);
					}
					
					if(!SameContent.contains(j)) {
						SameContent.add(j);
					}
					RtNum++;
					
				}
			}
				
			// 转发次数达到一定阈值，则写入文件
			if(clusterRtObjs.size() != 0 && RtNum > 1000) 
			{
				Comparator datecomp = new RtComparatorImplementation();
				Collections.sort(clusterRtObjs, datecomp);	//按时间进行升序排列
				StringBuilder RtCluster = new StringBuilder();
				RtCluster.append(rtobjs.get(i).getFromMsgId());
				RtCluster.append("\n");
				RtCluster.append(rtobjs.get(i).getFromUserName());
				RtCluster.append("\n");
				RtCluster.append(rtobjs.get(i).getOrignalText());
				RtCluster.append("\n\n");
				for(int k = 0; k < clusterRtObjs.size(); k++)
				{
					RtCluster.append(clusterRtObjs.get(k).getFromUserName());
					RtCluster.append(",");
					RtCluster.append(clusterRtObjs.get(k).getOriginalDate());
					RtCluster.append(",");
					RtCluster.append(clusterRtObjs.get(k).getToUserName());
					RtCluster.append(",");
					RtCluster.append(clusterRtObjs.get(k).getRetweetDate());
					RtCluster.append("\n");
				}
				TxtWriter.saveToFile(RtCluster.toString(), new File("file/RetweetGraph/DreamD" + i+ ".csv"), "UTF-8");
			}
			
			if (SameContent != null && SameContent.size() > 0) // 存在重复项
			{ 
				for (int k = SameContent.size() - 1; k >= 0; k--) {
					int index = Integer.parseInt(SameContent.get(k)
							.toString());
					rtobjs.remove(index);
				}
			}
		}
	}
}

class ComparatorImplementation implements Comparator<RetweetObject> {
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public int compare(RetweetObject s1,RetweetObject s2) {
		Date begin = null;
		try {
			begin = sdf.parse(s1.getOriginalDate());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date end = null;
		try {
			end = sdf.parse(s2.getOriginalDate());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(begin.after(end)){  
            return 1;  
        }  
        else{  
            return -1;  
        }    
	}
}

class RtComparatorImplementation implements Comparator<RetweetObject> {
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public int compare(RetweetObject s1,RetweetObject s2) {
		Date begin = null;
		try {
			begin = sdf.parse(s1.getRetweetDate());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date end = null;
		try {
			end = sdf.parse(s2.getRetweetDate());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(begin.after(end)){  
            return 1;  
        }  
        else{  
            return -1;  
        }    
	}
}
