package org.rtportfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class PortfolioSubscriber {
    private static Logger LOG = LoggerFactory.getLogger(PortfolioSubscriber.class);
    private static SocketChannel client;
    private static final byte[] tempSymbolBytes = new byte[RTConst.MSG_SYMBOL_SIZE];
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
                    LOG.debug("message not received completely - expectedSize {} receivedSize {}", expectedSize, buffer.position());
                } else {
                    buffer.flip();
                    buffer.getInt();
                    //decode, could have used SBE
                    int numOfPosition = buffer.getInt();
                    int updatedSymbolLen = parseSymbol(buffer);
                    String updatedSymbol = getSymbolStr(updatedSymbolLen);
                    double updatedPrice = buffer.getDouble();
                    LOG.debug("number of position: {}", numOfPosition);
                    System.out.printf("%-2s%-1s%-5d%-19s\n", "##", " ", numberOfUpdate++, " Market Data Update");
                    System.out.printf("%-6s%-11s%-20.2f\n", updatedSymbol, " change to ", updatedPrice);
                    System.out.printf("%-22s\n", "## Portfolio");
                    System.out.printf("%-22s%-22s%-22s%-22s\n", "symbol", "price", "qty", "value");
                    for (int i = 0; i < numOfPosition; i++) {
                        int len = parseSymbol(buffer);
                        //reuse String
                        String symbol = getSymbolStr(len);
                        double price = buffer.getDouble();
                        double marketValue = buffer.getDouble();
                        int qty = buffer.getInt();
                        System.out.printf("%-22s%-22.2f%-22d%-22.2f\n", symbol, price, qty, marketValue);
//                        LOG.info("{}\t{}\t{}\t{}", symbol, price, qty, marketValue);
                    }
                    double totalNav = buffer.getDouble();
//                    LOG.info("#Symbol {} change to {}", updatedSymbol, updatedPrice);
//                    LOG.info("#Total portfolio\t\t\t{}", totalNav);
                    System.out.println();
                    System.out.printf("%-22s%-22s%-22s%-22.2f\n", "##Total portfolio", "", "", totalNav);
                    System.out.println();
                    buffer.compact();
//                    LOG.info("totalNav: {} pos {} limit {} cap {}", totalNav, buffer.position(), buffer.limit(), buffer.capacity());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int parseSymbol(final ByteBuffer buffer) {
        buffer.get(tempSymbolBytes, 0, RTConst.MSG_SYMBOL_SIZE);
        int pos = 0;
        while (pos < RTConst.MSG_SYMBOL_SIZE && tempSymbolBytes[pos] != RTConst.PAD) {
            pos++;
        }
        return pos;
    }

    private static String getSymbolStr(final int symbolLen) {
        String symbol;
        if (byteArr2SymbolMap.containsKey(tempSymbolBytes)) {
            symbol = byteArr2SymbolMap.get(tempSymbolBytes);
        } else {
            symbol = new String(tempSymbolBytes, 0, symbolLen);
            byteArr2SymbolMap.put(tempSymbolBytes.clone(), symbol);
        }
        return symbol;
    }
}
