
package fr.sirs.util;

import fr.sirs.core.SirsCore;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.WeakHashMap;
import java.util.logging.Level;
import javafx.beans.property.ObjectProperty;

/**
 * A daemon which will run in background, closing all resources submitted.
 * Note : Give to the daemon an object to listen on, and aproperty on an {@link AutoCloseable}
 * object to close when the first object dies. That's all !
 *
 * @author Alexis Manin (Geomatys)
 */
public class ClosingDaemon {

    private static final ClosingDaemon INSTANCE = new ClosingDaemon();

    private final ReferenceQueue phantomQueue = new ReferenceQueue();

    private static final WeakHashMap<Object, PhantomReference> referenceCache = new WeakHashMap<>();

    private ClosingDaemon() {
        final Thread closer = new Thread(() -> {
            while (true) {
                try {
                    Reference removed = phantomQueue.remove();
                    if (removed instanceof AutoCloseable) {
                        ((AutoCloseable)removed).close();
                    }

                } catch (InterruptedException e) {
                    SirsCore.LOGGER.log(Level.WARNING, "Resource closer has been interrupted ! It could cause memory leaks.");
                    return;
                } catch (Throwable t) {
                    SirsCore.LOGGER.log(Level.WARNING, "Some resource cannot be released. It's likely to cause memory leaks !");
                }
            }
        });
        closer.setName("SIRS resource closer");
        closer.setDaemon(true);
        closer.start();
    }

    public static void watchResource(final Object toWatch, final ObjectProperty<? extends AutoCloseable> toClose) {
        referenceCache.put(toWatch, new ResourceReference(toWatch, INSTANCE.phantomQueue, toClose));
    }

    private static class ResourceReference extends PhantomReference implements AutoCloseable {

        private final ObjectProperty<? extends AutoCloseable> streamToClose;
        private ResourceReference(Object referent, ReferenceQueue q, ObjectProperty<? extends AutoCloseable> objectToClose) {
            super(referent, q);
            this.streamToClose = objectToClose;
        }

        @Override
        public void close() {
            try {
                if (streamToClose.get() != null) {
                    streamToClose.get().close();
                }
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.WARNING, "A streamed CouchDB view result cannot be closed. It's likely to cause memory leaks.", e);
            }
        }
    }
}
