package codes.ai.async;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

/** @author xuy. Copyright (c) Ai.codes */

/// Not used for now, as it is for reading messages from server.
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {
  private final WebSocketClientHandshaker handshaker;
  private ChannelPromise handshakeFuture;

  public WebSocketHandler(final WebSocketClientHandshaker handshaker) {
    this.handshaker = handshaker;
  }

  public ChannelFuture handshakeFuture() {
    return handshakeFuture;
  }

  @Override
  public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
    handshakeFuture = ctx.newPromise();
  }

  @Override
  public void channelActive(final ChannelHandlerContext ctx) throws Exception {
    handshaker.handshake(ctx.channel());
  }

  @Override
  public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
    System.out.println("WebSocket WebSocketClient disconnected!");
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    final Channel ch = ctx.channel();
    if (!handshaker.isHandshakeComplete()) {
      System.out.println("Instance connected");
      handshaker.finishHandshake(ch, (FullHttpResponse) msg);
      handshakeFuture.setSuccess();
      return;
    }

    if (msg instanceof FullHttpResponse) {
      final FullHttpResponse response = (FullHttpResponse) msg;
      throw new Exception(
          "Unexpected FullHttpResponse (getStatus="
              + response.status()
              + ", content="
              + response.content().toString(CharsetUtil.UTF_8)
              + ')');
    }

    final WebSocketFrame frame = (WebSocketFrame) msg;
    if (frame instanceof TextWebSocketFrame) {
      final TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
      // uncomment to print request
      // logger.info(textFrame.text());
    } else if (frame instanceof PongWebSocketFrame) {
    } else if (frame instanceof CloseWebSocketFrame) ch.close();
    else if (frame instanceof BinaryWebSocketFrame) {
      // uncomment to print request
      // logger.info(frame.content().toString());
    }
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause)
      throws Exception {
    cause.printStackTrace();

    if (!handshakeFuture.isDone()) {
      handshakeFuture.setFailure(cause);
    }

    ctx.close();
  }
}
