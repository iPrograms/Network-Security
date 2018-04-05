
/***/

import java.util.*;

/** A single Station which can transmit and receive data through the Carrier Channel (medium)
	@Author : Manzoor Ahmed
	@Project CS268 hw3 802.11 simulation
	@Date 03/24/2018
	@Version 1.0
 */

class Station extends Thread {

	private String stationName;
	private int RTS_TIME;
	private int SIFS_TIME;
	private int DIFS_TIME;
	private int CTS_TIME;
	private int ACK_TIME;
	private int IFS_TIME;

	/*****************************************************************************
	 * 
	 * Station sending variables
	 * 
	 ******************************************************************************/

	public boolean isRTS_sent = false;
	public boolean isCTS_received = false;
	public boolean isACK_received = false;
	public boolean isreadyForTransmission = false;
	public boolean isTransmitting = false;
	public boolean isNAVset = false;
	public long transmitionTime;

	private String data;
	private int ipaddress;
	public boolean isDestination = false;
	private int backOffTime;

	private int maxContensionWindowSize = 100;

	/*****************************************************************************
	 * 
	 * Station receiving variables
	 * 
	 ******************************************************************************/

	public boolean shouldSendCTS = false;
	public boolean shouldSendACK = false;
	public boolean isRTS_recived = false;
	public boolean isCTS_sent = false;
	public boolean isACK_sent = false;
	private Station destination;
	private Station source;
	private Carrier.Channel mainChannel;

	/**
	 * @param stationName,
	 *            the station name
	 * @param stationnumber
	 * 			  the station number
	 * @param dataTosend
	 * 			  the data the Station wants to send
	 *@param backofftime
	 *		  	  BO time for this Station
	 *@param rtstime
	 *		      RTS time for this Station
	 *@param sifstime
	 *		      SIFS time for this Station
	 *@param ctstime 
	 *			  CTS time for this Station
	 *@param difstime
	 *			  DIFS time for this Station
	 *@param acktime
	 *			  ACK time for this Station
	 *@param ifstime
	 *		 	  IFS time for this Station
	 */

	public Station(String stationName, int stationnumber, String dataTosend, int backofftime, int rtstime, int sifstime, 
			int ctstime, int difstime, int acktime, int ifstime) {

		this.stationName = "["+stationName + ":" + stationnumber + "]\t " ;
		this.data = dataTosend;
		this.backOffTime = backofftime;
		this.RTS_TIME = rtstime;
		this.SIFS_TIME = sifstime;
		this.CTS_TIME = ctstime;
		this.DIFS_TIME = difstime;
		this.ACK_TIME = acktime;
		this.setIFS_TIME(ifstime);
		this.isDestination = false; // so destination can not send data, for simulation only
		this.ipaddress = stationnumber;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		try {

			// At random period this station wants to transmit


			if (this.isDestination == true) {
				this.isreadyForTransmission = false;
				this.isTransmitting = false;

			}else {

				// At random time try to transmit
				transmit();

				// Needs to send data
				if(isreadyForTransmission) {

					//Check if Channel lets you
					if(this.mainChannel.isChannelBusy(this) == false) {

						// Channel increment how many stations wants to transmit through you
						mainChannel.incrementChannelQueue();

						// Keep other Stations quite for my RTS time
						///mainChannel.remainingStationMustSetNAVto(this.getIpaddress(), this.getRTSTime());
						
						
						// Channel is clear !
						if(mainChannel.isChannelBusy(this) == false) {

							
							// I can transmit now
							isTransmitting = true;
							
							// I will wait IFS time, and if the Channel is clear  I will trasmit
							waitIFS();
							Thread.sleep(IFS_TIME * 1000);
							
							mainChannel.remainingStationMustSetNAVto(this.getIpaddress(), getRTSTime());
							
							sendRTS();
							Thread.sleep(1000);
							destination.isRTS_recived = true;

							// The destination recived my RTS 
							if(getDestinationStation().isRTS_recived == true) {
	
								Thread.sleep(this.SIFS_TIME * 1000);
								waitSIFS();
	
								// Wait...
								Thread.sleep(this.CTS_TIME * 1000);
								sendCTS();
									
								destination.isCTS_sent = true;
								source.isCTS_received = true;
								
								if (getSourceStation().isCTS_received == true) {
	
									Thread.sleep(this.DIFS_TIME);
									waitDIFS();
	
									Thread.sleep(this.data.length());
									trasmitDataToDestination();
	
									Thread.sleep(this.SIFS_TIME * 1000);
									waitSIFS();
	
									Thread.sleep(this.ACK_TIME * 1000);
									sendACK();

									if (getSourceStation().isACK_received == true) {
	
										System.out.println("\nDone");
	
										mainChannel.decrementChannelQueue();
	
										try {
	
											notifyAll();
										} catch (IllegalMonitorStateException imse) {
	
										}
									}
								}
						else {
								this.setBackOffTime();
							 }
							}else {
								System.out.println("--busy---");
								this.setBackOffTime();
							}	
						}

					}else {

						//Channel is busy, set your backoff time
						System.out.println("---busy---");
						setBackOffTime();
					}

				}

			}

		}catch( InterruptedException e) {

		}

	}

	/** Start transmitting when data is available at Random time */
	/**
	 * 
	 */
	public synchronized void transmit() {

		if (this.isDestination == true) {
			isreadyForTransmission = false;
			isTransmitting = false;

		} else {
			try {
				System.out.println();
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`");
				Thread.sleep(new Random().nextInt(10000));

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setTransmisionTime(System.currentTimeMillis());
			isreadyForTransmission = true;
			isTransmitting = true;
			System.out.println(getStationName() + " want to transmit at t=: " + System.currentTimeMillis());
		}

	}

	/**
	 * @param currentTimeMillis
	 * 		The time when station wants to transmit
	 * 
	 */
	private synchronized void setTransmisionTime(long currentTimeMillis) {
		this.transmitionTime = currentTimeMillis;
	}

	/**
	 * @return transmision time 
	 */
	public synchronized long getTransmisionTime() {
		return this.transmitionTime;
	}

	/**
	 * send ACK to destination 
	 */
	private synchronized void sendACK() {

		System.out.println(this.getStationName() + " <---[ACK]--- " + this.getDestinationStation().getStationName() + "  "+ "\t t=: " +System.currentTimeMillis());
		this.getDestinationStation().isACK_received = true;
	}

	/**
	 * The station can transmit so send RTS destination
	 * @return the RTS time of this Station
	 */
	private synchronized int sendRTS() {

		// The data to send
		this.isRTS_sent = true;
		System.out.println(source.getStationName() + " ---[RTS]---> " + this.destination.getStationName() + "  "+  "\t t=: " + System.currentTimeMillis());

		// What is your RTS time?

		//destination.isRTS_recived = true;

		// Tell the carrier to clear channel for this period
		int myrtsTime = setRTSTime();
		return myrtsTime;
	}

	/**
	 * @param period
	 * 		the period the Station needs to set it's NAV
	 */
	public synchronized void waitNAVPeriod(int period) {

		// Destination Station does not need to set DIF or BO
		if (this.isDestination == true) {

		} else {
				
				System.out.println(getStationName() + " [---NAV"+ period *1000 + "ms"+ "-----]   "+  "\t t=: " +System.currentTimeMillis());
				
					Timer t = new Timer();
					t.schedule(new TimerTask() {
						
						public void run() {
							isreadyForTransmission = false;
							isTransmitting = false;
							isNAVset = true;
						}
					}, period * 1000);
					t.cancel();	
				
			// You can transmit now
			this.isreadyForTransmission = true;
			this.isTransmitting = true;
			this.isNAVset = false;
			//this.mainChannel.incrementChannelQueueBy(1);
		}
	}

	/**
	 * The Station is ready to send, but it needs to check if the carrier is free
	 *
	 * @return true if the Channel is busy
	 * 			false otherwise
	 */
	public synchronized boolean senseChannel() {

		return mainChannel.isChannelBusy(this);
	}

	/**
	 * The destination Station sends CTS after reciving RTS
	 */
	public synchronized void sendCTS() {

		// Did I get an RTS?
		if (this.getDestinationStation().isRTS_recived == true) {

			// Send CTS to destination
			System.out.println(getStationName() + " <---[CTS]--- " + this.getDestinationStation().getStationName() + "  "+  "\t t=: " + System.currentTimeMillis());

			//this.getSourceStation().isCTS_received = true;
		}
	}

	/**
	 * Waits SIF period
	 */
	public synchronized void waitSIFS() {

		System.out.println(getStationName() + " ---[ SIFS ]--- " + this.getDestinationStation().getStationName() + "  "+ "\t t=: " + System.currentTimeMillis());
	}

	/**
	 * Waits DIFS time
	 */
	public synchronized void waitDIFS() {

		System.out.println(getStationName() + " ---[DIFS]--- " + this.getDestinationStation().getStationName() + "  "+ "\t t=: "  + System.currentTimeMillis());
	}

	/**
	 * @return the current Station name
	 */
	public synchronized String getStationName() {
		return this.stationName;
	}

	/**
	 * @param stationName	
	 * 		 Give the Station name
	 */
	public synchronized void setStationName(String stationName) {
		this.stationName = stationName;
	}

	/** In case we want to change SIFS value for this station */
	/**
	 * @param newSIFSvalue
	 * 		  set SIFS time 
	 */
	public synchronized void setSIFSTime(int newSIFSvalue) {

		this.SIFS_TIME = newSIFSvalue;
	}

	/**
	 * @param newDIFSvalue
	 * 		set DIFS time
	 */
	public synchronized void setDIFSTime(int newDIFSvalue) {

		this.DIFS_TIME = newDIFSvalue;
	}

	public synchronized void waitIFS() {
		System.out.println(this.getStationName() + " ---[IFS]--- " + getDestinationStation().getStationName() + "  "+ "\t t= "+ System.currentTimeMillis());
	}
	/**
	 * @param newACKvalue
	 * 		set ACK time
	 */
	public synchronized void setACKTime(int newACKvalue) {
		this.ACK_TIME = newACKvalue;
	}

	/** Set back-off time so the carrier knows your turn 
	 *  
	 **/
	public synchronized void setBackOffTime() {

		// Are you waiting for a while?
		if (this.backOffTime >= this.maxContensionWindowSize) {

			System.out.println(this.stationName + " aborting after " + this.backOffTime + "." + "  "+ "\t t:= " + System.currentTimeMillis());
			try {
				this.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
				this.backOffTime+= (int) Math.pow(this.backOffTime, 2);
				
				Timer t = new Timer();
				t.schedule(new TimerTask() {
					
					public void run() {
						isreadyForTransmission = false;
						isTransmitting = false;
					}
				}, backOffTime * 1000);
				// Wait this period, someone is sending...
				// Set your back-off time and can not transmit
				
				
				System.out.println(getStationName()+"[BO "+backOffTime + "ms]");
				
			
			this.isreadyForTransmission = true;
		}
	}

	/**
	 * @return the BO time for this Station
	 */
	public synchronized int getBackoffTime() {
		return backOffTime;
	}

	/**
	 * @return the RTS time for this Station
	 */
	public synchronized int getRTSTime() {
		setRTSTime();
		return this.RTS_TIME;
	}

	/**
	 * @return the new RTS time
	 */
	public synchronized int setRTSTime() {
		this.RTS_TIME = 1 + this.SIFS_TIME + this.CTS_TIME + this.DIFS_TIME + this.SIFS_TIME + this.data.length()
		+ this.SIFS_TIME + this.ACK_TIME;
		return this.RTS_TIME;
	}

	/**
	 * Start transmiting to destination
	 */
	public void trasmitDataToDestination() {
		synchronized (this) {
			// Start transmitting...
			System.out.println(this.getSourceStation().getStationName() + " ---[ FRAME ]---> " + " "
					+ this.getDestinationStation().getStationName() + "  "+  "\t t=: " + System.currentTimeMillis());
			// The source received the ACK
			this.isACK_received = true;
		}
	}

	/**
	 * @param s
	 * 		the source Stations, this
	 */
	public synchronized void setSourceStation(Station s) {
		this.source = s;
	}

	/**
	 * @param d
	 * 		the destination Station to establish communication with
	 */
	public synchronized void setDestinationStation(Station d) {
		this.destination = d;
	}

	/**
	 * @return
	 * 		the source Station
	 */
	public synchronized Station getSourceStation() {
		return this.source;
	}

	/**
	 * @return 
	 * 		the destination Station
	 */
	public synchronized Station getDestinationStation() {
		return this.destination;
	}

	/**
	 * @return
	 * 		the IP address of this station, so it could be used for 
	 * 		resolving contension
	 */
	public synchronized int getIpaddress() {
		return ipaddress;
	}

	/**
	 * @param ipaddress
	 * 		  set the ipaddress for this Station
	 */
	public synchronized void setIpaddress(int ipaddress) {
		this.ipaddress = ipaddress;
	}

	/**
	 * @param carrierChannel
	 * 		the communication channel for this Station
	 * 
	 * */
	public void setCommunicationChannel(Carrier.Channel carrierChannel) {
		this.mainChannel = carrierChannel;

	}

	/**
	 * @return the iFS_TIME
	 */
	public int getIFS_TIME() {
		return IFS_TIME;
	}

	/**
	 * @param iFS_TIME the iFS_TIME to set
	 */
	public void setIFS_TIME(int iFS_TIME) {
		IFS_TIME = iFS_TIME;
	}

}
