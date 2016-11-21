package Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import userAndArtifacts.Artifacts;

import java.util.Random;

/**
 * Created by tobiaj on 2016-11-09.
 */
public class CuratorAgent extends SuperAgent {
private double maxPrice;

    public void setup() {
        super.setup();

        String service = "bidder";

        registerService(this, service);

        maxPrice = Math.random() * 1000;

        System.out.println("The Curator agent " + getLocalName() + " has started and will accept " + maxPrice);


        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                startingPoint();
            }
        });

    }

    private void startingPoint() {

        MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate messageTemplateWinner = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);


        MessageReceiver messageReceiver = new MessageReceiver(this, messageTemplate, Long.MAX_VALUE, null, null);
        WinnerMessage winnerMessage = new WinnerMessage(this, messageTemplateWinner, Long.MAX_VALUE, null, null);

        addBehaviour(winnerMessage);
        addBehaviour(messageReceiver);

    }

    public class MessageReceiver extends MsgReceiver {
        public MessageReceiver(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {
            Auction auction = new Auction();
            addBehaviour(auction);

        }

        @Override
        public int onEnd() {
            myAgent.addBehaviour(this);
            return super.onEnd();
        }
    }

    public class WinnerMessage extends MsgReceiver {
        public WinnerMessage(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
            super(a, mt, deadline, s, msgKey);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {
            super.handleMessage(msg);
            System.out.println("Jag heter " + getLocalName() + " och jag vann!");

            startingPoint();
            //takeDown();
        }


    }

    public class Auction extends Behaviour {

        @Override
        public void action() {

            if (!ArtistManager.auctionEnd) {
                MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL));
                ACLMessage msg = myAgent.blockingReceive(mt);

                if (msg != null) {
                    ACLMessage reply = msg.createReply();

                    if (msg.getContent().equals("START AUCTION")){
                        reply.setPerformative(ACLMessage.INFORM);


                        }

                   else if (Double.parseDouble(msg.getContent()) <= maxPrice) {
                        System.out.println("Curator agent: " + getLocalName() + " sends propose");
                        reply.setPerformative(ACLMessage.PROPOSE);
                    } else {
                        System.out.println("Curator agent: " + getLocalName() + " sends refuse");

                        reply.setPerformative(ACLMessage.REFUSE);
                    }
                    send(reply);
                } else {
                    block();
                }
            }
            else{

                //takeDown();
                startingPoint();
            }
        }
        @Override
        public boolean done() {
            return false;
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("The Artist manager agent " + getLocalName() + " has ended");
    }
}
