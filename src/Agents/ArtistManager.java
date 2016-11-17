package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

import jade.core.behaviours.DataStore;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import userAndArtifacts.Artifacts;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tobiaj on 2016-11-17.
 */
public class ArtistManager extends SuperAgent {
    private Artifacts artifact;

    private static ArrayList<AID> participants;
    private int receivedAnswers = 0;

    public void setup() {
        super.setup();
        System.out.println("The Artist manager agent " + getLocalName() + " has started");

        artifact = new Artifacts();
        getAllCuratorAID();
        startAuction();

    }

    public class Auctioneer extends CyclicBehaviour {

        @Override
        public void action() {

            ACLMessage msg = new ACLMessage(ACLMessage.CFP);

            msg.setContent("item" + " ,price: " + artifact.getInitialPrice());

            participants.forEach(msg::addReceiver);
            send(msg);

            MessageTemplate messageTemplateAccept = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            MessageTemplate messageTemplateDeny = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);

            ReceiveProposeAccept receiveProposeAccept = new ReceiveProposeAccept(myAgent, messageTemplateAccept, Long.MAX_VALUE, null, null);
            ReceiveProposeDeny receiveProposeDeny = new ReceiveProposeDeny(myAgent, messageTemplateDeny, Long.MAX_VALUE, null, null);

            addBehaviour(receiveProposeAccept);
            addBehaviour(receiveProposeDeny);

        }

        public class ReceiveProposeAccept extends MsgReceiver{
            public ReceiveProposeAccept(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
                super(a, mt, deadline, s, msgKey);
            }

            @Override
            protected void handleMessage(ACLMessage msg) {
                super.handleMessage(msg);
                    ACLMessage reply = blockingReceive();
                System.out.println("Got a approve form curator agent: " + msg.getSender());
                reply.addReceiver(msg.getSender());
                reply.createReply();
                reply.setOntology("Winner");
                reply.setContent("Winner");
                send(reply);

            }

        }
    }

    public class ReceiveProposeDeny extends MsgReceiver{
        public ReceiveProposeDeny(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {
            super.handleMessage(msg);
            ACLMessage reply = blockingReceive();

            if (receivedAnswers != participants.size()){
                System.out.println("Got deny message containing: " + reply.getContent());
                receivedAnswers++;
            }
            else {
                System.out.println("Taken all proposes");
                receivedAnswers = 0;
                double price = getNewProposal();
                ACLMessage newResponse = new ACLMessage(ACLMessage.INFORM);
                newResponse.setContent("item" + " ,price: " + price);
                send(newResponse);
            }
        }

        @Override
        public void reset() {
            super.reset();
        }
    }

    private double getNewProposal() {
        double newPrice =  (int) artifact.getInitialPrice() * 0.9;
        artifact.setInitialPrice(newPrice);
        return  newPrice;
    }

    private void startAuction() {
        System.out.println("Hur ser participants ut" + participants);
        ACLMessage initiate = new ACLMessage(ACLMessage.INFORM);
        initiate.setContent("START AUCTION");
        participants.forEach(initiate::addReceiver);
        send(initiate);

        Auctioneer auctioneer = new Auctioneer();
        addBehaviour(auctioneer);
    }

    public void getAllCuratorAID() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("bidder");
        template.addServices(serviceDescription);
        participants = new ArrayList<>();

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                for (DFAgentDescription temp : result)
                    participants.add(temp.getName());
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
