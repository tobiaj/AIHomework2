package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import userAndArtifacts.Artifacts;

import java.util.ArrayList;

/**
 * Created by tobiaj on 2016-11-17.
 */
public class ArtistManager extends SuperAgent {
    private Artifacts artifact;

    private static ArrayList<AID> participants;
    private static ArrayList<AID> toCheck;
    public static int receivedAnswers = 0;
    public static boolean auctionEnd = false;

    public void setup() {
        super.setup();
        System.out.println("The Artist manager agent " + getLocalName() + " has started");

        startingPoint();

    }

    private void startingPoint() {
        auctionEnd = false;
        getAllCuratorAID();
        artifact = new Artifacts();

        addBehaviour(new WakerBehaviour(this, 5000) {
            @Override
            protected void onWake() {
                System.out.println("STARTING!!!!!!!!!!!!!!!!");
                startAuction();
            }
        });

    }

    public class Auctioneer extends OneShotBehaviour {


        public void action() {

            MessageTemplate messageTemplateAccept = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            MessageTemplate messageTemplateDeny = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);

            ReceiveProposeAccept receiveProposeAccept = new ReceiveProposeAccept(myAgent, messageTemplateAccept, Long.MAX_VALUE, null, null);
            ReceiveProposeDeny receiveProposeDeny = new ReceiveProposeDeny(myAgent, messageTemplateDeny, Long.MAX_VALUE, null, null);

            addBehaviour(receiveProposeAccept);
            addBehaviour(receiveProposeDeny);

        }


        public class ReceiveProposeAccept extends MsgReceiver {
            public ReceiveProposeAccept(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
                super(a, mt, deadline, s, msgKey);
            }

            @Override
            protected void handleMessage(ACLMessage msg) {

                msg.addReceiver(msg.getSender());
                msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                send(msg);
                auctionEnd = true;

            }

            @Override
            public int onEnd() {
                startingPoint();
                return super.onEnd();
            }
        }
    }

    public class ReceiveProposeDeny extends MsgReceiver {
        public ReceiveProposeDeny(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {
            super.handleMessage(msg);

            if (!auctionEnd) {
                toCheck.remove(msg.getSender());

                if (toCheck.size() == 0) {
                    System.out.println("Taken all answers");
                    double price = getNewProposal();

                    addBehaviour(new WakerBehaviour(myAgent, 1000) {
                        @Override
                        protected void onWake() {
                            ACLMessage newResponse = new ACLMessage(ACLMessage.INFORM);
                            participants.forEach(newResponse::addReceiver);
                            newResponse.setContent(String.valueOf(price));

                            send(newResponse);
                            receivedAnswers = 0;
                            createToCheck();
                        }
                    });

                }
            }
        }
        @Override
        public int onEnd() {
            myAgent.addBehaviour(this);
            return super.onEnd();
        }
    }

    private double getNewProposal() {
        double newPrice = (int) artifact.getInitialPrice() * 0.9;
        if (newPrice < artifact.getReservedPrice()){
            newPrice = artifact.getReservedPrice();
        }
        else{
            artifact.setInitialPrice(newPrice);
        }
        return newPrice;
    }

    private void startAuction() {
        ACLMessage initiate = new ACLMessage(ACLMessage.INFORM);
        initiate.setContent("START AUCTION");
        participants.forEach(initiate::addReceiver);
        send(initiate);

        Auctioneer auctioneer = new Auctioneer();
        addBehaviour(auctioneer);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent(String.valueOf(artifact.getInitialPrice()));

        participants.forEach(msg::addReceiver);
        send(msg);

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
        createToCheck();
    }

    private void createToCheck() {
        toCheck = new ArrayList<>(participants);

    }
}
