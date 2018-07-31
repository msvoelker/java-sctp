import com.sun.nio.sctp.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.text.*;
import java.util.*;

public class DaytimeServer {
    static int SERVER_PORT = 3456;
    static int US_STREAM = 0;
    static int FR_STREAM = 1;

    static SimpleDateFormat USformatter = new SimpleDateFormat(
                                "h:mm:ss a EEE d MMM yy, zzzz", Locale.US);
    static SimpleDateFormat FRformatter = new SimpleDateFormat(
                                "h:mm:ss a EEE d MMM yy, zzzz", Locale.FRENCH);

    public static void main(String[] args) throws IOException {
        SctpServerChannel ssc = SctpServerChannel.open();
        InetSocketAddress serverAddr = new InetSocketAddress(SERVER_PORT);
        ssc.bind(serverAddr);

        ByteBuffer buf = ByteBuffer.allocateDirect(60);
        CharBuffer cbuf = CharBuffer.allocate(60);
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer recvbuf = ByteBuffer.allocateDirect(255);

        while (true) {
            SctpChannel sc = ssc.accept();

            /* get the current date */
            Date today = new Date();
            cbuf.put(USformatter.format(today)).flip();
            encoder.encode(cbuf, buf, true);
            buf.flip();

            /* send the message on the US stream */
            MessageInfo outMessageInfo = MessageInfo.createOutgoing(null,
                                                                 US_STREAM);
            sc.send(buf, outMessageInfo);

            /* update the buffer with French format */
            cbuf.clear();
            cbuf.put(FRformatter.format(today)).flip();
            buf.clear();
            encoder.encode(cbuf, buf, true);
            buf.flip();

            /* send the message on the French stream */
            outMessageInfo.streamNumber(FR_STREAM);
            sc.send(buf, outMessageInfo);

            cbuf.clear();
            buf.clear();

            // shutdown and receive all pending messages/notifications
            sc.shutdown();
            AssociationHandler assocHandler = new AssociationHandler();
            MessageInfo inMessageInfo = null;
            while (true) {
              inMessageInfo = sc.receive(recvbuf, System.out, assocHandler);
              if (inMessageInfo == null || inMessageInfo.bytes() == -1) {
                break;
              }
            }
            sc.close();
        }
    }

    static class AssociationHandler
        extends AbstractNotificationHandler<PrintStream>
    {
        public HandlerResult handleNotification(AssociationChangeNotification not,
                                                PrintStream stream) {
            stream.println("AssociationChangeNotification received: " + not);
            return HandlerResult.CONTINUE;
        }

        public HandlerResult handleNotification(ShutdownNotification not,
                                                PrintStream stream) {
            stream.println("ShutdownNotification received: " + not);
            return HandlerResult.RETURN;
        }
    }
}
