package it.unipi.mcsn.pad.core;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.LogLevel;
import com.google.code.gossip.RemoteGossipMember;


public class NodeRunner 
{
    public static void main( String[] args )
    {     	
    	try {
    		GossipSettings settings = new GossipSettings();
    		int seedNodes = 5;
    		int port = 2000;
    		List<GossipMember> startupMembers = new ArrayList<>();
    		for (int i = 1; i < seedNodes+1; ++i) {
    			startupMembers.add(new RemoteGossipMember("127.0.0." + i, port, i + ""));
    		}

    		List<Node> nodes = new ArrayList<>();
    		for (int i = 1; i < seedNodes+1; ++i) {

    			Node node = new Node(2001, 50, "127.0.0." + i, 2000, i + "", LogLevel.DEBUG,
    					startupMembers, settings, null);
    			node.start();
    			nodes.add(node);    				   
    		}   	

    		Thread.sleep(5000);
    		System.out.println("Size of membership list of node 2: " + nodes.get(2).
    				getNodeCommService().getGossipService().get_gossipManager().getMemberList().size());   	 	


    	} catch (UnknownHostException e) {			
    		e.printStackTrace();
    	} catch (InterruptedException e) {		
    		e.printStackTrace();
    	}
    }
}
