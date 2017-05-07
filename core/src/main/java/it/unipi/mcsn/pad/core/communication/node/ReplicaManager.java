package it.unipi.mcsn.pad.core.communication.node;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.code.gossip.GossipMember;
import com.google.code.gossip.LocalGossipMember;
import com.google.code.gossip.manager.GossipManager;

import it.unipi.mcsn.pad.core.message.SizedBackupMessage;
import it.unipi.mcsn.pad.core.message.UpdateMessage;
import it.unipi.mcsn.pad.core.storage.StorageManager;
import it.unipi.mcsn.pad.core.storage.StorageService;
import voldemort.versioning.Versioned;

public class ReplicaManager extends Thread{
	
	private StorageService storageService;	
	//private DatagramSocket socket = null;
	private AtomicBoolean isRunning;
	private final ExecutorService threadPool;
	private int nodePort;
	private NodeCommunicationManager nodeCommManager;
	private int nodeId;
	private int backupSenderId;
	private int backupSenderIdTemp;
	private int backupInterval;
	private List<String> removed;
	
	
	public ReplicaManager(StorageService ss, int nodePort,
			String ipAddress, NodeCommunicationManager ncm
			, int nid, int backupInterval) throws SocketException, UnknownHostException{
		storageService = ss;
		//socket = new DatagramSocket(nodePort, InetAddress.getByName(ipAddress));
		isRunning = new AtomicBoolean(true);
		this.nodePort = nodePort;
		threadPool = Executors.newCachedThreadPool();
		nodeCommManager = ncm;
		nodeId = nid;
		this.backupInterval = backupInterval;
		// Assign a negative value, meaning that current node has not received the backup database from any node
		// yet (recall that node id can only be positive)
		backupSenderId = -1; 
		backupSenderIdTemp = -1; 
		removed = null;
	}
	
	@Override
	public void run(){
		while (isRunning.get()) {
			try {
				Thread.sleep(backupInterval);
				Map<String,Versioned<String>> dump = storageService.getStorageManager().getDump();				
				List<UpdateMessage> updates = new ArrayList<>();
				createUpdates(updates, dump);					
				int replica = findBackup();
				if (replica >= 0)
					sendUpdates(replica, updates);						   				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				// Skip this sending bacause the backupNode is down, so have to compute another replica
			}
			  catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				//e.printStackTrace();
			} catch (NoSuchElementException e) {
				if (isRunning.get()) // Only if this node is running I print the error stack trace
					e.printStackTrace();	
			} catch (IllegalAccessError e) {
				if (isRunning.get()) // Only if this node is running I print the error stack trace
					e.printStackTrace();				
			}
			
		}		
	}
	
	
	public int findBackup(){
		GossipManager gManager = nodeCommManager.getNodeCommunicationService()
				.getGossipService().get_gossipManager();
		List<LocalGossipMember> members = new ArrayList<>(gManager.getMemberList());	
		if (members.size() == 0) // Not connected to any member, so return (better throw exception)
			return -1;
		LocalGossipMember myself = gManager.getMyself();
		NavigableMap<Integer, Integer> upMemberssMap = new ConcurrentSkipListMap<>();
		for(int i=0; i<members.size(); i++)
	    // Map is redundant actually, I only use it for its capability to return the "ceiling" of the key
			upMemberssMap.put(Integer.parseInt(members.get(i).getId()),
					Integer.parseInt(members.get(i).getId())); 
		Integer backupMemberId = upMemberssMap.ceilingKey(Integer.parseInt(myself.getId()));
		return backupMemberId!= null ? backupMemberId
				: upMemberssMap.firstKey();
	}
	
	
	
	public int getBackupId() {
		return backupSenderId;
	}

	public void setBackupId(int backupId) {
		this.backupSenderId = backupId;
	}
	
	public int getBackupIdTemp() {
		return backupSenderIdTemp;
	}

	public void setBackupIdTemp(int backupId) {
		this.backupSenderIdTemp = backupId;
	}

	/**
	 * Delivers the backup DB to a node by sending more UpdateMessages
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws UnknownHostException 
	 */
	public void sendUpdates(int replica, List<UpdateMessage> updates) 
			throws UnknownHostException, InterruptedException, ExecutionException, NullPointerException
	{
		String replicaAddress = nodeCommManager.getMemberFromId(replica).getHost();		
		int replicaPort = nodeCommManager.getRequestManager().getPort();
		for (UpdateMessage umsg : updates){ 
			nodeCommManager.getRequestManager().sendMessage(
					umsg, replicaAddress, replicaPort);
		
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
		if (!umsg.isEmpty() || umsg.isFirst())
			updates.add(umsg);		
	}
	
	/**
	 *   After crashed node resumes execution, keep in the Primary DB only the urls
	 *   assigned to this node, and put in the backup DB all the other urls
	 */
	public void partitionUrlsBetweenDB(){
		StorageManager sman = storageService.getStorageManager();
		Map<String,Versioned<String>> dump = sman.getDump();	
		int me = nodeId;
		for (Entry<String, Versioned<String>> e :dump.entrySet()){
			String surl = e.getKey();	
			int primary = nodeCommManager.findPrimary(surl);
			if ( primary != me){
				sman.remove(surl);
				sman.storeBackup(surl, e.getValue());
			}
		}
	}
	
	public void setRemoved(){
		removed = new ArrayList<>();
	}
	
	public void unsetRemoved(){
		removed = null;
	}
	
	public boolean isSetRemoved(){
		return (removed == null) ? false : true;
	}
	
	public List<String> getRemoved(){
		return removed;
	}
	
	/** If the member with id = mid belongs to the list up dead members,
	 *  adds it to the list of alive members
	 */
	public void addMemberToGossipListIfDead(int mid)
	{
		GossipManager gman = nodeCommManager.getNodeCommunicationService().
				getGossipService().get_gossipManager();
		for (LocalGossipMember dead: gman.getDeadList()){
			if (Integer.parseInt(dead.getId()) == mid)
				gman.createOrRevivieMember(dead);
		}			
	}

}
