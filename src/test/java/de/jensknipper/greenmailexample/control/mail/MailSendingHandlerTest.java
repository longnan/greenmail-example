package de.jensknipper.greenmailexample.control.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.Message;
import javax.mail.MessagingException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
class MailSendingHandlerTest {

  @Autowired private MailSendingHandler mailSendingHandler;

  @Value("${mail.imap.host}")
  private String imapHost;

  @Value("${spring.mail.host}")
  private String smtpHost;

  @Value("${mail.imap.port}")
  private Integer imapPort;

  @Value("${spring.mail.port}")
  private Integer smtpPort;

  @Value("${spring.mail.username}")
  private String username;

  @Value("${spring.mail.password}")
  private String password;

  @Test
  public void testSend() throws MessagingException {
    final ServerSetup[] setup = {
      new ServerSetup(imapPort, imapHost, "imap"),
      new ServerSetup(smtpPort, smtpHost, "smtp")
    };
    final GreenMail greenMail = new GreenMail(setup);
    greenMail.setUser(username, password);

    final String recipient = GreenMailUtil.random();
    final String subject = GreenMailUtil.random();
    final String text = GreenMailUtil.random();

    greenMail.start();

    mailSendingHandler.send(recipient, subject, text);

    assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();
    final Message[] messages = greenMail.getReceivedMessages();
    greenMail.stop();

    assertThat(messages.length).isEqualTo(1);
    assertThat(messages[0].getSubject()).isEqualTo(subject);
    assertThat(GreenMailUtil.getBody(messages[0]).trim()).isEqualTo(text);
  }
}
