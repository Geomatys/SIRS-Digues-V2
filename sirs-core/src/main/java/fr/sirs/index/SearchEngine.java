
package fr.sirs.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.model.Element;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.TopDocs;
import org.ektorp.CouchDbConnector;
import org.ektorp.StreamingViewResult;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;

/**
 *
 * @author Johann Sorel (Geomatys).
 */
public class SearchEngine implements DocumentListener {

    private static final Logger LOGGER = SirsCore.LOGGER;
    private static final Version VERSION = Version.LUCENE_4_10_2;
    
    private static final String FIELD_ID = "docid";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_KEYWORDS = "keywords";

    private final CouchDbConnector connector;
    private final DocumentChangeEmiter changeEmitter;
    private final String dbName;
    //thread safe
    private final IndexWriter indexWriter;
    private final IndexSearcher indexSearcher;


    public SearchEngine(String dbName, CouchDbConnector connector, DocumentChangeEmiter changeEmitter) throws IOException {
        this.dbName = dbName;
        this.connector = connector;
        this.changeEmitter = changeEmitter;
        changeEmitter.addListener(this);
        
        
        final File configFolder = SirsCore.getConfigFolder();
        final File luceneFolder = new File(new File(configFolder, "lucene"),dbName);
        final boolean isNew = !luceneFolder.exists();
        luceneFolder.mkdirs();
        
        final Directory directory = FSDirectory.open(luceneFolder, NoLockFactory.getNoLockFactory());
        final ClassicAnalyzer analyzer = new ClassicAnalyzer(VERSION);
        final IndexWriterConfig config = new IndexWriterConfig(VERSION, analyzer);
        indexWriter = new IndexWriter(directory, config);
        indexWriter.commit();
        
        final DirectoryReader directoryReader = DirectoryReader.open(indexWriter,true);
        indexSearcher = new IndexSearcher(directoryReader);
        
        //Creation de l'index s'il n'existe pas.
        if(isNew){
            rebuildIndex();
        }
    }
    
    private void rebuildIndex() throws IOException{
        //on nettoie la base
        indexWriter.deleteAll();
        
        final ViewQuery query = new ViewQuery().dbPath(connector.path()).viewName("_all_docs").includeDocs(true);
        final StreamingViewResult view = connector.queryForStreamingView(query);
        final Iterator<ViewResult.Row> ite = view.iterator();
        
        while(ite.hasNext()){
            final ViewResult.Row row = ite.next();
            final JsonNode node = row.getDocAsNode();
            final JsonNode idNode = node.get("_id");
            final JsonNode classNode = node.get("@class");
            if(idNode==null || classNode==null) continue;
            final String docId = idNode.textValue();
            final String[] type = classNode.textValue().split("\\.");
            final String keywords = createKeyWords(node);
            addDocument(docId, type[type.length-1], keywords, false);
        }
        
        indexWriter.commit();
        view.close();
    }
    
    @Override
    public void documentCreated(Element changed) {
        final String docId = changed.getDocumentId();
        final String type = changed.getClass().getSimpleName();
        final String keywords = createKeyWords(changed);
        try {
            addDocument(docId, type, keywords, true);
        } catch (IOException ex) {
            SirsCore.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    @Override
    public void documentChanged(Element changed) {
        final String docId = changed.getDocumentId();
        final String keywords = createKeyWords(changed);
        try {
            updateDocument(docId, keywords);
        } catch (IOException ex) {
            SirsCore.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    @Override
    public void documentDeleted(Element deleteObject) {
        try {
            deleteDocument(deleteObject.getDocumentId());
        } catch (IOException ex) {
            SirsCore.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }
    
    private String createKeyWords(Object obj){
        final JsonNode node;
        if(obj instanceof Element){
            node = new ObjectMapper().valueToTree(obj);
        }else if(obj instanceof JsonNode){
            node = (JsonNode) obj;
        }else{
            SirsCore.LOGGER.log(Level.WARNING, "Unsupported type for indexing {0}", obj);
            return "";
        }
        
        final Set<String> keywords = new HashSet<>();
        loopKeyWords(keywords, node);
        
        final StringBuilder sb = new StringBuilder();
        for(String str : keywords){
            sb.append(' ').append(str);
        }
        
        return sb.toString();
    }
    
    private void loopKeyWords(Set<String> keywords, JsonNode node){
        final JsonNodeType type = node.getNodeType();
        
        if(JsonNodeType.STRING.equals(type)){            
            final String txt = node.textValue();
            keywords.addAll(Arrays.asList(txt.split(" ")));
        }else{
            final Iterator<Map.Entry<String, JsonNode>> ite = node.fields();
            
            while(ite.hasNext()){
                final Map.Entry<String, JsonNode> entry = ite.next();
                final String name = entry.getKey().toLowerCase();
                if(!(name.startsWith("_") || name.endsWith("id") || name.endsWith("ids") || name.equals("@class"))){
                    loopKeyWords(keywords, entry.getValue());
                }
                
            }
        }
    }
    
    
    public void addDocument(String docId, String type, String keywords, boolean commit) throws IOException{
        final Document doc = new Document();
        doc.add(new TextField(FIELD_ID, docId, Field.Store.YES));
        doc.add(new TextField(FIELD_TYPE, type, Field.Store.YES));
        doc.add(new TextField(FIELD_KEYWORDS, keywords, Field.Store.YES));
        indexWriter.addDocument(doc);
        if(commit) indexWriter.commit();
    }
    
    public void updateDocument(String docId, String keywords) throws IOException{
        final Term filter = new Term(FIELD_ID, docId);
        indexWriter.updateDocValues(filter, 
                new TextField(FIELD_KEYWORDS, keywords, Field.Store.YES));
        indexWriter.commit();
    }
    
    public void deleteDocument(String docId) throws IOException{
        indexWriter.deleteDocuments(new Term(FIELD_ID, docId));
        indexWriter.commit();
    }
        
    public Set<String> search(final String type, final String ... words) throws ParseException, IOException {
        final Set<String> result = new HashSet<>();
                
        final ClassicAnalyzer analyzer = new ClassicAnalyzer(VERSION);
        final QueryParser queryParser = new QueryParser(VERSION, FIELD_KEYWORDS, analyzer);
        queryParser.setDefaultOperator(QueryParser.Operator.OR);
        
        //build query
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(FIELD_KEYWORDS).append(':').append(words[0]);
        for(int i=1;i<words.length;i++) {
            sb.append(" OR ");
            sb.append(FIELD_KEYWORDS).append(':').append(words[i]);
        }
        sb.append(')');
        if(type!=null){
            sb.append(" AND type:").append(type);
        }
        final Query q = queryParser.parse(sb.toString());
        
        
        final int maxRecords = (int)indexSearcher.collectionStatistics("id").maxDoc();
        if (maxRecords == 0) {
            return Collections.EMPTY_SET;
        }
        final TopDocs hits = indexSearcher.search(q, maxRecords);
        for (final ScoreDoc scoreDoc : hits.scoreDocs){
            final Document doc = indexSearcher.doc(scoreDoc.doc);
            final String value = doc.get(FIELD_ID);
            if(value !=null) result.add(value);
        }
        
        return result;
    }
    
    public void finalize() throws IOException {
        LOGGER.info("closing metadata index");
        indexWriter.close();
    }

    

}