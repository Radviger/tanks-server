package gtanks.main.netty;

import gtanks.logger.Logger;
import gtanks.system.destroy.Destroyable;
import gtanks.test.osgi.OSGi;
import gtanks.test.server.configuration.entitys.NettyConfiguratorEntity;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public enum NettyService implements Destroyable {
    INSTANCE;

    public int port;
    private final ServerBootstrap bootstrap;

    NettyService() {
        this.initParams();
        ExecutorService bossExec = new OrderedMemoryAwareThreadPoolExecutor(1, 400000000L, 2000000000L, 60L, TimeUnit.SECONDS);
        ExecutorService ioExec = new OrderedMemoryAwareThreadPoolExecutor(4, 400000000L, 2000000000L, 60L, TimeUnit.SECONDS);
        ChannelFactory factory = new NioServerSocketChannelFactory(bossExec, ioExec, 4);
        this.bootstrap = new ServerBootstrap(factory);
        this.bootstrap.setPipelineFactory(new NettyPipelineFactory());
        this.bootstrap.setOption("child.tcpNoDelay", true);
        this.bootstrap.setOption("child.keepAlive", true);
    }

    public void bind() {
        this.bootstrap.bind(new InetSocketAddress(this.port));
        Logger.log("[Netty] Server run on port: " + this.port);
    }

    @Override
    public void destroy() {
        this.bootstrap.releaseExternalResources();
        this.bootstrap.shutdown();
    }

    private void initParams() {
        this.port = ((NettyConfiguratorEntity) OSGi.getModelByInterface(NettyConfiguratorEntity.class)).getPort();
    }
}
