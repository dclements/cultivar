package com.readytalk.cultivar.cache;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.readytalk.cultivar.CuratorService;
import com.readytalk.cultivar.internal.Private;
import com.readytalk.cultivar.util.mapping.ByteArrayMapper;

@ThreadSafe
public class DefaultNodeContainer<T> extends AbstractIdleService implements CuratorService, NodeContainer<T> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultNodeContainer.class);

    private final NodeCache cache;
    private final ByteArrayMapper<T> mapper;

    @Inject
    DefaultNodeContainer(@Private final NodeCache cache, @Private final ByteArrayMapper<T> mapper) {
        this.cache = cache;
        this.mapper = mapper;
    }

    @Override
    @Nullable
    public T get() {

        ChildData data = cache.getCurrentData();

        if (data == null) {
            return null;
        }

        byte[] bytes = data.getData();

        if (bytes == null) {
            return null;
        }

        return mapper.map(bytes);
    }

    @Override
    public T get(final T def) {
        ChildData data = cache.getCurrentData();

        if (data == null) {
            return def;
        }

        byte[] bytes = data.getData();

        return mapper.map(bytes, def);
    }

    @Override
    public void addListener(final NodeCacheListener listener) {
        this.cache.getListenable().addListener(checkNotNull(listener));
    }

    @Override
    public void addListener(final NodeCacheListener listener, final Executor executor) {
        this.cache.getListenable().addListener(checkNotNull(listener), checkNotNull(executor));
    }

    /**
     * Rebuilds the cache, logging Exceptions.
     *
     * @throws java.lang.IllegalStateException
     *             If the cache has not been started yet.
     */
    @Override
    public synchronized void rebuild() {
        try {
            cache.rebuild();
        } catch (IllegalStateException ex) {
            throw Throwables.propagate(ex);
        } catch (Exception ex) {
            LOG.warn("Exception when rebuilding cache.", ex);
        }
    }

    @Override
    protected void startUp() throws Exception {
        cache.start(true);
    }

    @Override
    protected void shutDown() throws Exception {
        cache.close();
    }
}
