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


    public void setup() {
        super.setup();
        System.out.println("The Curator agent " + getLocalName() + " has started");

        String service = "bidder";

        registerService(this, service);

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
        }

    }

    public class Auction extends Behaviour {

        @Override
        public void action() {

            if (!ArtistManager.auctionEnd) {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.blockingReceive(mt);

                int rand = new Random().nextInt(100 + 1);

                if (msg != null && ArtistManager.receivedAnswers <= 1) {
                    ACLMessage reply = msg.createReply();
                    if (rand > 90) {
                        reply.setPerformative(ACLMessage.PROPOSE);
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                    }
                    send(reply);
                } else {
                    block();
                }
            }
            else{
                startingPoint();
            }
        }
        @Override
        public boolean done() {
            return false;
        }
    }
}
