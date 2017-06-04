package it.unipi.mcsn.pad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.LogLevel;
import com.google.code.gossip.RemoteGossipMember;

import it.unipi.mcsn.pad.core.Node;
import it.unipi.mcsn.pad.core.NodeRunner;
import it.unipi.mcsn.pad.core.communication.node.NodeCommunicationManager;
import it.unipi.mcsn.pad.core.communication.node.NodeCommunicationManager.MessageTypeException;
import it.unipi.mcsn.pad.core.message.ClientMessage;
import it.unipi.mcsn.pad.core.message.GetMessage;
import it.unipi.mcsn.pad.core.message.MessageType;
import it.unipi.mcsn.pad.core.message.NodeMessage;
import it.unipi.mcsn.pad.core.message.PutMessage;
import it.unipi.mcsn.pad.core.message.RemoveMessage;
import it.unipi.mcsn.pad.core.message.VersionedMessage;

public class ConflictResolutionTest {
	
	private static List<Node> nodes;
	private static List<Integer> backupIntervals;
	
	@Before
	public  void setupCluster(){		
		try {
			List<Integer> virtualInstances=null;
	    	File configFile = new File ("src/main/resources/core.conf");
	    	List<String> addresses;
			addresses = NodeRunner.getAddressesFromFile(configFile);
			virtualInstances = NodeRunner.getVirtualInstancesFromFile(configFile);
			backupIntervals =  NodeRunner.getBackupIntervalsFromFile(configFile);
			Map<String, Integer> confs = NodeRunner.getConfFromFile(configFile);
    		int gossipPort = confs.get("gossip_port");
    		int clientPort = confs.get("client_port");
    		int nodePort = confs.get("node_port");    		
    		int gossipInterval = confs.get("gossip_interval");
    		int cleanupInterval = confs.get("cleanup_interval");
    		GossipSettings settings = new GossipSettings(gossipInterval, cleanupInterval); 						
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
	
	@After
	public  void tearDownCluster(){
		for (Node node : nodes){
			node.getStorageService().getStorageManager().emptyPrimary();
			node.getStorageService().getStorageManager().emptyBackup();
			node.shutdown();
		}
	}
	
	
	@Test
	public void primaryShouldResolveConflictsAfterRejoiningCluster(){		
		try {
			String url = "www.stringa_di_prova.it/questaUrlèProcessataDalPrimario";
			ClientMessage cmsg = new PutMessage(url, null);
			int randomId = new Random().nextInt(nodes.size());
			NodeCommunicationManager manager = nodes.get(randomId).getNodeCommService().getCommunicationManager();
			NodeMessage reply = (NodeMessage)manager.processClientMessage(cmsg);				
			String surl = manager.getShortUrl(cmsg);	
			System.out.println("I insert the url " + url + " in the primary."+
					"\n It returns the shortened url " + surl);
			int primaryId = manager.findPrimary(surl);		
			System.out.println("Primary node before crashing = " + primaryId);
			Thread.sleep(12000); //12
			nodes.get(primaryId).shutdown();
			Node underTest = nodes.get(primaryId); 
			System.out.println("I shut down the primary...");
			Thread.sleep(25000); //25
			System.out.println("Now the primary is down, and I update the original url =>" +
					"\nThe updated url is now stored in the DB of the backup node ");			
			do { //Ask to every node but not to the crashed one
				randomId = new Random().nextInt(nodes.size());	
			} while (randomId == primaryId);	
			manager = nodes.get(randomId).getNodeCommService().getCommunicationManager();
			primaryId = manager.findPrimary(surl);		
			System.out.println("Primary node after crashing = " + primaryId);
			String upd = "www.stringa_di_prova.it/questaUrlèProcessataDalBackup/dopoFallimentoPrimario";
			NodeMessage updated = new VersionedMessage(
					upd, surl, reply.getVectorClock(), MessageType.PUT);
			nodes.get(primaryId).getStorageService().getStorageManager().store(updated);
			underTest.restart();
			System.out.println("Primary node restarted...");		
			Thread.sleep(30000); //20000
			primaryId = manager.findPrimary(surl);
			System.out.println("Primary node after re-joining of crashed node = " + primaryId);
			cmsg = new GetMessage(surl);	
			reply = (NodeMessage) manager.processClientMessage(cmsg);				
			assertEquals("After crashed node rejoins the cluster, it should have replaced its version"
					+ "of the url with the most updated version received from backup",
					upd, reply.getLongUrl());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MessageTypeException e) {			
			e.printStackTrace();
		}
	}	
	

	@Test
	public void primaryShouldRemoveUrlsRemovedByTheBackupNode(){		
		try {
			String url = "www.stringa_di_prova.it/questaUrlèProcessataDalPrimario";
			ClientMessage cmsg = new PutMessage(url, null);
			int randomId = new Random().nextInt(nodes.size());
			NodeCommunicationManager manager = nodes.get(randomId).getNodeCommService().getCommunicationManager();
			NodeMessage reply = (NodeMessage)manager.processClientMessage(cmsg);				
			String surl = manager.getShortUrl(cmsg);	
			System.out.println("I insert the url " + url + " in the primary."+
					"\n It returns the shortened url " + surl);
			int primaryId = manager.findPrimary(surl);		
			System.out.println("Primary node before crashing = " + primaryId);
			Thread.sleep(12000); //12
			nodes.get(primaryId).shutdown();
			Node underTest = nodes.get(primaryId); 
			System.out.println("I shut down the primary...");
			Thread.sleep(25000); //25
			System.out.println("Now the primary is down, and I delete the url =>" +
					"\nin the backup node ");			
			do { //Ask to every node but not to the crashed one
				randomId = new Random().nextInt(nodes.size());	
			} while (randomId == primaryId);	
			manager = nodes.get(randomId).getNodeCommService().getCommunicationManager();
			primaryId = manager.findPrimary(surl);		
			System.out.println("Primary node after crashing = " + primaryId);
			System.out.println("Now I remove the url from the new primary (i.e. the "
					+ "backup node) ");
			ClientMessage remove = new RemoveMessage(surl);
			manager.processClientMessage(remove);
			underTest.restart();
			System.out.println("Primary node restarted...");		
			Thread.sleep(30000); //20000
			primaryId = manager.findPrimary(surl);
			System.out.println("Primary node after re-joining of crashed node = " + primaryId);
			cmsg = new GetMessage(surl);	
			reply = (NodeMessage) manager.processClientMessage(cmsg);				
			assertNull("After crashed node rejoins the cluster, it should have removed the url",
					reply.getLongUrl());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MessageTypeException e) {			
			e.printStackTrace();
		}
	}
}
