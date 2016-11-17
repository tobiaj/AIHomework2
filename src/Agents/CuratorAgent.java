package Agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREResponder;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import userAndArtifacts.Artifacts;
import userAndArtifacts.User;

import java.io.IOException;

/**
 * Created by tobiaj on 2016-11-09.
 */
public class CuratorAgent extends SuperAgent {
    private java.util.HashMap<Integer, Artifacts> listOfArtifacts;


    public void setup() {
        super.setup();
        System.out.println("The Curator agent " + getLocalName() + " has started");

        String service = "curatorAgent";
        registerService(this, service);

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();

        SequentialBehaviour seq = new SequentialBehaviour();

        MessageTemplate messageTemplateRequest = MessageTemplate.MatchOntology("artifactsRequest");
        MessageTemplate messageTemplateInfo = MessageTemplate.MatchOntology("artifactsInfo");

        ArtifactsRequest artifactsRequest = new ArtifactsRequest(this, messageTemplateRequest);
        ArtifactsInfo artifactsInfo = new ArtifactsInfo(this, messageTemplateInfo);

        parallelBehaviour.addSubBehaviour(artifactsRequest);
        parallelBehaviour.addSubBehaviour(artifactsInfo);

        seq.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                createListOfArtifacts();

            }
        });

        seq.addSubBehaviour(parallelBehaviour);

        addBehaviour(seq);

    }

    private void createListOfArtifacts() {

        listOfArtifacts = new java.util.HashMap<>();
        for (int i = 0; i < 20; i++) {
            Artifacts artifact = new Artifacts();
            listOfArtifacts.put(artifact.getId(), artifact);
        }
    }


    class ArtifactsRequest extends SimpleAchieveREResponder {

        public ArtifactsRequest(Agent agent, MessageTemplate messageTemplate) {
            super(agent, messageTemplate);
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
            System.out.println("The curator agent " + myAgent.getLocalName() + " received a artifact request to create tour based on user preferences");
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            try {
                User user = (User) request.getContentObject();
                ArrayList listOfArtifactsIDs = createTourForUser(user);
                try {
                    reply.setContentObject(listOfArtifactsIDs);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            return reply;
        }

        @Override
        public int onEnd() {
            myAgent.addBehaviour(this);
            return super.onEnd();
        }

        private ArrayList createTourForUser(User user) {

            ArrayList listOfArtifactsIDs = new ArrayList();

            for (Artifacts artifact : listOfArtifacts.values()) {
                if (artifact.getGenre().equals(user.getInterest())){
                    listOfArtifactsIDs.add(artifact.getId());
                }
            }

            return listOfArtifactsIDs;
        }
    }


    class ArtifactsInfo extends SimpleAchieveREResponder {

        public ArtifactsInfo(Agent agent, MessageTemplate messageTemplate) {
            super(agent, messageTemplate);
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
            System.out.println("The curator agent " + myAgent.getLocalName() + " received a artifact information request to return information about artifacts");

            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            try {
                ArrayList listOfArtifactsIDs = (ArrayList) request.getContentObject();
                ArrayList listOfArtifactsInfo = getInfoAboutArtifacts(listOfArtifactsIDs);
                try {
                    reply.setContentObject(listOfArtifactsInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            return reply;
        }

        private ArrayList getInfoAboutArtifacts(ArrayList listOfArtifactsIDs) {

            ArrayList artifactsToReturnWithInformation = new ArrayList();

            Iterator it = listOfArtifactsIDs.iterator();

            while(it.hasNext()){
                Artifacts artifact = listOfArtifacts.get(it.next());
                if (artifact != null){
                    artifactsToReturnWithInformation.add(artifact);
                }

            }

            return artifactsToReturnWithInformation;
        }

        @Override
        public int onEnd() {
            myAgent.addBehaviour(this);
            return super.onEnd();
        }


    }


}
