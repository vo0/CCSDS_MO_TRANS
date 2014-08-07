/**
 * 
 */
package esa.mo.mal.transport.tcpip.util;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.transport.MALMessage;

import esa.mo.mal.transport.tcpip.TCPIPMessage;
import esa.mo.mal.transport.tcpip.TCPIPTransport;

/**
 * This thread handles a new connection from a client.
 * It receives data from it (MAL packets) and then forwards the incoming packet
 * to an asynchronous processor in order to return immediately and not hold the calling
 * thread while the packet is processed. 
 * 
 * In case of a communication problem it informs the transport and/or closes
 * the resource (socket)
 * 
 * @author Petros Pissias
 *
 */
public class TCPIPConnectionDataReceiver extends Thread {

    //reference to the transport
    private final TCPIPTransport transport;
    //the socket this thrad mamages
    private final Socket socket;
    //the low level data Transceiver
    private final TCPIPTransportDataTransceiver transportTransceiver;
    //logger
    private final java.util.logging.Logger LOGGER = TCPIPTransport.LOGGER;
    //the remote URI (client) this socket is associated to. This is volatile as it is potentially set by a different thread after its creation
    private volatile String remoteURI = null;
         
    public TCPIPConnectionDataReceiver(TCPIPTransport transport, Socket socket) throws IOException {
	this.transport = transport;
	this.socket = socket;
	transportTransceiver = new TCPIPTransportDataTransceiver(this.socket);
	setName(getClass().getName());
    }

    @Override
    public void run() {
	// handles data reads from this client
	while (!interrupted()) {
	    try {
		byte[] malMsgData = transportTransceiver.readPacket();

		TCPIPInputDataForwarder dataProc = new TCPIPInputDataForwarder(transport, malMsgData, this);
		transport.submitIncomingDataTask(dataProc);

	    } catch (IOException e) {
		LOGGER.log(Level.WARNING, "Cannot read data from client", e);

		if (remoteURI != null) {
		    // this socket has already received some data, inform
		    // transport about comms problem
		    transport.communicationError(remoteURI);
		} else {
		    //close socket 
		    try {
			socket.close();
		    } catch (IOException e1) {
			//ignore
		    }		    
		}
		//and terminate
		break;
	    }

	}
    }

    public Socket getSocket() {
	return socket;
    }

    public String getRemoteURI() {
	return remoteURI;
    }

    public void setRemoteURI(String remoteURI) {
	this.remoteURI = remoteURI;
	setName(getClass().getName()+" URI:"+remoteURI);
    }

}