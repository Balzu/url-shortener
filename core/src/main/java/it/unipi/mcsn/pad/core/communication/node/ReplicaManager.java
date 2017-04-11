package it.unipi.mcsn.pad.core.communication.node;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unipi.mcsn.pad.core.message.Message;
import it.unipi.mcsn.pad.core.message.SizedBackupMessage;
import it.unipi.mcsn.pad.core.message.UpdateMessage;
import it.unipi.mcsn.pad.core.storage.StorageService;
import voldemort.versioning.Versioned;

public class ReplicaManager extends Thread{
	
	private StorageService storageService;	
	private DatagramSocket socket = null;
	private AtomicBoolean isRunning;
	private final ExecutorService threadPool;
	private int nodePort;
	private NodeCommunicationManager nodeCommManager;
	private int nodeId;
	
	
	public ReplicaManager(StorageService ss, int nodePort,
			String ipAddress, NodeCommunicationManager ncm
			, int nid) throws SocketException, UnknownHostException{
		storageService = ss;
		socket = new DatagramSocket(nodePort, InetAddress.getByName(ipAddress));
		isRunning = new AtomicBoolean(true);
		this.nodePort = nodePort;
		threadPool = Executors.newCachedThreadPool();
		nodeCommManager = ncm;
		nodeId = nid;
	}
	
	@Override
	public void run(){
		//sendUpdates();
		while (isRunning.get()) {
			try {
				Thread.sleep(10000);
				Map<String,Versioned<String>> dump = storageService.getStorageManager().getDump();				
				List<UpdateMessage> updates = new ArrayList<>();
				createUpdates(updates, dump);
				int clusterSize = nodeCommManager.getClusterSize();
				int replica1 = ((nodeId+1) % clusterSize);
				int replica2 = ((nodeId+2) % clusterSize);
				sendUpdates(replica1, updates);
				sendUpdates(replica2, updates);				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}
	
	/**
	 * Periodically send updates to the proper Replica 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws UnknownHostException 
	 */
	private void sendUpdates(int replica, List<UpdateMessage> updates) throws UnknownHostException, InterruptedException, ExecutionException
	{
		String replicaAddress = nodeCommManager.getMemberFromId(replica).getHost();
		int replicaPort = nodeCommManager.getRequestManager().getPort();
		for (UpdateMessage umsg : updates){ // TODO: remove reply and print message, only put to do a check
			/*Message reply =*/ nodeCommManager.getRequestManager().sendMessage(
					umsg, replicaAddress, replicaPort);
			/*System.out.println("The response status of the update message sent by node " +
					nodeId + " is : " + reply.getMessageStatus());*/
		}		
	}
	
	public void shutdown(){
		isRunning.set(false);
	}
	
	
	public void createUpdates(List<UpdateMessage> updates, Map<String,Versioned<String>> dump){		
		UpdateMessage umsg = new SizedBackupMessage();
		for (Entry<String, Versioned<String>> e : dump.entrySet()){
			umsg.put(e.getKey(), e.getValue());
			if (umsg.isFull()){
				updates.add(umsg);
				umsg = new SizedBackupMessage();
			}
		}
		if (!umsg.isEmpty())
			updates.add(umsg);		
	}

}
