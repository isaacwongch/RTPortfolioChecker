package org.rtportfolio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class PortfolioSubscriber {
    private static SocketChannel client;

    private static Selector selector;
    public static void main(String[] args) {
        try {
            client = SocketChannel.open();
//            client.configureBlocking(false);
//            selector = Selector.open();
//            client.register(selector, SelectionKey.OP_READ);
            client.connect(new InetSocketAddress("localhost", 8001));
            ByteBuffer buffer = ByteBuffer.allocate(256);

            while (true){
                int i = client.read(buffer);
                if (i > 0){
                    buffer.flip();
                    int value = buffer.getInt();
                    System.out.println(value);
                    buffer.compact();
                }
            }
//            while (true) {
//                int n = selector.select();
//                if (n == 0){
//                    continue;
//                }
//                Set<SelectionKey> selectedKeys = selector.selectedKeys();
//                Iterator<SelectionKey> iter = selectedKeys.iterator();
//                while (iter.hasNext()) {
//
//                    SelectionKey key = iter.next();
//
//                    if (key.isReadable()) {
//                        System.out.println("isReadable");
//                        buffer.flip();
//                        System.out.println(buffer.getInt());
//                        buffer.clear();
//                    }
//                    iter.remove();
//                }
//            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public static void main(String[] args) {
//        ByteBuffer bb = ByteBuffer.allocateDirect(10);
//        System.out.println("bb pos " + bb.position());
//        System.out.println("bb limit " + bb.limit());
//        System.out.println("bb capacity " + bb.capacity());
//        bb.putInt(10);
//        System.out.println("bb pos " + bb.position());
//        System.out.println("bb limit " + bb.limit());
//        System.out.println("bb capacity " + bb.capacity());
//        bb.flip();
//        System.out.println("bb pos " + bb.position());
//        System.out.println("bb limit " + bb.limit());
//        System.out.println("bb capacity " + bb.capacity());
//        bb.getInt();
//        System.out.println("bb pos " + bb.position());
//        System.out.println("bb limit " + bb.limit());
//        System.out.println("bb capacity " + bb.capacity());
//        bb.compact();
//        System.out.println("bb pos " + bb.position());
//        System.out.println("bb limit " + bb.limit());
//        System.out.println("bb capacity " + bb.capacity());
//    }
}
