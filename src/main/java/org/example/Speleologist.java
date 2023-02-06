package org.example;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.concurrent.CyclicBarrier;

import static java.util.Objects.nonNull;

public class Speleologist extends Agent {

    @Override
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("speleologist");
        sd.setName("Wumpus-Speleologist-Agent");

        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new LocationRequestServer());
        addBehaviour(new ActionTakingServer());
    }

    private static class LocationRequestServer extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (nonNull(msg)) {
                ACLMessage reply = msg.createReply();

                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("Cave with 3 tunnels");

                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private static class ActionTakingServer extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);
            if (nonNull(msg)) {
                String action = msg.getContent();
                System.out.println("Speleologist received suggested action:" + action);

                ACLMessage reply = msg.createReply();

                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                System.out.println("Speleologist performs the suggested action:" + action);
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
