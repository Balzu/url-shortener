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
	private int backupId;
	private int backupInterval;
	
	
	public ReplicaManager(StorageService ss, int nodePort,
			String ipAddress, NodeCommunicationManager ncm
			, int nid, int backupInterval) throws SocketException, UnknownHostException{
		storageService = ss;
		socket = new DatagramSocket(nodePort, InetAddress.getByName(ipAddress));
		isRunning = new AtomicBoolean(true);
		this.nodePort = nodePort;
		threadPool = Executors.newCachedThreadPool();
		nodeCommManager = ncm;
		nodeId = nid;
		this.backupInterval = backupInterval;
		// Assign a negative value, meaning that current node has not received the backup database from any node
		// yet (recall that node id can only be positive)
		backupId = -1; 
	}
	
	@Override
	public void run(){
		while (isRunning.get()) {
			try {
				Thread.sleep(backupInterval);
				Map<String,Versioned<String>> dump = storageService.getStorageManager().getDump();				
				List<UpdateMessage> updates = new ArrayList<>();
				createUpdates(updates, dump);
				
				
				//TODO: backup non con modulo
				
				//TODO: backupId a che serve??
				
				
				int clusterSize = nodeCommManager.getClusterSize();
				int replica1 = ((nodeId+1) % clusterSize);
			//	int replica2 = ((nodeId+2) % clusterSize);
				sendUpdates(replica1, updates);
		   //  sendUpdates(replica2, updates);				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}			
		}		
	}
	
	public int getBackupId() {
		return backupId;
	}

	public void setBackupId(int backupId) {
		this.backupId = backupId;
	}

	/**
	 * Send updates to a replica 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws UnknownHostException 
	 */
	private void sendUpdates(int replica, List<UpdateMessage> updates) throws UnknownHostException, InterruptedException, ExecutionException
	{
		String replicaAddress = nodeCommManager.getMemberFromId(replica).getHost();
		int replicaPort = nodeCommManager.getRequestManager().getPort();
		for (UpdateMessage umsg : updates){ 
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
		// It is the first update message of a possible sequence of updates, so the flag is set to true.
		// Following messages (if any) will have the flag set to false.
		UpdateMessage umsg = new SizedBackupMessage(nodeId, true);
		for (Entry<String, Versioned<String>> e : dump.entrySet()){
			umsg.put(e.getKey(), e.getValue());
			if (umsg.isFull()){
				updates.add(umsg);
				umsg = new SizedBackupMessage(nodeId, false);
			}
		}
		if (!umsg.isEmpty())
			updates.add(umsg);		
	}

}
