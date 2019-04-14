package task2;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.main.Main;

/**
 * Класс реализуюет передачу сообщения из одной очереди Apache ActiveMQ в другую. 
 * Формат исходного сообщения base64. 
 * Программа пересылает сообщение в раскодированном виде. 
 * Добавлена обработка ошибок при получении пустого сообщения в виде вывода текста ошибки в консоль Java. 
 * Пустым сообщением считается сообщение, не содержащее ни одного символа.
 * @author Прилуков П.А.
 *
 */
public class Task2 {
	
	private Main camel;

	public static void main(String[] args) throws Exception {
		Task2 task = new Task2();
		task.execute();
	}
	
	public void execute() throws Exception {
		camel = new Main();		
		camel.addRouteBuilder(createRoute());		
		camel.bind("jms", JmsComponent.jmsComponentAutoAcknowledge(new ActiveMQConnectionFactory()));
		System.out.println("Press Ctrl+C to terminate JVM\n");
        camel.run();
	}
	
	/**
	 * Реализация маршрута сообщения из очереди source.queue в очередь target1.queue 
	 * с раскодированием исходного сообщения из base64 
	 *
	 */
	private RouteBuilder createRoute() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {	
				from("jms:source.queue")
					.doTry()
						.process(new EmptyChecker())
						.unmarshal().base64()
						.to("jms:target1.queue")
					.doCatch(EmptyMessageException.class)
						.transform().simple("${exception.message}")
						.to("stream:out")
					.end();
			}
		};
	}	
	
	/**
	 * Класс реализует обрабоку сообщения, передаваемого по маршруту
	 * Из условия задачи понятно, что сообщение текстовое.
	 *
	 */
	private class EmptyChecker implements Processor {

		public void process(Exchange exchange) throws Exception {
			Message msg = exchange.getIn();
			String txt = msg.getBody(String.class);
			if (txt == null || txt.isEmpty()) {
				throw new EmptyMessageException("Message is empty!");
			}
		}
	}
	
	/**
	 * Класс реализует исключение "пустое сообщение"
	 *
	 */
	@SuppressWarnings("serial")
	private class EmptyMessageException extends Exception {
		public EmptyMessageException(String responseString) {
			super(responseString);
		}
	}
}
