
package fr.sirs.index;

import fr.sirs.core.SirsCore;
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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 *
 * @author Johann Sorel (Geomatys).
 */
public class SearchEngine {

    private static final Logger LOGGER = SirsCore.LOGGER;
    private static final Version VERSION = Version.LUCENE_4_10_2;
    
    private static final String FIELD_ID = "docid";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_KEYWORDS = "keywords";

    private final String dbName;
    private IndexWriter indexWriter;
    private IndexSearcher indexSearcher;


    public SearchEngine(String dbName) throws IOException {
        this.dbName = dbName;
        createIndex();
        initIndexSearcher();
    }

    private void createIndex() throws IOException {
        final File configFolder = SirsCore.getConfigFolder();
        final File luceneFolder = new File(new File(configFolder, "lucene"),dbName);
        luceneFolder.mkdirs();
        
        final Directory directory = FSDirectory.open(luceneFolder, NoLockFactory.getNoLockFactory());
        final ClassicAnalyzer analyzer = new ClassicAnalyzer(VERSION);
        final IndexWriterConfig config = new IndexWriterConfig(VERSION, analyzer);
        indexWriter = new IndexWriter(directory, config);
        indexWriter.commit();
    }

    private void initIndexSearcher() throws IOException {
        DirectoryReader directoryReader = DirectoryReader.open(indexWriter.getDirectory());
        indexSearcher = new IndexSearcher(directoryReader);
    }
    
    public void addDocument(String docId, String type, String keywords) throws IOException{
        final Document doc = new Document();
        doc.add(new TextField(FIELD_ID, docId, Field.Store.YES));
        doc.add(new TextField(FIELD_TYPE, type, Field.Store.NO));
        doc.add(new TextField(FIELD_KEYWORDS, keywords, Field.Store.NO));
        indexWriter.addDocument(doc);
        indexWriter.commit();
    }
    
    public void updateDocument(String docId, String keywords) throws IOException{
        final Term filter = new Term(FIELD_ID, docId);
        indexWriter.updateDocValues(filter, 
                new TextField(FIELD_KEYWORDS, keywords, Field.Store.NO));
        indexWriter.commit();
    }
    
    public void deleteDocument(String docId) throws IOException{
        indexWriter.deleteDocuments(new Term(FIELD_ID, docId));
        indexWriter.commit();
    }
    
    
    public Set<String> search(final String type, final String ... words) throws ParseException, IOException {
        final Set<String> result = new HashSet<>();
        initIndexSearcher();
        
        final TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
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
        
        indexSearcher.search(q, collector);
        final ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (final ScoreDoc scoreDoc : hits){
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