package it.unipi.mcsn.pad;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.GossipSettings;
import com.google.code.gossip.LocalGossipMember;
import com.google.code.gossip.LogLevel;
import com.google.code.gossip.RemoteGossipMember;
import com.google.code.gossip.manager.GossipManager;

import it.unipi.mcsn.pad.consistent.ConsistentHasher;
import it.unipi.mcsn.pad.consistent.ConsistentHasherImpl;
import it.unipi.mcsn.pad.core.Node;
import it.unipi.mcsn.pad.core.NodeRunner;
import it.unipi.mcsn.pad.core.utils.Partitioner;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
    
    
     

    /**
     * Rigourous Test :-)
     * @param <B>
     * @param <M>
     */
    public <B, M> void testApp()
    {
    	//NodeRunner nr = new NodeRunner();
    	
		try {
			List<Integer> virtualInstances=null;
	    	List<Integer> backupIntervals=null;
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
			
			List<Node> nodes = new ArrayList<>();
			for (int i = 0; i < addresses.size(); ++i) {	    			
				Node node = new Node(clientPort, 50, addresses.get(i), gossipPort, i+"" , 
						LogLevel.DEBUG,	startupMembers, settings, null,  i, nodePort,
						virtualInstances.get(i), backupIntervals.get(i));
				node.start();
				nodes.add(node);    				   
			} 
			
			Partitioner<Integer, String> p =nodes.get(2).getNodeCommService().getCommunicationManager().getPartitioner();
			
			GossipManager gManager = nodes.get(2).getNodeCommService().getGossipService().get_gossipManager();
			List<LocalGossipMember> members = new ArrayList<>(gManager.getMemberList());
			members.add(gManager.getMyself());
			List<Integer> buckets = new ArrayList<>();
			for (LocalGossipMember member : members){
				//int id = Utils.getIntegerIpAddress(member.getId());
				int id = Integer.parseInt(member.getId());
				buckets.add(id);			
			}
			
			ConsistentHasher<Integer, String> ch = p.getConsistentHasher();
			
			List<Integer> oldBuckets = ch.getAllBuckets();
			// If the active nodes are different than the previous nodes in the bucket,
			// I remove all the old nodes and insert the new ones
			if (!p.areEqual(oldBuckets, buckets)){
				for (Integer bucket : oldBuckets)
					try {
						ch.removeBucket(bucket);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				for (Integer bucket : buckets)
					ch.addBucket( bucket);					
			}
			
			
			int result = ch.findBucket("pad.ly/f5103c51");
			
			assertEquals(2, result);
			
			
		} catch (IOException e) {
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
}
