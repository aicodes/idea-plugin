package codes.ai.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

import java.net.URI;

import io.netty.channel.nio.NioEventLoopGroup;

/** @author xuy. Copyright (c) Ai.codes */
public class WsClient {
  static final String URL = "ws://localhost:26337/";
  static WsClient instance = null;

  private WebSocketHandler handler;
  private Channel channel;
  private NioEventLoopGroup group;
  private Bootstrap b;
  private URI uri;

  private WsClient() {
    this.uri = URI.create(URL);
    this.group = new NioEventLoopGroup();

    this.handler =
        new WebSocketHandler(
            WebSocketClientHandshakerFactory.newHandshaker(
                uri,
                WebSocketVersion.V13,
                null,
                true /* allow extensions */,
                new DefaultHttpHeaders()));

    /// Channel handshake.
    try {
      b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                  ChannelPipeline p = ch.pipeline();
                  p.addLast(
                      new HttpClientCodec(),
                      new HttpObjectAggregator(8192),
                      WebSocketClientCompressionHandler.INSTANCE,
                      handler);
                }
              });
      this.channel = b.connect(this.uri.getHost(), this.uri.getPort()).sync().channel();
      this.channel.writeAndFlush(
          new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] {8, 1, 8, 1})));
      this.handler.handshakeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
      group.shutdownGracefully();
    }
  }
  
  public void sendMessage(String message) {
    TextWebSocketFrame frame = new TextWebSocketFrame(message);
    this.channel.writeAndFlush(frame);
  }

  private void shutdown() {
    this.group.shutdownGracefully();
  }

  public static WsClient getInstance() {
    if (instance == null) {
      instance = new WsClient();
    }
    return instance;
  }

  public static void main(String[] args) throws Exception {
    WsClient wsWsClient = WsClient.getInstance();
    int i = 0;
    while (i < 10) {
      wsWsClient.sendMessage("Message #1 " + Integer.toString(i));
      wsWsClient.sendMessage("Message #2 " + Integer.toString(i));
      i++;
    }
    wsWsClient.shutdown();
  }
}
