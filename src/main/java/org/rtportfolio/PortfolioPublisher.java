package org.rtportfolio;

import org.rtportfolio.model.Position;
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
import java.util.Map;

/**
 * Publish each position's market value & total portfolio NAV
 * <p>
 * 4 bytes total length | 4 bytes (indicates number of position in the portfolio)
 * 24 bytes updated symbol | 8 bytes updated price
 * 1...n - 24 bytes symbol | 8 bytes price | 4 bytes qty | 8 bytes market value |
 * 8 bytes (indicates total portfolio NAV)
 */
public class PortfolioPublisher {
    private static Logger LOG = LoggerFactory.getLogger(PortfolioPublisher.class);    //implied decimal places
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    //    private ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private boolean isStopped = false;

    private List<SocketChannel> clients;

    private final ByteBuffer bb = ByteBuffer.allocateDirect(8192);

    public PortfolioPublisher() {
        this.clients = new ArrayList<>(); //assume we have 1/ not too many clients now
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(8001));
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
                        //TODO check why multiple clients not working for now
                        SocketChannel client = serverSocketChannel.accept();
                        LOG.info("accepted a client");
                        clients.add(client);
                    }
                    selectionKeyIterator.remove();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void publishLatestPortfolio(final int messageSize, final String updatedSymbol, final double updatedPrice, final Map<String, Position> symbol2PosMap, final double portfolioNav){
        bb.clear();
        bb.putInt(messageSize);
        bb.putInt(symbol2PosMap.size()); //number of positions
        putSymbol(bb, updatedSymbol);
        bb.putDouble(updatedPrice);
        for (Map.Entry<String, Position> map : symbol2PosMap.entrySet()) {
            String symbol = map.getKey();
            Position position = map.getValue();
            putSymbol(bb, symbol);
            bb.putDouble(position.getSymbolCurrentValPerShare());
            bb.putDouble(position.getPositionMarketValue());
            bb.putInt(position.getPositionSize());
            LOG.info("symbol {} price {} qty {} market value {}", symbol, position.getSymbolCurrentValPerShare(), position.getPositionSize(), position.getPositionMarketValue());
        }
        LOG.info("Portfolio NAV: {}", portfolioNav);
        bb.putDouble(portfolioNav);
        doSend(bb);
    }

    private void putSymbol(final ByteBuffer bb, final String symbol) {
        int len = symbol.length();
        bb.put(symbol.getBytes(), 0, len);
        for (int i = len; i < RTConst.MSG_SYMBOL_SIZE; i++) {
            bb.put(RTConst.PAD);
        }
    }
    private void doSend(final ByteBuffer buffer) {

        buffer.flip();
        for (SocketChannel client : clients) {
            buffer.mark();
            while (buffer.hasRemaining()) {
                try {
                    int size = client.write(buffer);
                    LOG.info("size: {}", size);
                } catch (IOException e) {
                    //likely Broken pipe, remove
                    LOG.info("IOException encountered. Likely a client disconnected. Removing that client from the subscription list");
                    clients.remove(client);
                    return;
                }
            }
            buffer.reset();
        }
        buffer.clear();
    }

    public static void main(String[] args) {
        PortfolioPublisher p = new PortfolioPublisher();
        p.doWork();
    }
}
