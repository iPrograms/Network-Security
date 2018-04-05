import java.util.*;

/***
 * (Due on Monday,  April 2, 2018.)
 *
 * Follow the Wireless CSMA/CA explained in the PPT slides (including the flow chart and the frames transition chart example)
 * and write Java or C++ to implement the simulation of CSMA/CA. Consider at least 4 stations that are sharing a communication channel. 
 * These stations have to sense the channel before transmitting. If the channel is free, 
 * it should wait for IFS time and  again sense the channel if it is free,  calculate R wait for R slots and sense the channel and if it is free
 * send data otherwise it should use exponential back-off timer and try for retransmission later. Your simulator must allow users to enter 
 * all the required parameters such as those typical parameter values defined in IEEE802.11a, b, g, etc. (see PPT slides)
 **/

class Main{
	public static void main(String[] args) {

		if(args.length == 9) {
			ArrayList<Station> allStations;

			// The transmission medium or carrier
			Carrier.Channel carrierChannel = new Carrier.Channel();

			// Add as many as 100 stations
			for (int s=0; s <=7;s++ ) {

				//Add new stations to the carrier
				// String stationName, String dataTosend, int backofftime, int rtstime, int sifstime, 
				//int ctstime, int difstime, int acktime, int ifstime

				Station newStation = new Station(

						args[0],
						s,
						args[1], 
						Integer.parseInt(args[2]),
						Integer.parseInt(args[3]),
						Integer.parseInt(args[4]),
						Integer.parseInt(args[5]),
						Integer.parseInt(args[6]),
						Integer.parseInt(args[7]),
						Integer.parseInt(args[8]));

				newStation.setIpaddress(s);
				newStation.setCommunicationChannel(carrierChannel);

				//Add a new station to use the carrier channel
				carrierChannel.addNewStation(newStation);

			}



			//Get all the station that want to use this channel
			allStations = carrierChannel.getAllCarrierStations();

			System.out.println("****************************** ");
			System.out.println("Stations using channel:");
			System.out.println("****************************** ");



			//start communicating..

			// Source and destination
			// ---> 
			allStations.get(0).setSourceStation(allStations.get(0));
			allStations.get(0).setDestinationStation(allStations.get(1));
			allStations.get(1).isDestination = true;

			allStations.get(2).setSourceStation(allStations.get(2));
			allStations.get(2).setDestinationStation(allStations.get(3));
			allStations.get(3).isDestination = true;



			allStations.get(4).setSourceStation(allStations.get(4));
			allStations.get(4).setDestinationStation(allStations.get(5));
			allStations.get(5).isDestination = true;

			allStations.get(6).setSourceStation(allStations.get(6));
			allStations.get(6).setDestinationStation(allStations.get(7));
			allStations.get(7).isDestination = true;

			for (int stations =0; stations <= allStations.size() -1  ; stations++ ) {

				if(allStations.get(stations).isDestination == false) {
					System.out.print( allStations.get(stations).getStationName());
				}else {
					System.out.print(" ======== "+ allStations.get(stations).getStationName() + "\n");
				}
			}

			for (int s=0; s<=allStations.size() -1; s++) {
				allStations.get(s).start();
			}
		}else {

			System.out.println("--Usage:\n");
			System.out.println("stationName dataTosend BO RTS SIFS CTS DIFS ACK IFS");
			System.out.println("Station hi  3 10 1 1 6 1 1");
			System.out.println("\n");
			System.out.println("\n");

		}
	}
}