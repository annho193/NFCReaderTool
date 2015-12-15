import javax.smartcardio.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class Main {
    public static final byte[] READ_ID = {(byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x00};

    public static void main(String[] args) {
        try {

            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();

            if (terminals.size() <= 0) {
                System.out.println("Please attach a terminal... :(");
                return;
            }

            readCard(terminals.get(0));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readCard(CardTerminal terminal) throws Exception {
        System.out.println("Waiting for card...");
        boolean cardPresent = terminal.waitForCardPresent(1000);

        if (!cardPresent) {
            System.out.println("No card present.");
            readCard(terminal);
            return;
        }

        Card card = terminal.connect("T=0");
        System.out.println("card: " + card);

        CardChannel c = card.getBasicChannel();

        CommandAPDU com = new CommandAPDU(READ_ID);
        ResponseAPDU r = c.transmit(com);

        String cardId = bytesToHex(r.getData());
        System.out.println(cardId);
        sendReadRequest(cardId);

        Thread.sleep(5000);
        readCard(terminal);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static void sendReadRequest(String cardId) throws Exception{
        URL url = new URL("http://schoolplatform.majorstudios.nl/php/services/addReadRequest.php");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

        writer.write("card_id=" + cardId + "&reader_id=1");
        writer.flush();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        writer.close();
        reader.close();
    }
}