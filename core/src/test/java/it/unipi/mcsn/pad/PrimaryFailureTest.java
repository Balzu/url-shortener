package it.unipi.mcsn.pad;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
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
	
    /*private static List<GossipMember> startupMembers;
    private static  List<Integer> virtualInstances;	    	
    private static  Map<String, Integer> ports;
    private static int gossipPort;
    private static int clientPort;
    private static int nodePort;
    private static List<String> addresses;
    private static GossipSettings settings;
    */
    
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
	public void  backupShouldReplacePrimary()
	{		
		String url = "www.stringa_di_prova.it/testaFallimentoPrimario";
		ClientMessage cmsg = new PutMessage(url, null);
		int randomId = new Random().nextInt(nodes.size());
		NodeCommunicationManager manager = nodes.get(randomId).getNodeCommService().getCommunicationManager();
		manager.processClientMessage(cmsg);		
		// Now we retrieve the primary node for the url		
		String surl = manager.getShortUrl(cmsg);
		int primaryId = manager.findPrimary(surl);		
		// Then we wait for the primary to replicate its database into the backup node,
		// we shut down the primary to simulate a crash,
		// we wait for the backup node to merge its backup DB into the primary DB (eventual consistency)
		// and finally we check that the system still works despite primary failure
		try {
			Thread.sleep(12000);
			nodes.get(primaryId).shutdown();
			//TODO: it works because nodes[i] -> Node having id = 'i', but after I remove an item from nodes, this is no more true			
			nodes.remove(primaryId); 
			Thread.sleep(25000);
			cmsg = new GetMessage(surl);		
			randomId = new Random().nextInt(nodes.size());			
			manager = nodes.get(randomId).getNodeCommService().getCommunicationManager();
			NodeMessage reply = (NodeMessage) manager.processClientMessage(cmsg);			
			assertEquals("In case of primary failure, the system should automatically react"
					+ "by using the backup node to answer the request", url, reply.getLongUrl());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	@Test
	public void primaryShouldWorkWhenRejoiningCluster(){
		String url = "www.stringa_di_prova.it/testaPrimarioQuandoTornaInFunzioneDopoFallimento";
		ClientMessage cmsg = new PutMessage(url, null);
		int randomId = new Random().nextInt(nodes.size());
		NodeCommunicationManager manager = nodes.get(randomId).getNodeCommService().getCommunicationManager();
		manager.processClientMessage(cmsg);				
		String surl = manager.getShortUrl(cmsg);		
		int primaryId = manager.findPrimary(surl);	
		System.out.println("Primary node before crashing = " + primaryId);
		try {
			Thread.sleep(12000);
			nodes.get(primaryId).shutdown();
			Node underTest = nodes.remove(primaryId);
			Thread.sleep(25000);
			cmsg = new GetMessage(surl);		
			randomId = new Random().nextInt(nodes.size());			
			manager = nodes.get(randomId).getNodeCommService().getCommunicationManager();
			primaryId = manager.findPrimary(surl);		
			System.out.println("Primary node after crashing = " + primaryId);
			underTest.restart();			
			nodes.add(underTest);
			Thread.sleep(20000);
			primaryId = manager.findPrimary(surl);
			System.out.println("Primary node after re-joining of crashed node = " + primaryId);
			NodeMessage reply = (NodeMessage) manager.processClientMessage(cmsg);			
			assertEquals("After crashed node rejoins the cluster, it should immediately"
					+ "resume the correct behaviour", url, reply.getLongUrl());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
