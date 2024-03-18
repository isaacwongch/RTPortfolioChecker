package org.rtportfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Publish each position's market value & total portfolio NAV
 * <p>
 * 4 bytes total length | 4 bytes (indicates number of position in the portfolio)
 * 1...n - 24 bytes symbol | 8 bytes price | 4 bytes qty | 8 bytes market value | 1 byte boolean | 3 bytes padding |
 * 8 bytes (indicates total portfolio NAV)
 */
public class PortfolioPublisher {
    private static Logger LOG = LoggerFactory.getLogger(PortfolioPublisher.class);    //implied decimal places
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
//    private ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private boolean isStopped = false;

    private List<SocketChannel> clients;

    public PortfolioPublisher(int port){
        this.clients = new ArrayList<>(16); //assume we have 1/ not too many clients now
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread serverThread = new Thread(() -> doWork());
        serverThread.start();
    }

    public void doWork() {
        while (!isStopped) {
            try {
                int n = selector.select();
                if (n == 0) {
                    continue;
                }
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator(); //garbage address in jdk 11
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey key = selectionKeyIterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        clients.add(client);
//                        writeBuffer.clear();
//                        writeBuffer.putInt(30);
//                        writeBuffer.flip();
//                        while (writeBuffer.hasRemaining()) {
//                            System.out.println("writing");
//                            client.write(writeBuffer);
//                        }
//                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
//                        clientSocket = ssc.accept();
//                        clientSocket.configureBlocking(false);
//                        clientSocket.socket().setTcpNoDelay(true);
//                        clientSocket.register(selector, SelectionKey.OP_WRITE); //after someone connects we can send
//                        System.out.println("isAcceptable");
                    }
//                    if (key.isWritable()) {
//                        writeBuffer.clear();
//                        writeBuffer.putInt(30);
//                        writeBuffer.flip();
//                        while (writeBuffer.hasRemaining()) {
//                            System.out.println("writing");
//                            clientSocket.write(writeBuffer);
//                        }
//                    }
                    selectionKeyIterator.remove();

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void doSend(final ByteBuffer buffer) {
        for (SocketChannel client : clients) {
            buffer.flip();
            buffer.mark();
            while (buffer.hasRemaining()) {
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            buffer.reset();
        }
        buffer.clear();
    }

    public static void main(String[] args) {
        PortfolioPublisher p = new PortfolioPublisher(8081);
        p.doWork();
    }
}
