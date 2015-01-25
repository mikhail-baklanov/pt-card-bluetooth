package ru.relex.pt.passageway.scanners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.bluez.Adapter;
import org.bluez.Adapter.DeviceFound;
import org.bluez.Manager;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothScanner {

    private static final Logger LOG = LoggerFactory.getLogger(BluetoothScanner.class);

    private static class DeviceInfo {
	public String alias = "";
	public int sumRssi = 0;
	public int lastValue = 0;
	public long maxValue = Long.MIN_VALUE;
	public long maxValueTime = 0;
	public int sumDeriv = 0;
	public int n = 0;

	public DeviceInfo(String alias) {
	    super();
	    this.alias = alias;
	}
    }

    private Map<String, DeviceFoundHandler> handlers = new ConcurrentHashMap<String, BluetoothScanner.DeviceFoundHandler>();

    private class DeviceFoundHandler implements DBusSigHandler<DeviceFound> {
	private Map<String, DeviceInfo> info = new ConcurrentHashMap<String, BluetoothScanner.DeviceInfo>();

	public void handle(DeviceFound s) {
	    String address = s.b.get("Address").getValue().toString();
	    String alias = s.b.get("Alias").getValue().toString();
	    int rssi = Integer.valueOf(s.b.get("RSSI").getValue().toString());

	    DeviceInfo di = info.get(address);
	    if (di == null) {
		di = new DeviceInfo(alias);
		info.put(address, di);
	    }
	    di.n++;
	    di.sumRssi += rssi;
	    if (di.n > 1)
		di.sumDeriv += rssi - di.lastValue;
	    di.lastValue = rssi;
	    if (rssi > di.maxValue) {
		di.maxValue = rssi;
		di.maxValueTime = new Date().getTime();
	    }

	    LOG.info(s.getPath() + " " + address + " " + di.alias + " rssi=" + rssi + " avg rssi=" + di.sumRssi / di.n
		    + " avg sd=" + ((di.n > 1) ? di.sumDeriv / (float) (di.n - 1) : 0) + " max=" + di.maxValue + " at "
		    + new Date(di.maxValueTime).toString());
	    sendCardId(address);
	   
	}

	public Map<String, DeviceInfo> getInfo() {
	    return info;
	}
    }
    
    private Map<String, Long> lastEntrance = new HashMap<String, Long>();

    public void sendCardId(String address) {
	
	Long curTime = new Date().getTime();
	Long lastTime = lastEntrance.get(address);
	if (lastTime != null && (curTime-lastTime < (30*1000)))
	    return;
	lastEntrance.put(address, curTime);
	try {

	    URL url = new URL("http://localhost:8080/pt-api/rest/passway/entrance");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoOutput(true);
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type", "application/json");

	    String mac = address.replace(":", "");
	    String input = "{\"cardId\":\"mac:" + mac + "\"}";

	    OutputStream os = conn.getOutputStream();
	    os.write(input.getBytes());
	    os.flush();

	    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
		return; //throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
	    }

	    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

	    String output;
	    System.out.println("Output from Server .... \n");
	    while ((output = br.readLine()) != null) {
		System.out.println(output);
	    }

	    conn.disconnect();

	} catch (MalformedURLException e) {

	    e.printStackTrace();

	} catch (IOException e) {

	    e.printStackTrace();

	}
	
    }

    public static void main(String[] args) throws DBusException, ParseException {

	new BluetoothScanner().run();
    }

    static private void usage() {
	LOG.info("bscanner [<command>]");
	LOG.info("\tCommands: clean, update, populate, all, full, touch, compare");
	LOG.info("\t\tclean    - clean all database objects, run init scripts");
	LOG.info("\tOptions:");
	LOG.info("\t\t-s <url> server url, 'http://localhost:8080/' by default");
	LOG.info("\t\t-r register user cardId by alias");
	LOG.info("\tExamples:");
	LOG.info("\t\tbscanner -s http://example.com/");
    }

    private OptionSet getOptionSet(String[] args) {
	OptionParser parser = new OptionParser();
	parser.accepts("h");
	parser.accepts("f");
	parser.accepts("d");
	parser.accepts("F");
	parser.accepts("p").withRequiredArg();
	parser.accepts("v").withRequiredArg();
	parser.accepts("c").withRequiredArg();
	parser.accepts("r").withRequiredArg();
	parser.accepts("m").withRequiredArg();
	parser.accepts("l").withRequiredArg();
	parser.accepts("o").withRequiredArg();
	parser.accepts("D").withRequiredArg();
	parser.accepts("b").withRequiredArg();
	parser.accepts("s").withRequiredArg();
	return parser.parse(args);
    }

    /**
     * Парсинг аргументов командной строки.
     * 
     * @param args
     *            - аргументы командной строки.
     */
    public void parseCmdLine(String[] args) {
	Properties props = new Properties();
	OptionSet opset = getOptionSet(args);

	if (opset.has("h")) {
	    usage();
	    return;
	}

	List<String> commands = opset.nonOptionArguments();
	if (commands.size() != 1) {
	    usage();
	    return;
	}

	if (opset.has("c")) {
	    String configs = (String) opset.valueOf("c");

	}

    }

    public void run() throws DBusException {
	System.out.println("Creating Connection");
	final DBusConnection conn = DBusConnection.getConnection(DBusConnection.SYSTEM);

	try {
	    final List<Adapter> adapters = getAdapters(conn);
	    subscribeAdapters(conn, adapters);

	    Thread scanThread = new Thread("Bluetooth discovery thread") {
		@Override
		public void run() {
		    startDiscovery(conn, adapters);

		    try {
			while (!isInterrupted()) {
			    Thread.sleep(1000);
			}
		    } catch (InterruptedException e) {
		    }

		    stopDiscovery(conn, adapters);
		}
	    };
	    scanThread.start();
	    System.out.println("Input action:");
	    System.out.println("0 - exit");
	    boolean exit = false;
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    while (!exit) {
		String line = reader.readLine();
		if (line.equals("0"))
		    exit = true;
	    }
	    scanThread.interrupt();
	    scanThread.join();
	    unsubscribeAdapters(conn, adapters);
	    printInfo(adapters);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	conn.disconnect();
    }

    private void printInfo(List<Adapter> adapters) {

	for (Adapter adp : adapters) {
	    Map<String, DeviceInfo> info = handlers.get(adp.GetProperties().get("Address").getValue().toString())
		    .getInfo();
	    for (String address : info.keySet()) {
		DeviceInfo di = info.get(address);
		LOG.info(adp.toString() + " " + address + " " + di.alias + " " + di.n + " avg rssi=" + di.sumRssi
			/ di.n + " avg sd=" + ((di.n > 1) ? di.sumDeriv / (float) (di.n - 1) : 0) + " max="
			+ di.maxValue + " at " + new Date(di.maxValueTime).toString());
		 
	    }
	}
    }

    private void subscribeAdapters(DBusConnection conn, List<Adapter> adapters) {
	for (Adapter adp : adapters) {
	    try {
		DeviceFoundHandler handler = new DeviceFoundHandler();
		handlers.put(adp.GetProperties().get("Address").getValue().toString(), handler);
		conn.addSigHandler(Adapter.DeviceFound.class, adp, handler);
	    } catch (DBusException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    private void unsubscribeAdapters(DBusConnection conn, List<Adapter> adapters) {
	for (Adapter adp : adapters) {
	    try {
		conn.removeSigHandler(Adapter.DeviceFound.class, adp,
			handlers.get(adp.GetProperties().get("Address").getValue().toString()));
	    } catch (DBusException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    private void startDiscovery(DBusConnection conn, List<Adapter> adapters) {
	for (Adapter adp : adapters) {
	    adp.StartDiscovery();
	}
    }

    private void stopDiscovery(DBusConnection conn, List<Adapter> adapters) {
	for (Adapter adp : adapters) {
	    adp.StopDiscovery();
	}
    }

    private List<Adapter> getAdapters(DBusConnection conn) throws Exception {
	Manager mgr = (Manager) conn.getRemoteObject("org.bluez", "/", Manager.class);
	List<DBusInterface> adaptersLst = mgr.ListAdapters();
	List<Adapter> res = new LinkedList<Adapter>();
	System.out.println(adaptersLst);
	for (DBusInterface dbi : adaptersLst) {
	    System.out.println(dbi.toString());
	    String[] objectPath = dbi.toString().split(":");
	    Adapter adp = conn.getRemoteObject("org.bluez", objectPath[2], Adapter.class);
	    if (adp != null) {
		System.out.println(adp.toString());
		Map<String, Variant> props = adp.GetProperties();
		System.out.println(props);
	    }
	    res.add(adp);
	}
	return res;
    }

}
