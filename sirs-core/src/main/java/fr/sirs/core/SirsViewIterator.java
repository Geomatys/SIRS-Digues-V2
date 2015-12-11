package fr.sirs.core;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.ektorp.StreamingViewResult;
import org.ektorp.ViewResult.Row;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.util.ClosingDaemon;
import javafx.beans.property.SimpleObjectProperty;


public class SirsViewIterator<T> implements Iterator<T>, AutoCloseable {

    private final StreamingViewResult result;
    private final Class<? extends T> clazz;
    private Iterator<Row> iterator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SirsViewIterator(Class<? extends T> clazz, StreamingViewResult result) {
        this.clazz = clazz;
        this.result = result;
        if (result.getTotalRows() > 0)
            this.iterator = result.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator != null && iterator.hasNext();
    }

    @Override
    public T next() {
        if (iterator == null) return null;

        final Row next = iterator.next();
        try {
            return objectMapper.reader(clazz).readValue(next.getValueAsNode());
        } catch (JsonMappingException ex) {
            // Fix for some javascript views, that put the value into the document node
            // instead of the value node.
            try {
                return objectMapper.reader(clazz).readValue(next.getDocAsNode());
            } catch (IOException e) {
                throw new SirsCoreRuntimeException(e);
            }
        } catch (IOException e) {
            throw new SirsCoreRuntimeException(e);
        }

    }

    @Override
    public void close() throws Exception {
        result.close();
    }

    public static <T> SirsViewIterator<T> create(Class<T> resultType, StreamingViewResult queryForStreamingView) {
        SirsViewIterator<T> iterator = new SirsViewIterator<>(resultType, queryForStreamingView);
        ClosingDaemon.watchResource(iterator, new SimpleObjectProperty<>(iterator.result));
        return iterator;
    }
}
