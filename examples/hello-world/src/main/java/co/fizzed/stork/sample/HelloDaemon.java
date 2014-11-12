/*
 * Copyright 2014 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.fizzed.stork.sample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import io.netty.handler.codec.http.HttpHeaders.Values;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import java.util.ArrayList;

/**
 *
 * @author joelauer
 */
public class HelloDaemon {
    
    static int port;
    static List<String> lines;
    static Date startedAt;
    
    static public void main(String[] args) throws Exception {
        // call same as hello world
        HelloConsole.main(args);
        
        // was a port specified?
        port = 8888;
        if (args.length > 0) {
            String portString = args[0];
            port = Integer.parseInt(portString);
        }
        
        startedAt = new Date();
        lines = createLines(args);
        
        // start server
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             //.handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new HttpHelloWorldServerInitializer());

            Channel ch = b.bind(port).sync().channel();

            // what interfaces
            System.out.println("In your browser visit (all possible options):");
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    System.out.println("  http://" + inetAddress.getHostAddress() + ":" + port);
                }
            }
            
            // on shutdown...
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("Shutting down.");
                }
            });

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    static class HttpHelloWorldServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpHelloWorldServerHandler());
        }
    }
    
    static class HttpHelloWorldServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;
                if (HttpHeaders.is100ContinueExpected(req)) {
                    ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                }
                boolean keepAlive = HttpHeaders.isKeepAlive(req);
                
                String body = createBody();

                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body.getBytes()));
                response.headers().set(CONTENT_TYPE, "text/html");
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

                if (!keepAlive) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, Values.KEEP_ALIVE);
                    ctx.write(response);
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
    
    static public String createBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>Hello Daemon!</title></head>");
        sb.append("<body>");
        sb.append("<h1>Hi, i am an example daemon.</h1>");
        sb.append("Now: ").append(new Date()).append("<br/>");
        sb.append("Started: ").append(startedAt).append("<br/>");
        for (String line : lines) {
            sb.append(line).append("<br/>");
        }
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }
    
    static public List<String> createLines(String[] args) {
        List<String> a = new ArrayList<String>();
        a.add("working.dir: " + System.getProperty("user.dir"));
        a.add("home.dir: " + System.getProperty("user.home"));
        a.add("user.name: " + System.getProperty("user.name"));
        a.add("launcher.name: " + System.getProperty("launcher.name"));
        a.add("launcher.type: " + System.getProperty("launcher.type"));
        a.add("launcher.action: " + System.getProperty("launcher.action"));
        a.add("launcher.app.dir: " + System.getProperty("launcher.app.dir"));
        a.add("java.class.path: " + System.getProperty("java.class.path"));
        a.add("java.home: " + System.getProperty("java.home"));
        a.add("java.version: " + System.getProperty("java.version"));
        a.add("java.vendor: " + System.getProperty("java.vendor"));
        a.add("os.arch: " + System.getProperty("os.arch"));
        a.add("os.name: " + System.getProperty("os.name"));
        a.add("os.version: " + System.getProperty("os.version"));
        a.add("arguments:");
        for (String s : args) {
            a.add(" - argument: " + s);
        }
        return a;
    }
}
