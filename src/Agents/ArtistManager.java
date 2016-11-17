package Agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

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

    public class Auction extends CyclicBehaviour {

        @Override
        public void action() {

            ACLMessage msg = new ACLMessage(ACLMessage.CFP);

            msg.setContent(String.valueOf(100));

            participants.forEach(msg::addReceiver);
            send(msg);

            addBehaviour(new CyclicBehaviour() {
                @Override
                public void action() {
                    if (receivedAnswers != participants.size()){
                        ACLMessage reply = blockingReceive();
                        receivedAnswers++;
                    }
                    System.out.println("Fick " + receivedAnswers + " svar!");
                    removeBehaviour(this);
                }
            });

        }
    }

    private void startAuction() {
        ACLMessage initiate = new ACLMessage(ACLMessage.INFORM);
        participants.forEach(initiate::addReceiver);
    }

    public void getAllCuratorAID() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("curatorAgent");
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
