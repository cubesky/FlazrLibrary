package org.flazr.library;

import com.flazr.rtmp.RtmpConfig;
import com.flazr.rtmp.server.ServerPipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FlazrServer {
    private static ChannelGroup CHANNELS = null;
    private static Timer TIMER = null;
    private static ChannelFactory factory = null;

    public static void start() {
        if (CHANNELS == null && TIMER == null && factory == null) {
            RtmpConfig.configureServer();
            CHANNELS = new DefaultChannelGroup("server-channels");
            TIMER = new HashedWheelTimer(RtmpConfig.TIMER_TICK_SIZE, TimeUnit.MILLISECONDS);
            factory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool());
            final ServerBootstrap bootstrap = new ServerBootstrap(factory);

            bootstrap.setPipelineFactory(new ServerPipelineFactory());
            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.keepAlive", true);
            final InetSocketAddress socketAddress = new InetSocketAddress(RtmpConfig.SERVER_PORT);
            bootstrap.bind(socketAddress);
        }
    }

    public static void stop() {
        if (CHANNELS != null && TIMER != null && factory != null) {
            TIMER.stop();
            final ChannelGroupFuture future = CHANNELS.close();
            future.awaitUninterruptibly();
            factory.releaseExternalResources();
        }
    }
}
