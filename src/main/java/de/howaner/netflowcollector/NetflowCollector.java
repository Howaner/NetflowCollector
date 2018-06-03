package de.howaner.netflowcollector;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.howaner.netflowcollector.config.NetflowCollectorConfig;
import de.howaner.netflowcollector.database.DatabaseCache;
import de.howaner.netflowcollector.database.DatabaseConnection;
import de.howaner.netflowcollector.database.MongoConnection;
import de.howaner.netflowcollector.handler.FlowSetDeserializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetflowCollector {
	private static NetflowCollector instance;
	@Getter private final Logger logger;
	@Getter private final NetflowCollectorConfig config;
	@Getter private DatabaseCache databaseCache;
	@Getter private DatabaseConnection databaseConnection;

	private EventLoopGroup eventLoop;

	public NetflowCollector() {
		this.logger = LogManager.getLogger();
		this.config = new NetflowCollectorConfig();
	}

	public static void main(String[] args) {
		instance = new NetflowCollector();
		instance.start();
	}

	public static NetflowCollector getInstance() {
		return instance;
	}

	public void start() {
		try {
			if (!this.config.isExising())
				this.config.writeDefaultConfig();
			this.config.loadConfig();
		} catch (Exception ex) {
			this.logger.error("Exception while reading or creating config.json", ex);
			return;
		}

		try {
			switch (this.config.getDatabaseType()) {
				case "MongoDB":
					this.databaseConnection = new MongoConnection();
					if (this.config.getDatabaseUser().isEmpty())
						((MongoConnection) this.databaseConnection).connect(this.config.getDatabaseIp(), this.config.getDatabasePort(), this.config.getDatabaseDB());
					else
						((MongoConnection) this.databaseConnection).connect(this.config.getDatabaseIp(), this.config.getDatabasePort(), this.config.getDatabaseUser(), this.config.getDatabasePassword(), this.config.getDatabaseDB());
					break;
				default:
					// Can't happen because the database type was already checked in config parsing
					throw new UnsupportedOperationException();
			}
		} catch (Exception ex) {
			this.logger.error("Can't connect to databae", ex);
			return;
		}

		this.databaseCache = new DatabaseCache();
		this.databaseCache.startTimeoutChecker();

		boolean epoll = Epoll.isAvailable();
		this.eventLoop = epoll
				? new EpollEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("Netty #%d").setDaemon(true).build())
				: new NioEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("Netty #%d").setDaemon(true).build());

		this.logger.info("Start udp server at {}:{} ...", this.config.getBindingHost(), this.config.getBindingPort());
		try {
			Bootstrap bootstrap = new Bootstrap()
					.group(this.eventLoop)
					.channel(epoll ? EpollDatagramChannel.class : NioDatagramChannel.class)
					//.option(ChannelOption.SO_BROADCAST, true)
					.handler(new ChannelInitializer<DatagramChannel>() {
						@Override
						protected void initChannel(DatagramChannel channel) throws Exception {
							ChannelPipeline pipe = channel.pipeline();
							pipe.addLast(new FlowSetDeserializer());
							pipe.addLast(new PacketHandler());
						}
					});
			
			bootstrap.bind(InetAddress.getByName(this.config.getBindingHost()), this.config.getBindingPort()).sync().channel().closeFuture().await();
		} catch (UnknownHostException | InterruptedException ex) {
			ex.printStackTrace();
		}
	}

}
