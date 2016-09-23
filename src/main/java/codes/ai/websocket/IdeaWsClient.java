package codes.ai.websocket;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.ClosedChannelException;

public class IdeaWsClient {
	public static IdeaWsClient getInstance() {
		if (INSTANCE == null) {
//			INSTANCE = new IdeaWsClient();
		}
		return INSTANCE;
	}

	private final WebSocketServerHandshaker handshaker;
	private final Channel channel;
	private static IdeaWsClient INSTANCE;

	public IdeaWsClient(@NotNull Channel channel, @NotNull WebSocketServerHandshaker handshaker) {
		this.channel = channel;
		this.handshaker = handshaker;
	}

	@NotNull
	public ChannelFuture send(@NotNull ByteBuf message) {
		if (channel.isOpen()) {
			return channel.writeAndFlush(new TextWebSocketFrame(message));
		}
		else {
			return channel.newFailedFuture(new ClosedChannelException());
		}
	}

	public void sendHeartbeat() {
		channel.writeAndFlush(new PingWebSocketFrame());
	}

	public void disconnect(@NotNull CloseWebSocketFrame frame) {
		handshaker.close(channel, frame);
	}
}

