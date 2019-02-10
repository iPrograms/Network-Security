
import java.util.*;
/**
 * Carrier the carrier which holds the communication channel
 * 
 * @author Manzoor Ahmed 
 *  @version: 1.0
 */
public class Carrier {

	public static class Channel extends Thread {

		public boolean isChannelBusy = false;
		private int stationsUsingChannel = 0;
		private  ArrayList<Station> allStations;
		private  ArrayList<Station> queuedStations;

		/**
		 * The Channel which each Staion communicates 
		 * 
		 * **/
		public Channel(){

			//This carrier allows only 100 stations 
			this.allStations = new ArrayList<Station>(100);
			this.queuedStations = new ArrayList<Station>(100);
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

			
				if(this.queuedStations.size() > 1 ) {
					//if (allStations.get(index).isTransmitting == true) {

					isChannelBusy = true;
					System.out.println(" ---busy--- ");
					
					resolveContension();

				}else {
					isChannelBusy = false;
					System.out.println(" ---free--- ");
				}

			return isChannelBusy;
		}
		
		/**
		 * 
		 * 
		 * 
		 * */
		public synchronized void queueStationForTransmitting(Station s) {
			this.queuedStations.add(s);
		}

		/**
		 * Resolve contention between Stations
		 * 
		 * 
		 * */
		public synchronized void resolveContension(){
			
			// Find the Station with less back-off time and let it transmit first
			
			Collections.sort(this.queuedStations);
			
			System.out.println("Contention Stations:\n ");
			for(int cont=0; cont<= this.queuedStations.size() -1; cont++) {
				System.out.println("" +queuedStations.get(cont).getStationName() + "x ");
			}
			// Let the Station with low back-off time go first 
			this.queuedStations.get(0).isTransmitting = true;
			this.queuedStations.get(0).isreadyForTransmission = true;
			this.queuedStations.get(0).isNAVset = false;
			
			System.out.println(this.queuedStations.get(0).getStationName() + " won contention");

			// Other Stations should back-off

			for (int s=1;s<=this.queuedStations.size()-1; s++ ) {
				
				this.queuedStations.get(s).setBackOffTime();
			}
			
			// Clean up contention  
			this.queuedStations.clear();
		}

	
		public synchronized int getStationsUsingChannelCount() {
			return stationsUsingChannel;
		}

		public synchronized void remainingStationMustSetNAVto(int ip, int rtsTime){

			for(int rts=0; rts<=this.allStations.size()-1;rts++) {

				if(allStations.get(rts).getIpaddress() == ip || 
						allStations.get(rts).isDestination == true) {

					
				}else {

					// Set NAVS for other stations
					allStations.get(rts).waitNAVPeriod(rtsTime);
					
				}
			}
		}

	} // En
}