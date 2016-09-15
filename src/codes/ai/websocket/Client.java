package codes.ai.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import io.netty.channel.nio.NioEventLoopGroup;

import javax.xml.soap.Text;

/** @author xuy. Copyright (c) Ai.codes */
// See if this works, and see if we need to switch to a different client provided by IntelliJ.
public class Client {
  static final String URL = "ws://localhost:26337/";
  static Client instance = null;

  private WebSocketHandler handler;
  private Channel channel;
  private NioEventLoopGroup group;
  private Bootstrap b;
  private URI uri;

  private Client() {
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
      System.out.println("hello");
    } catch (Exception e) {
      e.printStackTrace();
      group.shutdownGracefully();
    }
  }

  private void connectChannel() {
    try {
      this.channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    this.channel.writeAndFlush(
        new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] {8, 1, 8, 1})));
  }

  public void sendMessage(String message) {
    TextWebSocketFrame frame = new TextWebSocketFrame(message);
    this.channel.writeAndFlush(frame);
  }

  public void shutdown() {
    this.group.shutdownGracefully();
  }

  public static Client getInstance() {
    if (instance == null) {
      instance = new Client();
    }
    return instance;
  }

  public static void main(String[] args) throws Exception {
    Client wsClient = Client.getInstance();
    int i = 0;
    while (i < 10) {
      wsClient.sendMessage("Message #1 " + Integer.toString(i));
      wsClient.sendMessage("Message #2 " + Integer.toString(i));
      i++;
    }
    wsClient.shutdown();
  }
}
