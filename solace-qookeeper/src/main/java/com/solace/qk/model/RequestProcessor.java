package com.solace.qk.model;

import com.solace.qk.Protocol;
import com.solace.qk.solace.SolServerWrapper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.solace.qk.Protocol.*;

public class RequestProcessor {
    static final private Logger logger = LoggerFactory.getLogger(RequestProcessor.class);

    public RequestProcessor(SolServerWrapper session, QKModel model, String outputStatusTopic) {
        this.session = session;
        this.model = model;
        this.serviceStatusTopic = outputStatusTopic;
    }

    /**
     * Dispatch any request type to the appropriate handler.
     * @param request client request object from JSON input
     * @param reqmsg the underlying source Solace requestmsg, this is required
     *               to properly route the reply in the request/reply model.
     */
    public void onRequest(JSONObject request, BytesXMLMessage reqmsg) {
        // Handle JSON request objects
        long msgtype = (long)request.get(MSGTYPE);
        try {
            if (msgtype == JOINREQUEST)
                handleJoinRequest(request, reqmsg);
            else if (msgtype == LEAVEREQUEST)
                handleLeaveRequest(request, reqmsg);
            else
                logger.warn("Unknown msgtype {}", msgtype);
            // Send out model update
            session.sendStatusUpdate(model.getModel(), this.serviceStatusTopic);
        }
        catch(Exception ex) {
            logger.error("EXCEPTION handling msgtype='"+ msgTypeString(msgtype), ex);
        }
    }


    /**
     * Handle a join request by identifying a queue with the fewest consumers, allocating this
     * client to that queue and tracking that mapping in the model, and sending a response
     * event back to the requester with the name of the queue they are to bind to.
     *
     * @param request request object including the client-name to be added to the Consumer Group.
     * @param reqmsg underlying Solace event being responded to.
     * @throws Exception
     */
    private void handleJoinRequest(JSONObject request, BytesXMLMessage reqmsg) throws Exception {
        // Choose the next queue for them
        String queueName = model.nextQueue();
        String clientName = (String)request.get(CLIENTNAME);
        logger.info("Assigning client [{}] to queue [{}]", clientName, queueName);
        model.addClient(queueName, clientName);
        // Send result
        session.sendJoinResult(reqmsg, queueName, clientName);
    }

    /**
     * Handle a leave request by identifying this client+queue and removing them from the
     * allocation model. This allows updated balancing of further consumers across the queues.
     *
     * @param request request object including the client-name and queue to be removed from the Consumer Group.
     * @param reqmsg underlying Solace event being responded to; can be null if client disconnected without sending leaverequest.
     * @throws Exception
     */
    private void handleLeaveRequest(JSONObject request, BytesXMLMessage reqmsg) throws Exception {
        String clientName = (String)request.get(CLIENTNAME);
        String queueName  = (String)request.get(QUEUENAME);
        if (queueName.equals("*")) {
            // remove from all
            logger.warn("TRYING TO REMOVE CLIENT: {} FROM ALL QUEUES IN THE GROUP.", clientName);
            int count = model.removeClient(clientName);
            logger.warn("Removed {} queue bindings for {}", count, clientName);
        }
        else if(!model.removeClient(queueName, clientName)) {
            logger.warn("Client thought they were leaving {" + queueName + "} but not removed");
        }
        // send leave result
        if (reqmsg != null)
            session.sendLeaveResult(reqmsg, queueName, clientName);
    }


    final private SolServerWrapper session;
    final private QKModel model;
    final private String serviceStatusTopic;
}
