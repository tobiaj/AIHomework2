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

import java.util.ArrayList;

/**
 * Created by tobiaj on 2016-11-17.
 */
public class ArtistManager extends SuperAgent {

    private static ArrayList<AID> participants;
    private int receivedAnswers = 0;

    public void setup() {
        super.setup();
        System.out.println("The Artist manager agent " + getLocalName() + " has started");

        getAllCuratorAID();
        startAuction();

    }

    public class Auctioneer extends CyclicBehaviour {

        @Override
        public void action() {

            ACLMessage msg = new ACLMessage(ACLMessage.CFP);

            msg.setContent(String.valueOf(100));

            participants.forEach(msg::addReceiver);
            send(msg);

            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ReceivePropose receivePropose = new ReceivePropose(myAgent, messageTemplate, Long.MAX_VALUE, null, null);
            addBehaviour(receivePropose);

    }

    public class ReceivePropose extends MsgReceiver{
        public ReceivePropose(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {
            super.handleMessage(msg);

            if (receivedAnswers != participants.size()){
                ACLMessage reply = blockingReceive();
                System.out.println(reply.getContent());
                receivedAnswers++;
            }
            else {
                System.out.println("Taken all proposes");
            }
            System.out.println("Fick " + receivedAnswers + " svar!");
        }

        @Override
        public void reset() {
            super.reset();
        }
    }
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
