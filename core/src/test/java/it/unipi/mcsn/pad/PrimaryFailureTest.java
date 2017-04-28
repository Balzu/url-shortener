package it.unipi.mcsn.pad;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import it.unipi.mcsn.pad.core.message.GetMessage;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.PutMessage;

public class PrimaryFailureTest {
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
	public void  backupShouldReplacePrimary(){
		
		String url = "www.stringa_di_prova.it/testaFallimentoPrimario";
		ClientMessage cmsg = new PutMessage(url, null);
		int random = new Random().nextInt(nodes.size());
		NodeCommunicationManager manager = nodes.get(random).getNodeCommService().getCommunicationManager();
		manager.processClientMessage(cmsg);
		
		// Now we retrieve the primary node for the url		
		String surl = manager.getShortUrl(cmsg);
		int primaryId = manager.findPrimary(surl);
		// First we check that we found the right primary
		String primaryUrl = nodes.get(primaryId).getStorageService().getStorageManager().read(surl).getValue();
		assertEquals("Primary node must have stored the given url", url, primaryUrl);
		// Then we wait for the primary to replicate its database into the backup node,
		// we shut down the primary to simulate a crash and finally we check that the system still
		// works despite primary failure
		try {
			Thread.sleep(backupIntervals.get(primaryId));
			nodes.get(primaryId).shutdown();
			nodes.remove(primaryId);
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		cmsg = new GetMessage(surl);
		random = new Random().nextInt(nodes.size());
		manager = nodes.get(random).getNodeCommService().getCommunicationManager();
		NodeMessage reply = (NodeMessage) manager.processClientMessage(cmsg);
		assertEquals("In case of primary failure, the system should automatically react"
				+ "by using the backup node to answer the request", url, reply.getLongUrl());
	}
	

}
