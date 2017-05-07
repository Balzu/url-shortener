package it.unipi.mcsn.pad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.LogLevel;
import com.google.code.gossip.RemoteGossipMember;

import it.unipi.mcsn.pad.core.Node;
import it.unipi.mcsn.pad.core.NodeRunner;
import it.unipi.mcsn.pad.core.communication.node.NodeCommunicationManager;
import it.unipi.mcsn.pad.core.message.ClientMessage;
import it.unipi.mcsn.pad.core.message.PutMessage;

public class DataReplicationTest{
	
	private static List<Node> nodes;
	private static List<Integer> backupIntervals;

	
	
	@BeforeClass
	public static void setupCluster(){		
		try {
			List<Integer> virtualInstances=null;	    	
	    	Map<String, Integer> ports= null;
	    	File configFile = new File ("src/main/resources/core.conf");
	    	List<String> addresses;
			addresses = NodeRunner.getAddressesFromFile(configFile);
			virtualInstances = NodeRunner.getVirtualInstancesFromFile(configFile);
			backupIntervals =  NodeRunner.getBackupIntervalsFromFile(configFile);
			ports = NodeRunner.getPortsFromFile(configFile);
			int gossipPort = ports.get("gossip_port");
			int clientPort = ports.get("client_port");
			int nodePort = ports.get("node_port");			    		
			GossipSettings settings = new GossipSettings();			
			List<GossipMember> startupMembers = new ArrayList<>();
			for (int i = 0; i < addresses.size(); ++i) {
				startupMembers.add(new RemoteGossipMember(addresses.get(i), gossipPort, i + "")); //TODO: fix the id
			}			
			nodes = new ArrayList<>();
			for (int i = 0; i < addresses.size(); ++i) {	    			
				Node node = new Node(clientPort, 50, addresses.get(i), gossipPort, i+"" , 
						LogLevel.DEBUG,	startupMembers, settings, null,  i, nodePort,
						virtualInstances.get(i), backupIntervals.get(i));
				node.start();
				nodes.add(node);    				   
			} 
		}		
		catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}	
		
	
	@AfterClass
	public static void tearDownCluster(){
		for (Node node : nodes){
			node.getStorageService().getStorageManager().emptyPrimary();
			node.getStorageService().getStorageManager().emptyBackup();
			node.shutdown();
		}		
	}
	
	
	
	@Test
    public void dataShouldBeReplicated(){
		String url = "www.stringa_di_prova.it";
		ClientMessage cmsg = new PutMessage(url, null);
		NodeCommunicationManager manager = nodes.get(0).getNodeCommService().getCommunicationManager();
		manager.processClientMessage(cmsg);			
		String surl = manager.getShortUrl(cmsg);
		int primaryId = manager.findPrimary(surl);			
		String primaryUrl = nodes.get(primaryId).getStorageService().getStorageManager().read(surl).getValue();		
		try {
			Thread.sleep(backupIntervals.get(primaryId));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		int backupId = (primaryId + 1) % nodes.size();		
		String backupUrl = nodes.get(backupId).getStorageService().getStorageManager().readBackup(surl).getValue();
		assertEquals("The Backup node must have a copy of the url"
				+ "in its backup database",	primaryUrl, backupUrl);
    }
	


}
