package org.jaggeryjs.hostobjects.email;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMElement;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.wso2.javascript.xmlimpl.XML;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * <p/>
 * The sender host object allows users to send out email from their mashups.  It helps notify users
 * of certain events and acts as a bridge between mashups and users.
 * <p/>
 * Notes:
 * <p/>
 * The constructor of the Emal object can be called with or without user credentials. If its called with credentials
 * they are used to authenticate the user. If the function is called without credentials the details
 * are taken from the server.xml found under conf directory where the mashup server is located.
 * So if you wish to keep the credentials in server.xml please update it with the needed usernames
 * and passwords. The section that corresponds to this is as follows.
 * <p/>
 * <p>
 * <!--Used to configure your default email account that will be used to send emails from mashups using the Sender host Object-->
 * <SenderConfig>
 * <host>smtp.gmail.com</host>
 * <port>25</port>
 * <username>username@gmail.com</username>
 * <password>password</password>
 * </SenderConfig>
 * <p/>
 * </p>
 * <p/>
 * <pre>
 * eg:
 * <p/>
 *     function sendEmail(){
 *          var email = new Sender("host", "port", "username", "password");
 *          var file = new File("temp.txt");
 *          email.from = "keith@wso2.com";
 *          email.to = "keith@wso2.com"; // alternatively message.to can be a array of strings. Same goes for cc and bcc
 *          email.cc = "keith@wso2.com";
 *          email.bcc = "keith@wso2.com";
 *          email.subject = "WSO2 Mashup server 1.0 Released";
 *          email.addAttachement(file, "temp.txt"); // Optionally can add attachements, it has a variable number of arguments. each argument can be a File hostObject or a string representing a file.
 *          email.text = "WSO2 Mashup server 1.0 was Released on 28th January 2008";
 *          email.send();
 *     }
 * <p/>
 * </pre>
 * </p>
 */
public class SenderHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(SenderHostObject.class);
    private Properties properties;
    private MimeMessage message;
    private String text;
    private String html;
    Multipart multipart;

    /**
     * Return the name of the class.
     * <p/>
     * This is typically the same name as the constructor.
     * Classes extending ScriptableObject must implement this abstract
     * method.
     */
    public String getClassName() {
        return "Sender";
    }

    /**
     * <p>
     * The Sender Object has three different constructors. Choose one depending on your configuration
     * and your needs.
     * <p/>
     * 1. The first constructor takes no parameters and uses configuration information specified in the
     * server.xml. Using a configuration such as this is useful if you want to use a default email
     * account to send out mail from your mashups. It also reduces the hassle of having to key in
     * the configuration details each time you need a new email object.
     * <p/>
     * var email = new Sender();
     * <p/>
     * 2. The second constructor, unlike the first, requires the user to provide the configuration
     * details each time he creates a new email object.  The benefit is that no server configuration
     * is needed and you can use diffent accounts when ever you need. The configuration details
     * should be given as follows:
     * <p/>
     * var email = new Sender("smtp.gmail.com", "25", "username@gmail.com", "password"); // host, port, username, password
     * <p/>
     * 3. The third is a slight variant of the second. It does not require a port to be specified:
     * <p/>
     * var email = new Sender("smtp.gmail.com", "username@gmail.com", "password"); // host, username, password
     * </p>
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {

        SenderHostObject senderHostObject = new SenderHostObject();
        Properties props = new Properties();
        senderHostObject.properties = props;
        senderHostObject.multipart = new MimeMultipart();

        String host, username, password;
        String port = null;
        //ServerConfiguration serverConfig = ServerConfiguration.getInstance();

        int length = args.length;
/*        if (length == 3) {
            // We assume that the three parameters are host, username and password
            host = (String) args[0];
            username = (String) args[1];
            password = (String) args[2];
            port = serverConfig.getFirstProperty("SenderConfig.port");
        } else*/
        if (length == 4) {
            //We assume that the parameters are host, port, username and password
            host = (String) args[0];
            port = (String) args[1];
            username = (String) args[2];
            password = (String) args[3];
        } else {
            throw new ScriptException("Incorrect number of arguments. Please specify host, username, " +
                    "password or host, port, username, password within the constructor of Sender hostobject.");
        }

        if (host == null) {
            throw new ScriptException("Invalid host name. Please recheck the given details " +
                    "within the constructor of Sender hostobject.");
        }
        senderHostObject.setProperty("mail.smtp.host", host);

        if (port != null) {
            senderHostObject.setProperty("mail.smtp.port", port);
        }

        SMTPAuthenticator smtpAuthenticator = null;
        if (username != null) {
            smtpAuthenticator = new SMTPAuthenticator(username, password);
            senderHostObject.setProperty("mail.smtp.auth", "true");
        }
        Session session = Session.getInstance(props, smtpAuthenticator);
        senderHostObject.message = new MimeMessage(session);

        senderHostObject.setProperty("mail.smtp.starttls.enable", "true");

        return senderHostObject;
    }

    private void setProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * <p>The from address to appear in the sender</p>
     * <pre>
     * sender.from = "keith@wso2.com";
     * </pre>
     */
    public void jsSet_from(String from) throws ScriptException {
        try {
            message.setFrom(new InternetAddress(from));
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }
    }

    public String jsGet_from() throws MessagingException {
        String from = null;
        Address[] addresses = message.getFrom();
        if (addresses != null && addresses.length > 0) {
            from = addresses[0].toString();
        }
        return from;
    }

    public String[] jsGet_to() throws ScriptException {
        Address[] addresses;
        try {
            addresses = message.getRecipients(Message.RecipientType.TO);
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }
        String[] to = new String[addresses.length];
        for (int i = 0; i < to.length; i++) {
            to[i] = addresses[i].toString();
        }
        return to;
    }

    /**
     * <p>The to address that the mail is sent to</p>
     * <pre>
     * sender.to = "keith@wso2.com";
     *
     * OR
     *
     * var to = new Array();
     * to[0] = "jonathan@wso2.com";
     * to[1] =  "keith@wso2.com";
     * sender.to = to;
     * </pre>
     */
    public void jsSet_to(Object toObject) throws ScriptException {
        addRecipients(Message.RecipientType.TO, toObject);
    }

    public String[] jsGet_cc() throws ScriptException {
        Address[] addresses;
        try {
            addresses = message.getRecipients(Message.RecipientType.CC);
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }
        String[] cc = new String[addresses.length];
        for (int i = 0; i < cc.length; i++) {
            cc[i] = addresses[i].toString();
        }
        return cc;
    }

    /**
     * <p>The cc address that the mail is sent to</p>
     * <pre>
     * sender.cc = "keith@wso2.com";
     *
     * OR
     *
     * var cc = new Array();
     * cc[0] = "jonathan@wso2.com";
     * cc[1] =  "keith@wso2.com";
     * sender.cc = cc;
     * </pre>
     */
    public void jsSet_cc(Object ccObject) throws ScriptException {
        addRecipients(Message.RecipientType.CC, ccObject);
    }

    public String[] jsGet_bcc() throws ScriptException {
        Address[] addresses;
        try {
            addresses = message.getRecipients(Message.RecipientType.BCC);
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }
        String[] bcc = new String[addresses.length];
        for (int i = 0; i < bcc.length; i++) {
            bcc[i] = addresses[i].toString();
        }
        return bcc;
    }

    /**
     * <p>The bcc address that the mail is sent to</p>
     * <pre>
     * sender.bcc = "keith@wso2.com";
     *
     * OR
     *
     * var bcc = new Array();
     * bcc[0] = "jonathan@wso2.com";
     * bcc[1] =  "keith@wso2.com";
     * sender.bcc = bcc;
     * </pre>
     */
    public void jsSet_bcc(Object bccObject) throws ScriptException {
        addRecipients(Message.RecipientType.BCC, bccObject);

    }

    /**
     * <p>The subject of the mail been sent</p>
     * <pre>
     * sender.subject = "WSO2 Mashup server 1.0 Released";
     * </pre>
     */
    public void jsSet_subject(String subject) throws ScriptException {
        try {
            message.setSubject(subject);
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }
    }

    public String jsGet_subject() throws ScriptException {
        try {
            return message.getSubject();
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }
    }

    /**
     * <p>The body text of the mail been sent</p>
     * <pre>
     * sender.text = "WSO2 Mashup server 1.0 was Released on 28th January 2008";
     * </pre>
     */
    public void jsSet_text(String text) throws ScriptException {
        this.text = text;
        BodyPart messageBodyPart = new MimeBodyPart();
        try {
            messageBodyPart.setText(text);
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }
    }

    public String jsGet_text() {
        return text;
    }

    /**
     * <p>The body of the sender to be sent. This function can be used to send HTML mail.</p>
     * <pre>
     * sender.html = "<h1>WSO2 Mashup server 1.0 was Released on 28th January 2008</h1>";  // Setthing the HTML content as a String
     *                                                   OR
     * sender.html = <h1>WSO2 Mashup server 1.0 was Released on 28th January 2008</h1>;     // Setting the HTML content as an XML object
     * </pre>
     */
    public void jsSet_html(Object html) throws ScriptException {
        if (html instanceof String) {
            this.html = (String) html;
        } else if (html instanceof XML) {
            OMNode node = ((XML) html).getAxiomFromXML();
            if (node instanceof OMElement) {
                OMElement htmlElement = (OMElement) node;
                this.html = htmlElement.toString();
            } else {
                throw new ScriptException("Invalid input argument. The html function accepts " +
                        "either a String or an XML element.");
            }
        } else {
            throw new ScriptException("Invalid input argument. The html function accepts " +
                    "either a String or an XML element.");
        }
        BodyPart messageBodyPart = new MimeBodyPart();
        DataHandler dataHandler = null;
        try {
            dataHandler = new DataHandler(
                    new ByteArrayDataSource(this.html, "text/html"));
            messageBodyPart.setDataHandler(dataHandler);
            multipart.addBodyPart(messageBodyPart);
        } catch (IOException e) {
            throw new ScriptException(e);
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }

    }

    public String jsGet_html() {
        return html;
    }

    /**
     * <p>Send the mail out</p>
     * <pre>
     * sender.send()
     * </pre>
     */
    public void jsFunction_send() throws ScriptException {
        try {
            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }
    }

    /**
     * <p>Add attachments to the mail been sent. This function  has a variable number of arguments,
     * each argument can be a File hostObject or a string representing a file.</p>
     * <pre>
     * var file = new File("temp.txt"); // A file exists at temp.txt
     * sender.addAttachement(file, "temp.txt");
     * </pre>
     */
    public static void jsFunction_addAttachment(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws ScriptException {
        SenderHostObject senderHostObject = (SenderHostObject) thisObj;
        for (Object argument : arguments) {
            final FileHostObject fileHostObject;
            Object object = argument;
            if (object instanceof FileHostObject) {
                fileHostObject = (FileHostObject) object;
            } else if (object instanceof String) {
                fileHostObject = (FileHostObject) cx.newObject(senderHostObject, "File", new Object[]{object});
            } else {
                throw new ScriptException("Invalid parameter. The attachment should be a " +
                        "FileHostObject or a string representing the path of a file");
            }
            BodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new DataSource() {
                @Override
                public InputStream getInputStream() throws IOException {
                    try {
                        return fileHostObject.getInputStream();
                    } catch (ScriptException e) {
                        throw new IOException(e);
                    }
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    try {
                        return fileHostObject.getOutputStream();
                    } catch (ScriptException e) {
                        throw new IOException(e);
                    }
                }

                @Override
                public String getContentType() {
                    return null;
                }

                @Override
                public String getName() {
                    try {
                        return fileHostObject.getName();
                    } catch (ScriptException e) {
                        log.error(e.getMessage(), e);
                    }
                    return null;
                }
            };
            try {
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(fileHostObject.getName());
                senderHostObject.multipart.addBodyPart(messageBodyPart);
            } catch (MessagingException e) {
                throw new ScriptException(e);
            }
        }
    }

    private static class SMTPAuthenticator extends javax.mail.Authenticator {

        private String username, password;

        private SMTPAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }

    private void addRecipients(Message.RecipientType recipientType, Object recipientObject)
            throws ScriptException {
        try {
            if (recipientObject instanceof String[]) {
                String[] to = (String[]) recipientObject;
                InternetAddress[] recipientAddresses = new InternetAddress[to.length];
                for (int i = 0; i < to.length; i++) {
                    recipientAddresses[i] = new InternetAddress(to[i]);
                }
                message.addRecipients(recipientType, recipientAddresses);
            } else if (recipientObject instanceof NativeArray) {
                NativeArray nativeArray = (NativeArray) recipientObject;
                Object[] objects = nativeArray.getIds();
                for (int i = 0; i < objects.length; i++) {
                    Object object = objects[i];
                    Object o;
                    if (object instanceof String) {
                        String property = (String) object;
                        o = nativeArray.get(property, nativeArray);
                    } else {
                        Integer property = (Integer) object;
                        o = nativeArray.get(property.intValue(), nativeArray);
                    }
                    message.addRecipient(recipientType, new InternetAddress((String) o));
                }
            } else if (recipientObject instanceof String) {
                message.addRecipient(recipientType, new InternetAddress((String) recipientObject));
            } else {
                throw new ScriptException(
                        "The argument to this function should be an array of email addresses or a " +
                                "single email address");
            }
        } catch (MessagingException e) {
            throw new ScriptException(e);
        }
    }

}
