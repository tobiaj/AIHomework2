package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.states.MsgReceiver;

import java.io.IOException;

/**
 * Created by tobiaj on 2016-11-09.
 */
public class TourGuideAgent extends SuperAgent {

    @Override
    protected void setup() {
        //calls the parent constructor with no arguments

        super.setup();
        System.out.println("The tour guide agent " + getLocalName() + " has started");
        System.out.println(getName());

        String service = "TourGuideAgent";

        registerService(this, service);

        waitForTourRequestMessages();

    }

    private void waitForTourRequestMessages() {

        MessageTemplate tourGuideRequest = MessageTemplate.MatchOntology("tour");

        MessageReceiver messageReceiver = new MessageReceiver(this, tourGuideRequest, Long.MAX_VALUE, null, null);

        addBehaviour(messageReceiver);

    }


    class MessageReceiver extends MsgReceiver {

        public MessageReceiver(Agent myAgent, MessageTemplate messageTemplate, long deadline, DataStore ds, Object msgKey) {
            super(myAgent, messageTemplate, deadline, ds, msgKey);
        }

        public void handleMessage(ACLMessage message) {

            System.out.println("The Tour guide agent " + myAgent.getLocalName() + " received a tour request from the profiler agent");

            AID AID = getCuratorAID(TourGuideAgent.this);

            System.out.println("Creating a ACL message for requesting artifacts from curator agent based in preferences");
            ACLMessage requestToCurator = new ACLMessage(ACLMessage.REQUEST);
            requestToCurator.setOntology("artifactsRequest");
            requestToCurator.addReceiver(AID);

            try {
                requestToCurator.setContentObject(message.getContentObject());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            HandleTourRequestMessage handle = new HandleTourRequestMessage(TourGuideAgent.this, requestToCurator, message);
            addBehaviour(handle);

        }
    }

    public class HandleTourRequestMessage extends SimpleAchieveREInitiator {
        private ACLMessage originalMessage;

        public HandleTourRequestMessage(Agent agent, ACLMessage requestToCurator, ACLMessage originalMessage) {
            super(agent, requestToCurator);
            this.originalMessage = originalMessage;
        }

        @Override
        protected ACLMessage prepareRequest(ACLMessage msg) {
            return super.prepareRequest(msg);
        }

        @Override
        protected void handleInform(ACLMessage msg){
            super.handleInform(msg);

            System.out.println("The tour guide agent " + myAgent.getLocalName() + " Received artifacts from the curator \n");
            System.out.println("Creating a reply message to the profiler with the created virtual tour");
            ACLMessage reply = originalMessage.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setOntology("reply");

            try {
                reply.setContentObject(msg.getContentObject());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            send(reply);
        }

        @Override
        public int onEnd() {
            //myAgent.addBehaviour(this);
            waitForTourRequestMessages();
            return super.onEnd();
        }
    }

}

