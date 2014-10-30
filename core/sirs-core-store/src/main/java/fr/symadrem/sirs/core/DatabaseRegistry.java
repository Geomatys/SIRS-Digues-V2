package fr.symadrem.sirs.core;

import static fr.symadrem.sirs.core.CouchDBInit.DB_CONNECTOR;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.ReplicationStatus;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseRegistry {

	private static final String BASE_LOCAL = "http://geouser:geopw@127.0.0.1:5984/";

	private DatabaseRegistry() {
	}

	public static List<String> listSirsDatabase(URL base) {

		HttpClient client = new StdHttpClient.Builder().url(base).build();
		CouchDbInstance dbInstance = new StdCouchDbInstance(client);

		List<String> res = new ArrayList<>();

		for (String db : dbInstance.getAllDatabases()) {
			if (db.startsWith("_"))
				continue;
			CouchDbConnector connector = dbInstance.createConnector(db, false);
			try {
				SirsDBInfo sirs = connector.get(SirsDBInfo.class, "$sirs");
				SymadremCore.LOGGER.log(Level.FINE, "SIRS database version: "
						+ sirs.getVersion());
				res.add(db);
			} catch (Exception e) {
				SymadremCore.LOGGER.log(Level.WARNING, e.getMessage(), e);
			}

		}
		return res;
	}

	public static void newLocalDB(String database) throws MalformedURLException {

		final CouchDbInstance couchsb = buildLocalInstance();
		final CouchDbConnector connector = couchsb.createConnector(database,
				true);

		final ClassPathXmlApplicationContext applicationContextParent = new ClassPathXmlApplicationContext();
		applicationContextParent.refresh();
		applicationContextParent.getBeanFactory().registerSingleton(
				DB_CONNECTOR, connector);

		final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				new String[] { "classpath:/symadrem/spring/couchdb-context.xml" },
				applicationContextParent);

		applicationContext.close();

	}

	public static void dropLocalDB(String database)
			throws MalformedURLException {

		cancelReplication(buildDatabaseLocalURL(database));
		
		final CouchDbInstance couchsb = buildLocalInstance();
		couchsb.deleteDatabase(database);
	}

	private static String buildDatabaseLocalURL(String database) {
		if(database.matches("https?://"))
			return database;
		return BASE_LOCAL + database + "/";
	}

	public static void newLocalDBFromRemote(String src, String dest,
			boolean continuous) throws MalformedURLException {
		final CouchDbInstance couchsb = buildLocalInstance();

		ReplicationCommand cmd = new ReplicationCommand.Builder()
				.continuous(continuous).source(src).target(dest)
				.createTarget(true).build();
		ReplicationStatus replicate = couchsb.replicate(cmd);
		System.out.println(replicate.getId());

	}

	private static CouchDbInstance buildLocalInstance() {
		HttpClient httpClient;
		try {
			httpClient = new StdHttpClient.Builder().url(BASE_LOCAL).build();
		} catch (MalformedURLException e) {
			throw new SymadremCoreRuntimeExecption(e);
		}
		return new StdCouchDbInstance(httpClient);
	}

	public static void cancelReplication(String dst) {

		CouchDbInstance buildLocalInstance = buildLocalInstance();
		DatabaseRegistry
				.getReplicationTasksByTarget(dst)
				.map(t -> t.get("replication_id"))
				.filter(n -> n != null)
				.map(t -> t.asText())
				.map(id -> new ReplicationCommand.Builder().id(id).cancel(true)
						.build())
				.forEach(cmd -> buildLocalInstance.replicate(cmd));

	}

	public static Stream<JsonNode> getReplicationTasks() {
		CouchDbInstance buildLocalInstance = buildLocalInstance();
		HttpResponse httpResponse = buildLocalInstance.getConnection().get(
				"/_active_tasks");

		ObjectMapper objectMapper = new ObjectMapper();

		try {
			List<JsonNode> tasks = objectMapper.readValue(httpResponse
					.getContent(), objectMapper.getTypeFactory()
					.constructCollectionType(List.class, JsonNode.class));

			return tasks.stream().filter(
					t -> t.get("type") == null ? false : t.get("type").asText()
							.equals("replication"));

		} catch (IOException e) {
			throw new SymadremCoreRuntimeExecption(e);
		}

	}

	public static Stream<JsonNode> getReplicationTasksByTarget(String dst) {
		return getReplicationTasks().filter(
				t -> matchTarget(t.get("target"), dst));
	}

	private static boolean matchTarget(JsonNode jsonNode, String dst) {
		if (jsonNode == null)
			return false;
		String dst2 = jsonNode.asText();
		int i = dst.indexOf('@');
		int j = dst2.indexOf('@');
		return dst.substring(i).equals(dst2.substring(j));
	}
}
