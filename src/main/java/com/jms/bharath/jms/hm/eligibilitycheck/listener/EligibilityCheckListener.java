package com.jms.bharath.jms.hm.eligibilitycheck.listener;

import java.io.Serializable;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import com.jms.bharath.jms.hm.model.Patient;

public class EligibilityCheckListener implements MessageListener {

	@Override
	public void onMessage(Message message) {
		
		ObjectMessage objectMessage = (ObjectMessage) message;
		
		try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
				JMSContext jmsContext = cf.createContext();) {
			
			InitialContext initialContext = new InitialContext();
			Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");
			
			Patient patient = (Patient) objectMessage.getObject();
			String insuranceProvider = patient.getInsuranceProvider();
			
			MapMessage replyMessage = jmsContext.createMapMessage();
			
			System.out.println("Insurance Provider:" + insuranceProvider);
			
			if (insuranceProvider.equals("Blue Cross Blue Shield") || insuranceProvider.equals("United Health") ) {
				if (patient.getCopay()<40 && patient.getAmoutToBePayed()<1000) {
					replyMessage.setBoolean("eligible", true);
				} else {
					replyMessage.setBoolean("eligible", false);
				}
				
				JMSProducer producer = jmsContext.createProducer();
				producer.send(replyQueue, replyMessage);
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
