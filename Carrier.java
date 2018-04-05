
import java.util.*;
/**
 * Carrier the carrier which holds the communication channel
 * 
 * @author Manzoor Ahmed 
   @version: 1.0
 */
public class Carrier {

	public static class Channel extends Thread {

		public boolean isChannelBusy = false;
		private int stationsUsingChannel = 0;
		private  ArrayList<Station> allStations;

		/**
		 * The Channel which each Staion communicates 
		 * 
		 * **/
		public Channel(){

			//This carrier allows only 100 stations 
			this.allStations = new ArrayList<Station>(100);
		}

		/**
		 * Adds a new station to the channel
		 * @param  station  a new station that wants to use the channel
		 * @return      returns true if a new station was added to the channel
		 */
		public synchronized boolean addNewStation(Station station){

			return allStations.add(station);
		}

		/**
		 * Removes existing station from the channel
		 * @param  station  a station that needs to be removed from the channel
		 * @return      returns true if the station was removed from the channel
		 */

		public synchronized boolean removeStation(Station station){
			return allStations.remove(station);
		}

		/**
		 * Get all existing station from the channel
		 * @return      returns all active channels from the channel
		 */
		public synchronized ArrayList<Station> getAllCarrierStations(){
			return this.allStations;
		}

		/** Broadcast your status to Stations
		 *  @return current channel status
		 */
		public synchronized boolean isChannelBusy(Station readyStation){

			try {

				Thread.sleep(100);
				if(this.stationsUsingChannel > 1) {
					//if (allStations.get(index).isTransmitting == true) {

					isChannelBusy = true;
					System.out.println(" ---busy--- ");

				}else {
					isChannelBusy = false;
					System.out.println(" ---free--- ");
				}

			}catch( InterruptedException e ) {
				;
			}
			return isChannelBusy;
		}


		public synchronized int resolveContension(int[] stationsBackoff){

			int stationNumber = 0;

			for (int conflicts=0; conflicts <= allStations.size() -1 ; conflicts++) {

				if(allStations.get(conflicts).isDestination == true) {

				}else {

					// Get the Station with high back-off time and let it transmit first
					if(allStations.get(conflicts).getBackoffTime() == stationsBackoff[stationsBackoff.length -1 ]){

						// Get the station ip 
						Station s = allStations.get(conflicts);
						stationNumber = s.getIpaddress();
					}
				}
			}

			return stationNumber;
		}

		public synchronized int getContensionStationsBackoffTime() {

			int cont[] = new int[allStations.size() -1 ];

			for(int c =0; c<=this.allStations.size() -1; c++) {

				if(allStations.get(c).isDestination == true) {

				}else {
					if(this.allStations.get(c).getBackoffTime() > 2) {

						//these are the stations with extra back-off time
						cont[c] = allStations.get(c).getBackoffTime();
					}
				}
			}

			// Resolve contension for these stations
			return this.resolveContension(cont);
		}

		public synchronized int getStationsUsingChannelCount() {
			return stationsUsingChannel;
		}

		public synchronized void incrementChannelQueue() {
			this.stationsUsingChannel++;
		}
		public synchronized void incrementChannelQueueBy(int s) {
			this.stationsUsingChannel = s;
			this.isChannelBusy = false;
		}
		public synchronized void decrementChannelQueue() {
			this.stationsUsingChannel--;
		}

		public synchronized void remainingStationMustSetNAVto(int ip, int rtsTime) {

			for(int rts=0; rts<=this.allStations.size()-1;rts++) {

				if(allStations.get(rts).getIpaddress() == ip || 
						allStations.get(rts).isDestination == true) {

					// No this Station
				}else {

					// Set NAVS for other stations
					allStations.get(rts).waitNAVPeriod(rtsTime);
					decrementChannelQueue();
				}
			}
		}

	} // En
}