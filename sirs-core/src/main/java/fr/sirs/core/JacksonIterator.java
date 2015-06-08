package fr.sirs.core;

import java.io.IOException;
import java.util.Iterator;

import org.ektorp.StreamingViewResult;
import org.ektorp.ViewResult.Row;

import com.fasterxml.jackson.databind.ObjectMapper;


public class JacksonIterator<T> implements Iterator<T>, AutoCloseable {

    private final StreamingViewResult result;
    private Iterator<Row> iterator;
    private final Class<? extends T> clazz;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JacksonIterator(Class<? extends T> clazz, StreamingViewResult result) {
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
        if (iterator == null)
            return null;
        Row next = iterator.next();
        try {
            return objectMapper.reader(clazz).readValue(next.getValueAsNode());
        } catch (IOException e) {
            throw new SirsCoreRuntimeExecption(e);
        }

    }



    @Override
    public void close() throws Exception {
        result.close();

    }

    public static <T> JacksonIterator<T> create(
            Class<T> class1,
            StreamingViewResult queryForStreamingView) {
        
        return new JacksonIterator<T>(class1, queryForStreamingView);
    }

}
