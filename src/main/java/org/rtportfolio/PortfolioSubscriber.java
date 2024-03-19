package org.rtportfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class PortfolioSubscriber {
    private static Logger LOG = LoggerFactory.getLogger(RTPortfolioChecker.class);
    private static SocketChannel client;
    private static final byte[] tempSymbolBytes = new byte[RTConst.MSG_POSITION_SYMBOL_SIZE];
    private static final Map<byte[], String> byteArr2SymbolMap = new HashMap<>();
    private static int numberOfUpdate = 0;

    public static void main(String[] args) {
        try {
            client = SocketChannel.open();
            client.connect(new InetSocketAddress("localhost", 8001));
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            while (true) {
                int result = client.read(buffer);
                if (result == -1) {
                    //indicate channel disconnected
                    LOG.error("Channel disconnected");
                    break;//for simplicity
                }
                if (buffer.position() < RTConst.INIT_EXPECT_MESSAGE_LEN) {
                    continue;
                }
                buffer.mark();
                int expectedSize = buffer.getInt();
                buffer.reset();
                if (expectedSize == 0) {
                    LOG.debug("Unexpected message size"); //clear?
                }
                //read 4 already
                if (expectedSize > buffer.position() - RTConst.INIT_EXPECT_MESSAGE_LEN) {
                    LOG.info("message not received completely - expectedSize {} receivedSize {}", expectedSize, buffer.position());
                } else {
                    buffer.flip();
                    buffer.getInt();
                    //decode, could have used SBE
                    int numOfPosition = buffer.getInt();
                    LOG.info("number of position: {}", numOfPosition);
                    for (int i = 0; i < numOfPosition; i++) {
                        buffer.get(tempSymbolBytes, 0, RTConst.MSG_POSITION_SYMBOL_SIZE);
                        int pos = 0;
                        while (pos < RTConst.MSG_POSITION_SYMBOL_SIZE && tempSymbolBytes[pos] != RTConst.PAD) {
                            pos++;
                        }
                        //reuse String
                        String symbol;
                        if (byteArr2SymbolMap.containsKey(tempSymbolBytes)) {
                            symbol = byteArr2SymbolMap.get(tempSymbolBytes);
                        } else {
                            symbol = new String(tempSymbolBytes, 0, pos);
                            byteArr2SymbolMap.put(tempSymbolBytes.clone(), symbol);
                        }
                        double price = buffer.getDouble();
                        double marketValue = buffer.getDouble();
                        int qty = buffer.getInt();
                        byte isUpdatedSymbol = buffer.get();
                        buffer.get();
                        buffer.get();
                        buffer.get();
                        LOG.info("symbol: {} price: {} qty: {} marketValue: {}", symbol, price, qty, marketValue);
                    }
                    double totalNav = buffer.getDouble();
                    LOG.info("totalNav: {}", totalNav);
                    buffer.compact();
//                    LOG.info("totalNav: {} pos {} limit {} cap {}", totalNav, buffer.position(), buffer.limit(), buffer.capacity());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
