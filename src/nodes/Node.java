package nodes;

import ledger.Block;
import sockets.NetworkUser;
import users.Transaction;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Node extends NetworkUser {

    static final int CHECK_TRANSACTION_DELAY_S = 5;
    static final int BLOCK_BUILD_TIME_S = 5;
    static final int MIN_TRANSACTIONS_BLOCK = 2;

    private final ArrayList<Transaction> transactions;

    private final ArrayList<Block> blocks;

    public Node(String id) {
        super(id);
        transactions = new ArrayList<>();
        blocks = new ArrayList<>();
        buildBlocks();
    }

    private void syncTransactions() {
        for (Object o: listener.getObjects()) {
            if(o instanceof Transaction t && !transactions.contains(t)) {
                transactions.add(t);
            }
        }
    }

    protected ArrayList<Transaction> getTransactions() {
        syncTransactions();
        return transactions;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    protected void buildBlocks() {
        new Thread(() -> {
            while(transactions.isEmpty()) {
                try {
                    TimeUnit.SECONDS.sleep(CHECK_TRANSACTION_DELAY_S);
                } catch (InterruptedException ignored) {}
                syncTransactions();
            }

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(transactions.size() >= MIN_TRANSACTIONS_BLOCK)
                        buildBlock();
                }
            }, BLOCK_BUILD_TIME_S * 1000, BLOCK_BUILD_TIME_S * 1000);
        }).start();
    }

    private void buildBlock() {
        syncTransactions();
        Block block = new Block(transactions);
        blocks.add(block);
        transactions.clear();
    }

    /**another 'ask' to send data*/

    /**another to maybe handshake,*/

    /**another to get version info to make sure they are all same version/etc*/

    /**Connect to other nodes**/

    /**Get connected by other nodes**/

    /**Propagate nodes*/

    /**Compare Ledgers**/

    /**Build Block**/

    /**Add Block to Ledger**/
}
